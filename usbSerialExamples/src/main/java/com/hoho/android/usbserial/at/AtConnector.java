package com.hoho.android.usbserial.at;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.hoho.android.usbserial.at.interfaces.ConnectStatusListener;
import com.hoho.android.usbserial.at.interfaces.Connector;
import com.hoho.android.usbserial.at.interfaces.DataListener;
import com.hoho.android.usbserial.at.interfaces.OnScanListener;
import com.hoho.android.usbserial.at.interfaces.PortConnectListener;
import com.hoho.android.usbserial.at.util.AtOperator;
import com.wms.logger.Logger;

import java.util.HashMap;
import java.util.Map;


/**
 * @Description:
 * @Author: yxf
 * @CreateDate: 2020/4/29 14:58
 * @UpdateUser: yxf
 * @UpdateDate: 2020/4/29 14:58
 */
public class AtConnector implements Connector {
    /**
     * 管理atOperator的集合
     */
    private static Map<Integer, AtConnector> atConnectorMap = new HashMap<>();
    private PortConnectListener portConnectListener;
    private ConnectStatusListener connectStatusListener;
    private DataListener dataListener;

    private AtOperator atOperator;

    private String patchMac;
    /**
     * 扫描出来的type，用于连接设备的参数
     */
    private int patchType;

    public AtConnector(Activity activity, Context context, int deviceId, int portNum) {
        atOperator = new AtOperator(activity, context, deviceId, portNum);
//        atOperator.openSerialPort();//打开串口
    }

    public void setPortConnectListener(PortConnectListener portConnectListener) {
        this.portConnectListener = portConnectListener;
        atOperator.setPortConnectListener(portConnectListener);//设置串口连接状态的回调
    }

    /**
     * 打开串口
     */
    public void openSerialPort(){
        atOperator.openSerialPort();
    }

    /**
     * 获取atConnector实例
     *
     * @param activity
     * @param context
     * @param deviceId
     * @param portNum
     * @return
     */
    public static AtConnector getInstance(Activity activity, Context context, int deviceId, int portNum) {
        if (!atConnectorMap.containsKey(deviceId)) {
            atConnectorMap.put(deviceId, new AtConnector(activity, context, deviceId, portNum));
        }
        return atConnectorMap.get(deviceId);
    }

    public void setPatchMac(String patchMac) {
        this.patchMac = patchMac;
    }

    public void setPatchType(int patchType) {
        this.patchType = patchType;
    }

    /**
     * 扫描设备
     */
    public void scanDevices(OnScanListener scanListener) {
        atOperator.scanDevice(scanListener);
    }

    @Override
    public void connect() {
        connect(null, null);
    }

    @Override
    public void connect(ConnectStatusListener connectStatusListener, DataListener dataListener) {
        this.connectStatusListener = connectStatusListener;
        this.dataListener = dataListener;

        atOperator.setConnectStatusListener(connectStatusListener);
        atOperator.setDataListener(dataListener);
        if (TextUtils.isEmpty(patchMac)) {
            Logger.w("patchMac is null");
            throw new NullPointerException("patchMac is null,please set first!!!");
        }
        atOperator.connectDevice(patchType, patchMac);
    }

    @Override
    public void disConnect() {
        atOperator.disConnect();
        atConnectorMap.remove(atConnectorMap);
    }

    @Override
    public boolean isConnected() {
        return null == atOperator ? false : atOperator.isConnected();
    }

    @Override
    public void cancelConnect() {
        atOperator.disConnect();
        atConnectorMap.remove(atConnectorMap);
    }

}
