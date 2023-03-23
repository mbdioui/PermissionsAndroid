package com.example.testbluetooth;

import android.util.Range;

public class BluetoothUtil {

    public static StrengthSignal checkSignal(Short rssi) {
        Range<Short> strong = Range.create((short) -55, (short) -10);
        Range<Short> good = Range.create((short) -70, (short) -55);
        Range<Short> low = Range.create((short) -90, (short) -80);
        if (strong.contains(rssi)) {
            return StrengthSignal.STRONG;
        } else if (good.contains(rssi)) {
            return StrengthSignal.GOOD;
        } else if (low.contains(rssi)) {
            return StrengthSignal.LOW;
        }
        return StrengthSignal.POOR;
    }
}
