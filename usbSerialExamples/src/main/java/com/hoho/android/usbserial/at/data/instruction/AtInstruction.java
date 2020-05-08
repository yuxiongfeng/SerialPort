package com.hoho.android.usbserial.at.data.instruction;

/**
 * @Description: 蓝牙模块通讯流程指令集
 * @Author: yxf
 * @CreateDate: 2020/4/29 15:02
 * @UpdateUser: yxf
 * @UpdateDate: 2020/4/29 15:02
 */
public class AtInstruction implements IDeviceInstruction {

    @Override
    public String queryRole() {
        return Constant.QUERY_ROLE;
    }

    @Override
    public String setRoleMaster() {
        return Constant.SET_ROLE_MASTER;
    }

    @Override
    public String queryImme() {
        return Constant.QUERY_IMME;
    }

    @Override
    public String setImmeManual() {
        return Constant.SET_IMME_MANUAL;
    }

    @Override
    public String scanDevices() {
        return Constant.SCAN_DEVICES;
    }

    @Override
    public String connectDevice(int type, String mac) {
        return Constant.CONN_PREFIX + type + mac;
    }

    @Override
    public String disconnectDevice() {
        return Constant.DISCONNECT_DEVICE;
    }

    @Override
    public String findAllCharacteristics() {
        return Constant.FIND_ALL_CHARACTERISTIC;
    }

    @Override
    public String notifyCharacteristic(String characteristicSerial) {
        return Constant.NOTIFY_CHARACTERISTIC_PREFIX + characteristicSerial;
    }

    @Override
    public String notifyOffCharacteristic(String characteristicSerial) {
        return Constant.NOTIFY_OFF_CHARACTERISTIC_PREFIX + characteristicSerial;
    }

    @Override
    public String readCharacteristic(String characteristicSerial) {
        return Constant.READ_CHARACTERISTIC_PREFIX + characteristicSerial+"?";
    }

    @Override
    public String sendDataPrepare(String type, String characteristicSerial) {
        return Constant.SEND_DATA_PREPARE_PREFIX + type + characteristicSerial;
    }

}
