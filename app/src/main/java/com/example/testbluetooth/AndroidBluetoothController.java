package com.example.testbluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressLint("MissingPermission")
public class AndroidBluetoothController implements BluetoothController, ScannedDeviceListener {

    public static final int REQUEST_BLUETOOTH_PERMISSION = 212;
    public static final int REQUEST_BLUETOOTH_SCAN = 31312;
    public static final int REQUEST_ENABLE_BT = 312;
    private final ActivityResultRegistry mActivityResultRegistry;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;

    private List<String> scannedDevices;


    private MutableLiveData<List<String>> _scannedDevicesLiveData = new MutableLiveData();

    public LiveData<List<String>> getScannedDevicesLiveData() {
        return _scannedDevicesLiveData;
    }

    List<BluetoothDeviceObject> pairedDevices = null;
    private Context mContext;
    private Activity mActivity;
    private ActivityResultLauncher<Intent> launcher;

    private final FoundDeviceReceiver foundDeviceReceiver = new FoundDeviceReceiver(this);


    public AndroidBluetoothController(Context applicationContext, ActivityResultRegistry activityResultRegistry) {
        mContext = applicationContext;
        mActivity = (Activity) applicationContext;
        mActivityResultRegistry = activityResultRegistry;
        bluetoothManager = applicationContext.getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        scannedDevices = new ArrayList<>();
        updatePairedDevice();
    }


    @Override
    public void startDiscovery() {
        if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT) && hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            mContext.registerReceiver(foundDeviceReceiver, filter);
            bluetoothAdapter.startDiscovery();
        }
    }

    @Override
    public void stopDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return;
        }
        bluetoothAdapter.cancelDiscovery();

    }

    @Override
    public void release() {
        if (foundDeviceReceiver.isOrderedBroadcast())
            mContext.unregisterReceiver(foundDeviceReceiver);
    }


    private void updatePairedDevice() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return;
        }
        pairedDevices = new ArrayList<>();
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            pairedDevices.add(BluetoothDeviceMapper.toBluetoothDeviceObject(device));
        }
    }

    private boolean hasPermission(String permission) {
        return mContext.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onScannedDevice(BluetoothDeviceObject bluetoothDevice) {
        if (bluetoothDevice != null) {
            scannedDevices.add(bluetoothDevice.name);
            scannedDevices = scannedDevices.stream().distinct().collect(Collectors.toList());
        }
        _scannedDevicesLiveData.setValue(scannedDevices);
    }

    @Override
    public void activateBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            // Demander à l'utilisateur d'activer le Bluetooth
            if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    public void requestBluetoothPermission() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_BLUETOOTH_PERMISSION);
    }
    @Override
    public void requestScanPermission() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, REQUEST_BLUETOOTH_SCAN);
        }
    }
}
