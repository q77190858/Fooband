package com.foogeez.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

public class SettingDialog extends AlertDialog {

    TextView mTextViewCancel = null;
    TextView mTextViewConfirm = null;
    
	public SettingDialog(Context context) {
		super(context);
	}
	
	public SettingDialog(Context context, int theme) {
		super(context, theme);
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
