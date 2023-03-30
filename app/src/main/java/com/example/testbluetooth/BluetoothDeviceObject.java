package com.example.testbluetooth;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.Nullable;

enum StrengthSignal {
    STRONG, GOOD, LOW, POOR
}

public class BluetoothDeviceObject {
    String name;
    String address;
    StrengthSignal strength;


    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof BluetoothDeviceObject && ((BluetoothDeviceObject) obj).name.equals(name);
    }


    @SuppressLint("MissingPermission")
    BluetoothDeviceObject fromBluetoothDevice(BluetoothDevice bluetoothDevice,StrengthSignal strength) {
        BluetoothDeviceObject object = new BluetoothDeviceObject();
        object.name=bluetoothDevice.getName();
        object.address=bluetoothDevice.getAddress();
        object.strength=strength;
        return object;
    }

}
