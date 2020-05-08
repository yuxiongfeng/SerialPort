package com.hoho.android.usbserial.at.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.hoho.android.usbserial.at.UsbPermission;
import com.hoho.android.usbserial.at.data.instruction.AtInstruction;
import com.hoho.android.usbserial.at.data.instruction.HmUUID;
import com.hoho.android.usbserial.at.data.instruction.IDeviceInstruction;
import com.hoho.android.usbserial.at.data.instruction.InstructionType;
import com.hoho.android.usbserial.at.data.instruction.ResultConstant;
import com.hoho.android.usbserial.at.interfaces.ConnectStatusListener;
import com.hoho.android.usbserial.at.interfaces.DataListener;
import com.hoho.android.usbserial.at.interfaces.OnScanListener;
import com.hoho.android.usbserial.at.interfaces.PortConnectListener;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.examples.BuildConfig;
import com.hoho.android.usbserial.examples.CustomProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.wms.logger.Logger;

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
    private static final int NOTIFY_MILLIS = 5000;
    private SerialInputOutputManager usbIoManager;
    private UsbSerialPort usbSerialPort;
    /**
     * 用于接收收到的数据
     */
    private StringBuilder sb = new StringBuilder();

    /**
     * 相关操作指令集
     */
    private IDeviceInstruction atInstruction = new AtInstruction();

    private PortConnectListener portConnectListener;
    private OnScanListener scanListener;
    private ConnectStatusListener connectStatusListener;
    private DataListener dataListener;

    /**
     * 当前指令
     */
    private String currentInstruction;

    private InstructionType currentInstructionType = InstructionType.NONE;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * 0：未连接 1：连接中 2：已连接 3：连接失败
     */
    private int connectStatus = 0;

    /**
     * 上次接收数据的时间
     */
    private long lastTime;

    /**
     * 订阅温度，首次返回的数据（OK+DATA-OK）
     */
    private boolean isFirstArrive = true;

    public AtOperator(Activity activity, Context context, int deviceId, int portNum, int baudRate, boolean withIoManager) {
        this.activity = activity;
        this.context = context;
        this.deviceId = deviceId;
        this.portNum = portNum;
        this.baudRate = baudRate;
        this.withIoManager = withIoManager;
    }

    public AtOperator(Activity activity, Context context, int deviceId, int portNum) {
        this(activity, context, deviceId, portNum, 9600, true);
    }

    /**
     * 设置打开串口的回调
     *
     * @param portConnectListener
     */
    public void setPortConnectListener(PortConnectListener portConnectListener) {
        this.portConnectListener = portConnectListener;
    }

    public void setScanListener(OnScanListener scanListener) {
        this.scanListener = scanListener;
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
     * 实时数据回调
     *
     * @param dataListener
     */
    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }

    /**
     * 设置成主模式
     */
    private void setRoleMaster() {
        currentInstruction = atInstruction.setRoleMaster();
        currentInstructionType = InstructionType.ROLE;
        send(currentInstructionType, currentInstruction);
    }

    /**
     * 查看设备当前的模式是否是主模式
     */
    private void isRoleMaster() {
        currentInstruction = atInstruction.queryRole();
        currentInstructionType = InstructionType.ROLE;
        send(currentInstructionType, currentInstruction);
    }


    /**
     * 设置成手动模式，默认是自动连接模式，但是自动连接会产生找不到从机的问题
     */
    private void setImmeManual() {
        currentInstruction = atInstruction.setImmeManual();
        currentInstructionType = InstructionType.IMME;
        send(currentInstructionType, currentInstruction);
    }

    private void isImmeManual() {
        currentInstruction = atInstruction.queryImme();
        currentInstructionType = InstructionType.IMME;
        send(currentInstructionType, currentInstruction);
    }

    /**
     * 连接设备
     *
     * @param scanListener
     */
    public void scanDevice(OnScanListener scanListener) {
        Logger.w("开始扫描。。。");
        setScanListener(scanListener);
        currentInstruction = atInstruction.scanDevices();
        currentInstructionType = InstructionType.DISC;
        //开启扫描
        this.scanListener = scanListener;
        send(currentInstructionType, currentInstruction);
    }

    /**
     * 连接设备
     *
     * @param type
     * @param patchMac
     */
    public void connectDevice(int type, String patchMac) {
        connectStatus = 1;
        currentInstruction = atInstruction.connectDevice(type, patchMac);
        currentInstructionType = InstructionType.COON;
        send(currentInstructionType, currentInstruction);
    }

    /**
     * 获取序列号
     */
    private void fetchSerialNum() {
        lastTime = System.currentTimeMillis();
        currentInstruction = atInstruction.readCharacteristic(HmUUID.CHARACTOR_SERIAL_NUM.getCharacteristicAlias());
        currentInstructionType = InstructionType.READ;
        send(currentInstructionType, currentInstruction);
    }

    /**
     * 获取版本号
     */
    private void fetchVersion() {
        lastTime = System.currentTimeMillis();
        currentInstruction = atInstruction.readCharacteristic(HmUUID.CHARACTOR_VERSION.getCharacteristicAlias());
        currentInstructionType = InstructionType.READ;
        send(currentInstructionType, currentInstruction);
    }

    /**
     * 订阅温度
     */
    private void subscribeTemp() {
        isFirstArrive = true;
        currentInstruction = atInstruction.notifyCharacteristic(HmUUID.CHARACTOR_TEMP.getCharacteristicAlias());
        currentInstructionType = InstructionType.NOTIFY;
        send(currentInstructionType, currentInstruction);
    }

    /**
     * 断开连接
     */
    public void disConnect() {
        if (portConnected) {
            currentInstruction = atInstruction.disconnectDevice();
            currentInstructionType = InstructionType.AT;
            //清空StringBuilder
            sb.delete(0, sb.length());
            send(currentInstructionType, currentInstruction);
        }
    }

    /**
     * 串口回调接口
     *
     * @param data
     */
    @Override
    public void onNewData(byte[] data) {
        long currentTime = System.currentTimeMillis();
        sb.append(new String(data));
        if (sb == null || sb.toString() == null || TextUtils.isEmpty(sb.toString())) {
            return;
        }
        String newData = sb.toString();

        if (currentInstructionType != InstructionType.COON) {

            if (newData.startsWith(ResultConstant.AT_LOST) || newData.endsWith(ResultConstant.AT_LOST)) {
                Logger.w("连接中断。。。");
                connectStatusListener.onDisconnect(false);
            }

        }

        if (newData.startsWith(ResultConstant.AT_LOST) || newData.endsWith(ResultConstant.AT_LOST)) {
            Logger.w("连接失败。。。");
            if (connectStatusListener != null) {
                connectStatusListener.onDisconnect(false);
            }
            return;
        }

        if (newData.startsWith(ResultConstant.COON_FAIL) || newData.endsWith(ResultConstant.COON_FAIL)) {
            connectStatus = 3;
            Logger.w("连接失败");
            if (connectStatusListener != null) {
                connectStatusListener.onConnectFaild();
            }
            return;
        }

        /**
         * 以下用于判断是否是同一个指令的数据，因为数据都是通过字节来返回的，且不是一起返回，所以需要判断
         */
        switch (currentInstructionType) {
            case AT:
                if (newData.startsWith(ResultConstant.AT) || newData.endsWith(ResultConstant.AT_LOST)) {
                    if (isConnected()) {
                        Logger.w("断开连接");
                        connectStatusListener.onDisconnect(true);
                    } else {
                        Logger.w("验证串口是否打开 : ", portConnected);
                    }
                    connectStatus = 0;
                    currentInstructionType = InstructionType.NONE;
                }
                break;
            case ROLE:
                if (newData.startsWith(ResultConstant.ROLE_START) && newData.endsWith(ResultConstant.ROLE_END)) {
                    Logger.w("role is : ", newData);
                    setImmeManual();
                    currentInstructionType = InstructionType.NONE;
                }
                break;
            case IMME:
                if (newData.startsWith(ResultConstant.IMME_START) && newData.endsWith(ResultConstant.IMME_END)) {
                    Logger.w("immi is : ", newData);
                    currentInstructionType = InstructionType.NONE;
                }
                break;
            case DISC:
                if (newData.startsWith(ResultConstant.DISC_START) && newData.endsWith(ResultConstant.DISC_END)) {
                    Logger.w("disc is : ", newData);
                    scanListener.onDeviceFound(newData);
                    currentInstructionType = InstructionType.NONE;
                    Logger.w("扫描结束。。。");
                }
                break;
            case COON:

                if (newData.startsWith(ResultConstant.AT_LOST) || newData.endsWith(ResultConstant.AT_LOST)) {
                    Logger.w("连接失败");
                    connectStatus = 3;
                }
                if (newData.startsWith(ResultConstant.COON_START)) {
                    Logger.w("coon is :", newData);
                    currentInstructionType = InstructionType.NONE;
                    mHandler.postDelayed(() -> {
                        Logger.w("开始获取serialNum...");
                        fetchSerialNum();
                    }, 500);
                }

                break;
            case READ://没有标识
                Logger.w("设备连接成功。。。");
                if (!isConnected()) {
                    connectStatus = 2;
                }

                if (isReadSerialNum(currentInstruction) && newData.length() == getSerialNumLength()) {
                    Logger.w("serial is : ", newData);
                    dataListener.receiveSerial(newData);
                    fetchVersion();
                }

                if (isReadVersion(currentInstruction) && newData.length() == getHardVersionLength()) {
                    Logger.w("hardVersion is : ", newData);
                    dataListener.receiveHardVersion(newData);
                    subscribeTemp();
                }

                break;
            case NOTIFY://没有标识
                if (isFirstArrive && newData.startsWith(ResultConstant.NOTIFY_SUCCESS)) {
                    if (!isConnected()) {
                        connectStatus = 2;
                        connectStatusListener.onConnectSuccess();
                    }
                    Logger.w("订阅温度成功");
                    //清空StringBuilder
                    sb.delete(0, sb.length());
                } else if (newData.length() == getTempDataLength()) {//一次完整的温度数据
                    Logger.w("实时温度 : ", BleUtils.parseTempV1_5(newData));
                    dataListener.receiveCurrentTemp(BleUtils.parseTempV1_5(newData));
                    //清空StringBuilder
                    sb.delete(0, sb.length());
                }
                if (isFirstArrive) {
                    isFirstArrive = false;
                }
                break;
            case SET_WAY:
                if (newData.startsWith(ResultConstant.SET_WAY)) {
                    Logger.w(newData);
                    currentInstructionType = InstructionType.NONE;
                }
                break;
            case NONE:
                if (newData.startsWith(ResultConstant.AT_LOST)) {//断线或连接失败

                }
                break;
        }
    }

    /**
     * 是否是读序列号的指令
     *
     * @param instruction
     * @return
     */
    private boolean isReadSerialNum(String instruction) {
        return instruction.equalsIgnoreCase(atInstruction.readCharacteristic(HmUUID.CHARACTOR_SERIAL_NUM.getCharacteristicAlias()));
    }

    private boolean isReadVersion(String instruction) {
        return instruction.equalsIgnoreCase(atInstruction.readCharacteristic(HmUUID.CHARACTOR_VERSION.getCharacteristicAlias()));
    }

    /**
     * 序列号长度
     *
     * @return
     */
    private int getSerialNumLength() {
        return 11;
    }

    /**
     * 版本号长度
     *
     * @return
     */
    private int getHardVersionLength() {
        return 6;
    }

    /**
     * 温度数据长度
     *
     * @return
     */
    private int getTempDataLength() {
        return 20;
    }

    @Override
    public void onRunError(Exception e) {
        portConnectListener.onConnectFaild(e.getMessage());
        closeSerialPort();
    }

    public boolean isConnected() {
        return connectStatus == 2;
    }

    /**
     * 连接串口
     */
    public void openSerialPort() {
        Logger.w("正在打开串口。。。");
        if (usbPermission == UsbPermission.Unknown || usbPermission == UsbPermission.Granted) {
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
//                setRoleMaster();
                Logger.w("串口打开成功。。。");
            } catch (Exception e) {
                portConnectListener.onConnectFaild("connection failed: " + e.getMessage());
                closeSerialPort();
            }
        }
    }

    /**
     * 关闭串口
     */
    private void closeSerialPort() {
        portConnected = false;
        if (usbIoManager != null)
            usbIoManager.stop();
        usbIoManager = null;
        try {
            usbSerialPort.close();
        } catch (IOException ignored) {
        }
        usbSerialPort = null;
        Logger.w("关闭串口");
    }

    private void send(InstructionType instructionType, String str) {
        //清空StringBuilder
        sb.delete(0, sb.length());
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

}
