package com.foogeez.dialog;

import com.foogeez.configuration.Configuration.UsrInfoConfiguration;
import com.foogeez.fooband.R;
import com.grdn.wheels.AbstractWheel;
import com.grdn.wheels.OnWheelChangedListener;
import com.grdn.wheels.adapters.NumericWheelAdapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class UsrInfoHeightSettingDialog extends SettingDialog {

    Context context;
	private UsrInfoConfiguration mConfig = null;
	private OnConfirmListener listener = null; 
	
	private final static int MIN_HEIGHT = 100;
	private final static int MAX_HEIGHT = 300;
	
	public UsrInfoHeightSettingDialog(Context context, UsrInfoConfiguration config) {
		super(context);
		mConfig = config;
        this.context = context;
	}
	
    public UsrInfoHeightSettingDialog(Context context, UsrInfoConfiguration config, int theme) {
		super(context, theme);
		mConfig = config;
        this.context = context;
    }
    
    public interface OnConfirmListener {
    	public void OnConfirm(UsrInfoConfiguration config);
    };
    
    public void setOnConfirmListener( OnConfirmListener listener ) {
    	this.listener = listener;
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_setting_usr_info_height);
        
        AbstractWheel height = (AbstractWheel) findViewById(R.id.usr_info_height);
        NumericWheelAdapter adapter = new NumericWheelAdapter(context, MIN_HEIGHT, MAX_HEIGHT, "%03d");
        adapter.setItemResource(R.layout.wheel_text_centered);
        adapter.setItemTextResource(R.id.text);
        height.setViewAdapter(adapter);
        height.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
				//mConfig.setDateTime(1000);
				mConfig.setHeight(newValue+MIN_HEIGHT);
			}
        });
        height.setCyclic(false);
        if (mConfig.getHeight() == 0) {
        	 height.setCurrentItem(60); 
		}
        height.setCurrentItem(mConfig.getHeight()-MIN_HEIGHT); // getCurrent;
//        Log.i("Robin", " --------height.setCurrentItem"+(mConfig.getHeight()-MIN_HEIGHT));
        initYesNo();
    }
    
	private void initYesNo() {
		mTextViewConfirm = (TextView)findViewById(R.id.id_tv_setting_confirm);
		mTextViewConfirm.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( listener != null )
					listener.OnConfirm(mConfig);
				UsrInfoHeightSettingDialog.this.dismiss();
			}
        });
        
		mTextViewCancel = (TextView)findViewById(R.id.id_tv_setting_cancel);
		mTextViewCancel.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				UsrInfoHeightSettingDialog.this.dismiss();
			}
        });
	}
	
	

}
