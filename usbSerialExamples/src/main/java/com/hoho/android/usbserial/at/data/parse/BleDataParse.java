package com.hoho.android.usbserial.at.data.parse;

import com.hoho.android.usbserial.at.util.BleUtils;

import java.util.List;

public class BleDataParse implements IBleDataParse {
    /**
     * 解析电量
     */
    @Override
    public int parseBattery(byte[] value) {
        return value[0] & 0x7F;
    }

    @Override
    public boolean parseCharge(byte[] value) {
        return (value[0] & 0x80) != 0;
    }

    @Override
    public String parseHardVersion(byte[] value) {
        return new String(value);
    }

    @Override
    public String parseSerial(byte[] value) {
        return new String(value);
    }

    @Override
    public TempDataBean parseTemp(byte[] value) {
        return BleUtils.parseTemp(value);
    }

    @Override
    public List<TempDataBean> parseTempV1_5(byte[] value) {
        return BleUtils.parseTempV1_5(value);
    }
}
