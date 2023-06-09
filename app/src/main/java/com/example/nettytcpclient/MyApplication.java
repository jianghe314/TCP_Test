package com.example.nettytcpclient;

import android.app.Application;

/**
 * Created by CIDI zhengxuan on 2023/6/9
 * QQ:1309873105
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThreadPoolUtils.InitThreadPool();
    }
}
