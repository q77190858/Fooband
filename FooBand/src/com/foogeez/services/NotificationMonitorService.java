package com.foogeez.services;

import com.grdn.util.Utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationMonitorService extends NotificationListenerService {
    private final static String TAG = NotificationMonitorService.class.getSimpleName();

    @Override  
    public void onCreate() {  
    	Log.i(TAG, "NotificationMonitorService --- onCreate");
    	super.onCreate();
    }
    
    @Override
    public void onDestroy() {
    	Log.i(TAG, "NotificationMonitorService --- onDestroy");
    	super.onDestroy();
    }
    
	@Override
	public IBinder onBind(Intent intent) {
    	Log.e(TAG, "NotificationMonitorService --- onbind");
    	/*
	    new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.e(TAG, "getCurrentNotifications=" + getCurrentNotifications().length);
			}
	    }, 3000);
    	*/
		return super.onBind(intent);
	}
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		Notification mNotification=sbn.getNotification();
		Log.e(TAG, "onNotificationPosted");
		Log.e(TAG, "StatusBarNotification Posted Time:  " + Utils.utc2DateTime((int) (sbn.getPostTime()/1000)) + "\r\n" + 
				   "StatusBarNotification Posted id:    " + sbn.getPackageName() + "\r\n" + 
				   "StatusBarNotification Posted Ticker:" + mNotification.tickerText + "\r\n" + 
				   "StatusBarNotification Posted Title: " + mNotification.extras.getString(Notification.EXTRA_TITLE) + "\r\n" +
				   "StatusBarNotification Posted Text:  " + mNotification.extras.getString(Notification.EXTRA_TEXT) + "\r\n" +
				   "StatusBarNotification Posted SText: " + mNotification.extras.getString(Notification.EXTRA_SUB_TEXT) + "\r\n" +
				   "StatusBarNotification Posted IText: " + mNotification.extras.getString(Notification.EXTRA_INFO_TEXT) + "\r\n" +
				   "StatusBarNotification Posted UText: " + mNotification.extras.getString(Notification.EXTRA_SUMMARY_TEXT) //+ "\r\n"
					);
	
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		Log.e(TAG, "onNotificationRemoved");
		Log.e(TAG, "StatusBarNotification Removed Time:" + Utils.utc2DateTime((int) (sbn.getPostTime()/1000)) + "\r\n" + 			   
					"StatusBarNotification Removed id: " + sbn.getPackageName()
					);
	}
	
	private StatusBarNotification[] mNotifications = null;
	
    public StatusBarNotification[] getCurrentNotifications() {
        try {
	    	mNotifications = getActiveNotifications();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        return mNotifications;
    }

    

}
