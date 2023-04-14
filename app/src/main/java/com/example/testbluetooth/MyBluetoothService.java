package com.example.testbluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MyBluetoothService {
    private static final String TAG = "MY_APP_DEBUG_TAG";

    private interface MessageConstants {
        int MESSAGE_READ = 0;
    }

    static public ConnectedThread makeClass(BluetoothSocket socket, Handler handler) {
        Log.i("ConnectedThread", "creating thread");
        return new ConnectedThread(socket, handler);
    }

    static public class ConnectedThread extends Thread {
        private InputStream mmInStream;
        private final BluetoothSocket mSocket;
        private Handler mmHandler;
        private boolean connected;


        public ConnectedThread(BluetoothSocket socket, Handler handler) {
            mmHandler = handler;
            mSocket = socket;
            InputStream tmpIn = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            mmInStream = tmpIn;
            connected = true;
        }

        public void run() {
            Log.d(TAG, "Thread  " + this.getId());
            while (connected) {
                // Read from the InputStream.
                InputStreamReader isReader = new InputStreamReader(mmInStream);
                try {
                    if (mmInStream != null && isReader.ready()) {
                        char[] charArray = new char[(int) 20];
                        isReader.read(charArray);
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("code", String.valueOf(charArray));
                        message.setData(bundle);
                        mmHandler.sendMessage(message);
                    }
                } catch (IOException e) {
                    closeSocket();
                    throw new RuntimeException(e);
                }
            }
        }

        public synchronized void cancel() {
            closeSocket();
        }

        public void closeSocket() {
            try {
                mSocket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}