package com.example.bluetoothchatapp.data.chat

import com.example.bluetoothchatapp.domain.chat.BluetoothMessage

fun String.toBluetoothMessage(isFromLocalUser: Boolean): BluetoothMessage {
    val senderName = substringBeforeLast("#")
    val message = substringAfterLast("#")
    return BluetoothMessage(
        message = message,
        senderName = senderName,
        isFromLocalUser = isFromLocalUser
    )

}

fun BluetoothMessage.toByteArray(): ByteArray {
    return "$senderName#$message".encodeToByteArray()
}