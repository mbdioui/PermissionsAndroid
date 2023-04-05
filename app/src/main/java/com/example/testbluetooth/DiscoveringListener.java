package com.example.testbluetooth;

import android.bluetooth.BluetoothDevice;

public interface DiscoveringListener {
   void onDiscovery();
   void onStopDiscovery();

}
