package com.foogeez.activity;

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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class PasswordChangeActivity extends SettingActivity {
	private final static String TAG = PasswordChangeActivity.class
			.getSimpleName();

	private EditText mEditTextOldPassword;
	private EditText mEditTextNewPassword;
	private EditText mEditTextNewPasswordRe;

	private Button mBtnSubmitNewPassword;
	private ImageView mBtnBackto;

	private LocalStorage mLocalStorage;

	private String oldPwd;
	private String newPwd;
	private String newPwdRe;

	private String oldDefaultPwd;
	private String newDefaultPwd;
	private String newDefaultPwdRe;

	private ConfigRspDialog mChangePwdDoingDialog;

	/** Name:Robin Time:20150925 Function:发送请求数据的等待过程 */
	private ProgressDialog progressDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Log.i(TAG, "PasswordChangeActivity --- onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_password_change);
		initSettingTitle(R.string.string_user_info_change_pwd);

		mLocalStorage = new LocalStorage(PasswordChangeActivity.this);

		initView();

		initOldPassword();
		initNewPassword();
		initRePassword();
	}

	private void initView() {
		mBtnSubmitNewPassword = (Button) findViewById(R.id.btn_submit_change_pwd);
		mBtnSubmitNewPassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				updatePassword();

			}
		});

		mBtnBackto = (ImageView) findViewById(R.id.id_iv_setting_back);
		mBtnBackto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PasswordChangeActivity.this,
						SettingUsrInfoActivity.class);
				startActivity(intent);
				UIfinish();
			}
		});

	}

	/** Name:Robin Time:20150924 Function:更新密码 */
	private void updatePassword() {
		String pwd = mLocalStorage.getPassword();
		// Log.i(TAG, "PasswordChangeActivity --- -密码----------"+pwd);

		// oldPwd = mEditTextOldPassword.getText().toString().trim();
		// Log.i(TAG, "--- -密码---oldPwd-------"+oldPwd);

		if (!oldPwd.equals(pwd)) {
			new ConfigRspDialog(PasswordChangeActivity.this, getResources()
					.getString(R.string.string_old_pwd_error)).show(1000);
			return;
		}
		
		//检查网络是否可用
		if (!isNetworkAvailable(getApplicationContext())) {
			Toast toast = Toast.makeText(getApplicationContext(), R.string.no_network_state_notice, Toast.LENGTH_SHORT);
			toast.show();
			return;
		}

		if ((newPwd.length() < 6) || (newPwd.length() > 16)) {
			new ConfigRspDialog(PasswordChangeActivity.this, getResources()
					.getString(R.string.string_new_pwd_error)).show(1000);
			return;
		}

		if (!Utils.checkPassword(newPwd)) {
			new ConfigRspDialog(PasswordChangeActivity.this, getResources()
					.getString(R.string.string_new_pwd_error)).show(1000);
			return;
		}
		if (!Utils.checkRePassword(newPwd, newPwdRe)) {
			new ConfigRspDialog(PasswordChangeActivity.this, getResources()
					.getString(R.string.string_login_re_password_error))
					.show(1000);
			return;
		}

		new NetworkUtils(PasswordChangeActivity.this, new NetworkCallback() {
			@Override
			public void OnPreExcute() {
				progressDialog = ProgressDialog.show(
						PasswordChangeActivity.this, "", getResources()
								.getString(R.string.string_doing_submit), true);

			}

			@Override
			public void OnExcuteSuccess(JSONObject obj) throws JSONException {
				progressDialog.dismiss();
				new ConfigRspDialog(PasswordChangeActivity.this, getResources()
						.getString(R.string.string_change_pwd_success))
						.show(1000);
				Log.e(TAG, "密码修改 success! rs: " + obj.getInt("rs"));
				mLocalStorage.setFstRefreshDatumFlag(true);
				mLocalStorage.exitCurrentUserLogin();
				enterLoginActivity();
			}

			@Override
			public void OnExcuteFailure(JSONObject obj) throws JSONException {
				progressDialog.dismiss();
				Log.e(TAG,
						"register failure! ----修改失败--rs: " + obj.getInt("rs"));
				new ConfigRspDialog(PasswordChangeActivity.this, getResources()
						.getString(R.string.string_change_pwd_failure))
						.show(2000);
			}

			@Override
			public void OnTimeOut() {
				progressDialog.dismiss();
				Log.e(TAG, "change pwd timeout!----");
				new ConfigRspDialog(PasswordChangeActivity.this, getResources()
						.getString(R.string.string_network_timeout)).show(2000);
			}
		}).request(NetworkUtils.POST, Urls.USER_NEWPWD, Utils.md5(newPwd),
				mLocalStorage.getSID());

		// Log.i(TAG, "------Utils.md5(newPwd)"+Utils.md5(newPwd));
		// Log.i(TAG, "------mLocalStorage.getSID()"+mLocalStorage.getSID());
	}

	// ///////////////////////////////////////////////////////////////////////////////////////

	private void initOldPassword() {
		oldPwd = "";// mLocalStorage.getPassword();
		mEditTextOldPassword = (EditText) findViewById(R.id.id_et_password_old);
		oldDefaultPwd = mEditTextOldPassword.getText().toString().trim();

		if (oldPwd.length() != 0) {
			mEditTextOldPassword
					.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD
							| InputType.TYPE_CLASS_TEXT);
			mEditTextOldPassword.setText(oldPwd);
			mEditTextOldPassword.setTextColor(getResources().getColor(
					R.color.black));
			mEditTextOldPassword.setSelection(oldPwd.length());
		} else {
			mEditTextOldPassword.setInputType(InputType.TYPE_CLASS_TEXT);
			mEditTextOldPassword.setTextColor(getResources().getColor(
					R.color.gray));
			mEditTextOldPassword.setText(oldDefaultPwd);
		}

		mEditTextOldPassword
				.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (!hasFocus) {
							if (oldPwd.length() == 0) {
								mEditTextOldPassword
										.setInputType(InputType.TYPE_CLASS_TEXT);
								mEditTextOldPassword
										.setTextColor(getResources().getColor(
												R.color.gray));
								mEditTextOldPassword.setText(oldDefaultPwd);
							}
						} else {
							Log.e(TAG, "input type: "
									+ "InputType.TYPE_TEXT_VARIATION_PASSWORD");
							mEditTextOldPassword
									.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD
											| InputType.TYPE_CLASS_TEXT);
							mEditTextOldPassword.setTextColor(getResources()
									.getColor(R.color.black));
							if (oldPwd.length() == 0) {
								mEditTextOldPassword.setText("");
							}
						}
					}
				});

		mEditTextOldPassword.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!s.toString().equals(oldDefaultPwd)) {
					oldPwd = s.toString();
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	private void initNewPassword() {
		newPwd = "";// mLocalStorage.getPassword();
		mEditTextNewPassword = (EditText) findViewById(R.id.id_et_password_new);
		newDefaultPwd = mEditTextNewPassword.getText().toString().trim();

		if (newPwd.length() != 0) {
			mEditTextNewPassword
					.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD
							| InputType.TYPE_CLASS_TEXT);
			mEditTextNewPassword.setText(newPwd);
			mEditTextNewPassword.setTextColor(getResources().getColor(
					R.color.black));
			mEditTextNewPassword.setSelection(newPwd.length());
		} else {
			mEditTextNewPassword.setInputType(InputType.TYPE_CLASS_TEXT);
			mEditTextNewPassword.setTextColor(getResources().getColor(
					R.color.gray));
			mEditTextNewPassword.setText(newDefaultPwd);
		}

		mEditTextNewPassword
				.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (!hasFocus) {
							if (newPwd.length() == 0) {
								mEditTextNewPassword
										.setInputType(InputType.TYPE_CLASS_TEXT);
								mEditTextNewPassword
										.setTextColor(getResources().getColor(
												R.color.gray));
								mEditTextNewPassword.setText(newDefaultPwd);
							}
						} else {
							Log.e(TAG, "input type: "
									+ "InputType.TYPE_TEXT_VARIATION_PASSWORD");
							mEditTextNewPassword
									.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD
											| InputType.TYPE_CLASS_TEXT);
							mEditTextNewPassword.setTextColor(getResources()
									.getColor(R.color.black));
							if (newPwd.length() == 0) {
								mEditTextNewPassword.setText("");
							}
						}
					}
				});

		mEditTextNewPassword.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!s.toString().equals(newDefaultPwd)) {
					newPwd = s.toString();
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	private void initRePassword() {
		newPwdRe = "";// mLocalStorage.getPassword();
		mEditTextNewPasswordRe = (EditText) findViewById(R.id.id_et_password_new_re);
		newDefaultPwdRe = mEditTextNewPasswordRe.getText().toString().trim();

		if (newPwdRe.length() != 0) {
			mEditTextNewPasswordRe
					.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD
							| InputType.TYPE_CLASS_TEXT);
			mEditTextNewPasswordRe.setText(newPwdRe);
			mEditTextNewPasswordRe.setTextColor(getResources().getColor(
					R.color.black));
			mEditTextNewPasswordRe.setSelection(newPwdRe.length());
		} else {
			mEditTextNewPasswordRe.setInputType(InputType.TYPE_CLASS_TEXT);
			mEditTextNewPasswordRe.setTextColor(getResources().getColor(
					R.color.gray));
			mEditTextNewPasswordRe.setText(newDefaultPwdRe);
		}

		mEditTextNewPasswordRe
				.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (!hasFocus) {
							if (newPwdRe.length() == 0) {
								mEditTextNewPasswordRe
										.setInputType(InputType.TYPE_CLASS_TEXT);
								mEditTextNewPasswordRe
										.setTextColor(getResources().getColor(
												R.color.gray));
								mEditTextNewPasswordRe.setText(newDefaultPwdRe);
							}
						} else {
							Log.e(TAG, "input type: "
									+ "InputType.TYPE_TEXT_VARIATION_PASSWORD");
							mEditTextNewPasswordRe
									.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD
											| InputType.TYPE_CLASS_TEXT);
							mEditTextNewPasswordRe.setTextColor(getResources()
									.getColor(R.color.black));
							if (newPwdRe.length() == 0) {
								mEditTextNewPasswordRe.setText("");
							}
						}
					}
				});

		mEditTextNewPasswordRe.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!s.toString().equals(newDefaultPwdRe)) {
					newPwdRe = s.toString();
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

	}

	/** Name:Robin Time:20150924 Function:更新密码成功后进入登录页面 */
	private void enterLoginActivity() {
		Intent intent = new Intent(PasswordChangeActivity.this,
				LoginActivity.class);
		startActivity(intent);
		ManagerActivity.instance.finish(); // 关闭指定Activity,再次进入为首页的页面
		overridePendingTransition(R.anim.translate_slide_in_right,
				R.anim.translate_slide_out_left);
		UIfinish();
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
			if (networkInfo != null && networkInfo.length > 0) {
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

	// 在需要监听的activity中重写onKeyDown()。
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// do something...
			Intent intent = new Intent(PasswordChangeActivity.this,
					SettingUsrInfoActivity.class);
			startActivity(intent);
			UIfinish();
			return true;
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
