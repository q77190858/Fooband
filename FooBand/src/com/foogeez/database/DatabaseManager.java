package com.foogeez.database;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseManager {
    private final static String TAG = DatabaseManager.class.getSimpleName();
    private final static boolean DEBUG = false;
    
	private Context mContext = null;
	private SQLiteDatabase mDB = null;
	
	public DatabaseManager( Context context, String dir, String name ) {
		mContext = context;
		DatabaseHelper helper = new DatabaseHelper(mContext, dir, name);
		if( mDB == null ) {
			mDB = helper.openDatabase();
		}
	}
	
    /***
     * 
     * @param db
     * @param datum
     */
    private void addDatum( ActionsDatum datum ) {
    	Log.i(TAG,"Robin----addDatum( ActionsDatum datum )");
        //ContentValues以键值对的形式存放数据  
    	String tabName = null;
    	String date = ActionsDatum.utc2DateString(datum.getUTC(), TimeZone.getDefault());
        ContentValues cv = new ContentValues();
        
        //Log.e(TAG, "Table Number : " + getTableNumber());
        //getRecentRecordUtc();
        
    	Log.e(TAG, "record UTC:  " + datum.getDateTime(TimeZone.getDefault()));
    	Log.i(TAG,"Robin----record UTC: "+datum.getDateTime(TimeZone.getDefault()));
    	if( datum.getType() == ActionsDatum.DATUM_TYPE_SPORT ) {
    		Log.i(TAG,"Robin----datum.getType() == ActionsDatum.DATUM_TYPE_SPORT ");
    		tabName = "sport_" + date;
    		//mDB.execSQL("DROP TABLE IF EXISTS sport_" + date);  //创建sport表  
    		mDB.execSQL("CREATE TABLE IF NOT EXISTS " + tabName + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            								"m_utc TIMESTAMP, " +
            								"m_date_time VARCHAR, " +
            								"m_flag INTERGER, " +
            								"m_type INTERGER, " +
            								"m_steps INTERGER, " +
            								"m_distance INTERGER, " +
            								"m_idle_caloric INTERGER, " +
            								"m_sport_caloric INTERGER, " +
            								"m_idle_time INTERGER, " +
            								"m_sport_time INTERGER)");
    		
            cv.put("m_utc", datum.getUTC());  
            cv.put("m_date_time", datum.getDateTime(TimeZone.getDefault()));
            cv.put("m_flag", datum.getType());  
            cv.put("m_type", datum.getDetailType());
            cv.put("m_steps", datum.getSportSteps());
            cv.put("m_distance", datum.getSportDistance());       
            cv.put("m_idle_caloric", datum.getSportIdleCaloric());
            cv.put("m_sport_caloric", datum.getSportActiveCaloric());
            cv.put("m_idle_time", datum.getSportIdleTime());       
            cv.put("m_sport_time", datum.getSportActiveTime());
            if(DEBUG)Log.e(TAG, "STEPS: " + datum.getSportSteps() + " Steps!");
    	}
    	else if( datum.getType() == ActionsDatum.DATUM_TYPE_SLEEP ) {
    		Log.i(TAG,"Robin----datum.getType() == ActionsDatum.DATUM_TYPE_SLEEP ");
    		tabName = "sleep_" + date;
    		//mDB.execSQL("DROP TABLE IF EXISTS sleep_" + date);  //创建sleep表  
    		mDB.execSQL("CREATE TABLE IF NOT EXISTS "+ tabName + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            								"m_utc TIMESTAMP, " +
            								"m_date_time VARCHAR, " +
            								"m_flag INTERGER, " +
            								"m_type INTERGER, " +
            								"m_status INTERGER, " +
            								"m_awake_time INTERGER, " +
            								"m_light_sleep_time INTERGER, " +
            								"m_deep_sleep_time INTERGER, " +
            								"m_awake_count INTERGER, " +
            								"m_total_sleep_time INTERGER)");

            cv.put("m_utc", datum.getUTC());  
            cv.put("m_date_time", datum.getDateTime(TimeZone.getDefault()));
            cv.put("m_flag", datum.getType());  
            cv.put("m_type", datum.getDetailType());
            cv.put("m_status", datum.getSleepStatus());
            cv.put("m_awake_time", datum.getSleepAwakeTime());       
            cv.put("m_light_sleep_time", datum.getSleepLightTime());
            cv.put("m_deep_sleep_time", datum.getSleepDeepTime());
            cv.put("m_awake_count", datum.getSleepAwakeCount());       
            cv.put("m_total_sleep_time", datum.getSleepTotalTime());
            if(DEBUG)Log.e(TAG, "SLEEP: " + datum.getSleepTotalTime() + " Minutes!");
    	}

    	Log.e(TAG, "mDB.insert("+tabName + ", null, cv) = " + mDB.insert(tabName, null, cv));
    }
    
    
    private static List<ActionsDatum> DatumsCache = new ArrayList<ActionsDatum>();
    public void clearDatumCache() {
    	DatumsCache.clear();
    }
    
    public void addDatumInCache(ActionsDatum datum) {
    	Log.i(TAG,"Robin----addDatumInCache(ActionsDatum datum)");
    	DatumsCache.add(datum);
    	
    	if(DEBUG)Log.e(TAG,  "add Datum cache: " + datum.getDateTime(TimeZone.getDefault()));
    }
    
    public void refreshDatumInDB()  {
    	Log.i(TAG,"Robin----refreshDatumInDB()");
    	for(ActionsDatum datum : DatumsCache) {
    		Log.i(TAG,"Robin----for(ActionsDatum datum : DatumsCache)");
    		addDatum(datum);
    		if(DEBUG)Log.e(TAG,  "add Datum sqlite: " + datum.getDateTime(TimeZone.getDefault()));
    	}
    }
    
    
    
    /**
     * 
     * @param tabName
     * @return
     */
    private boolean isTableExsit( String tabName ) {
    	boolean result = false;
    	Cursor cursor = mDB.rawQuery( "SELECT COUNT(*) FROM sqlite_master WHERE type='table' and name='" + tabName + "'" , null);
    	if( cursor.moveToNext() ) {
    		if( cursor.getInt(0) > 0 ) {
    			result = true;
    		}
    	}
    	
    	cursor.close();
    	return result;
    }
    
    /***
     * 
     * @return
     */
    private int getTableNumber() {
    	Cursor cursor = mDB.rawQuery( "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND (name LIKE 'sport_%' OR name LIKE 'sleep_%')", null);
    	cursor.moveToFirst();
    	int num = cursor.getInt(0);
    	
    	cursor.close();
    	return num;
    }
    
    
    /**
     * 
     * @param tabName
     * @return
     */
    private int getRecordNumber( String tabName ) {
    	Cursor cursor = mDB.rawQuery( "SELECT COUNT(*) FROM " + tabName, null);
    	cursor.moveToFirst();
    	int num = cursor.getInt(0);

    	cursor.close();
    	return num;
    }
    
    /**
     * 
     * 
     * @param tabName
     * @param datum
     * @return
     *
    private boolean isInvalidRecord( String tabName, ActionsDatum datum ) {
    	boolean result = false;
    	Cursor cursor = mDB.rawQuery( "SELECT * FROM " + tabName + " ORDER BY m_utc DESC", null);
    	
    	cursor.moveToFirst();
    	int first = cursor.getInt(cursor.getColumnIndex("m_utc"));
    	
    	cursor.moveToLast();
    	int last = cursor.getInt(cursor.getColumnIndex("m_utc"));
    	
    	Log.e(TAG, "first = " + ActionsDatum.utc2DateTimeString(first,TimeZone.getDefault()) + 
    				", last = " + ActionsDatum.utc2DateTimeString(last,TimeZone.getDefault()) + 
    				", UTC = " + datum.getDateTime(TimeZone.getDefault()));
    	
    	if( (datum.getUTC() <= first)&&(datum.getUTC() >= last) ) {
    		result = true;
    	}
    	
    	cursor.close();
    	return result;
    }
    
    private boolean isInvalidRecordOnlyOne( String tabName, ActionsDatum datum ) {
    	boolean result = false;
    	Cursor cursor = mDB.rawQuery( "SELECT * FROM " + tabName + " ORDER BY m_utc DESC", null);
    	
    	cursor.moveToFirst();
    	int first = cursor.getInt(cursor.getColumnIndex("m_utc"));
    	if( first == datum.getUTC() ) {
    		result = true;
    	}
    	
    	cursor.close();
    	return result;
    }
    
    
    /***
     * 
     * @param tabName
     * @return
     */
    private int getMaxUtcInTable(String tabName ) {
    	Cursor cursor = mDB.rawQuery( "SELECT * FROM " + tabName + " ORDER BY m_utc DESC", null);
    	cursor.moveToFirst();
    	int utc = cursor.getInt(cursor.getColumnIndex("m_utc"));
    	
    	cursor.close();
    	return utc;
    }
    
    private int getMinUtcInTable(String tabName ) {
    	Cursor cursor = mDB.rawQuery( "SELECT * FROM " + tabName + " ORDER BY m_utc ASC", null);
    	cursor.moveToFirst();
    	int utc = cursor.getInt(cursor.getColumnIndex("m_utc"));
    	
    	cursor.close();
    	return utc;
    }
    
    public int getRecentRecordUtc() {
    	
    	//Log.e(TAG, "STEP0");
    	//Log.e(TAG, "Table Number : " + getTableNumber());
    	if( getTableNumber() <= 0 ) {
    		return 0;
    	}
    	
    	//Log.e(TAG, "STEP1");
    	int sleepMaxUtc = 0;
    	Cursor xcursor = mDB.rawQuery( "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name LIKE 'sleep_%'" , null);
    	xcursor.moveToFirst();
    	if( xcursor.getInt(0) > 0 ) {
    		//Log.e(TAG, "SLEEP TAB NUMBER: " + xcursor.getInt(0));
    		xcursor.close();
    		Cursor xxcursor = mDB.rawQuery( "SELECT * FROM sqlite_master WHERE type='table' AND name LIKE 'sleep_%' ORDER BY name DESC" , null);
    		xxcursor.moveToFirst();
	    	String sleepMaxTableName = xxcursor.getString(xxcursor.getColumnIndex("name"));
	    	xxcursor.close();
	    	//Log.e(TAG, "SLEEP TAB MAX DATETIME NAME: " + sleepMaxTableName);
	    	if( getRecordNumber(sleepMaxTableName) > 0 ) {
	    		sleepMaxUtc = getMaxUtcInTable(sleepMaxTableName);
	    	}
    	}
    	//Log.e(TAG, "MAX SLEEP UTC : " + ActionsDatum.utc2DateTimeString(sleepMaxUtc, TimeZone.getDefault()));
    	
    	//Log.e(TAG, "STEP2");
    	int sportMaxUtc = 0;
    	Cursor ycursor = mDB.rawQuery( "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name LIKE 'sport_%'" , null);
    	ycursor.moveToFirst();
    	if( ycursor.getInt(0) > 0 ) {
    		//Log.e(TAG, "SPORT TAB NUMBER: " + ycursor.getInt(0));
    		ycursor.close();
    		Cursor yycursor = mDB.rawQuery( "SELECT * FROM sqlite_master WHERE type='table' AND name LIKE 'sport_%' ORDER BY name DESC" , null);
    		yycursor.moveToFirst();
	    	String sportMaxTableName = yycursor.getString(yycursor.getColumnIndex("name"));
	    	yycursor.close();
	    	//Log.e(TAG, "SPORT TAB MAX DATETIME NAME: " + sportMaxTableName);
	    	if( getRecordNumber(sportMaxTableName) > 0 ) {
	    		sportMaxUtc = getMaxUtcInTable(sportMaxTableName);
	    	}
    	}
    	//Log.e(TAG, "MAX SPORT UTC : " + ActionsDatum.utc2DateTimeString(sportMaxUtc, TimeZone.getDefault()));
    	
    	//Log.e(TAG, "STEP3");
    	//Log.e(TAG, "MAX TOTAL UTC : " + ActionsDatum.utc2DateTimeString(Math.max(sleepMaxUtc, sportMaxUtc), TimeZone.getDefault()));
    	return Math.max(sleepMaxUtc, sportMaxUtc);
    }
    
    public int getOldestRecordUtc() {
    	
    	//Log.e(TAG, "STEP0");
    	//Log.e(TAG, "Table Number : " + getTableNumber());
    	if( getTableNumber() <= 0 ) {
    		return 0;
    	}
    	
    	//Log.e(TAG, "STEP1");
    	int sleepMaxUtc = 0;
    	Cursor xcursor = mDB.rawQuery( "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name LIKE 'sleep_%'" , null);
    	xcursor.moveToFirst();
    	if( xcursor.getInt(0) > 0 ) {
    		//Log.e(TAG, "SLEEP TAB NUMBER: " + xcursor.getInt(0));
    		xcursor.close();
    		Cursor xxcursor = mDB.rawQuery( "SELECT * FROM sqlite_master WHERE type='table' AND name LIKE 'sleep_%' ORDER BY name ASC" , null);
    		xxcursor.moveToFirst();
	    	String sleepMaxTableName = xxcursor.getString(xxcursor.getColumnIndex("name"));
	    	xxcursor.close();
	    	//Log.e(TAG, "SLEEP TAB MAX DATETIME NAME: " + sleepMaxTableName);
	    	if( getRecordNumber(sleepMaxTableName) > 0 ) {
	    		sleepMaxUtc = getMinUtcInTable(sleepMaxTableName);
	    	}
    	}
    	//Log.e(TAG, "MAX SLEEP UTC : " + ActionsDatum.utc2DateTimeString(sleepMaxUtc, TimeZone.getDefault()));
    	
    	//Log.e(TAG, "STEP2");
    	int sportMaxUtc = 0;
    	Cursor ycursor = mDB.rawQuery( "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name LIKE 'sport_%'" , null);
    	ycursor.moveToFirst();
    	if( ycursor.getInt(0) > 0 ) {
    		//Log.e(TAG, "SPORT TAB NUMBER: " + ycursor.getInt(0));
    		ycursor.close();
    		Cursor yycursor = mDB.rawQuery( "SELECT * FROM sqlite_master WHERE type='table' AND name LIKE 'sport_%' ORDER BY name ASC" , null);
    		yycursor.moveToFirst();
	    	String sportMaxTableName = yycursor.getString(yycursor.getColumnIndex("name"));
	    	yycursor.close();
	    	//Log.e(TAG, "SPORT TAB MAX DATETIME NAME: " + sportMaxTableName);
	    	if( getRecordNumber(sportMaxTableName) > 0 ) {
	    		sportMaxUtc = getMinUtcInTable(sportMaxTableName);
	    	}
    	}
    	//Log.e(TAG, "MAX SPORT UTC : " + ActionsDatum.utc2DateTimeString(sportMaxUtc, TimeZone.getDefault()));
    	
    	//Log.e(TAG, "STEP3");
    	//Log.e(TAG, "MAX TOTAL UTC : " + ActionsDatum.utc2DateTimeString(Math.max(sleepMaxUtc, sportMaxUtc), TimeZone.getDefault()));
    	return Math.min(sleepMaxUtc, sportMaxUtc);
    }
    
    
    public static final int DATUM_TYPE_BY_DAY = 0;
    public static final int DATUM_TYPE_BY_WEEK = 1;
    public static final int DATUM_TYPE_BY_MONTH = 2;
    public static final int DATUM_TYPE_BY_YEAR = 3;
    
    public ActionsDatum 
    getTotalSportDatumByDate( final int type, int utc ) {
    	
    	if( DATUM_TYPE_BY_DAY == type ) {
    		String tabName = "sport_" + ActionsDatum.utc2DateTimeString(utc, "yyyy_MM_dd", TimeZone.getDefault());
    		if( !isTableExsit(tabName) ) {
    			return null;
    		}
    		if( getRecordNumber(tabName) <= 0 ) {
    			return null;
    		}
    		
    		Cursor cursor = mDB.rawQuery( "SELECT * FROM " + tabName + " ORDER BY m_utc DESC", null);
        	cursor.moveToFirst();
        	ActionsDatum actionsDatum = new ActionsDatum(
    			ActionsDatum.DATUM_TYPE_SPORT,
    			cursor.getInt(cursor.getColumnIndex("m_utc")),
    			cursor.getInt(cursor.getColumnIndex("m_type")),
    			cursor.getInt(cursor.getColumnIndex("m_steps")),
    			cursor.getInt(cursor.getColumnIndex("m_distance")),
    			cursor.getInt(cursor.getColumnIndex("m_idle_caloric")),
    			cursor.getInt(cursor.getColumnIndex("m_sport_caloric")),
    			cursor.getInt(cursor.getColumnIndex("m_idle_time")),
    			cursor.getInt(cursor.getColumnIndex("m_sport_time"))
        	);
        	cursor.close();
    		return actionsDatum;
    	}
    	return null;
    }
    

    @SuppressLint("DefaultLocale")
	public List<Integer> 
    getHistorySportDatumByDate( final int type, int utc ) {
    	
    	List<Integer> sportDatum = null;
    	if( DATUM_TYPE_BY_DAY == type ) {
    		String tabName = "sport_" + ActionsDatum.utc2DateTimeString(utc, "yyyy_MM_dd", TimeZone.getDefault());
    		if( !isTableExsit(tabName) ) {
    			return null;
    		}
    		if( getRecordNumber(tabName) <= 0 ) {
    			return null;
    		}
    		
    		sportDatum = new ArrayList<Integer>();
    		String dateKey = ActionsDatum.utc2DateString(utc, TimeZone.getDefault());
    		
    		int prvHourMaxStep;
    		int curHourMaxStep = 0;
     		//Log.e(TAG, "CURRENT HOUR: " + Integer.valueOf(ActionsDatum.utc2DateTimeString(utc, "HH", TimeZone.getDefault())));
    		for( int hour = 0; hour < 24; hour++ ) {
	    		prvHourMaxStep = curHourMaxStep;
	    		String hourKey = String.format("%02d", hour);
	    		Cursor cursor = mDB.rawQuery( "SELECT * FROM " + tabName + " WHERE m_date_time LIKE '" + dateKey + " " + hourKey + "%'  ORDER BY m_date_time DESC", null);
	    		if( !cursor.moveToFirst() ) {
	    			sportDatum.add(0);
	    			/**
	    			Log.e(TAG, "无记录！");
	    			Log.e(TAG, "SORT MIN " +
	    					", DATUM: " + ActionsDatum.utc2DateTimeString(curHourMinUtc, TimeZone.getDefault()) + 
	    					", STEPS: " + curHourMinStep +
	    					", STATE: " + curHourMinState);
	    			Log.e(TAG, "SORT MAX " +
	    					", DATUM: " + ActionsDatum.utc2DateTimeString(curHourMaxUtc, TimeZone.getDefault()) + 
	    					", STEPS: " + curHourMaxStep +
	    					", STATE: " + curHourMaxState);
	    			/**/
	    		}
	    		else {
	    			cursor.moveToFirst();
	    			curHourMaxStep = cursor.getInt(cursor.getColumnIndex("m_steps"));
			    	cursor.moveToLast();
	    			sportDatum.add(curHourMaxStep-prvHourMaxStep);
	    			/**
	    			Log.i(TAG, "有记录！");
	    			Log.i(TAG, "SORT MIN " +
	    					", DATUM: " + ActionsDatum.utc2DateTimeString(curHourMinUtc, TimeZone.getDefault()) + 
	    					", STEPS: " + curHourMinStep +
	    					", STATE: " + curHourMinState);
	    			Log.i(TAG, "SORT MAX " +
	    					", DATUM: " + ActionsDatum.utc2DateTimeString(curHourMaxUtc, TimeZone.getDefault()) + 
	    					", STEPS: " + curHourMaxStep +
	    					", STATE: " + curHourMaxState);
	    			/**/
	    		}
	    		cursor.close();
    		}
    		
    		//for( int i = 0; i < sportDatum.size(); i++ ) {
    		//	Log.e(TAG, "STEP HOUR[" + i + "] = " + sportDatum.get(i));
    		//}
    	}
    	
    	return sportDatum;
    }
    
	public List<ActionsDatum> 
    getHistorySportDatumByDate2( final int type, int utc ) {
    	List<ActionsDatum> sportDatums = new ArrayList<ActionsDatum>();
    	if( DATUM_TYPE_BY_DAY == type ) {
    		String tabName = "sport_" + ActionsDatum.utc2DateTimeString(utc, "yyyy_MM_dd", TimeZone.getDefault());
    		if( !isTableExsit(tabName) ) {
    			return null;
    		}
    		if( getRecordNumber(tabName) <= 0 ) {
    			return null;
    		}
    		
    		Cursor cursor = mDB.rawQuery( "SELECT * FROM " + tabName + " ORDER BY m_utc ASC", null);
        	if( cursor.moveToFirst() ) {
	        	do {
		        	ActionsDatum actionsDatum = new ActionsDatum(
		    			ActionsDatum.DATUM_TYPE_SPORT,
		    			cursor.getInt(cursor.getColumnIndex("m_utc")),
		    			cursor.getInt(cursor.getColumnIndex("m_type")),
		    			cursor.getInt(cursor.getColumnIndex("m_steps")),
		    			cursor.getInt(cursor.getColumnIndex("m_distance")),
		    			cursor.getInt(cursor.getColumnIndex("m_idle_caloric")),
		    			cursor.getInt(cursor.getColumnIndex("m_sport_caloric")),
		    			cursor.getInt(cursor.getColumnIndex("m_idle_time")),
		    			cursor.getInt(cursor.getColumnIndex("m_sport_time"))
		        	);
		        	sportDatums.add(actionsDatum);
				} 
				while( cursor.moveToNext() );
        	}
        	cursor.close();
    	}
    	return sportDatums;
    }
    
    public ActionsDatum 
    getTotalSleepDatumByDate( final int type, int utc ) {
    	ActionsDatum sleepDatum = null;
    	List<ActionsDatum> sleepDatums = getHistorySleepDatumByDate(type, utc);
    	if( sleepDatums != null ) {
    		sleepDatum = sleepDatums.get(0);
    	}
    	return sleepDatum;
    }
    
    private int mCurrentSleepDatumsNumber = 0;
    private List<List<ActionsDatum>> sleepDatums = new ArrayList<List<ActionsDatum>>();
    
    @SuppressLint("DefaultLocale")
	public List<ActionsDatum> 
    getHistorySleepDatumByDate( final int type, int utc ) {
    	
    	List<ActionsDatum> sleepDatum = null;
    	List<ActionsDatum> indexDatum = null;
    	
    	if( DATUM_TYPE_BY_DAY == type ) {

    		String curTabName = "sleep_" + ActionsDatum.utc2DateTimeString(utc, "yyyy_MM_dd", TimeZone.getDefault());
    		if( !isTableExsit(curTabName) ) {
    			return null;
    		}
    		if( getRecordNumber(curTabName) <= 0 ) {
    			return null;
    		}
    		
    		indexDatum = new ArrayList<ActionsDatum>();
    		String curRecordFilter = ActionsDatum.utc2DateTimeString(utc, "yyyy/MM/dd ", TimeZone.getDefault()) + "12:00:00";
    		Cursor curCursor = mDB.rawQuery( "SELECT * FROM " + curTabName + " WHERE m_date_time  < '" + curRecordFilter + "' ORDER BY m_date_time DESC", null);
    		if( curCursor.moveToFirst() ) {
    			do {
	            	ActionsDatum actionsDatum = new ActionsDatum(
	            			ActionsDatum.DATUM_TYPE_SLEEP,
	            			curCursor.getInt(curCursor.getColumnIndex("m_utc")),
	            			curCursor.getInt(curCursor.getColumnIndex("m_type")),
	            			curCursor.getInt(curCursor.getColumnIndex("m_status")),
	            			curCursor.getInt(curCursor.getColumnIndex("m_awake_time")),
	            			curCursor.getInt(curCursor.getColumnIndex("m_light_sleep_time")),
	            			curCursor.getInt(curCursor.getColumnIndex("m_deep_sleep_time")),
	            			curCursor.getInt(curCursor.getColumnIndex("m_awake_count")),
	            			curCursor.getInt(curCursor.getColumnIndex("m_total_sleep_time"))
	            			);
	            	indexDatum.add(actionsDatum);
    			} 
    			while( curCursor.moveToNext() );
    		}
    		curCursor.close();
    		
    		String prvTabName = "sleep_" + ActionsDatum.utc2DateTimeString(utc-24*3600, "yyyy_MM_dd", TimeZone.getDefault()); 
    		String prvRecordFilter = ActionsDatum.utc2DateTimeString(utc-24*3600, "yyyy/MM/dd ", TimeZone.getDefault()) + "12:00:00";
    		if( isTableExsit(prvTabName)&&(getRecordNumber(prvTabName) >= 0) ) {
        		Cursor prvCursor = mDB.rawQuery( "SELECT * FROM " + prvTabName + " WHERE m_date_time > '" + prvRecordFilter + "' ORDER BY m_date_time DESC", null);
        		if( prvCursor.moveToFirst() ) {
        			do {
    	            	ActionsDatum actionsDatum = new ActionsDatum(
    	            			ActionsDatum.DATUM_TYPE_SLEEP,
    	            			prvCursor.getInt(prvCursor.getColumnIndex("m_utc")),
    	            			prvCursor.getInt(prvCursor.getColumnIndex("m_type")),
    	            			prvCursor.getInt(prvCursor.getColumnIndex("m_status")),
    	            			prvCursor.getInt(prvCursor.getColumnIndex("m_awake_time")),
    	            			prvCursor.getInt(prvCursor.getColumnIndex("m_light_sleep_time")),
    	            			prvCursor.getInt(prvCursor.getColumnIndex("m_deep_sleep_time")),
    	            			prvCursor.getInt(prvCursor.getColumnIndex("m_awake_count")),
    	            			prvCursor.getInt(prvCursor.getColumnIndex("m_total_sleep_time"))
    	            			);
    	            	indexDatum.add(actionsDatum);
        			} 
        			while( prvCursor.moveToNext() );
        		}
        		prvCursor.close();
    		}
    		else {
    			if( indexDatum.size() == 0 ) {
    				return null;
    			}
    		}
    		
    		//List<List<ActionsDatum>> sleepDatums = new ArrayList<List<ActionsDatum>>();
    		sleepDatums.clear();
    		if( indexDatum.size() > 2 ) {
        		List<ActionsDatum> datum = new ArrayList<ActionsDatum>();
	    		for( int i = 0,j = 0; i < indexDatum.size(); i++,j++ ) {
	    			//Log.e(TAG,  "SLEEP DATETIME[" + i + "] = " + sleepDatum.get(i).getDateTime(TimeZone.getDefault()) + ", status = " + sleepDatum.get(i).getSleepStatus() + ", total = " + sleepDatum.get(i).getSleepTotalTime());
	    			if( i == 0 ) {
	    				datum.add(indexDatum.get(i));
	    				continue;
	    			}
	    			if( indexDatum.get(i).getSleepTotalTime() <= datum.get(j-1).getSleepTotalTime() ) {
	    				datum.add(indexDatum.get(i));
	    			}
	    			else {
	    				sleepDatums.add(datum);
	    				datum = new ArrayList<ActionsDatum>();
	    				datum.add(indexDatum.get(i));
	    				j = 0; 
	    			}
	    		}
	    		sleepDatums.add(datum);
    		}
    		else {
    			return null;
    		}
    		
    		//Log.e(TAG, "数据库中发现" + sleepDatums.size() + "段有效睡眠数据！");
    		
    		//for( int i = 0; i < sleepDatums.size(); i++ ) {
    		//	Log.e(TAG, "第" + i + "段睡眠数据：");
    		//	for( int j = 0; j < sleepDatums.get(i).size(); j++ ) {
    		//		Log.e(TAG, "数据[" + j + "] = " + sleepDatums.get(i).get(j).getSleepTotalTime() + " : " + sleepDatums.get(i).get(j).getDateTime(TimeZone.getDefault()) );
    		//	}
    		//}
    		
    		indexDatum = sleepDatums.get(0);
    		//for( List<ActionsDatum> datum : sleepDatums ) {
    		//	if( datum.get(0).getSleepTotalTime() > indexDatum.get(0).getSleepTotalTime() ) {
    		//		indexDatum = datum;
    		//	}
    		//}
    		
    		for( int i = 0; i < sleepDatums.size(); i++ ) {
    			List<ActionsDatum> datum = sleepDatums.get(i);
    			if( datum.get(0).getSleepTotalTime() > indexDatum.get(0).getSleepTotalTime() ) {
    				indexDatum = datum;
    				mCurrentSleepDatumsNumber = i;
    			}
    		}
    		
			if( indexDatum.size() > 8 ) {
				sleepDatum = indexDatum;
				Log.e(TAG, "sleepDatum.size() = " + sleepDatum.size());
			}
    		
    		//sleepDatum = sleepDatums.get(0);
    	}
    	
    	return sleepDatum;
    }
    
    
    public int getHistorySleepDatumSize() {
    	return sleepDatums.size();
    }
    
    public int getHistorySleepDatumIndex() {
    	return mCurrentSleepDatumsNumber;
    }
    
    public List<ActionsDatum> getHistorySleepDatumByNumber(int number) {
    	return sleepDatums.get(number);
    }
    
    
    /***
     * 
     * @param db
     */
    public void closeDB() {
    	mDB.close();
    }
    
    
	 /**
     * 
     * @param name
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
     */
	
}
