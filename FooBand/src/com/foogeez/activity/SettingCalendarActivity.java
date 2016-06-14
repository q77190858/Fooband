package com.foogeez.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.foogeez.configuration.LocalStorage;
import com.foogeez.configuration.Configuration.CalendarConfiguration;
import com.foogeez.dialog.CalendarSettingDialog;
import com.foogeez.fooband.R;
import com.foogeez.services.CentralService;
import com.grdn.util.Utils;
import com.grdn.widgets.SwitchButton;
import com.umeng.analytics.MobclickAgent;

public class SettingCalendarActivity extends SettingActivity {
    private final static String TAG = SettingCalendarActivity.class.getSimpleName();
    
    private CentralService mCentralService = null;
    
    private CalendarSettingDialog mCalendar0Setting;
    private CalendarSettingDialog mCalendar1Setting;
    private CalendarSettingDialog mCalendar2Setting;
    private CalendarSettingDialog mCalendar3Setting;
    private CalendarSettingDialog mCalendar4Setting;
    
    private LocalStorage mLocalStorage = new LocalStorage(SettingCalendarActivity.this);
    
    private CalendarConfiguration mCalendarConfig0;
    private CalendarConfiguration mCalendarConfig1;
    private CalendarConfiguration mCalendarConfig2;
    private CalendarConfiguration mCalendarConfig3;
    private CalendarConfiguration mCalendarConfig4;
    
    private TextView mCaldendarTitle0;
    private TextView mCaldendarTitle1;
    private TextView mCaldendarTitle2;
    private TextView mCaldendarTitle3;
    private TextView mCaldendarTitle4;
    
    private TextView mCaldendarDateTime0;
    private TextView mCaldendarDateTime1;
    private TextView mCaldendarDateTime2;
    private TextView mCaldendarDateTime3;
    private TextView mCaldendarDateTime4;
    
    private SwitchButton mCalendarActive0;
    private SwitchButton mCalendarActive1;
    private SwitchButton mCalendarActive2;
    private SwitchButton mCalendarActive3;
    private SwitchButton mCalendarActive4;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "SettingCalendarActivity --- onCreate");
    	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_calendar);
		initSettingTitle(R.string.string_function_agendar_reminder);
		
		if( !mLocalStorage.hasAnyAccount() ) {
    		Toast.makeText(
    				SettingCalendarActivity.this,
                    getResources().getString(R.string.string_please_register_first),
                    Toast.LENGTH_SHORT).show();
			
			super.UIfinish();
			return;
		}
		
		bindCentralService();
		
		mCaldendarTitle0 = (TextView)findViewById(R.id.id_tv_calendar0_title);
		mCaldendarTitle1 = (TextView)findViewById(R.id.id_tv_calendar1_title);
		mCaldendarTitle2 = (TextView)findViewById(R.id.id_tv_calendar2_title);
		mCaldendarTitle3 = (TextView)findViewById(R.id.id_tv_calendar3_title);
		mCaldendarTitle4 = (TextView)findViewById(R.id.id_tv_calendar4_title);

		mCaldendarDateTime0 = (TextView)findViewById(R.id.id_tv_calendar0_datetime);
		mCaldendarDateTime1 = (TextView)findViewById(R.id.id_tv_calendar1_datetime);
		mCaldendarDateTime2 = (TextView)findViewById(R.id.id_tv_calendar2_datetime);
		mCaldendarDateTime3 = (TextView)findViewById(R.id.id_tv_calendar3_datetime);
		mCaldendarDateTime4 = (TextView)findViewById(R.id.id_tv_calendar4_datetime);
		
		mCalendarConfig0 = mLocalStorage.restoreCalendarConfig(0);
		mCalendarConfig1 = mLocalStorage.restoreCalendarConfig(1);
		mCalendarConfig2 = mLocalStorage.restoreCalendarConfig(2);
		mCalendarConfig3 = mLocalStorage.restoreCalendarConfig(3);
		mCalendarConfig4 = mLocalStorage.restoreCalendarConfig(4);
		
		mCalendarActive0 = (SwitchButton)findViewById(R.id.id_sb_calendar0_active_flag);
		mCalendarActive0.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mCalendarConfig0.setActived(isChecked);
				updateCalendar0(mCalendarConfig0,true);
			}
		});
		
		mCalendarActive1 = (SwitchButton)findViewById(R.id.id_sb_calendar1_active_flag);
		mCalendarActive1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mCalendarConfig1.setActived(isChecked);
				updateCalendar1(mCalendarConfig1,true);
			}
		});
		
		mCalendarActive2 = (SwitchButton)findViewById(R.id.id_sb_calendar2_active_flag);
		mCalendarActive2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mCalendarConfig2.setActived(isChecked);
				updateCalendar2(mCalendarConfig2,true);
			}
		});
		
		mCalendarActive3 = (SwitchButton)findViewById(R.id.id_sb_calendar3_active_flag);
		mCalendarActive3.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mCalendarConfig3.setActived(isChecked);
				updateCalendar3(mCalendarConfig3,true);
			}
		});
		
		mCalendarActive4 = (SwitchButton)findViewById(R.id.id_sb_calendar4_active_flag);
		mCalendarActive4.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mCalendarConfig4.setActived(isChecked);
				updateCalendar4(mCalendarConfig4,true);
			}
		});
		
		initCalendar0(mCalendarConfig0);
		initCalendar1(mCalendarConfig1);
		initCalendar2(mCalendarConfig2);
		initCalendar3(mCalendarConfig3);
		initCalendar4(mCalendarConfig4);
		
		updateCalendar0(mCalendarConfig0, false);
		updateCalendar1(mCalendarConfig1, false);
		updateCalendar2(mCalendarConfig2, false);
		updateCalendar3(mCalendarConfig3, false);
		updateCalendar4(mCalendarConfig4, false);
		
    }
    
	private void updateCalendar0(CalendarConfiguration config, boolean save) {
		if( save ) {
			mLocalStorage.saveCalendarConfig(0, config);
		}
		mCalendarActive0.setChecked(config.isActived());
		mCaldendarTitle0.setText(config.getTitle());
		mCaldendarDateTime0.setText(Utils.utc2DateTime(config.getDateTime()));
		Log.e(TAG, Integer.toHexString(config.getDateTime()));
		
		if( config.isActived() ) {
			mCaldendarTitle0.setTextColor(getResources().getColor(R.color.black));
			mCaldendarDateTime0.setTextColor(getResources().getColor(R.color.black));
		}
		else {
			mCaldendarTitle0.setTextColor(getResources().getColor(R.color.gray));
			mCaldendarDateTime0.setTextColor(getResources().getColor(R.color.gray));
		}
		
		if( mCentralService != null ) {
			Log.e(TAG, "ENCODE: " + Utils.bytesToHexString(config.getEncode()));
			mCentralService.requestConnectionConfigFuncions(CentralService.DEVICE_CMD_ADDR_CALENDAR, config.getEncode());
		}
	}
	
	private void updateCalendar1(CalendarConfiguration config, boolean save) {
		if( save ) {
			mLocalStorage.saveCalendarConfig(1, config);
		}
		mCalendarActive1.setChecked(config.isActived());
		mCaldendarTitle1.setText(config.getTitle());
		mCaldendarDateTime1.setText(Utils.utc2DateTime(config.getDateTime()));
		
		if( config.isActived() ) {
			mCaldendarTitle1.setTextColor(getResources().getColor(R.color.black));
			mCaldendarDateTime1.setTextColor(getResources().getColor(R.color.black));
		}
		else {
			mCaldendarTitle1.setTextColor(getResources().getColor(R.color.gray));
			mCaldendarDateTime1.setTextColor(getResources().getColor(R.color.gray));
		}
		
		if( mCentralService != null ) {
			Log.e(TAG, "ENCODE: " + Utils.bytesToHexString(config.getEncode()));
			mCentralService.requestConnectionConfigFuncions(CentralService.DEVICE_CMD_ADDR_CALENDAR|0x0100, config.getEncode());
		}
	}

	private void updateCalendar2(CalendarConfiguration config, boolean save) {
		if( save ) {
			mLocalStorage.saveCalendarConfig(2, config);
		}
		mCalendarActive2.setChecked(config.isActived());
		mCaldendarTitle2.setText(config.getTitle());
		mCaldendarDateTime2.setText(Utils.utc2DateTime(config.getDateTime()));
		
		if( config.isActived() ) {
			mCaldendarTitle2.setTextColor(getResources().getColor(R.color.black));
			mCaldendarDateTime2.setTextColor(getResources().getColor(R.color.black));
		}
		else {
			mCaldendarTitle2.setTextColor(getResources().getColor(R.color.gray));
			mCaldendarDateTime2.setTextColor(getResources().getColor(R.color.gray));
		}
		
		if( mCentralService != null ) {
			Log.e(TAG, "ENCODE: " + Utils.bytesToHexString(config.getEncode()));
			mCentralService.requestConnectionConfigFuncions(CentralService.DEVICE_CMD_ADDR_CALENDAR|0x0200, config.getEncode());
		}
	}
	
	private void updateCalendar3(CalendarConfiguration config, boolean save) {
		if( save ) {
			mLocalStorage.saveCalendarConfig(3, config);
		}
		mCalendarActive3.setChecked(config.isActived());
		mCaldendarTitle3.setText(config.getTitle());
		mCaldendarDateTime3.setText(Utils.utc2DateTime(config.getDateTime()));
		
		if( config.isActived() ) {
			mCaldendarTitle3.setTextColor(getResources().getColor(R.color.black));
			mCaldendarDateTime3.setTextColor(getResources().getColor(R.color.black));
		}
		else {
			mCaldendarTitle3.setTextColor(getResources().getColor(R.color.gray));
			mCaldendarDateTime3.setTextColor(getResources().getColor(R.color.gray));
		}
		
		if( mCentralService != null ) {
			Log.e(TAG, "ENCODE: " + Utils.bytesToHexString(config.getEncode()));
			mCentralService.requestConnectionConfigFuncions(CentralService.DEVICE_CMD_ADDR_CALENDAR|0x0300, config.getEncode());
		}
	}
	
    private void updateCalendar4(CalendarConfiguration config, boolean save) {
		if( save ) {
			mLocalStorage.saveCalendarConfig(4, config);
		}
		mCalendarActive4.setChecked(config.isActived());
		mCaldendarTitle4.setText(config.getTitle());
		mCaldendarDateTime4.setText(Utils.utc2DateTime(config.getDateTime()));
		
		if( config.isActived() ) {
			mCaldendarTitle4.setTextColor(getResources().getColor(R.color.black));
			mCaldendarDateTime4.setTextColor(getResources().getColor(R.color.black));
		}
		else {
			mCaldendarTitle4.setTextColor(getResources().getColor(R.color.gray));
			mCaldendarDateTime4.setTextColor(getResources().getColor(R.color.gray));
		}
		
		if( mCentralService != null ) {
			Log.e(TAG, "ENCODE: " + Utils.bytesToHexString(config.getEncode()));
			mCentralService.requestConnectionConfigFuncions(CentralService.DEVICE_CMD_ADDR_CALENDAR|0x0400, config.getEncode());
		}
	}

	private void initCalendar0(final CalendarConfiguration configuration) {
	    ((RelativeLayout)findViewById(R.id.id_rl_setting_calendar0)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCalendar0Setting = new CalendarSettingDialog(SettingCalendarActivity.this, configuration);//, R.style.MyDialog);
		        Window dialogWindow = mCalendar0Setting.getWindow();
		        Log.i(TAG, "--------设置日历----11------"+dialogWindow);
		        
		        dialogWindow.setGravity(Gravity.CENTER);
		        mCalendar0Setting.setCanceledOnTouchOutside(false);
		        Log.i(TAG, "--------设置日历-----22-----");
		        mCalendar0Setting.setOnConfirmListener(new CalendarSettingDialog.OnConfirmListener() {
					@Override
					public void OnConfirm(CalendarConfiguration config) {
						config.setActived(true);
						Log.i(TAG, "--------设置日历-----33-----config.setActived(true)");
						updateCalendar0(config,true);
					}
				});
		        Log.i(TAG, "--------设置日历-----44-----mCalendar0Setting");
		        mCalendar0Setting.show();
		        Log.i(TAG, "--------设置日历-----55-----mCalendar0Setting");
			}
	    });
    }
    
    private void initCalendar1(final CalendarConfiguration configuration) {
	    ((RelativeLayout)findViewById(R.id.id_rl_setting_calendar1)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCalendar1Setting = new CalendarSettingDialog(SettingCalendarActivity.this, configuration);
		        Window dialogWindow = mCalendar1Setting.getWindow();
		        dialogWindow.setGravity(Gravity.CENTER);
		        mCalendar1Setting.setCanceledOnTouchOutside(false);
		        mCalendar1Setting.setOnConfirmListener(new CalendarSettingDialog.OnConfirmListener() {
					@Override
					public void OnConfirm(CalendarConfiguration config) {
						config.setActived(true);
						updateCalendar1(config,true);
					}
				});
		        mCalendar1Setting.show();
			}
	    });
    }
    
    private void initCalendar2(final CalendarConfiguration configuration) {
	    ((RelativeLayout)findViewById(R.id.id_rl_setting_calendar2)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCalendar2Setting = new CalendarSettingDialog(SettingCalendarActivity.this, configuration);
		        Window dialogWindow = mCalendar2Setting.getWindow();
		        dialogWindow.setGravity(Gravity.CENTER);
		        mCalendar2Setting.setCanceledOnTouchOutside(false);
		        mCalendar2Setting.setOnConfirmListener(new CalendarSettingDialog.OnConfirmListener() {
					@Override
					public void OnConfirm(CalendarConfiguration config) {
						config.setActived(true);
						updateCalendar2(config,true);
					}
				});
		        mCalendar2Setting.show();
			}
	    });
    }
    
    private void initCalendar3(final CalendarConfiguration configuration) {
	    ((RelativeLayout)findViewById(R.id.id_rl_setting_calendar3)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCalendar3Setting = new CalendarSettingDialog(SettingCalendarActivity.this, configuration);
		        Window dialogWindow = mCalendar3Setting.getWindow();
		        dialogWindow.setGravity(Gravity.CENTER);
		        mCalendar3Setting.setCanceledOnTouchOutside(false);
		        mCalendar3Setting.setOnConfirmListener(new CalendarSettingDialog.OnConfirmListener() {
					@Override
					public void OnConfirm(CalendarConfiguration config) {
						config.setActived(true);
						updateCalendar3(config,true);
					}
				});
		        mCalendar3Setting.show();
			}
	    });
    }
    
    private void initCalendar4(final CalendarConfiguration configuration) {
	    ((RelativeLayout)findViewById(R.id.id_rl_setting_calendar4)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCalendar4Setting = new CalendarSettingDialog(SettingCalendarActivity.this, configuration);
		        Window dialogWindow = mCalendar4Setting.getWindow();
		        dialogWindow.setGravity(Gravity.CENTER);
		        mCalendar4Setting.setCanceledOnTouchOutside(false);
		        mCalendar4Setting.setOnConfirmListener(new CalendarSettingDialog.OnConfirmListener() {
					@Override
					public void OnConfirm(CalendarConfiguration config) {
						config.setActived(true);
						updateCalendar4(config,true);
					}
				});
		        mCalendar4Setting.show();
			}
	    });
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	unbindCentralService();
    }
    
    private void bindCentralService() {
		Intent it = new Intent(SettingCalendarActivity.this, CentralService.class);
		bindService(it, mServiceConnection, BIND_AUTO_CREATE);
	}
    
    private void unbindCentralService() {
		unbindService(mServiceConnection);
	}
    
	// Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
        	Log.i(TAG, "CentralServiceConnection&SettingCalendarActivity --- onServiceConnected");
        	mCentralService = ((CentralService.LocalBinder)service).getService();
        }

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mCentralService = null;
		}
    };
    
	/** Name:Robin  Time:20150928  Function: 友盟session的统计  */
    @Override
    public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
    @Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

    
}
