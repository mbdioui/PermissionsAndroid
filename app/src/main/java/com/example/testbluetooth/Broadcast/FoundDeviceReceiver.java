package com.example.testbluetooth.Broadcast;

import static android.bluetooth.BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED;
import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.testbluetooth.BluetoothDeviceMapper;
import com.example.testbluetooth.BluetoothDeviceObject;
import com.example.testbluetooth.BluetoothUtil;
import com.example.testbluetooth.DiscoveringListener;
import com.example.testbluetooth.ScannedDeviceListener;

public class FoundDeviceReceiver extends BroadcastReceiver {

    private final ScannedDeviceListener scannedDeviceListener;
    private DiscoveringListener discoveringListener;

    public FoundDeviceReceiver(ScannedDeviceListener scannedDeviceListener, DiscoveringListener discoveringListener) {
        this.scannedDeviceListener = scannedDeviceListener;
        this.discoveringListener = discoveringListener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Log.i("Broadcast Receiver", "action triggered" + intent.getAction() + " context is " + context);
        switch (intent.getAction()) {
            case BluetoothDevice.ACTION_FOUND: {
                Short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MAX_VALUE);
                BluetoothDeviceObject bluetoothDevice = BluetoothDeviceMapper.toBluetoothDeviceObject(device, BluetoothUtil.checkSignal(rssi));
                assert bluetoothDevice != null;
                if (bluetoothDevice.getName() != null)
                    scannedDeviceListener.onScannedDevice(device, BluetoothUtil.checkSignal(rssi));
                break;
            }
            case ACTION_DISCOVERY_STARTED: {
                discoveringListener.onDiscovery();
                break;
            }
            case ACTION_DISCOVERY_FINISHED: {
                discoveringListener.onStopDiscovery();
                break;
            }
            case ACTION_CONNECTION_STATE_CHANGED: {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_CONNECTED:
                        discoveringListener.deviceConnected();
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTED:
                        discoveringListener.deviceDisconnected();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        discoveringListener.onConnectionState();
                        break;
                }
                break;
            }
            default:
                break;
        }
    }
}


