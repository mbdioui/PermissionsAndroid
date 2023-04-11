package com.example.testbluetooth.ui;


import android.bluetooth.BluetoothDevice;
import android.companion.AssociationInfo;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;

import com.example.testbluetooth.AndroidBluetoothController;
import com.example.testbluetooth.ClientBluetoothSocket;
import com.example.testbluetooth.DiscoveringListener;
import com.example.testbluetooth.OnElementClickListener;
import com.example.testbluetooth.ServerBlueToothSocket;
import com.example.testbluetooth.databinding.ActivityMainBinding;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

public class BluetoothPresenter implements BluetoothPresenterInterface, DiscoveringListener, OnElementClickListener {
    private IView viewContext;

    private Handler handler;

    public int SELECT_DEVICE_REQUEST_CODE = 54646554;
    private AndroidBluetoothController androidBluetoothController;


    BluetoothPresenter(Handler handler, IView context) {
        viewContext = context;
        this.handler = handler;

        androidBluetoothController = new AndroidBluetoothController((Context) viewContext, this);
        androidBluetoothController.requestScanPermission();
        androidBluetoothController.requestBluetoothPermission();
        androidBluetoothController.activateBluetooth();
    }

    @Override
    public LiveData<List<BluetoothDevice>> getScannedDeviceLD() {
        return androidBluetoothController.scannedDevicesLD;
    }

    @Override
    public LiveData<List<BluetoothDevice>> getPairedDeviceLD() {
        return androidBluetoothController.pairedDevicesLD;
    }


    @Override
    public void startDiscovery() {
        androidBluetoothController.startDiscovery();
    }

    @Override
    public void stopDiscovery() {
        androidBluetoothController.stopDiscovery();
    }

    @Override
    public void updatePairedDevices() {
        androidBluetoothController.updatePairedDevice();
    }

    @Override
    public void onBlueToothDeviceClick(BluetoothDevice bluetoothDevice, Handler handler) {
        if (ActivityCompat.checkSelfPermission((Context) viewContext, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        androidBluetoothController.stopDiscovery();
        Log.i("BluetoothPresenter", "asking device " + bluetoothDevice.getName() + " to pair/to connect");
        if (androidBluetoothController.bluetoothAdapter.getBondedDevices().contains(bluetoothDevice)) {
            ClientBluetoothSocket connectThread = new ClientBluetoothSocket(bluetoothDevice, androidBluetoothController.bluetoothAdapter, (Context) viewContext, handler);
            connectThread.start();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                BluetoothDeviceFilter deviceFilter = new BluetoothDeviceFilter.Builder()
                        .setNamePattern(Pattern.compile("S21 FE de Mohamed Salah"))
                        .build();
                AssociationRequest pairingRequest = new AssociationRequest.Builder()
                        .addDeviceFilter(deviceFilter)
                        .setSingleDevice(true)
                        .build();
                Context context = (Context) viewContext;

                CompanionDeviceManager deviceManager =
                        (CompanionDeviceManager) context.getSystemService(Context.COMPANION_DEVICE_SERVICE);

                Executor executor = Runnable::run;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    deviceManager.associate(pairingRequest, executor, new CompanionDeviceManager.Callback() {

                        @Override
                        public void onDeviceFound(IntentSender chooserLauncher) {
                            Log.i("MainActivity", "on device found");
                            viewContext.startPairingRequest(chooserLauncher);
                        }

                        @Override
                        public void onAssociationCreated(AssociationInfo associationInfo) {
                            Log.e("MainActivity", "Association created" + associationInfo.toString());
                        }

                        @Override
                        public void onFailure(CharSequence errorMessage) {
                            Log.e("MainActivity", "Failed to associate");
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onDiscovery() {
        Toast.makeText((Context) viewContext, "Discovery mode On", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStopDiscovery() {
        Toast.makeText((Context) viewContext, "Discovery mode Off", Toast.LENGTH_SHORT).show();
        updatePairedDevices();
    }

    @Override
    public void onConnectionState() {
        updatePairedDevices();
    }

    @Override
    public void startSocketServer() {
        ServerBlueToothSocket serverSocket = new ServerBlueToothSocket(androidBluetoothController.bluetoothAdapter, (Context) viewContext);
        serverSocket.start();
    }

    @Override
    public void activateBluetooth() {
        androidBluetoothController.activateBluetooth();
    }

    @Override
    public void onclick(BluetoothDevice bluetoothDevice) {
        onBlueToothDeviceClick(bluetoothDevice, handler);
    }
}
