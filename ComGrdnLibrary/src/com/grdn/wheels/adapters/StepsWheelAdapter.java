package com.grdn.wheels.adapters;

import android.content.Context;

/**
 * @author Robin
 * @time 20150921
 * @function 自定义适配器---跑步的步数
 */
public class StepsWheelAdapter extends AbstractWheelTextAdapter{

	/** The default min value */
    public static final int DEFAULT_MAX_VALUE = 9;

    /** The default max value */
    private static final int DEFAULT_MIN_VALUE = 0;
    
    // Values
    private int minValue ;
    private int maxValue ;
    
    // format
    private String format;
    
    int[] steps = {1000,3000,5000,7000,9000,10000,20000,30000,40000,50000,60000,70000,80000,90000,100000};
    
    /**
     * Constructor
     * @param context the current context
     */
    public StepsWheelAdapter(Context context) {
        this(context, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    /**
     * Constructor
     * @param context the current context
     * @param minValue the spinnerwheel min value
     * @param maxValue the spinnerwheel max value
     */
    public StepsWheelAdapter(Context context, int minValue, int maxValue) {
        this(context, minValue, maxValue, null);
    }

    /**
     * Constructor
     * @param context the current context
     * @param minValue the spinnerwheel min value
     * @param maxValue the spinnerwheel max value
     * @param format the format string
     */
    public StepsWheelAdapter(Context context, int minValue, int maxValue, String format) {
        super(context);
        
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.format = format;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
        notifyDataInvalidatedEvent();
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        notifyDataInvalidatedEvent();
    }

    @Override
    public CharSequence getItemText(int index) {
        if (index >= 0 && index < getItemsCount()) {
        	int value = steps[index];
            return format != null ? String.format(format, value) : Integer.toString(value);
        }
        return null;
    }

    @Override
    public int getItemsCount() {
    	return steps.length;
    }    
}
