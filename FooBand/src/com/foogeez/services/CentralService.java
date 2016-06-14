package com.foogeez.services;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.internal.telephony.ITelephony;
import com.foogeez.activity.ManagerActivity;
import com.foogeez.bluetooth.BluetoothLeGattAttributes;
import com.foogeez.bluetooth.BluetoothLeService;
import com.foogeez.bluetooth.BluetoothLeService.Token;
import com.foogeez.configuration.LocalStorage;
import com.foogeez.configuration.Configuration.SedentaryConfiguration;
import com.foogeez.database.ActionsDatum;
import com.foogeez.database.DatabaseHelper;
import com.foogeez.database.DatabaseManager;
import com.foogeez.fooband.R;
import com.foogeez.notification.NotificationsService;
import com.foogeez.notification.model.MessageBean;
import com.foogeez.notification.model.Messenger;
import com.grdn.util.FontPicker;
import com.grdn.util.Utils;

public class CentralService extends Service {  
    private static final String TAG = CentralService.class.getSimpleName();
    
    public static final String ACTION_BLUETOOTH_IS_DISABLE = 
    		"com.foogeez.services.CentralService.ACTION_BLUETOOTH_IS_DISABLE";
    
    public static final String ACTION_BLUETOOTH_SCAN_START = 
    		"com.foogeez.services.CentralService.ACTION_BLUETOOTH_SCAN_START";
    public static final String ACTION_BLUETOOTH_SCAN_STOP = 
    		"com.foogeez.services.CentralService.ACTION_BLUETOOTH_SCAN_STOP";
    public static final String ACTION_BLUETOOTH_SCAN_COMPLETE = 
    		"com.foogeez.services.CentralService.ACTION_BLUETOOTH_SCAN_COMPLETE"; 

    public static final String ACTION_BLUETOOTH_LE_STATE_CHANGING = 
    		"com.foogeez.services.CentralService.ACTION_BLUETOOTH_LE_STATE_CHANGING"; 

    public static final String ACTION_BLUETOOTH_LE_GATT_DISCOVERED = 
    		"com.foogeez.services.CentralService.ACTION_BLUETOOTH_LE_GATT_DISCOVERED"; 
    
    public static final String ACTION_ACTIONS_SPORT_DATUM_CHANGED = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_SPORT_DATUM_CHANGED";
    public static final String ACTION_ACTIONS_SLEEP_DATUM_CHANGED = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_SLEEP_DATUM_CHANGED"; 
    
    public static final String ACTION_ACTIONS_DATUM_SYNC_PROGRESS_CHANGED = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_DATUM_SYNC_PROGRESS_CHANGED"; 
    
    public static final String ACTION_ACTIONS_ENTER_DFU_SUCCESS = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_ENTER_DFU_SUCCESS"; 
    
    //public static final String ACTION_ACTIONS_ENTER_DFU_IN_MAIN = 
    //		"com.foogeez.services.CentralService.ACTION_ACTIONS_ENTER_DFU_IN_MAIN"; 
    
    //public static final String ACTION_ACTIONS_ENTER_DFU_IN_MAIN_SETTING = 
    //		"com.foogeez.services.CentralService.ACTION_ACTIONS_ENTER_DFU_IN_MAIN_SETTING";
    
    public static final String ACTION_ACTIONS_ENTER_DFU_IN_MANAGE_ACTIVITY = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_ENTER_DFU_IN_MANAGE_ACTIVITY";
    
    //public static final String ACTION_ACTIONS_CONFIRM_DFU_IN_MAIN = 
    //		"com.foogeez.services.CentralService.ACTION_ACTIONS_CONFIRM_DFU_IN_MAIN"; 
    
    //public static final String ACTION_ACTIONS_CONFIRM_DFU_IN_MAIN_SETTING = 
    //		"com.foogeez.services.CentralService.ACTION_ACTIONS_CONFIRM_DFU_IN_MAIN_SETTING";
    
    public static final String ACTION_ACTIONS_CONFIRM_DFU_IN_MANAGE_ACTIVITY = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_CONFIRM_DFU_IN_MANAGE_ACTIVITY"; 
    
    public static final String ACTION_ACTIONS_DATUM_REFRESH_START = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_DATUM_REFRESH_START";
    public static final String ACTION_ACTIONS_DATUM_REFRESH_DOING = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_DATUM_REFRESH_DOING";
    public static final String ACTION_ACTIONS_DATUM_REFRESH_COMPLETE = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_DATUM_REFRESH_COMPLETE";
    
    public static final String ACTION_ACTIONS_ADAPTER_SCAN_ERROR = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_ADAPTER_SCAN_ERROR";
    
    public static final String ACTION_ACTIONS_SERVICE_GATT_ERROR = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_SERVICE_GATT_ERROR";
    
    public static final String ACTION_ACTIONS_SERVICE_ENTER_DFU = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_SERVICE_ENTER_DFU";
    
    public static final String ACTION_ACTIONS_SERVICE_GATT_READ_ERROR = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_SERVICE_GATT_READ_ERROR";
    
    public static final String ACTION_ACTIONS_SERVICE_GATT_BUSY = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_SERVICE_GATT_BUSY";
    
    public static final String ACTION_ACTIONS_FUNCTION_CONFIG_SUCCESS = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_FUNCTION_CONFIG_SUCCESS";
    public static final String ACTION_ACTIONS_FUNCTION_CONFIG_FAILURE = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_FUNCTION_CONFIG_FAILURE";
    
    public static final String ACTION_ACTIONS_ACTIVITY_IN_STACK_ALL_CLEAR = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_ACTIVITY_IN_STACK_ALL_CLEAR";
    
    public static final String ACTION_ACTIONS_NO_ACTIVATED_DEVICE_WARNNING = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_NO_ACTIVATED_DEVICE_WARNNING";
    
    public static final String ACTION_ACTIONS_UPDATE_BATTERY_LEVEL = 
    		"com.foogeez.services.CentralService.ACTION_ACTIONS_UPDATE_BATTERY_LEVEL";
    
    public static final String ACTION_ACTIONS_NOTIFICATION_POSTED = 
            "com.foogeez.services.CentralService.ACTION_ACTIONS_NOTIFICATION_POSTED";
    public static final String ACTION_ACTIONS_ACTIVE_NOTIFICATIONS =
            "com.foogeez.services.CentralService.ACTION_ACTIONS_ACTIVE_NOTIFICATIONS";
    public static final String ACTION_ACTIONS_NOTIFICATION_REMOVED =
            "com.foogeez.services.CentralService.ACTION_ACTIONS_NOTIFICATION_REMOVED";
    
    public static final String DEVICE_INFO_TYPE = "device.info.type";
    public static final String DEVICE_INFO_NAME = "device.info.name";
    public static final String DEVICE_INFO_ADDR = "device.info.addr";
    public static final String DEVICE_INFO_RSSI = "device.info.rssi";
    public static final String DEVICE_INFO_HWVR = "device.info.hwversion";
    public static final String DEVICE_INFO_FWVR = "device.info.fwversion";
    public static final String DEVICE_INFO_SRLN = "device.info.serialnumber";
    
    public static final int CENTRAL_CMD_DFU = 1;
    
    public static final String ROM_IMAGE_INF = "http://www.foogeez.com/rom_test/info.txt";
    public static final String ROM_IMAGE = "http://www.foogeez.com/rom_test/fgb1882.hex";
    
//    public static final String ROM_IMAGE_INF = "http://www.foogeez.com/rom/info.txt";
//    public static final String ROM_IMAGE = "http://www.foogeez.com/rom/fgb1882.hex";
    
    public static final String APP_IMAGE_INF = "http://www.foogeez.com/app/app_info.txt";
    public static final String APP_IMAGE = "http://www.foogeez.com/app/FooBand.apk";            //------------->> 测试用
   
    public static final String DEVICE_NAME_FILTER0 = "FOOGEEZ ";
    public static final String DEVICE_NAME_FILTER1 = "FGZ ";
    
    private static final byte RECORD_TYPE_NAME = 0x09;
    private static final byte RECORD_TYPE_SUPPORTED_128SERVICE = 0x07;
    private static final int RECORD_TYPE_SUPPORTED_128SERVICE_LEN = 18;
    
    /** The profile is in disconnected state */
    //public static final int STATE_DISCONNECTED  = 0;
    /** The profile is in connecting state */
    //public static final int STATE_CONNECTING    = 1;
    /** The profile is in connected state */
    //public static final int STATE_CONNECTED     = 2;
    /** The profile is in disconnecting state */
    //public static final int STATE_DISCONNECTING = 3;
    
    public static final int DEVICE_CMD_ADDR_TIME_ZONE	= 0x0001;
    public static final int DEVICE_CMD_ADDR_SAVING_TIME = 0x0002;
    public static final int DEVICE_CMD_ADDR_UTC			= 0x0003;
    public static final int DEVICE_CMD_ADDR_OTA_ENABLE  = 0x001b;
    
    public static final int DEVICE_CMD_ADDR_TIME_FORMAT24		= 0x0004;
    public static final int DEVICE_CMD_ADDR_DISPLAY_DISTANCE 	= 0x0005;
    public static final int DEVICE_CMD_ADDR_DISPLAY_CALORIC 	= 0x0105;
    public static final int DEVICE_CMD_ADDR_DISPLAY_SLEEPTIME 	= 0x0205;
    public static final int DEVICE_CMD_ADDR_DISPLAY_POWER		= 0x0305;
    public static final int DEVICE_CMD_ADDR_DISPLAY_DATEWEEK	= 0x0024;
    public static final int DEVICE_CMD_ADDR_SEDENTARY			= 0x0006;
    public static final int DEVICE_CMD_ADDR_ALARM				= 0x0007;
    public static final int DEVICE_CMD_ADDR_CALENDAR			= 0x0008;
    public static final int DEVICE_CMD_ADDR_USR_INFO			= 0x0016;
    public static final int DEVICE_CMD_ADDR_STOP_WATCH_ENABLE	= 0x0020;
    public static final int DEVICE_CMD_ADDR_DISTURB_MODE_ENABLE	= 0x0021;
    
    public static final int DEVICE_CMD_ADDR_CALLING_MISS	= 0x0d;
    public static final int DEVICE_CMD_ADDR_MESSAGE_MISS	= 0x0e;
    
    public static final int DEVICE_CMD_ADDR_CALLING 	= 0x0c;
    public static final int DEVICE_CMD_ADDR_FONT_ARRAY  = 0x1a;
    public static final byte DEVICE_CMD_ADDR_FONT_TYPE_0= 0x00;
    public static final byte DEVICE_CMD_ADDR_FONT_TYPE_1= 0x01;    

    public static final int DEVICE_CMD_ADDR_DATUM_TOTAL = 0x0A;  
    public static final int DEVICE_CMD_ADDR_DATUM_INDEX = 0x0B;
    
    private int mConfigAddr = 0;
    private byte[] mConfigData;
    
    private byte[] mWriteBackBuf = new byte[20];
    private ActionsDatum mLastSportDatum = null;
    
    private String mRomFileFullName = null;
    private String mRomFileVersion = null;
    
    private MyPhoneStatReceiver mPhoneStatReceiver = null;
    
    private LocalStorage mLocalStorage;
    
    //private int mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
    //private int mRequestedLeState = BluetoothProfile.STATE_DISCONNECTED;
    
    private static final int CONNECT_LE_REQUEST_CODE_UNKNOWN = 0;
    private static final int CONNECT_LE_REQUEST_CODE_SYN_DATUM = 1;
    private static final int CONNECT_LE_REQUEST_CODE_REFRESH_DATUM = 2;
    private static final int CONNECT_LE_REQUEST_CODE_CALLING_DISPLAY = 3;
    private static final int CONNECT_LE_REQUEST_CODE_CALLING_HUNGED = 4;
    private static final int CONNECT_LE_REQUEST_CODE_UPGRADE_ROM = 5;
    private static final int CONNECT_LE_REQUEST_CODE_DATUM_RESTORE = 6;
    private static final int CONNECT_LE_REQUEST_CODE_NONE = 7;
    private static final int CONNECT_LE_REQUEST_CODE_CONFIG = 8;
    private static final int CONNECT_LE_REQUEST_CODE_MISSED_CALLING = 9;
    private static final int CONNECT_LE_REQUEST_CODE_MISSED_MESSAGE = 10;
    private static final int CONNECT_LE_REQUEST_CODE_AUTH = 11;
    private static final int CONNECT_LE_REQUEST_CODE_REFRESH_DATUM_AUTO = 12;
    private static final int CONNECT_LE_REQUEST_CODE_SYN_DATUM_AUTO = 13;
    private static final int CONNECT_LE_REQUEST_CODE_BOND = 14;
    
    private static int mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
    
    public static BluetoothAdapter mBluetoothAdapter = null;
    private static BluetoothLeService mBluetoothLeService = null;

    private static Context context;

    private boolean mScanning = false;
    
    private boolean mFirstRunning = true;
    
    private static Map<String,Object> mMinRssiLeDevice = new HashMap<String,Object>();
    //private static List<Map<String,Object>> mBluetoothLeDevicesInfo = new ArrayList<Map<String,Object>>();
    
    private String mUserAccount = null;
    private String mUserBindDeviceName = null;
    private static String mUserBindDeviceAddr = null;
    private static String mUserBindDeviceSrln = null;
    
    private DatabaseManager mDBmanager = null;
    private int mRecentUtc = 0;
    
    private Handler mHandler = new Handler();
    private final IBinder mIBinder = new LocalBinder();
    
    private SmsObserver smsObserver;
    private PhnObserver phnObserver;
    
    private ActionsDatum mActionsDatum = null;
    private boolean sportFirstF = true;
    private boolean sleepFirstF = true;
	private int mDatumLen = 0;
	private int mDatumMaxLen = 0;
	
    private int mBatteryLevel = 0;
    public int getBatteryLevel() {
    	return mBatteryLevel;
    }
	

    public class LocalBinder extends Binder {
        public CentralService getService() {
            return CentralService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }
    
    @Override  
    public void onCreate() {  
        super.onCreate();  
        Log.i(TAG, "***** CentralService *****: onCreate");  
       
        initNotification();
        //initBluetoothLeAdapter();
        
        initBluetoothLeService();
        //bindBluetoothLeService();
        
        //initNotificationMonitorService();
        //bindNotificationMonitorService();
        
        //Robin test 不在此运行
//        initAlarmSyncTask(); 
        
        updateUserAndDeviceInfo();
        
        initTelephonyService();
        
		final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(mDfuUpdateReceiver, makeDfuUpdateIntentFilter());
        
//        registerReceiver(mActiveNotificationReceiver, makeActiveNotifIntentFilter());
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//            NotificationsService.ENABLED = true;
//        }
    }
    
    private AlarmManager mAlarmManager = null;
    private BroadcastReceiver mAlarmReceiver = null;
    public static final String ALARM_SYNC_TASK = "com.foogeez.services.CentralService.ALARM_SYNC_TASK";
    public static final String ALARM_SYNC_TASK_DO = "com.foogeez.services.CentralService.ALARM_SYNC_TASK_DO";

    private void initAlarmSyncTask() {
    	Log.i(TAG, "Robin----- 自动同步时间设置----initAlarmSyncTask");
	    Intent intent = new Intent(ALARM_SYNC_TASK);
		PendingIntent pi = PendingIntent.getBroadcast(CentralService.this, 0, intent, 0);	
				
		/** Robin  --20151112 */
		long firstTime = SystemClock.elapsedRealtime();	// 开机之后到现在的运行时间(包括睡眠时间)
        long systemTime = System.currentTimeMillis();
        
		Calendar calendar = Calendar.getInstance();						//获取日期对象    
		calendar.setTimeInMillis(System.currentTimeMillis());			//设置Calendar对象
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 这里时区需要设置一下，不然会有8个小时的时间差
		calendar.set(Calendar.HOUR_OF_DAY,	18);						//设置闹钟小时数
		calendar.set(Calendar.MINUTE, 		1);							//设置闹钟的分钟数
		calendar.set(Calendar.SECOND,		0);							//设置闹钟的秒数
		calendar.set(Calendar.MILLISECOND,	0);							//设置闹钟的毫秒数
       
		// 选择的每天定时时间
	 	long selectTime = calendar.getTimeInMillis();	

	 	// 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
	 	if(systemTime > selectTime) {
//		 		Toast.makeText(MainActivity.this, "设置的时间小于当前时间", Toast.LENGTH_SHORT).show();
	 		calendar.add(Calendar.DAY_OF_MONTH, 1);
	 		selectTime = calendar.getTimeInMillis();
	 	}

	 	// 计算现在时间到设定时间的时间差
	 	long time = selectTime - systemTime;
 		firstTime += time;
		
 		mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstTime, 12*60*60*1000, pi);
	
        Log.i(TAG, "time ==== " + time + ", selectTime ===== "
    			+ selectTime + ", systemTime ==== " + systemTime + ", firstTime === " + firstTime);
	        
        mAlarmReceiver = new AlarmReceiver();
        registerReceiver(mAlarmReceiver, new IntentFilter(ALARM_SYNC_TASK));
	}
	
	@SuppressLint("SimpleDateFormat")
	public class AlarmReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Robin------接收到---AlarmReceiver--+ intent.getAction()" );
	    	if( intent.getAction().equals(CentralService.ALARM_SYNC_TASK) ) { 
		    	Log.d(TAG, "自动同步数据！" + new SimpleDateFormat("yyyy-MM-dd E HH:mm").format(new Date(System.currentTimeMillis())));
		    	refreshDatumAuto();
	    	}
		}
	}

	@Override  
    public void onStart(Intent intent, int startId) {  
        Log.i(TAG, "***** CentralService *****: onStart");  
        // 这里可以做Service该做的事  
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.i(TAG, "***** CentralService *****: onStartCommand");
    	
    	flags = START_STICKY;
    	
    	LoopThread lt = new LoopThread();
    	lt.start();
    	
    	return super.onStartCommand(intent, flags, startId);
    }
    
    @Override
    public void onDestroy() {
    	Log.i(TAG, "***** CentralService *****: onDestroy");
    	
    	unbindBluetoothLeService();
    	//unbindNotificationMonitorService();
    	
    	if( mPhoneStatReceiver != null ) {
    		unregisterReceiver(mPhoneStatReceiver);
    	}
    	
    	if( mAlarmReceiver != null ) {
    		unregisterReceiver(mPhoneStatReceiver);
    	}
    	
		final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
    	broadcastManager.unregisterReceiver(mDfuUpdateReceiver);
//    	unregisterReceiver(mActiveNotificationReceiver);
    	
    	if( mDBmanager != null ) {
    		mDBmanager.closeDB();
    	}
    	
//    	clearNotiList();
    }
    
    private void clearNotiList(){
        if(!mNotifyMap.isEmpty()){
            mNotifyMap.clear();
        }
        if (!mNotifyPosts.isEmpty()) {
            mNotifyPosts.clear();
        }
        
        if (!mNotifyRemoves.isEmpty()) {
            mNotifyRemoves.clear();
        }
        
    }
    
    
    private String getDeviceOtaName( String name ) {
    	if( name ==  null ) {
    		Log.e(TAG, "getDeviceOtaName() = null");
    		return null;
    	}
		return "FGZ " + name.substring(name.lastIndexOf(" ")+1);
    }
    
    
//    private static final String PREFS_DEVICE_NAME = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_DEVICE_NAME";
//	private static final String PREFS_FILE_NAME = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_NAME";
//	private static final String PREFS_FILE_TYPE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_TYPE";
//	private static final String PREFS_FILE_SIZE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_SIZE";
    
    private String getDfuAddress(String address) {
    	byte[] addressBytes = Utils.hexStringToBytes(address.replace(":", ""));
    	
    	Log.e(TAG, "getDfuAddress FW VERSION: " + mLocalStorage.getActivatedDeviceFwvr());
    	if( mLocalStorage.getActivatedDeviceFwvr().compareToIgnoreCase("2.3.4.7")>0 ) {
    		addressBytes[5] += 1;
    	}
    	String DfuStringAddress = String.format("%02X:%02X:%02X:%02X:%02X:%02X", addressBytes[0], addressBytes[1],addressBytes[2],
    																 			 addressBytes[3],addressBytes[4],addressBytes[5]);
    	Log.e(TAG, "DfuStringAddress: " + DfuStringAddress);
    	return DfuStringAddress;
    }
    
    private String getNormalAddress(String address) {
    	byte[] addressBytes = Utils.hexStringToBytes(address.replace(":", ""));
    	
    	Log.e(TAG, "getDfuAddress FW VERSION: " + mLocalStorage.getActivatedDeviceFwvr());
    	if( mLocalStorage.getActivatedDeviceFwvr().compareToIgnoreCase("2.3.4.7")<=0 ) {
    		addressBytes[5] -= 1;
    	}
    	String DfuStringAddress = String.format("%02X:%02X:%02X:%02X:%02X:%02X", addressBytes[0], addressBytes[1],addressBytes[2],
    																 			 addressBytes[3],addressBytes[4],addressBytes[5]);
    	Log.e(TAG, "DfuStringAddress: " + DfuStringAddress);
    	return DfuStringAddress;
    }
    
    
    private void upgradeRom( String romFileFullName ) {
//    	final File file = new File(romFileFullName);
    	/**
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final boolean dfuInProgress = preferences.getBoolean(DfuService.DFU_IN_PROGRESS, false);
//		if (dfuInProgress) {
//			//showUploadCancelDialog();
//			Log.i("onUploadClicked", "update error");
//			Message msg = new Message();
//			msg.what = UPDATE_FAILED;
//			myHandler.sendMessage(msg);
//			return;
//		}

		// Save current state in order to restore it if user quit the Activity
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFS_DEVICE_NAME, getDeviceOtaName(mUserBindDeviceName));//"FOOGEEZ");
		editor.putString(PREFS_FILE_NAME, file.getName());
		editor.putString(PREFS_FILE_TYPE, "Application");
		editor.putString(PREFS_FILE_SIZE, getString(R.string.dfu_file_size_text, file.length()));
		editor.commit();
    	**/
    	
		String uri = null;
		String mime_type = null;
		final Intent service = new Intent(this, DfuService.class);
		service.putExtra(DfuService.EXTRA_DEVICE_ADDRESS,	getDfuAddress(mUserBindDeviceAddr));
		service.putExtra(DfuService.EXTRA_DEVICE_NAME,		getDeviceOtaName(mUserBindDeviceName));
		service.putExtra(DfuService.EXTRA_FILE_MIME_TYPE,	mime_type);//DfuService.MIME_TYPE_HEX);
		service.putExtra(DfuService.EXTRA_FILE_TYPE,		DfuService.TYPE_APPLICATION);
		service.putExtra(DfuService.EXTRA_FILE_PATH,		romFileFullName);
		service.putExtra(DfuService.EXTRA_FILE_URI,			uri); // uri must be null;
		startService(service);
		Log.d(TAG, "UPGRADE DEVICE: " + mUserBindDeviceName);
	}
    
	public void upgradeRomRequest( String romFileFullName, String Version ) {
		mRomFileVersion = Version;
		mRomFileFullName = romFileFullName;
		connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_UPGRADE_ROM);
	}


	/***
     * 
     * 
     * 
     * @param phoneNumber
     * @return
     */
    
	private String getCallingName(String phoneNumber) {
	    Cursor cursor = getContentResolver().query( Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, phoneNumber), 
	    											new String[] {PhoneLookup._ID,PhoneLookup.NUMBER,PhoneLookup.DISPLAY_NAME,PhoneLookup.TYPE, PhoneLookup.LABEL }, 
	    											null, null, null );
	    /** Name:Robin  Time:20151215  Function: 此处发生 java.lang.NullPointerException 添加方法判断*/
	    if (cursor == null) {
	    	 Log.d(TAG, "Robin----getPeople null---获取联系人为空");
	    	 return null;
	    }
	    if(cursor.getCount() == 0) {
	        //没找到电话号码
	    	Log.d(TAG, "Robin----没找到电话号码");
	    } else if (cursor.getCount() > 0) {
	    	Log.i(TAG, "Robin----获取姓名---电话号码");
	        cursor.moveToFirst();
	        return cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME)); //获取姓名
	    }
	    return null;
    }
    
	/***
	 * 
	 * 
	 * 
	 */
	 private int prev_unread_message_cnt = -1;
	 private int prev_missed_calling_cnt = -1;
	 
	 private void checkUnreadSmsCnt() {
         int unreadMessageCnt = getSmsFromPhone();
         if( unreadMessageCnt !=  prev_unread_message_cnt ) {
        	 if( !mLocalStorage.restoreFunctionConfig().getMessageRemainder() ) return;

             prev_unread_message_cnt = unreadMessageCnt;
             connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_MISSED_MESSAGE);
         }
	 }
	 
	 private void checkMissCallingCnt() {
         int missedCallingCnt = getPhnFromPhone();
         if( missedCallingCnt !=  prev_missed_calling_cnt ) {

			 if( mPhoneStates == PHONE_STATE_CALLING ) {
				 Log.e(TAG, "mPhoneStates == PHONE_STATE_CALLING");
				 return;
			 }
			 if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_CALLING_HUNGED ) {
				 Log.e(TAG, "mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_CALLING_HUNGED");
				 return;
			 }
        	 if( !mLocalStorage.restoreFunctionConfig().getCallingRemainder() ) {
				 Log.e(TAG, "!mLocalStorage.restoreFunctionConfig().getCallingRemainder()");
        		 return;
        	 }
        	 
        	 prev_missed_calling_cnt = missedCallingCnt;
             connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_MISSED_CALLING);
         }
	 }
	 
	 public static Handler nonHandler = new Handler() {  //这里可以进行回调的操作  
         public void handleMessage(Message msg) {
        	 Log.d(TAG, "MESSAGE: " + msg.what);
         }
	 };
	
	 class SmsObserver extends ContentObserver {  
        public SmsObserver(Context context, Handler handler) {  
            super(handler);  
        }  
  
        @Override  
        public void onChange(boolean selfChange) {  
            super.onChange(selfChange);  
            Log.d(TAG, "SMS CONTENT CHANGED!!!!");
            checkUnreadSmsCnt();
        }  
	 }
	 
	 class PhnObserver extends ContentObserver {  
        public PhnObserver(Context context, Handler handler) {  
            super(handler);  
        }  
  
        @Override  
        public void onChange(boolean selfChange) {  
            super.onChange(selfChange);  
            Log.d(TAG, "PHN CONTENT CHANGED!!!!");
            checkMissCallingCnt();
       }
	 }
	
	private Uri SMS_INBOX = Uri.parse("content://sms/");
	private int getSmsFromPhone() {  
        String where0 = "date>" + Utils.TodayZeroUtc() + " and type = 1 and read = 0";
        Cursor cur0 = getContentResolver().query(Uri.parse("content://sms/"), null, where0, null, "date desc");
        int unread_message_count = 0;
        if( null == cur0 ) return 0;
        unread_message_count = cur0.getCount();
        Log.d(TAG, "MESSAGE COUNT: " + unread_message_count);
	        
        cur0.close();
        return unread_message_count;
    }
    
    private Uri PHN_INBOX = CallLog.Calls.CONTENT_URI;
    private int getPhnFromPhone() {
        ContentResolver cr = getContentResolver();  
        String[] projection = new String[] { Calls.TYPE };
        String where = "date>" + Utils.TodayZeroUtc() + " and type=? and new=?";
    	Cursor cur = cr.query(PHN_INBOX, projection, where, new String[] {  Calls.MISSED_TYPE + "", "1"  }, "date desc");
        if (null == cur) return 0;
        int miss_calling_count = cur.getCount();
        Log.d(TAG, "CALLING COUNT: " + miss_calling_count);

        cur.close();
        return miss_calling_count;
    }
    
    private void onCallingStateChangeDo( int state, Intent intent ) {
    	switch ( state ) {
		case TelephonyManager.CALL_STATE_IDLE: //空闲  
        	Log.d(TAG, "Intent - CALL HANGED.");
        	if( !mLocalStorage.restoreFunctionConfig().getCallingRemainder() ) break;
        	if( mLeConnectRequestCode != CONNECT_LE_REQUEST_CODE_UNKNOWN ) {
        		Log.e(TAG, "mLeConnectRequestCode = " + mLeConnectRequestCode);
        		if( mLeConnectRequestCode != CONNECT_LE_REQUEST_CODE_CALLING_DISPLAY ) break;
        		else {
        			mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
        		}
        	}
			if( mPhoneStates == PHONE_STATE_CALLING ) {
				connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_CALLING_HUNGED);
			}
        	mPhoneStates = PHONE_STATE_HUNG_UP;
            break;  
        case TelephonyManager.CALL_STATE_RINGING: //来电  
        	mCallingNumber = intent.getStringExtra("incoming_number");  
        	mCallingName = getCallingName(mCallingNumber);
        	Log.d(TAG, "Intent - CALLING : " + mCallingNumber);
        	Log.d(TAG, "Intent - CALLING NAME: " + mCallingName);
        	if( !mLocalStorage.restoreFunctionConfig().getCallingRemainder() ) break;
        	if( mLeConnectRequestCode != CONNECT_LE_REQUEST_CODE_UNKNOWN ) {
        		Log.e(TAG, "mLeConnectRequestCode != CONNECT_LE_REQUEST_CODE_UNKNOWN");
        		break;
        	}
        	connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_CALLING_DISPLAY);
        	mPhoneStates = PHONE_STATE_CALLING;
            break;  
        case TelephonyManager.CALL_STATE_OFFHOOK: //摘机（正在通话中）  
        	Log.d(TAG, "Intent - CALL OFFHOOK.");
        	if( !mLocalStorage.restoreFunctionConfig().getCallingRemainder() ) break;
        	connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_CALLING_HUNGED);
        	mPhoneStates = PHONE_STATE_OFFHOOK;
            break;  
		}
    }
    
    private void onCallingStateChangeDo( int state, String incomingNumber ) {
    	switch ( state ) {
		case TelephonyManager.CALL_STATE_IDLE: //空闲  
        	Log.d(TAG, "onCallingStateChangeDo - CALL HANGED.");
        	if( !mLocalStorage.restoreFunctionConfig().getCallingRemainder() ) break;
        	if( mLeConnectRequestCode != CONNECT_LE_REQUEST_CODE_UNKNOWN ) {
        		Log.e(TAG, "mLeConnectRequestCode = " + mLeConnectRequestCode);
        		break;
        	}
        	
    		if( mUserBindDeviceAddr != null ) {
    			if( mPhoneStates == PHONE_STATE_CALLING ) {
    				connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_CALLING_HUNGED);
    			}
    		}
        	mPhoneStates = PHONE_STATE_HUNG_UP;
            break;  
        case TelephonyManager.CALL_STATE_RINGING: //来电  
        	mCallingNumber = incomingNumber;
        	mCallingName = getCallingName(mCallingNumber);
        	Log.d(TAG, "onCallingStateChangeDo - CALLING : " + mCallingNumber);
        	Log.d(TAG, "onCallingStateChangeDo - CALLING NAME: " + mCallingName);
        	if( !mLocalStorage.restoreFunctionConfig().getCallingRemainder() ) break;

        	//if( mCallingName == null ) {
        	//	mCallingName = "未知";
        	//}
        	
        	if( mLeConnectRequestCode != CONNECT_LE_REQUEST_CODE_UNKNOWN ) {
        		Log.e(TAG, "mLeConnectRequestCode != CONNECT_LE_REQUEST_CODE_UNKNOWN");
        		break;
        	}
        	
    		if( mUserBindDeviceAddr != null ) {
    			connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_CALLING_DISPLAY);
    		}
        	mPhoneStates = PHONE_STATE_CALLING;
            break;  
        case TelephonyManager.CALL_STATE_OFFHOOK: //摘机（正在通话中）  
        	Log.d(TAG, "onCallingStateChangeDo - CALL HANGED.");
        	if( !mLocalStorage.restoreFunctionConfig().getCallingRemainder() ) break;
    		if( mUserBindDeviceAddr != null ) {
    			connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_CALLING_HUNGED);
    		}
        	mPhoneStates = PHONE_STATE_OFFHOOK;
            break;  
		}
    }
    
	class MyPhoneStatReceiver extends BroadcastReceiver {   		    		     
	    @Override  
	    public void onReceive(Context context, Intent intent) { 
	    	if(!intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){ 
	    		TelephonyManager tm =  (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);  
	    		onCallingStateChangeDo(tm.getCallState(), intent);
	    	}
	    }
	}
    
    private final int PHONE_STATE_HUNG_UP = 0;
    private final int PHONE_STATE_CALLING = 1;
    private final int PHONE_STATE_OFFHOOK = 2;
    private int mPhoneStates = PHONE_STATE_HUNG_UP;
    
    private String mCallingName = null;
	private String mCallingNumber = null;
	private void initTelephonyService() {
		if((android.os.Build.MANUFACTURER.equalsIgnoreCase("YULONG"))&&(android.os.Build.MODEL.equalsIgnoreCase("COOLPAD 9190L"))) {
		    TelephonyManager tm =  (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);  
		    tm.listen(new PhoneStateListener() {
		    	@Override
		    	public void onCallStateChanged(int state, String incomingNumber) {
		    		onCallingStateChangeDo(state, incomingNumber);
		    	}
		    }, PhoneStateListener.LISTEN_CALL_STATE);
		}
		else {
			mPhoneStatReceiver = new MyPhoneStatReceiver();
			IntentFilter filter = new IntentFilter();
		    filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
		    filter.addAction("android.intent.action.PHONE_STATE");
		    registerReceiver(mPhoneStatReceiver, filter);
		}

		// INIT. SMS CONTENT
		smsObserver = new SmsObserver(this, nonHandler);  
        getContentResolver().registerContentObserver(SMS_INBOX, true,  smsObserver);  
        
        phnObserver = new PhnObserver(this, nonHandler);
        getContentResolver().registerContentObserver(PHN_INBOX, true,  phnObserver);  
	}


	/**
	 * 
	 * 
	 * 
	 */
	public void updateUserAndDeviceInfo() {
		mLocalStorage = new LocalStorage(CentralService.this);
		if( (!mLocalStorage.hasAnyAccount())||(!mLocalStorage.isDeviceActivated()) ) {
			if( getLeConnectState() == BluetoothProfile.STATE_CONNECTED ) {
				disconnectLeDevice();
			}
			mFirstConnectF = true;
        	mUserBindDeviceName = null;
        	mUserBindDeviceAddr = null;
        	return;
		}
		mUserAccount = mLocalStorage.getAccount();
		mUserBindDeviceName = mLocalStorage.getActivatedDeviceName();
		mUserBindDeviceAddr = mLocalStorage.getActivatedDeviceAddr();
        //Log.e(TAG, "DEVICE SERIAL NUMBER: " + sp.getString(LocalStorage.KEY_DEVICE_SRLN, null));
        mDBmanager = new DatabaseManager( CentralService.this, mUserAccount, mLocalStorage.getActivatedDeviceSrln()+".db3" );
        return;
    }
	
    public int getSQLiteOldestUtc() {
        if (mDBmanager != null) {
            return mDBmanager.getOldestRecordUtc();
        }
        return 0;
    }
	
	public int getSQLiteRecentUtc() {
	    if (mDBmanager != null) {
            return mDBmanager.getRecentRecordUtc();
        }
		return 0;
	}
	
    public String 
    getUserAccount() {
    	return mUserAccount;
    }
  
    public String
    getBindedDevice() {
    	return mUserBindDeviceAddr;
    }
    
    /*
     * 
     * 
     * 
     * 
     */
    private boolean isServiceRunning = false; 
    private void checkBluetoothLeServices( Context context ) {
	    ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE); 
	    for( RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE) ) { 
		    if("com.foogeez.bluetooth.BluetoothLeService".equals(service.service.getClassName())) { //Service的类名 
		    	Log.d(TAG, "Found " + service.service.getClassName() + "has been running!");
		    	isServiceRunning = true; 
		    }
	    }
	    
	    if( !isServiceRunning ) { 
	    	Log.d(TAG, "com.foogeez.bluetooth.BluetoothLeService is not running!");
	    	bindBluetoothLeService();
	    }
    }
    
    /**
    private boolean isNMServiceRunning = false; 
    private void checkNotificationMonitorServices( Context context ) {
	    ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE); 
	    for( RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE) ) { 
		    if("com.foogeez.services.NotificationMonitorService".equals(service.service.getClassName())) { //Service的类名 
		    	Log.e(TAG, "Found " + service.service.getClassName() + "has been running!");
		    	isNMServiceRunning = true; 
	        	//StatusBarNotification[] notifications  = mNotificationMonitorService.getActiveNotifications();
	        	//Log.e(TAG, "nofication length=" + notifications);
		    }
	    } 
	    
	    if( !isNMServiceRunning ) { 
	    	Log.e(TAG, "com.foogeez.services.NotificationMonitorService is not running!");
	    	//bindNotificationMonitorService();
	    }
    }
    /**/
    
    private void bindBluetoothLeService() {
		Intent it = new Intent(CentralService.this, BluetoothLeService.class);
		//startService(it);
		bindService(it, mServiceConnection, BIND_AUTO_CREATE);
	}
    
    private void unbindBluetoothLeService() {
		unbindService(mServiceConnection);
		//Intent it = new Intent(CentralService.this, BluetoothLeService.class);
		//stopService(it);
	} 
    
    /**
    private void bindNotificationMonitorService() {
		Intent it = new Intent(CentralService.this, NotificationMonitorService.class);
		startService(it);
		bindService(it, mNMServiceConnection, 0);
	}
	
    private void unbindNotificationMonitorService() {
		unbindService(mNMServiceConnection);
		Intent it = new Intent(CentralService.this, NotificationMonitorService.class);
		stopService(it);
	} 
	/**/
    
    private void initBluetoothLeService() {
    	Log.i(TAG, "initBluetoothLeService");
    	checkBluetoothLeServices(CentralService.this);
    }
    
    /**
    private void initNotificationMonitorService() {
    	Log.i(TAG, "initNotificationMonitorService");
    	checkNotificationMonitorServices(CentralService.this);
    }
    /**/
    private void refreshBluetoothAdapter() {
        mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        Log.i(TAG, "mBluetoothAdapter = " + mBluetoothAdapter);
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        }
    }
    
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
        	Log.i(TAG, "CentralService&BluetoothLeService --- onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder)service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                return;
            }
           
            refreshBluetoothAdapter();
            //mBluetoothAdapter = mBluetoothLeService.getBluetoothAdapter();
            broadcastUpdate(ACTION_BLUETOOTH_LE_STATE_CHANGING);
            
    		if( isInLauncher() ) {
    			requestConnectionCheckOnly();
    		}
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        	Log.i(TAG, "CentralService&BluetoothLeService --- onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };
    
    private SharedPreferences share;
    // 存储sharedpreferences
    public void setSharedPreference() {
    	Log.i(TAG, "Robin----存入数据--setSharedPreference");
    	share = getSharedPreferences("record", Context.MODE_PRIVATE);
	    Editor editor = share.edit();
	    editor.putInt("recordID", 11010);
	    editor.commit();// 提交修改
    }
    
	// 清除sharedpreferences的数据
    private void removeSharedPreference() {
    	Log.i(TAG, "Robin------void removeSharedPreference()");
    	share = getSharedPreferences("record", Context.MODE_PRIVATE);
	    Editor editor = share.edit();
	    editor.remove("recordID");
	    editor.commit();// 提交修改
    }
    
    // Code to manage Service lifecycle.
    private final ServiceConnection mNMServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
        	Log.i(TAG, "mNMServiceConnection&NotificationMonitorService --- onServiceConnected");
        }
        
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        	Log.i(TAG, "mNMServiceConnection&NotificationMonitor --- onServiceDisconnected");
        }
    };
    
    
    
    
    
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			String deviceName = getDeviceNameFromRecord(scanRecord);
			Log.i(TAG, "Robin------device name = " + deviceName + "rssi = " + rssi + "record = " + Utils.bytesToHexString(scanRecord));
			
			if( !mScanning ) {
				scanLeDevices(false, 0);
				return;
			}
			
			Log.i(TAG, "Robin------deviceName.length()---");
			/** Name:Robin  Time:20151208  Function: 添加判断对象为null的情况 */
			if(deviceName == null ||deviceName.isEmpty()==true || deviceName.length() < 8 ) {
				Log.e(TAG, "NO SUPPORT DEVICE: because name!");
				return;
			}
			if( deviceName.substring(0, 8).equals(DEVICE_NAME_FILTER0) ) {
				//ArrayList<String> recordContent = decodeLeRecord(scanRecord);
				String productType = getProductTypeFromRecord(scanRecord);
				String serialNumber = getSerialNumberFromRecord(scanRecord);
				String hardwareVersion = getHardwareVersionFromRecord(scanRecord);
				String firmwareVersion = getFirmwareVersionFromRecord(scanRecord);

				if( (mMinRssiLeDevice.get(DEVICE_INFO_RSSI) == null)||((rssi >= (Integer)mMinRssiLeDevice.get(DEVICE_INFO_RSSI))&&(rssi != 127)) ) {
					if( (productType == null)||(productType.length() != 4) ) {
						
						mMinRssiLeDevice.put(DEVICE_INFO_TYPE, "0000");
					}
					else {
						mMinRssiLeDevice.put(DEVICE_INFO_TYPE, productType);
					}
					
					if( (deviceName == null)||(deviceName.length() != 12) ) {
						mMinRssiLeDevice.put(DEVICE_INFO_NAME, "FOOGEEZ xxxx");
					}
					else {
						mMinRssiLeDevice.put(DEVICE_INFO_NAME, deviceName);
					}

					mMinRssiLeDevice.put(DEVICE_INFO_ADDR, device.getAddress());
					mMinRssiLeDevice.put(DEVICE_INFO_RSSI, rssi);
					
					if( (serialNumber == null)||(serialNumber.length() != 16) ) {
						mMinRssiLeDevice.put(DEVICE_INFO_SRLN, "FFFFFFFFFFFFFFFF");
					}
					else {
						mMinRssiLeDevice.put(DEVICE_INFO_SRLN, serialNumber);
					}

					if( (hardwareVersion == null)||(hardwareVersion.length() != 4) ) {
						mMinRssiLeDevice.put(DEVICE_INFO_HWVR, "2030");
					}
					else {
						mMinRssiLeDevice.put(DEVICE_INFO_HWVR, hardwareVersion);
					}
					
					if( (firmwareVersion == null)||(firmwareVersion.length() != 4) ) {
						mMinRssiLeDevice.put(DEVICE_INFO_FWVR, "2332");
					}
					else {
						mMinRssiLeDevice.put(DEVICE_INFO_FWVR, firmwareVersion);
					}
				}
			}
		}
    };
    
    private Runnable task = new Runnable() {
        @Override
        public void run() {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            broadcastUpdate(ACTION_BLUETOOTH_SCAN_COMPLETE);
        }
    };
    
    public boolean scanLeDevices( final boolean enable, final int delayms ) {
    	
    	refreshBluetoothAdapter();
    	if( mBluetoothAdapter == null ) {
    		Log.e(TAG, "scanLeDevices --- mBluetoothAdapter == null");
    		return false;
    	}
    	
        if( !mBluetoothAdapter.isEnabled() ) {
			Log.e(TAG, "BluetoothAdapter is disable.");
			broadcastUpdate(ACTION_BLUETOOTH_IS_DISABLE);
			mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
			return false;
        }
    	
        if( enable ) {
            // Stops scanning after a pre-defined scan period.
            if( mScanning == true ) mHandler.removeCallbacks(task);
            else mScanning = true;
            
        	Log.e(TAG, "scanLeDevices...do scan...");
            
            mMinRssiLeDevice.clear();
            //mBluetoothLeDevicesInfo.clear();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            broadcastUpdate(ACTION_BLUETOOTH_SCAN_START);
            mHandler.postDelayed(task, delayms);
            return true;
        }
        else {
        	
        	Log.e(TAG, "scanLeDevices...do scan...");
        	
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            broadcastUpdate(ACTION_BLUETOOTH_SCAN_STOP);
            return true;
        }
    }
    

    /*
     * 
     * 
     * 
     * 
     * 
     */
    
    public int getLeConnectState() {
    	if( mBluetoothLeService == null ) {
    		Log.e(TAG, "mBluetoothLeService == null");
    		return -1;
    	}
    	
    	return mBluetoothLeService.getConnectionStates();
    }

    
    
    private static boolean mScanningBleFlag = false;
    private static Handler mHandlerForConnect = new Handler();
    private static BluetoothDevice mBluetoothDevice = null;

    public void clearBluetoothDevice() {
    	mBluetoothDevice = null;
    }

    private boolean scanDevicesForConnectInThread(final boolean enable, final int delayms) {
		if( android.os.Build.MANUFACTURER.toUpperCase().equals("MEIZU") || android.os.Build.MANUFACTURER.toUpperCase().equals("LENOVO")) {
			mBluetoothLeService.unbond(mUserBindDeviceAddr);
		}
		
        new Thread(new Runnable() {
            @Override
            public void run() {
            	scanLeDevicesForConnect(enable, delayms);
            }
        }).start();
    	return true;
    }
    
    private Runnable mScanLeDeviceTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			//if( !mScanningBleFlag ) return;
        	mRemoteDevices.clear();
        	mScanningBleFlag = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallbackForConnect);
			broadcastUpdate(ACTION_ACTIONS_ADAPTER_SCAN_ERROR);
			mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
            Log.d(TAG, "scanLeDevicesForConnect --- stop...timeout!");
		}
    };
    
    private Set<BluetoothDevice> mRemoteDevices = new HashSet<BluetoothDevice>();
    private boolean scanLeDevicesForConnect( final boolean enable, final int delayms ) {
 	
    	refreshBluetoothAdapter();
    	if( mBluetoothAdapter == null ) {
    		Log.e(TAG, "scanLeDevicesForConnect --- mBluetoothAdapter == null");
    		return false;
    	}
    	
        if( !mBluetoothAdapter.isEnabled() ) {
			Log.e(TAG, "BluetoothAdapter is disable.");
			broadcastUpdate(ACTION_BLUETOOTH_IS_DISABLE);
			mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
			return false;
        }
    	
        if( enable ) {
        	if( mScanningBleFlag ) {
        		Log.e(TAG, "scanning...abort current request!");
        		return false;
        	}
        	
        	Log.e(TAG, "scanLeDevicesForConnect...do scan...");
        	
        	mScanningBleFlag = true;
            mBluetoothAdapter.startLeScan(mLeScanCallbackForConnect);
            mHandlerForConnect.postDelayed(mScanLeDeviceTimeoutRunnable, delayms);
            Log.d(TAG, "scanLeDevicesForConnect --- start...");
            return true;
        }
        else {
        	
        	Log.e(TAG, "scanLeDevicesForConnect...do stop scan...");
        	
        	mRemoteDevices.clear();
        	mScanningBleFlag = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallbackForConnect);
            mHandlerForConnect.removeCallbacks(mScanLeDeviceTimeoutRunnable);
            Log.d(TAG, "scanLeDevicesForConnect --- stop...");
            return true;
        }
    }
    
    private static final int DEVICE_MODE_NORMAL = 0;
    private static final int DEVICE_MODE_DFU = 1;
    
    private static int mDeviceMode = DEVICE_MODE_NORMAL;
    private BluetoothAdapter.LeScanCallback mLeScanCallbackForConnect = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			String deviceName = getDeviceNameFromRecord(scanRecord);
			String deviceSerialNumber = getSerialNumberFromRecord(scanRecord);
			
			mUserBindDeviceSrln = mLocalStorage.getActivatedDeviceSrln();
			//Log.e(TAG, "mUserBindDeviceSrln: " + mUserBindDeviceSrln.substring(3));
			//Log.e(TAG, "deviceSerialNumber:  " + deviceSerialNumber);
			// rssi 信号强度，即距离
			Log.i(TAG, "device name = " + deviceName + "rssi = " + rssi + "record = " + Utils.bytesToHexString(scanRecord));
			
			if( mScanningBleFlag != true ) {
				scanLeDevicesForConnect(false, 0);
				Log.e(TAG, "Scanning LE device must stoped!!!");
				return;
			}
			
			Log.i(TAG, "Robin---222---deviceName.length()---");
			/** Name:Robin  Time:20151208  Function: 添加判断对象为null的情况 */
			if(deviceName == null || deviceName.isEmpty()==true || deviceName.length() < 8 ) {
				Log.e(TAG, "NO SUPPORT DEVICE: because name!--第二次");
				return;
			}
			
			mRemoteDevices.add(device);
			if( mRemoteDevices.size() >= 10 ) {
				scanLeDevicesForConnect(false, 0);
				//broadcastUpdate(ACTION_ACTIONS_ADAPTER_SCAN_ERROR);
				//mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
				Log.e(TAG, "RemoteDevices too much, try to connect, directly!");
		    	BluetoothDevice ndevice = mBluetoothAdapter.getRemoteDevice(mUserBindDeviceAddr);
		    	connectLeDevice(ndevice, false);
			}
			else {
				
				if( deviceName.substring(0, DEVICE_NAME_FILTER0.length()).equals(DEVICE_NAME_FILTER0) ) {
					if( deviceSerialNumber.equalsIgnoreCase(mUserBindDeviceSrln.substring(3)) ) {
						Log.d(TAG, "发现已绑定设备：" + device.getAddress() + " 设备模式：普通");
						scanLeDevicesForConnect(false, 0);
						connectLeDevice(device, false);
						mDeviceMode = DEVICE_MODE_NORMAL;
					}
				}
				else if( deviceName.substring(0, DEVICE_NAME_FILTER1.length()).equals(DEVICE_NAME_FILTER1) ) {
					if( device.getAddress().equalsIgnoreCase(getDfuAddress(mUserBindDeviceAddr)) ) {
						Log.e(TAG, "发现已绑定设备：" + device.getAddress() + " 设备模式：DFU");
						scanLeDevicesForConnect(false, 0);
						connectLeDevice(device, true);
						mDeviceMode = DEVICE_MODE_DFU;
					}
				}
			}
		}
    };

    /**
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * @param addr
     */
    private void connectLeDevice(final String addr) {
    	Log.i(TAG, "connectLeDevice --- " + addr);
    	
    	refreshBluetoothAdapter();
    	
    	if( mBluetoothAdapter == null ) {
    		Log.e(TAG, "connectLeDevice --- mBluetoothAdapter == null");
    		return;
    	}

        if( !mBluetoothAdapter.isEnabled() ) {
			Log.e(TAG, "BluetoothAdapter is disable.");
			broadcastUpdate(ACTION_BLUETOOTH_IS_DISABLE);
			mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
			return;
        }
        
    	if( mBluetoothLeService == null ) {
    		Log.e(TAG, "mBluetoothLeService == null");
    		return;
    	}
    	
    	if( getLeConnectState() != BluetoothProfile.STATE_DISCONNECTED ) {
    		Log.e(TAG, "mConnectionState != STATE_DISCONNECTED");
    	}
    	
    	broadcastUpdate(ACTION_BLUETOOTH_LE_STATE_CHANGING);
    	//mBluetoothLeService.connect(addr, mBluetoothLeOpCallback);
    	
    	new Thread() {
    		@Override
			public void run() {
    			mBluetoothLeService.connect(addr, mBluetoothLeOpCallback);
    		}
    	}.start();
    }
    /**/
    
    /**/
    private void connectLeDevice(final BluetoothDevice device, final boolean clrCacheFlag) {
    	Log.i(TAG, "connectLeDevice --- " + device.getAddress());
    	
    	refreshBluetoothAdapter();
    	
    	if( mBluetoothAdapter == null ) {
    		Log.e(TAG, "connectLeDevice --- mBluetoothAdapter == null");
    		return;
    	}
    	
        if( !mBluetoothAdapter.isEnabled() ) {
			Log.e(TAG, "BluetoothAdapter is disable.");
			broadcastUpdate(ACTION_BLUETOOTH_IS_DISABLE);
			mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
			return;
        }
        
    	if( mBluetoothLeService == null ) {
    		Log.e(TAG, "mBluetoothLeService == null");
    		return;
    	}
    	
    	if( getLeConnectState() != BluetoothProfile.STATE_DISCONNECTED ) {
    		Log.d(TAG, "mConnectionState != STATE_DISCONNECTED");
    	}
    	
    	broadcastUpdate(ACTION_BLUETOOTH_LE_STATE_CHANGING);
    	//mBluetoothLeService.connect(device, mBluetoothLeOpCallback);
    	
    	new Thread() {
    		@Override
			public void run() {
    			mBluetoothLeService.connect(device, mBluetoothLeOpCallback, clrCacheFlag);
    		}
    	}.start();
    }
    
    /**
    private void connectLeDeviceWithCommand(String addr, int requestCode) {
    	Log.e(TAG, "connectLeDeviceWithCommand --- requestCode " + requestCode);
    	mLeConnectRequestCode = requestCode;
    	if( mBluetoothLeService.getConnectionStates() != BluetoothProfile.STATE_CONNECTED ) {
    		connectLeDevice(addr);
    	}
    	else {
    		if( mBluetoothLeService != null ) {
    			mBluetoothLeService.discoverServices();
    		}
    	}
    }
    /**/
    
    private void connectLeDeviceWithCommand(String addr, int requestCode) {
    	if( addr == null ) {
    		Log.e(TAG, "addr == null");
    		return;
    	}
    	
    	if( mBluetoothLeService == null ) {
    		Log.e(TAG, "mBluetoothLeService == null");
    		return;
    	}
    	
    	if( mBluetoothLeService.BluetoothLeIsBusy() ) {
			broadcastUpdate(ACTION_ACTIONS_SERVICE_GATT_BUSY);
			return;
    	}
    	
    	mLeConnectRequestCode = requestCode;
    	if( mBluetoothLeService.isConnectedDevice(mUserBindDeviceAddr) ) {
			if( !mBluetoothLeService.discoverServices() ) {
				connectLeDevice(mUserBindDeviceAddr);
			}
    	}
    	else {
    		//scanDevicesForConnectInThread(true, 5000);
    	}
    }
    
    private boolean mFirstConnectF = true;
    private void connectLeDeviceWithCommand(int requestCode) {
    	if( mUserBindDeviceAddr == null ) {
    		Log.e(TAG, "mUserBindDeviceAddr == null");
    		return;
    	}
    	
    	if( mBluetoothLeService == null ) {
    		Log.e(TAG, "mBluetoothLeService == null");
    		return;
    	}
    	
    	if( mBluetoothLeService.BluetoothLeIsBusy() ) {
			broadcastUpdate(ACTION_ACTIONS_SERVICE_GATT_BUSY);
			return;
    	}
    	
    	mLeConnectRequestCode = requestCode;
    	if( mBluetoothLeService.isConnectedDevice(mUserBindDeviceAddr) ) {
			if( !mBluetoothLeService.discoverServices() ) {
				connectLeDevice(mUserBindDeviceAddr);
			}
    	}
    	else {
    		scanDevicesForConnectInThread(true, 5000);
    	}

    }
    
    /**
    public void requestConnectionCheckOnly(String deviceAddr) {
    	connectLeDeviceWithCommand(deviceAddr, CONNECT_LE_REQUEST_CODE_NONE);
    }
    /**/
    
    public void requestConnectionCheckOnly() {
    	Log.i(TAG, "Robin----执行--requestConnectionCheckOnly");
    	setSharedPreference();
    	if( !mLocalStorage.isDeviceActivated() ) {
    		Log.e(TAG, "requestConnectionCheckOnly --- !mLocalStorage.isDeviceActivated()");
    		return;
    	}
    	connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_NONE);
    }
    
    public boolean requestConnectionConfigFuncions(int addr, byte[] data) {
    	Log.d(TAG, "mLeConnectRequestCode: " + mLeConnectRequestCode);
    	if( mLeConnectRequestCode != CONNECT_LE_REQUEST_CODE_UNKNOWN ) {
			broadcastUpdate(ACTION_ACTIONS_FUNCTION_CONFIG_FAILURE);
    		return false;
    	}
		if( mUserBindDeviceAddr != null ) {
			connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_CONFIG);
			mConfigAddr = addr;
			mConfigData = data;
			return true;
		}
		broadcastUpdate(ACTION_ACTIONS_FUNCTION_CONFIG_FAILURE);
		return false;
    }
    
    public boolean requestConnectionConfigFuncions(int addr, int data) {
    	return requestConnectionConfigFuncions(addr, Utils.int2Bytes(data, Utils.BIG_ENDIUM));
    }
    
    public void requestAppConfigFunctions() {
    	broadcastUpdate(ACTION_ACTIONS_FUNCTION_CONFIG_SUCCESS);
    }
    
    /**
    public void requestRefreshDatum() {
    	if( mUserBindDeviceAddr != null ) {
     		broadcastUpdate(ACTION_ACTIONS_DATUM_REFRESH_START);
    	}
    }
    /**/
    
    public void refreshDatum() {
    	Log.d(TAG, "refreshDatum Do!");
    	
    	if( getLeConnectState() == BluetoothProfile.STATE_CONNECTED ) {
    		Log.d(TAG, "refreshDatum --- mConnectionState == BluetoothProfile.STATE_CONNECTED");
    		if( mUserBindDeviceAddr != null ) {
    		    
//    		    broadcastUpdate(NotificationsService.ACTION_ACTIONS_ACTIVE_NOTIFICATIONS);//get active notifications
    		    
	    		sportFirstF = true;
	    		mSyncProgress = 0;
	    		mSubSyncProgress = 0;
	   			connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_REFRESH_DATUM);
    		}
    		else {
    			broadcastUpdate(ACTION_ACTIONS_DATUM_REFRESH_COMPLETE);
    		}
    	}
    	else if( getLeConnectState() == BluetoothProfile.STATE_DISCONNECTED ){
    		Log.d(TAG, "refreshDatum --- mConnectionState != BluetoothProfile.STATE_CONNECTED");
    		if( mUserBindDeviceAddr != null ) {
    			mSyncProgress = 0;
    			mSubSyncProgress = 0;
    			connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_SYN_DATUM);
    		}
    		else {
    			broadcastUpdate(ACTION_ACTIONS_DATUM_REFRESH_COMPLETE);
    		}
    	}
    	else {
    		broadcastUpdate(ACTION_ACTIONS_SERVICE_GATT_BUSY);
    		disconnectLeDevice();
    		if( mBluetoothLeService != null ) {
    			mBluetoothLeService.close();
    		}
    		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
    	}
    }
    
    public void refreshDatumAuto() {
    	Log.d(TAG, "refreshDatum Do!");
    	
    	if( getLeConnectState() == BluetoothProfile.STATE_CONNECTED ) {
    		Log.d(TAG, "refreshDatum --- mConnectionState == BluetoothProfile.STATE_CONNECTED");
    		if( mUserBindDeviceAddr != null ) {
    		    
//    		    broadcastUpdate(NotificationsService.ACTION_ACTIONS_ACTIVE_NOTIFICATIONS);//get active notifications
    		    
	    		sportFirstF = true;
	    		mSyncProgress = 0;
	    		mSubSyncProgress = 0;
	   			connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_REFRESH_DATUM_AUTO);
    		}
    		else {
    			broadcastUpdate(ACTION_ACTIONS_DATUM_REFRESH_COMPLETE);
    		}
    	}
    	else if( getLeConnectState() == BluetoothProfile.STATE_DISCONNECTED ){
    		Log.d(TAG, "refreshDatum --- mConnectionState != BluetoothProfile.STATE_CONNECTED");
    		if( mUserBindDeviceAddr != null ) {
    			mSyncProgress = 0;
    			mSubSyncProgress = 0;
    			connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_SYN_DATUM_AUTO);
    		}
    		else {
    			broadcastUpdate(ACTION_ACTIONS_DATUM_REFRESH_COMPLETE);
    		}
    	}
    	else {
    		broadcastUpdate(ACTION_ACTIONS_SERVICE_GATT_BUSY);
    		//disconnectLeDevice();
    		//if( mBluetoothLeService != null ) {
    		//	mBluetoothLeService.close();
    		//}
    		//mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
    	}
    }
    
    public void restoreSportDatum() {
    	Log.d(TAG, "restoreSportDatum Do!");
    	if( getLeConnectState() == BluetoothProfile.STATE_CONNECTED ) {
    		Log.d(TAG, "restoreSportDatum --- mConnectionState == BluetoothProfile.STATE_CONNECTED");
    		if( mUserBindDeviceAddr != null ) {
	   			mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_DATUM_RESTORE;
	   			mBluetoothLeService.writeDatumValue(mWriteBackBuf);
    		}
    	}
    	else {
    		Log.d(TAG, "restoreSportDatum --- mConnectionState != BluetoothProfile.STATE_CONNECTED");
    		if( mUserBindDeviceAddr != null ) {
    			connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_DATUM_RESTORE);
    		}
    		else {
    			broadcastUpdate(ACTION_ACTIONS_DATUM_REFRESH_COMPLETE);
    		}
    	}
    }
    
	private void createBond() {
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mUserBindDeviceAddr);
		try {
			final Method createbond = device.getClass().getMethod("createBond");
			if (createbond != null) {
				final boolean success = (Boolean) createbond.invoke(device);
				Log.i(TAG, "createBond result: " + success);
			}
		} catch (Exception e) {
			Log.e(TAG, "An exception occured while createBond " + e);
		}
	}
    
    
    public void requestAuth() {
    	Log.d(TAG, "requestAuth Do!");
    	if( getLeConnectState() == BluetoothProfile.STATE_CONNECTED ) {
    		Log.d(TAG, "requestAuth --- mConnectionState == BluetoothProfile.STATE_CONNECTED");
    		if( mUserBindDeviceAddr != null ) {
    			connectLeDeviceWithCommand(mUserBindDeviceAddr, CONNECT_LE_REQUEST_CODE_BOND);
    			//createBond();
    		}
    	}
    	else {
    		Log.d(TAG, "requestAuth --- mConnectionState != BluetoothProfile.STATE_CONNECTED");
    		if( mUserBindDeviceAddr != null ) {
    			connectLeDeviceWithCommand(mUserBindDeviceAddr, CONNECT_LE_REQUEST_CODE_BOND);
    			//createBond();
    		}
    	}
    }
    
    public void requestUnbond() {
		//mBluetoothLeService.refreshDeviceCache();
    	mBluetoothLeService.disconnect();
		mBluetoothLeService.unbond(mUserBindDeviceAddr);
    }
    
    public void disconnectLeDevice() {
    	Log.i(TAG, "disconnectLeDevice");
    	
		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
    	
    	if( mBluetoothAdapter == null ) {
    		Log.e(TAG, "disconnectLeDevice --- mBluetoothAdapter == null");
    		return;
    	}
		
        if( !mBluetoothAdapter.isEnabled() ) {
			Log.e(TAG, "BluetoothAdapter is disable.");
			broadcastUpdate(ACTION_BLUETOOTH_IS_DISABLE);
			return;
        }
    	
    	if( mBluetoothLeService == null ) {
    		Log.e(TAG, "mBluetoothLeService == null");
    		return;
    	}
    	
    	new Thread() {
    		@Override
			public void run() {
    			mBluetoothLeService.disconnect();
    			//mBluetoothLeService.close();
    		}
    	}.start();
    	
    }
    
    public boolean BluetoothLeIsBusy() {
    	return mBluetoothLeService.BluetoothLeIsBusy();
    }
    
    public List<BluetoothGattService> getSupportedServices() {
    	if( mBluetoothLeService == null ) {
    		Log.e(TAG, "mBluetoothLeService == null");
    		return null;
    	}
    	return mBluetoothLeService.getSupportedGattServices();
    }
    
    private boolean isInLauncher() {
        ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTasks = manager.getRunningTasks(2);
        
        for( RunningTaskInfo info : runningTasks ) {
        	String name = info.topActivity.getClassName();
        	Log.d(TAG, "RuningTask(1).get(" + ") =" + name);
        	if( name.equals("com.foogeez.activity.ManagerActivity") ) {
        		return true;
        	}
        }
        
    	return false;
    }
    
    private boolean isInManagerActivityLauncher() {
        ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
        
        for( RunningTaskInfo info : runningTasks ) {
        	String name = info.topActivity.getClassName();
        	Log.d(TAG, "RuningTask(1).get(" + ") =" + name);
        	if( name.equals("com.foogeez.activity.ManagerActivity") ) {
        		return true;
        	}
        }
        
    	return false;
    }
    

    private int mSubSyncProgress = 0;
    private int mSyncProgress = 0;
    public int getSyncProgress() {
    	return mSyncProgress;
    }
    
    
    private final static int ENTER_DFU_MODE = 1;
    private final static int ENTER_CONNECT_CHECK = 2;
    private final static int EXSIST_NEW_VERSION = 3;
    private final static int ENTER_REQUEST_AUTH = 4;
    private final static int ENTER_REQUEST_SYNC_AUTO = 5;
    
    public static String ROM_FILE_FULL_NAME = null;
    public static String ROM_FILE_VERSION = null;
    
    @SuppressLint("InflateParams")
	public void initConfirmUpdateRomDialog(Context context) {
    	Builder builder = new AlertDialog.Builder(context); 
    	builder.setTitle(getString(R.string.string_dialog_title_upgrade_rom));
		builder.setMessage(getString(R.string.string_dialog_message_upgrade_rom));
		builder.setPositiveButton(R.string.string_dialog_positive, mNewVersionUpgradeYesNo);
		builder.setNegativeButton(R.string.string_dialog_negative, mNewVersionUpgradeYesNo);
		builder.setCancelable(false);
		builder.show(); 
    }
    
    @SuppressLint("HandlerLeak")
	Handler centralHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	        super.handleMessage(msg);
	        switch(msg.what) {
	        	case ENTER_REQUEST_SYNC_AUTO:
	        		refreshDatum();
	        		break;
	        	case ENTER_CONNECT_CHECK:
	        		requestConnectionCheckOnly();
	        		break;
	        	case ENTER_DFU_MODE:
	        		Log.d(TAG, "Upgrading...");
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
			        		upgradeRom(mRomFileFullName);
						}
					}, 3000);
	        		break;
	        	case EXSIST_NEW_VERSION:
			        Bundle data = msg.getData();
			        String val1 = data.getString("ROM_INFO_VER");
		        	ROM_FILE_VERSION = val1;
			        ROM_FILE_FULL_NAME = data.getString("ROM_FILE_NAME");
	        		if( isInManagerActivityLauncher() ) {
	        			broadcastUpdate(ACTION_ACTIONS_CONFIRM_DFU_IN_MANAGE_ACTIVITY);
	        		}
	        		break;
	        	case ENTER_REQUEST_AUTH:
	    			if( android.os.Build.MANUFACTURER.toUpperCase().equals("MEIZU") || android.os.Build.MANUFACTURER.toUpperCase().equals("LENOVO")) {
	    				mBluetoothLeService.unbond(mUserBindDeviceAddr);
	    			}
	    			else {
	    				requestAuth();
	    			}
	        		break;

	        }
	    }
    };
    
	DialogInterface.OnClickListener mNewVersionUpgradeYesNo = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch( which ) {
				case DialogInterface.BUTTON_POSITIVE:
				    upgradeRomRequest(ROM_FILE_FULL_NAME, ROM_FILE_VERSION);
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					break;
			}
		}
    };
    
    private void upgradeRomPrepare() {
		if( isInManagerActivityLauncher() ) {
			broadcastUpdate(ACTION_ACTIONS_ENTER_DFU_IN_MANAGE_ACTIVITY);
		}
		
		checkRomOnLocal();
		Message msg = new Message();
		msg.what = ENTER_DFU_MODE;
		centralHandler.sendMessage(msg);
    	mLocalStorage.setFstRefreshDatumFlag(true);
    }
    
    
	private void checkRomOnLocal() {
		String romURL = CentralService.ROM_IMAGE;
		String path = new DatabaseHelper(this, null, null).getDatabasePath() + "/rom";
		mRomFileFullName =  path + "/" + romURL.substring(romURL.lastIndexOf("/")+1);
	}
	
	private BluetoothLeService.BluetoothLeOpCallback mBluetoothLeOpCallback = new BluetoothLeService.BluetoothLeOpCallback() {
		@Override
		public void onBluetoothLeOpStart(Token token) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onBluetoothLeOpComplete(Token token) {
			Log.i(TAG, "onBluetoothLeOpComplete --- token.type = " + token.getType());
			if( token.getType() == BluetoothLeService.OP_TYPE_READ_CONFIG ) {
				byte[] cmdAddr = token.getAddr();
				byte[] cmdData = token.getData();
				Log.d(TAG, "DATA[0x" + Utils.bytesToHexString(cmdAddr) + "] = 0x" + Utils.bytesToHexString(cmdData) );
				int addr = Utils.bytes2Int(cmdAddr, 0, 4, Utils.BIG_ENDIUM);
				int data = Utils.bytes2Int(cmdData, 0, 4, Utils.BIG_ENDIUM);
				
				if( (mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_SYN_DATUM)||(mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_REFRESH_DATUM) ) {
					mSubSyncProgress++;
					mSyncProgress = mSubSyncProgress;
					broadcastUpdate(ACTION_ACTIONS_DATUM_SYNC_PROGRESS_CHANGED);
				}
				
				if( addr == DEVICE_CMD_ADDR_TIME_ZONE ) {
		        	TimeZone tz = TimeZone.getDefault();  
		        	int offset = tz.getRawOffset()/60000;
		        	String s = "TimeZone   "+tz.getDisplayName(false, TimeZone.SHORT)+" Timezon id :: " +tz.getID() + "rawoffset:" + tz.getRawOffset()/60000;  
		        	Log.d(TAG, "TZ:" + s);
		        	if( data != offset ) {
		        		mBluetoothLeService.writeConfigValue( DEVICE_CMD_ADDR_TIME_ZONE, offset);
		        	}
				}
				else if( addr == DEVICE_CMD_ADDR_UTC ) {
		        	Calendar cal = Calendar.getInstance();
		        	int utc2 = (int) ((cal.getTimeInMillis()/1000));
		        	Log.d(TAG, "utc2 = " + utc2);		    
		        	if( Math.abs(data-utc2) >= 3 ) {
		        		mBluetoothLeService.writeConfigValue( DEVICE_CMD_ADDR_UTC, utc2 );
		    			writeConfigurtionAll(true);
		        	}
		        	else {
		        		writeConfigurtionAll(false);
		        	}
		        	return;
				}
				
				if( addr == DEVICE_CMD_ADDR_DATUM_TOTAL ) {
					Log.i(TAG, "The band has " + data + " sport history records!!!");
					if( data <= 0 ) {
						mDatumLen = 0;
						mDatumMaxLen = 0;
					}
					else {
						if( data > 2016 ) {
							mDatumLen = 0;
							mDatumMaxLen = 0;
							broadcastUpdate(ACTION_ACTIONS_SERVICE_GATT_READ_ERROR, 2016);
							return;
						}
						mDatumLen = data;
						mDatumMaxLen = data;
					}
					mBluetoothLeService.writeConfigValue( DEVICE_CMD_ADDR_DATUM_INDEX, mDatumLen );
					if( mDBmanager == null ) {
						Log.e(TAG, "mDBmanager == null!!!");
						return;
					}
					
		    		Calendar calendar = Calendar.getInstance();
		    	    int utc = (int)(calendar.getTimeInMillis()/1000);
					mLastSportDatum = getTotalSportDatumByDate(DatabaseManager.DATUM_TYPE_BY_DAY, utc);
					
				    mRecentUtc = mDBmanager.getRecentRecordUtc();
				    mBluetoothLeService.readDatumValue(BluetoothLeService.OP_TYPE_READ_DATUM_RECENT);
				    mDBmanager.clearDatumCache();
				    return;
				}
			}
			
			if( token.getType() == BluetoothLeService.OP_TYPE_READ_BATTERY_LEVEL ) {
				byte[] battery_level = token.getData();
				mBatteryLevel = battery_level[0];
				broadcastUpdate(ACTION_ACTIONS_UPDATE_BATTERY_LEVEL);
				Log.d(TAG, "BATTERY_LEVEL = 0x" + Utils.bytesToHexString(battery_level) );
			}
			
			if( token.getType() == BluetoothLeService.OP_TYPE_WRITE_CONFIG ) {
				
			}
			
			if( (token.getType()&BluetoothLeService.OP_TYPE_READ_DATUM) == BluetoothLeService.OP_TYPE_READ_DATUM ) {
				byte[] datum = token.getData();
				ActionsDatum actionsDatum = new ActionsDatum(datum);
				
				if( token.getType() == BluetoothLeService.OP_TYPE_READ_DATUM_RECENT ) {
					if( mDatumMaxLen <= 0 ) {
						mSyncProgress = 100;
					}
					else {
						Log.d(TAG, "progress : " + mSyncProgress);
						mSyncProgress = mSubSyncProgress + (100-mSubSyncProgress)*(mDatumMaxLen-mDatumLen)/mDatumMaxLen;
					}
					broadcastUpdate(ACTION_ACTIONS_DATUM_SYNC_PROGRESS_CHANGED);
					
            		if( mDatumLen <= 0 ) {
            			mWriteBackBuf = token.getData();
            			//Log.e(TAG, "mWriteBackBuf = " + Utils.bytesToHexString(mWriteBackBuf));
            			//Log.e(TAG, "S DatumEncode = " + Utils.bytesToHexString(ActionsDatum.SportDatumEncode(actionsDatum)));
            			//mWriteBackBuf = ActionsDatum.SportDatumEncode(actionsDatum);
            			
            			Log.d(TAG, "已没有数据需要读取！！！");
            			Log.d(TAG, "actionsDatum.getSportIdleCaloric = " + actionsDatum.getSportIdleCaloric() + " ,actionsDatum.getSportIdleCaloric = " + actionsDatum.getSportActiveCaloric() );
            			
                    	if( actionsDatum.getType() == ActionsDatum.DATUM_TYPE_SPORT ) {
                    		Log.e(TAG, "STEP0");
    	        			if( mLastSportDatum == null ) {
    	        				mDBmanager.addDatumInCache(actionsDatum);
    	        				mLastSportDatum = actionsDatum;
    	        			}
    	        			
    	        			if( (mLastSportDatum.getSportSteps() < actionsDatum.getSportSteps())
    	        					||(mLastSportDatum.getSportIdleCaloric() < actionsDatum.getSportIdleCaloric()) ) {
                    			mDBmanager.addDatumInCache(actionsDatum);
                    		}
    	        			else if( mLastSportDatum.getSportSteps() > actionsDatum.getSportSteps() ) {
                    			Log.e(TAG, "ERROR actionsDatum, new < old, restore it!!!");
                    			mLastSportDatum.refreshUTC();
                    			mWriteBackBuf = ActionsDatum.SportDatumEncode(mLastSportDatum);
            		        	TimeZone tz = TimeZone.getDefault();  
            		        	int offset = tz.getRawOffset()/60000;
                    			mBluetoothLeService.writeConfigValue( DEVICE_CMD_ADDR_TIME_ZONE, offset);
                    			mBluetoothLeService.writeDatumValue(mWriteBackBuf);
                    			return;
                    		}
                    	}
              		}
            		else {
	            		Log.d(TAG, "还有" + (mDatumLen) + "条数据未读取！！！");
	        			if( actionsDatum.getUTC() <= mRecentUtc ) {
	        				Log.d(TAG, "CurUTC : " + ActionsDatum.utc2DateTimeString( actionsDatum.getUTC(), TimeZone.getDefault()) );
	        				Log.d(TAG, "DbiUTC : " + ActionsDatum.utc2DateTimeString( mRecentUtc, TimeZone.getDefault()) );
	        				mBluetoothLeService.writeConfigValue(DEVICE_CMD_ADDR_DATUM_INDEX, 0);
	        				mBluetoothLeService.readDatumValue(BluetoothLeService.OP_TYPE_READ_DATUM_RECENT);
	        				mDatumLen = 0;
	        				return;
	        			}
	        			else {
	        				mBluetoothLeService.readDatumValue(BluetoothLeService.OP_TYPE_READ_DATUM_RECENT);
	        				mDatumLen--;
	        			}
	        			
                    	if( actionsDatum.getType() == ActionsDatum.DATUM_TYPE_SPORT ) {
		        			if( mLastSportDatum == null ) {
	                			mDBmanager.addDatumInCache(actionsDatum);
		        			}
		        			else if( mLastSportDatum.getSportSteps() < actionsDatum.getSportSteps() ) {
		        				mDBmanager.addDatumInCache(actionsDatum);
	                		}
	                		else if( mLastSportDatum.getSportSteps() > actionsDatum.getSportSteps() ) {
	                			Log.d(TAG, "ERROR DATUM, new < old");
	                			return;
	                		}
                    	}
                    	else {
                    		mDBmanager.addDatumInCache(actionsDatum);
                    	}
            		}
            	}
            	
				mActionsDatum = actionsDatum;
				if( mActionsDatum.getType() == ActionsDatum.DATUM_TYPE_SPORT ) {
					if( sportFirstF ) {
						sportFirstF = false;
						broadcastUpdate(ACTION_ACTIONS_SPORT_DATUM_CHANGED);
						//updateNotification(mActionsDatum.getSportSteps(), mActionsDatum.getSportSteps()*100/10000);
						return;
					}
					return;
				}
				
				if( mActionsDatum.getType() == ActionsDatum.DATUM_TYPE_SLEEP ) {
					if( sleepFirstF ) {
						sleepFirstF = false;
						broadcastUpdate(ACTION_ACTIONS_SLEEP_DATUM_CHANGED);
						//updateNotification(mActionsDatum.getSportSteps(), mActionsDatum.getSportSteps()*100/10000);
						return;
					}
					return;
				}
			}
		}

		@Override
		public void onBluetoothLeOpCompleteAll(Token token) {
			Log.i(TAG, "onBluetoothLeOpCompleteAll --- token.type = " + token.getType());

			if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_SYN_DATUM ) {
	    		mLocalStorage.setFstRefreshDatumFlag(false);
	    		if( !isInLauncher() ) {
	    			disconnectLeDevice();
	    		}
	    		broadcastUpdate(ACTION_ACTIONS_DATUM_REFRESH_COMPLETE);
	    		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;

	    		mDBmanager.refreshDatumInDB();
	    		
	    		broadcastUpdate(ACTION_ACTIONS_SPORT_DATUM_CHANGED);
	    		broadcastUpdate(ACTION_ACTIONS_SLEEP_DATUM_CHANGED);
	    		
	    		checkRomByInternet(false);
	    		
	    		if( mProgressDialog != null ) {
	    			mProgressDialog.dismiss();
	    			mProgressDialog = null;
	    		}
	    		
	    		if( !mBluetoothAdapter.getBondedDevices().contains(mBluetoothAdapter.getRemoteDevice(mUserBindDeviceAddr)) ) {
		    		Message msg = new Message();
		    		msg.what = ENTER_REQUEST_AUTH;
		    		centralHandler.sendMessage(msg);
	    		}
	    		
	    		Calendar calendar = Calendar.getInstance();
	    	    int utc = (int)(calendar.getTimeInMillis()/1000);
	    	    mLocalStorage.saveLastSyncTime(Utils.utc2DateTime(utc));
	    	    
	    	    //getLastUploadUtc();
			}
			else if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_REFRESH_DATUM ) {
	    		if( !isInLauncher() ) {
	    			disconnectLeDevice();
	    		}
	    		
	    		checkRomByInternet(false);
	    		
	    		broadcastUpdate(ACTION_ACTIONS_DATUM_REFRESH_COMPLETE);
	    		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
	    		
	    		mDBmanager.refreshDatumInDB();
	    		
	    		if( !mBluetoothAdapter.getBondedDevices().contains(mBluetoothAdapter.getRemoteDevice(mUserBindDeviceAddr)) ) {
		    		Message msg = new Message();
		    		msg.what = ENTER_REQUEST_AUTH;
		    		centralHandler.sendMessage(msg);
	    		}
	    		
	    		Calendar calendar = Calendar.getInstance();
	    	    int utc = (int)(calendar.getTimeInMillis()/1000);
	    	    mLocalStorage.saveLastSyncTime(Utils.utc2DateTime(utc));
	    	    
	    	    //getLastUploadUtc();
			}
			else if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_SYN_DATUM_AUTO ) {
	    		mLocalStorage.setFstRefreshDatumFlag(false);
	    		if( !isInLauncher() ) {
	    			disconnectLeDevice();
	    		}
	    		broadcastUpdate(ACTION_ACTIONS_DATUM_REFRESH_COMPLETE);
	    		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
	    		
	    		mDBmanager.refreshDatumInDB();
	    		
	    		if( !mBluetoothAdapter.getBondedDevices().contains(mBluetoothAdapter.getRemoteDevice(mUserBindDeviceAddr)) ) {
		    		Message msg = new Message();
		    		msg.what = ENTER_REQUEST_AUTH;
		    		centralHandler.sendMessage(msg);
	    		}
	    		
	    		Calendar calendar = Calendar.getInstance();
	    	    int utc = (int)(calendar.getTimeInMillis()/1000);
	    	    mLocalStorage.saveLastSyncTime(Utils.utc2DateTime(utc));
	    	    
	    	    //getLastUploadUtc();
			}
			else if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_REFRESH_DATUM_AUTO ) {
	    		if( !isInLauncher() ) {
	    			disconnectLeDevice();
	    		}
	    		broadcastUpdate(ACTION_ACTIONS_DATUM_REFRESH_COMPLETE);
	    		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
	    		
	    		mDBmanager.refreshDatumInDB();
	    		
	    		if( !mBluetoothAdapter.getBondedDevices().contains(mBluetoothAdapter.getRemoteDevice(mUserBindDeviceAddr)) ) {
		    		Message msg = new Message();
		    		msg.what = ENTER_REQUEST_AUTH;
		    		centralHandler.sendMessage(msg);
	    		}
	    		
	    		Calendar calendar = Calendar.getInstance();
	    	    int utc = (int)(calendar.getTimeInMillis()/1000);
	    	    mLocalStorage.saveLastSyncTime(Utils.utc2DateTime(utc));
	    	    
	    	    //getLastUploadUtc();
			}
			else if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_CALLING_DISPLAY ) {
	    		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
	    		TelephonyManager tm =  (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE); 
	    		if( tm.getCallState() != TelephonyManager.CALL_STATE_RINGING ) {
	    			connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_CALLING_HUNGED);
	    		}
			}
			else if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_CALLING_HUNGED ) {
	    		if( !isInLauncher() ) {
	    			disconnectLeDevice();
	    		}
	    		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
			}
			else if( mLeConnectRequestCode ==  CONNECT_LE_REQUEST_CODE_MISSED_CALLING ) {
	    		if( !isInLauncher() ) {
	    			disconnectLeDevice();
	    		}
	    		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
			}
			else if( mLeConnectRequestCode ==  CONNECT_LE_REQUEST_CODE_MISSED_MESSAGE ) {
	    		if( !isInLauncher() ) {
	    			disconnectLeDevice();
	    		}
	    		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
			}
    		else if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_DATUM_RESTORE ) {
	    		if( !isInLauncher() ) {
	    			disconnectLeDevice();
	    		}
	    		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
    		}
    		else if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_CONFIG ) {
    			mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
    			broadcastUpdate(ACTION_ACTIONS_FUNCTION_CONFIG_SUCCESS);
    		}
    		else if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_AUTH ) {
    			mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
    		}
		}

		@Override
		public void onBluetoothLeConnectionStateChange(int status, int newState) {
        	Log.i(TAG, "onConnectionStateChange, status = " + status + ", new State = " + newState);
        	
        	if( (newState == BluetoothProfile.STATE_DISCONNECTED)&&(mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_UPGRADE_ROM) ) {
				requestUnbond();
		    	upgradeRomPrepare();
		    	mLocalStorage.setFstRefreshDatumFlag(true);
		    	return;
        	}
        	
			if(status != BluetoothGatt.GATT_SUCCESS) {
        		Log.e(TAG, "GATT ERROR = " + status);
        		broadcastUpdate(ACTION_ACTIONS_SERVICE_GATT_ERROR, status);
        		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
			}
			else {
				broadcastUpdate(ACTION_BLUETOOTH_LE_STATE_CHANGING);
    			if (newState == BluetoothProfile.STATE_DISCONNECTED) {
    				Log.w(TAG, "Disconnected from GATT server.");
    				Log.d(TAG, "mLeConnectRequestCode = " + mLeConnectRequestCode);
    				if( mLeConnectRequestCode != CONNECT_LE_REQUEST_CODE_UNKNOWN ) {
    					mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
    				}

		        	if( mBluetoothLeService.getDataAccessibleMode() == BluetoothLeService.DEVICE_ACCESSIBLE_MODE_DFU ) {
		        		requestConnectionCheckOnly();
		        	}
    			}
			}

        	/**
    		if (status != BluetoothGatt.GATT_SUCCESS) {
        		Log.e(TAG, "GATT ERROR = " + status);
        		broadcastUpdate(ACTION_ACTIONS_SERVICE_GATT_ERROR, status);
        		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
        	}
    		else {
				broadcastUpdate(ACTION_BLUETOOTH_LE_STATE_CHANGING);
    			if (newState == BluetoothProfile.STATE_DISCONNECTED) {
    				Log.w(TAG, "Disconnected from GATT server.");
    				Log.d(TAG, "mLeConnectRequestCode = " + mLeConnectRequestCode);
    				if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_UPGRADE_ROM ) {
    					requestUnbond();
    			    	upgradeRomPrepare();
    			    	mLocalStorage.setFstRefreshDatumFlag(true);
    				}
    				else if( mLeConnectRequestCode != CONNECT_LE_REQUEST_CODE_UNKNOWN ) {
    					mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
    	        		broadcastUpdate(ACTION_ACTIONS_SERVICE_GATT_ERROR, status);
    		        	if( mBluetoothLeService.getDataAccessibleMode() == BluetoothLeService.DEVICE_ACCESSIBLE_MODE_DFU ) {
    		        		requestConnectionCheckOnly();
    		        	}
    				}
    				else {
    					mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
    		        	if( mBluetoothLeService.getDataAccessibleMode() == BluetoothLeService.DEVICE_ACCESSIBLE_MODE_DFU ) {
    		        		requestConnectionCheckOnly();
    		        	}
    				}
    			}
    		}
    		/**/
		}

		@Override
		public void onBluetoothLeServicesDiscovered(int status) {
        	Log.d(TAG, "onServicesDiscovered!!!!!, status = " + status);
        	if (status != BluetoothGatt.GATT_SUCCESS) {
        		Log.e(TAG, "GATT ERROR = " + status);
        		broadcastUpdate(ACTION_ACTIONS_SERVICE_GATT_ERROR, status);
        		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
        		return;
        	}
			
        	mFirstConnectF = false;
        	if( mBluetoothLeService.getDataAccessibleMode() == BluetoothLeService.DEVICE_ACCESSIBLE_MODE_DFU ) {
        		/**
    			Log.e(TAG, "MY GOD! DFU : " + mFirstRunning + " ?????????");
        		if( mFirstRunning ) {
        			mFirstRunning = false;
        			mBluetoothLeService.refreshDeviceCache();
        			disconnectLeDevice();
        			return;
        		}
        		else {
        			mBluetoothLeService.writeDfuReset();
        		}
        		/**/
        		disconnectLeDevice();
        		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UPGRADE_ROM;
        		
        		//upgradeRomPrepare();
        		//mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
        	}
        	else if( mBluetoothLeService.getDataAccessibleMode() == BluetoothLeService.DEVICE_ACCESSIBLE_MODE_NORMAL ) {
	    		broadcastUpdate(ACTION_BLUETOOTH_LE_GATT_DISCOVERED);
	    		
	    		if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_NONE ) {
	    			mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
	    		}
	    		else if(mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_BOND ) {
	    			createBond();
	    			mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
	    		}
	    		else if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_SYN_DATUM ) {	//CONNECT_LE_REQUEST_CODE_CALLING_DISPLAY
	    	    	mBluetoothLeService.readBatteryValue();
	    			mBluetoothLeService.writeConfigValue(DEVICE_CMD_ADDR_CALLING_MISS, mLocalStorage.restoreFunctionConfig().getCallingRemainder()?getPhnFromPhone():0);
	    			mBluetoothLeService.writeConfigValue(DEVICE_CMD_ADDR_MESSAGE_MISS, mLocalStorage.restoreFunctionConfig().getMessageRemainder()?mActiveNofiCount:0);
	    			mBluetoothLeService.readConfigValue(DEVICE_CMD_ADDR_TIME_ZONE);
	    			mBluetoothLeService.readConfigValue(DEVICE_CMD_ADDR_SAVING_TIME);
	    			mBluetoothLeService.readConfigValue(DEVICE_CMD_ADDR_UTC);
	    			mBluetoothLeService.readConfigValue(DEVICE_CMD_ADDR_DATUM_TOTAL);
	    		}
	    		else if( (mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_REFRESH_DATUM)||
	    				 (mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_SYN_DATUM_AUTO)||
	    				 (mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_REFRESH_DATUM_AUTO) ) {
	    	    	mBluetoothLeService.readBatteryValue();
	    			mBluetoothLeService.writeConfigValue(DEVICE_CMD_ADDR_CALLING_MISS, mLocalStorage.restoreFunctionConfig().getCallingRemainder()?getPhnFromPhone():0);
	    			mBluetoothLeService.writeConfigValue(DEVICE_CMD_ADDR_MESSAGE_MISS, mLocalStorage.restoreFunctionConfig().getMessageRemainder()?mActiveNofiCount:0);
	    			mBluetoothLeService.readConfigValue(DEVICE_CMD_ADDR_TIME_ZONE);
	    			mBluetoothLeService.readConfigValue(DEVICE_CMD_ADDR_UTC);
		   			mBluetoothLeService.readConfigValue(DEVICE_CMD_ADDR_DATUM_TOTAL);
	    		}
	    		else if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_CALLING_DISPLAY ) { //CONNECT_LE_REQUEST_CODE_SYN_DATUM
	    			if( mCallingNumber != null ) {
	    				callingIncoming(mCallingNumber, mCallingName);
	    				}
	    		}
	    		else if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_CALLING_HUNGED ) {
		    		unCallingIncoming();
		    		int miss_calling_cnt = getPhnFromPhone();
		    		if( prev_missed_calling_cnt != miss_calling_cnt ) {
		    			prev_missed_calling_cnt = miss_calling_cnt;
		    			mBluetoothLeService.writeConfigValue(DEVICE_CMD_ADDR_CALLING_MISS, prev_missed_calling_cnt);
		    		}
	    		}
	    		else if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_MISSED_CALLING ) {
	    			mBluetoothLeService.writeConfigValue(DEVICE_CMD_ADDR_CALLING_MISS, prev_missed_calling_cnt);
	    		}
	    		else if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_MISSED_MESSAGE ) {
	    		   /** BlockingDeque<MessageBean> list = mNotifyMap.get(CONNECT_LE_REQUEST_CODE_MISSED_MESSAGE);
                    int unreadSize = 0;
                    while (list.size() > 0) {
                        unreadSize = list.size();
                        MessageBean messageBean = list.poll();
                        Log.i("evan", "consume=" + messageBean.toString() + " unreadSize=" + unreadSize);
                        mBluetoothLeService.writeConfigValue(DEVICE_CMD_ADDR_MESSAGE_MISS |
                                (messageBean.dataType << 8),
                                unreadSize);
                    }

                    while (mNotifyRemoves.size() > 0) {
                        MessageBean messageBean = mNotifyRemoves.poll();
                        Log.i("evan", "write remove=" + messageBean.toString());
                        mBluetoothLeService.writeConfigValue(DEVICE_CMD_ADDR_MESSAGE_MISS |
                                (messageBean.dataType << 8),
                                0);
                    }*/
	    		    
	    		    mBluetoothLeService.writeConfigValue(DEVICE_CMD_ADDR_MESSAGE_MISS, prev_unread_message_cnt);
	    		}
	    		else if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_UPGRADE_ROM ) {
	    			mBluetoothLeService.writeConfigValue( DEVICE_CMD_ADDR_OTA_ENABLE, 0x01000000 );
	    		}
	    		else if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_DATUM_RESTORE ) {
	    			mBluetoothLeService.writeDatumValue(mWriteBackBuf);
	    		}
	    		else if( mLeConnectRequestCode == CONNECT_LE_REQUEST_CODE_CONFIG ) {
	    			mBluetoothLeService.writeConfigValue(mConfigAddr, mConfigData);
	    		}
				
				sportFirstF = true;
				sleepFirstF = true;
        	}
			mFirstRunning = false;
		}

		@Override
		public void onBluetoothLeCharacteristicRead(int status,BluetoothGattCharacteristic characteristic) {
        	Log.i(TAG, "onCharacteristicRead, status = " + status); 	
        	if (status != BluetoothGatt.GATT_SUCCESS) {
        		Log.e(TAG, "GATT ERROR = " + status);
        		broadcastUpdate(ACTION_ACTIONS_SERVICE_GATT_ERROR,status);
        		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
        		return;
        	}
		}

		@Override
		public void onBluetoothLeCharacteristicWrite(int status,BluetoothGattCharacteristic characteristic) {
        	Log.i(TAG, "onCharacteristicWrite, status = " + status);	
        	if (status != BluetoothGatt.GATT_SUCCESS) {
        		Log.e(TAG, "GATT ERROR = " + status);
        		broadcastUpdate(ACTION_ACTIONS_SERVICE_GATT_ERROR,status);
        		if( mLeConnectRequestCode != CONNECT_LE_REQUEST_CODE_UPGRADE_ROM ) {
        			mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
        		}
        		return;
        	}
		}

		@Override
		public void onBluetoothLeCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        	Log.i(TAG, "onCharacteristicChanged");
        	if( characteristic.getUuid().toString().equals(BluetoothLeGattAttributes.DEVICE_CONFIG_DATA) ) {
        		Log.e(TAG, "value : " + Utils.bytesToHexString(characteristic.getValue()));
				endCallingInComing();
        	}
		}

		@Override
		public void onBluetoothLeDescriptorWrite(int status, BluetoothGattDescriptor descriptor) {
        	Log.i(TAG, "onDescriptorWrite, status = " + status);	
        	if (status != BluetoothGatt.GATT_SUCCESS) {
        		Log.e(TAG, "GATT ERROR = " + status);
        		broadcastUpdate(ACTION_ACTIONS_SERVICE_GATT_ERROR,status);
        		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
        		return;
        	}
		}

    };
    
    private void writeConfigurtionAll( boolean force ) {
    	if( (mBluetoothLeService != null)&&((mLocalStorage.getFstRefreshDatumFlag()||force)) ) {
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_ALARM|0x0000, mLocalStorage.restoreAlarmConfig(0).getEncode());
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_ALARM|0x0100, mLocalStorage.restoreAlarmConfig(1).getEncode());
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_ALARM|0x0200, mLocalStorage.restoreAlarmConfig(2).getEncode());
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_ALARM|0x0300, mLocalStorage.restoreAlarmConfig(3).getEncode());
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_ALARM|0x0400, mLocalStorage.restoreAlarmConfig(4).getEncode());
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_CALENDAR|0x0000, mLocalStorage.restoreCalendarConfig(0).getEncode());
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_CALENDAR|0x0100, mLocalStorage.restoreCalendarConfig(1).getEncode());
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_CALENDAR|0x0200, mLocalStorage.restoreCalendarConfig(2).getEncode());
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_CALENDAR|0x0300, mLocalStorage.restoreCalendarConfig(3).getEncode());
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_CALENDAR|0x0400, mLocalStorage.restoreCalendarConfig(4).getEncode());
    		
    		SedentaryConfiguration config = mLocalStorage.restoreSedentaryConfig();
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_SEDENTARY, config.isActived()?config.getEncode():0);
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_USR_INFO, mLocalStorage.restoreUsrInfoConfig().getEncode());
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_DISTURB_MODE_ENABLE, mLocalStorage.restoreDisturbModeConfig().getEncode());

    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_TIME_FORMAT24, mLocalStorage.restoreFunctionConfig().getDisplayFormat24()?0:1);
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_DISPLAY_DISTANCE, mLocalStorage.restoreFunctionConfig().getDisplayDistance()?1:0);
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_DISPLAY_CALORIC, mLocalStorage.restoreFunctionConfig().getDisplayCaloric()?1:0);
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_DISPLAY_SLEEPTIME, mLocalStorage.restoreFunctionConfig().getDisplaySleepTime()?1:0);
    		mBluetoothLeService.writeConfigValue(CentralService.DEVICE_CMD_ADDR_STOP_WATCH_ENABLE, mLocalStorage.restoreFunctionConfig().getStopWatchEnable()?1:0);
    	}
    }
    
    private void callingIncoming( String number, String name ) {

    	byte nextC = 0;
    	byte nextR = 0;
    	byte startC = 0;
    	byte startR = 0;
    	byte[] command = new byte[4];
    	byte[] parameter = new byte[20];
    	
		byte[] param = new byte[16];
    	
		param[0] = 0x01;
		param[1] = (byte) ((name==null)?0x00:0x02);
		param[2] = 0x00;
		param[3] = 0x00;
		param[4] = 0x00;
		param[5] = 0x00;
		
    	if( name != null ) {
    		//name = "未知";
	    	int type = FontPicker.FONT_SIZE_TYPE_1;
	    	FontPicker fontPicker = new FontPicker(CentralService.this);
	    	fontPicker.setSrc(name, type);
	    	byte[] fontarray = fontPicker.getFont(0xa8);
	    	Log.d(TAG, "fontarray.length = " + fontarray.length);
	    	Log.d(TAG, "fontarray = " + Utils.bytesToHexString(fontarray));
			
			for( int i = 0; i < fontarray.length;) {
				
				int n = 0;
				for( n = 0; n < 20; n++) {
					parameter[n] = fontarray[i++];
					if( i >= fontarray.length ) break;
				}
				
				byte[] sendbuf = new byte[n];
				for( int j = 0; j < n; j++ ) {
					sendbuf[j] = parameter[j];
				}
				
				nextC = (byte)(i/((type==FontPicker.FONT_SIZE_TYPE_1)?3:2));
				nextR = (byte)(i%((type==FontPicker.FONT_SIZE_TYPE_1)?3:2));
				
				command[0] = DEVICE_CMD_ADDR_FONT_ARRAY;
				command[1] = (byte) type;
				command[2] = startC;
				command[3] = startR;
				mBluetoothLeService.writeConfigValue(command, sendbuf);
	 			
	 			startC = nextC;
	 			startR = nextR;
			}
			
			Log.d(TAG, "OVER, C: " + nextC + " R: " + nextR );
    	}
		
    	int len = 0;
    	byte[] strFilter = null;

    	if( number.length() > 12 ) {
    		len = 12;
    		strFilter = number.substring(number.length()-12).getBytes();
    	}
    	else {
    		len = number.length();
    		strFilter = number.getBytes();
    	}
    	
		byte[] temp = new byte[20];
    	for( int i = 0; i < 20; i++ ) {
    		if( i >= len ) {
    			temp[i] = 0x0f;
    		}
    		else {
	    		if( (strFilter[i] >= '0')&&(strFilter[i] <= '9') ) {
	    			temp[i] = (byte) (strFilter[i]-'0');
	    		}
	    		else {
	    			temp[i] = 0;
	    		}
    		}
    	}
    	
    	for( int i = 0; i < 10; i++ ) {
    		param[6+i] = (byte) ((temp[i*2]<<4) + temp[i*2+1]);
    	}

    	mBluetoothLeService.writeConfigValue(DEVICE_CMD_ADDR_CALLING, param);
    	mBluetoothLeService.enableConfigValueNotification();
    }
    
    private void unCallingIncoming() {
    	byte[] param = new byte[16];
    	mBluetoothLeService.writeConfigValue(DEVICE_CMD_ADDR_CALLING, param);
    }
    
	public void endCallingInComing() {  
        TelephonyManager telMag = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);  
        Class<TelephonyManager> c = TelephonyManager.class;  
        Method mthEndCall = null;  
        try {  
            mthEndCall = c.getDeclaredMethod("getITelephony", (Class[]) null);  
            mthEndCall.setAccessible(true);  
            ITelephony iTel = (ITelephony) mthEndCall.invoke(telMag, (Object[])null);  
            iTel.endCall();  
            //LogOut.out(this, iTel.toString());  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		mPhoneStates = PHONE_STATE_HUNG_UP;
		mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
		
		if( !isInLauncher() ) {
			disconnectLeDevice();
		}

       // LogOut.out(this, "endCall test");  
    }
    
    /*
     * 
     * 
     * 
     * 
     * 
     */
    //public List<Map<String,Object>>
    //getLeDevicesInfo() {
    //	return mBluetoothLeDevicesInfo;
    //}
    
    public Map<String,Object>
    getMinRssiLeDevice() {
		return mMinRssiLeDevice;
    }
    
    public ActionsDatum 
    getTotalSportDatumByDate( int type, int utc ) {
    	if( mDBmanager == null ) return null;
	    ActionsDatum actionsDatum = mDBmanager.getTotalSportDatumByDate(type, utc);
	    return actionsDatum;
    }
    
    public List<Integer> 
    getHistorySportDatumByDate( int type, int utc) {
    	if( mDBmanager == null ) return null;
	    List<Integer> historySportDatum = mDBmanager.getHistorySportDatumByDate(type, utc);
	    return historySportDatum;
    }
    
    public List<ActionsDatum> 
    getHistorySportDatumByDate2( int type, int utc) {
    	if( mDBmanager == null ) return null;
	    List<ActionsDatum> historySportDatum = mDBmanager.getHistorySportDatumByDate2(type, utc);
	    return historySportDatum;
    }
    
    public ActionsDatum 
    getTotalSleepDatumByDate( int type, int utc ) {
    	if( mDBmanager == null ) return null;
	    ActionsDatum actionsDatum = mDBmanager.getTotalSleepDatumByDate(type, utc);
	    return actionsDatum;
    }
    
    public List<ActionsDatum> 
    getHistorySleepDatumByDate( int type, int utc) {
    	if( mDBmanager == null ) return null;
	    List<ActionsDatum> historySleepDatum = mDBmanager.getHistorySleepDatumByDate(type, utc);
	    return historySleepDatum;
    }
    
    public List<ActionsDatum>
    getHistorySleepDatumByNumber( int number ) {
    	if( mDBmanager == null ) return null;
    	List<ActionsDatum> historySleepDatum = mDBmanager.getHistorySleepDatumByNumber(number);
    	return historySleepDatum;
    }
    
    public List<ActionsDatum> getNewSportDatum(int utcLast) {
    	List<ActionsDatum> sportDatums = new ArrayList<ActionsDatum>();
    	int currentUtc = Utils.getUtc();
    	for( int indexUtc = utcLast; indexUtc <= currentUtc+24*3600; indexUtc += 24*3600 ) {
    		List<ActionsDatum> sports = getHistorySportDatumByDate2(DatabaseManager.DATUM_TYPE_BY_DAY, indexUtc);
    		if( sports != null ) {
    			sportDatums.addAll(sports);
    		}
    	}
    	return sportDatums;
    }
    
    public List<ActionsDatum> getNewSleepDatum(int utcLast) {
    	List<ActionsDatum> sleepDatums = new ArrayList<ActionsDatum>();
    	int currentUtc = Utils.getUtc();
    	for( int indexUtc = utcLast; indexUtc <= currentUtc+24*3600; indexUtc += 24*3600 ) {
    		List<ActionsDatum> sleeps = getHistorySleepDatumByDate(DatabaseManager.DATUM_TYPE_BY_DAY, indexUtc);
    		if( sleeps != null ) {
    			sleepDatums.addAll(sleeps);
    		}
    	}
    	return sleepDatums;
    }
    
    public int 
    getHistorySleepDatumIndex() {
    	if( mDBmanager == null ) return 0;
    	int index = mDBmanager.getHistorySleepDatumIndex();
    	return index;
    }
    
    public int 
    getHistorySleepDatumSize() {
    	if( mDBmanager == null ) return 0;
    	int size = mDBmanager.getHistorySleepDatumSize();
    	return size;
    }
    
    /**
    private static boolean 
    isOldLeDevice( String sn ) {
		for( Map<String,Object> selector : mBluetoothLeDevicesInfo ) {
			if( selector.get(DEVICE_INFO_SRLN).equals(sn) ) {
				return true;
			}
		}
		return false;
    }
    /**/
    /*
     * 
     */
    private void initNotification() {
		Log.i(TAG, "initNotification");
    	
    	context = CentralService.this;
    	
    	Intent it  = new Intent(context, ManagerActivity.class);
    	it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	PendingIntent pt = PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);
    	
    	
    	String appTitle = getResources().getString(R.string.app_title);
    	
    	//NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	Notification notification = new Notification.Builder(context)
        //.setLargeIcon(icon)
        .setSmallIcon(R.drawable.ic_launcher)
        .setTicker(appTitle)
        //.setContentInfo("contentInfo")
        .setContentTitle(appTitle)
        .setContentText(" ")
        //.setNumber(124)
        //.setAutoCancel(true)
        .setDefaults(Notification.DEFAULT_ALL & (~Notification.DEFAULT_LIGHTS))
        .setContentIntent(pt)
        .build();
    	
    	//manager.notify(1, notification);
        startForeground(1, notification);
    }
    
    
    @SuppressLint("DefaultLocale")
	private void updateNotification(int steps, int percent) {
    	context = CentralService.this;
    	
    	Intent it  = new Intent(context, ManagerActivity.class);
    	it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	PendingIntent pt = PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);
    	
    	String notice = String.format("您今天走了%d步， 完成了目标的%d!", steps, percent);
    	
    	//NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	Notification notification = new Notification.Builder(context)
        //.setLargeIcon(icon)
        .setSmallIcon(R.drawable.ic_launcher)
        .setTicker("极致生活")
        //.setContentInfo("contentInfo")
        .setContentTitle("极致生活")
        .setContentText(notice)
        //.setNumber(124)
        //.setAutoCancel(true)
        .setDefaults(Notification.DEFAULT_ALL)
        .setContentIntent(pt)
        .build();
    	
    	//manager.notify(1, notification);
        startForeground(1, notification);
    }
    

    /*
     * 
     * 
     * 
     * 
     * 
     */
	class LoopThread extends Thread {
		public void run() {
			for( ;; ) {
				try {
					Thread.sleep(1000);
					//Log.i(TAG, "sleep 1000 ms!");
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
    	}
    }
	
    /*
     * 
     * 
     * 
     * 
     * 
     */
    public void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    
    public void broadcastUpdate(final String action, int status) {
        final Intent intent = new Intent(action);
        Bundle bundle = new Bundle();
        bundle.putInt("GATT_STATUS", status);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }
    
    public void broadcastUpdate(final String action, byte value) {
        final Intent intent = new Intent(action);
        Bundle bundle = new Bundle();
        bundle.putInt("GATT_DATA", value);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }
    
    /**
    public void enterMainSettingActivityWithCmd(int command) {
		Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt("CENTRAL_CMD", command);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
		intent.setClass(this, MainSettingActivity.class);
		startActivity(intent);
    }
    /**/
    
    
    public ActionsDatum getActionsDatum() {
    	return mActionsDatum;
    }
    
    
    
    public void checkRomByInternet(final boolean force) {
		new Thread(new Runnable(){
			@SuppressLint("DefaultLocale")
			@Override
		    public void run() {
				
		        String today_date = Utils.getTodayDate();
		        String last_check = mLocalStorage.getRomUpdateLastCheck();
		        
				if( !force ) {
			        Log.d(TAG, "today_date: " + today_date);
			        Log.d(TAG, "last_check: " + last_check);
			        if( today_date.compareToIgnoreCase(last_check) <= 0 ) {
			        	return;
			        }
				}
				
		        Message msg = new Message();
		        Bundle data = new Bundle();
		        
		        String romURL = CentralService.ROM_IMAGE+"?utc="+Utils.getUtc();
		        String infoURL = CentralService.ROM_IMAGE_INF+"?utc="+Utils.getUtc();
		        
		        String path = new DatabaseHelper(CentralService.this, null, null).getDatabasePath() + "/rom";
		        String info = Utils.downloadFile2String(path, infoURL);
		        if( info == null ) {
		        	return;
		        }
		        
		        String md5 = info.substring(info.indexOf(":")+1, info.indexOf("\n")-1);
		        String ver = info.substring(info.lastIndexOf(":")+1);
		        String rom = Utils.downloadFileAndCaculateMd5(path, romURL);
		        if( rom == null ) {
		        	return;
		        }
		        
		        String mDeviceFwvr = mLocalStorage.getActivatedDeviceFwvr(); 
		        
	        	Log.d(TAG, "ver = " + ver);
	        	Log.d(TAG, "cur = " + mDeviceFwvr.toUpperCase().replace(".", ""));
		        if( ver.toUpperCase().compareTo(mDeviceFwvr.toUpperCase().replace(".", "")) > 0 ) {  //-----888888888----固件更新----版本判断
		        	if( md5.equalsIgnoreCase(rom) ) {
		        		
		        		mLocalStorage.setRomUpdateLastCheck(today_date);
		        		
				        data.putString("ROM_FILE_NAME", path + "/" + romURL.substring(romURL.lastIndexOf("/")+1, romURL.indexOf("?")));
				        data.putString("ROM_INFO_MD5", md5);
				        data.putString("ROM_INFO_VER", Utils.addVersionDot(ver));
				        mDeviceFwvr = Utils.addVersionDot(ver);
				        data.putString("ROM_FILE_MD5", rom);
				        msg.setData(data);
						msg.what = EXSIST_NEW_VERSION;
						centralHandler.sendMessage(msg);
		        	}
		        }
		        else {

		        }
		    }
		}).start();
	}
    
    /*
     * 
     * 
     * 
     */
    
	private TextView	mProgressPercent = null;
    private TextView	mProgressStatus = null;
    private ProgressBar mProgressBar = null;
    private AlertDialog mProgressDialog = null;
    
    private Context mContext = null;
    
	@SuppressLint("InflateParams")
	public void initUpdateRomDialog(Context context) {
		mContext = context;
	    final View view = LayoutInflater.from(mContext).inflate(R.layout.update_rom_dialog_style, null); 
	    Builder builder = new AlertDialog.Builder(mContext); 
	    builder.setCancelable(false);
	    mProgressDialog = builder.setView(view).show();
	    mProgressPercent = (TextView) view.findViewById(R.id.id_tv_progress_percent);
	    mProgressStatus = (TextView) view.findViewById(R.id.id_tv_update_status);
	    mProgressBar = (ProgressBar) view.findViewById(R.id.id_pbr_progress);
    }
    
	public void updateProgressBar(final int progress, final int part, final int total, final boolean error) {
		switch (progress) {
		case DfuService.PROGRESS_CONNECTING:
			mProgressBar.setIndeterminate(true);
			mProgressStatus.setText(R.string.dfu_status_connecting);
			break;
		case DfuService.PROGRESS_STARTING:
			mProgressBar.setIndeterminate(true);
			mProgressStatus.setText(R.string.dfu_status_starting);
			break;
		case DfuService.PROGRESS_VALIDATING:
			mProgressBar.setIndeterminate(true);
			mProgressStatus.setText(R.string.dfu_status_validating);
			break;
		case DfuService.PROGRESS_DISCONNECTING:
			mProgressBar.setIndeterminate(true);
			mProgressStatus.setText(R.string.dfu_status_disconnecting);
			break;
		case DfuService.PROGRESS_COMPLETED:
			mProgressBar.setIndeterminate(false);
			mProgressStatus.setText(R.string.dfu_status_completed);
			mProgressPercent.setText(getString(R.string.dfu_progress, 100));
			//mProgressPercent.setText(getString(R.string.dfu_status_uploading, 100));

			// let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
			new Handler().postDelayed(new Runnable() {
				@SuppressLint("InflateParams")
				@Override
				public void run() {
					//onTransferCompleted();
					Log.i("DfuService", "update success");

					// if this activity is still open and upload process was completed, cancel the notification
					final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					manager.cancel(DfuService.NOTIFICATION_ID);
					
					mProgressDialog.dismiss();
				    final View view = LayoutInflater.from(mContext).inflate(R.layout.update_rom_finish_dialog_style, null); 
				    Builder builder = new AlertDialog.Builder(mContext); 
				    mProgressDialog = builder.setView(view).show();
				    mProgressDialog.setCanceledOnTouchOutside(false);
				    
				    new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							broadcastUpdate(ACTION_ACTIONS_ENTER_DFU_SUCCESS);
							mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
							mUserBindDeviceAddr = getNormalAddress(mUserBindDeviceAddr);
							mLocalStorage.saveActivatedDeviceAddr(mUserBindDeviceAddr);
							mLocalStorage.saveActivatedDeviceFwvr(mRomFileVersion);
							refreshDatum();
						}
				    }, 5000);
				}
			}, 200);
			

			break;
		case DfuService.PROGRESS_ABORTED:
			mProgressPercent.setText(R.string.dfu_status_aborted);
			mProgressStatus.setText(R.string.dfu_status_aborted);
			//progressBar.setMessage(getString(R.string.dfu_status_aborted));
			// let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
			mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
	    	broadcastUpdate(ACTION_ACTIONS_SERVICE_GATT_ERROR);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					//onUploadCanceled();
					Log.i("DfuService", "update cancel");
					mProgressDialog.dismiss();
					// if this activity is still open and upload process was completed, cancel the notification
					final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					manager.cancel(DfuService.NOTIFICATION_ID);
				}
			}, 200);
			break;
		default:
			mProgressBar.setIndeterminate(false);
			if (error) {
				//showErrorMessage(progress);
				Log.i("DfuService", "update error");
				mProgressDialog.dismiss();
				mLeConnectRequestCode = CONNECT_LE_REQUEST_CODE_UNKNOWN;
				
				mProgressDialog.dismiss();
			    final View view = LayoutInflater.from(mContext).inflate(R.layout.update_rom_finish_dialog_style, null);
			    TextView updateWarnningTitle = (TextView)view.findViewById(R.id.id_tv_update_success);
			    TextView updateWarnningContent = (TextView)view.findViewById(R.id.id_tv_update_status);
			    updateWarnningTitle.setText(R.string.dfu_update_rom_failure);
			    updateWarnningContent.setText(R.string.dfu_update_device_info2);
			    Builder builder = new AlertDialog.Builder(mContext); 
			    mProgressDialog = builder.setView(view).show();
			    mProgressDialog.setCanceledOnTouchOutside(false);
			    
			    new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						mProgressDialog.dismiss();
					}
			    }, 3000);
		    	//broadcastUpdate(ACTION_ACTIONS_SERVICE_GATT_ERROR);
		    	//requestConnectionCheckOnly();
				//mProgressDialog.dismiss();
				//new ConfigRspDialog(MainSettingActivity.this, getResources().getString(R.string.string_login_account_error)).show(1000);
			} else {
				mProgressBar.setProgress(progress);
				mProgressPercent.setText(getString(R.string.dfu_progress, progress));
				//mProgressPercent.setText(getString(R.string.dfu_status_uploading, progress));
				
				//progressStatus.setText(getString(R.string.dfu_progress, progress));
				//if (total > 1)
				//	progressStatus.setText(getString(R.string.dfu_status_uploading_part, part, total));
				//else
				//	progressStatus.setText(R.string.dfu_status_uploading);
				mProgressStatus.setText(getString(R.string.dfu_status_uploading));
			}
			break;
		}
	}
	
    private static IntentFilter makeActiveNotifIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NotificationsService.ACTION_ACTIONS_ACTIVE_NOTIFICATIONS);
        intentFilter.addAction(CentralService.ACTION_ACTIONS_NOTIFICATION_POSTED);
        intentFilter.addAction(CentralService.ACTION_ACTIONS_NOTIFICATION_REMOVED);
        return intentFilter;
    }
	
    private ConcurrentHashMap<Integer,LinkedBlockingDeque<MessageBean>> mNotifyMap=new ConcurrentHashMap<Integer, LinkedBlockingDeque<MessageBean>>(); 
    private LinkedBlockingDeque<MessageBean> mNotifyPosts=new LinkedBlockingDeque<MessageBean>();
    private LinkedBlockingDeque<MessageBean> mNotifyRemoves=new LinkedBlockingDeque<MessageBean>();

    private int mActiveNofiCount=0;
    
	private final BroadcastReceiver mActiveNotificationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(NotificationsService.ACTION_ACTIONS_ACTIVE_NOTIFICATIONS)) {
                mActiveNofiCount = intent.getIntExtra(NotificationsService.EXTRA_DATA, 0);
                Log.e("evan", "activeCount="+mActiveNofiCount);
            } else {
                MessageBean message = intent.getParcelableExtra(NotificationsService.EXTRA_MESSAGE);
                Log.e("evan", "message onReceive=" + message.toString());

                Messenger messenger = Messenger.getSourceMessenger(message.app);
                switch (messenger) {
                case SMS:
                    if (!mLocalStorage.restoreFunctionConfig().getMessageRemainder()) break;
                case MOBILEQQ:
                    if (!mLocalStorage.restoreFunctionConfig().getMessageRemainderQQ()) break;
                case WECHAT:
                    if (!mLocalStorage.restoreFunctionConfig().getMessageRemainderWechat()) break;
                case FACEBOOK:
                case SYKPE:
                    if (action.equals(CentralService.ACTION_ACTIONS_NOTIFICATION_POSTED)) {
                        mNotifyPosts.add(message);
                        mNotifyMap.put(CONNECT_LE_REQUEST_CODE_MISSED_MESSAGE, mNotifyPosts);
                    } else if (action.equals(ACTION_ACTIONS_NOTIFICATION_REMOVED)) {
                        mNotifyRemoves.add(message);
                    }

                    connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_MISSED_MESSAGE);
                    break;
//                case SAMSUNGNOTE3PHONE:
//                case NEXUS:
//                case ANDROIDPHONE:
//                    if (action.equals(CentralService.ACTION_ACTIONS_NOTIFICATION_POSTED)) {
//                        mNotifyPosts.add(message);
//                        mNotifyMap.put(CONNECT_LE_REQUEST_CODE_CALLING_DISPLAY, mNotifyRemoves);
//                        String incomeTxt = message.message;
//                        try {
//                            Integer.parseInt(incomeTxt);
//                            Log.i("evan", "incomeNumber="+incomeTxt);
//                            mCallingName="";
//                            mCallingNumber=incomeTxt;
//                        } catch (NumberFormatException e) {
//                            mCallingName=incomeTxt;
//                            mCallingNumber="";
//                            Log.i("evan", "incomeName="+incomeTxt);
//                        }
//                    } else if (action.equals(ACTION_ACTIONS_NOTIFICATION_REMOVED)) {
//                        // mNotifyRemoves.add(message);// Todo: 未接来电
//                    }
//
//                    connectLeDeviceWithCommand(CONNECT_LE_REQUEST_CODE_CALLING_DISPLAY);
//                    break;

                default:
                    break;
                }
            }

        }
    };
	
    /***
     * 
     * 
     * 
     */
	
	private static IntentFilter makeDfuUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DfuService.BROADCAST_PROGRESS);
		intentFilter.addAction(DfuService.BROADCAST_ERROR);
		intentFilter.addAction(DfuService.BROADCAST_LOG);
		return intentFilter;
	}
	
	private final BroadcastReceiver mDfuUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction(); // DFU is in progress or an error occurred 
			//Log.e(TAG, "mDfuUpdateReceiver = " + action);
			if (DfuService.BROADCAST_PROGRESS.equals(action)) {
				final int progress = intent.getIntExtra(DfuService.EXTRA_DATA, 0);
				final int totalParts = intent.getIntExtra(DfuService.EXTRA_PARTS_TOTAL, 1);
				final int currentPart = intent.getIntExtra(DfuService.EXTRA_PART_CURRENT, 1);
				updateProgressBar(progress, currentPart, totalParts, false);
				//Log.e(TAG, "{PRO}: progress = " + progress + " ,currentPart = " + currentPart + " ,totalParts = " + totalParts);
			} 
			else if( DfuService.BROADCAST_LOG.equals(action)) {
				final String message = intent.getStringExtra(DfuService.EXTRA_LOG_MESSAGE);
				final int level = intent.getIntExtra(DfuService.EXTRA_LOG_LEVEL, 0);
				Log.d(TAG, "{LOG}: message = " + message + " ,level = " + level);
			}
			else if (DfuService.BROADCAST_ERROR.equals(action)) {
				Log.i("BroadcastReceiver", "BROADCAST_ERROR");
				final int error = intent.getIntExtra(DfuService.EXTRA_DATA, 0);
				updateProgressBar(error, 0, 0, true);
				/**
				if(updateagain){
					updateagain = false;
					onUploadClicked(address);
				}else{
					Message msg = new Message();
					msg.what = UPDATE_FAILED;
					myHandler.sendMessage(msg);
				}
				**/
				// We have to wait a bit before canceling notification. This is called before DfuService creates the last notification.
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						// if this activity is still open and upload process was completed, cancel the notification
						final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						manager.cancel(DfuService.NOTIFICATION_ID);
					}
				}, 200);
			}

		}
	};
    
    
    
    /*
     * 
     * 
     * 
     * 
     * 
     */
    private static String
    getDeviceNameFromRecord( byte[] record ) {
    	String result = null;
    	for( int offset = 0, byteCount = 0; offset < record.length; ) {
    		byteCount = record[offset]+1;
    		if( byteCount <= 1 ) break;
    		if( record[offset+1] == RECORD_TYPE_NAME ) {
        		byte[] temp = new byte[byteCount-2];
        		for( int i = 0; i < byteCount-2; i++ ) {
        			temp[i] = record[offset+2+i];
        		}
        		result = new String(temp);
        		break;
    		}
    		offset += byteCount;
    	}
		return result;
    }
    
    private static String
    getSerialNumberFromRecord( byte[] record ) {
    	String result = null;
    	for( int offset = 0, byteCount = 0; offset < record.length; ) {
    		byteCount = record[offset]+1;
    		if( byteCount <= 1 ) break;
    		
    		//Log.i( TAG, "getSerialNumberFromRecord --- " + record[offset+1] + " byteCount = " + byteCount );
    		if( (record[offset+1] == RECORD_TYPE_SUPPORTED_128SERVICE)&&(byteCount == RECORD_TYPE_SUPPORTED_128SERVICE_LEN) ) {
        		byte[] temp = new byte[8];
        		for( int i = 0; i < 8; i++ ) {
        			temp[8-1-i] = record[offset+2+i];
        		}
        		result = Utils.bytesToHexString(temp);
        		//Log.i( TAG, "getSerialNumberFromRecord --- " + result);
        		break;
    		}
    		offset += byteCount;
    	}
		return result;
    }
    
    private static String
    getHardwareVersionFromRecord( byte[] record ) {
    	String result = null;
    	for( int offset = 0, byteCount = 0; offset < record.length; ) {
    		byteCount = record[offset]+1;
    		if( byteCount <= 1 ) break;
    		
    		//Log.i( TAG, "getSerialNumberFromRecord --- " + record[offset+1] + " byteCount = " + byteCount );
    		if( (record[offset+1] == RECORD_TYPE_SUPPORTED_128SERVICE)&&(byteCount == RECORD_TYPE_SUPPORTED_128SERVICE_LEN) ) {
        		byte[] temp = new byte[2];
        		for( int i = 0; i < 2; i++ ) {
        			temp[2-1-i] = record[offset+2+8+2+i];
        		}
        		result = Utils.bytesToHexString(temp);
        		//Log.i( TAG, "getHardwareVersionFromRecord --- " + result);
        		break;
    		}
    		offset += byteCount;
    	}
		return result;
    }
    
    private static String
    getFirmwareVersionFromRecord( byte[] record ) {
    	String result = null;
    	for( int offset = 0, byteCount = 0; offset < record.length; ) {
    		byteCount = record[offset]+1;
    		if( byteCount <= 1 ) break;
    		
    		//Log.i( TAG, "getSerialNumberFromRecord --- " + record[offset+1] + " byteCount = " + byteCount );
    		if( (record[offset+1] == RECORD_TYPE_SUPPORTED_128SERVICE)&&(byteCount == RECORD_TYPE_SUPPORTED_128SERVICE_LEN) ) {
        		byte[] temp = new byte[2];
        		for( int i = 0; i < 2; i++ ) {
        			temp[2-1-i] = record[offset+2+8+i];
        		}
        		result = Utils.bytesToHexString(temp);
        		//Log.i( TAG, "getFirmwareVersionFromRecord --- " + result);
        		break;
    		}
    		offset += byteCount;
    	}
		return result;
    }
    
    private static String
    getProductTypeFromRecord( byte[] record ) {
    	String result = null;
    	for( int offset = 0, byteCount = 0; offset < record.length; ) {
    		byteCount = record[offset]+1;
    		if( byteCount <= 1 ) break;
    		
    		//Log.i( TAG, "getSerialNumberFromRecord --- " + record[offset+1] + " byteCount = " + byteCount );
    		if( (record[offset+1] == RECORD_TYPE_SUPPORTED_128SERVICE)&&(byteCount == RECORD_TYPE_SUPPORTED_128SERVICE_LEN) ) {
        		byte[] temp = new byte[2];
        		for( int i = 0; i < 2; i++ ) {
        			temp[2-1-i] = record[offset+2+8+2+2+i];
        		}
        		result = Utils.bytesToHexString(temp);
        		//Log.i( TAG, "getFirmwareVersionFromRecord --- " + result);
        		break;
    		}
    		offset += byteCount;
    	}
		return result;
    }
}
