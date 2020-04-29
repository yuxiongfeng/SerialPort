package com.hoho.android.usbserial.at;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;

import com.hoho.android.usbserial.at.interfaces.ConnectStatusListener;
import com.hoho.android.usbserial.at.interfaces.Connector;
import com.hoho.android.usbserial.at.interfaces.DataListener;
import com.hoho.android.usbserial.at.interfaces.PortConnectListener;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.examples.BuildConfig;
import com.hoho.android.usbserial.examples.CustomProber;
import com.hoho.android.usbserial.examples.TerminalFragment;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * @Description:
 * @Author: yxf
 * @CreateDate: 2020/4/29 14:58
 * @UpdateUser: yxf
 * @UpdateDate: 2020/4/29 14:58
 */
public class AtConnector implements Connector, SerialInputOutputManager.Listener {

    private Activity activity;
    private Context context;
    private int deviceId, portNum, baudRate;

    private ConnectStatusListener connectorListener;
    private PortConnectListener portConnectListener;
    private DataListener dataListener;
    private UsbSerialPort usbSerialPort;
    private UsbPermission usbPermission = UsbPermission.Unknown;
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";
    private SerialInputOutputManager usbIoManager;
    private boolean withIoManager;
    private boolean portConnected;

    private ControlLines controlLines;

    private BroadcastReceiver broadcastReceiver;
    private Handler mainLooper;

    public AtConnector(Activity activity,Context context, int deviceId, int portNum, int baudRate, boolean withIoManager) {
        this.activity = activity;
        this.context = context;
        this.deviceId = deviceId;
        this.portNum = portNum;
        this.baudRate = baudRate;
        this.withIoManager = withIoManager;
        mainLooper = new Handler(Looper.getMainLooper());
        controlLines=new ControlLines();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(INTENT_ACTION_GRANT_USB)) {
                    usbPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                            ? UsbPermission.Granted : UsbPermission.Denied;
//                    connectSerialPort();
                }
            }
        };

        activity.registerReceiver(broadcastReceiver, new IntentFilter(INTENT_ACTION_GRANT_USB));
        if (usbPermission == UsbPermission.Unknown || usbPermission == UsbPermission.Granted){}
//            mainLooper.post(this::connectSerialPort);
    }

    @Override
    public void connect() {

    }

    @Override
    public void connect(ConnectStatusListener connectorListener, DataListener dataListener) {

    }

    @Override
    public void connect(ConnectStatusListener connectorListener, PortConnectListener portConnectListener, DataListener dataListener) {
        this.connectorListener = connectorListener;
        this.portConnectListener = portConnectListener;
        this.dataListener = dataListener;
    }

    @Override
    public void disConnect() {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void cancelConnect() {

    }


    @Override
    public void onNewData(byte[] data) {

    }

    @Override
    public void onRunError(Exception e) {

    }


    class ControlLines {
        private static final int refreshInterval = 200; // msec
        private Runnable runnable;

        ControlLines() {
            runnable = this::start; // w/o explicit Runnable, a new lambda would be created on each postDelayed, which would not be found again by removeCallbacks
        }

        private boolean refresh() {
            return true;
        }

        void start() {
            if (portConnected && refresh())
                mainLooper.postDelayed(runnable, refreshInterval);
        }

        void stop() {
            mainLooper.removeCallbacks(runnable);
        }
    }


}
