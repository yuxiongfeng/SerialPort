package com.hoho.android.usbserial.bean;

import java.io.Serializable;

public class DeviceBean implements Serializable {
    private String macaddress;
    private int bluetoothRssi;

    public DeviceBean(String macaddress, int bluetoothRssi) {
        this.macaddress = macaddress;
        this.bluetoothRssi = bluetoothRssi;
    }

    public String getMacaddress() {
        return macaddress;
    }

    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }

    public int getBluetoothRssi() {
        return bluetoothRssi;
    }

    public void setBluetoothRssi(int bluetoothRssi) {
        this.bluetoothRssi = bluetoothRssi;
    }
}