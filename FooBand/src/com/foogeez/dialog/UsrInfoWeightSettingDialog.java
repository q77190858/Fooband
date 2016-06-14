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

public class UsrInfoWeightSettingDialog extends SettingDialog {
	
    Context context;
	private UsrInfoConfiguration mConfig = null;
	private OnConfirmListener listener = null; 
	
	private final static int MIN_WEIGHT = 25;
	private final static int MAX_WEIGHT = 300;
	
	public UsrInfoWeightSettingDialog(Context context, UsrInfoConfiguration config) {
		super(context);
		mConfig = config;
        this.context = context;
	}
	
    public UsrInfoWeightSettingDialog(Context context, UsrInfoConfiguration config, int theme) {
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
        this.setContentView(R.layout.dialog_setting_usr_info_weight);
        
        AbstractWheel weight = (AbstractWheel) findViewById(R.id.usr_info_weight);
        NumericWheelAdapter adapter = new NumericWheelAdapter(context, MIN_WEIGHT, MAX_WEIGHT, "%d");
        adapter.setItemResource(R.layout.wheel_text_centered);
        adapter.setItemTextResource(R.id.text);
        weight.setViewAdapter(adapter);
        weight.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
				//mConfig.setDateTime(1000);
				mConfig.setWeight(newValue+MIN_WEIGHT);
//				Log.i("UsrInfoWeightSettingDialog", "mConfig.setWeight----->" + (newValue+MIN_WEIGHT));
			}
        });
        weight.setCyclic(false);
        if (mConfig.getWeight() == 0) {
        	 weight.setCurrentItem(25);       //Robin  --- 20151117  设置当前对话框滚轮的默认数值 
		}
        weight.setCurrentItem(mConfig.getWeight()-MIN_WEIGHT); // getCurrent;
        
//        Log.i("UsrInfoWeightSettingDialog", "steps.setCurrentItem----->" + (mConfig.getWeight()-MIN_WEIGHT));
        
        initYesNo();
    }
    
	private void initYesNo() {
		mTextViewConfirm = (TextView)findViewById(R.id.id_tv_setting_confirm);
		mTextViewConfirm.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( listener != null )
					listener.OnConfirm(mConfig);
				UsrInfoWeightSettingDialog.this.dismiss();
			}
        });
        
		mTextViewCancel = (TextView)findViewById(R.id.id_tv_setting_cancel);
		mTextViewCancel.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				UsrInfoWeightSettingDialog.this.dismiss();
			}
        });
	}
}
