package com.hoho.android.usbserial.examples;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hoho.android.usbserial.at.AtConnector;
import com.hoho.android.usbserial.at.UsbPermission;
import com.hoho.android.usbserial.at.data.parse.TempDataBean;
import com.hoho.android.usbserial.at.interfaces.ConnectStatusListener;
import com.hoho.android.usbserial.at.interfaces.DataListener;
import com.hoho.android.usbserial.at.interfaces.OnScanListener;
import com.hoho.android.usbserial.at.interfaces.PortConnectListener;
import com.wms.logger.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int MSG_OPEN_PORT = 0x01;
    public static final int MSG_SCAN = 0x02;

    private List<String> deviceList = new ArrayList<>();


    private int deviceId;
    private int portNum;
    private int baudRate;
    private boolean withIoManager;

    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";
    private UsbPermission usbPermission = UsbPermission.Unknown;

    private AtConnector atConnector;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(INTENT_ACTION_GRANT_USB)) {
                usbPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        ? UsbPermission.Granted : UsbPermission.Denied;
                atConnector.openSerialPort();
            }
        }
    };

    private Button idScan;
    private Button idDisconnect;
    private TextView txtShow;
    private MyHandler myHandler;
    private ScanAdapter scanAdapter;
    private RecyclerView idRecyclerView;

    private static class MyHandler extends Handler {

        WeakReference<TestActivity> mActivity;

        public MyHandler(TestActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TestActivity activity = mActivity.get();
            switch (msg.what) {
                case MSG_OPEN_PORT:
                    activity.txtShow.append(msg.obj.toString());
                    break;
            }
        }
    }


    private Handler mHandler = new Handler(Looper.getMainLooper());


    private PortConnectListener portConnectListener = new PortConnectListener() {
        @Override
        public void onConnectSuccess() {
            super.onConnectSuccess();
            Logger.w("串口打开成功 current thread is : ", Thread.currentThread().getName());
            Message message = Message.obtain();
            message.what = MSG_OPEN_PORT;
            message.obj = "串口打开成功";
            myHandler.sendMessage(message);
        }

        @Override
        public void onConnectFaild(String msg) {
            super.onConnectFaild(msg);
        }
    };

    private ConnectStatusListener connectStatusListener = new ConnectStatusListener() {
        @Override
        public void onConnectSuccess() {
            super.onConnectSuccess();
        }

        @Override
        public void onConnectFaild() {
            super.onConnectFaild();
        }

        @Override
        public void onDisconnect(boolean isManual) {
            super.onDisconnect(isManual);
        }

        @Override
        public void receiveReconnectTimes(int retryCount, int leftCount, long totalTime) {
            super.receiveReconnectTimes(retryCount, leftCount, totalTime);
        }
    };

    private DataListener dataListener = new DataListener() {
        @Override
        public void receiveCurrentTemp(List<TempDataBean> tempLists) {
            super.receiveCurrentTemp(tempLists);
        }

        @Override
        public void receiveSerial(String serial) {
            super.receiveSerial(serial);
        }

        @Override
        public void receiveHardVersion(String hardVersion) {
            super.receiveHardVersion(hardVersion);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
        myHandler = new MyHandler(this);
        deviceId = getIntent().getIntExtra("device", 0);
        portNum = getIntent().getIntExtra("port", 0);
        baudRate = getIntent().getIntExtra("baud", 9600);
        withIoManager = getIntent().getBooleanExtra("withIoManager", false);

        atConnector = new AtConnector(this, this, deviceId, portNum);
        atConnector.setPortConnectListener(portConnectListener);
        atConnector.openSerialPort();
        registerReceiver(broadcastReceiver, new IntentFilter(INTENT_ACTION_GRANT_USB));

        scanAdapter = new ScanAdapter(this, deviceList);
        scanAdapter.setListener(new ScanAdapter.ItemClickListener() {
            @Override
            public void itemClickListener(String mac) {
                atConnector.setPatchMac(mac);
                atConnector.setPatchType(0);
                atConnector.connect(connectStatusListener, dataListener);
            }
        });
        idRecyclerView.setAdapter(scanAdapter);
        idRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initView() {
        idScan = findViewById(R.id.id_scan);
        idDisconnect = findViewById(R.id.id_disconnect);
        txtShow = findViewById(R.id.id_show);

        idScan.setOnClickListener(this);
        idDisconnect.setOnClickListener(this);
        idRecyclerView = findViewById(R.id.id_recyclerView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_scan:
                txtShow.append("\n开始扫描...\n");
                if (deviceList != null) {
                    deviceList.clear();
                }
                atConnector.scanDevices(new OnScanListener() {
                    @Override
                    public void onDeviceFound(String data) {
                        super.onDeviceFound(data);
                        if (data != null && !TextUtils.isEmpty(data)) {
                            String[] macList = data.split("\r\n");
                            for (int i = 0; i < macList.length; i++) {
                                if (macList[i].contains("BLE_TEMP")) {
                                    int index = macList[i].indexOf(":");
                                    deviceList.add(macList[i].substring(index+1, index + 13));
                                }
                            }
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    scanAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                });
                break;
            case R.id.id_disconnect:
                atConnector.disConnect();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
