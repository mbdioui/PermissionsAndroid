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
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;

import com.example.testbluetooth.AndroidBluetoothController;
import com.example.testbluetooth.ClientBluetoothSocket;
import com.example.testbluetooth.DiscoveringListener;
import com.example.testbluetooth.OnElementClickListener;
import com.example.testbluetooth.ServerBlueToothSocket;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

public class BluetoothPresenter implements BluetoothPresenterInterface, DiscoveringListener, OnElementClickListener {
    private IView bluetoothView;

    private Handler handler;

    public int SELECT_DEVICE_REQUEST_CODE = 54646554;
    private AndroidBluetoothController androidBluetoothController;

    private ServerBlueToothSocket serverSocket;
    private ClientBluetoothSocket clientSocket;


    BluetoothPresenter(Handler handler, IView context) {
        bluetoothView = context;
        this.handler = handler;

        androidBluetoothController = new AndroidBluetoothController((Context) bluetoothView, this);
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
        if (ActivityCompat.checkSelfPermission((Context) bluetoothView, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            bluetoothView.PermissionNotAccorded();
            return;
        }
        androidBluetoothController.stopDiscovery();

        if (androidBluetoothController.bluetoothAdapter.getBondedDevices().contains(bluetoothDevice)) {
            if (clientSocket == null) {
                clientSocket = new ClientBluetoothSocket(bluetoothDevice, androidBluetoothController.bluetoothAdapter, (Context) bluetoothView, handler);
                clientSocket.start();
            }
            else{
                clientSocket.cancel();
                clientSocket = null;
            }

        } else {
            handlePairingRequest();
        }
    }

    private void handlePairingRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            BluetoothDeviceFilter deviceFilter = new BluetoothDeviceFilter.Builder()
                    .setNamePattern(Pattern.compile("OBID"))
                    .build();
            AssociationRequest pairingRequest = new AssociationRequest.Builder()
                    .addDeviceFilter(deviceFilter)
                    .setSingleDevice(true)
                    .build();
            Context context = (Context) bluetoothView;

            CompanionDeviceManager deviceManager =
                    (CompanionDeviceManager) context.getSystemService(Context.COMPANION_DEVICE_SERVICE);

            Executor executor = Runnable::run;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                deviceManager.associate(pairingRequest, executor, new CompanionDeviceManager.Callback() {

                    @Override
                    public void onDeviceFound(IntentSender chooserLauncher) {
                        Log.i("MainActivity", "on device found");
                        bluetoothView.startPairingRequest(chooserLauncher);
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

    @Override
    public void onDiscovery() {
        Toast.makeText((Context) bluetoothView, "Discovery mode On", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStopDiscovery() {
        Toast.makeText((Context) bluetoothView, "Discovery mode Off", Toast.LENGTH_SHORT).show();
        updatePairedDevices();
    }

    @Override
    public void onConnectionState() {
        updatePairedDevices();
        bluetoothView.showConnectionInfo("Bluetooth turned off");
    }

    @Override
    public void deviceConnected() {
        updatePairedDevices();
        bluetoothView.showConnectionInfo("device connected");
    }

    @Override
    public void deviceDisconnected() {
        bluetoothView.showConnectionInfo("device disconnected");
    }

    @Override
    public void startSocketServer() {
        serverSocket = new ServerBlueToothSocket(androidBluetoothController.bluetoothAdapter, (Context) bluetoothView);
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

    @Override
    public void closeSockets() {
        if (clientSocket != null)
            clientSocket.cancel();
        serverSocket.cancel();
    }

}
