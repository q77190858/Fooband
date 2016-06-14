package com.foogeez.notification;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.foogeez.notification.model.MessageBean;
import com.foogeez.notification.model.Messenger;
import com.foogeez.services.CentralService;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationsService extends NotificationListenerService {
    private static final String TAG = NotificationsService.class
            .getName();

    private NLServiceReceiver nlservicereciver;
    public static final String ACTION_ACTIONS_ACTIVE_NOTIFICATIONS =
            "com.foogeez.services.NotificationsService.ACTION_ACTIONS_ACTIVE_NOTIFICATIONS";

    public static final String EXTRA_DATA = "extra_data";
    public static final String EXTRA_MESSAGE = "extra_message";
    public static boolean ENABLED = false;

    @Override
    public void onCreate() {
        super.onCreate();

        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CentralService.ACTION_ACTIONS_ACTIVE_NOTIFICATIONS);
        registerReceiver(nlservicereciver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nlservicereciver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!ENABLED)
            return;

        Log.d(TAG, "Incoming notification!");
        Log.d(TAG, "Ticker: " + sbn.getNotification().tickerText);
        Log.d(TAG, "package: " + sbn.getPackageName());
        MessageBean message = Messenger.getMessage(sbn.getPackageName(),
                sbn.getNotification());

        if (message == null)
            return;

        Log.i(TAG, "Successfully parsed message:");
        Log.i(TAG, message.toString());

        Intent messageIntent = new Intent(CentralService.ACTION_ACTIONS_NOTIFICATION_POSTED);
        messageIntent.putExtra(EXTRA_MESSAGE, message);
        sendBroadcast(messageIntent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "onNotificationRemoved!"+" pkg="+sbn.getPackageName());
        // 移除通知
        String pkg = sbn.getPackageName();
//        List<String> lStrings = getInterstActiveNotifies();
//        for (String string : lStrings) {
//            Log.d(TAG, "package interset=" + string);
//        }
//        if (lStrings.size() == 0)
//            return;
//
//        if (lStrings.contains(pkg)) {
//            // still live in notification bar
//        } else {
        
            Messenger messenger = Messenger.getSourceMessenger(pkg);
            if (messenger==null) {
                return;
            }
        
            Intent messageIntent = new Intent(CentralService.ACTION_ACTIONS_NOTIFICATION_REMOVED);
            MessageBean message = new MessageBean();
            message.app = sbn.getPackageName();
            message.dataType = Messenger.getDataType(pkg);
            messageIntent.putExtra(EXTRA_MESSAGE, message);
            sendBroadcast(messageIntent);

            Log.d(TAG, "package removed: " + sbn.getPackageName());
//        }
    }

    class NLServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent active = new Intent(NotificationsService.ACTION_ACTIONS_ACTIVE_NOTIFICATIONS);
            List<String> interests = getInterstActiveNotifies();
            int count = interests == null ? 0 : interests.size();
            Log.d(TAG, "count=" + count);
            active.putExtra(NotificationsService.EXTRA_DATA, count);
            sendBroadcast(active);
        }
    }

    private List<String> getInterstActiveNotifies() {
        List<String> resulst = new ArrayList<String>();
        StatusBarNotification[] statusBarNotifications = getActiveNotifications();
        for (StatusBarNotification statusBarNotification : statusBarNotifications) {
            Log.i(TAG, "active="+statusBarNotification.getPackageName());
            Messenger messenger = Messenger.getSourceMessenger(statusBarNotification.getPackageName());
            if (messenger != null) {
                resulst.add(statusBarNotification.getPackageName());
            }
            resulst.add(statusBarNotification.getPackageName());
        }

        return resulst;
    }

}
