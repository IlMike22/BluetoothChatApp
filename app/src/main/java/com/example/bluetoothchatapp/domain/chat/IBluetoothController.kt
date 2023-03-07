package com.example.bluetoothchatapp.domain.chat

import com.example.bluetoothchatapp.domain.chat.ConnectionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface IBluetoothController {
    val isConnected: StateFlow<Boolean>
    val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
    val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>

    fun startDiscovery()
    fun stopDiscovery()
    fun startBluetoothServer(): Flow<ConnectionResult>
    fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult>
    fun closeConnection()
    fun release()
}