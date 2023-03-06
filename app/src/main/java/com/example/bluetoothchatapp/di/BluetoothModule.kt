package com.example.bluetoothchatapp.di

import android.content.Context
import com.example.bluetoothchatapp.data.BluetoothController
import com.example.bluetoothchatapp.domain.IBluetoothController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BluetoothModule {
    @Provides
    @Singleton
    fun provideBluetoothController(@ApplicationContext context: Context): IBluetoothController {
        return BluetoothController(context)
    }
}