package com.foogeez.activity;

import com.foogeez.configuration.LocalStorage;
import com.foogeez.configuration.Configuration.DisturbModeConfiguration;
import com.foogeez.configuration.Configuration.FunctionConfiguration;
import com.foogeez.configuration.Configuration.UsrTargetConfiguration;
import com.foogeez.database.DatabaseHelper;
import com.foogeez.dialog.DisturbModeSettingDialog;
import com.foogeez.dialog.UserTargetSleepDialog;
import com.foogeez.dialog.UserTargetStepsDialog;
import com.foogeez.fooband.R;
import com.foogeez.services.CentralService;
import com.grdn.util.Utils;
import com.grdn.widgets.SwitchButton;
import com.jauker.widget.BadgeView;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingFragment extends Fragment {
	private final static String TAG = SettingFragment.class.getSimpleName();

	// private float width;
	// private float height;

	ImageView ivSettingBack = null;
	// private ScrollView mSvSettingContent = null;
	// private GestureDetector mGestureDetector = null;

	private CentralService mCentralService = null;

	private Button mBtnCheckUpdate = null;
	private Button mBtnBindRelease = null;

	private TextView mTextViewDeviceName = null;
	private TextView mTextViewDeviceHwvr = null;
	private TextView mTextViewDeviceFwvr = null;
	private TextView mTextViewDeviceSrln = null;

	private TextView mTextViewDisturbModeTime = null;

	private String mDeviceFwvr = null;

	private LocalStorage mLocalStorage = null;

	private final int NEW_VERSION_NONE = 0;
	private final int NEW_VERSION_EXSIST = 1;
	private final int NEW_VERSION_ERROR = 2;

	public static String ROM_FILE_FULL_NAME = null;
	public static String ROM_FILE_VERSION = null;

	private SwitchButton mDisturbModeActive;
	private DisturbModeSettingDialog mDisturbModeSetting;

	private UserTargetStepsDialog mStepsDialog;
	private TextView mTextViewSteps;
	private UserTargetSleepDialog mSleepHoursDialog;
	private TextView mTextViewSleepHours;

	private AlertDialog mDialog;

	private View redViewPoint;
	private BadgeView badgeView,badgeView1;
	
	 private SharedPreferences share;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View SettingLayout = inflater.inflate(R.layout.activity_setting_a,
				container, false);
		
		Log.i(TAG, "Robin-------- onCreateView");
		
		return SettingLayout;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "MainSettingFragment --- onCreate");

		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_setting_a);
		// initSettingTitle(R.string.string_global_setting);

		// DisplayMetrics dm = new DisplayMetrics();
		// getWindowManager().getDefaultDisplay().getMetrics(dm);
		// width = dm.widthPixels;//*dm.density;
		// height= dm.heightPixels;//*dm.density;
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(TAG, "MainSettingFragment --- onStart");

		mLocalStorage = new LocalStorage(getActivity());

		redViewPoint = (View) getActivity().findViewById(R.id.red_notice_view);

		mBtnCheckUpdate = (Button) getActivity().findViewById(
				R.id.id_btn_check_update);
		mBtnCheckUpdate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// mCentralService.checkRomByInternet();
				if (mLocalStorage.isDeviceActivated()) {
					mDialog = DialogShow(
							getString(R.string.string_dialog_check_newest_rom_version),
							getString(R.string.string_dialog_checking_newest_rom_version),
							null, null);
					mDialog.setCancelable(true);  //设置点击其他位置消失
					checkRomByInternet();
				} else {
					mCentralService
							.broadcastUpdate(CentralService.ACTION_ACTIONS_NO_ACTIVATED_DEVICE_WARNNING);
				}
				
			}
		});

		if (redViewPoint.isShown()) {
			showRedPoint();
		}

		mBtnBindRelease = (Button) getActivity().findViewById(
				R.id.id_btn_bind_release);
		mBtnBindRelease.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mLocalStorage.isDeviceActivated()) {
					DialogShow(getString(R.string.string_dialog_unpairs_title),
							"    " + mLocalStorage.getActivatedDeviceName(),
							// getString(R.string.string_dialog_pairs_message_header),
							mUnBindConfirmYesNo, mUnBindConfirmYesNo);
					setSharedPreference();
				} else {
					enterSettingSubActivity(SettingPairsActivity.class);
				}
			}
		});

		initUsrTarget();
		initFunctionsActived();
		initConfigurationsDetail();

		bindCentralService();
		getActivity().registerReceiver(mBroadcastReceiver, makeIntentFilter());

		updateUserAndDeviceInfo();
		updateDeviceInfo();

		if (mLocalStorage.isDeviceActivated()) {
			mBtnBindRelease
					.setText(R.string.string_btn_release_current_device_bond);
			return;
		} else {
			mBtnBindRelease
					.setText(R.string.string_btn_bind_new_device_with_user);
			return;
		}
	}

	 // 存储sharedpreferences
    public void setSharedPreference() {
    	Log.i(TAG, "Robin----存入数据--setSharedPreference");
    	share = getActivity().getSharedPreferences("record", Context.MODE_PRIVATE);
	    Editor editor = share.edit();
	    editor.putInt("recordID", 11010);
	    editor.commit();// 提交修改
    }
    
	@Override
	public void onStop() {
		Log.i(TAG, "Robin----------onStop()");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "Robin----------onDestroy()");
		super.onDestroy();

		unbindCentralService();
		getActivity().unregisterReceiver(mBroadcastReceiver);

	}

	/**
	 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
	 *           if(keyCode == KeyEvent.KEYCODE_BACK) { UIfinish(); return true;
	 *           } else { return super.onKeyDown(keyCode, event); } } /
	 **/

	private void updateUserAndDeviceInfo() {
		if (mCentralService == null) {
			Log.e(TAG, "mCentralService == null");
			return;
		}

		mCentralService.updateUserAndDeviceInfo();
	}

	private void initUsrTarget() {
		final UsrTargetConfiguration target = mLocalStorage
				.restoreUsrTargetConfig();
		if (target == null)
			return;

		/** sports -- steps */
		((RelativeLayout) getActivity().findViewById(
				R.id.id_rl_setting_targets_sport))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mStepsDialog = new UserTargetStepsDialog(getActivity(),
								mLocalStorage.restoreUsrTargetConfig());
						Window dialogWindow = mStepsDialog.getWindow();
						dialogWindow.setGravity(Gravity.CENTER);
						mStepsDialog.setCanceledOnTouchOutside(false);
						mStepsDialog
								.setOnConfirmListener(new UserTargetStepsDialog.OnConfirmListener() {
									@Override
									public void OnConfirm(
											UsrTargetConfiguration config) {
										String targetSteps = getString(R.string.string_steps_content);
										targetSteps = String.format(
												targetSteps, config.getSteps());

										mTextViewSteps.setText(targetSteps);
										mLocalStorage
												.saveUsrTargetConfig(config);

										Log.i(TAG,
												"-------Robin--mLocalStorage.saveUsrTargetConfig----"
														+ config);
									}
								});
						mStepsDialog.show();
					}
				});

		/** sleep----hours */
		((RelativeLayout) getActivity().findViewById(
				R.id.id_rl_setting_targets_sleep))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mSleepHoursDialog = new UserTargetSleepDialog(
								getActivity(), mLocalStorage
										.restoreUsrTargetConfig());
						Window dialogWindow = mSleepHoursDialog.getWindow();
						dialogWindow.setGravity(Gravity.CENTER);
						mSleepHoursDialog.setCanceledOnTouchOutside(false);
						mSleepHoursDialog
								.setOnConfirmListener(new UserTargetSleepDialog.OnConfirmListener() {
									@Override
									public void OnConfirm(
											UsrTargetConfiguration config) {
										String targetSleepsHours = getString(R.string.string_sleep_hours_content);
										targetSleepsHours = String.format(
												targetSleepsHours,
												config.getHours());

										mTextViewSleepHours
												.setText(targetSleepsHours);
										mLocalStorage
												.saveUsrTargetConfig(config);

										Log.i(TAG,
												"---Robin--Hours--mLocalStorage.saveUsrTargetConfig--"
														+ config);
									}
								});
						mSleepHoursDialog.show();
					}
				});
	}

	private void initFunctionsActived() {

		final FunctionConfiguration config = mLocalStorage
				.restoreFunctionConfig();
		if (config == null)
			return;

		/**
		 * Log.e(TAG, "config0: " + config.getCallingRemainder()); Log.e(TAG,
		 * "config1: " + config.getMessageRemainder()); Log.e(TAG, "config2: " +
		 * config.getDisplayFormat24()); Log.e(TAG, "config3: " +
		 * config.getDisplayDistance()); Log.e(TAG, "config4: " +
		 * config.getDisplayCaloric()); Log.e(TAG, "config5: " +
		 * config.getDisplaySleepTime());
		 **/

		((SwitchButton) getActivity().findViewById(R.id.id_sb_calling_reminder))
				.setChecked(config.getCallingRemainder());
		((SwitchButton) getActivity().findViewById(R.id.id_sb_calling_reminder))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Log.e(TAG, "onCheckedChanged0");
						config.setCallingRemainder(isChecked);
						mLocalStorage.saveFunctionConfig(config);

						if (mCentralService != null) {
							mCentralService.requestAppConfigFunctions();
						}
					}
				});

		((SwitchButton) getActivity().findViewById(R.id.id_sb_message_reminder))
				.setChecked(config.getMessageRemainder());
		((SwitchButton) getActivity().findViewById(R.id.id_sb_message_reminder))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Log.e(TAG, "onCheckedChanged1");
						config.setMessageRemainder(isChecked);
						mLocalStorage.saveFunctionConfig(config);

						if (mCentralService != null) {
							mCentralService.requestAppConfigFunctions();
						}
					}
				});

		/**
		 * ((SwitchButton)getActivity().findViewById(R.id.
		 * id_sb_message_reminder_qq
		 * )).setChecked(config.getMessageRemainderQQ());
		 * ((SwitchButton)getActivity
		 * ().findViewById(R.id.id_sb_message_reminder_qq
		 * )).setOnCheckedChangeListener(new OnCheckedChangeListener() {
		 * 
		 * @Override public void onCheckedChanged(CompoundButton buttonView,
		 *           boolean isChecked) { Log.e(TAG, "onCheckedChanged1");
		 *           config.setMessageRemainderQQ(isChecked);
		 *           mLocalStorage.saveFunctionConfig(config);
		 * 
		 *           if( mCentralService != null ) {
		 *           mCentralService.requestAppConfigFunctions(); } } });
		 * 
		 *           ((SwitchButton)getActivity().findViewById(R.id.
		 *           id_sb_message_reminder_wechat
		 *           )).setChecked(config.getMessageRemainderWechat());
		 *           ((SwitchButton)getActivity().findViewById(R.id.
		 *           id_sb_message_reminder_wechat
		 *           )).setOnCheckedChangeListener(new OnCheckedChangeListener()
		 *           {
		 * @Override public void onCheckedChanged(CompoundButton buttonView,
		 *           boolean isChecked) { Log.e(TAG, "onCheckedChanged1");
		 *           config.setMessageRemainderWechat(isChecked);
		 *           mLocalStorage.saveFunctionConfig(config);
		 * 
		 *           if( mCentralService != null ) {
		 *           mCentralService.requestAppConfigFunctions(); } } });
		 **/

		((SwitchButton) getActivity()
				.findViewById(R.id.id_sb_stop_watch_enable)).setChecked(config
				.getStopWatchEnable());
		((SwitchButton) getActivity()
				.findViewById(R.id.id_sb_stop_watch_enable))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Log.e(TAG, "onCheckedChanged1");
						config.setStopWatchEnable(isChecked);
						mLocalStorage.saveFunctionConfig(config);
						mCentralService
								.requestConnectionConfigFuncions(
										CentralService.DEVICE_CMD_ADDR_STOP_WATCH_ENABLE,
										isChecked ? 1 : 0);
					}
				});

		((SwitchButton) getActivity().findViewById(
				R.id.id_sb_daylight_saving_time)).setChecked(config
				.getDaylightSavingTime());
		((SwitchButton) getActivity().findViewById(
				R.id.id_sb_daylight_saving_time))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Log.e(TAG, "onCheckedChanged2");
						config.setDaylightSavingTime(isChecked);
						mLocalStorage.saveFunctionConfig(config);
						mCentralService.requestConnectionConfigFuncions(
								CentralService.DEVICE_CMD_ADDR_SAVING_TIME,
								isChecked ? 1 : 0);
					}
				});

		((SwitchButton) getActivity().findViewById(
				R.id.id_sb_time_format_24_display)).setChecked(config
				.getDisplayFormat24());
		((SwitchButton) getActivity().findViewById(
				R.id.id_sb_time_format_24_display))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Log.e(TAG, "onCheckedChanged2");
						config.setDisplayFormat24(isChecked);
						mLocalStorage.saveFunctionConfig(config);
						mCentralService.requestConnectionConfigFuncions(
								CentralService.DEVICE_CMD_ADDR_TIME_FORMAT24,
								isChecked ? 0 : 1);
					}
				});

		((SwitchButton) getActivity().findViewById(R.id.id_sb_date_display))
				.setChecked(config.getDisplayDateWeek());
		((SwitchButton) getActivity().findViewById(R.id.id_sb_date_display))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Log.e(TAG, "onCheckedChanged3");
						config.setDisplayDateWeek(isChecked);
						mLocalStorage.saveFunctionConfig(config);
						mCentralService
								.requestConnectionConfigFuncions(
										CentralService.DEVICE_CMD_ADDR_DISPLAY_DATEWEEK,
										isChecked ? 1 : 0);
					}
				});

		((SwitchButton) getActivity().findViewById(R.id.id_sb_distance_display))
				.setChecked(config.getDisplayDistance());
		((SwitchButton) getActivity().findViewById(R.id.id_sb_distance_display))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Log.e(TAG, "onCheckedChanged3");
						config.setDisplayDistance(isChecked);
						mLocalStorage.saveFunctionConfig(config);
						mCentralService
								.requestConnectionConfigFuncions(
										CentralService.DEVICE_CMD_ADDR_DISPLAY_DISTANCE,
										isChecked ? 1 : 0);
					}
				});

		((SwitchButton) getActivity().findViewById(R.id.id_sb_caloric_display))
				.setChecked(config.getDisplayCaloric());
		((SwitchButton) getActivity().findViewById(R.id.id_sb_caloric_display))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Log.e(TAG, "onCheckedChanged4");
						config.setDisplayCaloric(isChecked);
						mLocalStorage.saveFunctionConfig(config);
						mCentralService.requestConnectionConfigFuncions(
								CentralService.DEVICE_CMD_ADDR_DISPLAY_CALORIC,
								isChecked ? 1 : 0);
					}
				});

		((SwitchButton) getActivity().findViewById(
				R.id.id_sb_sleep_time_display)).setChecked(config
				.getDisplaySleepTime());
		((SwitchButton) getActivity().findViewById(
				R.id.id_sb_sleep_time_display))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Log.e(TAG, "onCheckedChanged5");
						config.setDisplaySleepTime(isChecked);
						mLocalStorage.saveFunctionConfig(config);
						mCentralService
								.requestConnectionConfigFuncions(
										CentralService.DEVICE_CMD_ADDR_DISPLAY_SLEEPTIME,
										isChecked ? 1 : 0);
					}
				});

		final DisturbModeConfiguration config2 = mLocalStorage
				.restoreDisturbModeConfig();

		mTextViewDisturbModeTime = (TextView) getActivity().findViewById(
				R.id.id_tv_disturb_mode_content);
		mDisturbModeActive = (SwitchButton) getActivity().findViewById(
				R.id.id_sb_no_disturb_mode);
		mDisturbModeActive.setChecked(config2.getActivated());
		mDisturbModeActive
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						config2.setActivated(isChecked);
						mLocalStorage.saveDisturbModeConfig(config2);
						mTextViewDisturbModeTime.setTextColor(config2
								.getActivated() ? getResources().getColor(
								R.color.black) : getResources().getColor(
								R.color.gray));
						upgdateDisturbMode(config2, false, true);
					}
				});

		initDisturbMode(mLocalStorage.restoreDisturbModeConfig());
		upgdateDisturbMode(mLocalStorage.restoreDisturbModeConfig(), false,
				false);

		// ////////////////////////////////////////////////
		final UsrTargetConfiguration target = mLocalStorage
				.restoreUsrTargetConfig();

		/** Name:Robin Time:20150922 Function:显示步数的文本框---已经设置好的步数 */
		mTextViewSteps = (TextView) getActivity().findViewById(
				R.id.id_tv_setting_steps_content);
		mTextViewSleepHours = (TextView) getActivity().findViewById(
				R.id.id_tv_setting_sleep_hours_content);

		String targetSteps = getString(R.string.string_steps_content);
		String targetSleepsHours = getString(R.string.string_sleep_hours_content);

		targetSteps = String.format(targetSteps, target.getSteps());
		targetSleepsHours = String.format(targetSleepsHours, target.getHours());

		mTextViewSteps.setText(targetSteps);
		mTextViewSleepHours.setText(targetSleepsHours);
	}

	private void initDisturbMode(final DisturbModeConfiguration configuration) {
		((RelativeLayout) getActivity().findViewById(R.id.RelativeLayout18))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mDisturbModeSetting = new DisturbModeSettingDialog(
								getActivity(), configuration);
						Window dialogWindow = mDisturbModeSetting.getWindow();
						dialogWindow.setGravity(Gravity.CENTER);
						mDisturbModeSetting.setCanceledOnTouchOutside(false);
						mDisturbModeSetting
								.setOnConfirmListener(new DisturbModeSettingDialog.OnConfirmListener() {
									@Override
									public void OnConfirm(
											DisturbModeConfiguration config) {
										boolean flag = config.getActivated();
										config.setActivated(true);
										upgdateDisturbMode(config, true,
												flag == true);
									}
								});
						mDisturbModeSetting.show();
					}
				});
	}

	private void upgdateDisturbMode(DisturbModeConfiguration config,
			boolean save, boolean settingFlag) {
		if (save) {
			mLocalStorage.saveDisturbModeConfig(config);
		}

		mDisturbModeActive.setChecked(config.getActivated());
		int resourceId = R.string.string_setting_no_disturb_time_content1;
		if (config.getStart() >= config.getStop())
			resourceId = R.string.string_setting_no_disturb_time_content0;
		String format = String.format(getResources().getString(resourceId),
				config.getStart(), config.getStop());
		mTextViewDisturbModeTime
				.setTextColor(config.getActivated() ? getResources().getColor(
						R.color.black) : getResources().getColor(R.color.gray));
		mTextViewDisturbModeTime.setText(format);

		if ((mCentralService != null) && settingFlag) {
			mCentralService.requestConnectionConfigFuncions(
					CentralService.DEVICE_CMD_ADDR_DISTURB_MODE_ENABLE,
					config.getEncode());
		}
	}

	private void initConfigurationsDetail() {
		((RelativeLayout) getActivity().findViewById(
				R.id.id_rl_setting_usr_info_enter))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						enterSettingSubActivity(SettingUsrInfoActivity.class);
					}
				});

		((RelativeLayout) getActivity().findViewById(R.id.id_rl_setting_alarm))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						enterSettingSubActivity(SettingAlarmActivity.class);
					}
				});

		((RelativeLayout) getActivity().findViewById(
				R.id.id_rl_setting_calendar))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						enterSettingSubActivity(SettingCalendarActivity.class);
					}
				});

		((RelativeLayout) getActivity().findViewById(
				R.id.id_rl_setting_sedentary))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						enterSettingSubActivity(SettingSedentaryActivity.class);
					}
				});

		((RelativeLayout) getActivity().findViewById(R.id.id_rl_app_about))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						enterSettingSubActivity(SettingAppAboutActivity.class);
					}
				});

	}

	private AlertDialog DialogShow(String title, String message,
			DialogInterface.OnClickListener ok,
			DialogInterface.OnClickListener cancel) {
		Builder builder = new AlertDialog.Builder(getActivity());
		if (title != null)
			builder.setTitle(title);
		if (message != null)
			builder.setMessage(message);
		if (ok != null)
			builder.setPositiveButton(R.string.string_dialog_positive, ok);
		if (cancel != null)
			builder.setNegativeButton(R.string.string_dialog_negative, cancel);
		builder.setCancelable(false);
		return builder.show();
	}

	DialogInterface.OnClickListener mConfigConfirm = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				dialog.dismiss();
				break;
			}
		}
	};

	DialogInterface.OnClickListener mUnBindConfirmYesNo = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {

			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				if (mCentralService != null) {
					mCentralService.requestUnbond();
					mCentralService.clearBluetoothDevice();
				}
				mLocalStorage.clearDeviceActivated();
				updateDeviceInfo();

				if (mLocalStorage.isDeviceActivated()) {
					mBtnBindRelease
							.setText(R.string.string_btn_release_current_device_bond);
				} else {
					mBtnBindRelease
							.setText(R.string.string_btn_bind_new_device_with_user);
				}
				// UIfinish();
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				// Nothing to do
				break;
			}
		}
	};

	DialogInterface.OnClickListener mNewVersionConfirmYes = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {

			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}
	};

	DialogInterface.OnClickListener mNewVersionUpgradeYesNo = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				mCentralService.upgradeRomRequest(ROM_FILE_FULL_NAME,
						ROM_FILE_VERSION);
				
				if (redViewPoint.getVisibility() == View.VISIBLE) {
					notShowRedPoint();
				}
				
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}
	};

	DialogInterface.OnClickListener mNetworkErrorYes = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				mDialog.dismiss();
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}
	};

	public void checkRomByInternet() {
		//检查网络是否可用
		if (!isNetworkAvailable(getActivity())) {
			mDialog.dismiss();
			Toast toast = Toast.makeText(getActivity(), R.string.no_network_state_notice, Toast.LENGTH_SHORT);
//			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}
		
		new Thread(new Runnable() {
			@SuppressLint("DefaultLocale")
			@Override
			public void run() {
				Message msg = new Message();
				Bundle data = new Bundle();

				String romURL = CentralService.ROM_IMAGE + "?utc="
						+ Utils.getUtc();
				String infoURL = CentralService.ROM_IMAGE_INF + "?utc="
						+ Utils.getUtc();

				String path = new DatabaseHelper(getActivity(), null, null)
						.getDatabasePath() + "/rom";
				Log.i(TAG, "Robin---path" + path);
				String info = Utils.downloadFile2String(path, infoURL);
				if (info == null) {
					mDialog.dismiss();
					msg.what = NEW_VERSION_ERROR;
					handler.sendMessage(msg);
					return;
				}

				String md5 = info.substring(info.indexOf(":") + 1,
						info.indexOf("\n") - 1);
				String ver = info.substring(info.lastIndexOf(":") + 1);
				String rom = Utils.downloadFileAndCaculateMd5(path, romURL);
				if (rom == null) {
					mDialog.dismiss();
					msg.what = NEW_VERSION_ERROR;
					handler.sendMessage(msg);
					return;
				}

				mDeviceFwvr = mLocalStorage.getActivatedDeviceFwvr();

				Log.e(TAG, "ver = " + ver);
				Log.e(TAG, "cur = "
						+ mDeviceFwvr.toUpperCase().replace(".", ""));
				if ((ver.toUpperCase().compareTo(
						mDeviceFwvr.toUpperCase().replace(".", "")) > 0) // 检测固件更新是否为最新版本
						|| (mDeviceFwvr.equalsIgnoreCase("N/A"))) {
					data.putString(
							"ROM_FILE_NAME",
							path
									+ "/"
									+ romURL.substring(
											romURL.lastIndexOf("/") + 1,
											romURL.indexOf("?")));
					data.putString("ROM_INFO_MD5", md5);
					data.putString("ROM_INFO_VER", Utils.addVersionDot(ver));
					mDeviceFwvr = Utils.addVersionDot(ver);
					data.putString("ROM_FILE_MD5", rom);
					msg.setData(data);
					msg.what = NEW_VERSION_EXSIST;
					handler.sendMessage(msg);
				} else {
					msg.what = NEW_VERSION_NONE;
					handler.sendMessage(msg);
				}
				mDialog.dismiss();
			}
		}).start();
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case NEW_VERSION_EXSIST:
				Bundle data = msg.getData();
				String val0 = data.getString("ROM_INFO_MD5");
				String val1 = data.getString("ROM_INFO_VER");
				String val2 = data.getString("ROM_FILE_MD5");
				if (val0.equalsIgnoreCase(val2)) {
					ROM_FILE_VERSION = val1;
					ROM_FILE_FULL_NAME = data.getString("ROM_FILE_NAME");
					Log.e(TAG, "DOWNLOAD ROM FILE SUCCESS!!! ---"
							+ ROM_FILE_FULL_NAME);
					Log.e(TAG,
							"ROM FILE URI:"
									+ Utils.fileToUri(ROM_FILE_FULL_NAME));
					Log.e(TAG, "MD5 OK: MD5(RomFile)=" + val2);
					Log.e(TAG, "      : MD5(InfFile)=" + val0);
					DialogShow(
							getString(R.string.string_dialog_title_upgrade_rom)
									+ "  " + val1,
							getString(R.string.string_dialog_message_upgrade_rom),
							mNewVersionUpgradeYesNo, mNewVersionUpgradeYesNo);
				} else {
					Log.e(TAG, "MD5 ERR: MD5(RomFile)=" + val2);
					Log.e(TAG, "       : MD5(InfFile)=" + val0);
				}
				break;
			case NEW_VERSION_NONE:
				DialogShow(
						getString(R.string.string_dialog_positive),
						getString(R.string.string_dialog_current_rom_is_newest),
						mNetworkErrorYes, null);
				if (redViewPoint.getVisibility() == View.VISIBLE) {
					notShowRedPoint();
				}
				break;
			case NEW_VERSION_ERROR:
				DialogShow(getString(R.string.httpError),
						getString(R.string.string_network_timeout),
						mNetworkErrorYes, null);
				break;

			}
		}
	};

	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			Log.i(TAG, action);
			if (action
					.equals(CentralService.ACTION_ACTIONS_ACTIVITY_IN_STACK_ALL_CLEAR)) {
				// finish();
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_ENTER_DFU_IN_MANAGE_ACTIVITY)) {
				Log.e(TAG, "IN ENTER: getFragmentId() = "
						+ ((ManagerActivity) getActivity()).getFragmentId());
				if (((ManagerActivity) getActivity()).getFragmentId() == 4)
					mCentralService.initUpdateRomDialog(getActivity());
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_CONFIRM_DFU_IN_MANAGE_ACTIVITY)) {
				Log.e(TAG, "IN CONFIRM: getFragmentId() = "
						+ ((ManagerActivity) getActivity()).getFragmentId());
				if (((ManagerActivity) getActivity()).getFragmentId() == 4)
					mCentralService.initConfirmUpdateRomDialog(getActivity());
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_ENTER_DFU_SUCCESS)) {
				mTextViewDeviceFwvr.setText(String.format(
						getString(R.string.string_device_fw_version),
						mDeviceFwvr));
			}
		}
	};

	private static IntentFilter makeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_ACTIVITY_IN_STACK_ALL_CLEAR);
		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_ENTER_DFU_IN_MANAGE_ACTIVITY);
		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_CONFIRM_DFU_IN_MANAGE_ACTIVITY);
		intentFilter.addAction(CentralService.ACTION_ACTIONS_ENTER_DFU_SUCCESS);
		return intentFilter;
	}

	String name = null;

	// String fwvr = null;

	private void updateDeviceInfo() {
		name = mLocalStorage.getActivatedDeviceName();
		mDeviceFwvr = mLocalStorage.getActivatedDeviceFwvr();
		String hwvr = mLocalStorage.getActivatedDeviceHwvr();
		String srln = mLocalStorage.getActivatedDeviceSrln();

		mTextViewDeviceName = (TextView) getActivity().findViewById(
				R.id.id_tv_device_info_device_name);
		mTextViewDeviceHwvr = (TextView) getActivity().findViewById(
				R.id.id_tv_device_info_device_hwvr);
		mTextViewDeviceFwvr = (TextView) getActivity().findViewById(
				R.id.id_tv_device_info_device_fwvr);
		mTextViewDeviceSrln = (TextView) getActivity().findViewById(
				R.id.id_tv_device_info_device_srln);

		String formatString0 = getString(R.string.string_device_name);
		formatString0 = String.format(formatString0, name);
		mTextViewDeviceName.setText(formatString0);

		String formatString1 = getString(R.string.string_device_hw_version);
		formatString1 = String.format(formatString1, hwvr);
		mTextViewDeviceHwvr.setText(formatString1);

		String formatString2 = getString(R.string.string_device_fw_version);
		formatString2 = String.format(formatString2, mDeviceFwvr);
		mTextViewDeviceFwvr.setText(formatString2);

		String formatString3 = getString(R.string.string_device_serial_number);
		formatString3 = String.format(formatString3, srln);
		mTextViewDeviceSrln.setText(formatString3);
	}

	private void bindCentralService() {
		Intent it = new Intent(getActivity(), CentralService.class);
		getActivity().bindService(it, mServiceConnection,
				getActivity().BIND_AUTO_CREATE);
	}

	private void unbindCentralService() {
		getActivity().unbindService(mServiceConnection);
	}

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			Log.i(TAG,
					"CentralServiceConnection&MainSettingActivity --- onServiceConnected");
			mCentralService = ((CentralService.LocalBinder) service)
					.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mCentralService = null;
		}
	};
	
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

	private void enterSettingSubActivity(Class<?> cls) {
		Intent intent = new Intent();
		intent.setClass(getActivity(), cls);
		startActivity(intent);
		getActivity().overridePendingTransition(
				R.anim.translate_slide_in_right,
				R.anim.translate_slide_out_left);
	}

	private void showRedPoint() {
		/** 20151029 Robin ----设置消息提示的红点  --报空指针因为没有new 对象*/
		badgeView = new BadgeView(getActivity());
		badgeView.setTargetView(mBtnCheckUpdate);
		badgeView.setBadgeCount(1);
		badgeView.setWidth(5);
		badgeView.setHeight(5);
		badgeView.setTextColor(0x00000000); // 设置字体颜色为透明
		badgeView.setBackgroundResource(R.drawable.red_notice);
		badgeView.setBadgeGravity(Gravity.TOP | Gravity.RIGHT);
	}

	private void notShowRedPoint() {
		badgeView.setVisibility(View.GONE);
		badgeView1 = new BadgeView(getActivity());
		badgeView1.setTargetView(redViewPoint);
		badgeView1.setBadgeCount(1);
		badgeView1.setWidth(5);
		badgeView1.setHeight(5);
		badgeView1.setTextColor(0x00000000); // 设置字体颜色为透明
		badgeView1.setBackgroundResource(R.drawable.red_white_notice);
		badgeView1.setBadgeGravity(Gravity.CENTER);
	}

	/** Name:Robin Time:20150928 Function: 友盟session的统计 */
	private String mPageName;

	private ImageView mBindNewDevice;
	
	/**
	 * 在此执行的操作 1.更新扫描到的手环固件信息
	 *            2.同时更新 MainFragment 连接设备的按钮
	 */
	@Override
	public void onResume() {		
		Log.i(TAG, "Robin--------- onResume() ");
		super.onResume();
		MobclickAgent.onPageStart(mPageName);
		updateDeviceInfo();
		mBindNewDevice = (ImageView) getActivity().findViewById(
				R.id.id_iv_bind_new_device);
		Log.i(TAG, "Robin--------- onResume()---updateDeviceInfo(); ");
		if (mLocalStorage.isDeviceActivated()) {
			mBtnBindRelease
					.setText(R.string.string_btn_release_current_device_bond);
			return;
		} else {
			mBtnBindRelease
					.setText(R.string.string_btn_bind_new_device_with_user);
			mBindNewDevice.setVisibility(View.VISIBLE);
			return;
		}
		
	}

	@Override
	public void onPause() {
		Log.i(TAG, "Robin--------- onPause() ");
		super.onPause();
		MobclickAgent.onPageEnd(mPageName);
		
	}

}

/**
 * @AIClick({R.id.main_show_dialog_btn ) public void onClickCallbackSample(View
 *                                     view) { switch (view.getId()) { case
 *                                     R.id.main_show_dialog_btn: View outerView
 *                                     = LayoutInflater.from(context).inflate(R.
 *                                     layout.wheel_view, null); WheelView wv =
 *                                     (WheelView) outerView.findViewById(R.id.
 *                                     wheel_view_wv); wv.setOffset(2);
 *                                     wv.setItems(Arrays.asList(PLANETS));
 *                                     wv.setSeletion(3);
 *                                     wv.setOnWheelViewListener(new
 *                                     WheelView.OnWheelViewListener() {
 * @Override public void onSelected(int selectedIndex, String item) {
 *           Logger.d(TAG, "[Dialog]selectedIndex: " + selectedIndex +
 *           ", item: " + item); } });
 * 
 *           new AlertDialog.Builder(context) .setTitle("WheelView in Dialog")
 *           .setView(outerView) .setPositiveButton("OK", null) .show();
 * 
 *           break; } } /
 **/

