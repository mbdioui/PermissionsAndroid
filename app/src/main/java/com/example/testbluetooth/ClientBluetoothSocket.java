package com.example.testbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;

public class ClientBluetoothSocket extends Thread {
    private BluetoothSocket mmSocket;
    private Context mContext;

    private Handler handler;

    private BluetoothAdapter mmBluetoothAdapter;

    private MyBluetoothService.ConnectedThread mInputStreamThread;


    public ClientBluetoothSocket(BluetoothDevice device, BluetoothAdapter bluetoothAdapter, Context context, Handler handler) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.

        Log.i("client socket", "creating rf common socket");
        BluetoothSocket tmp = null;
        mmBluetoothAdapter = bluetoothAdapter;
        this.handler = handler;
        this.mContext = context;
        try {
            Log.i("client socket", "creating rf common socket");
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e("client side thread", "checking if permissions is granted");
                return;
            }
            //tmp = bluetoothAdapter.getRemoteDevice(device.getAddress()).createInsecureRfcommSocketToServiceRecord(DEFAULT_SPP_UUID);
            // tmp = device.createInsecureRfcommSocketToServiceRecord(DEFAULT_SPP_UUID);
            tmp = device.createRfcommSocketToServiceRecord(ServerBlueToothSocket.DEFAULT_SPP_UUID);
        } catch (IOException e) {
            Log.e("client side thread", "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mmBluetoothAdapter.cancelDiscovery();

        try {
            Log.i("client socket", "connecting to bluetooth socket");
            mmSocket.connect();

        } catch (IOException connectException) {
            Log.i("client socket", "error while connecting to bluetooth socket");
            Log.i("client socket", connectException.getMessage());
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e("client thread", "Could not close the client socket", closeException);
            }
            return;
        }
        mInputStreamThread = MyBluetoothService.makeClass(mmSocket, handler);
        mInputStreamThread.run();

    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            if (mInputStreamThread != null)
                mInputStreamThread.cancel();
            mmSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
