package com.example.testbluetooth.ui;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import androidx.lifecycle.LiveData;

import java.util.List;

public interface BluetoothPresenterInterface {

    LiveData<List<BluetoothDevice>> getScannedDeviceLD();

    LiveData<List<BluetoothDevice>> getPairedDeviceLD();

    void startDiscovery();
    void stopDiscovery();


    void onBlueToothDeviceClick(BluetoothDevice bluetoothDevice, Handler handler);

    void startSocketServer();
    
    void activateBluetooth();

    void updatePairedDevices();

    void closeSockets();
}
