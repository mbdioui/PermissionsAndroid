package com.example.testbluetooth;

import static android.bluetooth.BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED;
import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED;
import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;

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

import androidx.activity.result.contract.ActivityResultContract;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.testbluetooth.Broadcast.FoundDeviceReceiver;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("MissingPermission")
public class AndroidBluetoothController implements BluetoothController, ScannedDeviceListener {

    public static final int REQUEST_BLUETOOTH_PERMISSION = 212;
    public static final int REQUEST_BLUETOOTH_SCAN = 31312;
    public static final int REQUEST_ENABLE_BT = 312;
    BluetoothManager bluetoothManager;
    public BluetoothAdapter bluetoothAdapter;

    private List<BluetoothDevice> scannedDevices;


    private MutableLiveData<List<BluetoothDevice>> _scannedDevicesLiveData = new MutableLiveData();
    private MutableLiveData<List<BluetoothDevice>> _pairedDevices = new MutableLiveData();


    public LiveData<List<BluetoothDevice>> pairedDevicesLD = _pairedDevices;
    public LiveData<List<BluetoothDevice>> scannedDevicesLD = _scannedDevicesLiveData;



    private Context mContext;
    private Activity mActivity;

    private FoundDeviceReceiver foundDeviceReceiver ;


    public AndroidBluetoothController(Context applicationContext,DiscoveringListener discoveringListener) {
        mContext = applicationContext;
        foundDeviceReceiver=new FoundDeviceReceiver(this,discoveringListener);
        mActivity = (Activity) applicationContext;
        bluetoothManager = applicationContext.getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        scannedDevices = new ArrayList<>();
        updatePairedDevice();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(ACTION_DISCOVERY_STARTED);
        filter.addAction(ACTION_DISCOVERY_FINISHED);
        filter.addAction(ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mContext.registerReceiver(foundDeviceReceiver, filter);
    }


    @Override
    public void startDiscovery() {
        if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT) && hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
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


    public void updatePairedDevice() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return;
        }
        List<BluetoothDevice> tmp = new ArrayList<>();
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            if (tmp != null)
                if (device != null && !tmp.contains(device)) {
                    tmp.add(device);
                    _pairedDevices.setValue(tmp);
                }
        }
    }

    private boolean hasPermission(String permission) {
        return mContext.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onScannedDevice(BluetoothDevice bluetoothDevice, StrengthSignal strength) {
        if (bluetoothDevice != null && !scannedDevices.contains(bluetoothDevice)) {
            scannedDevices.add(bluetoothDevice);
            _scannedDevicesLiveData.setValue(scannedDevices);
        }
    }

    @Override
    public void activateBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
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
