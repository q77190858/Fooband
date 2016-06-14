package com.foogeez.dialog;

import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;


public class ConfigRspDialog {

	Toast mToast;
	Object mObject;
	
	Method mShow, mHide;
	
    private Handler mHandler = new Handler();

	@SuppressLint("ShowToast")
	public ConfigRspDialog(Context context, String string) {
		mToast = Toast.makeText(context, string, Toast.LENGTH_SHORT);
		//mToast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
	}
	
	public void show(int milseconds) {
		if( milseconds > 0 ) {
			mToast.setDuration(Toast.LENGTH_SHORT);
			mHandler.postDelayed(task, milseconds);
			Log.e("DEBUG_TO", "DELAY TO CLOSE");
		}
		else {
			mToast.setDuration(Toast.LENGTH_SHORT);
		}
		mToast.show();
	}
	
	public void cancel() {
		mToast.cancel();
	}
	
    private Runnable task = new Runnable() {
        @Override
        public void run() {
        	cancel();
        }
    };
	
}
