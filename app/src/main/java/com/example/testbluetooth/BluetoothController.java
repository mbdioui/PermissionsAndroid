package com.example.testbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;

import java.util.List;

public interface BluetoothController {



    void startDiscovery();

    void stopDiscovery();

    void release();

    void activateBluetooth();
}
