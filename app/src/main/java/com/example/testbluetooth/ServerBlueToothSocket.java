package com.example.testbluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.UUID;

public class ServerBlueToothSocket extends Thread {
    public final String name = "bluetooth Server";

    public static UUID DEFAULT_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothServerSocket mmServerSocket;


    public ServerBlueToothSocket(BluetoothAdapter bluetoothAdapter, Context context) {
        BluetoothServerSocket tmp = null;

        try {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return ;
            }
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(name, DEFAULT_SPP_UUID);
            Log.i("server socket","listening server side");
        } catch (IOException e) {
            Log.e("ACCEPT bluetooth Thread", "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                Log.i("server socket","accepting bluetooth sockets communications");
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e("ACCEPT bluetooth Thread", "Socket's accept() method failed", e);
                break;
            }
            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                //mmServerSocket.close();
                break;
            }
        }
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e("ACCEPT bluetooth Thread", "Could not close the connect socket", e);
        }
    }
}
