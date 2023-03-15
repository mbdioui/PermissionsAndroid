package com.example.testbluetooth;


import androidx.annotation.Nullable;

public class BluetoothDeviceObject {
    String name;
    String address;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof BluetoothDeviceObject && ((BluetoothDeviceObject) obj).name.equals(name))
            return true;
        return false;
    }

}
