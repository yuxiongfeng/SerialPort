package com.hoho.android.usbserial.at.interfaces;

/**
 * 连接器回调
 */
public abstract class ConnectStatusListener {

    /**
     * 连接成功
     */
    public void onConnectSuccess() {
    }

    public void onConnectSuccess(String result) {
    }

    /**
     * 连接失败
     */
    public void onConnectFaild() {
    }

    /**
     * 断开连接了
     *
     * @param isManual 是否是手动调用连接断开的
     */
    public void onDisconnect(boolean isManual) {
    }

    /**
     * 接收重连次数
     *
     * @param retryCount 当前重连次数
     * @param leftCount  剩余次数
     * @param totalTime  重连总耗时
     */
    public void receiveReconnectTimes(int retryCount, int leftCount, long totalTime) {
    }

    /**
     * 首次连接失败，如果首次连接失败则不会再去重连，直接disconnect
     */
    public void firstConnectFail() {

    }

}
