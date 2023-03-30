package com.example.testbluetooth;

import android.bluetooth.BluetoothDevice;

public interface ScannedDeviceListener {
   void onScannedDevice(BluetoothDevice bluetoothDevice,StrengthSignal strength);

}
