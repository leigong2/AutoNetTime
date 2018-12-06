package com.zune.nettimeclockauto;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zune.nettimeclockauto.PeocessService;


/**
 * Created by wuao on 2017/2/6.
 */

public class RemoteServer extends Service {
    private MyBinder binder;
    private myBinder2 myBinder2;
    private Myconn conn;

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new MyBinder();
        if (conn == null) {
            conn = new Myconn();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("zune:", "远程服务已经开启");
        RemoteServer.this.bindService(new Intent(RemoteServer.this, PushWorkServer.class), conn, Context.BIND_IMPORTANT);
        return super.onStartCommand(intent, flags, startId);
    }

    class MyBinder extends PeocessService.Stub {

        @Override
        public String getServiceName() throws RemoteException {
            return "RemoteServer";
        }
    }

    class Myconn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("zune:", "连接本地客户端进程成功");
        }


        //在绑定断开的时候 会会掉此方法
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("zune:", "本地客户端进程被杀死");
            Intent service = new Intent(RemoteServer.this, PushWorkServer.class);
            service.putExtra("tag","remote_start");
            RemoteServer.this.startService(service);
            RemoteServer.this.bindService(new Intent(RemoteServer.this, PushWorkServer.class), conn, Context.BIND_IMPORTANT);

        }
    }

    class myBinder2 extends Binder {
        public RemoteServer getService() {
            return RemoteServer.this;
        }

    }


}
