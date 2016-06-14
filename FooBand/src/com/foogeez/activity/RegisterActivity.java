package com.foogeez.activity;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.foogeez.configuration.LocalStorage;
import com.foogeez.dialog.ConfigRspDialog;
import com.foogeez.fooband.R;
import com.foogeez.https.Urls;
import com.foogeez.network.NetworkUtils;
import com.foogeez.network.NetworkUtils.NetworkCallback;
import com.grdn.util.Utils;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class RegisterActivity extends SettingActivity {
	private final static String TAG = RegisterActivity.class.getSimpleName();

	private TextView mUserNotice;
	private TextView mUserLogin;
	private TextView mUserRegister;

	private EditText mUserAccount;
	private EditText mUserPassword;
	private EditText mUserRePassword;

	private String mUserAccoutDefaultString;
	private String mUserPasswordDefaultString;
	private String mUserRePasswordDefaultString;

	private String mUserAccountString;
	private String mUserPasswordString;
	private String mUserRePasswordString;

	private ImageView mImageViewWelcome;

	/** Name:Robin Time:20150925 Function:发送请求数据的等待过程 */
	private ProgressDialog progressDialog = null;

	private LocalStorage mLocalStorage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		// mImageViewWelcome = (ImageView)
		// findViewById(R.id.id_iv_welcome_register);
		//
		// //改变图片颜色
		// DrawableUtil.setImageViewColor(mImageViewWelcome,
		// getResources().getColor(R.color.logo_blue_change));

		mUserNotice = (TextView) findViewById(R.id.id_tv_user_notice);
		String notice = getResources().getString(
				R.string.string_user_register_password_notice);
		String content = getResources().getString(
				R.string.string_user_notice_content);

		/** Name:Robin Time:20150918 Function:判断为汉语时，协议的颜色 */
		String locale = Locale.getDefault().getLanguage();
		if (locale.equals("zh")) {
			mUserNotice.setText(Html.fromHtml(notice
					+ "<font color=\"#0077C2\">" + content + "</font>"));
		} else {
			mUserNotice.setText(Html.fromHtml(notice
					+ "<font color=\"#3498db\">" + content + "</font>"));
		}
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
				UIfinish();
			}
		});

		mUserRegister = (TextView) findViewById(R.id.id_tv_register);
		mUserRegister.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//检查网络是否可用
				if (!isNetworkAvailable(getApplicationContext())) {
					Toast toast = Toast.makeText(getApplicationContext(), R.string.no_network_state_notice, Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				if ((!Utils.isEmail(mUserAccountString))
						&& (!Utils.isPhone(mUserAccountString))) {
					new ConfigRspDialog(RegisterActivity.this, getResources()
							.getString(R.string.string_login_account_error))
							.show(1000);
					return;
				}
				if (!Utils.checkPassword(mUserPasswordString)) {
					new ConfigRspDialog(RegisterActivity.this, getResources()
							.getString(R.string.string_login_password_error))
							.show(1000);
					return;
				}
				if (!Utils.checkRePassword(mUserPasswordString,
						mUserRePasswordString)) {
					new ConfigRspDialog(RegisterActivity.this, getResources()
							.getString(R.string.string_login_re_password_error))
							.show(1000);
					return;
				}

				new NetworkUtils(RegisterActivity.this, new NetworkCallback() {
					@Override
					public void OnPreExcute() {
						progressDialog = ProgressDialog.show(
								RegisterActivity.this,
								"",
								getResources().getString(
										R.string.string_register_doing), true);
					}

					@Override
					public void OnExcuteSuccess(JSONObject obj)
							throws JSONException {
						// int result = obj.getInt("rs");
						progressDialog.dismiss();

						registerUserAccount();
						
						//创建快捷方式
//				        isExistShortCut();
//				        createShortcut();
					}

					@Override
					public void OnExcuteFailure(JSONObject obj)
							throws JSONException {
						progressDialog.dismiss();
						int result = obj.getInt("rs");
						Log.e(TAG, "-----robin--check user name: failure! rs: "
								+ result);
						// 这里的result 可能返回101 或 102，所以不管返回什么，都提示账号存在
						new ConfigRspDialog(
								RegisterActivity.this,
								getResources()
										.getString(
												R.string.string_register_error_account_exsist))
								.show(2000);
					}

					@Override
					public void OnTimeOut() {
						progressDialog.dismiss();
						Log.e(TAG, "check user name: time out!");
						new ConfigRspDialog(RegisterActivity.this,
								getResources().getString(
										R.string.string_network_timeout))
								.show(2000);
					}
				}).request(NetworkUtils.POST, Urls.USER_CHECK,
						mUserAccountString);

			}
		});

		mLocalStorage = new LocalStorage(RegisterActivity.this);

		initAccount();
		initPassword();
		initRePassword();

	}

	private void initAccount() {
		mUserAccountString = "";// mLocalStorage.getAccount();
		mUserAccount = (EditText) findViewById(R.id.id_et_account);
		mUserAccoutDefaultString = mUserAccount.getText().toString();

		mUserAccount.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

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
						mUserAccount.setTextColor(getResources().getColor(
								R.color.gray));
						mUserAccount.setText(mUserAccoutDefaultString);
					}
				} else {
					mUserAccount.setTextColor(getResources().getColor(
							R.color.black));
					if (mUserAccountString.length() == 0) {
						mUserAccount.setText("");
					}
				}
			}
		});

		mUserAccount.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
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
		mUserPasswordString = "";// mLocalStorage.getPassword();

		mUserPassword = (EditText) findViewById(R.id.id_et_password);
		mUserPasswordDefaultString = mUserPassword.getText().toString();

		if (mUserPasswordString.length() != 0) {
			mUserPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD
					| InputType.TYPE_CLASS_TEXT);
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
						mUserPassword.setTextColor(getResources().getColor(
								R.color.gray));
						mUserPassword.setText(mUserPasswordDefaultString);
					}
				} else {
					Log.e(TAG, "input type: "
							+ "InputType.TYPE_TEXT_VARIATION_PASSWORD");
					mUserPassword
							.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD
									| InputType.TYPE_CLASS_TEXT);
					mUserPassword.setTextColor(getResources().getColor(
							R.color.black));
					if (mUserPasswordString.length() == 0) {
						mUserPassword.setText("");
					}
				}
			}
		});

		mUserPassword.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!s.toString().equals(mUserPasswordDefaultString)) {
					mUserPasswordString = s.toString();
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	private void initRePassword() {
		mUserRePasswordString = "";// mLocalStorage.getPassword();

		mUserRePassword = (EditText) findViewById(R.id.id_et_re_password);
		mUserRePasswordDefaultString = mUserRePassword.getText().toString();

		if (mUserRePasswordString.length() != 0) {
			mUserRePassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD
					| InputType.TYPE_CLASS_TEXT);
			mUserRePassword.setText(mUserRePasswordString);
			mUserRePassword
					.setTextColor(getResources().getColor(R.color.black));
			mUserRePassword.setSelection(mUserRePasswordString.length());
		} else {
			mUserRePassword.setInputType(InputType.TYPE_CLASS_TEXT);
			mUserRePassword.setTextColor(getResources().getColor(R.color.gray));
			mUserRePassword.setText(mUserRePasswordDefaultString);
		}

		mUserRePassword.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					if (mUserRePasswordString.length() == 0) {
						mUserRePassword.setInputType(InputType.TYPE_CLASS_TEXT);
						mUserRePassword.setTextColor(getResources().getColor(
								R.color.gray));
						mUserRePassword.setText(mUserRePasswordDefaultString);
					}
				} else {
					Log.e(TAG, "input type: "
							+ "InputType.TYPE_TEXT_VARIATION_PASSWORD");
					mUserRePassword
							.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD
									| InputType.TYPE_CLASS_TEXT);
					mUserRePassword.setTextColor(getResources().getColor(
							R.color.black));
					if (mUserRePasswordString.length() == 0) {
						mUserRePassword.setText("");
					}
				}
			}
		});

		mUserRePassword.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!s.toString().equals(mUserRePasswordDefaultString)) {
					mUserRePasswordString = s.toString();
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

	}

	private void registerUserAccount() {

		String language = Locale.getDefault().toString();
		int lang = 0;
		if (language.equals("zh_CN")) {
			lang = 1;
		} else if (language.equals("zh_TW")) {
			lang = 2;
		} else {
			lang = 3;
		}

		// USER_REGISTER
		new NetworkUtils(RegisterActivity.this, new NetworkCallback() {
			@Override
			public void OnPreExcute() {
				progressDialog = ProgressDialog.show(RegisterActivity.this, "",
						getResources()
								.getString(R.string.string_register_doing),
						true);
			}

			@Override
			public void OnExcuteSuccess(JSONObject obj) throws JSONException {
				progressDialog.dismiss();
				Log.e(TAG, "register success! rs: " + obj.getInt("rs"));
				new ConfigRspDialog(RegisterActivity.this, getResources()
						.getString(R.string.string_register_success))
						.show(1000);
				// UIfinish();
				reLogin();
			}

			@Override
			public void OnExcuteFailure(JSONObject obj) throws JSONException {
				progressDialog.dismiss();
				int result = obj.getInt("rs");
				Log.e(TAG, "register failure! rs: " + obj.getInt("rs"));
				if (result == 101) {
					new ConfigRspDialog(
							RegisterActivity.this,
							getResources()
									.getString(
											R.string.string_register_error_account_exsist))
							.show(2000);
				}
			}

			@Override
			public void OnTimeOut() {
				progressDialog.dismiss();
				Log.e(TAG, "register timeout!");
				new ConfigRspDialog(RegisterActivity.this, getResources()
						.getString(R.string.string_network_timeout)).show(2000);
			}

		}).request(NetworkUtils.POST, Urls.USER_REGISTER, mUserAccountString,
				Utils.md5(mUserPasswordString), lang);

	}

	/**
	 * 注册完直接登录
	 */
	private void reLogin() {

		Log.i(TAG, "Robin -------reLogin");
		new NetworkUtils(RegisterActivity.this, new NetworkCallback() {

			@Override
			public void OnPreExcute() {
//				progressDialog = ProgressDialog.show(RegisterActivity.this, "",
//						getResources().getString(R.string.string_login_doing),
//						true);
				Log.e(TAG, "login OnPreExcute!-------");
				// mLoginDoingDialog = new ConfigRspDialog(LoginActivity.this,
				// getResources().getString(
				// R.string.string_login_doing));
				// mLoginDoingDialog.show(-1);
			}

			@Override
			public void OnExcuteSuccess(JSONObject obj) throws JSONException {
				progressDialog.dismiss();
				Log.e(TAG, "login success!");

				mLocalStorage.saveAccount(mUserAccountString);
				mLocalStorage.savePassword(mUserPasswordString);
				Log.e(TAG, "login success!-------" + mUserPasswordString);

				mLocalStorage.saveUID(obj.getString("uid"));
				mLocalStorage.saveSID(obj.getString("sid"));
				// mLoginDoingDialog.cancel();
//				new ConfigRspDialog(RegisterActivity.this, getResources()
//						.getString(R.string.string_login_success)).show(1000);

				enterMainActivity();
			}

			@Override
			public void OnExcuteFailure(JSONObject obj) {
				progressDialog.dismiss();
				Log.e(TAG, "login failure!");

				int errorCode = 500;
				try {
					errorCode = obj.getInt("rs");
				} catch (JSONException e) {
					e.printStackTrace();
				}

				if (errorCode == 500) {
					new ConfigRspDialog(RegisterActivity.this, getResources()
							.getString(R.string.string_newwork_error))
							.show(2000);
				} else {
					new ConfigRspDialog(RegisterActivity.this, getResources()
							.getString(R.string.string_login_error)).show(2000);
				}
			}

			@Override
			public void OnTimeOut() {
				progressDialog.dismiss();
				Log.e(TAG, "login timeout!");
				new ConfigRspDialog(RegisterActivity.this, getResources()
						.getString(R.string.string_network_timeout)).show(2000);
			}
		}).request(NetworkUtils.POST, Urls.USER_LOGIN, mUserAccountString,
				Utils.md5(mUserPasswordString));

	}

	private void enterMainActivity() {
		//生成一个Intent对象  
        Intent intent = new Intent();  
		//在Intent对象当中添加一个键值对  
        intent.putExtra("testIntent", "123");  
        //设置Intent对象要启动的Activity  
        intent.setClass(RegisterActivity.this, LauncherActivity.class);  
        //通过Intent对象启动另外一个Activity  
//		Intent intent = new Intent(RegisterActivity.this,
//				LauncherActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.translate_slide_in_right,
				R.anim.translate_slide_out_left);
		finish();
	}

	@SuppressLint("DefaultLocale")
	public void dialog() {
		final WebView mWebView = new WebView(this);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		/**
		 * 判断android设备当前设置的语言使用Locale类中getLanguage()方法；
		 * Locale.getDefault().getLanguage(); Locale.getDefault().toString()
		 */
		int lang = 0;
		String lan = Locale.getDefault().toString();

		if (lan.equals("zh_CN")) {
			/** zh_CN 中文简体 */
			lang = 1;
		} else if (lan.equals("zh_TW")) {
			/** zh_TW 中文繁体 */
			lang = 2;
		} else {
			lang = 3;
		}
		String registersign = String.format(Urls.REG_SIGN, lang);
		mWebView.loadUrl(registersign);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(mWebView).setPositiveButton(
				getResources().getString(R.string.string_dialog_positive),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		builder.show();
	}
	
	  /** Name:Robin  Time:20150928  Function: 创建桌面快捷方式  */
    private void createShortcut() {  
    	Log.i(TAG, "------------>---createShortcut");
        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");  
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));  
        shortcut.putExtra("duplicate", false);//设置是否重复创建  
        Intent intent = new Intent(Intent.ACTION_MAIN);  
        intent.addCategory(Intent.CATEGORY_LAUNCHER);  
        intent.setClass(this, LauncherActivity.class);//设置第一个页面  
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);  
        ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher);  
        // 点击快捷图片，运行的程序主入口
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);  
        sendBroadcast(shortcut);  
    }  
    
    /** Name:Robin  Time:20150928  Function:  判断是否已经存在快捷方式    */
    public boolean isExistShortCut() {  
    	Log.i(TAG, "------------>---isExistShortCut");
        boolean isInstallShortcut = false;  
        final ContentResolver cr = RegisterActivity.this.getContentResolver();  
        // 本人的2.2系统是”com.android.launcher2.settings”,网上见其他的为"com.android.launcher.settings"  
        final String AUTHORITY = "com.android.launcher2.settings";  
        final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");  
        Cursor c = cr.query(CONTENT_URI, new String[] { "title", "iconResource" }, "title=?", new String[] { getString(R.string.app_name) }, null);  
        if (c != null && c.getCount() > 0) {  
            isInstallShortcut = true;  
            Log.i(TAG, "Robin----------已经存在快捷方式");  
        }  
        return isInstallShortcut;  
    } 
    
    /** 
     * 检测当的网络（WLAN、3G/2G）状态 
     * @param context Context 
     * @return true 表示网络可用 
     */  
	public boolean isNetworkAvailable(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		}else {
			//获取NetworkInfo对象
			NetworkInfo[] networkInfo = cm.getAllNetworkInfo();
			if (networkInfo != null && networkInfo.length > 0 ) {
				for (int i = 0; i < networkInfo.length; i++) {
					//判断当前网络状态是否为连接状态
					if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(RegisterActivity.this,
					LoginActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.translate_slide_in_right,
					R.anim.translate_slide_out_left);
			finish();
		}

		return super.onKeyDown(keyCode, event);
	}

	/** Name:Robin Time:20150928 Function: 友盟session的统计 */
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
