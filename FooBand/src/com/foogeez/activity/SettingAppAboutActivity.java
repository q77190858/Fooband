package com.foogeez.activity;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.foogeez.configuration.LocalStorage;
import com.foogeez.database.DatabaseHelper;
import com.foogeez.fooband.R;
import com.foogeez.https.Urls;
import com.foogeez.services.CentralService;
import com.grdn.util.Utils;
import com.umeng.analytics.MobclickAgent;

public class SettingAppAboutActivity extends SettingActivity implements OnClickListener {
	private final static String TAG = SettingAppAboutActivity.class
			.getSimpleName();

	private LocalStorage mLocalStorage = new LocalStorage(
			SettingAppAboutActivity.this);;

	private TextView mTextVeiwAppVersion;
	private TextView mTextViewAndroidVersion;
	private TextView mTextViewBluetoothVersion;
	private TextView mTextViewManufacturer;
	private TextView mTextViewModel;

	private Button mBtnCheckUpdate = null;
	private Button mBtnUsrAgreement = null;

	private String mNewAppFullName;
	private String mNewAppVersion;

	private ProgressDialog Mydialog;

	@SuppressLint("DefaultLocale")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "SettingAppAboutActivity --- onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_about);
		initSettingTitle(R.string.string_setting_app_about);

		mTextVeiwAppVersion = (TextView) findViewById(R.id.id_tv_setting_app_version_content);
		mTextViewAndroidVersion = (TextView) findViewById(R.id.id_tv_setting_android_suitability_test);
		mTextViewBluetoothVersion = (TextView) findViewById(R.id.id_tv_setting_bluetooth_suitability_test);
		mTextViewManufacturer = (TextView) findViewById(R.id.id_tv_setting_manufacturer);
		mTextViewModel = (TextView) findViewById(R.id.id_tv_setting_model);
		
		// 百度
		Mydialog = new ProgressDialog(this);
		Mydialog.setIndeterminate(true);
		
		mBtnCheckUpdate = (Button) findViewById(R.id.id_btn_check_app_new_version);
		mBtnCheckUpdate.setOnClickListener(this);
						
		mBtnUsrAgreement = (Button) findViewById(R.id.id_btn_user_agreement);
		mBtnUsrAgreement.setOnClickListener(this); 

		mTextVeiwAppVersion.setText(getAppVersionName());

		String format0 = getString(R.string.string_setting_mobile_manufacturer);
		format0 = String.format(format0, android.os.Build.MANUFACTURER);
		format0 = format0.toUpperCase();
		mTextViewManufacturer.setText(format0);

		String format1 = getString(R.string.string_setting_mobile_model);
		format1 = String.format(format1, android.os.Build.MODEL);
		format1 = format1.toUpperCase();
		mTextViewModel.setText(format1);

		String format2 = getString(R.string.string_setting_android_suitability_test);
		format2 = String.format(format2, android.os.Build.VERSION.RELEASE);
		mTextViewAndroidVersion.setText(format2);
		if (android.os.Build.VERSION.SDK_INT >= 18) {
			mTextViewAndroidVersion.setTextColor(0xff000000);
		} else {
			mTextViewAndroidVersion.setTextColor(0xFFFF0000);
		}

		String format3 = getString(R.string.string_setting_bluetooth_suitability_test);
		format3 = String.format(format3, getAndroidSupportBT());
		mTextViewBluetoothVersion.setText(format3);
		if (isSupportedBLE()) {
			mTextViewBluetoothVersion.setTextColor(0xff000000);
		} else {
			mTextViewBluetoothVersion.setTextColor(0xFFFF0000);
		}

	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_btn_check_app_new_version:
			// checkAppUpdate();
			// 百度更新
//			Mydialog.show();
			checkAppUpdate();
//			Log.i(TAG, "Robin---------	Mydialog.show();");
//			BDAutoUpdateSDK.uiUpdateAction(this, new MyUICheckUpdateCallback());
//			Log.i(TAG, "Robin---------	BDAutoUpdateSDK.uiUpdateAction(this, new MyUICheckUpdateCallback())");
			break;
		case R.id.id_btn_user_agreement:
			dialog();
			break;
		default:
			break;
		}
	}

	private String getAppVersionName() {
		PackageManager manager;
		PackageInfo info = null;
		manager = this.getPackageManager();

		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "0.0.0";
		}

		// return info.versionCode;
		return info.versionName;
		// info.packageName;
		// info.signatures;
	}

	private boolean isSupportedBLE() {
		return (getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE));
	}

	private String getAndroidSupportBT() {
		String result = "";
		boolean supportedEDR = (getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH));
		boolean supportedBLE = (getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE));
		result = supportedEDR ? "EDR/" : "";
		result = result + (supportedBLE ? "BLE" : "");
		return result;
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
		} else if (lan.equals("zh_TW")) {
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

	// /////////////////////////////－－－－更新ＡＰＰ－－－－///////////////////////////////////
	private ProgressDialog progressDialog;
	private final int NEW_VERSION_NONE = 0;
	private final int NEW_VERSION_EXSIST = 1;
	private final int NEW_VERSION_ERROR = 2;
	private final int NEW_VERSION_IF_UPDATE = 3;
	private final int UPDATE_AFTER = 5;

	private String app = null;

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case NEW_VERSION_EXSIST:
				Bundle data = msg.getData();
				mNewAppVersion = data.getString("APP_NEW_VER");
				mNewAppFullName = data.getString("APP_FULL_NAME");
				Log.e(TAG, "update....");
				Intent i = new Intent(Intent.ACTION_VIEW);
				// 保证安装的时候不闪退，安装完成可直接打开
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.setDataAndType(Utils.fileToUri(mNewAppFullName),
						"application/vnd.android.package-archive");
				startActivity(i);

				break;
			case NEW_VERSION_NONE:
				new AlertDialog.Builder(SettingAppAboutActivity.this)
				.setTitle(R.string.string_find_new_app_version) // 检查更新
				.setMessage(R.string.string_find_new_app_version_none) // 当前App为最新版本！
				.setPositiveButton(R.string.string_dialog_positive,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();
				break;

			case NEW_VERSION_ERROR:
				new AlertDialog.Builder(SettingAppAboutActivity.this)
				.setTitle(R.string.string_find_new_app_version) // 检查更新
				.setMessage(R.string.string_newwork_error) // 当前App为最新版本！
				.setPositiveButton(R.string.string_dialog_positive,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();
				break;

			case NEW_VERSION_IF_UPDATE: // 点击检测更新，如有更新，就直接下载
				Bundle data1 = msg.getData();
				mNewAppVersion = data1.getString("APP_NEW_VER");
				mNewAppFullName = data1.getString("APP_FULL_NAME");
				Log.i(TAG, "NEW_VERSION_IF_UPDATE------ mNewAppVersion"
						+ mNewAppVersion);
				String format = getString(R.string.string_find_new_app_version_content);
				format = String.format(format, mNewAppVersion);
				new AlertDialog.Builder(SettingAppAboutActivity.this)
						.setTitle(R.string.string_find_new_app_version)
						.setMessage(format)
						.setPositiveButton(R.string.string_dialog_positive,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										startUpdateApp();
									}
								})
						.setNegativeButton(R.string.string_dialog_negative,
								null).show();
				break;

			case UPDATE_AFTER:
				progressDialog.dismiss();
				startSetupApp();
				break;
			}
		}
	};

	private void checkAppUpdate() {

//		Toast.makeText(getApplicationContext(),
//				R.string.apk_is_connected_internet, Toast.LENGTH_SHORT).show();

		new Thread(new Runnable() {
			@SuppressLint("DefaultLocale")
			@Override
			public void run() {
				Message msg = new Message();

				String appURL = CentralService.APP_IMAGE + "?utc="
						+ Utils.getUtc();
				String infoURL = CentralService.APP_IMAGE_INF + "?utc="
						+ Utils.getUtc();
				String path = new DatabaseHelper(SettingAppAboutActivity.this,
						null, null).getDatabasePath() + "/app";
				String info = Utils.downloadFile2String(path, infoURL);
				if (info == null) {
					msg.what = NEW_VERSION_ERROR;
					handler.sendMessage(msg);
					return;
				}
				String ver = info.substring(info.lastIndexOf(":") + 1);
				String old = getAppVersionName();
				Log.e(TAG, "APP HAS NEW VERSION! --- checkAppUpdate()" + ver
						+ ", old=" + old);

				if (ver.compareToIgnoreCase(old) > 0) {
					Bundle data1 = new Bundle();
					String newFilename = path
							+ "/"
							+ appURL.substring(appURL.lastIndexOf("/") + 1,
									appURL.indexOf("?"));

					Log.i(TAG, "Robin -------- data --- " + data1);
					data1.putString("APP_NEW_VER", ver);
					data1.putString("APP_FULL_NAME", newFilename);
					msg.setData(data1); // 数据传递
					msg.what = NEW_VERSION_IF_UPDATE;
					handler.sendMessage(msg);
				} else {
					msg.what = NEW_VERSION_NONE;
					handler.sendMessage(msg);
				}
			}
		}).start();
	}

	/**
	 * 下载 app
	 */
	private void startUpdateApp() {
		Log.i(TAG, "Robin--------startUpdateApp");

		progressDialog = new ProgressDialog(SettingAppAboutActivity.this); // 进度条，在下载的时候实时更新进度，提高用户友好度
		// progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setTitle(R.string.apk_is_downloading);
		progressDialog.isIndeterminate();
		progressDialog.setMessage(getString(R.string.apk_is_downloading_wait));
		progressDialog.show();
		progressDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失

		new Thread(new Runnable() {
			@SuppressLint("DefaultLocale")
			@Override
			public void run() {
				Message msg = new Message();

				String appURL = CentralService.APP_IMAGE + "?utc="
						+ Utils.getUtc();
				String path = new DatabaseHelper(SettingAppAboutActivity.this,
						null, null).getDatabasePath() + "/app";

				app = Utils.downloadFileAndCaculateMd5(path, appURL);

				msg.what = UPDATE_AFTER;
				handler.sendMessage(msg);
				Log.i(TAG, "Robin--------downloadFileAndCaculateMd5-----app"
						+ app);

			}
		}).start();
	}

	/**
	 * 开始安装 App
	 */
	private void startSetupApp() {

		Log.i(TAG, "Robin--------startUpdateApp");

		new Thread(new Runnable() {
			@SuppressLint("DefaultLocale")
			@Override
			public void run() {

				Message msg = new Message();

				String appURL = CentralService.APP_IMAGE + "?utc="
						+ Utils.getUtc();
				String infoURL = CentralService.APP_IMAGE_INF + "?utc="
						+ Utils.getUtc();

				String today_date = Utils.getTodayDate();

				String path = new DatabaseHelper(SettingAppAboutActivity.this,
						null, null).getDatabasePath() + "/app";
				String info = Utils.downloadFile2String(path, infoURL);

				String md5 = info.substring(info.indexOf(":") + 1,
						info.indexOf("\n") - 1);
				String ver = info.substring(info.lastIndexOf(":") + 1);

				if (md5.equalsIgnoreCase(app)) {
					Log.e(TAG, "MD5:ok --- " + app);
					mLocalStorage.setAppUpdateLastCheck(today_date);
					String newFilename = path
							+ "/"
							+ appURL.substring(appURL.lastIndexOf("/") + 1,
									appURL.indexOf("?"));
					Bundle data = new Bundle();
					data.putString("APP_NEW_VER", ver);
					data.putString("APP_FULL_NAME", newFilename);
					msg.setData(data);
					msg.what = NEW_VERSION_EXSIST;
					handler.sendMessage(msg);
				} else {
					Log.e(TAG, "MD5= " + md5);
					Log.e(TAG, "APP= " + app);
					msg.what = NEW_VERSION_ERROR;
					handler.sendMessage(msg);
				}
			}
		}).start();

	}

	// 百度更新
//	@Override
//	protected void onDestroy() {
//		Mydialog.dismiss();
//		Log.i(TAG, "Robin--------onDestroy()");
//		super.onDestroy();
//	}
//
//	private class MyUICheckUpdateCallback implements UICheckUpdateCallback {
//
//		@Override
//		public void onCheckComplete() {
//			Mydialog.dismiss();
//			
//			Log.i(TAG, "Robin--------onCheckComplete");
//		}
//
//	}
	
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
