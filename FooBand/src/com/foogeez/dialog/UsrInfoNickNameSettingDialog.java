package com.foogeez.dialog;

import java.util.Calendar;

import com.foogeez.configuration.Configuration.UsrInfoConfiguration;
import com.foogeez.fooband.R;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public class UsrInfoNickNameSettingDialog extends SettingDialog {
    private final static String TAG = CalendarSettingDialog.class.getSimpleName();
    
    private Context context;
    private EditText mEditTextNickName = null;
	
	private OnConfirmListener listener = null; 
	private UsrInfoConfiguration mConfig = null;
	
	public UsrInfoNickNameSettingDialog(Context context, UsrInfoConfiguration config) {
		super(context);
		this.context = context;
		mConfig = config;
	}
	
    public UsrInfoNickNameSettingDialog(Context context, UsrInfoConfiguration config, int theme) {
		super(context,theme);
        this.context = context;
		mConfig = config;
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
        this.setContentView(R.layout.dialog_setting_nickname);
        
        
        initYesNo();
        initNickName();
        
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);	
	}

	private void initNickName() {
		mEditTextNickName = ((EditText)findViewById(R.id.id_et_usr_info_nickname));
		mEditTextNickName.setText(mConfig.getNickName());
		mEditTextNickName.clearFocus();		
	}

	private void initYesNo() {

		mTextViewConfirm = (TextView)findViewById(R.id.id_tv_setting_confirm);
		mTextViewConfirm.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				mConfig.setNickName(mEditTextNickName.getText().toString());
				if( listener != null )
					listener.OnConfirm(mConfig);
				UsrInfoNickNameSettingDialog.this.dismiss();
			}
        });
        
		mTextViewCancel = (TextView)findViewById(R.id.id_tv_setting_cancel);
		mTextViewCancel.setOnClickListener(new TextView.OnClickListener() {
			@Override
			public void onClick(View v) {
				UsrInfoNickNameSettingDialog.this.dismiss();
			}
        });
		
		
	}
    
    
    
	
}
