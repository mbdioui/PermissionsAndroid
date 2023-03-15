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
            BluetoothDeviceObject bluetoothDevice = new BluetoothDeviceObject();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                bluetoothDevice.name = intent.getParcelableExtra(BluetoothDevice.EXTRA_NAME, String.class);
                bluetoothDevice.address = intent.getParcelableExtra(BluetoothDevice.EXTRA_UUID, String.class);
            } else {
                bluetoothDevice.name = intent.getParcelableExtra(BluetoothDevice.EXTRA_NAME);
                bluetoothDevice.address = intent.getParcelableExtra(BluetoothDevice.EXTRA_UUID);
            }
            if (bluetoothDevice.name != null)
                scannedDeviceListener.onScannedDevice(bluetoothDevice);
        }
    }
}


