package com.grdn.widgets;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.grdn.library.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class HistogramChart extends View {

    private Paint mPaint; 
    
    private int mHistogramType = HISTOGRAM_TYPE_FOR_MONTH;
    public static final int HISTOGRAM_TYPE_FOR_DAY = 0;
    public static final int HISTOGRAM_TYPE_FOR_WEEK = 1;
    public static final int HISTOGRAM_TYPE_FOR_MONTH = 2;
    public static final int HISTOGRAM_TYPE_FOR_YEAR = 3;
    public static final int HISTOGRAM_TYPE_FOR_SLEEP_DAY = 4;
    
    private final int COLOR_DEEP_SLEEP = 0xff0080ff;
    private final int COLOR_LIGHT_SLEEP = 0xff80c4ff;
    private final int COLOR_AWAKE = 0xff707173;
    
    
    private int mHistogramColor;
    
    private int mHistogramColumnNum;
    
    private float mHistogramColumnDefaultH;
    
    private float mHistogramColumnBetweenInvert;
    
	private List<Integer> mHistogramColumnValues = null;
	
	private List<Map<String,Integer>> mHistogramSleepColumnValues = null;
	
	private int mTexSize = 28;
	
    private int mXmax;
    
    private int mYmax;
	
	public HistogramChart(Context context) {
		this(context, null); 
	}

	public HistogramChart(Context context, AttributeSet attrs) {
		this(context, attrs, 0); 
	}
	
	public HistogramChart(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr); 
		
		mPaint = new Paint();
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.histogramChart); 
        mHistogramColor = mTypedArray.getColor(R.styleable.histogramChart_histogramColor, 0x60F0E00C); 
        mHistogramColumnNum = mTypedArray.getInteger(R.styleable.histogramChart_histogramColumnNum, 24);
        mHistogramColumnDefaultH = mTypedArray.getDimension(R.styleable.histogramChart_histogramColumnDefaultH, 2);
        mHistogramColumnBetweenInvert = mTypedArray.getDimension(R.styleable.histogramChart_histogramColumnBetween, 1);
        mHistogramType =  mTypedArray.getInt(R.styleable.histogramChart_histogramType, HISTOGRAM_TYPE_FOR_DAY);
        mTypedArray.recycle();
        
        //mHistogramColumnValues = new int[mHistogramColumnNum];
        //for( int i = 0; i < mHistogramColumnNum; i++ ) {
        //	mHistogramColumnValues[i] = 0;//(int)(Math.random()*(mYmax-mHistogramColumnDefaultH));
        //}
        
	}
	
	
    protected void onDraw(Canvas canvas) { 
        super.onDraw(canvas); 
        
        mXmax = getWidth();
        mYmax = getHeight();
        
        Log.e("DEBUG_PULL: ", "" + mYmax);
        
        if( mHistogramType != HISTOGRAM_TYPE_FOR_SLEEP_DAY ) {
        	if( mHistogramColumnValues == null ) return;
        	
        	int columnWidth = (int)((mXmax-mHistogramColumnBetweenInvert*(mHistogramColumnNum-1))/mHistogramColumnNum);
            int actualX = (int) ((columnWidth+mHistogramColumnBetweenInvert)*mHistogramColumnNum-mHistogramColumnBetweenInvert);
            int startX = (mXmax - actualX)/2;
        	
	        mPaint.setStrokeWidth(columnWidth);
	        mPaint.setAntiAlias(true);
	        mTexSize = (int) (mYmax/10);
	        
	    	float w = mHistogramColumnBetweenInvert+columnWidth;
	    	int H = mYmax-1-mTexSize*2;									// 定义最大柱状高度		
	    	int Husr = H*93/100;
	        for( int i = 0; i < mHistogramColumnNum; i++ ) {
	        	int value = mHistogramColumnValues.get(i);
	        	if( value < 0 ) {
	        		value = 0;
	        		mPaint.setColor(mHistogramColor);
	        	}
	        	if( value > 2000 ) {
	        		if( value > 12000 ) value = 12000;
	        		int colorInc = (value-2000)*(0xff-((mHistogramColor&0x00ff0000)>>16))/10000;
	        		int colorDec = (value-2000)*((mHistogramColor&0x0000ff00)>>8)/10000;
	        		//Log.e("HistogramChart", "colorDec: " + Integer.toHexString((colorDec<<8)));
	        		//Log.e("HistogramChart", "colorInc: " + Integer.toHexString((colorInc<<16)));
	        		//Log.e("HistogramChart", "mHistogramColor: " + Integer.toHexString(mHistogramColor));
	        		mPaint.setColor(mHistogramColor + (colorInc<<16) - (colorDec<<8));
	        		value = 2000;										// 约束实际数据的高度不要超过最大高度	
	        	}
	        	else {
	        		mPaint.setColor(mHistogramColor);
	        	}
	        	float h = value*(Husr-mHistogramColumnDefaultH)/2000+mHistogramColumnDefaultH;	
	        	
	        	canvas.drawLine(startX+w/2+w*i, H,
	        					startX+w/2+w*i, H-h,
	        					mPaint);
	        }
	        
	        mPaint.setStrokeWidth(0);
	        mPaint.setColor(0xff000000);
	        mPaint.setTextSize(mTexSize);
	        mPaint.setTypeface(Typeface.DEFAULT_BOLD); 						//设置字体 	
	        
	        for( int i = 0; i < mHistogramColumnNum; i++ ) {
	        	
	        	int datPlus = 0;
	        	String strPlus = "";
	        	switch( mHistogramType ) {
	        		case HISTOGRAM_TYPE_FOR_DAY:
	        			datPlus = 0;
	        			strPlus = "h";
	        			break;
	        		case HISTOGRAM_TYPE_FOR_WEEK:
	        			datPlus = 1;
	        			strPlus = "d";
	        			break;
	        		case HISTOGRAM_TYPE_FOR_MONTH:
	        			datPlus = 1;
	        			strPlus = "d";
	        			break;
	        		case HISTOGRAM_TYPE_FOR_YEAR:
	        			datPlus = 1;
	        			strPlus = "m";
	        			break;
	        	}
	        	
	        	int displayNumber = i + datPlus;
	        	float tw = mPaint.measureText(displayNumber + strPlus);
	        	
	        	if( mHistogramColumnNum > 12 ) {
	        		if( mHistogramColumnNum % 2 == 1 ) {
		        		if( i%2 == 0 ) {
		        			canvas.drawText(displayNumber+strPlus, startX+w/2+w*i-tw/2, mYmax-1-mTexSize, mPaint); 			
		        		}
	        		}
	        		else {
		        		if( i%2 == 0 ) {
		        			canvas.drawText(displayNumber+strPlus, startX+w/2+w*i-tw/2, mYmax-1-mTexSize, mPaint); 			
		        		}
	        		}
	        	}
	        	else {
	        		canvas.drawText(displayNumber+strPlus, startX+w/2+w*i-tw/2, mYmax-1-mTexSize, mPaint);
	        	}
	        }
        }
        else {
        	if( mHistogramSleepColumnValues == null ) return;
        	
        	mTexSize = mYmax/10;
        	int H = mYmax-1-mTexSize*2;
        	
        	int sX = 9;
        	int eX = mXmax-sX;
        	
        	if( mHistogramSleepColumnValues.size() > 2 ) {
	        	Map<String, Integer> Smap = mHistogramSleepColumnValues.get(0);
	        	int S_UTC = Smap.get("utc");
	        	Map<String, Integer> Emap = mHistogramSleepColumnValues.get(mHistogramSleepColumnValues.size()-1);
	        	int E_UTC = Emap.get("utc");
	        
	        	int StartX = sX;
	        	for( int i = 1; i < mHistogramSleepColumnValues.size(); i++ ) {
	        		Map<String, Integer> Pmap = mHistogramSleepColumnValues.get(i-1);
	        		Map<String, Integer> Cmap = mHistogramSleepColumnValues.get(i);
	        		int diffUtc = Cmap.get("utc") - Pmap.get("utc");
	        		int width = diffUtc*(eX-sX)/(E_UTC-S_UTC);
	        		
	        		int color = 0xff000000;
	        		int height = 1;
	        		if( Pmap.get("status") == 134 ) {
	        			if( diffUtc >= 5*60 ) {
		        			color = COLOR_DEEP_SLEEP;
		        			height = H*70/100;
	        			}
	        			else {
		        			color = COLOR_LIGHT_SLEEP;
		        			height = H*50/100;
	        			}
	        		}
	        		else if( Pmap.get("status") == 133 ) {
	        			color = COLOR_LIGHT_SLEEP;
	        			height = H*50/100;
	        		}
	        		else {
	        			color = COLOR_AWAKE;
	        			height = H*40/100;
	        		}
	        		
	        		//Log.e("HistogramChart", "HISTOGRAM[" + i + "]: StartX = " + StartX + ", EndX = " + (StartX+width) + ", Height = " + height);
	        		//int EndX = StartX + width;
	        		if( i == (mHistogramSleepColumnValues.size()-1) ) {
	        			drawSleepBar( StartX, eX, height, H, color, canvas );
	        		}
	        		else {
		        		drawSleepBar( StartX, StartX+width, height, H, color, canvas );
		        		StartX += width;
	        		}
	        	}
	        	
	        	mPaint.setStrokeWidth(1);
	        	mPaint.setColor(0xff000000);
	            canvas.drawLine( sX-1, H+mTexSize, sX-1, H-H*70/100, mPaint );
	            
	        	mPaint.setStrokeWidth(1);
	        	mPaint.setColor(0xff000000);
	            canvas.drawLine( eX+1, H+mTexSize, eX+1, H-H*70/100, mPaint );
	            
	            String sStr = utc2DateTimeString(mHistogramSleepColumnValues.get(0).get("utc"), "HH:mm", TimeZone.getDefault());
		        mPaint.setAntiAlias(true);
		        mPaint.setStrokeWidth(0);
		        mPaint.setColor(0xff000000);
		        mPaint.setTextSize(mTexSize);
		        mPaint.setTypeface(Typeface.DEFAULT_BOLD); 						//设置字体 	
	            canvas.drawText(sStr, sX+3, H+mTexSize, mPaint);
	            
	            String eStr = utc2DateTimeString(mHistogramSleepColumnValues.get(mHistogramSleepColumnValues.size()-1).get("utc"), "HH:mm", TimeZone.getDefault());
	            float txtWidth = mPaint.measureText(eStr);
	            canvas.drawText(eStr, eX-txtWidth-3, H+mTexSize, mPaint);
        	}
        }
        
    	mPaint.setStrokeWidth(2);
    	mPaint.setColor(0xff000000);
        canvas.drawLine( 1, mYmax-1, mXmax+1, mYmax-1, mPaint );
    }
    
	public static String utc2DateTimeString( int seconds, String format, TimeZone tz ) {
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
		sdf.setTimeZone(tz);
		return sdf.format(new Date(((long)seconds)*1000));
	}
    
    private void drawSleepBar( int startX, int endX, int height, int limitH, int color, Canvas canvas) {
    	mPaint.setStrokeWidth(height);
        mPaint.setColor(color);
    	mPaint.setAntiAlias(true);
    	canvas.drawLine(startX, limitH-height/2, endX, limitH-height/2, mPaint);
    }
    
    public void setHistogramType( int type ) {
    	mHistogramType = type;
    }
    
    public void setHistogramValues( List<Integer> values ) {
    	mHistogramColumnValues = values;
    	invalidate();
    }
    
    public void setHistogramSleepValues( List<Map<String,Integer>> values ) {
    	mHistogramType = HISTOGRAM_TYPE_FOR_SLEEP_DAY;
    	mHistogramSleepColumnValues = values;
    	invalidate();
    }
	
	

}
