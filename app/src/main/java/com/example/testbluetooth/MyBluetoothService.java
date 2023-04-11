package com.example.testbluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MyBluetoothService {
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private interface MessageConstants {
        int MESSAGE_READ = 0;
    }

    public ConnectedThread makeClass(BluetoothSocket socket, Handler handler) {
        return new ConnectedThread(socket, handler);
    }

    public class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private Handler mmHandler;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket, Handler handler) {
            mmHandler = handler;
            InputStream tmpIn = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            mmInStream = tmpIn;
        }

        public void run() {
            mmBuffer = new byte[2048];
            int numBytes; // bytes returned from read()

            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    String readMessage = new String(mmBuffer, 0, numBytes);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = mmHandler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            readMessage);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }
    }
}