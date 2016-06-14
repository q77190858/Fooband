package com.foogeez.activity;

import java.io.File;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.foogeez.configuration.LocalStorage;
import com.foogeez.configuration.Configuration.UsrInfoConfiguration;
import com.foogeez.database.ActionsDatum;
import com.foogeez.dialog.UsrInfoBirthdaySettingDialog;
import com.foogeez.dialog.UsrInfoHeightSettingDialog;
import com.foogeez.dialog.UsrInfoNickNameSettingDialog;
import com.foogeez.dialog.UsrInfoSexySettingDialog;
import com.foogeez.dialog.UsrInfoWeightSettingDialog;
import com.foogeez.fooband.R;
import com.foogeez.https.Urls;
import com.foogeez.network.NetworkUtils;
import com.foogeez.network.NetworkUtils.NetworkCallback;
import com.foogeez.services.CentralService;
import com.grdn.util.Utils;
import com.umeng.analytics.MobclickAgent;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingUsrInfoActivity extends SettingActivity {
	private final static String TAG = SettingUsrInfoActivity.class
			.getSimpleName();

	private CentralService mCentralService = null;

	private UsrInfoSexySettingDialog mUsrInfoSexySettingDialog = null;
	private UsrInfoHeightSettingDialog mUsrInfoHeightSettingDialog = null;
	private UsrInfoWeightSettingDialog mUsrInfoWeightSettingDialog = null;
	private UsrInfoBirthdaySettingDialog mUsrInfoBirthdaySettingDialog = null;
	private UsrInfoNickNameSettingDialog mUsrInfoNickNameSettingDialog = null;

	private ImageView mMyAvatar = null;
	private File mCurrentPhotoFile;
	private Bitmap imageBitmap;

	private String formatString0 = null;
	private String formatString1 = null;
	private String formatString2 = null;
	private String formatString3 = null;
	private String formatString4 = null;
	private String formatString5 = null;

	private LocalStorage mLocalStorage = new LocalStorage(
			SettingUsrInfoActivity.this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "SettingUsrInfoActivity --- onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_usr_info);
		initSettingTitle(R.string.string_user_info);

		if (!mLocalStorage.hasAnyAccount()) {
			Toast.makeText(
					SettingUsrInfoActivity.this,
					getResources().getString(
							R.string.string_please_register_first),
					Toast.LENGTH_SHORT).show();

			super.UIfinish();
			return;
		}

		bindCentralService();

		/**
		 * Robin ---- 判断是否需要从服务器下载更新 表单数据
		 */
		if (mLocalStorage.restoreUsrInfoConfig().getNickName() != "N/A") {
			getUserInfoFromLocalStorage();
		} else {
			downloadUsrInfo();
		}

		((Button) findViewById(R.id.id_btn_exit_user_login))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.i(TAG,
								"-----------清除缓存并退出---btn_exit_user_login--------");
						mCentralService.disconnectLeDevice();
						mLocalStorage.setFstRefreshDatumFlag(true);
						mLocalStorage.exitCurrentUserLogin();
						enterLoginActivity();
					}
				});

		/**
		 * -------------------------------------------------------------
		 * Name:Robin Time:20150924 Function:点击进入修改页面
		 * -------------------------------------------------------------
		 */
		((RelativeLayout) findViewById(R.id.id_rl_setting_usr_info_password))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.i(TAG, "-------11----进入修改密码页面--------");
						Intent intent = new Intent(SettingUsrInfoActivity.this,
								PasswordChangeActivity.class);
						startActivity(intent);
						overridePendingTransition(
								R.anim.translate_slide_in_right,
								R.anim.translate_slide_out_left);
						finish();
					}
				});

		mMyAvatar = (ImageView) findViewById(R.id.id_iv_my_avatar);
		mMyAvatar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getPicByTakePhoto();
			}
		});

		formatString0 = getString(R.string.string_user_height_content);
		formatString0 = String.format(formatString0, mLocalStorage
				.restoreUsrInfoConfig().getHeight());
		// ((TextView)findViewById(R.id.id_tv_setting_usr_info_height_content)).setText(formatString0);

		formatString1 = getString(R.string.string_user_weight_content);
		formatString1 = String.format(formatString1, mLocalStorage
				.restoreUsrInfoConfig().getWeight());
		// ((TextView)findViewById(R.id.id_tv_setting_usr_info_weight_content)).setText(formatString1);

		String man = getString(R.string.string_user_sexy_man);
		String woman = getString(R.string.string_user_sexy_woman);
		formatString2 = getString(R.string.string_user_sexy_content);
		formatString2 = String.format(formatString2, (mLocalStorage
				.restoreUsrInfoConfig().getSexy() == 0) ? woman : man);
		// ((TextView)findViewById(R.id.id_tv_setting_usr_info_sexy_content)).setText(formatString2);

		formatString3 = getString(R.string.string_user_birthday_content);
		formatString3 = String.format(formatString3, ActionsDatum
				.utc2DateTimeString(mLocalStorage.restoreUsrInfoConfig()
						.getBirthday(), "yyyy-MM-dd", TimeZone.getDefault()));
		// ((TextView)findViewById(R.id.id_tv_setting_usr_info_birthday_content)).setText(formatString3);

		((RelativeLayout) findViewById(R.id.id_rl_setting_usr_info_height))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mUsrInfoHeightSettingDialog = new UsrInfoHeightSettingDialog(
								SettingUsrInfoActivity.this, mLocalStorage
										.restoreUsrInfoConfig());
						Window dialogWindow = mUsrInfoHeightSettingDialog
								.getWindow();
						dialogWindow.setGravity(Gravity.CENTER);
						mUsrInfoHeightSettingDialog
								.setCanceledOnTouchOutside(false);
						mUsrInfoHeightSettingDialog
								.setOnConfirmListener(new UsrInfoHeightSettingDialog.OnConfirmListener() {
									@Override
									public void OnConfirm(
											UsrInfoConfiguration config) {
										String formatString0 = getString(R.string.string_user_height_content);
										formatString0 = String.format(
												formatString0,
												config.getHeight());
										((TextView) findViewById(R.id.id_tv_setting_usr_info_height_content))
												.setText(formatString0);
										mLocalStorage.saveUsrInfoConfig(config);
										updateUsrHeightInfo(config.getHeight());
									
										Log.i(TAG,"Robin---上传--"+config.getHeight());
										
										if (mCentralService != null) {
											Log.e(TAG,
													"ENCODE: "
															+ Utils.bytesToHexString(config
																	.getEncode()));
											mCentralService
													.requestConnectionConfigFuncions(
															CentralService.DEVICE_CMD_ADDR_USR_INFO,
															config.getEncode());
										}
									}
								});
						mUsrInfoHeightSettingDialog.show();
					}
				});

		((RelativeLayout) findViewById(R.id.id_rl_setting_usr_info_weight))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mUsrInfoWeightSettingDialog = new UsrInfoWeightSettingDialog(
								SettingUsrInfoActivity.this, mLocalStorage
										.restoreUsrInfoConfig());
						Window dialogWindow = mUsrInfoWeightSettingDialog
								.getWindow();
						dialogWindow.setGravity(Gravity.CENTER);
						mUsrInfoWeightSettingDialog
								.setCanceledOnTouchOutside(false);
						mUsrInfoWeightSettingDialog
								.setOnConfirmListener(new UsrInfoWeightSettingDialog.OnConfirmListener() {
									@Override
									public void OnConfirm(
											UsrInfoConfiguration config) {
										String formatString1 = getString(R.string.string_user_weight_content);
										formatString1 = String.format(
												formatString1,
												config.getWeight());
										((TextView) findViewById(R.id.id_tv_setting_usr_info_weight_content))
												.setText(formatString1);
										mLocalStorage.saveUsrInfoConfig(config);
										updateUsrWeightInfo(config.getWeight());
										
										Log.i(TAG,"Robin---上传--config.getWeight()"+config.getWeight());
										if (mCentralService != null) {
											Log.e(TAG,
													"ENCODE: "
															+ Utils.bytesToHexString(config
																	.getEncode()));
											mCentralService
													.requestConnectionConfigFuncions(
															CentralService.DEVICE_CMD_ADDR_USR_INFO,
															config.getEncode());
										}
									}
								});
						mUsrInfoWeightSettingDialog.show();
					}
				});

		((RelativeLayout) findViewById(R.id.id_rl_setting_usr_info_sexy))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mUsrInfoSexySettingDialog = new UsrInfoSexySettingDialog(
								SettingUsrInfoActivity.this, mLocalStorage
										.restoreUsrInfoConfig());
						Window dialogWindow = mUsrInfoSexySettingDialog
								.getWindow();
						dialogWindow.setGravity(Gravity.CENTER);
						mUsrInfoSexySettingDialog
								.setCanceledOnTouchOutside(false);
						mUsrInfoSexySettingDialog
								.setOnConfirmListener(new UsrInfoSexySettingDialog.OnConfirmListener() {
									@Override
									public void OnConfirm(
											UsrInfoConfiguration config) {
										String man = getString(R.string.string_user_sexy_man);
										String woman = getString(R.string.string_user_sexy_woman);
										String formatString2 = getString(R.string.string_user_sexy_content);
										formatString2 = String.format(
												formatString2,
												(config.getSexy() == 0) ? woman
														: man);
										Log.e(TAG, "formatString2 : "
												+ formatString2);
										((TextView) findViewById(R.id.id_tv_setting_usr_info_sexy_content))
												.setText(formatString2);
										mLocalStorage.saveUsrInfoConfig(config);
										updateUsrSexInfo(config.getSexy());
										Log.i(TAG,"Robin---上传--config.getSexy()"+config.getSexy());
										if (mCentralService != null) {
											Log.e(TAG,
													"ENCODE: "
															+ Utils.bytesToHexString(config
																	.getEncode()));
											mCentralService
													.requestConnectionConfigFuncions(
															CentralService.DEVICE_CMD_ADDR_USR_INFO,
															config.getEncode());
										}
									}
								});
						mUsrInfoSexySettingDialog.show();
					}
				});

		((RelativeLayout) findViewById(R.id.id_rl_setting_usr_info_birthday))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mUsrInfoBirthdaySettingDialog = new UsrInfoBirthdaySettingDialog(
								SettingUsrInfoActivity.this, mLocalStorage
										.restoreUsrInfoConfig());
						Window dialogWindow = mUsrInfoBirthdaySettingDialog
								.getWindow();
						dialogWindow.setGravity(Gravity.CENTER);
						mUsrInfoBirthdaySettingDialog
								.setCanceledOnTouchOutside(false);
						mUsrInfoBirthdaySettingDialog
								.setOnConfirmListener(new UsrInfoBirthdaySettingDialog.OnConfirmListener() {
									@Override
									public void OnConfirm(
											UsrInfoConfiguration config) {
										String formatString3 = getString(R.string.string_user_birthday_content);
										formatString3 = String.format(
												formatString3,
												ActionsDatum.utc2DateTimeString(
														config.getBirthday(),
														"yyyy-MM-dd",
														TimeZone.getDefault()));
										((TextView) findViewById(R.id.id_tv_setting_usr_info_birthday_content))
												.setText(formatString3);
										mLocalStorage.saveUsrInfoConfig(config);
										updateUsrBirthdayInfo(formatString3);
										Log.i(TAG,"Robin---上传--生日"+formatString3);
										if (mCentralService != null) {
											Log.e(TAG,
													"ENCODE: "
															+ Utils.bytesToHexString(config
																	.getEncode()));
											mCentralService
													.requestConnectionConfigFuncions(
															CentralService.DEVICE_CMD_ADDR_USR_INFO,
															config.getEncode());
										}
									}
								});
						mUsrInfoBirthdaySettingDialog.show();
					}
				});

		((RelativeLayout) findViewById(R.id.id_rl_setting_usr_info_nickname))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mUsrInfoNickNameSettingDialog = new UsrInfoNickNameSettingDialog(
								SettingUsrInfoActivity.this, mLocalStorage
										.restoreUsrInfoConfig());
						Window dialogWindow = mUsrInfoNickNameSettingDialog
								.getWindow();
						dialogWindow.setGravity(Gravity.CENTER);
						mUsrInfoNickNameSettingDialog
								.setCanceledOnTouchOutside(false);
						mUsrInfoNickNameSettingDialog
								.setOnConfirmListener(new UsrInfoNickNameSettingDialog.OnConfirmListener() {
									@Override
									public void OnConfirm(
											UsrInfoConfiguration config) {
										String formatString4 = getString(R.string.string_user_nickname_content);
										formatString4 = String.format(
												formatString4,
												config.getNickName());
										((TextView) findViewById(R.id.id_tv_setting_usr_info_nickname_content))
												.setText(formatString4);
										mLocalStorage.saveUsrInfoConfig(config);
										updateUsrNickInfo(formatString4);
										Log.i(TAG,"Robin---上传--昵称"+formatString4);
										Log.i(TAG,
												"Robin-------updateUsrNickInfo");
										if (mCentralService != null) {
											Log.e(TAG,
													"ENCODE: "
															+ Utils.bytesToHexString(config
																	.getEncode()));
											mCentralService
													.requestConnectionConfigFuncions(
															CentralService.DEVICE_CMD_ADDR_USR_INFO,
															config.getEncode());
										}
									}
								});
						mUsrInfoNickNameSettingDialog.show();
					}
				});

	}

	// 退出当前Activity或者跳转到新Activity时被调用
	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "SettingUsrInfoActivity --- ------> onStop called.");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "SettingUsrInfoActivity --- ------> onDestroy");
		unbindCentralService();
	}

	private void bindCentralService() {
		Intent it = new Intent(SettingUsrInfoActivity.this,
				CentralService.class);
		bindService(it, mServiceConnection, BIND_AUTO_CREATE);
	}

	private void unbindCentralService() {
		unbindService(mServiceConnection);
	}

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			Log.i(TAG,
					"CentralServiceConnection&SettingUsrInfoActivity --- onServiceConnected");
			mCentralService = ((CentralService.LocalBinder) service)
					.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mCentralService = null;
		}
	};

	private void enterLoginActivity() {
		mCentralService
				.broadcastUpdate(CentralService.ACTION_ACTIONS_ACTIVITY_IN_STACK_ALL_CLEAR);

		Intent intent = new Intent(SettingUsrInfoActivity.this,
				LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
		startActivity(intent);
		overridePendingTransition(R.anim.translate_slide_in_right,
				R.anim.translate_slide_out_left);
		ManagerActivity.instance.finish(); // 关闭指定Activity,再次进入为首页的页面
		finish();
	}

	private void updateUsrSexInfo(int sex) {
		// 检查网络是否可用
		if (!isNetworkAvailable(getApplicationContext())) {
			Toast toast = Toast.makeText(getApplicationContext(),
					R.string.change_user_info_no_network_fail, Toast.LENGTH_SHORT);
//			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}

		new NetworkUtils(this, new NetworkCallback() {
			@Override
			public void OnPreExcute() {
				Log.e(TAG, "onPreExcute");
			}

			@Override
			public void OnExcuteSuccess(JSONObject obj) throws JSONException {
				Log.e(TAG,
						"updateUsrSexInfo-----OnExcuteSuccess:"
								+ obj.toString());
			}

			@Override
			public void OnExcuteFailure(JSONObject obj) {
				Log.e(TAG, "OnExcuteFailure");
			}

			@Override
			public void OnTimeOut() {
				Log.e(TAG, "OnTimeOut");
			}
		}).request(NetworkUtils.POST, Urls.USERINFO_SEX, Integer.toString(sex),
				mLocalStorage.getSID());
	}

	private void updateUsrHeightInfo(int height) {
		// 检查网络是否可用
		if (!isNetworkAvailable(getApplicationContext())) {
			Toast toast = Toast.makeText(getApplicationContext(),
					R.string.change_user_info_no_network_fail, Toast.LENGTH_SHORT);
//			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}

		new NetworkUtils(this, new NetworkCallback() {
			@Override
			public void OnPreExcute() {
				Log.e(TAG, "onPreExcute");
			}

			@Override
			public void OnExcuteSuccess(JSONObject obj) throws JSONException {
				Log.e(TAG, "updateUsrHeightInfo----------OnExcuteSuccess:"
						+ obj.toString());
			}

			@Override
			public void OnExcuteFailure(JSONObject obj) {
				Log.e(TAG, "OnExcuteFailure");
			}

			@Override
			public void OnTimeOut() {
				Log.e(TAG, "OnTimeOut");
			}
		}).request(NetworkUtils.POST, Urls.USERINFO_HEIGHT,
				Integer.toString(height), mLocalStorage.getSID());
	}

	private void updateUsrWeightInfo(int weight) {
		// 检查网络是否可用
		if (!isNetworkAvailable(getApplicationContext())) {
			Toast toast = Toast.makeText(getApplicationContext(),
					R.string.change_user_info_no_network_fail, Toast.LENGTH_SHORT);
//			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}

		new NetworkUtils(this, new NetworkCallback() {
			@Override
			public void OnPreExcute() {
				Log.e(TAG, "onPreExcute");
			}

			@Override
			public void OnExcuteSuccess(JSONObject obj) throws JSONException {
				Log.e(TAG,
						"updateUsrWeightInfo------OnExcuteSuccess:"
								+ obj.toString());
			}

			@Override
			public void OnExcuteFailure(JSONObject obj) {
				Log.e(TAG, "OnExcuteFailure");
			}

			@Override
			public void OnTimeOut() {
				Log.e(TAG, "OnTimeOut");
			}
		}).request(NetworkUtils.POST, Urls.USERINFO_WEIGHT,
				Integer.toString(weight), mLocalStorage.getSID());
	}

	private void updateUsrBirthdayInfo(String birthday) {
		// 检查网络是否可用
		if (!isNetworkAvailable(getApplicationContext())) {
			Toast toast = Toast.makeText(getApplicationContext(),
					R.string.change_user_info_no_network_fail, Toast.LENGTH_SHORT);
//			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}

		new NetworkUtils(this, new NetworkCallback() {
			@Override
			public void OnPreExcute() {
				Log.e(TAG, "onPreExcute");
			}

			@Override
			public void OnExcuteSuccess(JSONObject obj) throws JSONException {
				Log.e(TAG, "updateUsrBirthdayInfo--------OnExcuteSuccess:"
						+ obj.toString());
			}

			@Override
			public void OnExcuteFailure(JSONObject obj) {
				Log.e(TAG, "OnExcuteFailure");
			}

			@Override
			public void OnTimeOut() {
				Log.e(TAG, "OnTimeOut");
			}
		}).request(NetworkUtils.POST, Urls.USERINFO_BIRTHDAY, birthday,
				mLocalStorage.getSID());
	}

	private void updateUsrNickInfo(String nickname) {
		// 检查网络是否可用
		if (!isNetworkAvailable(getApplicationContext())) {
			Toast toast = Toast.makeText(getApplicationContext(),
					R.string.change_user_info_no_network_fail, Toast.LENGTH_SHORT);
//			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}

		new NetworkUtils(this, new NetworkCallback() {
			@Override
			public void OnPreExcute() {
				Log.e(TAG, "onPreExcute");
			}

			@Override
			public void OnExcuteSuccess(JSONObject obj) throws JSONException {
				Log.e(TAG,
						"updateUsrNickInfo------OnExcuteSuccess:"
								+ obj.toString());
			}

			@Override
			public void OnExcuteFailure(JSONObject obj) {
				Log.e(TAG, "OnExcuteFailure");
			}

			@Override
			public void OnTimeOut() {
				Log.e(TAG, "OnTimeOut");
			}
		}).request(NetworkUtils.POST, Urls.USERINFO_NICKNAME, nickname,
				mLocalStorage.getSID());
	}

	private void updateMyImage(String avatar) {
		String savepath = android.os.Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/fooband/avatar/";
		new NetworkUtils(this, new NetworkCallback() {
			@Override
			public void OnPreExcute() {
				Log.e(TAG, "onPreExcute");
			}

			@Override
			public void OnExcuteSuccess(JSONObject obj) throws JSONException {
				Log.e(TAG, "OnExcuteSuccess:" + obj.toString());
			}

			@Override
			public void OnExcuteFailure(JSONObject obj) {
				Log.e(TAG, "OnExcuteFailure:" + obj.toString());
			}

			@Override
			public void OnTimeOut() {
				Log.e(TAG, "OnTimeOut");
			}
		}).getMyAvatar(Urls.USER_HEAD, mLocalStorage.getSID(), savepath, avatar);
	}

	private void uploadMyImage(String filepath, String avatar) {
		File file = new File(filepath, avatar);
		new NetworkUtils(this, new NetworkCallback() {
			@Override
			public void OnPreExcute() {
				Log.e(TAG, "onPreExcute");
			}

			@Override
			public void OnExcuteSuccess(JSONObject obj) throws JSONException {
				Log.e(TAG, "OnExcuteSuccess:" + obj.toString());

			}

			@Override
			public void OnExcuteFailure(JSONObject obj) {
				Log.e(TAG, "OnExcuteFailure:" + obj.toString());
			}

			@Override
			public void OnTimeOut() {
				Log.e(TAG, "OnTimeOut");
			}
		}).postMyAvatar(Urls.POST_MY_IMAGE, file, mLocalStorage.getSID(),
				Urls.mapk);
	}

	private void downloadUsrInfo() {
		new NetworkUtils(this, new NetworkCallback() {
			@Override
			public void OnPreExcute() {
				Log.e(TAG, "onPreExcute");
			}

			@Override
			public void OnExcuteSuccess(JSONObject obj) throws JSONException {
				JSONObject usrObj = obj.getJSONObject("user");
				Log.e(TAG, "OnExcuteSuccess:" + obj.toString());
				formatString0 = getString(R.string.string_user_height_content);
				formatString0 = String.format(formatString0,
						usrObj.getInt("height"));
				((TextView) findViewById(R.id.id_tv_setting_usr_info_height_content))
						.setText(formatString0);

				formatString1 = getString(R.string.string_user_weight_content);
				formatString1 = String.format(formatString1,
						usrObj.getInt("weight"));
				((TextView) findViewById(R.id.id_tv_setting_usr_info_weight_content))
						.setText(formatString1);

				String man = getString(R.string.string_user_sexy_man);
				String woman = getString(R.string.string_user_sexy_woman);
				formatString2 = getString(R.string.string_user_sexy_content);
				formatString2 = String.format(formatString2,
						(usrObj.getInt("gender") == 0) ? woman : man);
				((TextView) findViewById(R.id.id_tv_setting_usr_info_sexy_content))
						.setText(formatString2);

				formatString3 = getString(R.string.string_user_birthday_content);
				formatString3 = String.format(formatString3,
						usrObj.getString("birthday"));
				((TextView) findViewById(R.id.id_tv_setting_usr_info_birthday_content))
						.setText(formatString3);

				formatString4 = getString(R.string.string_user_id_content);
				formatString4 = String.format(formatString4,
						usrObj.getString("userId"));
				((TextView) findViewById(R.id.id_tv_setting_usr_info_id_content))
						.setText(formatString4);

				formatString5 = getString(R.string.string_user_nickname_content);
				formatString5 = String.format(formatString5,
						usrObj.getString("nickname"));
				((TextView) findViewById(R.id.id_tv_setting_usr_info_nickname_content))
						.setText(formatString5);

				UsrInfoConfiguration config = mLocalStorage
						.restoreUsrInfoConfig();
				config.setBirthday(Utils.birthday2Utc(usrObj
						.getString("birthday")));
				config.setHeight(usrObj.getInt("height"));
				config.setWeight(usrObj.getInt("weight"));
				config.setSexy(usrObj.getInt("gender"));
				config.setId(usrObj.getString("userId"));
				config.setNickName(usrObj.getString("nickname"));
				mLocalStorage.saveUsrInfoConfig(config);

				updateMyImage(usrObj.getString("userId") + ".jpg");

				String savepath = android.os.Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/fooband/avatar/";
				Bitmap bm = BitmapFactory.decodeFile(
						savepath + usrObj.getString("userId") + ".jpg", null);
				if (bm != null) {
					mMyAvatar.setImageBitmap(bm);
				}
			}

			@Override
			public void OnExcuteFailure(JSONObject obj) {
				Log.e(TAG, "OnExcuteFailure:" + obj.toString());
				formatString0 = getString(R.string.string_user_height_content);
				formatString0 = String.format(formatString0, mLocalStorage
						.restoreUsrInfoConfig().getHeight());
				((TextView) findViewById(R.id.id_tv_setting_usr_info_height_content))
						.setText(formatString0);

				formatString1 = getString(R.string.string_user_weight_content);
				formatString1 = String.format(formatString1, mLocalStorage
						.restoreUsrInfoConfig().getWeight());
				((TextView) findViewById(R.id.id_tv_setting_usr_info_weight_content))
						.setText(formatString1);

				String man = getString(R.string.string_user_sexy_man);
				String woman = getString(R.string.string_user_sexy_woman);
				formatString2 = getString(R.string.string_user_sexy_content);
				formatString2 = String.format(formatString2, (mLocalStorage
						.restoreUsrInfoConfig().getSexy() == 0) ? woman : man);
				((TextView) findViewById(R.id.id_tv_setting_usr_info_sexy_content))
						.setText(formatString2);

				formatString3 = getString(R.string.string_user_birthday_content);
				formatString3 = String.format(formatString3, ActionsDatum
						.utc2DateTimeString(mLocalStorage
								.restoreUsrInfoConfig().getBirthday(),
								"yyyy-MM-dd", TimeZone.getDefault()));
				((TextView) findViewById(R.id.id_tv_setting_usr_info_birthday_content))
						.setText(formatString3);

				formatString4 = getString(R.string.string_user_id_content);
				formatString4 = String.format(formatString4, mLocalStorage
						.restoreUsrInfoConfig().getId());
				((TextView) findViewById(R.id.id_tv_setting_usr_info_id_content))
						.setText(formatString4);

				formatString5 = getString(R.string.string_user_nickname_content);
				formatString5 = String.format(formatString5, mLocalStorage
						.restoreUsrInfoConfig().getNickName());
				((TextView) findViewById(R.id.id_tv_setting_usr_info_nickname_content))
						.setText(formatString5);
			}

			@Override
			public void OnTimeOut() {
				Log.e(TAG, "OnTimeOut");
			}
		}).request(NetworkUtils.GET, Urls.USERINFO_INFO, mLocalStorage.getSID());
	}

	private void getUserInfoFromLocalStorage() {
		Log.i(TAG, "Robin ----- getUserInfoFromLocalStorage");
		formatString0 = getString(R.string.string_user_height_content);
		formatString0 = String.format(formatString0, mLocalStorage
				.restoreUsrInfoConfig().getHeight());
		((TextView) findViewById(R.id.id_tv_setting_usr_info_height_content))
				.setText(formatString0);

		formatString1 = getString(R.string.string_user_weight_content);
		formatString1 = String.format(formatString1, mLocalStorage
				.restoreUsrInfoConfig().getWeight());
		((TextView) findViewById(R.id.id_tv_setting_usr_info_weight_content))
				.setText(formatString1);

		String man = getString(R.string.string_user_sexy_man);
		String woman = getString(R.string.string_user_sexy_woman);
		formatString2 = getString(R.string.string_user_sexy_content);
		formatString2 = String.format(formatString2, (mLocalStorage
				.restoreUsrInfoConfig().getSexy() == 0) ? woman : man);
		((TextView) findViewById(R.id.id_tv_setting_usr_info_sexy_content))
				.setText(formatString2);

		formatString3 = getString(R.string.string_user_birthday_content);
		formatString3 = String.format(formatString3, ActionsDatum
				.utc2DateTimeString(mLocalStorage.restoreUsrInfoConfig()
						.getBirthday(), "yyyy-MM-dd", TimeZone.getDefault()));
		((TextView) findViewById(R.id.id_tv_setting_usr_info_birthday_content))
				.setText(formatString3);

		formatString4 = getString(R.string.string_user_id_content);
		formatString4 = String.format(formatString4, mLocalStorage
				.restoreUsrInfoConfig().getId());
		((TextView) findViewById(R.id.id_tv_setting_usr_info_id_content))
				.setText(formatString4);

		formatString5 = getString(R.string.string_user_nickname_content);
		formatString5 = String.format(formatString5, mLocalStorage
				.restoreUsrInfoConfig().getNickName());
		((TextView) findViewById(R.id.id_tv_setting_usr_info_nickname_content))
				.setText(formatString5);

		downloadUsrInfoAvatar();
	}

	private void downloadUsrInfoAvatar() {
		new NetworkUtils(this, new NetworkCallback() {
			@Override
			public void OnPreExcute() {
				Log.e(TAG, "onPreExcute");
			}

			@Override
			public void OnExcuteSuccess(JSONObject obj) throws JSONException {
				JSONObject usrObj = obj.getJSONObject("user");
				Log.e(TAG, "OnExcuteSuccess:" + obj.toString());

				String savepath = android.os.Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/fooband/avatar/";
				Bitmap bm = BitmapFactory.decodeFile(
						savepath + usrObj.getString("userId") + ".jpg", null);
				if (bm != null) {
					mMyAvatar.setImageBitmap(bm);
				}
			}

			@Override
			public void OnExcuteFailure(JSONObject obj) {
				Log.e(TAG, "OnExcuteFailure:" + obj.toString());
			}

			@Override
			public void OnTimeOut() {
				Log.e(TAG, "OnTimeOut");
			}
		}).request(NetworkUtils.GET, Urls.USERINFO_INFO, mLocalStorage.getSID());
	}

	// private static final int CAMERA_WITH_DATA = 3000; // 用来标识请求照相功能的activity
	// private static final int PHOTO_PICKED_WITH_DATA = 3001; //
	// 用来标识请求gallery的activity
	// private static final int PHOTO_CROP_DATA = 3002; // 用来标识裁剪图片的activity
	// private static int mRequestCode = CAMERA_WITH_DATA;
	private static final int PHOTO_PICKED_WITH_DATA = 1881;
	private static final int CAMERA_WITH_DATA = 1882;
	private static final int CAMERA_CROP_RESULT = 1883;
	private static final int PHOTO_CROP_RESOULT = 1884;

	/**
	 * 读取本地相册
	 */
	protected void pickPhotoFromGallery() {
		try {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
			intent.setType("image/*");
			startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "no phote");
		}
	}

	/**
	 * 拍照获取图片
	 */
	protected void getPicByTakePhoto() {
		try {
			String picName = mLocalStorage.restoreUsrInfoConfig().getId()
					+ ".jpg";
			// Log.i(TAG, "调用系统相机拍照"+picName);
			String savepath = android.os.Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ "/fooband/avatar/";
			// Log.i(TAG, "调用系统相机拍照"+savepath);
			File file = new File(savepath);
			if (!file.exists()) {
				file.mkdirs();
			}
			mCurrentPhotoFile = new File(file, picName);
			final Intent intent = getTakePickIntent(mCurrentPhotoFile);
			startActivityForResult(intent, CAMERA_WITH_DATA);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "no camera");
		}
	}

	/**
	 * Constructs an intent for capturing a photo and storing it in a temporary
	 * file.
	 */
	public static Intent getTakePickIntent(File f) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		return intent;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PHOTO_PICKED_WITH_DATA:
				// 相册选择图片后裁剪图片
				// startPhotoZoom(data.getData());
				break;
			case PHOTO_CROP_RESOULT:
				// Bundle extras = data.getExtras();
				// if (extras != null) {
				// imageBitmap = extras.getParcelable("data");
				// //imageBitmap.compress(Bitmap.CompressFormat.PNG, 100,
				// stream);
				// mMyAvatar.setImageBitmap(imageBitmap);
				// }
				break;
			case CAMERA_WITH_DATA:
				// 相机拍照后裁剪图片
				doCropPhoto(mCurrentPhotoFile);
				break;
			case CAMERA_CROP_RESULT:
				imageBitmap = data.getParcelableExtra("data");
				// imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
				mMyAvatar.setImageBitmap(imageBitmap);
				String picName = mLocalStorage.restoreUsrInfoConfig().getId()
						+ ".jpg";
				String savepath = android.os.Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/fooband/avatar/";
				Utils.saveBitmap(imageBitmap, savepath, picName);
				// 检查网络是否可用
				if (!isNetworkAvailable(getApplicationContext())) {
					Toast toast = Toast.makeText(getApplicationContext(),
							R.string.avatar_update_no_network_fail, Toast.LENGTH_SHORT);
//					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return;
				}
				uploadMyImage(savepath, picName);
				Log.i(TAG, "savepath"+savepath+"----picName"+picName);
				Toast.makeText(this, R.string.avatar_update_success,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}

	/**
	 * 相机剪切图片
	 */
	protected void doCropPhoto(File f) {
		try {
			// Add the image to the media store
			MediaScannerConnection.scanFile(this,
					new String[] { f.getAbsolutePath() },
					new String[] { null }, null);

			// Launch gallery to crop the photo
			final Intent intent = getCropImageIntent(Uri.fromFile(f));
			startActivityForResult(intent, CAMERA_CROP_RESULT);
		} catch (Exception e) {
			// Toast.makeText(this, R.string.photoPickerNotFoundText,
			// Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 获取系统剪裁图片的Intent.
	 */
	public static Intent getCropImageIntent(Uri photoUri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(photoUri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 234);
		intent.putExtra("outputY", 234);
		intent.putExtra("return-data", true);
		return intent;
	}

	/**
	 * 检测当的网络（WLAN、3G/2G）状态
	 * 
	 * @param context
	 *            Context
	 * @return true 表示网络可用
	 */
	public boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm == null) {
			return false;
		} else {
			// 获取NetworkInfo对象
			NetworkInfo[] networkInfo = cm.getAllNetworkInfo();
			if (networkInfo != null && networkInfo.length > 0) {
				for (int i = 0; i < networkInfo.length; i++) {
					// 判断当前网络状态是否为连接状态
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
