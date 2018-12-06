package com.zune.nettimeclockauto;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.coolerfall.daemon.Daemon;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Fussen on 2017/2/21.
 * <p>
 * 需要正常工作的服务
 */
public class PushWorkServer extends Service {

    /**
     * 定时唤醒的时间间隔，5分钟
     */
    private final static int ALARM_INTERVAL = 10 * 60 * 1000;
    private final static int WAKE_REQUEST_CODE = 6666;
    private Myconn conn;
    private MyBinder binder;
    private MyThread thread;
    private String tag;

    @Override
    public void onCreate() {
        Log.d("zune", "创建成功");
        Daemon.run(this, PushWorkServer.class, Daemon.INTERVAL_ONE_MINUTE * 10);
        binder = new MyBinder();
        if (conn == null) {
            conn = new Myconn();
        }
        super.onCreate();
    }

    /**
     * 最近任务列表中划掉卡片时回调
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        onEnd(rootIntent);
    }

    /**
     * 设置-正在运行中停止服务时回调
     */
    @Override
    public void onDestroy() {
        onEnd(null);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    void onEnd(Intent rootIntent) {
        System.out.println("保存数据到磁盘。");
        thread = null;
        Intent service = new Intent(this, PushWorkServer.class);
        service.putExtra("tag", "re_start    ");
        startService(service);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("zune:", "我又回来了");
        if (intent.getStringExtra("tag") != null)
            tag = intent.getStringExtra("tag");
        else
            tag = "re_start    ";
        registScreen();
        startService(new Intent(this, RemoteServer.class));
        PushWorkServer.this.bindService(new Intent(PushWorkServer.this, RemoteServer.class), conn, Context.BIND_IMPORTANT);
        MiPushClient.enablePush(this);
        //发送唤醒广播来促使挂掉的UI进程重新启动起来
        restartDelay();
        /**zune: 设定网络时间**/
        resetNetTime();
        return START_STICKY;
    }

    private void restartDelay() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, PushWorkServer.class);
        alarmIntent.setAction(WakeReceiver.GRAY_WAKE_ACTION);
        PendingIntent operation = PendingIntent.getBroadcast(this, WAKE_REQUEST_CODE, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), ALARM_INTERVAL, operation);
    }

    private void registScreen() {
        ScreenBroadcastListener.getInstance(this)
                .registerListener(new ScreenBroadcastListener.ScreenStateListener() {
                    @Override
                    public void onScreenOn() {
                        resetNetTime();
                        tag = "screen_on   ";
                    }

                    @Override
                    public void onScreenOff() {
                        resetNetTime();
                        tag = "screen_off  ";
                    }
                });
    }

    class MyBinder extends PeocessService.Stub {
        @Override
        public String getServiceName() throws RemoteException {
            return "PushWorkServer";
        }
    }

    class Myconn implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("zune:", "连接远程服务进程成功");
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("zune:", "远程服务进程被杀死");
            PushWorkServer.this.startService(new Intent(PushWorkServer.this, RemoteServer.class));
            PushWorkServer.this.bindService(new Intent(PushWorkServer.this, RemoteServer.class), conn, Context.BIND_IMPORTANT);
        }
    }

    class myBinder2 extends Binder {
        public PushWorkServer getService() {
            return PushWorkServer.this;
        }
    }

    private void resetNetTime() {
        thread = new MyThread();
        thread.start();
    }

    private void getNetTime() {
        try {
            URL url = new URL("http://www.baidu.com");
            URLConnection uc = url.openConnection();//生成连接对象
            uc.connect(); //发出连接
            long ld = uc.getDate(); //取得网站日期时间
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(ld);
            String format = formatter.format(calendar.getTime());
            Log.i("zune:", "网络时间为 = " + tag + " " + format);
            writeOnLocal(format);
            String year = format.substring(0, 4);
            String month = format.substring(5, 7);
            String day = format.substring(8, 10);
            String hour = format.substring(11, 13);
            String minute = format.substring(14, 16);
            String sec = format.substring(17, 19);
            int[] time = {Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day)
                    , Integer.parseInt(hour), Integer.parseInt(minute), Integer.parseInt(sec)};

            SimpleDateFormat format0 = new SimpleDateFormat("yyyyMMdd.HHmmss");
            String time0 = null;
            try {
                time0 = format0.format(new Date(ld));
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("zune:", "time0 e = " + e);
            }
            testDate(time0);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("zune:", "getNetTime e = " + e);
        }
    }

    private void writeOnLocal(String time) {
        File file = new File(getExternalCacheDir(), "time.txt");
        if (!file.exists()) {
            try {
                boolean newFile = file.createNewFile();
                Log.i("zune:", "create = " + newFile);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("zune:", "create  e = " + e);
                return;
            }
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(tag + " " + time + "\n");
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void testDate(String time) {
        DataOutputStream os = null;
        try {
            Process process = Runtime.getRuntime().exec("su");
            String datetime = time; //测试的设置的时间【时间格式 yyyyMMdd.HHmmss】
            os = new DataOutputStream(process.getOutputStream());
            //os.writeBytes("setprop persist.sys.timezone GMT\n");   //自动设置时区
            os.writeBytes("/system/bin/date -s " + datetime + "\n");
            os.writeBytes("clock -w\n");
            os.writeBytes("exit\n");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("zune:", "testDate e = " + e);
        } finally {
            try {
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("zune:", "IOException e = " + e);
            }
        }
    }

    private class MyThread extends Thread {
        @Override
        public void run() {
            getNetTime();
        }
    }
}
