package com.hoho.android.usbserial.at.data.instruction;

/**
 * @Description:
 * @Author: yxf
 * @CreateDate: 2020/4/29 15:04
 * @UpdateUser: yxf
 * @UpdateDate: 2020/4/29 15:04
 */
public class Constant {

    /**
     * 查询模式
     */
    public static final String QUERY_ROLE = "AT+ROLE?";
    /**
     * 设置为主机模式
     */
    public static final String SET_ROLE_MASTER = "AT+ROLE1";
    /**
     * 设置是否为自动模式，如：0: 上电立即工作  1: 上电后暂不工作，等待AT+START/AT+CON/AT
     */
    public static final String QUERY_IMME = "AT+IMME?";
    /**
     * 设置为手动模式
     */
    public static final String SET_IMME_MANUAL = "AT+IMME1";

    /**
     * 扫描从设备，使用条件AT＋IMME１；AT+ROLE１
     */
    public static final String SCAN_DEVICES = "AT+DISC?";

    /**
     * 连接设备的前缀，如：AT+CO<P1><P2>
     * P1: 可能的值，0，1，2，AT+DISC？可得到该值。
     * P2: MAC 地址，12 位长度，可由 AT+DISC？得到。
     */
    public static final String CONN_PREFIX = "AT+CO";

    /**
     * 断开设备
     */
    public static final String DISCONNECT_DEVICE = "AT";

    /**
     * 查看所有特征值
     */
    public static final String FIND_ALL_CHARACTERISTIC = "AT+FINDALLCHARS?";

    /**
     * 订阅特征值的前缀,如: AT+NOTIFY_ON<P1>    P1: characteristic的编号
     */
    public static final String NOTIFY_CHARACTERISTIC_PREFIX = "AT+NOTIFY_ON";

    /**
     * 取消该特征值的订阅，如：AT+NOTIFYOFF<P1>  P1: characteristic的编号
     */
    public static final String NOTIFY_OFF_CHARACTERISTIC_PREFIX = "AT+NOTIFYOFF";

    /**
     * 读取特征值数据，如：AT+READDATA<P1>?
     */
    public static final String READ_CHARACTERISTIC_PREFIX = "AT+READDATA";

    /**
     * 向特征值写数据前的准备工作 的前缀，如：AT+SET_WAY<P1><P2>
     */
    public static final String SEND_DATA_PREPARE_PREFIX = "AT+SET_WAY";

}
