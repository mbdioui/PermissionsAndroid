package com.example.testbluetooth.ui;

import static com.example.testbluetooth.AndroidBluetoothController.REQUEST_BLUETOOTH_SCAN;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.companion.CompanionDeviceManager;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.testbluetooth.AndroidBluetoothController;
import com.example.testbluetooth.BluetoothScannedDeviceAdapter;
import com.example.testbluetooth.databinding.ActivityMainBinding;

public class BluetoothActivity extends AppCompatActivity implements IView {

    private ActivityMainBinding binding;

    private BluetoothScannedDeviceAdapter scanListAdapter;

    private Handler handler;

    private BluetoothScannedDeviceAdapter pairedListAdapter;

    private BluetoothPresenter activityPresenter;

    @Override
    public void startPairingRequest(IntentSender chooserLauncher) {
        try {
            Log.i("MainActivity", chooserLauncher.getCreatorUserHandle().toString());
            startIntentSenderForResult(
                    chooserLauncher, activityPresenter.SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0, new Bundle()
            );
        } catch (IntentSender.SendIntentException e) {
            Log.e("MainActivity", "Failed to send intent");
        }
    }

    @Override
    public void showConnectionInfo(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void PermissionNotAccorded() {
        Toast.makeText(this,"Please verify that the permissions has been accorded",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        handler = new Handler(msg -> {
            Bundle b = msg.getData();
            binding.textViewScanned.setText(b.getString("code"));
            return true;
        });

        activityPresenter = new BluetoothPresenter(handler, this);

        activityPresenter.startSocketServer();

        activityPresenter.getScannedDeviceLD().observe(this, bluetoothDevices -> scanListAdapter.submitList(bluetoothDevices));

        binding.PairedList.setLayoutManager(new LinearLayoutManager(this));
        pairedListAdapter = new BluetoothScannedDeviceAdapter(activityPresenter);
        binding.PairedList.setAdapter(pairedListAdapter);

        binding.scanList.setLayoutManager(new LinearLayoutManager(this));
        scanListAdapter = new BluetoothScannedDeviceAdapter(activityPresenter);
        binding.scanList.setAdapter(scanListAdapter);

        binding.buttonScan.setOnClickListener(v -> activityPresenter.startDiscovery());
        binding.buttonStopScan.setOnClickListener(v -> activityPresenter.stopDiscovery());

        activityPresenter.getPairedDeviceLD().observe(this, bluetoothDevices -> pairedListAdapter.submitList(bluetoothDevices));
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityPresenter.getPairedDeviceLD().removeObservers(this);
        activityPresenter.getScannedDeviceLD().removeObservers(this);
        activityPresenter.closeSockets();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AndroidBluetoothController.REQUEST_BLUETOOTH_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activityPresenter.startDiscovery();
            } else {
                Toast.makeText(this, "Permission activation bluetooth non accordée", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_BLUETOOTH_SCAN && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activityPresenter.activateBluetooth();
            } else {
                Toast.makeText(this, "Permission scan non accordée", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AndroidBluetoothController.REQUEST_ENABLE_BT) {
            handleAcceptEnableBluetooth(resultCode);
        }
        if (requestCode == activityPresenter.SELECT_DEVICE_REQUEST_CODE) {
            handleRequestPairing(resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleRequestPairing(int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            BluetoothDevice deviceToPair = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                deviceToPair = data.getParcelableExtra(
                        CompanionDeviceManager.EXTRA_DEVICE
                );
            }

            if (deviceToPair != null) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                deviceToPair.createBond();
            }
        }
    }

    private void handleAcceptEnableBluetooth(int resultCode) {
        if (resultCode == RESULT_OK) {
            activityPresenter.startDiscovery();
            activityPresenter.updatePairedDevices();
        }
    }


    @Override
    public void startDiscovery() {
        activityPresenter.startDiscovery();
    }

    @Override
    public void stopDiscovery() {
        activityPresenter.stopDiscovery();
    }

    @Override
    protected void onResume() {
        Log.i("activity","onResume");
        activityPresenter.updatePairedDevices();
        super.onResume();
    }

    @Override
    public void bluetoothDeviceClick(BluetoothDevice bluetoothDevice) {
        activityPresenter.onBlueToothDeviceClick(bluetoothDevice,handler);
    }
}