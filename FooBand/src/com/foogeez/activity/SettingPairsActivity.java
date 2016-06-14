package com.foogeez.activity;

import java.util.Map;

import com.foogeez.configuration.LocalStorage;
import com.foogeez.fooband.R;
import com.foogeez.services.CentralService;
import com.grdn.util.DrawableUtil;
import com.grdn.util.Utils;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * 设置绑定蓝牙设备的信息 
 */
public class SettingPairsActivity extends SettingActivity {
    private final static String TAG = SettingPairsActivity.class.getSimpleName();
	
	private RelativeLayout lyPairsContent = null;
	
    private GestureDetector mGestureDetector = null;
    
	private float width;
	private float height;
	
	private LocalStorage mLocalStorage;
	private CentralService mCentralService = null;
	
	private ImageView mImageViewPair = null;
	private Button btnPairNewDevice = null;
	private ProgressBar pgbScanLeDevices = null;
	
	private String name = null;
	private String addr = null;
	private String hwvr = null;
	private String fwvr = null;
	private String srln = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_pairs);
		
        DisplayMetrics dm = new DisplayMetrics();  
        getWindowManager().getDefaultDisplay().getMetrics(dm);  
        width = dm.widthPixels;//*dm.density;  
        height= dm.heightPixels;//*dm.density;
		
        /** 绑定新设备  */
		initSettingTitle(R.string.string_tv_pairs_title);
		
		mLocalStorage = new LocalStorage(SettingPairsActivity.this);
		bindCentralService();/**Robin  20151215  初始化服务要放在使用服务之前*/
		
		mImageViewPair = (ImageView) findViewById(R.id.id_iv_pairs_content);
		DrawableUtil.setImageViewColor(mImageViewPair, getResources().getColor(R.color.orangered));
		
		btnPairNewDevice = (Button)findViewById(R.id.id_btn_pairs_new_device);
		btnPairNewDevice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.e(TAG, "start scan le device...");
				pgbScanLeDevices.setVisibility(ProgressBar.VISIBLE);
		        new Thread(new Runnable() {
		            @Override
		            public void run() {
		            	mCentralService.scanLeDevices(true, 5000);
		            }
		        }).start();

			}
		});
		
		pgbScanLeDevices = (ProgressBar)findViewById(R.id.id_pgb_scan_device);
		pgbScanLeDevices.setVisibility(ProgressBar.GONE);
		
		mGestureDetector = new GestureDetector(SettingPairsActivity.this, new LearnGestureListener());
		lyPairsContent = (RelativeLayout)findViewById(R.id.id_ly_pairs_content);
		lyPairsContent.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch( event.getAction() ) {
					case MotionEvent.ACTION_UP:
						break;
				}
				return mGestureDetector.onTouchEvent(event);
			}
		});
		
		registerReceiver(mBroadcastReceiver, makeGattUpdateIntentFilter());
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
    	unbindCentralService();
    	unregisterReceiver(mBroadcastReceiver);
		
	}
	
	class LearnGestureListener extends GestureDetector.SimpleOnGestureListener{
		@Override
		public boolean onSingleTapUp(MotionEvent ev) {
			Log.d(TAG, "onSingleTapUp"+ev.toString());
			return true;
		}
		@Override
		public void onShowPress(MotionEvent ev) {
		    Log.d(TAG,"onShowPress"+ev.toString());
		}
		@Override
		public void onLongPress(MotionEvent ev) {
			Log.d(TAG,"onLongPress"+ev.toString());
		}
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			//Log.d(TAG,"onScroll"+e1.toString());
			return true;
		}
		@Override
		public boolean onDown(MotionEvent ev) {
			Log.d(TAG,"onDownd"+ev.toString());
			return true;
		}
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			//Log.d(TAG,"onFling - e1"+e1.toString());
			//Log.d(TAG,"onFling - e2"+e2.toString());
			
			float EndX = e2.getAxisValue(MotionEvent.AXIS_X);
			float EndY = e2.getAxisValue(MotionEvent.AXIS_Y);
			float StartX = e1.getAxisValue(MotionEvent.AXIS_X);
			float StartY = e1.getAxisValue(MotionEvent.AXIS_Y);

			Log.e(TAG,"StartX["+StartX+"]" + "EndX["+EndX+"] " + width);
			Log.e(TAG,"StartY["+StartY+"]" + "EndY["+EndY+"] " + height);

			if( (EndX - StartX) > (width/5) ) {
				Log.e(TAG, "LEFT FLING");
				UIfinish();
			}
			
			return true;
		}
	}    
	
    private void bindCentralService() {
		Intent it = new Intent(SettingPairsActivity.this, CentralService.class);
		bindService(it, mServiceConnection, BIND_AUTO_CREATE);
	}
    
    private void unbindCentralService() {
		unbindService(mServiceConnection);
	}
    
 // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
        	Log.i(TAG, "CentralServiceConnection --- onServiceConnected");
        	mCentralService = ((CentralService.LocalBinder)service).getService();
        }
        
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        	Log.i(TAG, "CentralServiceConnection --- onServiceDisconnected");
        	mCentralService = null;
        }
    };
	
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CentralService.ACTION_BLUETOOTH_SCAN_START);
        intentFilter.addAction(CentralService.ACTION_BLUETOOTH_SCAN_COMPLETE);
        return intentFilter;
    }
    
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@SuppressLint("DefaultLocale")
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if( action.equals(CentralService.ACTION_BLUETOOTH_SCAN_START) ) {
				Log.i(TAG, action);
				return;
			}
			if( action.equals(CentralService.ACTION_BLUETOOTH_SCAN_COMPLETE) ) {
				Log.i(TAG, action);

				/**
				for( Map<String,Object> map: mCentralService.getLeDevicesInfo() ) {
					Log.i( TAG, "LE DEVICE IN RANGE = " + map.get(CentralService.DEVICE_INFO_NAME) );
				}
				/**/
				
				Map<String,Object> map = mCentralService.getMinRssiLeDevice();
				Log.i( TAG, "MIN RSSI DEVICE: name = " + map.get(CentralService.DEVICE_INFO_NAME) + 
											" addr = " + map.get(CentralService.DEVICE_INFO_ADDR) + 
											" rssi = " + map.get(CentralService.DEVICE_INFO_RSSI) + 
											" hwvr = " + map.get(CentralService.DEVICE_INFO_HWVR) +
											" fwvr = " + map.get(CentralService.DEVICE_INFO_FWVR) +
											" srln = " + map.get(CentralService.DEVICE_INFO_SRLN) );
				
				pgbScanLeDevices.setVisibility(ProgressBar.GONE);
				
				if( map.get(CentralService.DEVICE_INFO_ADDR) == null ) {
					new AlertDialog.Builder(SettingPairsActivity.this)   
					.setTitle(R.string.string_dialog_pairs_title_no_device)  
					.setPositiveButton(R.string.string_dialog_positive, null)
					.show();  
				}
				else {
					name = (String) map.get(CentralService.DEVICE_INFO_NAME);
					addr = (String) map.get(CentralService.DEVICE_INFO_ADDR);
					hwvr = (String) map.get(CentralService.DEVICE_INFO_HWVR);
					fwvr = (String) map.get(CentralService.DEVICE_INFO_FWVR);
					srln = (String) map.get(CentralService.DEVICE_INFO_SRLN);
					
					hwvr = Utils.addVersionDot(hwvr).toUpperCase();
					fwvr = Utils.addVersionDot(fwvr).toUpperCase();
					srln = "FGB" + srln.toUpperCase();
					
					new AlertDialog.Builder(SettingPairsActivity.this)   
					.setTitle(R.string.string_dialog_pairs_title)  
					.setMessage(
						getResources().getString(R.string.string_dialog_pairs_message_header) + "\r\n" +
						getResources().getString(R.string.string_dialog_pairs_message_device_name) + name + "\r\n" +
						getResources().getString(R.string.string_dialog_pairs_message_device_hw_version) + hwvr + "\r\n" + 	
						getResources().getString(R.string.string_dialog_pairs_message_device_fw_version) + fwvr + "\r\n" + 
						getResources().getString(R.string.string_dialog_pairs_message_device_serial_number) + srln + "\r\n" +
						getResources().getString(R.string.string_dialog_pairs_message_tailer) )
					.setPositiveButton(R.string.string_dialog_positive, mConfirmYesNo)  
					.setNegativeButton(R.string.string_dialog_negative, mConfirmYesNo)  
					.show();  
				}
				return;
			}
		}
    };
    
    DialogInterface.OnClickListener mConfirmYesNo = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			
			switch( which ) {
				case DialogInterface.BUTTON_POSITIVE:
                    mLocalStorage.saveActivatedDeviceName(name);
                    mLocalStorage.saveActivatedDeviceAddr(addr);
                    mLocalStorage.saveActivatedDeviceHwvr(hwvr);
                    mLocalStorage.saveActivatedDeviceFwvr(fwvr);
                    mLocalStorage.saveActivatedDeviceSrln(srln);
                    mCentralService.updateUserAndDeviceInfo();
                    mCentralService.requestAuth();
                    /** Name:Robin  Time:20151214  Function: 绑定成功后进行蓝牙连接 */
                    mCentralService.requestConnectionCheckOnly();
                    Log.i(TAG, "Robin ---- 绑定成功后进行蓝牙连接 ");
                    UIfinish();
					break;
					
				case DialogInterface.BUTTON_NEGATIVE:
					//mCentralService.disconnectLeDevice();
					break;
			}
		}
    };
    
	/** Name:Robin  Time:20150928  Function: 友盟session的统计  */
    @Override
    public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		
		Log.i(TAG, "Robin---------onResume()");
	}
    @Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		
		Log.i(TAG, "Robin---------onPause()");
	}

    
}
