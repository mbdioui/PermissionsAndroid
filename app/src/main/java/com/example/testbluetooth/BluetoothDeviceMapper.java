package com.example.testbluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import org.jetbrains.annotations.Nullable;

public class BluetoothDeviceMapper {

    @SuppressLint("MissingPermission")
    @Nullable
    static public BluetoothDeviceObject toBluetoothDeviceObject(BluetoothDevice device) {
        if (device == null) {
            return null;
        }
        BluetoothDeviceObject deviceObject = new BluetoothDeviceObject();
        deviceObject.name = device.getName();
        deviceObject.address = device.getAddress().isEmpty() ? "adresse non accessible" : device.getAddress();
        return deviceObject;
    }
}
