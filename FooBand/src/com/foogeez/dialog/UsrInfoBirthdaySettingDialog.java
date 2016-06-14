package com.foogeez.dialog;

import java.util.Calendar;

import com.foogeez.configuration.Configuration.UsrInfoConfiguration;
import com.foogeez.fooband.R;
import com.grdn.wheels.AbstractWheel;
import com.grdn.wheels.OnWheelChangedListener;
import com.grdn.wheels.adapters.NumericWheelAdapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class UsrInfoBirthdaySettingDialog extends SettingDialog {
    private final static String TAG = UsrInfoBirthdaySettingDialog.class.getSimpleName();
    
    Context context;
	private UsrInfoConfiguration mConfig = null;
	private OnConfirmListener listener = null; 
	
	AbstractWheel mday = null;
	NumericWheelAdapter mdayAdapter = null;
	
	private int mCalendarYear = 1980;
	private int mCalendarMonth = 9;
	private int mCalendarMonthday = 28;
	
	private void initBirthday() {
		long utc = (long)mConfig.getBirthday()*1000;
		Calendar calendar = Calendar.getInstance(); 
		calendar.clear(); 
		calendar.setTimeInMillis(utc);
		mCalendarYear = calendar.get(Calendar.YEAR);
		mCalendarMonth = calendar.get(Calendar.MONTH)+1;
		mCalendarMonthday = calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	public UsrInfoBirthdaySettingDialog(Context context, UsrInfoConfiguration config) {
		super(context);
		mConfig = config;
        this.context = context;
        initBirthday();
	}
	
    public UsrInfoBirthdaySettingDialog(Context context, UsrInfoConfiguration config, int theme) {
		super(context, theme);
		mConfig = config;
        this.context = context;
        initBirthday();
    }
    
    public interface OnConfirmListener {
    	public void OnConfirm(UsrInfoConfiguration config);
    };
    
    public void setOnConfirmListener( OnConfirmListener listener ) {
    	this.listener = listener;
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_setting_usr_info_birthday);
        
        mday = (AbstractWheel) findViewById(R.id.usr_info_birthday_day);
        mdayAdapter = new NumericWheelAdapter(context, 1, getDayCount(), "%02d");
        mdayAdapter.setItemResource(R.layout.wheel_text_centered);
        mdayAdapter.setItemTextResource(R.id.text);
        mday.setViewAdapter(mdayAdapter);
        mday.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
				Log.e(TAG, "day --- onChanged");
				mCalendarMonthday = newValue+1;
				mConfig.setBirthday(mCalendarYear,mCalendarMonth,mCalendarMonthday);
			}
        });
        mday.setCyclic(false);
        mday.setCurrentItem(mCalendarMonthday-1);
        
        AbstractWheel year = (AbstractWheel) findViewById(R.id.usr_info_birthday_year);
        NumericWheelAdapter yearAdapter = new NumericWheelAdapter(context, 1900, getMaxYear(), "%04d");
        yearAdapter.setItemResource(R.layout.wheel_text_centered);
        yearAdapter.setItemTextResource(R.id.text);
        year.setViewAdapter(yearAdapter);
        year.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
				Log.e(TAG, "year --- onChanged");
				mCalendarYear = newValue+1900;
				refreshDayWheel();
				mConfig.setBirthday(mCalendarYear,mCalendarMonth,mCalendarMonthday);
			}
        });
        year.setCyclic(false);
        year.setCurrentItem(mCalendarYear-1900); // getCurrent;

        AbstractWheel month = (AbstractWheel) findViewById(R.id.usr_info_birthday_month);
        NumericWheelAdapter monthAdapter = new NumericWheelAdapter(context, 1, 12, "%02d");
        monthAdapter.setItemResource(R.layout.wheel_text_centered);
        monthAdapter.setItemTextResource(R.id.text);
        month.setViewAdapter(monthAdapter);
        month.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
				Log.e(TAG, "month --- onChanged");
				mCalendarMonth = newValue+1;
				refreshDayWheel();
				mConfig.setBirthday(mCalendarYear,mCalendarMonth,mCalendarMonthday);
			}
        });
        month.setCyclic(false);
        month.setCurrentItem(mCalendarMonth-1);
        
        initYesNo();
    }
    
	private void initYesNo() {
		mTextViewConfirm = (TextView)findViewById(R.id.id_tv_setting_confirm);
		mTextViewConfirm.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( listener != null )
					listener.OnConfirm(mConfig);
				UsrInfoBirthdaySettingDialog.this.dismiss();
			}
        });
        
		mTextViewCancel = (TextView)findViewById(R.id.id_tv_setting_cancel);
		mTextViewCancel.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				UsrInfoBirthdaySettingDialog.this.dismiss();
			}
        });
	}

	/****
	 * 
	 * 
	 * @author Gordon
	 *
	 */
	private int getDayCount() {
		Calendar calendar = Calendar.getInstance(); 
		calendar.clear(); 
		calendar.set(Calendar.YEAR, mCalendarYear); 			//year年
		calendar.set(Calendar.MONTH, mCalendarMonth-1);			//Calendar对象默认一月为0,month月            
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);//本月份的天数
	}
	
	private int getMaxYear() {
		Calendar calendar = Calendar.getInstance(); 
		return calendar.get(Calendar.YEAR);
	}
	
	private void refreshDayWheel() {
        if( mCalendarMonthday > getDayCount() ) mCalendarMonthday = getDayCount();
        mday.setCurrentItem(mCalendarMonthday-1);
        
        mdayAdapter.setMaxValue(getDayCount());
        mday.setViewAdapter(mdayAdapter);
	}
	
	
	/**
	private class DayArrayAdapter extends AbstractWheelTextAdapter {
		private int daysCount = 1;
        Calendar calendar;
        
        protected DayArrayAdapter(Context context, Calendar calendar) {
            super(context, R.layout.birthday_picker_format, NO_RESOURCE);
            this.calendar = calendar;
            
            // Count of days to be shown
            daysCount = getDayCount();
            
            setItemTextResource(R.id.id_tv_usr_birthday_picker_day);
        }
        
        public int getToday() {
            //return daysCount / 2;
        	return getDay()-1;
        }

		@SuppressLint("SimpleDateFormat")
		@Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            //int day = -daysCount/2 + index;
			int day = index;
			
			Log.e(TAG, "index = " + index + " ,day = " + day);
			
            Calendar newCalendar = (Calendar) calendar.clone();
            newCalendar.add(Calendar.DAY_OF_MONTH, day);
            
            View view = super.getItem(index, cachedView, parent);

            TextView monthday = (TextView) view.findViewById(R.id.id_tv_usr_birthday_picker_day);
            DateFormat format = new SimpleDateFormat("dd");
            monthday.setText(format.format(newCalendar.getTime()));
            monthday.setTextColor(0xFF111111);

            DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd");
            view.setTag(dFormat.format(newCalendar.getTime()));
            
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
    /**/
    

}
