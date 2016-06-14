package com.foogeez.activity;

import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.foogeez.configuration.LocalStorage;
import com.foogeez.configuration.Configuration.UsrInfoConfiguration;
import com.foogeez.database.ActionsDatum;
import com.foogeez.dialog.UsrInfoBirthdaySettingDialog;
import com.foogeez.dialog.UsrInfoHeightSettingDialog;
import com.foogeez.dialog.UsrInfoSexySettingDialog;
import com.foogeez.dialog.UsrInfoWeightSettingDialog;
import com.foogeez.fooband.R;
import com.foogeez.https.Urls;
import com.foogeez.network.NetworkUtils;
import com.foogeez.network.NetworkUtils.NetworkCallback;
import com.foogeez.services.CentralService;
import com.grdn.util.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingUsrInfoActDetail extends SettingActivity {
	private final static String TAG = SettingUsrInfoActDetail.class
			.getSimpleName();

	private TextView btnSkip;
	private ImageView imgViewVisible;
	private Button btnNext;

	private CentralService mCentralService = null;

	private UsrInfoSexySettingDialog mUsrInfoSexySettingDialog = null;
	private UsrInfoHeightSettingDialog mUsrInfoHeightSettingDialog = null;
	private UsrInfoWeightSettingDialog mUsrInfoWeightSettingDialog = null;
	private UsrInfoBirthdaySettingDialog mUsrInfoBirthdaySettingDialog = null;

	private String formatString0 = null;
	private String formatString1 = null;
	private String formatString2 = null;
	private String formatString3 = null;
	
	private View viewHeight;
	private View viewWeight;
	private View viewSex;
	private View viewBirthday;

	private LocalStorage mLocalStorage = new LocalStorage(
			SettingUsrInfoActDetail.this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_usr_info_detail);

		Log.i(TAG, "Robin----------onCreate");

		initSettingTitle(R.string.setting_usr_info_act_detail_title);
		imgViewVisible = (ImageView) findViewById(R.id.id_iv_setting_back);
		imgViewVisible.setVisibility(View.GONE);

		btnNext = (Button) findViewById(R.id.id_btn_usr_info_detail_next);
		btnNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (viewHeight.getVisibility() == View.INVISIBLE) {
					Toast.makeText(getApplicationContext(), R.string.please_set_the_height, Toast.LENGTH_SHORT).show();
					return;
				}
				if (viewWeight.getVisibility() == View.INVISIBLE) {
					Toast.makeText(getApplicationContext(), R.string.please_set_the_weight, Toast.LENGTH_SHORT).show();
					return;
				}
				if (viewSex.getVisibility() == View.INVISIBLE) {
					Toast.makeText(getApplicationContext(), R.string.please_set_the_sex, Toast.LENGTH_SHORT).show();
					return;
				}
				if (viewBirthday.getVisibility() == View.INVISIBLE) {
					Toast.makeText(getApplicationContext(), R.string.please_set_the_birthday, Toast.LENGTH_SHORT).show();
					return;
				}
				enterManagerActivity();
			}
		});

		btnSkip = (TextView) findViewById(R.id.txt_title_skip);
		btnSkip.setVisibility(View.VISIBLE);
		btnSkip.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				enterManagerActivity();
			}
		});
		
		viewHeight = findViewById(R.id.view_judge_height);
		viewWeight = findViewById(R.id.view_judge_weight);
		viewSex = findViewById(R.id.view_judge_sex);
		viewBirthday = findViewById(R.id.view_judge_birthday);

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
								SettingUsrInfoActDetail.this, mLocalStorage
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
										viewHeight.setVisibility(View.GONE);
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
								SettingUsrInfoActDetail.this, mLocalStorage
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
										viewWeight.setVisibility(View.GONE);
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
								SettingUsrInfoActDetail.this, mLocalStorage
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
										viewSex.setVisibility(View.GONE);
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
								SettingUsrInfoActDetail.this, mLocalStorage
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
										viewBirthday.setVisibility(View.GONE);
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

	}

	private void updateUsrSexInfo(int sex) {
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

	private void enterManagerActivity() {
		Intent mainIntent = new Intent(SettingUsrInfoActDetail.this,
				ManagerActivity.class);
		startActivity(mainIntent);
		overridePendingTransition(R.anim.translate_slide_in_right,
				R.anim.translate_slide_out_left);
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			enterManagerActivity();
		}
		return true;
	}


}
