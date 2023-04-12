package com.example.testbluetooth;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.Nullable;

enum StrengthSignal {
    STRONG, GOOD, LOW, POOR
}

public class BluetoothDeviceObject {
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public StrengthSignal getStrength() {
        return strength;
    }

    public void setStrength(StrengthSignal strength) {
        this.strength = strength;
    }

    String address;
    StrengthSignal strength;


    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof BluetoothDeviceObject && ((BluetoothDeviceObject) obj).name.equals(name);
    }


    @SuppressLint("MissingPermission")
    BluetoothDeviceObject fromBluetoothDevice(BluetoothDevice bluetoothDevice, StrengthSignal strength) {
        BluetoothDeviceObject object = new BluetoothDeviceObject();
        object.name = bluetoothDevice.getName();
        object.address = bluetoothDevice.getAddress();
        if (strength != null)
            object.strength = strength;
        return object;
    }

}
