package com.foogeez.dialog;

import com.foogeez.configuration.Configuration.UsrTargetConfiguration;
import com.foogeez.fooband.R;
import com.grdn.wheels.AbstractWheel;
import com.grdn.wheels.OnWheelChangedListener;
import com.grdn.wheels.adapters.StepsWheelAdapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class UserTargetStepsDialog extends SettingDialog {
	private final static String TAG = UserTargetStepsDialog.class.getSimpleName();

    Context context;
	private UsrTargetConfiguration mConfig = null;
	private OnConfirmListener listener = null; 
	
	private final static int MIN_STEPS = 1000;
	private final static int MAX_STEPS = 100000;
	
	public UserTargetStepsDialog(Context context, UsrTargetConfiguration config) {
		super(context);
		mConfig = config;
        this.context = context;
	}
	
    public UserTargetStepsDialog(Context context, UsrTargetConfiguration config, int theme) {
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
        this.setContentView(R.layout.dialog_setting_steps);
        
        AbstractWheel steps = (AbstractWheel) findViewById(R.id.setting_steps_number);
        StepsWheelAdapter adapter = new StepsWheelAdapter(context, MIN_STEPS, MAX_STEPS, "%04d");
        adapter.setItemResource(R.layout.wheel_text_centered);
        adapter.setItemTextResource(R.id.text);
        steps.setViewAdapter(adapter);
        
        
        steps.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
				//mConfig.setDateTime(1000);
				int[] steps = {1000, 3000, 5000, 7000, 9000,10000,20000,30000,40000,50000,60000,70000,80000,90000,100000};
				int stepsValue = steps[newValue];
				mConfig.setSteps(stepsValue);
				Log.i(TAG, "mConfig.setSteps---11-->" + (stepsValue));
			}
        });
        steps.setCyclic(false);
//        steps.setCurrentItem(mConfig.getSteps()-MIN_STEPS); // getCurrent;
        
        /** Name:Robin  Time:20150925  Function:设置弹出dialog时，当前数值对应的item*/
        if (mConfig.getSteps() == 1000) {
        	steps.setCurrentItem(0);
		}else if (mConfig.getSteps() == 3000) {
			steps.setCurrentItem(1);
		}else if (mConfig.getSteps() == 5000) {
			steps.setCurrentItem(2);
		}else if (mConfig.getSteps() == 7000) {
			steps.setCurrentItem(3);
		}else if (mConfig.getSteps() == 9000) {
			steps.setCurrentItem(4);
		}else if (mConfig.getSteps() > 9000) {
			steps.setCurrentItem(mConfig.getSteps()/10000+4);
		}
               
        Log.i(TAG, "steps.setCurrentItem--22--->" + mConfig.getSteps());
             
        initYesNo();
    }
    
	private void initYesNo() {
		mTextViewConfirm = (TextView)findViewById(R.id.id_tv_setting_confirm);
		mTextViewConfirm.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( listener != null )
					listener.OnConfirm(mConfig);
				UserTargetStepsDialog.this.dismiss();
			}
        });
        
		mTextViewCancel = (TextView)findViewById(R.id.id_tv_setting_cancel);
		mTextViewCancel.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				UserTargetStepsDialog.this.dismiss();
			}
        });
	}
	
}
