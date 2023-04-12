package com.example.testbluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class MyBluetoothService {
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private interface MessageConstants {
        int MESSAGE_READ = 0;
    }

    static public ConnectedThread makeClass(BluetoothSocket socket, Handler handler) {
        Log.i("ConnectedThread","creating thread");
        return new ConnectedThread(socket, handler);
    }

    static public class ConnectedThread extends Thread {
        private InputStream mmInStream;
        private final BluetoothSocket mSocket;
        private Handler mmHandler;
        private byte[] mmBuffer; // mmBuffer store for the stream
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
            connected=true;
        }

        public void run() {
            mmBuffer = new byte[2048];
            int numBytes; // bytes returned from read()
            Log.d(TAG, "Thread  "+this.getId());
            while (connected) {
                try {
                    // Read from the InputStream.
                    if(mmInStream!=null){
                        numBytes = mmInStream.read(mmBuffer);
                        String readMessage = new String(mmBuffer, 0, numBytes);
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("code", readMessage);
                        message.setData(bundle);
                        // Send the obtained bytes to the UI activity.
                        mmHandler.sendMessage(message);
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    try {
                        mSocket.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                }
            }
        }

        public synchronized void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
            catch (Exception e){
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}