package com.zune.nettimeclockauto;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED){
            Intent mainIntent = intent;
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mainIntent.setAction("com.zune.nettimeclockauto.PushWorkServer");
            mainIntent.addCategory("android.intent.category.DEFAULT");
            mainIntent.putExtra("tag","boot_start  ");
            context.startService(mainIntent);
        }
    }
}
