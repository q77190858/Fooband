package com.foogeez.activity;

import com.foogeez.configuration.LocalStorage;
import com.foogeez.configuration.Configuration.AlarmConfiguration;
import com.foogeez.dialog.AlarmSettingDialog;
import com.foogeez.fooband.R;
import com.foogeez.services.CentralService;
import com.grdn.util.Utils;
import com.grdn.widgets.SwitchButton;
import com.umeng.analytics.MobclickAgent;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingAlarmActivity extends SettingActivity {
    private final static String TAG = SettingAlarmActivity.class.getSimpleName();
    
    private CentralService mCentralService = null;
    
    private TextView mAlarmTime0;
    private TextView mAlarmTime1;
    private TextView mAlarmTime2;
    private TextView mAlarmTime3;
    private TextView mAlarmTime4;
    
    private TextView mAlarmCyclic0;
    private TextView mAlarmCyclic1;
    private TextView mAlarmCyclic2;
    private TextView mAlarmCyclic3;
    private TextView mAlarmCyclic4;
    
    private TextView[] mAlarmWeekPicker0 = new TextView[7] ;
    private TextView[] mAlarmWeekPicker1 = new TextView[7] ;
    private TextView[] mAlarmWeekPicker2 = new TextView[7] ;
    private TextView[] mAlarmWeekPicker3 = new TextView[7] ;
    private TextView[] mAlarmWeekPicker4 = new TextView[7] ;

    private SwitchButton mAlarmActive0;
    private SwitchButton mAlarmActive1;
    private SwitchButton mAlarmActive2;
    private SwitchButton mAlarmActive3;
    private SwitchButton mAlarmActive4;
    
    private AlarmConfiguration mAlarm0Config;
    private AlarmConfiguration mAlarm1Config;
    private AlarmConfiguration mAlarm2Config;
    private AlarmConfiguration mAlarm3Config;
    private AlarmConfiguration mAlarm4Config;
    
    private AlarmSettingDialog mAlarm0Setting;
    private AlarmSettingDialog mAlarm1Setting;
    private AlarmSettingDialog mAlarm2Setting;
    private AlarmSettingDialog mAlarm3Setting;
    private AlarmSettingDialog mAlarm4Setting;
    private LocalStorage mLocalStorage = new LocalStorage(SettingAlarmActivity.this);
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "SettingAlarmActivity --- onCreate");
    	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_alarm);
		initSettingTitle(R.string.string_function_alarm_reminder);
		
		if( !mLocalStorage.hasAnyAccount() ) {
			
    		Toast.makeText(
    				SettingAlarmActivity.this,
                    getResources().getString(R.string.string_please_register_first),
                    Toast.LENGTH_SHORT).show();
			
			super.UIfinish();
			return;
		}
		
		mAlarmTime0 = (TextView)findViewById(R.id.id_tv_alarm0_time);
		mAlarmTime1 = (TextView)findViewById(R.id.id_tv_alarm1_time);
		mAlarmTime2 = (TextView)findViewById(R.id.id_tv_alarm2_time);
		mAlarmTime3 = (TextView)findViewById(R.id.id_tv_alarm3_time);
		mAlarmTime4 = (TextView)findViewById(R.id.id_tv_alarm4_time);
		
		mAlarmCyclic0 = (TextView)findViewById(R.id.id_tv_alarm0_week_cyclic);
		mAlarmCyclic1 = (TextView)findViewById(R.id.id_tv_alarm1_week_cyclic);
		mAlarmCyclic2 = (TextView)findViewById(R.id.id_tv_alarm2_week_cyclic);
		mAlarmCyclic3 = (TextView)findViewById(R.id.id_tv_alarm3_week_cyclic);
		mAlarmCyclic4 = (TextView)findViewById(R.id.id_tv_alarm4_week_cyclic);
		
		mAlarmWeekPicker0[0] = (TextView)findViewById(R.id.id_tv_alarm0_week0);
		mAlarmWeekPicker0[1] = (TextView)findViewById(R.id.id_tv_alarm0_week1);
		mAlarmWeekPicker0[2] = (TextView)findViewById(R.id.id_tv_alarm0_week2);
		mAlarmWeekPicker0[3] = (TextView)findViewById(R.id.id_tv_alarm0_week3);
		mAlarmWeekPicker0[4] = (TextView)findViewById(R.id.id_tv_alarm0_week4);
		mAlarmWeekPicker0[5] = (TextView)findViewById(R.id.id_tv_alarm0_week5);
		mAlarmWeekPicker0[6] = (TextView)findViewById(R.id.id_tv_alarm0_week6);
		
		mAlarmWeekPicker1[0] = (TextView)findViewById(R.id.id_tv_alarm1_week0);
		mAlarmWeekPicker1[1] = (TextView)findViewById(R.id.id_tv_alarm1_week1);
		mAlarmWeekPicker1[2] = (TextView)findViewById(R.id.id_tv_alarm1_week2);
		mAlarmWeekPicker1[3] = (TextView)findViewById(R.id.id_tv_alarm1_week3);
		mAlarmWeekPicker1[4] = (TextView)findViewById(R.id.id_tv_alarm1_week4);
		mAlarmWeekPicker1[5] = (TextView)findViewById(R.id.id_tv_alarm1_week5);
		mAlarmWeekPicker1[6] = (TextView)findViewById(R.id.id_tv_alarm1_week6);
		
		mAlarmWeekPicker2[0] = (TextView)findViewById(R.id.id_tv_alarm2_week0);
		mAlarmWeekPicker2[1] = (TextView)findViewById(R.id.id_tv_alarm2_week1);
		mAlarmWeekPicker2[2] = (TextView)findViewById(R.id.id_tv_alarm2_week2);
		mAlarmWeekPicker2[3] = (TextView)findViewById(R.id.id_tv_alarm2_week3);
		mAlarmWeekPicker2[4] = (TextView)findViewById(R.id.id_tv_alarm2_week4);
		mAlarmWeekPicker2[5] = (TextView)findViewById(R.id.id_tv_alarm2_week5);
		mAlarmWeekPicker2[6] = (TextView)findViewById(R.id.id_tv_alarm2_week6);
		
		mAlarmWeekPicker3[0] = (TextView)findViewById(R.id.id_tv_alarm3_week0);
		mAlarmWeekPicker3[1] = (TextView)findViewById(R.id.id_tv_alarm3_week1);
		mAlarmWeekPicker3[2] = (TextView)findViewById(R.id.id_tv_alarm3_week2);
		mAlarmWeekPicker3[3] = (TextView)findViewById(R.id.id_tv_alarm3_week3);
		mAlarmWeekPicker3[4] = (TextView)findViewById(R.id.id_tv_alarm3_week4);
		mAlarmWeekPicker3[5] = (TextView)findViewById(R.id.id_tv_alarm3_week5);
		mAlarmWeekPicker3[6] = (TextView)findViewById(R.id.id_tv_alarm3_week6);
		
		mAlarmWeekPicker4[0] = (TextView)findViewById(R.id.id_tv_alarm4_week0);
		mAlarmWeekPicker4[1] = (TextView)findViewById(R.id.id_tv_alarm4_week1);
		mAlarmWeekPicker4[2] = (TextView)findViewById(R.id.id_tv_alarm4_week2);
		mAlarmWeekPicker4[3] = (TextView)findViewById(R.id.id_tv_alarm4_week3);
		mAlarmWeekPicker4[4] = (TextView)findViewById(R.id.id_tv_alarm4_week4);
		mAlarmWeekPicker4[5] = (TextView)findViewById(R.id.id_tv_alarm4_week5);
		mAlarmWeekPicker4[6] = (TextView)findViewById(R.id.id_tv_alarm4_week6);
		
		mAlarm0Config = mLocalStorage.restoreAlarmConfig(0);
		mAlarm1Config = mLocalStorage.restoreAlarmConfig(1);
		mAlarm2Config = mLocalStorage.restoreAlarmConfig(2);
		mAlarm3Config = mLocalStorage.restoreAlarmConfig(3);
		mAlarm4Config = mLocalStorage.restoreAlarmConfig(4);
		
		mAlarmActive0 = (SwitchButton)findViewById(R.id.id_sb_alarm0_active_flag);
		mAlarmActive0.setChecked(mAlarm0Config.isActived());
		mAlarmActive0.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mAlarm0Config.setActived(isChecked);
				upgdateAlarm0(mAlarm0Config, true);
			}
		});

		mAlarmActive1 = (SwitchButton)findViewById(R.id.id_sb_alarm1_active_flag);
		mAlarmActive1.setChecked(mAlarm1Config.isActived());
		mAlarmActive1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mAlarm1Config.setActived(isChecked);
				upgdateAlarm1(mAlarm1Config, true);
			}
		});
		
		mAlarmActive2 = (SwitchButton)findViewById(R.id.id_sb_alarm2_active_flag);
		mAlarmActive2.setChecked(mAlarm2Config.isActived());
		mAlarmActive2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mAlarm2Config.setActived(isChecked);
				upgdateAlarm2(mAlarm2Config, true);
			}
		});
		
		mAlarmActive3 = (SwitchButton)findViewById(R.id.id_sb_alarm3_active_flag);
		mAlarmActive3.setChecked(mAlarm3Config.isActived());
		mAlarmActive3.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mAlarm3Config.setActived(isChecked);
				upgdateAlarm3(mAlarm3Config, true);
			}
		});
		
		mAlarmActive4 = (SwitchButton)findViewById(R.id.id_sb_alarm4_active_flag);
		mAlarmActive4.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mAlarm4Config.setActived(isChecked);
				upgdateAlarm4(mAlarm4Config, true);
			}
		});
		
		/** java字符串格式化：String.format()方法的使用 ---->%02d:%02d 时间格式化，表示显示两位数，不足两位用0补上 */
		mAlarmTime0.setText(String.format("%02d:%02d", mAlarm0Config.getHour(), mAlarm0Config.getMinute()));
		mAlarmTime1.setText(String.format("%02d:%02d", mAlarm1Config.getHour(), mAlarm1Config.getMinute()));
		mAlarmTime2.setText(String.format("%02d:%02d", mAlarm2Config.getHour(), mAlarm2Config.getMinute()));
		mAlarmTime3.setText(String.format("%02d:%02d", mAlarm3Config.getHour(), mAlarm3Config.getMinute()));
		mAlarmTime4.setText(String.format("%02d:%02d", mAlarm4Config.getHour(), mAlarm4Config.getMinute()));
		
		mAlarmCyclic0.setText(mAlarm0Config.isCycliced()?R.string.string_alarm_dialog_circle_true:R.string.string_alarm_dialog_circle_false);
		mAlarmCyclic1.setText(mAlarm1Config.isCycliced()?R.string.string_alarm_dialog_circle_true:R.string.string_alarm_dialog_circle_false);
		mAlarmCyclic2.setText(mAlarm2Config.isCycliced()?R.string.string_alarm_dialog_circle_true:R.string.string_alarm_dialog_circle_false);
		mAlarmCyclic3.setText(mAlarm3Config.isCycliced()?R.string.string_alarm_dialog_circle_true:R.string.string_alarm_dialog_circle_false);
		mAlarmCyclic4.setText(mAlarm4Config.isCycliced()?R.string.string_alarm_dialog_circle_true:R.string.string_alarm_dialog_circle_false);
		
		initAlarm0(mAlarm0Config);
		initAlarm1(mAlarm1Config);
		initAlarm2(mAlarm2Config);
		initAlarm3(mAlarm3Config);
		initAlarm4(mAlarm4Config);
		
		upgdateAlarm0(mAlarm0Config, false);
		upgdateAlarm1(mAlarm1Config, false);
		upgdateAlarm2(mAlarm2Config, false);
		upgdateAlarm3(mAlarm3Config, false);
		upgdateAlarm4(mAlarm4Config, false);
		
		bindCentralService();
    }
    
    private void upgdateAlarm0(AlarmConfiguration config, boolean save) {
		if( save ) {
			mLocalStorage.saveAlarmConfig(0, config);
		}
		mAlarmActive0.setChecked(config.isActived());
		mAlarmTime0.setText(String.format("%02d:%02d", config.getHour(), config.getMinute()));
		mAlarmCyclic0.setText(config.isCycliced()?R.string.string_alarm_dialog_circle_true:R.string.string_alarm_dialog_circle_false);
    	
		if( config.isActived() ) {
			mAlarmTime0.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			mAlarmTime0.setTextColor(getResources().getColor(R.color.black));
			
			mAlarmCyclic0.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			mAlarmCyclic0.setTextColor(getResources().getColor(R.color.blue_medion));
			
			for( int i = 0; i < 7; i++ ) {
				if( config.getWeekChecked(i) ) {
					mAlarmWeekPicker0[i].setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
					mAlarmWeekPicker0[i].setTextColor(getResources().getColor(R.color.blue_medion));
				}
				else {
					mAlarmWeekPicker0[i].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
					mAlarmWeekPicker0[i].setTextColor(getResources().getColor(R.color.gray));
				}
			}
		}
		else {
			mAlarmTime0.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			mAlarmTime0.setTextColor(getResources().getColor(R.color.gray));
			
			mAlarmCyclic0.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			mAlarmCyclic0.setTextColor(getResources().getColor(R.color.gray));
			 
			for( int i=0; i < 7; i++ ) {
				mAlarmWeekPicker0[i].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
				mAlarmWeekPicker0[i].setTextColor(getResources().getColor(R.color.gray));
			}
		}
		
		
		if( mCentralService != null ) {
			Log.e(TAG, "ENCODE: " + Utils.bytesToHexString(config.getEncode()));
			mCentralService.requestConnectionConfigFuncions(CentralService.DEVICE_CMD_ADDR_ALARM, config.getEncode());
		}
    }
    
    private void upgdateAlarm1(AlarmConfiguration config, boolean save) {
		if( save ) {
			mLocalStorage.saveAlarmConfig(1, config);
		}
		mAlarmActive1.setChecked(config.isActived());
		mAlarmTime1.setText(String.format("%02d:%02d", config.getHour(), config.getMinute()));
		mAlarmCyclic1.setText(config.isCycliced()?R.string.string_alarm_dialog_circle_true:R.string.string_alarm_dialog_circle_false);
		
		if( config.isActived() ) {
			mAlarmTime1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			mAlarmTime1.setTextColor(getResources().getColor(R.color.black));
			
			mAlarmCyclic1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			mAlarmCyclic1.setTextColor(getResources().getColor(R.color.blue_medion));
			
			for( int i = 0; i < 7; i++ ) {
				if( config.getWeekChecked(i) ) {
					mAlarmWeekPicker1[i].setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
					mAlarmWeekPicker1[i].setTextColor(getResources().getColor(R.color.blue_medion));
				}
				else {
					mAlarmWeekPicker1[i].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
					mAlarmWeekPicker1[i].setTextColor(getResources().getColor(R.color.gray));
				}
			}
		}
		else {
			mAlarmTime1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			mAlarmTime1.setTextColor(getResources().getColor(R.color.gray));
			
			mAlarmCyclic1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			mAlarmCyclic1.setTextColor(getResources().getColor(R.color.gray));
			 
			for( int i=0; i < 7; i++ ) {
				mAlarmWeekPicker1[i].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
				mAlarmWeekPicker1[i].setTextColor(getResources().getColor(R.color.gray));
			}
		}
		
		if( mCentralService != null ) {
			Log.e(TAG, "ENCODE: " + Utils.bytesToHexString(config.getEncode()));
			mCentralService.requestConnectionConfigFuncions(CentralService.DEVICE_CMD_ADDR_ALARM|0x0100, config.getEncode());
		}
    }
    
    private void upgdateAlarm2(AlarmConfiguration config, boolean save) {
		if( save ) {
			mLocalStorage.saveAlarmConfig(2, config);
		}
		mAlarmActive2.setChecked(config.isActived());
		mAlarmTime2.setText(String.format("%02d:%02d", config.getHour(), config.getMinute()));
		mAlarmCyclic2.setText(config.isCycliced()?R.string.string_alarm_dialog_circle_true:R.string.string_alarm_dialog_circle_false);
		
		if( config.isActived() ) {
			mAlarmTime2.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			mAlarmTime2.setTextColor(getResources().getColor(R.color.black));
			
			mAlarmCyclic2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			mAlarmCyclic2.setTextColor(getResources().getColor(R.color.blue_medion));
			
			for( int i = 0; i < 7; i++ ) {
				if( config.getWeekChecked(i) ) {
					mAlarmWeekPicker2[i].setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
					mAlarmWeekPicker2[i].setTextColor(getResources().getColor(R.color.blue_medion));
				}
				else {
					mAlarmWeekPicker2[i].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
					mAlarmWeekPicker2[i].setTextColor(getResources().getColor(R.color.gray));
				}
			}
		}
		else {
			mAlarmTime2.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			mAlarmTime2.setTextColor(getResources().getColor(R.color.gray));
			
			mAlarmCyclic2.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			mAlarmCyclic2.setTextColor(getResources().getColor(R.color.gray));
			 
			for( int i=0; i < 7; i++ ) {
				mAlarmWeekPicker2[i].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
				mAlarmWeekPicker2[i].setTextColor(getResources().getColor(R.color.gray));
			}
		}
		
		if( mCentralService != null ) {
			Log.e(TAG, "ENCODE: " + Utils.bytesToHexString(config.getEncode()));
			mCentralService.requestConnectionConfigFuncions(CentralService.DEVICE_CMD_ADDR_ALARM|0x0200, config.getEncode());
		}
    }
    
    private void upgdateAlarm3(AlarmConfiguration config, boolean save) {
		if( save ) {
			mLocalStorage.saveAlarmConfig(3, config);
		}
		mAlarmActive3.setChecked(config.isActived());
		mAlarmTime3.setText(String.format("%02d:%02d", config.getHour(), config.getMinute()));
		mAlarmCyclic3.setText(config.isCycliced()?R.string.string_alarm_dialog_circle_true:R.string.string_alarm_dialog_circle_false);
		
		if( config.isActived() ) {
			mAlarmTime3.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			mAlarmTime3.setTextColor(getResources().getColor(R.color.black));
			
			mAlarmCyclic3.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			mAlarmCyclic3.setTextColor(getResources().getColor(R.color.blue_medion));
			
			for( int i = 0; i < 7; i++ ) {
				if( config.getWeekChecked(i) ) {
					mAlarmWeekPicker3[i].setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
					mAlarmWeekPicker3[i].setTextColor(getResources().getColor(R.color.blue_medion));
				}
				else {
					mAlarmWeekPicker3[i].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
					mAlarmWeekPicker3[i].setTextColor(getResources().getColor(R.color.gray));
				}
			}
		}
		else {
			mAlarmTime3.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			mAlarmTime3.setTextColor(getResources().getColor(R.color.gray));
			
			mAlarmCyclic3.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			mAlarmCyclic3.setTextColor(getResources().getColor(R.color.gray));
			 
			for( int i=0; i < 7; i++ ) {
				mAlarmWeekPicker3[i].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
				mAlarmWeekPicker3[i].setTextColor(getResources().getColor(R.color.gray));
			}
		}
		
		if( mCentralService != null ) {
			Log.e(TAG, "ENCODE: " + Utils.bytesToHexString(config.getEncode()));
			mCentralService.requestConnectionConfigFuncions(CentralService.DEVICE_CMD_ADDR_ALARM|0x0300, config.getEncode());
		}
    }
   
    private void upgdateAlarm4(AlarmConfiguration config, boolean save) {
		if( save ) {
			mLocalStorage.saveAlarmConfig(4, config);
		}
		
		mAlarmActive4.setChecked(config.isActived());
		mAlarmTime4.setText(String.format("%02d:%02d", config.getHour(), config.getMinute()));
		mAlarmCyclic4.setText(config.isCycliced()?R.string.string_alarm_dialog_circle_true:R.string.string_alarm_dialog_circle_false);
		
		if( config.isActived() ) {
			mAlarmTime4.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			mAlarmTime4.setTextColor(getResources().getColor(R.color.black));
			
			mAlarmCyclic4.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			mAlarmCyclic4.setTextColor(getResources().getColor(R.color.blue_medion));
			
			for( int i = 0; i < 7; i++ ) {
				if( config.getWeekChecked(i) ) {
					mAlarmWeekPicker4[i].setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
					mAlarmWeekPicker4[i].setTextColor(getResources().getColor(R.color.blue_medion));
				}
				else {
					mAlarmWeekPicker4[i].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
					mAlarmWeekPicker4[i].setTextColor(getResources().getColor(R.color.gray));
				}
			}
		}
		else {
			mAlarmTime4.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			mAlarmTime4.setTextColor(getResources().getColor(R.color.gray));
			
			mAlarmCyclic4.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			mAlarmCyclic4.setTextColor(getResources().getColor(R.color.gray));
			 
			for( int i=0; i < 7; i++ ) {
				mAlarmWeekPicker4[i].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
				mAlarmWeekPicker4[i].setTextColor(getResources().getColor(R.color.gray));
			}
		}
		
		if( mCentralService != null ) {
			Log.e(TAG, "ENCODE: " + Utils.bytesToHexString(config.getEncode()));
			mCentralService.requestConnectionConfigFuncions(CentralService.DEVICE_CMD_ADDR_ALARM|0x0400, config.getEncode());
		}
    }
    
	private void initAlarm4( final AlarmConfiguration configuration ) {
	    ((RelativeLayout)findViewById(R.id.id_rl_setting_alarm4)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAlarm4Setting = new AlarmSettingDialog(SettingAlarmActivity.this, configuration);
		        Window dialogWindow = mAlarm4Setting.getWindow();
		        dialogWindow.setGravity(Gravity.CENTER);
		        mAlarm4Setting.setCanceledOnTouchOutside(false);
				mAlarm4Setting.setOnConfirmListener(new AlarmSettingDialog.OnConfirmListener() {
					@Override
					public void OnConfirm(AlarmConfiguration config) {
						config.setActived(true);
						upgdateAlarm4(config, true);
					}
				});
		        mAlarm4Setting.show();
			}
	    });
	}

	private void initAlarm3( final AlarmConfiguration configuration ) {
	    ((RelativeLayout)findViewById(R.id.id_rl_setting_alarm3)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAlarm3Setting = new AlarmSettingDialog(SettingAlarmActivity.this, configuration);
		        Window dialogWindow = mAlarm3Setting.getWindow();
		        dialogWindow.setGravity(Gravity.CENTER);
		        mAlarm3Setting.setCanceledOnTouchOutside(false);
				mAlarm3Setting.setOnConfirmListener(new AlarmSettingDialog.OnConfirmListener() {
					@Override
					public void OnConfirm(AlarmConfiguration config) {
						config.setActived(true);
						upgdateAlarm3(config, true);
					}
				});
		        mAlarm3Setting.show();
			}
	    });
	}

	private void initAlarm2( final AlarmConfiguration configuration ) {
	    ((RelativeLayout)findViewById(R.id.id_rl_setting_alarm2)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAlarm2Setting = new AlarmSettingDialog(SettingAlarmActivity.this, configuration);
		        Window dialogWindow = mAlarm2Setting.getWindow();
		        dialogWindow.setGravity(Gravity.CENTER);
		        mAlarm2Setting.setCanceledOnTouchOutside(false);
				mAlarm2Setting.setOnConfirmListener(new AlarmSettingDialog.OnConfirmListener() {
					@Override
					public void OnConfirm(AlarmConfiguration config) {
						config.setActived(true);
						upgdateAlarm2(config, true);
					}
				});
		        mAlarm2Setting.show();
			}
	    });
	}

	private void initAlarm1( final AlarmConfiguration configuration ) {
		((RelativeLayout)findViewById(R.id.id_rl_setting_alarm1)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAlarm1Setting = new AlarmSettingDialog(SettingAlarmActivity.this, configuration);
		        Window dialogWindow = mAlarm1Setting.getWindow();
		        dialogWindow.setGravity(Gravity.CENTER);
		        mAlarm1Setting.setCanceledOnTouchOutside(false);
				mAlarm1Setting.setOnConfirmListener(new AlarmSettingDialog.OnConfirmListener() {
					@Override
					public void OnConfirm(AlarmConfiguration config) {
						config.setActived(true);
						upgdateAlarm1(config, true);
					}
				});
		        mAlarm1Setting.show();
			}
	    });
	}

	private void initAlarm0( final AlarmConfiguration configuration ) {
		((RelativeLayout)findViewById(R.id.id_rl_setting_alarm0)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAlarm0Setting = new AlarmSettingDialog(SettingAlarmActivity.this, configuration);
		        Window dialogWindow = mAlarm0Setting.getWindow();
		        dialogWindow.setGravity(Gravity.CENTER);
				mAlarm0Setting.setCanceledOnTouchOutside(false);
				mAlarm0Setting.setOnConfirmListener(new AlarmSettingDialog.OnConfirmListener() {
					@Override
					public void OnConfirm(AlarmConfiguration config) {
						config.setActived(true);
						upgdateAlarm0(config, true);
					}
				});
				mAlarm0Setting.show();
			}
	    });
	}
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	unbindCentralService();
    }
    
    private void bindCentralService() {
		Intent it = new Intent(SettingAlarmActivity.this, CentralService.class);
		bindService(it, mServiceConnection, BIND_AUTO_CREATE);
	}
    
    private void unbindCentralService() {
		unbindService(mServiceConnection);
	}
    
	// Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
        	Log.i(TAG, "CentralServiceConnection&SettingAlarmActivity --- onServiceConnected");
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
