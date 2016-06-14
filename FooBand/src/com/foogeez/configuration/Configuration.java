package com.foogeez.configuration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.util.Log;

public class Configuration {

	private List<AlarmConfiguration> mAlarmConfigurations = null;
	private List<CalendarConfiguration> mCalendarConfigurations = null;
	
	private UsrInfoConfiguration mUsrInfoConfiguration = null;
	private SedentaryConfiguration mSedentaryConfiguration = null;
	private UsrTargetConfiguration mUsrTargetConfiguration = null;
	private FunctionConfiguration mFunctionConfiguration = null;
	private DisturbModeConfiguration mDisturbModeConfiguration = null;
	
	
	public Configuration() {
		mAlarmConfigurations = new ArrayList<AlarmConfiguration>();
		mAlarmConfigurations.add(new AlarmConfiguration());
		mAlarmConfigurations.add(new AlarmConfiguration());
		mAlarmConfigurations.add(new AlarmConfiguration());
		mAlarmConfigurations.add(new AlarmConfiguration());
		mAlarmConfigurations.add(new AlarmConfiguration());
		mCalendarConfigurations = new ArrayList<CalendarConfiguration>();
		mCalendarConfigurations.add(new CalendarConfiguration());
		mCalendarConfigurations.add(new CalendarConfiguration());
		mCalendarConfigurations.add(new CalendarConfiguration());
		mCalendarConfigurations.add(new CalendarConfiguration());
		mCalendarConfigurations.add(new CalendarConfiguration());
		
		mUsrInfoConfiguration = new UsrInfoConfiguration();
		mFunctionConfiguration = new FunctionConfiguration();
		mSedentaryConfiguration = new SedentaryConfiguration();
		mUsrTargetConfiguration = new UsrTargetConfiguration();
		mDisturbModeConfiguration = new DisturbModeConfiguration();
	}

	
	public AlarmConfiguration getAlarmConfig(int alarmIndex) {
		return mAlarmConfigurations.get(alarmIndex);
	}
	
	public UsrInfoConfiguration getUsrInfoConfig() {
		return mUsrInfoConfiguration;
	}
	
	public CalendarConfiguration getCalendarConfig(int calendarIndex) {
		return mCalendarConfigurations.get(calendarIndex);
	}
	
	public SedentaryConfiguration getSedentaryConfig() {
		return mSedentaryConfiguration;
	}
	
	public UsrTargetConfiguration getUsrTargetConfig() {
		return mUsrTargetConfiguration;
	}
	
	public FunctionConfiguration getFunctionConfig() {
		return mFunctionConfiguration;
	}
	
	public DisturbModeConfiguration getDisturbModeConfig() {
		return mDisturbModeConfiguration;
	}

	
	/****
	 * 
	 * 
	 * @author Gordon
	 *
	 */
	public class FunctionConfiguration {
		private boolean CallingRemainder = true;
		private boolean MessageRemainder = true;
		private boolean MessageRemainderQQ = true;
		private boolean MessageRemainderWechat = true;
		private boolean StopWatchEnable = false;
		private boolean DaylightSavingTime = false;
		private boolean DisplayFormat24	= true;
		private boolean DisplayDateWeek = false;
		private boolean DisplayDistance = false;
		private boolean DisplayCaloric	= false;
		private boolean DisplaySleepTime = false;
		
		public boolean getCallingRemainder() { return CallingRemainder; }
		public boolean getMessageRemainder() { return MessageRemainder; }
		public boolean getMessageRemainderQQ() { return MessageRemainderQQ; }
		public boolean getMessageRemainderWechat() { return MessageRemainderWechat; }
		public boolean getStopWatchEnable() { return StopWatchEnable; }
		public boolean getDaylightSavingTime() { return DaylightSavingTime; }
		public boolean getDisplayFormat24() { return DisplayFormat24; }
		public boolean getDisplayDateWeek() { return DisplayDateWeek; }
		public boolean getDisplayDistance() { return DisplayDistance; } 
		public boolean getDisplayCaloric() { return DisplayCaloric; }
		public boolean getDisplaySleepTime() { return DisplaySleepTime; }
		
		public void setCallingRemainder( boolean flag ) { CallingRemainder = flag; }
		public void setMessageRemainder( boolean flag ) { MessageRemainder = flag; }
		public void setMessageRemainderQQ( boolean flag ) { MessageRemainderQQ = flag; }
		public void setMessageRemainderWechat( boolean flag ) { MessageRemainderWechat = flag; }
		public void setStopWatchEnable( boolean flag ) { StopWatchEnable = flag; }
		public void setDaylightSavingTime( boolean flag ) { DaylightSavingTime = flag; }
		public void setDisplayFormat24( boolean flag ) { DisplayFormat24 = flag; }
		public void setDisplayDateWeek( boolean flag ) { DisplayDateWeek = flag; }
		public void setDisplayDistance( boolean flag ) { DisplayDistance = flag; } 
		public void setDisplayCaloric( boolean flag ) { DisplayCaloric = flag; }
		public void setDisplaySleepTime( boolean flag ) { DisplaySleepTime = flag; }
	}
	
	public class DisturbModeConfiguration {
		private boolean ActiveFlag = false;
		private int StartTime = 22;	// in hours
		private int StopTime = 8;	// in hours
		
		public boolean getActivated() { return ActiveFlag; }
		public int getStart() { return StartTime; }
		public int getStop() { return StopTime; }
		
		public void setActivated( boolean flag ) { ActiveFlag = flag; }
		public void setStart( int value ) { StartTime = value; }
		public void setStop( int value ) { StopTime = value; }
		
		public byte[] getEncode() {
			byte[] result = new byte[4];
			result[0] = (byte) (ActiveFlag?0x01:0x00);
			result[1] = 0x00;
			result[2] = (byte) StartTime;
			result[3] = (byte) StopTime;
			return result;
		}
	}
	
	public class UsrInfoConfiguration {
		
		private int UsrInfoHeight = 170;
		private int UsrInfoWeight = 50;
		private int UsrInfoSexy = 0;
		private String UsrInfoId = "N/A";
		private String UsrInfoNickName = "N/A";
		
		private int UsrInfoBirthday = 0;
		private int UsrInfoBirthdayYear = 0;
		private int UsrInfoBirthdayMonth = 0;
		private int UsrInfoBirthdayDay = 0;
		
		public byte[] getEncode() {
			byte[] data = new byte[16];
			data[0] = (byte)(UsrInfoBirthdayYear);
			data[1] = (byte)(UsrInfoBirthdayYear>>8);
			data[2] = (byte)(UsrInfoBirthdayMonth);
			data[3] = (byte)(UsrInfoBirthdayDay);
			data[4] = (byte)(UsrInfoHeight);
			data[5] = (byte)(UsrInfoHeight>>8);
			data[6] = (byte)(UsrInfoWeight);
			data[7] = (byte)(UsrInfoWeight>>8);
			data[8] = (byte)(UsrInfoSexy);
			return data;
		}
		
		public int getHeight() { return UsrInfoHeight; }
		public int getWeight() { return UsrInfoWeight; }
		public int getSexy() { return UsrInfoSexy; }
		public int getBirthday() { return UsrInfoBirthday; }
		public String getId() { return UsrInfoId; }
		public String getNickName() { return UsrInfoNickName; }
		
		public void setId(String id) { UsrInfoId = id; };
		public void setNickName(String nickname) { UsrInfoNickName = nickname; };
		public void setHeight( int value ) { UsrInfoHeight = value; }
		public void setWeight( int value ) { UsrInfoWeight = value; }
		public void setSexy( int value ) { UsrInfoSexy = value; }
		public void setBirthday( int value ) { UsrInfoBirthday = value; }
		public void setBirthday( int year, int month, int day ) {
			UsrInfoBirthdayYear = year;
			UsrInfoBirthdayMonth = month;
			UsrInfoBirthdayDay = day;
			Calendar calendar = Calendar.getInstance(); 
			calendar.clear(); 
			calendar.set(Calendar.YEAR, UsrInfoBirthdayYear); 			//year��
			calendar.set(Calendar.MONTH, UsrInfoBirthdayMonth-1);		//Calendar����Ĭ��һ��Ϊ0,month��      
			calendar.set(Calendar.DAY_OF_MONTH, UsrInfoBirthdayDay);
			UsrInfoBirthday = (int)(calendar.getTimeInMillis()/1000);
			Log.e("DEBUG_", "year: " + UsrInfoBirthdayYear);
			Log.e("DEBUG_", "mont: " + UsrInfoBirthdayMonth);
			Log.e("DEBUG_", " day: " + UsrInfoBirthdayDay);
			Log.e("DEBUG_", "UsrInfoBirthday: " + Integer.toHexString(UsrInfoBirthday));
		}
	}
	
	public class UsrTargetConfiguration {
		private int UserTargetSteps = 10000;
		private int UserTargetSleepHours = 8;
		
		public byte[] getEncode() {
			byte[] data = new byte[6];
			data[0] = (byte)(UserTargetSteps);
			data[1] = (byte)(UserTargetSteps>>8);
			data[2] = (byte)(UserTargetSleepHours);
//			data[3] = (byte)(UserTargetSleepHours>>8);
			return data;
		}
		
		public int getSteps() { return UserTargetSteps; }
		public int getHours() { return UserTargetSleepHours; }
		
		public void setSteps(int value) { UserTargetSteps = value; }
		public void setHours(int value) { UserTargetSleepHours = value; }
		
	}
	
	public class SedentaryConfiguration {
		private boolean ActiveFlag = false; 
		private int Hour = 1;
		private int Minute = 0;
		
		public void setActived(boolean flag) { ActiveFlag = flag; }
		public void setHour(int hour) { Hour = hour; }
		public void setMinute(int minute) { Minute = minute; }
		
		public boolean isActived() { return ActiveFlag; }
		public int getHour() { return Hour; }
		public int getMinute() { return Minute; }
		
		public int getEncode() {
			return Hour*60 + Minute;
		}
	}
	
	public class CalendarConfiguration {
		private int CalendarDateTime = 0;
		private String CalendarTitle = "N/A";
		private boolean CalendarActiveFlag = false;
		
		public byte[] getEncode() {
			byte[] data = new byte[16];
			
			Calendar calendar = Calendar.getInstance();
			calendar.clear();
			calendar.setTimeInMillis((long)CalendarDateTime*1000);
			
			data[0] = (byte) (CalendarActiveFlag?0x01:0x00);
			data[1] = (byte) calendar.get(Calendar.YEAR);
			data[2] = (byte) (calendar.get(Calendar.YEAR)>>8);
			data[3] = (byte) (calendar.get(Calendar.MONTH)+1);
			data[4] = (byte) (calendar.get(Calendar.DAY_OF_MONTH));
			data[5] = (byte) (calendar.get(Calendar.HOUR_OF_DAY));
			data[6] = (byte) (calendar.get(Calendar.MINUTE));
			
			return data;
		}
		
		public boolean isActived() {
			return CalendarActiveFlag;
		}
		
		public String getTitle() {
			return CalendarTitle;
		}
		
		public int getDateTime() {
			return CalendarDateTime;
		}
		
		public void setActived( boolean flag ) {
			CalendarActiveFlag = flag;
		}
		
		public void setTitle( String title ) {
			CalendarTitle = title;
		}
		
		public void setDateTime( int utc ) {
			CalendarDateTime = utc;
		}
		
	}
	
	public class AlarmConfiguration {
		
		private boolean mActiveFlag = false;
		private boolean mCyclicFlag = false;
		
		private String mTitle = "N/A";
		
		private int mHour = 0;
		private int mMinute = 0;
		
		private boolean[] mWeekActiveFlags = { false, false, false, false, false, false, false };
		
		private byte getWeekEncode() {
			int result = 0;
			for( int i = 0; i < 7; i++ ) {
				result |= mWeekActiveFlags[i]?(1<<i):0;
			}
			return (byte)result;
		}
		
		public byte[] getEncode() {
			byte[] data = new byte[4];
			data[0] = (byte)(mActiveFlag?0x01:0x00);
			data[1] = (byte)(mHour);
			data[2] = (byte)(mMinute);
			data[3] = (byte)((mCyclicFlag?0x80:0x00)|getWeekEncode());
			return data;
		}
		
		public String getTitle() {
			return mTitle;
		}
		public void setTitle( String title ) {
			mTitle = title;
		}
		public boolean isActived() {
	    	return mActiveFlag;
	    }
	    public boolean isCycliced() {
	    	return mCyclicFlag;
	    }
	    public boolean isWeek0Checked() {
	    	return mWeekActiveFlags[0];
	    }
	    public boolean isWeek1Checked() {
	    	return mWeekActiveFlags[1];
	    }
	    public boolean isWeek2Checked() {
	    	return mWeekActiveFlags[2];
	    }
	    public boolean isWeek3Checked() {
	    	return mWeekActiveFlags[3];
	    }
	    public boolean isWeek4Checked() {
	    	return mWeekActiveFlags[4];
	    }
	    public boolean isWeek5Checked() {
	    	return mWeekActiveFlags[5];
	    }
	    public boolean isWeek6Checked() {
	    	return mWeekActiveFlags[6];
	    }
	    public int getHour() {
	    	return mHour;
	    }
	    public int getMinute() {
	    	return mMinute;
	    }
	    
	    public void setHour( int hour) {
	    	mHour = hour;
	    }
	    public void setMinute( int minute ) {
	    	mMinute = minute;
	    }
	    
	    public void setActived( boolean flag ) {
	    	mActiveFlag = flag;
	    }
	    
	    public void setCycliced( boolean flag ) {
	    	mCyclicFlag = flag;
	    }

	    public void setWeekChecked( int weekId, boolean checked ) {
	    	mWeekActiveFlags[weekId] = checked;
	    }
	    
	    public boolean getWeekChecked( int weekId ) {
	    	return mWeekActiveFlags[weekId];
	    }
	    
	    public boolean isAnyDayChecked() {
	    	return isWeek0Checked()||isWeek1Checked()||isWeek2Checked()||isWeek3Checked()||isWeek4Checked()||isWeek5Checked()||isWeek6Checked();
	    }
	}


}
