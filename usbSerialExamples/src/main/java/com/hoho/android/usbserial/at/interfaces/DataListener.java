package com.hoho.android.usbserial.at.interfaces;

/**
 * 只接收数据
 */
public class DataListener {

    /**
     * 接收当前温度
     */
    public void receiveCurrentTemp(float currentTemp) {
    }

    /**
     * 读取电量
     */
    public void receiveBattery(Integer battery) {
    }

    /**
     * 是否充电
     */
    public void receiveCharge(boolean isCharge) {
    }

    /**
     * 读取序列号
     */
    public void receiveSerial(String serial) {
    }

    /**
     * 读取硬件版本号
     */
    public void receiveHardVersion(String hardVersion) {
    }

    /**
     * 接收缓存温度数量
     */
    public void receiveCacheTotal(Integer cacheCount) {

    }

    public void receiveResult(String data) {
    }


}
