package com.foogeez.dialog;

import com.foogeez.configuration.Configuration.DisturbModeConfiguration;
import com.foogeez.fooband.R;
import com.grdn.wheels.AbstractWheel;
import com.grdn.wheels.OnWheelChangedListener;
import com.grdn.wheels.adapters.NumericWheelAdapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class DisturbModeSettingDialog extends SettingDialog {
	//private final static String TAG = CalendarSettingDialog.class.getSimpleName();
    
    private Context context;
    //private EditText mDisturbModeTitle = null;
    
	private OnConfirmListener listener = null; 
    private DisturbModeConfiguration mConfig = null;
    
	public DisturbModeSettingDialog(Context context, DisturbModeConfiguration config) {
		super(context);
		this.context = context;
		mConfig = config;
	}
	
    public DisturbModeSettingDialog(Context context, DisturbModeConfiguration config, int theme) {
		super(context,theme);
        this.context = context;
		mConfig = config;
    }
    
    public interface OnConfirmListener {
    	public void OnConfirm(DisturbModeConfiguration config);
    };
    
    public void setOnConfirmListener( OnConfirmListener listener ) {
    	this.listener = listener;
    }
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_setting_disturb_mode);
        
        final AbstractWheel start = (AbstractWheel) findViewById(R.id.disturb_mode_start);
        start.setViewAdapter(new NumericWheelAdapter(context, 0, 23));
        start.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
				mConfig.setStart(newValue);
			}
        });
        start.setCyclic(true);
        start.setCurrentItem(mConfig.getStart());

        final AbstractWheel stop = (AbstractWheel) findViewById(R.id.disturb_mode_stop);
        stop.setViewAdapter(new NumericWheelAdapter(context, 0, 23));
        stop.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
				mConfig.setStop(newValue);
			}
        });
        stop.setCyclic(true);
        stop.setCurrentItem(mConfig.getStop());
        
        initYesNo();
        initDisturbModeTitle();
	}
    
	private void initYesNo() {
		mTextViewConfirm = (TextView)findViewById(R.id.id_tv_setting_confirm);
		mTextViewConfirm.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				//mLastCalendar.set(Calendar.HOUR_OF_DAY, mHour);
				//mLastCalendar.set(Calendar.MINUTE, mMinute);
				//mConfig.setDateTime((int)(mLastCalendar.getTimeInMillis()/1000));
				//Log.e("DEBUG_CALENDAR", Integer.toHexString(mConfig.getDateTime()));
				//mConfig.setTitle(mDisturbModeTitle.getText().toString());
				if( listener != null )
					listener.OnConfirm(mConfig);
				DisturbModeSettingDialog.this.dismiss();
			}
        });
        
		mTextViewCancel = (TextView)findViewById(R.id.id_tv_setting_cancel);
		mTextViewCancel.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				DisturbModeSettingDialog.this.dismiss();
			}
        });
	}
	
	private void initDisturbModeTitle() {
		//mDisturbModeTitle = ((EditText)findViewById(R.id.id_et_calendar_title));
        //mDisturbModeTitle.setText(context.getResources().getString(R.string.string_setting_no_disturb_time));
        //mDisturbModeTitle.clearFocus();
	}
    

}
