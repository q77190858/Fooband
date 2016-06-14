package com.foogeez.dialog;

import com.foogeez.configuration.Configuration.AlarmConfiguration;
import com.foogeez.fooband.R;
import com.grdn.wheels.AbstractWheel;
import com.grdn.wheels.OnWheelChangedListener;
import com.grdn.wheels.adapters.NumericWheelAdapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.CheckBox;

public class AlarmSettingDialog extends SettingDialog {
    //private final static String TAG = AlarmSettingDialog.class.getSimpleName();
    
    Context context;

	private AlarmConfiguration mConfig = null;
	private OnConfirmListener listener = null; 
	
    public AlarmSettingDialog(Context context, AlarmConfiguration config) {
        super(context);
        this.context = context;
        mConfig = config;
    }
    
    protected AlarmSettingDialog(Context context, AlarmConfiguration config, int theme) {
		super(context,theme);
        this.context = context;
        mConfig = config;
    }
    
    public interface OnConfirmListener {
    	public void OnConfirm(AlarmConfiguration config);
    };
    
    public void setOnConfirmListener( OnConfirmListener listener ) {
    	this.listener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_setting_alarm);
        
        AbstractWheel hours = (AbstractWheel) findViewById(R.id.alarm_hours);
        hours.setViewAdapter(new NumericWheelAdapter(context, 0, 23));
        hours.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
				mConfig.setHour(newValue);
			}
        });
        hours.setCyclic(true);
        hours.setCurrentItem(mConfig.getHour());

        AbstractWheel mins = (AbstractWheel) findViewById(R.id.alarm_minutes);
        mins.setViewAdapter(new NumericWheelAdapter(context, 0, 59, "%02d"));
        mins.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
				mConfig.setMinute(newValue);
			}
        });
        mins.setCyclic(true);
        mins.setCurrentItem(mConfig.getMinute());
        
        initYesNo();
        initCyclic(mConfig);
        initWeekSetting(mConfig);
    }
    
	private void initYesNo() {
		mTextViewConfirm = (TextView)findViewById(R.id.id_tv_setting_confirm);
		mTextViewConfirm.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( listener != null )
					listener.OnConfirm(mConfig);
				AlarmSettingDialog.this.dismiss();
			}
        });
        
		mTextViewCancel = (TextView)findViewById(R.id.id_tv_setting_cancel);
		mTextViewCancel.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlarmSettingDialog.this.dismiss();
			}
        });
	}
    
	private void initCyclic(AlarmConfiguration config) {
		CheckBox cyclic = ((CheckBox)findViewById(R.id.id_cb_alarm_cyclic));
		cyclic.setChecked(config.isCycliced());
		cyclic.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( ((CheckBox)v).isChecked() ) mConfig.setCycliced(true);
				else mConfig.setCycliced(false);
			}
        });
	}
	
	private void initWeekSetting(AlarmConfiguration config) {
		TextView week0 = ((TextView)findViewById(R.id.id_tv_week0));
		if( config.isWeek0Checked() ) {
			week0.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			week0.setTextColor(context.getResources().getColor(R.color.blue_medion));
		}
		else {
			week0.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			week0.setTextColor(context.getResources().getColor(R.color.gray));
		}
		week0.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				mConfig.setWeekChecked(0, !mConfig.isWeek0Checked());
				if( mConfig.isWeek0Checked() ) {
					((TextView)v).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
					((TextView)v).setTextColor(context.getResources().getColor(R.color.blue_medion));
				}
				else {
					((TextView)v).setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
					((TextView)v).setTextColor(context.getResources().getColor(R.color.gray));
				}
			}
        });
        
		TextView week1 = ((TextView)findViewById(R.id.id_tv_week1));
		if( config.isWeek1Checked() ) {
			week1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			week1.setTextColor(context.getResources().getColor(R.color.blue_medion));
		}
		else {
			week1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			week1.setTextColor(context.getResources().getColor(R.color.gray));
		}
		week1.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				mConfig.setWeekChecked(1, !mConfig.isWeek1Checked());
				if( mConfig.isWeek1Checked() ) {
					((TextView)v).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
					((TextView)v).setTextColor(context.getResources().getColor(R.color.blue_medion));
				}
				else {
					((TextView)v).setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
					((TextView)v).setTextColor(context.getResources().getColor(R.color.gray));
				}
			}
        });
        
		TextView week2 = ((TextView)findViewById(R.id.id_tv_week2));
		if( config.isWeek2Checked() ) {
			week2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			week2.setTextColor(context.getResources().getColor(R.color.blue_medion));
		}
		else {
			week2.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			week2.setTextColor(context.getResources().getColor(R.color.gray));
		}
		week2.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				mConfig.setWeekChecked(2, !mConfig.isWeek2Checked());
				if( mConfig.isWeek2Checked() ) {
					((TextView)v).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
					((TextView)v).setTextColor(context.getResources().getColor(R.color.blue_medion));
				}
				else {
					((TextView)v).setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
					((TextView)v).setTextColor(context.getResources().getColor(R.color.gray));
				}
			}
        });
        
		TextView week3 = ((TextView)findViewById(R.id.id_tv_week3));
		if( config.isWeek3Checked() ) {
			week3.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			week3.setTextColor(context.getResources().getColor(R.color.blue_medion));
		}
		else {
			week3.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			week3.setTextColor(context.getResources().getColor(R.color.gray));
		}
		week3.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				mConfig.setWeekChecked(3, !mConfig.isWeek3Checked());
				if( mConfig.isWeek3Checked() ) {
					((TextView)v).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
					((TextView)v).setTextColor(context.getResources().getColor(R.color.blue_medion));
				}
				else {
					((TextView)v).setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
					((TextView)v).setTextColor(context.getResources().getColor(R.color.gray));
				}
			}
        });
        
		TextView week4 = ((TextView)findViewById(R.id.id_tv_week4));
		if( config.isWeek4Checked() ) {
			week4.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			week4.setTextColor(context.getResources().getColor(R.color.blue_medion));
		}
		else {
			week4.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			week4.setTextColor(context.getResources().getColor(R.color.gray));
		}
		week4.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				mConfig.setWeekChecked(4, !mConfig.isWeek4Checked());
				if( mConfig.isWeek4Checked() ) {
					((TextView)v).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
					((TextView)v).setTextColor(context.getResources().getColor(R.color.blue_medion));
				}
				else {
					((TextView)v).setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
					((TextView)v).setTextColor(context.getResources().getColor(R.color.gray));
				}
			}
        });
        
		TextView week5 = ((TextView)findViewById(R.id.id_tv_week5));
		if( config.isWeek5Checked() ) {
			week5.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			week5.setTextColor(context.getResources().getColor(R.color.blue_medion));
		}
		else {
			week5.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			week5.setTextColor(context.getResources().getColor(R.color.gray));
		}
		week5.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				mConfig.setWeekChecked(5, !mConfig.isWeek5Checked());
				if( mConfig.isWeek5Checked() ) {
					((TextView)v).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
					((TextView)v).setTextColor(context.getResources().getColor(R.color.blue_medion));
				}
				else {
					((TextView)v).setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
					((TextView)v).setTextColor(context.getResources().getColor(R.color.gray));
				}
			}
        });
        
		TextView week6 = ((TextView)findViewById(R.id.id_tv_week6));
		if( config.isWeek6Checked() ) {
			week6.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			week6.setTextColor(context.getResources().getColor(R.color.blue_medion));
		}
		else {
			week6.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			week6.setTextColor(context.getResources().getColor(R.color.gray));
		}
		week6.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				mConfig.setWeekChecked(6, !mConfig.isWeek6Checked());
				if( mConfig.isWeek6Checked() ) {
					((TextView)v).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
					((TextView)v).setTextColor(context.getResources().getColor(R.color.blue_medion));
				}
				else {
					((TextView)v).setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
					((TextView)v).setTextColor(context.getResources().getColor(R.color.gray));
				}
			}
        });
		
	}
	

}
