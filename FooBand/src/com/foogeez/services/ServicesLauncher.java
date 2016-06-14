package com.foogeez.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServicesLauncher extends BroadcastReceiver { 
	
    //private PendingIntent mAlarmSender; 
//    static final String action_boot="android.intent.action.BOOT_COMPLETED"; 
    
    @Override 
    public void onReceive(Context context, Intent intent) { 
    	Log.i("ServicesLauncher--", " 启动--Robin-----BroadcastReceiver");
    	
//    	if (intent.getAction().equals(action_boot)) {
	        // 在这里干你想干的事（启动一个Service，Activity等），本例是启动一个定时调度程序，每30分钟启动一个Service去更新数据 
	        //mAlarmSender = PendingIntent.getService(context, 0, new Intent(context, BackgroundService.class), 0); 
	        //long firstTime = SystemClock.elapsedRealtime(); 
	        //AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE); 
	        //am.cancel(mAlarmSender); 
	        //am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 3 * 1000, mAlarmSender); 
//    		Intent it = new Intent(context, CentralService.class);
//    		context.startService(it);
//    	}
    }
}

/** 
public class ServicesLauncher extends BroadcastReceiver {
	
    static final String action_boot="android.intent.action.BOOT_COMPLETED"; 
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(action_boot)) {
            Log.v("TAG", "开机自动服务自动启动....."); 
            context.startService(new Intent("com.foogeez.services.BackgroundService")); // 调用 Service
        }
    }
    
}
/**/

