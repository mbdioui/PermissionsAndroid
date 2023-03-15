package com.example.testbluetooth;

import static com.example.testbluetooth.AndroidBluetoothController.REQUEST_BLUETOOTH_SCAN;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.example.testbluetooth.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AndroidBluetoothController androidBluetoothController;
    private List<String> devicesNames = new ArrayList();
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        androidBluetoothController = new AndroidBluetoothController(this);
        androidBluetoothController.requestScanPermission();
        androidBluetoothController.requestBluetoothPermission();
        androidBluetoothController.activateBluetooth();
        androidBluetoothController.startDiscovery();
        androidBluetoothController.getScannedDevicesLiveData().observe(this, observeBluetoothDevices());

        listAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                devicesNames);
        binding.listView.setAdapter(listAdapter);

        binding.button.setOnClickListener(v -> {
            androidBluetoothController.stopDiscovery();
            androidBluetoothController.release();
        });

    }

    @NonNull
    private Observer<List<BluetoothDeviceObject>> observeBluetoothDevices() {
        return bluetoothDevices -> {
            devicesNames.clear();
            for (BluetoothDeviceObject device : bluetoothDevices) {
                devicesNames.add(device.name);
            }
            listAdapter.notifyDataSetChanged();
        };
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
            }
        }
    }

}