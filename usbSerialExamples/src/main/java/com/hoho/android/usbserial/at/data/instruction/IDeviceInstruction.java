package com.hoho.android.usbserial.at.data.instruction;

/**
 * @Description:
 * @Author: yxf
 * @CreateDate: 2020/4/29 14:44
 * @UpdateUser: yxf
 * @UpdateDate: 2020/4/29 14:44
 */
public interface IDeviceInstruction {
    /**
     * 未连接的时候用于测试串口链路或者已连接用于断开
     */
    String getBaseVerifyInstruction();

    /**
     * 查询当前是主机模式还是从机模式
     *
     * @return
     */
    String queryRole();

    /**
     * 设置为主机模式
     */
    String setRoleMaster();

    /**
     * 设置是否为自动模式，如：0: 上电立即工作  1: 上电后暂不工作，等待AT+START/AT+CON/AT
     */
    String queryImme();

    /**
     * 设置为手动模式
     */
    String setImmeManual();

    /**
     * 扫描从设备，使用条件AT＋IMME１；AT+ROLE１
     */
    String scanDevices();

    /**
     * 连接从设备的前缀，如：AT+CO<P1><P2>
     * P1: 可能的值，0，1，2，AT+DISC？可得到该值。
     * P2: MAC 地址，12 位长度，可由 AT+DISC？得到。
     */
    String connectDevice(int type, String mac);

    /**
     * 断开连接
     */
    String disconnectDevice();

    /**
     * 查看所有特征值
     */
    String findAllCharacteristics();

    /**
     * 订阅特征值的前缀,如: AT+NOTIFY_ON<P1>    P1: characteristic的编号
     */
    String notifyCharacteristic(String characteristicSerial);

    /**
     * 取消该特征值的订阅，如：AT+NOTIFYOFF<P1>  P1: characteristic的编号
     */
    String notifyOffCharacteristic(String characteristicSerial);

    /**
     * 读取特征值数据，如：AT+READDATA<P1>?  P1: characteristic的编号
     */
    String readCharacteristic(String characteristicSerial);

    /**
     * 向特征值写数据前的准备工作，如：AT+SET_WAY<P1><P2>
     * P1: 长度 2 位，发送数据的方式，可能的值 WR，WN，NO，IN
     * P2: 长度 4 位，具备发送功能的 Char 的编号，值域 0x0001~0xFFFF。指令设置成功会返回 OK+SEND-OK，失败无返回。
     * 设置了这个指令并且收到返回值以后，如果设置值正确，理论上现在应当可以开始透传数据了，即你发送什么数据，从机就收到什么，不再需要 AT 指令辅助。
     */
    String sendDataPrepare(String type, String characteristicSerial);
}
