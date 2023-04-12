package com.example.testbluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testbluetooth.databinding.ItemBluetoothBinding;

import java.util.List;

public class BluetoothScannedDeviceAdapter extends RecyclerView.Adapter<BluetoothScannedDeviceAdapter.BluetoothDeviceVH> {

    private OnElementClickListener onElementClickListener;
    List<BluetoothDevice> bluetoothDevices;


    @NonNull
    @Override
    public BluetoothDeviceVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBluetoothBinding binding = ItemBluetoothBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BluetoothDeviceVH(binding);
    }

    public BluetoothScannedDeviceAdapter(OnElementClickListener onElementClickListener) {
        this.onElementClickListener = onElementClickListener;
    }


    public void submitList(List<BluetoothDevice> bluetoothDevices) {
        this.bluetoothDevices = bluetoothDevices;
        notifyDataSetChanged();
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull BluetoothDeviceVH holder, int position) {
        BluetoothDevice bluetoothDevice = bluetoothDevices.get(position);
        holder.binding.bluetoothName.setText(bluetoothDevice.getName());
        holder.binding.bluetoothUdid.setText(bluetoothDevice.getAddress());
        holder.binding.getRoot().setOnClickListener(onClickEvent(bluetoothDevice));
    }

    private View.OnClickListener onClickEvent( BluetoothDevice bluetoothDevice) {
        return v -> onElementClickListener.onclick(bluetoothDevice);
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices == null ? 0 : bluetoothDevices.size();
    }


    public class BluetoothDeviceVH extends RecyclerView.ViewHolder {
        public ItemBluetoothBinding binding;

        public BluetoothDeviceVH(ItemBluetoothBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
