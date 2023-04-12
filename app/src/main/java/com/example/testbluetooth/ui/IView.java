package com.example.testbluetooth.ui;

import android.bluetooth.BluetoothDevice;
import android.content.IntentSender;

public interface IView {
    void startDiscovery();
    void stopDiscovery();

    void bluetoothDeviceClick(BluetoothDevice bluetoothDevice);

    void startPairingRequest(IntentSender chooserLauncher);

    void showConnectionInfo(String device_connected);

    void PermissionNotAccorded();
}
