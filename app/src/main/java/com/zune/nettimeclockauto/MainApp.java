package com.zune.nettimeclockauto;


import android.app.Application;
import android.content.Intent;

import com.xiaomi.mipush.sdk.MiPushClient;

public class MainApp extends Application {
    public static final String PUSH_APP_ID = "2882303761517566170";
    public static final String PUSH_APP_KEY = "5121756688170";
    @Override
    public void onCreate() {
        super.onCreate();
        MiPushClient.registerPush(this, PUSH_APP_ID, PUSH_APP_KEY);
        //我们现在需要服务运行, 将标志位重置为 false
        Intent intent = new Intent(this, PushWorkServer.class);
        intent.putExtra("tag","normal_start");
        startService(intent);
    }
}
