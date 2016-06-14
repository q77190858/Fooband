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
import com.foogeez.configuration.Configuration.SedentaryConfiguration;
import com.foogeez.dialog.SedentarySettingDialog;
import com.foogeez.fooband.R;
import com.foogeez.services.CentralService;
import com.grdn.widgets.SwitchButton;
import com.umeng.analytics.MobclickAgent;

/**
 *  久坐提醒---> 设置 
 */
public class SettingSedentaryActivity extends SettingActivity {
    private final static String TAG = SettingSedentaryActivity.class.getSimpleName();
    
    private CentralService mCentralService = null;

    private SedentarySettingDialog mSedentarySetting;
    private LocalStorage mLocalStorage = new LocalStorage(SettingSedentaryActivity.this);
    
    private SedentaryConfiguration mSedentaryConfig;
    
    private TextView mTextViewSedentaryTime;
    private TextView mTextViewSedentaryDescriptor;
    private SwitchButton mSedentaryActived;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "SettingSedentaryActivity --- onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_sedentary);
		initSettingTitle(R.string.string_function_sedentary_reminder);
		
		if( !mLocalStorage.hasAnyAccount() ) {
    		Toast.makeText(
    				SettingSedentaryActivity.this,
                    getResources().getString(R.string.string_please_register_first),
                    Toast.LENGTH_SHORT).show();
			
			super.UIfinish();
			return;
		}
		
		bindCentralService();
		
		mSedentaryConfig = mLocalStorage.restoreSedentaryConfig();
		
		mTextViewSedentaryTime = (TextView)findViewById(R.id.id_tv_setting_sedentary_remainder);
		mTextViewSedentaryTime.setText( String.format(getResources().getString(R.string.string_function_sedentary_invert_time), 
										mSedentaryConfig.getHour(), mSedentaryConfig.getMinute()));
		
		mTextViewSedentaryDescriptor =  (TextView)findViewById(R.id.id_tv_sedentary_active_descriptor);

		mSedentaryActived = (SwitchButton)findViewById(R.id.id_sb_sedentary_reminder_active);
		mSedentaryActived.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mSedentaryConfig.setActived(isChecked);
				updateSedentary(mSedentaryConfig, true);
			}
		});
		
		initSedentary();
		updateSedentary(mSedentaryConfig, false);
    }
    
	private void initSedentary() {
	    ((RelativeLayout)findViewById(R.id.id_rl_setting_sedentary_remainder)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSedentarySetting = new SedentarySettingDialog(SettingSedentaryActivity.this, mLocalStorage.restoreSedentaryConfig());
		        Window dialogWindow = mSedentarySetting.getWindow();
		        dialogWindow.setGravity(Gravity.CENTER);
		        mSedentarySetting.setCanceledOnTouchOutside(false);
		        mSedentarySetting.setOnConfirmListener(new SedentarySettingDialog.OnConfirmListener() {
					@Override
					public void OnConfirm(SedentaryConfiguration config) {
						config.setActived(true);
						updateSedentary(config, true);
					}
				});
		        mSedentarySetting.show();
			}
	    });
	}

	private void updateSedentary(SedentaryConfiguration config, boolean save) {
		if( save ) {
			mLocalStorage.saveSedentaryConfig(config);
		}
		mSedentaryActived.setChecked(config.isActived());

		mTextViewSedentaryTime.setText( String.format(getResources().getString(R.string.string_function_sedentary_invert_time), 
										config.getHour(), config.getMinute()));
		
		if( config.isActived() ) {
			mTextViewSedentaryTime.setTextColor(getResources().getColor(R.color.black));
			mTextViewSedentaryDescriptor.setTextColor(getResources().getColor(R.color.black));
		}
		else {
			mTextViewSedentaryTime.setTextColor(getResources().getColor(R.color.gray));
			mTextViewSedentaryDescriptor.setTextColor(getResources().getColor(R.color.gray));
		}
		
		if( mCentralService != null ) {
			mCentralService.requestConnectionConfigFuncions(CentralService.DEVICE_CMD_ADDR_SEDENTARY, config.isActived()?config.getEncode():0);
		}
		
	}
	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	unbindCentralService();
    }
	
    private void bindCentralService() {
		Intent it = new Intent(SettingSedentaryActivity.this, CentralService.class);
		bindService(it, mServiceConnection, BIND_AUTO_CREATE);
	}
    
    private void unbindCentralService() {
		unbindService(mServiceConnection);
	}
    
	// Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
        	Log.i(TAG, "CentralServiceConnection&SettingSedentaryActivity --- onServiceConnected");
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
