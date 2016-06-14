package com.foogeez.activity;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.foogeez.configuration.LocalStorage;
import com.foogeez.dialog.ConfigRspDialog;
import com.foogeez.fooband.R;
import com.foogeez.https.Urls;
import com.foogeez.network.NetworkUtils;
import com.foogeez.network.NetworkUtils.NetworkCallback;
import com.foogeez.services.CentralService;
import com.grdn.util.RuntimeCmdManager;
import com.grdn.util.Utils;
import com.umeng.analytics.MobclickAgent;

public class LoginActivity extends SettingActivity {
    private final static String TAG = LoginActivity.class.getSimpleName();
    private final static int REQUEST_CODE_GET_ACCOUNT = 1;

    private TextView mUserNotice;
    private TextView mUserLogin;
    private TextView mUserRegister;

    private EditText mUserAccount;
    private EditText mUserPassword;

    private String mUserAccoutDefaultString;
    private String mUserPasswordDefaultString;

    private String mUserAccountString;
    private String mUserPasswordString;

    private LocalStorage mLocalStorage;
    private ConfigRspDialog mLoginDoingDialog;
    
    private ImageView mImageViewWelcome;
    
    /** Name:Robin  Time:20150925  Function:发送请求数据的等待过程*/
    private ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserNotice = (TextView) findViewById(R.id.id_tv_user_notice);
        String notice = getResources().getString(R.string.string_user_login_notice);
        String content = getResources().getString(R.string.string_user_notice_content);
        
        mImageViewWelcome = (ImageView) findViewById(R.id.id_iv_welcome_login);
//        //改变图片颜色
//        DrawableUtil.setImageViewColor(mImageViewWelcome, getResources().getColor(R.color.logo_blue_change));
        
        /**Robin 20150918 --> 判断为汉语时，协议的颜色*/
//        String locale = Locale.getDefault().getLanguage();
//        if (locale.equals("zh")) {
        	 mUserNotice.setText(Html.fromHtml(notice + "<font color=\"#0077C2\">" + content + "</font>"));
//		}else {
//			mUserNotice.setText(Html.fromHtml(notice + "<font color=\"#3498db\">" + content + "</font>"));
//		}      
        mUserNotice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog();
            }
        });

        mUserLogin = (TextView) findViewById(R.id.id_tv_login);
        mUserLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((!Utils.isEmail(mUserAccountString)) && (!Utils.isPhone(mUserAccountString))) {
                    new ConfigRspDialog(LoginActivity.this, getResources().getString(
                            R.string.string_login_account_error)).show(1000);
                    return;
                }
                if ((mUserPasswordString.length() < 6) || (mUserPasswordString.length() > 16)) {
                    new ConfigRspDialog(LoginActivity.this, getResources().getString(
                            R.string.string_login_password_error)).show(1000);
                    return;
                }

                new NetworkUtils(LoginActivity.this, new NetworkCallback() {
                    @Override
                    public void OnPreExcute() {
                    	progressDialog = ProgressDialog.show(LoginActivity.this, "",
                    			getResources().getString(R.string.string_login_doing),true);
                    	
//                        mLoginDoingDialog = new ConfigRspDialog(LoginActivity.this, getResources().getString(
//                                R.string.string_login_doing));
//                        mLoginDoingDialog.show(-1);
                    }

                    @Override
                    public void OnExcuteSuccess(JSONObject obj) throws JSONException {
                    	progressDialog.dismiss();
                        Log.e(TAG, "login success!");
                        mLocalStorage.saveAccount(mUserAccountString);
                        mLocalStorage.savePassword(mUserPasswordString);
                        
//                        Log.e(TAG, "login success!-------" + mUserPasswordString);
                        
                        mLocalStorage.saveUID(obj.getString("uid"));
                        mLocalStorage.saveSID(obj.getString("sid"));
//                        mLoginDoingDialog.cancel();
                        new ConfigRspDialog(LoginActivity.this, getResources().getString(R.string.string_login_success))
                                .show(1000);

                        enterMainActivity();
                    }

                    @Override
                    public void OnExcuteFailure(JSONObject obj) {
                    	progressDialog.dismiss();
                        Log.e(TAG, "login failure!");
                        
                        int errorCode = 500;
                        try {
							errorCode = obj.getInt("rs");
						} 
                        catch (JSONException e) {
							e.printStackTrace();
						}
                        
                        if( errorCode == 500 ) {
	                        new ConfigRspDialog(LoginActivity.this, getResources().getString(R.string.string_newwork_error))
                            .show(2000);
                        }
                        else {
	                        new ConfigRspDialog(LoginActivity.this, getResources().getString(R.string.string_login_error))
                            .show(2000);
                        }
                    }

                    @Override
                    public void OnTimeOut() {
                    	progressDialog.dismiss();
                        Log.e(TAG, "login timeout!");
                        new ConfigRspDialog(LoginActivity.this, getResources().getString(
                                R.string.string_network_timeout)).show(2000);
                    }
                }).request(NetworkUtils.POST, Urls.USER_LOGIN, mUserAccountString, Utils.md5(mUserPasswordString));

            }
        });

        mUserRegister = (TextView) findViewById(R.id.id_tv_register);
        mUserRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                enterRegisterActivity();
            }
        });

        Button passwordRecoverd = (Button) findViewById(R.id.tv_password_recoverd);
        passwordRecoverd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                enterPasswdRecoverdActivity();
            }
        });

        mLocalStorage = new LocalStorage(LoginActivity.this);

        initAccount();
        initPassword();
        
    }

    private void initAccount() {
        mUserAccountString = mLocalStorage.getAccount();
        mUserAccount = (EditText) findViewById(R.id.id_et_account);
        mUserAccoutDefaultString = mUserAccount.getText().toString();

        mUserAccount.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        if (mUserAccountString.length() != 0) {
            mUserAccount.setTextColor(getResources().getColor(R.color.black));
            mUserAccount.setText(mUserAccountString);
            mUserAccount.setSelection(mUserAccountString.length());
        } else {
            mUserAccount.setTextColor(getResources().getColor(R.color.gray));
            mUserAccount.setText(mUserAccoutDefaultString);
        }

        mUserAccount.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (mUserAccountString.length() == 0) {
                        mUserAccount.setTextColor(getResources().getColor(R.color.gray));
                        mUserAccount.setText(mUserAccoutDefaultString);
                    }
                }
                else {
                    mUserAccount.setTextColor(getResources().getColor(R.color.black));
                    if (mUserAccountString.length() == 0) {
                        mUserAccount.setText("");
                    }
                }
            }
        });

        mUserAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(mUserAccoutDefaultString)) {
                    mUserAccountString = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initPassword() {
        mUserPasswordString = mLocalStorage.getPassword();

        mUserPassword = (EditText) findViewById(R.id.id_et_password);
        mUserPasswordDefaultString = mUserPassword.getText().toString();

        if (mUserPasswordString.length() != 0) {
            mUserPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
            mUserPassword.setText(mUserPasswordString);
            mUserPassword.setTextColor(getResources().getColor(R.color.black));
            mUserPassword.setSelection(mUserPasswordString.length());
        } else {
            mUserPassword.setInputType(InputType.TYPE_CLASS_TEXT);
            mUserPassword.setTextColor(getResources().getColor(R.color.gray));
            mUserPassword.setText(mUserPasswordDefaultString);
        }

        mUserPassword.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (mUserPasswordString.length() == 0) {
                        mUserPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                        mUserPassword.setTextColor(getResources().getColor(R.color.gray));
                        mUserPassword.setText(mUserPasswordDefaultString);
                    }
                }
                else {
                    Log.e(TAG, "input type: " + "InputType.TYPE_TEXT_VARIATION_PASSWORD");
                    mUserPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    mUserPassword.setTextColor(getResources().getColor(R.color.black));
                    if (mUserPasswordString.length() == 0) {
                        mUserPassword.setText("");
                    }
                }
            }
        });

        mUserPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(mUserPasswordDefaultString)) {
                    mUserPasswordString = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    @SuppressLint("DefaultLocale")
    public void dialog() {
        final WebView mWebView = new WebView(this);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        int lang = 0;
        String lan = Locale.getDefault().toString();
        if (lan.equals("zh_CN")) {
            lang = 1;
        }
        else if (lan.equals("zh_TW")) {
            lang = 2;
        }
        else {
            lang = 3;
        }
        String registersign = String.format(Urls.REG_SIGN, lang);
        mWebView.loadUrl(registersign);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(mWebView)
                .setPositiveButton(getResources()
                        .getString(R.string.string_dialog_positive),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
        builder.show();
    }

    private void enterRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.translate_slide_in_right, R.anim.translate_slide_out_left);
        finish();
    }

    private void enterPasswdRecoverdActivity() {
        Intent intent = new Intent(LoginActivity.this, PasswordRecoveredActivity.class);
        startActivityForResult(intent, REQUEST_CODE_GET_ACCOUNT);
        overridePendingTransition(R.anim.translate_slide_in_right, R.anim.translate_slide_out_left);
    }

    private void enterMainActivity() {
        Intent intent = new Intent(LoginActivity.this, LauncherActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.translate_slide_in_right, R.anim.translate_slide_out_left);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CODE_GET_ACCOUNT:
            if (data != null) {
                String account = data.getStringExtra("accountName");
                mUserAccount.setText(account);
            }
            break;
        default:
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
      
    /**-------------------------------------------------------------
     * Name:Robin  Time:20150923  Function:按返回键时，退出应用程序
     *-------------------------------------------------------------
     */
    private String packageName="com.foogeez.fooband";
    private CentralService mCentralService = null;
    
    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {  
        if (keyCode == KeyEvent.KEYCODE_BACK )  
        {      	
        	overridePendingTransition(R.anim.translate_slide_in_right, R.anim.translate_slide_out_left);
        	RuntimeCmdManager.clearAppUserData(packageName);
        	finish();  
        	mCentralService.broadcastUpdate(CentralService.ACTION_ACTIONS_ACTIVITY_IN_STACK_ALL_CLEAR);
        	
            Log.i(TAG, "--------------login--退出--exit-App--------");
        }  
        return false;  
    } 
    
    /** Name:Robin  Time:20150928  Function: 友盟session的统计  */
    @Override
    public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
    @Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
  
}
