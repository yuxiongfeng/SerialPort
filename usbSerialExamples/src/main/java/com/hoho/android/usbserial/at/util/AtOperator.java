package com.hoho.android.usbserial.at.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.hoho.android.usbserial.at.UsbPermission;
import com.hoho.android.usbserial.at.data.instruction.AtInstruction;
import com.hoho.android.usbserial.at.data.instruction.IDeviceInstruction;
import com.hoho.android.usbserial.at.interfaces.ConnectStatusListener;
import com.hoho.android.usbserial.at.interfaces.OnScanListener;
import com.hoho.android.usbserial.at.interfaces.PortConnectListener;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.examples.BuildConfig;
import com.hoho.android.usbserial.examples.CustomProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * @Description: 指令工具类（发送数据后，所有的回调都在onNewData里面）
 * @Author: yxf
 * @CreateDate: 2020/4/29 15:00
 * @UpdateUser: yxf
 * @UpdateDate: 2020/4/29 15:00
 */
public class AtOperator implements SerialInputOutputManager.Listener {

    private Activity activity;
    private Context context;
    private UsbPermission usbPermission = UsbPermission.Unknown;

    private boolean portConnected = false;
    private int deviceId, portNum, baudRate;
    private boolean withIoManager;

    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";
    private static final int WRITE_WAIT_MILLIS = 2000;
    private static final int READ_WAIT_MILLIS = 2000;
    private SerialInputOutputManager usbIoManager;
    private UsbSerialPort usbSerialPort;

    /**
     * 相关操作指令集
     */
    private IDeviceInstruction atInstruction = new AtInstruction();

    private PortConnectListener portConnectListener;
    private OnScanListener scanListener;
    private ConnectStatusListener connectStatusListener;

    /**
     * 当前指令
     */
    private String currentInstruction;



    public AtOperator(Activity activity, Context context, int deviceId, int portNum, int baudRate, boolean withIoManager) {
        this.activity = activity;
        this.context = context;
        this.deviceId = deviceId;
        this.portNum = portNum;
        this.baudRate = baudRate;
        this.withIoManager = withIoManager;
    }

    /**
     * 设置打开串口的回调
     *
     * @param portConnectListener
     */
    public void setPortConnectListener(PortConnectListener portConnectListener) {
        this.portConnectListener = portConnectListener;
    }

    /**
     * 设置连接蓝牙设备的回调
     *
     * @param connectStatusListener
     */
    public void setConnectStatusListener(ConnectStatusListener connectStatusListener) {
        this.connectStatusListener = connectStatusListener;
    }

    /**
     * 验证串口是否可用
     */
    private void verifySerialPortAvaliable() {
        currentInstruction = atInstruction.getBaseVerifyInstruction();
        send(currentInstruction);
    }

    /**
     * 连接设备
     *
     * @param scanListener
     */
    public void scanDevice(OnScanListener scanListener) {
        currentInstruction = atInstruction.scanDevices();
        //设置主模式 ， 手动模式
        //开启扫描
        this.scanListener = scanListener;
        send(currentInstruction);
    }

    /**
     * 连接设备
     *
     * @param type
     * @param mac
     */
    public void connectDevice(int type, String mac) {
        currentInstruction = atInstruction.connectDevice(type, mac);
    }

    /**
     * 订阅特征值
     *
     * @param characteristic
     */
    private void subscribeNotification(String characteristic) {
        currentInstruction = atInstruction.notifyCharacteristic(characteristic);

    }


    /**
     * 连接串口
     */
    private void connectSerialPort() {
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
        for (UsbDevice v : usbManager.getDeviceList().values())
            if (v.getDeviceId() == deviceId)
                device = v;
        if (device == null) {
            portConnectListener.onConnectFaild("connection failed: device not found");
            return;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber(context).probeDevice(device);
        if (driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if (driver == null) {
            portConnectListener.onConnectFaild("connection failed: no driver for device");
            return;
        }
        if (driver.getPorts().size() < portNum) {
            portConnectListener.onConnectFaild("connection failed: not enough ports at device");
            return;
        }
        usbSerialPort = driver.getPorts().get(portNum);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if (usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(driver.getDevice())) {
            usbPermission = UsbPermission.Requested;
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(activity, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if (usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice()))
                portConnectListener.onConnectFaild("connection failed: permission denied");
            else
                portConnectListener.onConnectFaild("connection failed: open failed");
            return;
        }

        try {
            usbSerialPort.open(usbConnection);
            usbSerialPort.setParameters(baudRate, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            if (withIoManager) {
                usbIoManager = new SerialInputOutputManager(usbSerialPort, this);
                Executors.newSingleThreadExecutor().submit(usbIoManager);
            }
            portConnected = true;
            portConnectListener.onConnectSuccess();

        } catch (Exception e) {
            portConnectListener.onConnectFaild("connection failed: " + e.getMessage());
            disconnectSerialPort();
        }
    }

    /**
     * 断开串口
     */
    private void disconnectSerialPort() {
        portConnected = false;
        if (usbIoManager != null)
            usbIoManager.stop();
        usbIoManager = null;
        try {
            usbSerialPort.close();
        } catch (IOException ignored) {
        }
        usbSerialPort = null;
    }

    private void send(String str) {
        send(str, false);
    }

    private void send(String str, boolean isHexString) {
        if (!portConnected) {
            portConnectListener.onConnectFaild("port not connected");
            return;
        }
        try {
            byte[] data;
            if (isHexString) {
                data = HexDump.hexStringToByteArray(str);
            } else {
                data = (str).getBytes();
            }
            usbSerialPort.write(data, WRITE_WAIT_MILLIS);
        } catch (Exception e) {
            onRunError(e);
        }
    }

    @Override
    public void onNewData(byte[] data) {

    }

    @Override
    public void onRunError(Exception e) {

    }
}
