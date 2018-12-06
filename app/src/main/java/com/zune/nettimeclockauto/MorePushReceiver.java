package com.zune.nettimeclockauto;

import android.content.Context;

import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

/**
 * Created by Administrator on 2017/4/11.
 */

public class MorePushReceiver extends PushMessageReceiver {


    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {}

    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {}

    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {}

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {}

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {}
}
