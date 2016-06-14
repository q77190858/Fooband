package com.foogeez.configuration;

import com.foogeez.configuration.Configuration.AlarmConfiguration;
import com.foogeez.configuration.Configuration.CalendarConfiguration;
import com.foogeez.configuration.Configuration.DisturbModeConfiguration;
import com.foogeez.configuration.Configuration.FunctionConfiguration;
import com.foogeez.configuration.Configuration.SedentaryConfiguration;
import com.foogeez.configuration.Configuration.UsrInfoConfiguration;
import com.foogeez.configuration.Configuration.UsrTargetConfiguration;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalStorage {
	
	public final static String LOCAL_STORAGE_FILE_NAME = "com.foogeez.fooband.LOCAL_STORAGE_FILE_NAME";
	
	public final static String USR_ACCOUNT 		= "USR_ACCOUNT";
	public final static String USR_PASSWRD 		= "USR_PASSWRD";
	public final static String USR_UID	   		= "USR_UID";
	public final static String USR_SID	   		= "USR_SID";
	public final static String USR_DID			= "USR_DID";
	public final static String UPLOAD_UTC		= "UPLOAD_UTC";
	
	public final static String DEVICE_ACTIVATED = "DEVICE_ACTIVATED";
	public final static String DEVICE_NAME		= "DEVICE_NAME";
	public final static String DEVICE_ADDR		= "DEVICE_ADDR";
	public final static String DEVICE_HWVR		= "DEVICE_HWVR";
	public final static String DEVICE_FWVR		= "DEVICE_FWVR";
	public final static String DEVICE_SRLN		= "DEVICE_SRLN";
	
	public final static String USE_FOOGEEZ_DEVICE_FIRST		= "USE_FOOGEEZ_DEVICE_FIRST";
	public final static String USE_FOOGEEZ_APP_FIRST 		= "USE_FOOGEEZ_APP_FIRST";
	public final static String APP_UPDATE_LAST_CHECK		= "APP_UPDATE_LAST_CHECK";
	public final static String ROM_UPDATE_LAST_CHECK		= "ROM_UPDATE_LAST_CHECK";
	
	public final static String CONFIG_ALARM 	= "CONFIG_ALARM";
	public final static String ALARM_ACTIVE 	= "ALARM_ACTIVE";
	public final static String ALARM_CYCLIC 	= "ALARM_CYCLIC";
	public final static String ALARM_HOURS 		= "ALARM_HOURS";
	public final static String ALARM_MINUTES 	= "ALARM_MINUTES";
	public final static String ALARM_WEEK0 		= "ALARM_WEEK0";
	public final static String ALARM_WEEK1 		= "ALARM_WEEK1";
	public final static String ALARM_WEEK2 		= "ALARM_WEEK2";
	public final static String ALARM_WEEK3		= "ALARM_WEEK3";
	public final static String ALARM_WEEK4		= "ALARM_WEEK4";
	public final static String ALARM_WEEK5		= "ALARM_WEEK5";
	public final static String ALARM_WEEK6		= "ALARM_WEEK6";
	
	public final static String CONFIG_CALENDAR 		= "CONFIG_CALENDAR";
	public final static String CALENDAR_TITLE		= "CALENDAR_TITLE";
	public final static String CALENDAR_ACTIVE		= "CALENDAR_ACTIVE";
	public final static String CALENDAR_DATETIME	= "CALENDAR_DATETIME";
	
	public final static String CONFIG_SEDENTARY 	= "CONFIG_SEDENTARY";
	public final static String SEDENTARY_TITLE		= "SEDENTARY_TITLE";
	public final static String SEDENTARY_ACTIVE		= "SEDENTARY_ACTIVE";
	public final static String SEDENTARY_HOUR		= "SEDENTARY_HOUR";
	public final static String SEDENTARY_MINUTE		= "SEDENTARY_MINUTE";
	
	public final static String CONFIG_USR_INFO 		= "CONFIG_USR_INFO";
	public final static String USR_INFO_HEIGHT 		= "USR_INFO_HEIGHT";
	public final static String USR_INFO_WEIGHT 		= "USR_INFO_WEIGHT";
	public final static String USR_INFO_SEXY   		= "USR_INFO_SEXY";
	public final static String USR_INFO_BIRTHDAY	= "USR_INFO_BIRTHDAY";
	public final static String USR_INFO_ID			= "USR_INFO_ID";
	public final static String USR_INFO_NICKNAME	= "USR_INFO_NICKNAME";
	
	public final static String CONFIG_USR_TARGET	= "CONFIG_USR_TARGET";
	public final static String USR_TARGET_SPORT		= "USR_TARGET_SPORT";
	public final static String USR_TARGET_SLEEP 	= "USR_TARGET_SLEEP";
	
	public final static String CONFIG_DISTURB_MODE	= "CONFIG_DISTURB_MODE";
	public final static String DISTURB_MODE_ACTIVED = "DISTURB_MODE_ACTIVED";
	public final static String DISTURB_MODE_START	= "DISTURB_MODE_START";
	public final static String DISTURB_MODE_STOP	= "DISTURB_MODE_STOP";
	
	public final static String CONFIG_FUNCTION		= "CONFIG_FUNCTION";
	public final static String FUNCTION_CALLING		= "FUNCTION_CALLING";
	public final static String FUNCTION_MESSAGE		= "FUNCTION_MESSAGE";
	public final static String FUNCTION_MESSAGEQQ   = "FUNCTION_MESSAGEQQ";
	public final static String FUNCTION_MESSAGEWECHAT = "FUNCTION_MESSAGEWECHAT";
	public final static String FUNCTION_STOPWATCH	= "FUNCTION_STOPWATCH";
	public final static String FUNCTION_DAYLIGHT_SAVING_TIME = "FUNCTION_DAYLIGHT_SAVING_TIME";
	public final static String FUNCTION_FORMAT24	= "FUNCTION_FORMAT24";
	public final static String FUNCTION_DATEWEEK	= "FUNCTION_DATEWEEK";
	public final static String FUNCTION_DISTANCE	= "FUNCTION_DISTANCE";
	public final static String FUNCTION_CALORIC		= "FUNCTION_CALORIC";
	public final static String FUNCTION_SLEEP_TIME	= "FUNCTION_SLEEP_TIME";
	
	public final static String DATUM_LAST_SYNC_TIME = "DATUM_LAST_SYNC_TIME";
	
	Context mContext;
	Configuration mConfiguration;
	
	public LocalStorage( Context context ) {
		mContext = context;
		mConfiguration = new Configuration();
	}
	
	public boolean hasAnyAccount() {
		if( !getAccount().equals("") ) return true;
        return false;
	}
	
	public boolean hasPasswordByAccount() {
		if( !getPassword().equals("") ) return true;
        return false;
	}
	
	public void exitCurrentUserLogin() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().remove(USR_PASSWRD).commit();
	}
	
	public String getAccount() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(USR_ACCOUNT, "");
	}
	
	public String getPassword() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(USR_PASSWRD, "");
	}
	
	public String getUID() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(USR_UID, "");
	}
	
	public String getSID() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(USR_SID, "");
	}
	
	public Long getDID() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getLong(USR_DID, 0);
	}
	
	public Long getUPLOADUTC() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getLong(UPLOAD_UTC, 0);
	}
	
	public String getLastSyncTime() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(DATUM_LAST_SYNC_TIME, "?");
	}
	
	public void saveLastSyncTime(String time) {
		SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
		sp.edit().putString(DATUM_LAST_SYNC_TIME, time).commit();
	}
	
	public void saveAccount(String account) {
		SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
		sp.edit().putString(USR_ACCOUNT, account).commit();
	}
	
	public void savePassword(String password) {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(USR_PASSWRD, password).commit();
	}
	
	public void saveUID(String uid) {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(USR_UID, uid).commit();
	}
	
	public void saveSID(String sid) {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(USR_SID, sid).commit();
	}
	
	public void saveDID(long did) {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().putLong(USR_DID, did).commit();
	}
	
	public void saveUPLOADUTC(long utc) {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().putLong(UPLOAD_UTC, utc).commit();
	}
	
	public boolean isDeviceActivated() {
        if( !getActivatedDeviceSrln().equals("N/A") ) return true;
        return false;
	}
	
	public boolean getFstRefreshDatumFlag() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(getAccount()+"?"+getActivatedDeviceSrln(), true);
	}
	
	public void setFstRefreshDatumFlag(boolean flag) {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(getAccount()+"?"+getActivatedDeviceSrln(), flag).commit();
	}
	
	public boolean getFstUseApp() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(USE_FOOGEEZ_APP_FIRST, true);
	}
	
	public void setFstUseApp(boolean flag) {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(USE_FOOGEEZ_APP_FIRST, flag).commit();
	}
	
	public String getAppUpdateLastCheck() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(APP_UPDATE_LAST_CHECK, "1970-01-01");
	}
	
	public String getRomUpdateLastCheck() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(ROM_UPDATE_LAST_CHECK, "1970-01-01");
	}
	
	public void setAppUpdateLastCheck(String date) {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(APP_UPDATE_LAST_CHECK, date).commit();
	}
	
	public void setRomUpdateLastCheck(String date) {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(ROM_UPDATE_LAST_CHECK, date).commit();
	}

	public void clearDeviceActivated() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE); 
        sp.edit().remove(getAccount()+"?"+getActivatedDeviceSrln()).commit();
        sp.edit().remove(getAccount()+"?"+DEVICE_NAME).commit();
        sp.edit().remove(getAccount()+"?"+DEVICE_ADDR).commit();
        sp.edit().remove(getAccount()+"?"+DEVICE_HWVR).commit();
        sp.edit().remove(getAccount()+"?"+DEVICE_FWVR).commit();
        sp.edit().remove(getAccount()+"?"+DEVICE_SRLN).commit();
	}
	
	// catch device information
	public String getActivatedDeviceName() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE); 
        return sp.getString( getAccount()+"?"+DEVICE_NAME, "N/A");
	}
	
	public String getActivatedDeviceAddr() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE); 
        return sp.getString( getAccount()+"?"+DEVICE_ADDR, "N/A");
	}
	
	public String getActivatedDeviceHwvr() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE); 
        return sp.getString( getAccount()+"?"+DEVICE_HWVR, "N/A");
	}
	
	public String getActivatedDeviceFwvr() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE); 
        return sp.getString( getAccount()+"?"+DEVICE_FWVR, "N/A");
	}
	
	public String getActivatedDeviceSrln() {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE); 
        return sp.getString( getAccount()+"?"+DEVICE_SRLN, "N/A");
	}

	// save device information
	public void saveActivatedDeviceName(String name) {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE); 
        sp.edit().putString( getAccount()+"?"+DEVICE_NAME, name).commit();
	}
	
	public void saveActivatedDeviceAddr(String addr) {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE); 
        sp.edit().putString( getAccount()+"?"+DEVICE_ADDR, addr).commit();
	}
	
	public void saveActivatedDeviceHwvr(String hwvr) {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE); 
        sp.edit().putString( getAccount()+"?"+DEVICE_HWVR, hwvr).commit();
	}
	
	public void saveActivatedDeviceFwvr(String fwvr) {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE); 
        sp.edit().putString( getAccount()+"?"+DEVICE_FWVR, fwvr).commit();
	}
	
	public void saveActivatedDeviceSrln(String srln) {
        SharedPreferences sp = mContext.getSharedPreferences(LOCAL_STORAGE_FILE_NAME, Context.MODE_PRIVATE); 
        sp.edit().putString( getAccount()+"?"+DEVICE_SRLN, srln).commit();
	}
	

	/**
	 * @param indexAlarm
	 * @param config
	 */
	// Alarm
	public void saveAlarmConfig( int indexAlarm, AlarmConfiguration config ) {
		if( !hasAnyAccount() ) {
			return;
		}
		
		if( !isDeviceActivated() ) {
			return;
		}
		
        SharedPreferences sp = mContext.getSharedPreferences( getAccount()+"?"+getActivatedDeviceSrln()+"?"+CONFIG_ALARM+"="+indexAlarm, Context.MODE_PRIVATE); 
        sp.edit().clear().commit();
        sp.edit().putInt(ALARM_HOURS,	config.getHour()).commit();
        sp.edit().putInt(ALARM_MINUTES,	config.getMinute()).commit();
        sp.edit().putBoolean(ALARM_ACTIVE,	config.isActived()).commit(); 
        sp.edit().putBoolean(ALARM_CYCLIC,	config.isCycliced()).commit(); 
        sp.edit().putBoolean(ALARM_WEEK0,	config.isWeek0Checked()).commit(); 
        sp.edit().putBoolean(ALARM_WEEK1,	config.isWeek1Checked()).commit(); 
        sp.edit().putBoolean(ALARM_WEEK2,	config.isWeek2Checked()).commit(); 
        sp.edit().putBoolean(ALARM_WEEK3,	config.isWeek3Checked()).commit(); 
        sp.edit().putBoolean(ALARM_WEEK4,	config.isWeek4Checked()).commit(); 
        sp.edit().putBoolean(ALARM_WEEK5,	config.isWeek5Checked()).commit(); 
        sp.edit().putBoolean(ALARM_WEEK6,	config.isWeek6Checked()).commit(); 
    }
	
	public AlarmConfiguration restoreAlarmConfig( int indexAlarm ) {
		AlarmConfiguration config = mConfiguration.getAlarmConfig(indexAlarm);
		if( (!hasAnyAccount())||(!isDeviceActivated()) ) {
			config.setHour(0);
			config.setMinute(0);
			config.setActived(false);
			config.setCycliced(false);
			config.setWeekChecked(0, false);
			config.setWeekChecked(1, false);
			config.setWeekChecked(2, false);
			config.setWeekChecked(3, false);
			config.setWeekChecked(4, false);
			config.setWeekChecked(5, false);
			config.setWeekChecked(6, false);
		}
		else {
			SharedPreferences sp = mContext.getSharedPreferences( getAccount()+"?"+getActivatedDeviceSrln()+"?"+CONFIG_ALARM+"="+indexAlarm, Context.MODE_PRIVATE); 
			config.setHour(sp.getInt(ALARM_HOURS, 0));
			config.setMinute(sp.getInt(ALARM_MINUTES, 0));
			config.setActived(sp.getBoolean(ALARM_ACTIVE, false));
			config.setCycliced(sp.getBoolean(ALARM_CYCLIC, false));
			config.setWeekChecked(0, sp.getBoolean(ALARM_WEEK0, false));
			config.setWeekChecked(1, sp.getBoolean(ALARM_WEEK1, false));
			config.setWeekChecked(2, sp.getBoolean(ALARM_WEEK2, false));
			config.setWeekChecked(3, sp.getBoolean(ALARM_WEEK3, false));
			config.setWeekChecked(4, sp.getBoolean(ALARM_WEEK4, false));
			config.setWeekChecked(5, sp.getBoolean(ALARM_WEEK5, false));
			config.setWeekChecked(6, sp.getBoolean(ALARM_WEEK6, false));
		}
		return config;
	}
	
	// Calendar
	public void saveCalendarConfig(int indexCalendar, CalendarConfiguration config ) {
		if( !hasAnyAccount() ) {
			return;
		}
		if( !isDeviceActivated() ) {
			return;
		}
		
        SharedPreferences sp = mContext.getSharedPreferences( getAccount()+"?"+getActivatedDeviceSrln()+"?"+CONFIG_CALENDAR+"="+indexCalendar, Context.MODE_PRIVATE); 
        sp.edit().clear().commit();
        sp.edit().putBoolean(CALENDAR_ACTIVE, config.isActived()).commit();
        sp.edit().putString(CALENDAR_TITLE, config.getTitle()).commit();
        sp.edit().putInt(CALENDAR_DATETIME, config.getDateTime()).commit();
	}
	
	public CalendarConfiguration restoreCalendarConfig( int indexCalendar ) {
		CalendarConfiguration config = mConfiguration.getCalendarConfig(indexCalendar);
		if( (!hasAnyAccount())||(!isDeviceActivated()) ) {
			config.setActived(false);
			config.setTitle("N/A");
			config.setDateTime(0);
		}
		else {
			SharedPreferences sp = mContext.getSharedPreferences( getAccount()+"?"+getActivatedDeviceSrln()+"?"+CONFIG_CALENDAR+"="+indexCalendar, Context.MODE_PRIVATE); 
			config.setActived(sp.getBoolean(CALENDAR_ACTIVE, false));
			config.setTitle(sp.getString(CALENDAR_TITLE, "N/A"));
			config.setDateTime(sp.getInt(CALENDAR_DATETIME, 0));
		}
		return config;
	}

	// Sedentary
	public void saveSedentaryConfig(SedentaryConfiguration config ) {
		if( !hasAnyAccount() ) {
			return;
		}
		if( !isDeviceActivated() ) {
			return;
		}
		
        SharedPreferences sp = mContext.getSharedPreferences( getAccount()+"?"+getActivatedDeviceSrln()+"?"+CONFIG_SEDENTARY, Context.MODE_PRIVATE); 
        sp.edit().clear().commit();
        sp.edit().putBoolean(SEDENTARY_ACTIVE, config.isActived()).commit();
        sp.edit().putInt(SEDENTARY_HOUR, config.getHour()).commit();
        sp.edit().putInt(SEDENTARY_MINUTE, config.getMinute()).commit();
	}
	
	public SedentaryConfiguration restoreSedentaryConfig() {
		SedentaryConfiguration config = mConfiguration.getSedentaryConfig();
		if( (!hasAnyAccount())||(!isDeviceActivated()) ) {
	        config.setActived(false);
	        config.setHour(1);
	        config.setMinute(0);
		}
		else {
			SharedPreferences sp = mContext.getSharedPreferences( getAccount()+"?"+getActivatedDeviceSrln()+"?"+CONFIG_SEDENTARY, Context.MODE_PRIVATE); 
	        config.setActived(sp.getBoolean(SEDENTARY_ACTIVE, false));
	        config.setHour(sp.getInt(SEDENTARY_HOUR, 1));
	        config.setMinute(sp.getInt(SEDENTARY_MINUTE, 0));
		}
		return config;
	}
	
	// UsrInfo
	public void saveUsrInfoConfig(UsrInfoConfiguration config ) {
		if( !hasAnyAccount() ) {
			return;
		}
		
        SharedPreferences sp = mContext.getSharedPreferences( getAccount()+"?"+CONFIG_USR_INFO, Context.MODE_PRIVATE); 
        sp.edit().clear().commit();
        sp.edit().putInt(USR_INFO_SEXY, config.getSexy()).commit();
        sp.edit().putInt(USR_INFO_HEIGHT, config.getHeight()).commit();
        sp.edit().putInt(USR_INFO_WEIGHT, config.getWeight()).commit();
        sp.edit().putInt(USR_INFO_BIRTHDAY, config.getBirthday()).commit();
        sp.edit().putString(USR_INFO_ID, config.getId()).commit();
        sp.edit().putString(USR_INFO_NICKNAME, config.getNickName()).commit();
	}
	
	public UsrInfoConfiguration restoreUsrInfoConfig() {
		UsrInfoConfiguration config = mConfiguration.getUsrInfoConfig();
		if( !hasAnyAccount() ) {
			config.setHeight(170);
			config.setWeight(50);
			config.setSexy(0);
			config.setBirthday(0);
			config.setId("N/A");
			config.setNickName("N/A");
		}
		else {
			SharedPreferences sp = mContext.getSharedPreferences( getAccount()+"?"+CONFIG_USR_INFO, Context.MODE_PRIVATE); 
			config.setHeight(sp.getInt(USR_INFO_HEIGHT, 170));
			config.setWeight(sp.getInt(USR_INFO_WEIGHT, 50));
			config.setSexy(sp.getInt(USR_INFO_SEXY, 0));
			config.setBirthday(sp.getInt(USR_INFO_BIRTHDAY, 0));
			config.setId(sp.getString(USR_INFO_ID, "N/A"));
			config.setNickName(sp.getString(USR_INFO_NICKNAME, "N/A"));
		}
		return config;
	}
	
	// UsrTarget
	public void saveUsrTargetConfig(UsrTargetConfiguration config ) {
		if( !hasAnyAccount() ) {
			return;
		}
		
        SharedPreferences sp = mContext.getSharedPreferences( getAccount()+"?"+CONFIG_USR_TARGET, Context.MODE_PRIVATE); 
        sp.edit().clear().commit();
        
        /** Name:Robin  Time:20150921  Function:输入的步数 --睡眠时间*/
        sp.edit().putInt(USR_TARGET_SPORT, config.getSteps()).commit();
        sp.edit().putInt(USR_TARGET_SLEEP, config.getHours()).commit();
	}
	
	public UsrTargetConfiguration restoreUsrTargetConfig() {
		UsrTargetConfiguration config = mConfiguration.getUsrTargetConfig();
		if( !hasAnyAccount() ) {
			config.setSteps(10000);
			config.setHours(8);
		}
		else {
			SharedPreferences sp = mContext.getSharedPreferences( getAccount()+"?"+CONFIG_USR_TARGET, Context.MODE_PRIVATE); 
			config.setSteps(sp.getInt(USR_TARGET_SPORT, 10000));
			config.setHours(sp.getInt(USR_TARGET_SLEEP, 8));
//			config.setCurrentSportTargetId(sp.getInt(USR_TARGET_SPORT, 3));
//			config.setCurrentSleepTargetId(sp.getInt(USR_TARGET_SLEEP, 3));
		}
		return config;
	}
	
	// Disturb Mode
	public void saveDisturbModeConfig(DisturbModeConfiguration config ) {
		if( !hasAnyAccount() ) {
			return;
		}
		
        SharedPreferences sp = mContext.getSharedPreferences( getAccount()+"?"+CONFIG_DISTURB_MODE, Context.MODE_PRIVATE); 
        sp.edit().clear().commit();
        sp.edit().putBoolean(DISTURB_MODE_ACTIVED, config.getActivated()).commit();
        sp.edit().putInt(DISTURB_MODE_START, config.getStart()).commit();
        sp.edit().putInt(DISTURB_MODE_STOP, config.getStop()).commit();
	}
	
	public DisturbModeConfiguration restoreDisturbModeConfig() {
		DisturbModeConfiguration config = mConfiguration.getDisturbModeConfig();
		if( !hasAnyAccount() ) {
			config.setActivated(false);
			config.setStart(22);
			config.setStop(8);
		}
		else {
			SharedPreferences sp = mContext.getSharedPreferences( getAccount()+"?"+CONFIG_DISTURB_MODE, Context.MODE_PRIVATE); 
			config.setActivated(sp.getBoolean(DISTURB_MODE_ACTIVED, false));
			config.setStart(sp.getInt(DISTURB_MODE_START, 22));
			config.setStop(sp.getInt(DISTURB_MODE_STOP, 8));
		}
		return config;
	}
	
	// Functions
	public void saveFunctionConfig(FunctionConfiguration config ) {
		if( !hasAnyAccount() ) {
			return;
		}
		if( !isDeviceActivated() ) {
			return;
		}
		
		SharedPreferences sp = mContext.getSharedPreferences( getAccount()+"?"+getActivatedDeviceSrln()+"?"+CONFIG_FUNCTION, Context.MODE_PRIVATE); 
        sp.edit().clear().commit();
        sp.edit().putBoolean(FUNCTION_CALLING, config.getCallingRemainder()).commit();
        sp.edit().putBoolean(FUNCTION_MESSAGE, config.getMessageRemainder()).commit();
        sp.edit().putBoolean(FUNCTION_MESSAGEQQ, config.getMessageRemainderQQ()).commit();
        sp.edit().putBoolean(FUNCTION_MESSAGEWECHAT, config.getMessageRemainderWechat()).commit();
        sp.edit().putBoolean(FUNCTION_STOPWATCH, config.getStopWatchEnable()).commit();
        sp.edit().putBoolean(FUNCTION_DAYLIGHT_SAVING_TIME, config.getDaylightSavingTime()).commit();
        sp.edit().putBoolean(FUNCTION_FORMAT24, config.getDisplayFormat24()).commit();
        sp.edit().putBoolean(FUNCTION_DATEWEEK, config.getDisplayDateWeek()).commit();
        sp.edit().putBoolean(FUNCTION_DISTANCE, config.getDisplayDistance()).commit();
        sp.edit().putBoolean(FUNCTION_CALORIC, config.getDisplayCaloric()).commit();
        sp.edit().putBoolean(FUNCTION_SLEEP_TIME, config.getDisplaySleepTime()).commit();
	}
	
	public FunctionConfiguration restoreFunctionConfig() {
		FunctionConfiguration config = mConfiguration.getFunctionConfig();
		if( (!hasAnyAccount())||(!isDeviceActivated()) ) {
			config.setCallingRemainder(true);
			config.setMessageRemainder(true);
			config.setStopWatchEnable(false);
			config.setDaylightSavingTime(false);
			config.setDisplayFormat24(true);
			config.setDisplayDateWeek(false);
			config.setDisplayDistance(false);
			config.setDisplayCaloric(false);
			config.setDisplaySleepTime(false);
		}
		else {
			SharedPreferences sp = mContext.getSharedPreferences( getAccount()+"?"+getActivatedDeviceSrln()+"?"+CONFIG_FUNCTION, Context.MODE_PRIVATE); 
			config.setCallingRemainder(sp.getBoolean(FUNCTION_CALLING, true));
			config.setMessageRemainder(sp.getBoolean(FUNCTION_MESSAGE, true));
			config.setMessageRemainderQQ(sp.getBoolean(FUNCTION_MESSAGEQQ, true));
			config.setMessageRemainderWechat(sp.getBoolean(FUNCTION_MESSAGEWECHAT, true));
			config.setStopWatchEnable(sp.getBoolean(FUNCTION_STOPWATCH, false));
			config.setDaylightSavingTime(sp.getBoolean(FUNCTION_DAYLIGHT_SAVING_TIME, false));
			config.setDisplayFormat24(sp.getBoolean(FUNCTION_FORMAT24, true));
			config.setDisplayDateWeek(sp.getBoolean(FUNCTION_DATEWEEK, false));
			config.setDisplayDistance(sp.getBoolean(FUNCTION_DISTANCE, false));
			config.setDisplayCaloric(sp.getBoolean(FUNCTION_CALORIC, false));
			config.setDisplaySleepTime(sp.getBoolean(FUNCTION_SLEEP_TIME, false));
		}
		
		return config;
	}
		
}
