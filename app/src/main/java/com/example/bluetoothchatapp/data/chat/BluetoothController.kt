package com.example.bluetoothchatapp.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import com.example.bluetoothchatapp.data.toBluetoothDeviceDomain
import com.example.bluetoothchatapp.domain.chat.BluetoothDevice
import com.example.bluetoothchatapp.domain.chat.BluetoothDeviceDomain
import com.example.bluetoothchatapp.domain.chat.ConnectionResult
import com.example.bluetoothchatapp.domain.chat.IBluetoothController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.util.*

@SuppressLint("MissingPermission")
class BluetoothController(
    private val context: Context
) : IBluetoothController {
    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())

    private val _isConnected = MutableStateFlow<Boolean>(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }
    }

    private val bluetoothStateReceiver = BluetoothStateReceiver { isConnected, bluetoothDevice ->
    }

    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null

    init {
        updatePairedDevices()
    }

    override fun startDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(android.bluetooth.BluetoothDevice.ACTION_FOUND)
        )
        updatePairedDevices()
        bluetoothAdapter?.startDiscovery()
    }

    override fun stopDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT).not()) {
                throw SecurityException("No bluetooth connect permission")
            }
            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "chat_service",
                UUID.fromString(SERVICE_UUID)
            )
            var shouldLoop = true
            while (shouldLoop) {
                currentClientSocket = try {
                    currentServerSocket?.accept()

                } catch (exception: IOException) {
                    shouldLoop = false
                    null
                }
                emit(ConnectionResult.ConnectionEstablished)
                currentClientSocket?.let {
                    currentServerSocket?.close()
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No bluetooth connect permission")
            }

            val bluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address)

            currentClientSocket = bluetoothDevice
                ?.createInsecureRfcommSocketToServiceRecord(
                    UUID.fromString(SERVICE_UUID)
                )
            stopDiscovery()

            if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == false) {

            }
            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)

                } catch (exception: IOException) {
                    socket.close()
                    currentClientSocket = null
                    emit(ConnectionResult.Error("Connection was interrupted!"))
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun closeConnection() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentServerSocket = null
        currentClientSocket = null
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        closeConnection()
    }

    private fun updatePairedDevices() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        bluetoothAdapter
            ?.bondedDevices
            ?.map {
                it.toBluetoothDeviceDomain()
            }
            ?.also { devices ->
                _pairedDevices.update { devices }
            }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val SERVICE_UUID = "86c0378a-6ef7-4c69-9c9a-500a7b51f945"
    }
}