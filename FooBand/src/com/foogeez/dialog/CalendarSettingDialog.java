package com.foogeez.dialog;

import java.util.Calendar;

import com.foogeez.configuration.Configuration.CalendarConfiguration;
import com.foogeez.fooband.R;
import com.grdn.util.Utils;
import com.grdn.wheels.AbstractWheel;
import com.grdn.wheels.OnWheelChangedListener;
import com.grdn.wheels.adapters.AbstractWheelTextAdapter;
import com.grdn.wheels.adapters.NumericWheelAdapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public class CalendarSettingDialog extends SettingDialog {
    private final static String TAG = CalendarSettingDialog.class.getSimpleName();
    
    private Context context;
    private EditText mCalendarTitle = null;

    private int mHour = 0;
    private int mMinute = 0;
	private Calendar mFirstCalendar = null;
	private Calendar mLastCalendar = null;
    
	private OnConfirmListener listener = null; 
	private CalendarConfiguration mConfig = null;
	
	public CalendarSettingDialog(Context context, CalendarConfiguration config) {
		super(context);
		this.context = context;
		mConfig = config;
	}
	
    public CalendarSettingDialog(Context context, CalendarConfiguration config, int theme) {
		super(context,theme);
        this.context = context;
		mConfig = config;
    }
    
    public interface OnConfirmListener {
    	public void OnConfirm(CalendarConfiguration config);
    };
    
    public void setOnConfirmListener( OnConfirmListener listener ) {
    	this.listener = listener;
    }
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_setting_calendar);
//        this.setContentView(R.layout.dialog_setting_alarm);
        
        AbstractWheel hours = (AbstractWheel) findViewById(R.id.calendar_hours);
        NumericWheelAdapter hourAdapter = new NumericWheelAdapter(context, 0, 23, "%02d");
        hourAdapter.setItemResource(R.layout.wheel_text_centered);
        hourAdapter.setItemTextResource(R.id.text);
        hours.setViewAdapter(hourAdapter);
        hours.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
				mHour = newValue;
			}
        });
        hours.setCyclic(true);
        hours.setCurrentItem(0); 			// getCurrent;

        AbstractWheel mins = (AbstractWheel) findViewById(R.id.calendar_minutes);
        NumericWheelAdapter minAdapter = new NumericWheelAdapter(context, 0, 59, "%02d");
        minAdapter.setItemResource(R.layout.wheel_text_centered);
        minAdapter.setItemTextResource(R.id.text);
        mins.setViewAdapter(minAdapter);
        mins.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
				mMinute = newValue;
			}
        });
        mins.setCyclic(true);
        mins.setCurrentItem(0);
        
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeInMillis((long)mConfig.getDateTime()*1000);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
    	
        mFirstCalendar = Calendar.getInstance();
        mLastCalendar = (Calendar) mFirstCalendar.clone();
        
        final AbstractWheel day = (AbstractWheel) findViewById(R.id.calendar_day);
        DayArrayAdapter dayAdapter = new DayArrayAdapter(context, mFirstCalendar);
        day.setViewAdapter(dayAdapter);
        day.setCurrentItem(dayAdapter.getToday());
        day.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
				Log.e("DEBUG_CALENDAR", "NEW VALUE: " + newValue );
				mLastCalendar = (Calendar) mFirstCalendar.clone();
				mLastCalendar.add(Calendar.DAY_OF_YEAR, newValue);
			}
        });
        
        hours.setCurrentItem(mHour);
        mins.setCurrentItem(mMinute);
        
        if( mConfig.getDateTime() > (int)(mFirstCalendar.getTimeInMillis()/1000) ) {
        	day.setCurrentItem(calendar.get(Calendar.DAY_OF_YEAR)-mFirstCalendar.get(Calendar.DAY_OF_YEAR));
        }
        
        initYesNo();
        initCalendarTitle();
        
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
      }
    
    
	private void initYesNo() {
		mTextViewConfirm = (TextView)findViewById(R.id.id_tv_setting_confirm);
		mTextViewConfirm.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				mLastCalendar.set(Calendar.HOUR_OF_DAY, mHour);
				mLastCalendar.set(Calendar.MINUTE, mMinute);
				mConfig.setDateTime((int)(mLastCalendar.getTimeInMillis()/1000));
				Log.e("DEBUG_CALENDAR", Integer.toHexString(mConfig.getDateTime()));
				mConfig.setTitle(mCalendarTitle.getText().toString());
				if( listener != null )
					listener.OnConfirm(mConfig);
				CalendarSettingDialog.this.dismiss();
			}
        });
        
		mTextViewCancel = (TextView)findViewById(R.id.id_tv_setting_cancel);
		mTextViewCancel.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				CalendarSettingDialog.this.dismiss();
			}
        });
	}
	
	private void initCalendarTitle() {
        mCalendarTitle = ((EditText)findViewById(R.id.id_et_calendar_title));
        mCalendarTitle.setText(mConfig.getTitle());
        mCalendarTitle.clearFocus();
	}

    /**
     * Day adapter
     *
     */
	public class DayArrayAdapter extends AbstractWheelTextAdapter {
        // Count of days to be shown
        private final int daysCount = 365;
        
        // Calendar
        Calendar calendar;
        
        /**
         * Constructor
         */
        protected DayArrayAdapter(Context context, Calendar calendar) {
            super(context, R.layout.time_picker_custom_day, NO_RESOURCE);
            this.calendar = calendar;

            setItemTextResource(R.id.time2_monthday);
        }
        public int getToday() {
            //return daysCount / 2;
        	return 0;
        }

		@Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            //int day = -daysCount/2 + index;
			int day = index;
			
			Log.e(TAG, "index = " + index + " ,day = " + day);
			
            Calendar newCalendar = (Calendar) calendar.clone();
            newCalendar.add(Calendar.DAY_OF_YEAR, day);
            
            View view = super.getItem(index, cachedView, parent);

            TextView weekday = (TextView) view.findViewById(R.id.time2_weekday);
            TextView monthday = (TextView) view.findViewById(R.id.time2_monthday);
            

            if (day == 0) {
            	if( Utils.utc2DateTime("yyyy-MM-dd", newCalendar.getTime()).equals(Utils.utc2DateTime("yyyy-MM-dd", Calendar.getInstance().getTime()))) {
	                monthday.setText("Today");
	                monthday.setTextColor(0xFF0000F0);
	                weekday.setText("");
            	}
            	else {
                	monthday.setText(Utils.utc2DateTime("yyyy-MM-dd", newCalendar.getTime()));
                    monthday.setTextColor(0xFF111111);
                    weekday.setText(Utils.utc2DateTime("E", newCalendar.getTime()));
            	}
            
            } else {
                //DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                //monthday.setText(format.format(newCalendar.getTime()));
            	monthday.setText(Utils.utc2DateTime("yyyy-MM-dd", newCalendar.getTime()));
                monthday.setTextColor(0xFF111111);
                weekday.setText(Utils.utc2DateTime("E", newCalendar.getTime()));
            }
            
            //DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd");
            //view.setTag(dFormat.format(newCalendar.getTime()));
            view.setTag(Utils.utc2DateTime("yyyy-MM-dd", newCalendar.getTime()));
            return view;
        }
        
        @Override
        public int getItemsCount() {
            return daysCount + 1;
        }
        
        @Override
        protected CharSequence getItemText(int index) {
            return "";
        }
    }

}
