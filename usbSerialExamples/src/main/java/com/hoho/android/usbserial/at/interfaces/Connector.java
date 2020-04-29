package com.hoho.android.usbserial.at.interfaces;

/**
 * 连接器
 */
public interface Connector {
    /**
     * 连接设备
     */
    void connect();

    void connect(ConnectStatusListener connectorListener, DataListener dataListener);

    void connect(ConnectStatusListener connectorListener, PortConnectListener portConnectListener, DataListener dataListener);

    /**
     * 断开连接设备
     */
    void disConnect();

    /**
     * 是否连接上了
     */
    boolean isConnected();

    /**
     * 取消连接
     */
    void cancelConnect();


}
