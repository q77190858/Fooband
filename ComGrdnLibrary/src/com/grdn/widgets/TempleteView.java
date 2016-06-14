package com.grdn.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class TempleteView extends View {

	
	public TempleteView(Context context) {
		super(context);
	}

	public TempleteView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public TempleteView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
    protected void onDraw(Canvas canvas) { 
        super.onDraw(canvas); 
        
        
    }
	
	

}
