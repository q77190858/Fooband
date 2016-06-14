package com.foogeez.dialog;

import com.foogeez.configuration.Configuration.UsrInfoConfiguration;
import com.foogeez.fooband.R;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class UsrInfoSexySettingDialog extends SettingDialog {
	
    Context context;
	private UsrInfoConfiguration mConfig = null;
	private OnConfirmListener listener = null; 
	
	public UsrInfoSexySettingDialog(Context context, UsrInfoConfiguration config) {
		super(context);
		mConfig = config;
        this.context = context;
	}
	
    public UsrInfoSexySettingDialog(Context context, UsrInfoConfiguration config, int theme) {
		super(context, theme);
		mConfig = config;
        this.context = context;
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
        this.setContentView(R.layout.dialog_setting_usr_info_sexy);
        
        ((ImageView)findViewById(R.id.usr_info_sexy)).setOnClickListener(new ImageView.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateSexy();
			}
        });
        
        initSexy();
        initYesNo();
    }
    
    private void initSexy() {
		if( mConfig.getSexy() == 0 ) {
			((ImageView)findViewById(R.id.usr_info_sexy)).setImageResource(R.drawable.sex_changewoman);
		}
		else {
			((ImageView)findViewById(R.id.usr_info_sexy)).setImageResource(R.drawable.sex_changeman);
		}
    }
    
    private void updateSexy() {
		if( mConfig.getSexy() == 0 ) {
			mConfig.setSexy(1);
			((ImageView)findViewById(R.id.usr_info_sexy)).setImageResource(R.drawable.sex_changeman);
		}
		else {
			mConfig.setSexy(0);
			((ImageView)findViewById(R.id.usr_info_sexy)).setImageResource(R.drawable.sex_changewoman);
		}
    }
    
	private void initYesNo() {
		mTextViewConfirm = (TextView)findViewById(R.id.id_tv_setting_confirm);
		mTextViewConfirm.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( listener != null )
					listener.OnConfirm(mConfig);
				UsrInfoSexySettingDialog.this.dismiss();
			}
        });
        
		mTextViewCancel = (TextView)findViewById(R.id.id_tv_setting_cancel);
		mTextViewCancel.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				UsrInfoSexySettingDialog.this.dismiss();
			}
        });
	}
	
}
