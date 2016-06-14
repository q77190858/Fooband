package com.foogeez.dialog;

import com.foogeez.configuration.Configuration.SedentaryConfiguration;
import com.foogeez.fooband.R;
import com.grdn.wheels.AbstractWheel;
import com.grdn.wheels.OnWheelChangedListener;
import com.grdn.wheels.adapters.NumericWheelAdapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SedentarySettingDialog extends SettingDialog {

    Context context;
	private SedentaryConfiguration mConfig = null;
	private OnConfirmListener listener = null; 
	
	public SedentarySettingDialog(Context context, SedentaryConfiguration config) {
		super(context);
		mConfig = config;
        this.context = context;
	}
	
    protected SedentarySettingDialog(Context context, SedentaryConfiguration config, int theme) {
		super(context, theme);
		mConfig = config;
        this.context = context;
    }
    
    public interface OnConfirmListener {
    	public void OnConfirm(SedentaryConfiguration config);
    };
    
    public void setOnConfirmListener( OnConfirmListener listener ) {
    	this.listener = listener;
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_setting_sedentary);
        
        final AbstractWheel hours = (AbstractWheel) findViewById(R.id.sedentary_hours);
        final AbstractWheel mins = (AbstractWheel) findViewById(R.id.sedentary_minutes);
        
        NumericWheelAdapter hourAdapter = new NumericWheelAdapter(context, 0, 7, "%d");
        hourAdapter.setItemResource(R.layout.wheel_text_centered);
        hourAdapter.setItemTextResource(R.id.text);
        hours.setViewAdapter(hourAdapter);
        hours.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
				mConfig.setHour(newValue);
				if( mConfig.getHour() <= 0 ) {
					if( mConfig.getMinute() < 20 ) {
						mConfig.setMinute(20);
						mins.setCurrentItem(mConfig.getMinute());
					}
				}
			}
        });
        hours.setCyclic(false);


        NumericWheelAdapter minAdapter = new NumericWheelAdapter(context, 0, 59, "%02d");
        minAdapter.setItemResource(R.layout.wheel_text_centered);
        minAdapter.setItemTextResource(R.id.text);
        mins.setViewAdapter(minAdapter);
        mins.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
				if( (mConfig.getHour() <= 0)&&(newValue < 20) ) {
					mConfig.setMinute(20);
					mins.setCurrentItem(mConfig.getMinute());
				}
				else {
					mConfig.setMinute(newValue);
				}
			}
        });
        mins.setCyclic(false);
        
        hours.setCurrentItem(mConfig.getHour()); // getCurrent;
        mins.setCurrentItem(mConfig.getMinute());

        initYesNo();
    }
    
	private void initYesNo() {
		mTextViewConfirm = (TextView)findViewById(R.id.id_tv_setting_confirm);
		mTextViewConfirm.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( listener != null )
					listener.OnConfirm(mConfig);
				SedentarySettingDialog.this.dismiss();
			}
        });
        
		mTextViewCancel = (TextView)findViewById(R.id.id_tv_setting_cancel);
		mTextViewCancel.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				SedentarySettingDialog.this.dismiss();
			}
        });
	}
    

}
