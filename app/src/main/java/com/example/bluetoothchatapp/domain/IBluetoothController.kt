package com.example.bluetoothchatapp.domain

import kotlinx.coroutines.flow.StateFlow

interface IBluetoothController {
    val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
    val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>

    fun startDiscovery()
    fun stopDiscovery()
    fun release()
}