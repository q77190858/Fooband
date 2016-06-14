package com.foogeez.activity;

import com.foogeez.fooband.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class SettingActivity extends Activity {
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
    
    @Override 
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_BACK) {
    		UIfinish();
    		return true;
    	}
    	else {
    		return super.onKeyDown(keyCode, event);
    	}
    }
    
	public void UIfinish() {
		finish();
		overridePendingTransition( R.anim.translate_slide_in_left, R.anim.translate_slide_out_right );
	}
	
	public void initSettingTitle(int id) {
		((TextView)findViewById(R.id.id_tv_setting_title)).setText(id);
		((ImageView)findViewById(R.id.id_iv_setting_back)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				UIfinish();
			}
		});
	}
	
}
