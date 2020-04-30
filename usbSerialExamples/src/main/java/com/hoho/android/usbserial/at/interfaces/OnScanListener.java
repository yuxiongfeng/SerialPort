package com.hoho.android.usbserial.at.interfaces;

/**
 * 扫描监听器
 */

public class OnScanListener {

    /**
     * 开始扫描
     */
    public void onScanStart() {
    }

    /**
     * 发现设备
     */
    public void onDeviceFound(byte[] data){
    }

    /**
     * 停止搜索
     */
    public void onScanStopped() {
    }

    /**
     * 搜索取消
     */
    public void onScanCanceled() {
    }
}
