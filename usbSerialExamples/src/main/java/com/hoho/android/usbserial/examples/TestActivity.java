package com.hoho.android.usbserial.examples;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.hoho.android.usbserial.at.AtConnector;
import com.hoho.android.usbserial.at.UsbPermission;
import com.hoho.android.usbserial.at.interfaces.OnScanListener;
import com.hoho.android.usbserial.at.interfaces.PortConnectListener;
import com.hoho.android.usbserial.util.HexDump;
import com.wms.logger.Logger;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {

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
    private Button idConnect;


    private PortConnectListener portConnectListener=new PortConnectListener(){
        @Override
        public void onConnectSuccess() {
            super.onConnectSuccess();
        }

        @Override
        public void onConnectFaild(String msg) {
            super.onConnectFaild(msg);
        }
    };
    private boolean is_o_letter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
        deviceId = getIntent().getIntExtra("device", 0);
        portNum = getIntent().getIntExtra("port", 0);
        baudRate = getIntent().getIntExtra("baud", 9600);
        withIoManager = getIntent().getBooleanExtra("withIoManager", false);

        atConnector = new AtConnector(this, this, deviceId, portNum);
        atConnector.setPortConnectListener(portConnectListener);
        atConnector.openSerialPort();
        registerReceiver(broadcastReceiver, new IntentFilter(INTENT_ACTION_GRANT_USB));

    }


    private void initView() {
        idScan = findViewById(R.id.id_scan);
        idConnect = findViewById(R.id.id_connect);

        idScan.setOnClickListener(this);
        idConnect.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_scan:
                atConnector.scanDevices(new OnScanListener(){
                    @Override
                    public void onDeviceFound(byte[] data) {
                        super.onDeviceFound(data);

                        SpannableStringBuilder spn = new SpannableStringBuilder();
                        if (data.length > 0) {
                            String dumpHexString = HexDump.dumpHexString(data);
                            if (is_o_letter && dumpHexString.equalsIgnoreCase("k")) {
                                spn.append(new String(data) + "\n");
                            } else {
                                spn.append(new String(data));
                            }
                            if (dumpHexString.equalsIgnoreCase("o")) {
                                is_o_letter = true;
                            } else {
                                is_o_letter = false;
                            }
                        }
                        Logger.w(spn.toString());
                    }
                });
                break;
            case R.id.id_connect:

                break;
        }
    }
}
