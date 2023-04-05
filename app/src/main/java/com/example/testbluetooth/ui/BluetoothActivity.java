package com.example.testbluetooth.ui;

import static com.example.testbluetooth.AndroidBluetoothController.REQUEST_BLUETOOTH_SCAN;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.companion.AssociationInfo;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.testbluetooth.AndroidBluetoothController;
import com.example.testbluetooth.BluetoothScannedDeviceAdapter;
import com.example.testbluetooth.ClientBluetoothSocket;
import com.example.testbluetooth.DiscoveringListener;
import com.example.testbluetooth.OnElementClickListener;
import com.example.testbluetooth.ServerBlueToothSocket;
import com.example.testbluetooth.databinding.ActivityMainBinding;

import java.util.List;
import java.util.concurrent.Executor;

public class BluetoothActivity extends AppCompatActivity implements OnElementClickListener, DiscoveringListener {

    private ActivityMainBinding binding;
    private AndroidBluetoothController androidBluetoothController;
    private BluetoothScannedDeviceAdapter adapter;

    private Handler handler;
    private int SELECT_DEVICE_REQUEST_CODE = 54646554;
    private BluetoothScannedDeviceAdapter pairedListAdapter;

    private BluetoothPresenter activityPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        handler = new Handler(msg -> {
            Toast.makeText(this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
            return true;
        });

        androidBluetoothController = new AndroidBluetoothController(this,this);
        androidBluetoothController.requestScanPermission();
        androidBluetoothController.requestBluetoothPermission();
        androidBluetoothController.activateBluetooth();

        ServerBlueToothSocket serverSocket = new ServerBlueToothSocket(androidBluetoothController.bluetoothAdapter, this);
        serverSocket.start();


        androidBluetoothController.getScannedDevicesLiveData().observe(this, observeBluetoothDevices());

        //already paired devices
        //androidBluetoothController.getPairedDevices().observe(this, observeBluetoothDevices());

        /*listAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                devicesNames);*/
        binding.PairedList.setLayoutManager(new LinearLayoutManager(this));
        pairedListAdapter = new BluetoothScannedDeviceAdapter(this);
        binding.PairedList.setAdapter(pairedListAdapter);

        binding.scanList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BluetoothScannedDeviceAdapter(this);
        binding.scanList.setAdapter(adapter);

        binding.buttonPermission.setOnClickListener(v -> {
            androidBluetoothController.startDiscovery();
        });
        binding.button.setOnClickListener(v -> {
            androidBluetoothController.stopDiscovery();
            androidBluetoothController.release();
        });

        androidBluetoothController.PairedDevicesLD.observe(this, bluetoothDevices -> pairedListAdapter.submitList(bluetoothDevices));
    }

    @NonNull
    private Observer<List<BluetoothDevice>> observeBluetoothDevices() {
        return bluetoothDevices -> adapter.submitList(bluetoothDevices);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        androidBluetoothController.getScannedDevicesLiveData().removeObservers(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AndroidBluetoothController.REQUEST_BLUETOOTH_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                androidBluetoothController.startDiscovery();
            } else {
                Toast.makeText(this, "Permission activation bluetooth non accordée", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_BLUETOOTH_SCAN && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                androidBluetoothController.activateBluetooth();
            } else {
                Toast.makeText(this, "Permission scan non accordée", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AndroidBluetoothController.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                androidBluetoothController.startDiscovery();
                androidBluetoothController.updatePairedDevice();
            }
        }
        if (requestCode == SELECT_DEVICE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                BluetoothDevice deviceToPair = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    deviceToPair = data.getParcelableExtra(
                            CompanionDeviceManager.EXTRA_DEVICE
                    );
                }

                if (deviceToPair != null) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    deviceToPair.createBond();
                    // ... Continue interacting with the paired device.
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onclick(BluetoothDevice bluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (androidBluetoothController.bluetoothAdapter.getBondedDevices().contains(bluetoothDevice)) {
            ClientBluetoothSocket connectThread = new ClientBluetoothSocket(bluetoothDevice, androidBluetoothController.bluetoothAdapter, this, handler);
            connectThread.start();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                BluetoothDeviceFilter deviceFilter = new BluetoothDeviceFilter.Builder()
                        //.setNamePattern(Pattern.compile("MBA5"))
                        .build();
                AssociationRequest pairingRequest = new AssociationRequest.Builder()
                        // Find only devices that match this request filter.
                        .addDeviceFilter(deviceFilter)
                        // Stop scanning as soon as one device matching the filter is found.
                        .setSingleDevice(false)
                        .build();

                CompanionDeviceManager deviceManager =
                        (CompanionDeviceManager) getSystemService(Context.COMPANION_DEVICE_SERVICE);

                Executor executor = Runnable::run;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    deviceManager.associate(pairingRequest, executor, new CompanionDeviceManager.Callback() {

                        // Called when a device is found. Launch the IntentSender so the user can
                        // select the device they want to pair with.
                        @Override
                        public void onDeviceFound(IntentSender chooserLauncher) {
                            try {
                                Log.i("luncher", chooserLauncher.getCreatorUserHandle().toString());
                                startIntentSenderForResult(
                                        chooserLauncher, SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0, new Bundle()
                                );
                            } catch (IntentSender.SendIntentException e) {
                                Log.e("MainActivity", "Failed to send intent");
                            }
                        }

                        @Override
                        public void onAssociationCreated(AssociationInfo associationInfo) {
                            Log.e("MainActivity", "Association created");
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
        Toast.makeText(this, "Device discovery", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStopDiscovery() {
        Toast.makeText(this, "Device stop discovery", Toast.LENGTH_SHORT).show();
    }
}