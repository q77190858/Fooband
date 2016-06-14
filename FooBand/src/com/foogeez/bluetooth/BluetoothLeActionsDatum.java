package com.foogeez.bluetooth;

import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class BluetoothLeActionsDatum {
	public static final String TAG = "BluetoothLeActionsDatum";
	
    public static final int DATUM_TYPE_SPORT	= 0;
    public static final int DATUM_TYPE_SLEEP	= 1;
    //private byte mDatumType = DATUM_TYPE_SPORT;
    
    public static final int BIG_ENDIUM = 0;
    public static final int LITTLE_ENDIUM = 1;
	
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
	
	public BluetoothLeActionsDatum(byte[] recvdata) {
		
		m_utc = bytes2Int(recvdata, 0, 4, BIG_ENDIUM);
		m_type = bytes2Int(recvdata, 18, 1, BIG_ENDIUM);
		m_flag = bytes2Int(recvdata, 19, 1, BIG_ENDIUM);
		
		if( m_flag == DATUM_TYPE_SPORT ) {
			m_steps = bytes2Int(recvdata, 4, 4, BIG_ENDIUM);
			m_distance = bytes2Int(recvdata, 8, 2, BIG_ENDIUM);				// precision 10m.
			m_idle_caloric = bytes2Int(recvdata, 10, 2, BIG_ENDIUM);		// precision 1 cal.
			m_sport_caloric = bytes2Int(recvdata, 12, 2, BIG_ENDIUM);		// precision 1 cal.
			m_idle_time = bytes2Int(recvdata, 14, 2, BIG_ENDIUM);			// precision 1 minute.
			m_sport_time = bytes2Int(recvdata, 16, 2, BIG_ENDIUM);			// precision 1 minute.
		}
		else if( m_flag == DATUM_TYPE_SLEEP ) {
			m_status = bytes2Int(recvdata, 4, 1, BIG_ENDIUM);
			//private byte resv0;
			m_awake_time = bytes2Int(recvdata, 6, 2, BIG_ENDIUM);
			m_light_sleep_time = bytes2Int(recvdata, 8, 2, BIG_ENDIUM);
			m_deep_sleep_time = bytes2Int(recvdata, 10, 2, BIG_ENDIUM);
			m_awake_count = bytes2Int(recvdata, 12, 2, BIG_ENDIUM);
			m_total_sleep_time = bytes2Int(recvdata, 14, 2, BIG_ENDIUM);
			//private int resv1;
		}
		else {
			
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
	
	public static String utc2DateString( int seconds ) {
		String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)
			.format(new Date(((long)seconds)*1000));
		//Log.i(TAG, "DATE : " + date);
		return date;
	}
	
	public static String utc2DateString( int seconds, String format ) {
		String date = new java.text.SimpleDateFormat(format, Locale.US)
			.format(new Date(((long)seconds)*1000));
		//Log.i(TAG, "DATE : " + date);
		return date;
	}
	
	public static int bytes2Int(byte[] src, int offset, int size, final int type) {  
	    int value = 0;
	    
	    if( (size <= 0)||(size>=5) ) {
	    	Log.e(TAG, "size error:" + size);
	    	return 0;
	    }
	    
	    if( (type != BIG_ENDIUM)&&(type != LITTLE_ENDIUM) ) {
	    	Log.e(TAG, "type error:" + type);
	    	return 0;
	    }
	    
	    if( type == BIG_ENDIUM ) {
		    for( int i = 0; i < size; i++ ) {
			    value |= (int)((src[offset+i]&0xFF)<<i*8);
		    }
	    }
	    else {
		    for( int i = 0; i < size; i++ ) {
			    value |= (int)((src[offset+i]&0xFF)<<(size-1-i)*8);
		    }
	    }
	    
	    return value;
	}
	

	
}
