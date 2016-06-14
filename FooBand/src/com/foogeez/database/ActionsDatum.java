package com.foogeez.database;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.grdn.util.Utils;

import android.util.Log;

public class ActionsDatum {
	public static final String TAG = "ActionsDatum";
	
    public static final int DATUM_TYPE_SPORT	= 0;
    public static final int DATUM_TYPE_SLEEP	= 1;
    
    public static final int DATUM_SUB_TYPE_SPORT_IDLE = 1;
    public static final int DATUM_SUB_TYPE_SPORT_WALK = 2;
    public static final int DATUM_SUB_TYPE_SPORT_RUN = 3;
    //private byte mDatumType = DATUM_TYPE_SPORT;

	
	private int	m_utc;
	private int m_flag;
	private int m_type;

	private int	m_steps;
	private int	m_distance;			// precision 10m.
	private int	m_idle_caloric;		// precision 1 cal.
	private int	m_sport_caloric;		// precision 1 cal.
	private int	m_idle_time;			// precision 1 minute.
	private int	m_sport_time;			// precision 1 minute.
	
	private int m_status;
	private int m_awake_time;
	private int m_light_sleep_time;
	private int m_deep_sleep_time;
	private int m_awake_count;
	private int m_total_sleep_time;
	
	public ActionsDatum(byte[] recvdata) {
		Log.i(TAG,"Robin---- ActionsDatum(byte[] recvdata) ");
		m_utc = Utils.bytes2Int(recvdata, 0, 4, Utils.BIG_ENDIUM);
		m_type = Utils.bytes2Int(recvdata, 18, 1, Utils.BIG_ENDIUM);
		m_flag = Utils.bytes2Int(recvdata, 19, 1, Utils.BIG_ENDIUM);
		
		if( m_flag == DATUM_TYPE_SPORT ) {
			m_steps = Utils.bytes2Int(recvdata, 4, 4, Utils.BIG_ENDIUM);
			m_distance = Utils.bytes2Int(recvdata, 8, 2, Utils.BIG_ENDIUM);				// precision 10m.
			m_idle_caloric = Utils.bytes2Int(recvdata, 10, 2, Utils.BIG_ENDIUM);		// precision 1 cal.
			m_sport_caloric = Utils.bytes2Int(recvdata, 12, 2, Utils.BIG_ENDIUM);		// precision 1 cal.
			m_idle_time = Utils.bytes2Int(recvdata, 14, 2, Utils.BIG_ENDIUM);			// precision 1 minute.
			m_sport_time = Utils.bytes2Int(recvdata, 16, 2, Utils.BIG_ENDIUM);			// precision 1 minute.
		}
		else if( m_flag == DATUM_TYPE_SLEEP ) {
			m_status = Utils.bytes2Int(recvdata, 4, 1, Utils.BIG_ENDIUM);
			//private byte resv0;
			m_awake_time = Utils.bytes2Int(recvdata, 6, 2, Utils.BIG_ENDIUM);
			m_light_sleep_time = Utils.bytes2Int(recvdata, 8, 2, Utils.BIG_ENDIUM);
			m_deep_sleep_time = Utils.bytes2Int(recvdata, 10, 2, Utils.BIG_ENDIUM);
			m_awake_count = Utils.bytes2Int(recvdata, 12, 2, Utils.BIG_ENDIUM);
			m_total_sleep_time = Utils.bytes2Int(recvdata, 14, 2, Utils.BIG_ENDIUM);
			//private int resv1;
		}
		else {
			
		}
	}
	
	public ActionsDatum(int flag, int utc, int type, int p1, int p2, int p3, int p4, int p5, int p6) {
		Log.i(TAG,"Robin----ActionsDatum(int flag, int utc, int type, int p1, int p2,");
		m_flag = flag;
		m_utc = utc;
		m_type = type;
		if( m_flag == DATUM_TYPE_SPORT ) {
			m_steps = p1;
			m_distance = p2;			// precision 10m.
			m_idle_caloric = p3;		// precision 1 cal.
			m_sport_caloric = p4;		// precision 1 cal.
			m_idle_time = p5;			// precision 1 minute.
			m_sport_time = p6;			// precision 1 minute.
		}
		else if( m_flag == DATUM_TYPE_SLEEP ) {
			m_status = p1;
			//private byte resv0;
			m_awake_time = p2;
			m_light_sleep_time = p3;
			m_deep_sleep_time = p4;
			m_awake_count = p5;
			m_total_sleep_time = p6;
		}
	}
	
	public int getUTC() 				{ return m_utc; }
	public int getType() 				{ return (m_flag == 0)?DATUM_TYPE_SPORT:DATUM_TYPE_SLEEP; }
	public int getDetailType() 			{ return m_type; }
	
	public int getSportSteps() 			{ return m_steps; }
	public int getSportDistance() 		{ return m_distance; }
	public int getSportIdleCaloric() 	{ return m_idle_caloric; }
	public int getSportActiveCaloric()	{ return m_sport_caloric; }
	public int getSportIdleTime() 		{ return m_idle_time; }
	public int getSportActiveTime() 	{ return m_sport_time; }
	
	public int getSleepStatus() 		{ return m_status; }
	public int getSleepAwakeTime() 		{ return m_awake_time; }
	public int getSleepLightTime() 		{ return m_light_sleep_time; }
	public int getSleepDeepTime() 		{ return m_deep_sleep_time; }
	public int getSleepAwakeCount() 	{ return m_awake_count; }
	public int getSleepTotalTime() 		{ return m_total_sleep_time; }
	
	public String getDateTime(TimeZone tz) 	{ return utc2DateTimeString(m_utc, tz); }
	public void refreshUTC() {
		Calendar calendar = Calendar.getInstance();
		m_utc = (int)(calendar.getTimeInMillis()/1000);
	}
	
	public void displayDatum() {
		Log.i(TAG, " m_utc              = " + m_utc);
		Log.i(TAG, " Date               = " + utc2DateTimeString(m_utc, TimeZone.getTimeZone("GMT+8")));
		Log.i(TAG, " m_flag             = " + m_flag);
		Log.i(TAG, " m_type             = " + m_type);

		if( m_flag == DATUM_TYPE_SPORT ) {
			Log.i(TAG, " m_steps            = " + m_steps);
			Log.i(TAG, " m_distance         = " + m_distance);			// precision 10m.
			Log.i(TAG, " m_idle_caloric     = " + m_idle_caloric);		// precision 1 cal.
			Log.i(TAG, " m_sport_caloric    = " + m_sport_caloric);		// precision 1 cal.
			Log.i(TAG, " m_idle_time        = " + m_idle_time);			// precision 1 minute.
			Log.i(TAG, " m_sport_time       = " + m_sport_time);		// precision 1 minute.
		}
		else if( m_flag == DATUM_TYPE_SLEEP ) {
			Log.i(TAG, " m_status           = " + m_status);
			Log.i(TAG, " m_awake_time       = " + m_awake_time);
			Log.i(TAG, " m_light_sleep_time = " + m_light_sleep_time);
			Log.i(TAG, " m_deep_sleep_time  = " + m_deep_sleep_time);
			Log.i(TAG, " m_awake_count      = " + m_awake_count);
			Log.i(TAG, " m_total_sleep_time = " + m_total_sleep_time);
		}
	}
	
	public static byte[] SportDatumEncode( ActionsDatum actionDatum ) {
		Log.i(TAG, "Robin---static byte[] SportDatumEncode");
		byte[] result = new byte[20];
		byte[] utcs = Utils.int2Bytes(actionDatum.getUTC(), Utils.BIG_ENDIUM);

		byte[] steps = Utils.int2Bytes(actionDatum.getSportSteps(), Utils.BIG_ENDIUM);
		byte[] distance = Utils.int2Bytes(actionDatum.getSportDistance(), Utils.BIG_ENDIUM);
		byte[] icaloric = Utils.int2Bytes(actionDatum.getSportIdleCaloric(), Utils.BIG_ENDIUM);
		byte[] scaloric = Utils.int2Bytes(actionDatum.getSportActiveCaloric(), Utils.BIG_ENDIUM);
		byte[] idletime = Utils.int2Bytes(actionDatum.getSportIdleTime(), Utils.BIG_ENDIUM);
		byte[] sportime = Utils.int2Bytes(actionDatum.getSportActiveTime(), Utils.BIG_ENDIUM);
		
		byte flag = (byte) actionDatum.getType();
		byte type = (byte) actionDatum.getDetailType();
		
		int cnt = 0;
		
		for( int i = 0; i < 4; i++ ) {
			result[cnt++] = utcs[i]++;
		}
		
		for( int i = 0; i < 4; i++ ) {
			result[cnt++] = steps[i]++;
		}
		
		for( int i = 0; i < 2; i++ ) {
			result[cnt++] = distance[i]++;
		}
		
		for( int i = 0; i < 2; i++ ) {
			result[cnt++] = icaloric[i]++;
		}
		
		for( int i = 0; i < 2; i++ ) {
			result[cnt++] = scaloric[i]++;
		}
		
		for( int i = 0; i < 2; i++ ) {
			result[cnt++] = idletime[i]++;
		}
		
		for( int i = 0; i < 2; i++ ) {
			result[cnt++] = sportime[i]++;
		}
		
		result[cnt++] = type;
		result[cnt] = flag;
		
		return result;
	}
	
	public static String utc2DateTimeString( int seconds, TimeZone tz ) {
		String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
							.format(new Date(((long)seconds)*1000));
		return date;
	}
	
	public static String utc2DateTimeString( int seconds, String format, TimeZone tz ) {
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
		sdf.setTimeZone(tz);
		return sdf.format(new Date(((long)seconds)*1000));
	}

	public static String utc2DateString( int seconds, TimeZone tz ) {
		return utc2DateTimeString( seconds, "yyyy_MM_dd", tz);
	}

	
	
	

	
}
