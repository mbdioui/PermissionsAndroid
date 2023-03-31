package com.example.testbluetooth;

import static com.example.testbluetooth.ServerBlueToothSocket.DEFAULT_SPP_UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;

public class ConnectThread extends Thread {
    private BluetoothSocket mmSocket;
    private Context mContext;

    private Handler handler = new Handler();

    private InputStream mmInStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    private final BluetoothAdapter mmBluetoothAdapter;

    public ConnectThread(BluetoothDevice device, BluetoothAdapter bluetoothAdapter, Context context) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        Log.i("client socket", "creating rf common socket");
        BluetoothSocket tmp = null;
        mmBluetoothAdapter = bluetoothAdapter;
        this.mContext = context;
        try {
            Log.i("client socket", "creating rf common socket");
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e("client side thread", "checking if permissions is granted");
                return;
            }
            tmp = device.createInsecureRfcommSocketToServiceRecord(DEFAULT_SPP_UUID);
            //tmp = device.createRfcommSocketToServiceRecord(ServerBlueToothSocket.DEFAULT_SPP_UUID);
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

            MyBluetoothService bluetoothService = new MyBluetoothService();
            MyBluetoothService.ConnectedThread thread = bluetoothService.makeClass(mmSocket, handler);
            thread.run();


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

    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e("client side thread", "Could not close the client socket", e);
        }
    }
}
