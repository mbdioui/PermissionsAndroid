package com.example.testbluetooth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testbluetooth.databinding.ItemBluetoothBinding;

import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.BluetoothDeviceVH> {

    List<BluetoothDeviceObject> bluetoothDevices;


    @NonNull
    @Override
    public BluetoothDeviceVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBluetoothBinding binding = ItemBluetoothBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BluetoothDeviceVH(binding);
    }

    public void submitList(List<BluetoothDeviceObject> bluetoothDevices) {
        this.bluetoothDevices = bluetoothDevices;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull BluetoothDeviceVH holder, int position) {
        BluetoothDeviceObject bluetoothDevice = bluetoothDevices.get(position);
        holder.binding.bluetoothName.setText(bluetoothDevice.name);
        holder.binding.bluetoothUdid.setText(bluetoothDevice.address);
        switch (bluetoothDevice.strength) {
            case STRONG:
                holder.binding.bluetoothStatus.setImageResource(R.drawable.good_signal);
                break;
            case GOOD:
                holder.binding.bluetoothStatus.setImageResource(R.drawable.medium_signal);
                break;
            case LOW:
                holder.binding.bluetoothStatus.setImageResource(R.drawable.poor_signal);
                break;
            default:
                holder.binding.bluetoothStatus.setImageResource(R.drawable.useless_signal);
                break;
        }
        holder.binding.getRoot().setOnClickListener(onClickEvent(holder, bluetoothDevice));
    }

    private View.OnClickListener onClickEvent(BluetoothDeviceVH holder, BluetoothDeviceObject bluetoothDevice) {
        return v -> Toast.makeText(holder.binding.getRoot().getContext(), "bluetooth" + bluetoothDevice.name + " chosen", Toast.LENGTH_SHORT).show();
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
