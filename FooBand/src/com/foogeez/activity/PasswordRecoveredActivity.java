package com.foogeez.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.foogeez.configuration.LocalStorage;
import com.foogeez.dialog.ConfigRspDialog;
import com.foogeez.fooband.R;
import com.foogeez.https.Urls;
import com.foogeez.network.NetworkUtils;
import com.foogeez.network.NewWorkUtilsForCode;
import com.foogeez.network.NewWorkUtilsForCode.NetworkCallCodeback;
import com.grdn.util.CountDownButtonHelper.OnFinishListener;
import com.grdn.util.Utils;
import com.grdn.util.CountDownButtonHelper;
import com.umeng.analytics.MobclickAgent;

public class PasswordRecoveredActivity extends SettingActivity {
	private final static String TAG = PasswordRecoveredActivity.class
			.getSimpleName();

	private Button mUserLogin;
	private TextView mUserRegister;

	private EditText mUserAccount;
	// private EditText mUserCode;

	private String mUserAccountString;
	// private String mValideCodeString;

	private LocalStorage mLocalStorage;

	private Button mImageViewBtnGetCode;

	private ImageView mImageViewWelcome;

	/** Name:Robin Time:20150925 Function:发送请求数据的等待过程 */
	private ProgressDialog progressDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_password_recovered);

//		mImageViewWelcome = (ImageView) findViewById(R.id.id_iv_welcome_pwd_re);
//
//		// 改变图片颜色
//		DrawableUtil.setImageViewColor(mImageViewWelcome, getResources()
//				.getColor(R.color.logo_blue_change));

		// mImageViewBtnGetCode = (Button) findViewById(R.id.iv_vericode);
		// mImageViewBtnGetCode.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		//
		// CountDownButtonHelper helper = new
		// CountDownButtonHelper(mImageViewBtnGetCode,
		// getResources().getString(R.string.again_get_test_code), 60, 1);
		//
		// if (!validateInput(false)) {
		// return;
		// }
		// /** Name:Robin Time:20150929 Function:网络请求，请求验证码 */
		// new
		// NewWorkUtilsForCode(PasswordRecoveredActivity.this.getApplicationContext(),
		// new NetworkCallCodeback()
		// {
		//
		// @Override
		// public void OnPreExcute() {
		// new ConfigRspDialog(PasswordRecoveredActivity.this,
		// getResources().getString(
		// R.string.string_login_code_error_send))
		// .show(2000);
		// }
		//
		// }).request(NetworkUtils.GET, Urls.USER_VERICODE, mUserAccountString);
		//
		// helper.setOnFinishListener(new OnFinishListener() {
		//
		// @Override
		// public void finish() {
		// // Toast.makeText(PasswordRecoveredActivity.this, "倒计时结束",
		// // Toast.LENGTH_SHORT).show();
		// }
		// });
		// helper.start();
		//
		// }
		//
		//
		// });

		mUserLogin = (Button) findViewById(R.id.id_tv_login);
		mUserLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//检查网络是否可用
				if (!isNetworkAvailable(getApplicationContext())) {
					Toast toast = Toast.makeText(getApplicationContext(), R.string.no_network_state_notice, Toast.LENGTH_SHORT);
					toast.show();
					return;
				}

				mUserLogin.setBackground(getResources().getDrawable(
						R.drawable.bg_edittext));

				CountDownButtonHelper helper = new CountDownButtonHelper(
						mUserLogin, getResources().getString(
								R.string.again_get_test_code), 60, 1);

				if (!validateInput(true)) {
					return;
				}

				new NewWorkUtilsForCode(PasswordRecoveredActivity.this
						.getApplicationContext(), new NetworkCallCodeback() {

					@Override
					public void OnPreExcute() {
						new ConfigRspDialog(PasswordRecoveredActivity.this,
								getResources().getString(
										R.string.string_login_code_error_send))
								.show(2000);
					}

				}).request(NetworkUtils.GET, Urls.USER_PASSWORD,
						mUserAccountString);
				Log.e(TAG, Urls.USER_PASSWORD + "----------robin");
				// }).request(NetworkUtils.POST, Urls.USER_PASSWORD,
				// mUserAccountString, mValideCodeString);

				helper.setOnFinishListener(new OnFinishListener() {

					@Override
					public void finish() {
						// Toast.makeText(PasswordRecoveredActivity.this,
						// "倒计时结束",
						// Toast.LENGTH_SHORT).show();
						jumpBackWithData();
					}
				});
				helper.start();
			}
		});

		mUserRegister = (TextView) findViewById(R.id.id_tv_register);
		mUserRegister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UIfinish();
			}
		});

		mLocalStorage = new LocalStorage(PasswordRecoveredActivity.this);

		initAccount();
		// initValidateCode();
	}

	private void initAccount() {
		mUserAccountString = mLocalStorage.getAccount();
		mUserAccount = (EditText) findViewById(R.id.id_et_account);

		mUserAccount.setText(mUserAccountString);
	}

	private boolean validateInput(boolean containAll) {
		mUserAccountString = mUserAccount.getText().toString().trim();
		// mValideCodeString = mUserCode.getText().toString().trim();

		if (TextUtils.isEmpty(mUserAccountString)
				|| (!TextUtils.isEmpty(mUserAccountString) && !Utils
						.isEmail(mUserAccountString))
				&& (!Utils.isPhone(mUserAccountString))) {
			new ConfigRspDialog(PasswordRecoveredActivity.this, getResources()
					.getString(R.string.string_login_account_error)).show(1000);
			return false;
		}

		// if (containAll) {
		// if (TextUtils.isEmpty(mValideCodeString) ||
		// (mValideCodeString.length() > 16)) {
		// new ConfigRspDialog(PasswordRecoveredActivity.this,
		// getResources().getString(
		// R.string.string_user_password_code_error)).show(1000);
		// return false;
		// }
		// }

		return true;
	}

	// private void initValidateCode() {
	// mUserCode = (EditText) findViewById(R.id.id_et_password);
	// }

	private void jumpBackWithData() {
		Intent intent = new Intent();
		intent.putExtra("accountName", mUserAccountString);
		setResult(RESULT_OK, intent);
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
