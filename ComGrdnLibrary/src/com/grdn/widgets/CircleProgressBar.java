package com.grdn.widgets;

import com.grdn.library.R;
import com.grdn.util.DrawableUtil;

import android.annotation.SuppressLint;
import android.content.Context; 
import android.content.res.TypedArray; 
import android.graphics.Bitmap;
import android.graphics.Canvas; 
import android.graphics.Color; 
import android.graphics.Paint; 
import android.graphics.RectF; 
import android.graphics.Typeface; 
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet; 
import android.util.Log; 
import android.view.View; 
 

/**
* 仿iphone带进度的进度条，线程安全的View，可直接在线程中更新进度
* @author xiaanming
*
*/ 
public class CircleProgressBar extends View { 
    /**
     * 画笔对象的引用
     */ 
    private Paint paint; 
     
    /**
     * 圆环的颜色
     */ 
    private int roundColor; 
     
    /**
     * 圆环进度的颜色
     */ 
    private int roundProgressColor; 
     
    /**
     * 中间进度百分比的字符串的颜色
     */ 
    private int textColor; 
     
    /**
     * 中间进度百分比的字符串的字体
     */ 
    private float textSize; 
     
    /**
     * 圆环的宽度
     */ 
    private float roundWidth; 
     
    /**
     * 最大进度
     */ 
    private int max; 
     
    /**
     * 当前进度
     */ 
    private int progress; 
    /**
     * 是否显示中间的进度
     */ 
    private boolean textIsDisplayable; 
    
    /**
     * 中间图标
     */
    private Drawable icon;
     
    /**
     * 进度的风格，实心或者空心
     */ 
    private int style; 
     
    public static final int STROKE = 0; 
    public static final int FILL = 1; 
    
    
    private int type;
    
    public static final int SPORT = 0;
    public static final int SLEEP = 1;
    
    
     
    public CircleProgressBar(Context context) { 
        this(context, null); 
    } 
 
    public CircleProgressBar(Context context, AttributeSet attrs) { 
        this(context, attrs, 0); 
    } 
     
    public CircleProgressBar(Context context, AttributeSet attrs, int defStyle) { 
        super(context, attrs, defStyle); 
         
        paint = new Paint(); 
 
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.circleProgressBar); 
         
        //获取自定义属性和默认值 
        roundColor = mTypedArray.getColor(R.styleable.circleProgressBar_circleColor, 0x800000FF); 
        roundProgressColor = mTypedArray.getColor(R.styleable.circleProgressBar_circleProgressColor, Color.GREEN); 
        textColor = mTypedArray.getColor(R.styleable.circleProgressBar_textColor, Color.GREEN); 
        textSize = mTypedArray.getDimension(R.styleable.circleProgressBar_textSize, 15); 
        roundWidth = mTypedArray.getDimension(R.styleable.circleProgressBar_circleWidth, 5); 
        progress = mTypedArray.getInteger(R.styleable.circleProgressBar_progress, 40); 
        max = mTypedArray.getInteger(R.styleable.circleProgressBar_max, 100); 
        textIsDisplayable = mTypedArray.getBoolean(R.styleable.circleProgressBar_textIsDisplayable, true); 
        style = mTypedArray.getInt(R.styleable.circleProgressBar_style, STROKE); 
        type = mTypedArray.getInt(R.styleable.circleProgressBar_type, SPORT); 
        icon = mTypedArray.getDrawable(R.styleable.circleProgressBar_icon);
        mTypedArray.recycle(); 
    } 
     
 
    @SuppressLint("DrawAllocation")
	@Override 
    protected void onDraw(Canvas canvas) { 
        super.onDraw(canvas); 
         
        /**
         * 画最外层的大圆环
         */ 
        int centre = getWidth()/2; 					//获取圆心的x坐标 
        int radius = (int) (centre*5/6 - roundWidth/2); //圆环的半径 
        paint.setColor(roundColor); 				//设置圆环的颜色 
        paint.setStyle(Paint.Style.STROKE); 		//设置空心 
        paint.setStrokeWidth(roundWidth); 			//设置圆环的宽度 
        paint.setAntiAlias(true);  					//消除锯齿  
        
        RectF oval = new RectF(centre-radius, centre-radius, centre+radius, centre+radius);  //用于定义的圆弧的形状和大小的界限 
        //canvas.drawCircle(centre, centre, radius, paint); //画出圆环 
        for( int i = 0; i < 360; i+=2 ) {
        	canvas.drawArc(oval, i, 1, false, paint);
        }
         
        Log.e("log", centre + ""); 
         
        /**
         * 画进度百分比
         */ 
        paint.setStrokeWidth(0);  
        paint.setColor(textColor); 
        paint.setTextSize(textSize); 
        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC);
        paint.setTypeface(font); 						//设置字体 
        
        int percent = (int)(((float)progress / (float)max) * 100);  	//中间的进度百分比，先转换成float在进行除法运算，不然都为0 

        float textWidth = paint.measureText(percent + "%");   			//测量字体宽度，我们需要根据字体的宽度设置在圆环中间 
        if(textIsDisplayable && percent >= 0 && style == STROKE){ 
        	canvas.drawText(percent + "%", centre - textWidth / 2, centre + textSize, paint); //画出进度百分比 
        }
        
        paint.setStrokeWidth(4);
        canvas.drawLine(getWidth()/4, centre, getWidth()*3/4, centre, paint);
        
        paint.setStrokeWidth(0);
        paint.setAlpha(120);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        BitmapDrawable bd = (BitmapDrawable)icon;
        RectF local = new RectF(centre-radius/3, centre-radius/3-radius*0.55f, centre+radius/3, centre+radius/3-radius*0.55f);
        
        Bitmap bitMapIcon = DrawableUtil.getAlphaBitmap(bd.getBitmap(), 0xff000000);
        
        canvas.drawBitmap(bitMapIcon, null, local, paint);
        
        if( type == SPORT ) {
	        paint.setStrokeWidth(0);  
	        paint.setColor(textColor); 
	        paint.setTextSize(textSize); 
	        float textWidthNumber = paint.measureText(progress+"");
	        
	        paint.setStrokeWidth(0);  
	        paint.setColor(textColor); 
	        paint.setTextSize(textSize/2); 
	        float textWidthAscii = paint.measureText(getResources().getString(R.string.string_steps));
	        
	        float textWidthSport = textWidthNumber + textWidthAscii;
	        
	        paint.setStrokeWidth(0);  
	        paint.setColor(textColor); 
	        paint.setTextSize(textSize); 
	        if(textIsDisplayable && percent >= 0 && style == STROKE){ 
	        	canvas.drawText(progress+"", centre - textWidthSport/2, centre-textSize/4, paint); //画出进度百分比
	        }
	        
	        paint.setStrokeWidth(0);  
	        paint.setColor(textColor); 
	        paint.setTextSize(textSize/2); 
	        if(textIsDisplayable && percent >= 0 && style == STROKE) { 
	        	canvas.drawText(getResources().getString(R.string.string_steps), centre - textWidthSport/2 + textWidthNumber, centre-textSize/4, paint); //画出进度百分比
	        }
	        
	        paint.setStrokeWidth(0);
	        paint.setTextSize(textSize/2);
	        String format = getResources().getString(R.string.string_sport_target);
	        float targetWidth = paint.measureText(String.format(format, max));
	        canvas.drawText(String.format(format, max), centre - targetWidth / 2, centre + textSize + textSize*2/3, paint); //画出进度百分比 
        }
        else {
        	if( progress >= 60 ) {
        		
    	        paint.setStrokeWidth(0);  
    	        paint.setColor(textColor); 
    	        paint.setTextSize(textSize); 
    	        float textWidthSleepTimeHour = paint.measureText(progress/60+"");
    	        
    	        paint.setStrokeWidth(0);  
    	        paint.setColor(textColor); 
    	        paint.setTextSize(textSize/2); 
    	        float textWidthSleepTimeHourUnit = paint.measureText(getResources().getString(R.string.string_hours));
    	        
    	        paint.setStrokeWidth(0);  
    	        paint.setColor(textColor); 
    	        paint.setTextSize(textSize); 
    	        float textWidthSleepTimeMinute = paint.measureText(progress%60+"");
    	        
    	        paint.setStrokeWidth(0);  
    	        paint.setColor(textColor); 
    	        paint.setTextSize(textSize/2); 
    	        float textWidthSleepTimeMinuteUnit = paint.measureText(getResources().getString(R.string.string_short_minutes));
    	        
    	        float textWidthSleep = textWidthSleepTimeHour +
    	        					   textWidthSleepTimeHourUnit +
    	        					   textWidthSleepTimeMinute +
    	        					   textWidthSleepTimeMinuteUnit;
    	        
    	        paint.setStrokeWidth(0);  
    	        paint.setColor(textColor); 
    	        paint.setTextSize(textSize); 
    	        if(textIsDisplayable && percent >= 0 && style == STROKE){ 
    	        	canvas.drawText(progress/60+"", centre - textWidthSleep/2, 
    	        			centre-textSize/4, paint); //画出进度百分比
    	        }
    	        
    	        paint.setStrokeWidth(0);  
    	        paint.setColor(textColor); 
    	        paint.setTextSize(textSize/2); 
    	        if(textIsDisplayable && percent >= 0 && style == STROKE) { 
    	        	canvas.drawText(getResources().getString(R.string.string_hours), centre - textWidthSleep/2 + textWidthSleepTimeHour, 
    	        			centre-textSize/4, paint); //画出进度百分比
    	        }
    	        
    	        paint.setStrokeWidth(0);  
    	        paint.setColor(textColor); 
    	        paint.setTextSize(textSize); 
    	        if(textIsDisplayable && percent >= 0 && style == STROKE){ 
    	        	canvas.drawText(progress%60+"", centre - textWidthSleep/2 + textWidthSleepTimeHour + textWidthSleepTimeHourUnit, 
    	        			centre-textSize/4, paint); //画出进度百分比
    	        }
    	        
    	        paint.setStrokeWidth(0);  
    	        paint.setColor(textColor); 
    	        paint.setTextSize(textSize/2); 
    	        if(textIsDisplayable && percent >= 0 && style == STROKE) { 
    	        	canvas.drawText(getResources().getString(R.string.string_short_minutes), centre - textWidthSleep/2 + textWidthSleepTimeHour + textWidthSleepTimeHourUnit + textWidthSleepTimeMinute, 
    	        			centre-textSize/4, paint); //画出进度百分比
    	        }
        	}
        	else {
        		
    	        paint.setStrokeWidth(0);  
    	        paint.setColor(textColor); 
    	        paint.setTextSize(textSize); 
    	        float textWidthSleepTimeMinute = paint.measureText(progress%60+"");
    	        
    	        paint.setStrokeWidth(0);  
    	        paint.setColor(textColor); 
    	        paint.setTextSize(textSize/2); 
    	        float textWidthSleepTimeMinuteUnit = paint.measureText(getResources().getString(R.string.string_minutes));
    	        
    	        float textWidthSleep = textWidthSleepTimeMinute + textWidthSleepTimeMinuteUnit;
    	        
    	        paint.setStrokeWidth(0);  
    	        paint.setColor(textColor); 
    	        paint.setTextSize(textSize); 
    	        if(textIsDisplayable && percent >= 0 && style == STROKE){ 
    	        	canvas.drawText(progress%60+"", centre - textWidthSleep/2, 
    	        			centre-textSize/4, paint); //画出进度百分比
    	        }
    	        
    	        paint.setStrokeWidth(0);  
    	        paint.setColor(textColor); 
    	        paint.setTextSize(textSize/2); 
    	        if(textIsDisplayable && percent >= 0 && style == STROKE) { 
    	        	canvas.drawText(getResources().getString(R.string.string_minutes), centre - textWidthSleep/2 + textWidthSleepTimeMinute, 
    	        			centre-textSize/4, paint); //画出进度百分比
    	        }
        	}
        	
	        paint.setStrokeWidth(0);
	        paint.setTextSize(textSize/2);
	        String format = getResources().getString(R.string.string_sleep_target);
	        float targetWidth = paint.measureText(String.format(format, max));
	        canvas.drawText(String.format(format, max/60), centre - targetWidth / 2, centre + textSize + textSize*2/3, paint); //画出进度百分比 
        }
        
        /**
         * 画圆弧 ，画圆环的进度
         */ 
         
        //设置进度是实心还是空心 
        paint.setStrokeWidth(roundWidth); //设置圆环的宽度 
        paint.setColor(roundProgressColor);  //设置进度的颜色 

        switch (style) { 
	        case STROKE:{ 
	            paint.setStyle(Paint.Style.STROKE); 
	            //canvas.drawArc(oval, 0, 360 * progress / max, false, paint);  //根据进度画圆弧 
	            int round = 360*progress/max;
	            if( (round/360) > 1 ) round = 360 + round%360;
	            for( int i = 270; i < 270+round; i+=2 ) {
	            	canvas.drawArc(oval, i, 1, false, paint);
	            }
	            break; 
	        } 
	        case FILL:{ 
	            paint.setStyle(Paint.Style.FILL_AND_STROKE); 
	            if(progress !=0) 
	                canvas.drawArc(oval, 0, 360 * progress / max, true, paint);  //根据进度画圆弧 
	            break; 
	        } 
        }

         
    } 
     
     
    public //synchronized 
    int getMax() { 
        return max; 
    } 
 
    /**
     * 设置进度的最大值
     * @param max
     */ 
    public //synchronized 
    void setMax(int max) { 
        if(max < 0){ 
            throw new IllegalArgumentException("max not less than 0"); 
        } 
        this.max = max; 
    } 
 
    /**
     * 获取进度.需要同步
     * @return
     */ 
    public //synchronized 
    int getProgress() { 
        return progress; 
    } 
 
    /**
     * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
     * 刷新界面调用postInvalidate()能在非UI线程刷新
     * @param progress
     */ 
    public //synchronized 
    void setProgress(int progress) { 
        if(progress < 0){ 
            throw new IllegalArgumentException("progress not less than 0"); 
        } 
        //if(progress > max){ 
        //    progress = max; 
        //} 
        //if(progress <= max){ 
            this.progress = progress; 
            //Log.e("setProress", "progress = " + progress);
            //postInvalidate(); 
            invalidate();
        //} 
    } 
     
     
    public int getCricleColor() { 
        return roundColor; 
    } 
 
    public void setCricleColor(int cricleColor) { 
        this.roundColor = cricleColor; 
    } 
 
    public int getCricleProgressColor() { 
        return roundProgressColor; 
    } 
 
    public void setCricleProgressColor(int cricleProgressColor) { 
        this.roundProgressColor = cricleProgressColor; 
    } 
 
    public int getTextColor() { 
        return textColor; 
    } 
 
    public void setTextColor(int textColor) { 
        this.textColor = textColor; 
    } 
 
    public float getTextSize() { 
        return textSize; 
    } 
 
    public void setTextSize(float textSize) { 
        this.textSize = textSize; 
    } 
 
    public float getRoundWidth() { 
        return roundWidth; 
    } 
 
    public void setRoundWidth(float roundWidth) { 
        this.roundWidth = roundWidth; 
    } 
 
 
 
} 
