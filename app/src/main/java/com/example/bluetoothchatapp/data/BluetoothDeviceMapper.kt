package com.example.bluetoothchatapp.data

import android.annotation.SuppressLint
import com.example.bluetoothchatapp.domain.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun android.bluetooth.BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}