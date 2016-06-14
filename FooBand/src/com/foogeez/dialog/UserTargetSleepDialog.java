
package com.foogeez.dialog;

import com.foogeez.configuration.Configuration.UsrTargetConfiguration;
import com.foogeez.fooband.R;
import com.grdn.wheels.AbstractWheel;
import com.grdn.wheels.OnWheelChangedListener;
import com.grdn.wheels.adapters.NumericWheelAdapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class UserTargetSleepDialog extends SettingDialog {
	private final static String TAG = UserTargetSleepDialog.class.getSimpleName();

    Context context;
	private UsrTargetConfiguration mConfig = null;
	private OnConfirmListener listener = null; 
	
	private final static int MIN_HOURS = 5;
	private final static int MAX_HOURS = 10;
	
	public UserTargetSleepDialog(Context context, UsrTargetConfiguration config) {
		super(context);
		mConfig = config;
        this.context = context;
	}
	
    public UserTargetSleepDialog(Context context, UsrTargetConfiguration config, int theme) {
		super(context, theme);
		mConfig = config;
        this.context = context;
    }
    
    public interface OnConfirmListener {
    	public void OnConfirm(UsrTargetConfiguration config);
    };
    
    public void setOnConfirmListener( OnConfirmListener listener ) {
    	this.listener = listener;
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_setting_sleep);
        
        AbstractWheel sleepHours = (AbstractWheel) findViewById(R.id.setting_sleep_number);
        NumericWheelAdapter adapter = new NumericWheelAdapter(context, MIN_HOURS, MAX_HOURS, "%01d");
        adapter.setItemResource(R.layout.wheel_text_centered);
        adapter.setItemTextResource(R.id.text);
        sleepHours.setViewAdapter(adapter);
        sleepHours.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
				//mConfig.setDateTime(1000);
				mConfig.setHours(newValue+MIN_HOURS);
				Log.i(TAG, "mConfig.setHours----->" + (newValue+MIN_HOURS));
			}
        });
        sleepHours.setCyclic(false);
        sleepHours.setCurrentItem(mConfig.getHours()-MIN_HOURS); // getCurrent;
        
        Log.i(TAG, "steps.setCurrentItem----->" + (mConfig.getHours()-MIN_HOURS));
        
        initYesNo();
    }
    
	private void initYesNo() {
		mTextViewConfirm = (TextView)findViewById(R.id.id_tv_setting_confirm);
		mTextViewConfirm.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( listener != null )
					listener.OnConfirm(mConfig);
				UserTargetSleepDialog.this.dismiss();
			}
        });
        
		mTextViewCancel = (TextView)findViewById(R.id.id_tv_setting_cancel);
		mTextViewCancel.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				UserTargetSleepDialog.this.dismiss();
			}
        });
	}
	
	

}
