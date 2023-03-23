package com.example.testbluetooth;


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

}
