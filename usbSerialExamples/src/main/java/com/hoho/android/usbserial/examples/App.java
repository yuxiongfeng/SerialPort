package com.hoho.android.usbserial.examples;

import android.app.Application;

import com.wms.logger.Logger;

/**
 * @Description:
 * @Author: yxf
 * @CreateDate: 2020/4/26 15:04
 * @UpdateUser: yxf
 * @UpdateDate: 2020/4/26 15:04
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化日志
        Logger.newBuilder()
                .tag("serial_port")
                .showThreadInfo(false)
                .methodCount(1)
                .saveLogCount(7)
                .context(this)
                .deleteOnLaunch(false)
                .saveFile(BuildConfig.DEBUG)
                .isDebug(BuildConfig.DEBUG)
                .build();

    }
}
