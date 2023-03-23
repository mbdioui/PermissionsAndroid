package com.example.testbluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FoundDeviceReceiver extends BroadcastReceiver {

    private final ScannedDeviceListener scannedDeviceListener;

    public FoundDeviceReceiver(ScannedDeviceListener scannedDeviceListener) {
        this.scannedDeviceListener = scannedDeviceListener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MAX_VALUE);
            BluetoothDeviceObject bluetoothDevice = BluetoothDeviceMapper.toBluetoothDeviceObject(device);
            bluetoothDevice.strength = BluetoothUtil.checkSignal(rssi);
            if (bluetoothDevice.name != null)
                scannedDeviceListener.onScannedDevice(bluetoothDevice);
        }
    }
}


