package com.foogeez.activity;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.foogeez.bluetooth.BluetoothLeService;
import com.foogeez.configuration.LocalStorage;
import com.foogeez.database.ActionsDatum;
import com.foogeez.database.DatabaseHelper;
import com.foogeez.database.DatabaseManager;
import com.foogeez.dialog.ConfigRspDialog;
import com.foogeez.fooband.R;
import com.foogeez.https.Urls;
import com.foogeez.network.NetworkUtils;
import com.foogeez.network.NetworkUtils.NetworkCallback;
import com.foogeez.services.CentralService;
import com.grdn.animations.ColorAnimationView;
import com.grdn.pulltorefresh.library.PullToRefreshBase;
import com.grdn.pulltorefresh.library.PullToRefreshBase.Mode;
import com.grdn.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.grdn.pulltorefresh.library.PullToRefreshScrollView;
import com.grdn.util.DrawableUtil;
import com.grdn.util.Utils;
import com.grdn.widgets.CircleProgressBar;
import com.grdn.widgets.HistogramChart;
import com.jauker.widget.BadgeView;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.facebook.controller.UMFacebookHandler;
import com.umeng.socialize.facebook.media.FaceBookShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

@SuppressLint("InflateParams")
public class MainFragment<ReceiveBroadCast> extends Fragment {
	private final static String TAG = MainFragment.class.getSimpleName();

	private LocalStorage mLocalStorage = null;

	private List<View> views;
	private MyPagerAdapter mAdapter;
	private ViewPager mGuideViewPager = null;
	private ColorAnimationView mColorAnimationView;

	private ImageView mBindNewDevice;
	private ImageView mBluetoothConnectionState;

	private RelativeLayout mNumLayout;

	private LinearLayout mSleepView;
	private LinearLayout mSportView;

	ScrollView mScrollView;
	private PullToRefreshScrollView refreshableView;
	// private PullToRefreshListView refreshableView;
	// private PullRefreshView refreshableView;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private String[] items = { "", "", "", };

	private static final int REQUEST_ENABLE_BT = 1;
	private CentralService mCentralService = null;

	private CircleProgressBar mSportCircleProgressBar = null;
	private CircleProgressBar mSleepCircleProgressBar = null;

	private HistogramChart mSportHistogramChart = null;
	private HistogramChart mSleepHistogramChart = null;

	private TextView mTextViewBatteryLevel = null;
	private TextView mTextViewLastSyncTime = null;

	private TextView mTextViewTotalCaloric = null;
	private TextView mTextViewDistance = null;
	private TextView mTextViewActiveCaloric = null;
	private TextView mTextViewActiveTime = null;

	private TextView mTextViewDeepSleepTime = null;
	private TextView mTextViewLightSleepTime = null;
	private TextView mTextViewAwakeCount = null;
	private TextView mTextViewAwakeTime = null;

	private TextView mTextViewTitleDescript = null;
	private TextView mTextViewTitleDateTime = null;
	// private TextView mTextViewNoticePullDown = null;

	private ImageView mImageViewTitlePrvRecord = null;
	private ImageView mImageViewTitleNxtRecord = null;

	private ImageView mImageViewBtSync = null;

	private ImageView mImageViewSleepPrvPiece = null;
	private ImageView mImageViewSleepNxtPiece = null;

	private int mSleepPieceIndex = 0;

	private int mDayCount = 0;
	private int mPagerPostion = 0;

	private ProgressBar mProgressBarDatumSync = null;

	private String mNewAppFullName;
	private String mNewAppVersion;

	private String mPageName;

	private ImageView share;
	// 首先在您的Activity中添加如下成员变量
	final UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.share");
	Map<String, SHARE_MEDIA> mPlatformsMap = new HashMap<String, SHARE_MEDIA>();

	private View redNotice; // 显示在设置旁边的红点

	@Override
	public void onCreate(Bundle savedInstanceState) {
		/** Name:Robin  Time:20151208  Function:友盟检测到NullPointerException，所以注释 */
//		if (savedInstanceState != null) {
//			Log.v(TAG, savedInstanceState.getString("data"));
//		}
		Log.i(TAG, "MainFragment --- onCreate");
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.activity_main, container,
				false);
		Log.i(TAG, "MainFragment --- onCreateView");

		return mainView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.i(TAG, "MainFragment --- onActivityCreated");
		mLocalStorage = new LocalStorage(getActivity());

		initWidgets();
		initViewPager();
		initViewPagerContents();
		initPullRefreshView();

		initBluetoothLe();
		initCentralServices();

		getActivity().registerReceiver(mBroadcastReceiver,
				makeGattUpdateIntentFilter());

		checkAppUpdate();
		setSportCpbTarget();
		setSleepCpbTarget();
		updateUserAndDeviceInfo();
		refreshDID();

		if (!mLocalStorage.isDeviceActivated()) {
			mBindNewDevice.setVisibility(ImageView.VISIBLE);
		} else {
			mBindNewDevice.setVisibility(ImageView.GONE);
			/** Robin 20151029 --> 判断为汉语、韩语时，分享图标的可见性 */
			String locale = Locale.getDefault().getLanguage();
			if (locale.equals("zh") || locale.equals("ko")) {
				share.setVisibility(ImageView.GONE);
			} else {
				share.setVisibility(ImageView.VISIBLE);
			}
		}
		redNotice.setVisibility(View.GONE);
		// initNotificationListener();// hide notification service
//		 showRedPoint(); //测试红点提示用

	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(TAG, "MainFragment --- onStart");
	}

	private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

	private boolean isEnabled() {
		String pkgName = getActivity().getPackageName();
		final String flat = Settings.Secure.getString(getActivity()
				.getContentResolver(), ENABLED_NOTIFICATION_LISTENERS);
		if (!TextUtils.isEmpty(flat)) {
			final String[] names = flat.split(":");
			for (int i = 0; i < names.length; i++) {
				final ComponentName cn = ComponentName
						.unflattenFromString(names[i]);
				if (cn != null) {
					if (TextUtils.equals(pkgName, cn.getPackageName())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private AlertDialog mNotifiAuthDialog;

	private void initNotificationListener() {
		boolean settingNeedNotify = mLocalStorage.restoreFunctionConfig()
				.getMessageRemainderQQ()
				|| mLocalStorage.restoreFunctionConfig()
						.getMessageRemainderWechat()
				|| mLocalStorage.restoreFunctionConfig().getMessageRemainder();

		if (settingNeedNotify && !isEnabled()) {
			if (mNotifiAuthDialog == null) {
				mNotifiAuthDialog = new AlertDialog.Builder(getActivity())
						.setTitle(
								R.string.string_notification_read_authorize_title)
						.setMessage(
								R.string.string_notification_read_authorize_message)
						.setPositiveButton(R.string.string_dialog_positive,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Intent intent = new Intent(
												"android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
										startActivity(intent);
									}
								})
						.setNegativeButton(R.string.string_dialog_negative,
								null).create();
			}
			if (getActivity() != null && !getActivity().isFinishing()
					&& !mNotifiAuthDialog.isShowing()) {
				mNotifiAuthDialog.show();
			}
		}
	}

	private LocalHandler handler = new LocalHandler(this);

	// /////////////////////////////－－－－更新ＡＰＰ－－－－///////////////////////////////////
	private ProgressDialog progressDialog;
	private final static int NEW_VERSION_NONE = 0;
	private final static int NEW_VERSION_EXSIST = 1;
	private final static int NEW_VERSION_ERROR = 2;
	private final static int NEW_VERSION_IF_UPDATE = 3;
	private final static int UPDATE_AFTER = 5;

	private String app = null;

	private static class LocalHandler extends Handler {
		private WeakReference<MainFragment> mContext;

		public LocalHandler(MainFragment context) {
			this.mContext = new WeakReference<MainFragment>(context);
		}

		@Override
		public void handleMessage(Message msg) {
			final MainFragment context = mContext.get();
			if (context != null && context.isAdded()) {
				switch (msg.what) {
				case NEW_VERSION_EXSIST:
					Bundle data = msg.getData();
					context.mNewAppVersion = data.getString("APP_NEW_VER");
					context.mNewAppFullName = data.getString("APP_FULL_NAME");
					Log.e(TAG, "update....");
					Intent i = new Intent(Intent.ACTION_VIEW);
					// 保证安装的时候不闪退，安装完成可直接打开
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.setDataAndType(Utils.fileToUri(context.mNewAppFullName),
							"application/vnd.android.package-archive");
					context.getActivity().startActivity(i);

					break;
				case NEW_VERSION_NONE:

					break;

				case NEW_VERSION_ERROR:
					/**
					 * new AlertDialog.Builder(context.getActivity())
					 * .setTitle(R.string.string_find_new_app_version) // 检查更新
					 * .setMessage( R.string.string_find_new_app_version_none)
					 * // 当前App为最新版本！
					 * .setPositiveButton(R.string.string_dialog_positive, new
					 * DialogInterface.OnClickListener() {
					 * 
					 * @Override public void onClick( DialogInterface dialog,
					 *           int which) { } }).show(); /
					 **/
					break;

				case NEW_VERSION_IF_UPDATE: // 点击检测更新，如有更新，就直接下载
					Bundle data1 = msg.getData();
					context.mNewAppVersion = data1.getString("APP_NEW_VER");
					context.mNewAppFullName = data1.getString("APP_FULL_NAME");
					Log.i(TAG, "NEW_VERSION_IF_UPDATE------ mNewAppVersion"
							+ context.mNewAppVersion);
					String format = context
							.getString(R.string.string_find_new_app_version_content);
					format = String.format(format, context.mNewAppVersion);
					new AlertDialog.Builder(context.getActivity())
							.setTitle(R.string.string_find_new_app_version)
							.setMessage(format)
							.setPositiveButton(R.string.string_dialog_positive,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											context.startUpdateApp();
										}
									})
							.setNegativeButton(R.string.string_dialog_negative,
									null).show();
					break;

				case UPDATE_AFTER:
					context.progressDialog.dismiss();
					context.startSetupApp();
					break;
				}
			}
		}
	}

	/**
	 * @SuppressLint("HandlerLeak") Handler handler = new Handler(){
	 * @Override public void handleMessage(Message msg) {
	 *           super.handleMessage(msg); switch(msg.what) { case
	 *           NEW_VERSION_EXSIST: Bundle data = msg.getData(); mNewAppVersion
	 *           = data.getString("APP_NEW_VER"); mNewAppFullName =
	 *           data.getString("APP_FULL_NAME"); String format =
	 *           getString(R.string.string_find_new_app_version_content); format
	 *           = String.format(format, mNewAppVersion);
	 * 
	 *           new AlertDialog.Builder(getActivity())
	 *           .setTitle(R.string.string_find_new_app_version)
	 *           .setMessage(format)
	 *           .setPositiveButton(R.string.string_dialog_positive, new
	 *           DialogInterface.OnClickListener() {
	 * @Override public void onClick(DialogInterface dialog, int which) {
	 *           Log.e(TAG, "update...."); Intent i = new
	 *           Intent(Intent.ACTION_VIEW);
	 *           i.setDataAndType(Utils.fileToUri(mNewAppFullName),
	 *           "application/vnd.android.package-archive");
	 *           getActivity().startActivity(i); } })
	 *           .setNegativeButton(R.string.string_dialog_negative, null)
	 *           .show();
	 * 
	 *           break; case NEW_VERSION_NONE: break; case NEW_VERSION_ERROR:
	 *           break; } } };
	 **/

	private void checkAppUpdate() {
		new Thread(new Runnable() {
			@SuppressLint("DefaultLocale")
			@Override
			public void run() {
				Message msg = new Message();

				String appURL = CentralService.APP_IMAGE + "?utc="
						+ Utils.getUtc();
				String infoURL = CentralService.APP_IMAGE_INF + "?utc="
						+ Utils.getUtc();
				String path = new DatabaseHelper(getActivity(), null, null)
						.getDatabasePath() + "/app";
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

		progressDialog = new ProgressDialog(getActivity()); // 进度条，在下载的时候实时更新进度，提高用户友好度
		// progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setTitle(R.string.apk_is_downloading);
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
				String path = new DatabaseHelper(getActivity(), null, null)
						.getDatabasePath() + "/app";

				app = Utils.downloadFileAndCaculateMd5(path, appURL);
				Log.i(TAG, "Robin--------downloadFileAndCaculateMd5-----app"
						+ app);
				msg.what = UPDATE_AFTER;
				handler.sendMessage(msg);
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

				String path = new DatabaseHelper(getActivity(), null, null)
						.getDatabasePath() + "/app";
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

	private String getAppVersionName() {

		if (getActivity() == null) {
			return "";
		}
		PackageManager manager;
		PackageInfo info = null;
		manager = getActivity().getPackageManager();

		try {
			info = manager.getPackageInfo(getActivity().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}

		// return info.versionCode;
		return info.versionName;
		// info.packageName;
		// info.signatures;
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(mPageName);
		Log.i(TAG, "MainFragment --- onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.i(TAG, "MainFragment --- onStop");
		// stopLeConnection();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "MainFragment --- onDestroy");

		if (!mCentralService.BluetoothLeIsBusy()) {
			stopLeConnection();
		}

		unbindCentralService();
		getActivity().unregisterReceiver(mBroadcastReceiver);

		handler.removeCallbacksAndMessages(null);
	}

	/**
	 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
	 *           if(keyCode == KeyEvent.KEYCODE_BACK) { if(
	 *           !mCentralService.BluetoothLeIsBusy() ) { stopLeConnection(); }
	 *           finish(); return true; } else { return super.onKeyDown(keyCode,
	 *           event); } } /
	 **/

	private void updateUserAndDeviceInfo() {
		if (mCentralService == null) {
			Log.e(TAG, "mCentralService == null");
			return;
		}

		mCentralService.updateUserAndDeviceInfo();
	}

	// private void startLeConnection() {
	// Log.i(TAG, "startLeConnection");
	// connectBindedDevice();
	// }

	private void stopLeConnection() {
		Log.i(TAG, "stopLeConnection");
		if (mCentralService == null) {
			Log.e(TAG, "mCentralService == null");
			return;
		}

		// if( mCentralService.getLeConnectState() !=
		// BluetoothProfile.STATE_DISCONNECTED ) {
		mCentralService.disconnectLeDevice();
		// }
	}

	private void initWidgets() {
		Log.i(TAG, "initWidgets");

		redNotice = (View) getActivity().findViewById(R.id.red_notice_view);

		mTextViewBatteryLevel = (TextView) getActivity().findViewById(
				R.id.id_tv_battery_level);
		mTextViewBatteryLevel.setTextColor(getResources().getColor(
				R.color.black));
		mTextViewBatteryLevel.setText("N/A");

		mTextViewLastSyncTime = (TextView) getActivity().findViewById(
				R.id.id_tv_last_sync_time);
		mTextViewLastSyncTime.setTextColor(getResources().getColor(
				R.color.black));

		String syncTime = mLocalStorage.getLastSyncTime();
		syncTime = String.format(
				getActivity().getResources().getString(
						R.string.string_last_sync_time), syncTime);
		mTextViewLastSyncTime.setText(syncTime);

		mBindNewDevice = (ImageView) getActivity().findViewById(
				R.id.id_iv_bind_new_device);
		mBindNewDevice.setClickable(true);
		mBindNewDevice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				enterPairsActivity();
			}
		});

		share = (ImageView) getActivity().findViewById(
				R.id.img_share_sport_sleep);
		share.setClickable(true);
		share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// myShot(getActivity());
				addCustomPlatforms();
				showCustomUI(true);
			}
		});
		initPlatformMap();

		mBluetoothConnectionState = (ImageView) getActivity().findViewById(
				R.id.id_iv_bt_status);
		mBluetoothConnectionState.setImageDrawable(getResources().getDrawable(
				R.drawable.bt_disconnected));

		mProgressBarDatumSync = (ProgressBar) getActivity().findViewById(
				R.id.id_pgbr_datum_sync);
		mProgressBarDatumSync.setProgress(0);
		mProgressBarDatumSync.setVisibility(ProgressBar.GONE);

		// mTextViewNoticePullDown =
		// (TextView)findViewById(R.id.id_tv_notice_pull_down_to_refresh);
	}

	private void initViewPager() {
		Log.i(TAG, "initViewPager");

		mSportView = (LinearLayout) getActivity().getLayoutInflater().inflate(
				R.layout.sport_content, null);
		mSleepView = (LinearLayout) getActivity().getLayoutInflater().inflate(
				R.layout.sleep_content, null);

		mGuideViewPager = (ViewPager) getActivity().findViewById(
				R.id.id_vp_switch_sport_sleep);
		views = new ArrayList<View>();

		views.add(mSportView);
		views.add(mSleepView);

		mAdapter = new MyPagerAdapter(views);
		mGuideViewPager.setAdapter(mAdapter);
		mGuideViewPager.setCurrentItem(0);

		mColorAnimationView = (ColorAnimationView) getActivity().findViewById(
				R.id.id_cav_viewpager_background);
		mColorAnimationView
				.setOnPageChangeListener(new MyOnPageChangeListener());
		mColorAnimationView.setmViewPager(mGuideViewPager, views.size(),
				0xff3498db, 0xff023665);// 0xfff7941e, 0xff14a2d4);
	}

	public void refreshViewPager(int number) {
		mGuideViewPager.setCurrentItem(number);
	}

	private void initViewPagerContents() {
		Log.i(TAG, "initViewPagerContents");

		// create guide bar object
		mNumLayout = (RelativeLayout) getActivity().findViewById(
				R.id.id_rl_switch_director);
		mNumLayout.setVisibility(RelativeLayout.GONE);

		Button buttonSport = new Button(getActivity());
		buttonSport.setBackgroundResource(R.drawable.btn_dynamic_mode_sport);
		buttonSport.setAlpha(0.7f);
		mNumLayout.addView(buttonSport);

		Button buttonSleep = new Button(getActivity());
		buttonSleep.setBackgroundResource(R.drawable.dummy);
		mNumLayout.addView(buttonSleep);

		// sport datum display
		mSportCircleProgressBar = (CircleProgressBar) mSportView
				.findViewById(R.id.id_cpb_roundProgressBar_sport);
		mSportHistogramChart = (HistogramChart) mSportView
				.findViewById(R.id.id_hc_histogramChart_sport);

		mTextViewTotalCaloric = (TextView) mSportView
				.findViewById(R.id.id_tv_total_caloric);
		mTextViewDistance = (TextView) mSportView
				.findViewById(R.id.id_tv_distance);
		mTextViewActiveCaloric = (TextView) mSportView
				.findViewById(R.id.id_tv_active_caloric);
		mTextViewActiveTime = (TextView) mSportView
				.findViewById(R.id.id_tv_active_time);

		ImageView nImageViewTotalCaloric = (ImageView) mSportView
				.findViewById(R.id.id_iv_icon_total_caloric);
		ImageView nImageViewDistance = (ImageView) mSportView
				.findViewById(R.id.id_iv_icon_distance);
		ImageView nImageViewActiveCaloric = (ImageView) mSportView
				.findViewById(R.id.id_iv_icon_active_caloric);
		ImageView nImageViewActiveTime = (ImageView) mSportView
				.findViewById(R.id.id_iv_icon_active_time);

		DrawableUtil.setImageViewColor(nImageViewTotalCaloric, 0xffa0a0a0);
		DrawableUtil.setImageViewColor(nImageViewDistance, 0xffa0a0a0);
		DrawableUtil.setImageViewColor(nImageViewActiveCaloric, 0xffa0a0a0);
		DrawableUtil.setImageViewColor(nImageViewActiveTime, 0xffa0a0a0);

		// sleep datum display
		mSleepCircleProgressBar = (CircleProgressBar) mSleepView
				.findViewById(R.id.id_cpb_roundProgressBar_sleep);
		mSleepHistogramChart = (HistogramChart) mSleepView
				.findViewById(R.id.id_hc_histogramChart_sleep);

		mTextViewDeepSleepTime = (TextView) mSleepView
				.findViewById(R.id.id_tv_light_sleep_time);
		mTextViewLightSleepTime = (TextView) mSleepView
				.findViewById(R.id.id_tv_deep_sleep_time);
		mTextViewAwakeCount = (TextView) mSleepView
				.findViewById(R.id.id_tv_awake_count);
		mTextViewAwakeTime = (TextView) mSleepView
				.findViewById(R.id.id_tv_awake_time);

		// pager title init.
		mTextViewTitleDescript = (TextView) getActivity().findViewById(
				R.id.id_tv_record_descriptor);
		mTextViewTitleDateTime = (TextView) getActivity().findViewById(
				R.id.id_tv_record_datetime);
		mTextViewTitleDescript.setTextColor(getResources().getColor(
				R.color.black));
		mTextViewTitleDateTime.setTextColor(getResources().getColor(
				R.color.black));

		mImageViewBtSync = (ImageView) getActivity().findViewById(
				R.id.id_iv_btn_sync);
		mImageViewBtSync.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshableView.setRefreshing();
			}
		});

		mImageViewTitlePrvRecord = (ImageView) getActivity().findViewById(
				R.id.id_iv_prv_record);
		mImageViewTitlePrvRecord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDayCount--;
				mImageViewTitleNxtRecord.setImageResource(R.drawable.nxt);

				Calendar calendar = Calendar.getInstance();
				int utc = (int) (calendar.getTimeInMillis() / 1000) + 24 * 3600
						* mDayCount;

				mTextViewTitleDateTime.setText(ActionsDatum.utc2DateTimeString(
						utc, getString(R.string.string_date_format),
						TimeZone.getDefault()));

				ActionsDatum totalSportDatum = mCentralService
						.getTotalSportDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				List<Integer> historySportDatum = mCentralService
						.getHistorySportDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				refreshUI(totalSportDatum, historySportDatum,
						ActionsDatum.DATUM_TYPE_SPORT);

				ActionsDatum totalSleepDatum = mCentralService
						.getTotalSleepDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				List<ActionsDatum> historySleepDatum = mCentralService
						.getHistorySleepDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				refreshUI(totalSleepDatum, historySleepDatum,
						ActionsDatum.DATUM_TYPE_SLEEP);
			}
		});

		mImageViewTitleNxtRecord = (ImageView) getActivity().findViewById(
				R.id.id_iv_nxt_record);
		mImageViewTitleNxtRecord.setImageResource(R.drawable.nxt_freeze);
		mImageViewTitleNxtRecord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (++mDayCount >= 0) {
					mDayCount = 0;
					mImageViewTitleNxtRecord
							.setImageResource(R.drawable.nxt_freeze);

					if (mPagerPostion == 0)
						mTextViewTitleDateTime
								.setText(R.string.string_title_sport_datetime);
					else
						mTextViewTitleDateTime
								.setText(R.string.string_title_sleep_datetime);
				}

				Calendar calendar = Calendar.getInstance();
				int utc = (int) (calendar.getTimeInMillis() / 1000) + 24 * 3600
						* mDayCount;

				if (mDayCount < 0) {
					mTextViewTitleDateTime.setText(ActionsDatum
							.utc2DateTimeString(utc,
									getString(R.string.string_date_format),
									TimeZone.getDefault()));
				}

				ActionsDatum totalSportDatum = mCentralService
						.getTotalSportDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				List<Integer> historySportDatum = mCentralService
						.getHistorySportDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				refreshUI(totalSportDatum, historySportDatum,
						ActionsDatum.DATUM_TYPE_SPORT);

				ActionsDatum totalSleepDatum = mCentralService
						.getTotalSleepDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				List<ActionsDatum> historySleepDatum = mCentralService
						.getHistorySleepDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				refreshUI(totalSleepDatum, historySleepDatum,
						ActionsDatum.DATUM_TYPE_SLEEP);
			}
		});

		/**
		 * mImageViewSleepPrvPiece = (ImageView)
		 * mSleepView.findViewById(R.id.id_iv_counter_prev);
		 * mImageViewSleepPrvPiece.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { int size =
		 *           mCentralService.getHistorySleepDatumSize(); int index =
		 *           mCentralService.getHistorySleepDatumIndex(); if( size <= 1
		 *           ) { return; }
		 * 
		 *           if( --mSleepPieceIndex < 0 ) { mSleepPieceIndex = 0;
		 *           mImageViewSleepPrvPiece.setImageResource(R.drawable.prv_1);
		 *           } else {
		 *           mImageViewSleepPrvPiece.setImageResource(R.drawable.prv_0);
		 *           }
		 * 
		 *           Calendar calendar = Calendar.getInstance(); int utc =
		 *           (int)(calendar.getTimeInMillis()/1000) + 24*3600*mDayCount;
		 *           ActionsDatum totalSleepDatum =
		 *           mCentralService.getTotalSleepDatumByDate
		 *           (DatabaseManager.DATUM_TYPE_BY_DAY, utc);
		 *           List<ActionsDatum> historySleepDatum =
		 *           mCentralService.getHistorySleepDatumByNumber
		 *           (mSleepPieceIndex); refreshUI(totalSleepDatum,
		 *           historySleepDatum, ActionsDatum.DATUM_TYPE_SLEEP); } });
		 * 
		 *           mImageViewSleepNxtPiece = (ImageView)
		 *           mSleepView.findViewById(R.id.id_iv_counter_next);
		 *           mImageViewSleepNxtPiece.setOnClickListener(new
		 *           OnClickListener() {
		 * @Override public void onClick(View v) { int size =
		 *           mCentralService.getHistorySleepDatumSize(); int index =
		 *           mCentralService.getHistorySleepDatumIndex(); if( size <= 1
		 *           ) { return; }
		 * 
		 *           if( ++mSleepPieceIndex >= size ) { mSleepPieceIndex = size;
		 *           mImageViewSleepNxtPiece.setImageResource(R.drawable.nxt_1);
		 *           } else {
		 *           mImageViewSleepNxtPiece.setImageResource(R.drawable.nxt_0);
		 *           }
		 * 
		 *           Calendar calendar = Calendar.getInstance(); int utc =
		 *           (int)(calendar.getTimeInMillis()/1000) + 24*3600*mDayCount;
		 *           ActionsDatum totalSleepDatum =
		 *           mCentralService.getTotalSleepDatumByDate
		 *           (DatabaseManager.DATUM_TYPE_BY_DAY, utc);
		 *           List<ActionsDatum> historySleepDatum =
		 *           mCentralService.getHistorySleepDatumByNumber
		 *           (mSleepPieceIndex); refreshUI(totalSleepDatum,
		 *           historySleepDatum, ActionsDatum.DATUM_TYPE_SLEEP); } }); /
		 **/

		refreshZeroUI(ActionsDatum.DATUM_TYPE_SPORT);
		refreshZeroUI(ActionsDatum.DATUM_TYPE_SLEEP);

		if (mCentralService != null) {
			Calendar calendar = Calendar.getInstance();
			int utc = (int) (calendar.getTimeInMillis() / 1000) - 24 * 3600 * 0;

			Log.i(TAG, "Robin-------if (mCentralService != null) ");

			ActionsDatum totalSportDatum = mCentralService
					.getTotalSportDatumByDate(
							DatabaseManager.DATUM_TYPE_BY_DAY, utc);
			List<Integer> historySportDatum = mCentralService
					.getHistorySportDatumByDate(
							DatabaseManager.DATUM_TYPE_BY_DAY, utc);
			refreshUI(totalSportDatum, historySportDatum, null);

			ActionsDatum totalSleepDatum = mCentralService
					.getTotalSleepDatumByDate(
							DatabaseManager.DATUM_TYPE_BY_DAY, utc);
			List<ActionsDatum> historySleepDatum = mCentralService
					.getHistorySleepDatumByDate(
							DatabaseManager.DATUM_TYPE_BY_DAY, utc);
			refreshUI(totalSleepDatum, null, historySleepDatum);
		}
	}

	private void setSportCpbProgress(int steps) {
		if (mSportCircleProgressBar == null) {
			Log.e(TAG,
					"setSportCpbProgress --- 'mSportCircleProgressBar == null'");
			return;
		}

		// Log.e(TAG, "steps = " + steps);
		// mSportCircleProgressBar.setMax(mLocalStorage.restoreUsrTargetConfig().getSportTarget());
		mSportCircleProgressBar.setMax(mLocalStorage.restoreUsrTargetConfig()
				.getSteps());
		mSportCircleProgressBar.setProgress(steps);
		mGuideViewPager.getAdapter().notifyDataSetChanged();
	}

	private void setSportCpbTarget() {
		if (mSportCircleProgressBar == null) {
			Log.e(TAG,
					"setSportCpbProgress --- 'mSportCircleProgressBar == null'");
			return;
		}

		// Log.e(TAG, "steps = " + steps);
		// mSportCircleProgressBar.setMax(mLocalStorage.restoreUsrTargetConfig().getSportTarget());
		mSportCircleProgressBar.setMax(mLocalStorage.restoreUsrTargetConfig()
				.getSteps());
		mGuideViewPager.getAdapter().notifyDataSetChanged();
	}

	private void setSportTxtTotalCaloric(int caloric) {
		if (mTextViewTotalCaloric == null) {
			Log.e(TAG, "setSportTxtCaloric --- 'mTextViewCaloric == null'");
			return;
		}

		String formatString = getString(R.string.string_total_calorics);
		formatString = String.format(formatString, caloric);
		mTextViewTotalCaloric.setText(formatString);
		mGuideViewPager.getAdapter().notifyDataSetChanged();
	}

	private void setSportTxtDistance(int distance) {
		if (mTextViewDistance == null) {
			Log.e(TAG, "setSportTxtDistance --- 'mTextViewDistance == null'");
			return;
		}

		String formatString = getString(R.string.string_distance);
		formatString = String.format(formatString, (float) distance / 100);
		mTextViewDistance.setText(formatString);
		mGuideViewPager.getAdapter().notifyDataSetChanged();
	}

	private void setSportTxtActiveCaloric(int caloric) {
		if (mTextViewActiveCaloric == null) {
			Log.e(TAG,
					"setSportTxtActiveCaloric --- 'mTextViewActiveCaloric == null'");
			return;
		}

		String formatString = getString(R.string.string_active_calorics);
		formatString = String.format(formatString, caloric);
		mTextViewActiveCaloric.setText(formatString);
		mGuideViewPager.getAdapter().notifyDataSetChanged();
	}

	private void setSportTxtActiveTime(int minutes) {
		if (mTextViewActiveTime == null) {
			Log.e(TAG,
					"setSportTxtActiveTime --- 'mTextViewActiveTime == null'");
			return;
		}

		String strHourUnit = this.getResources().getString(
				R.string.string_time_hour);
		if (minutes >= 60) {
			strHourUnit = String.format(strHourUnit, minutes / 60);
		} else {
			strHourUnit = "";
		}
		String formatString = getString(R.string.string_active_time);
		formatString = String.format(formatString, strHourUnit, minutes % 60);
		mTextViewActiveTime.setText(formatString);
		mGuideViewPager.getAdapter().notifyDataSetChanged();
	}

	private void setSleepCpbProgress(int minutes) {
		if (mSleepCircleProgressBar == null) {
			Log.e(TAG,
					"setSleepCpbProgress --- 'mSleepCircleProgressBar == null'");
			return;
		}

		// Log.e(TAG, "total sleep time = " + minutes);
		mSleepCircleProgressBar.setMax(mLocalStorage.restoreUsrTargetConfig()
				.getHours() * 60);
		// mSleepCircleProgressBar.setMax(mLocalStorage.restoreUsrTargetConfig().getCurrentSleepTargetId());
		Log.i(TAG, "----111------>>>"
				+ mLocalStorage.restoreUsrTargetConfig().getHours() * 60);
		mSleepCircleProgressBar.setProgress(minutes);
		mGuideViewPager.getAdapter().notifyDataSetChanged();
	}

	private void setSleepCpbTarget() {
		if (mSleepCircleProgressBar == null) {
			Log.e(TAG,
					"setSleepCpbProgress --- 'mSleepCircleProgressBar == null'");
			return;
		}

		// Log.e(TAG, "total sleep time = " + minutes);
		mSleepCircleProgressBar.setMax(mLocalStorage.restoreUsrTargetConfig()
				.getHours() * 60);
		// mSleepCircleProgressBar.setMax(mLocalStorage.restoreUsrTargetConfig().getCurrentSleepTargetId());
		Log.i(TAG, "------222---->>>"
				+ mLocalStorage.restoreUsrTargetConfig().getHours() * 60);
		mGuideViewPager.getAdapter().notifyDataSetChanged();
	}

	private void setSleepTxtLightSleepTime(int minutes) {
		if (mTextViewLightSleepTime == null) {
			Log.e(TAG,
					"setSleepTxtLightSleepTime --- 'mTextViewLightSleepTime == null'");
			return;
		}

		String strHourUnit = this.getResources().getString(
				R.string.string_time_hour);
		if (minutes >= 60) {
			strHourUnit = String.format(strHourUnit, minutes / 60);
		} else {
			strHourUnit = "";
		}
		String formatString = getString(R.string.string_light_sleep_time);
		formatString = String.format(formatString, strHourUnit, minutes % 60);
		mTextViewLightSleepTime.setText(formatString);
		mGuideViewPager.getAdapter().notifyDataSetChanged();
	}

	private void setSleepTxtDeepSleepTime(int minutes) {
		if (mTextViewDeepSleepTime == null) {
			Log.e(TAG,
					"setSleepTxtDeepSleepTime --- 'mTextViewDeepSleepTime == null'");
			return;
		}

		String strHourUnit = this.getResources().getString(
				R.string.string_time_hour);
		if (minutes >= 60) {
			strHourUnit = String.format(strHourUnit, minutes / 60);
		} else {
			strHourUnit = "";
		}
		String formatString = getString(R.string.string_deep_sleep_time);
		formatString = String.format(formatString, strHourUnit, minutes % 60);
		mTextViewDeepSleepTime.setText(formatString);
		mGuideViewPager.getAdapter().notifyDataSetChanged();
	}

	private void setSleepTxtAwakeCount(int count) {
		if (mTextViewAwakeCount == null) {
			Log.e(TAG,
					"setSleepTxtAwakeCount --- 'mTextViewAwakeCount == null'");
			return;
		}

		String formatString = getString(R.string.string_awake_count);
		formatString = String.format(formatString, count);
		mTextViewAwakeCount.setText(formatString);
		mGuideViewPager.getAdapter().notifyDataSetChanged();
	}

	private void setSleepTxtAwakeTime(int minutes) {
		if (mTextViewAwakeTime == null) {
			Log.e(TAG, "setSleepTxtAwakeTime --- 'mTextViewAwakeTime == null'");
			return;
		}

		String strHourUnit = this.getResources().getString(
				R.string.string_time_hour);
		if (minutes >= 60) {
			strHourUnit = String.format(strHourUnit, minutes / 60);
		} else {
			strHourUnit = "";
		}
		String formatString = getString(R.string.string_awake_time);
		formatString = String.format(formatString, strHourUnit, minutes % 60);
		mTextViewAwakeTime.setText(formatString);
		mGuideViewPager.getAdapter().notifyDataSetChanged();
	}

	private void setSportHistogramDatum(List<Integer> historySportDatum) {
		if (mSportHistogramChart == null) {
			Log.e(TAG,
					"setSportHistogramDatum --- 'mSportHistogramChart == null'");
			return;
		}

		if (historySportDatum == null) {
			Log.e(TAG, "setSportHistogramDatum --- 'historySportDatum == null'");
			return;
		}

		mSportHistogramChart.setHistogramValues(historySportDatum);
		mGuideViewPager.getAdapter().notifyDataSetChanged();
	}

	private void setSleepHistogramDatum(List<ActionsDatum> historySleepDatum) {
		if (mSleepHistogramChart == null) {
			Log.e(TAG,
					"setSleepHistogramDatum --- 'mSleepHistogramChart == null'");
			return;
		}

		if (historySleepDatum == null) {
			Log.e(TAG, "setSleepHistogramDatum --- 'historySleepDatum == null'");
			return;
		}

		List<Map<String, Integer>> datums = new ArrayList<Map<String, Integer>>();
		for (int i = historySleepDatum.size() - 1; i >= 0; i--) {
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("utc", historySleepDatum.get(i).getUTC());
			map.put("status", historySleepDatum.get(i).getSleepStatus());
			datums.add(map);
		}

		// Log.e(TAG, "datums.size() = " + datums.size());

		mSleepHistogramChart.setHistogramSleepValues(datums);
		mGuideViewPager.getAdapter().notifyDataSetChanged();
	}

	private LinkedList<String> mListItems;
	private ArrayAdapter<String> mPullRefreshAdapter;

	private void initPullRefreshView() {
		/**/
		refreshableView = (PullToRefreshScrollView) getActivity().findViewById(
				R.id.refreshable_view);
		refreshableView.setOnDragListener(new OnDragListener() {
			@Override
			public boolean onDrag(View v, DragEvent event) {
				// TODO Auto-generated method stub
				Log.e(TAG, "onDrag");
				return false;
			}
		});

		refreshableView
				.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ScrollView> refreshView) {
						// TODO Auto-generated method stub
						Log.e(TAG, "initPullRefreshView --- onRefresh");
						if (mCentralService == null) {
							Log.e(TAG, "mCentralService == null");
							return;
						}
						if (mLocalStorage.isDeviceActivated()) {
							mLocalStorage.setFstUseApp(false);
							mCentralService.refreshDatum();
						} else {
							mCentralService
									.broadcastUpdate(CentralService.ACTION_ACTIONS_NO_ACTIVATED_DEVICE_WARNNING);
						}
						
					}

				});

		refreshableView.setMode(Mode.PULL_FROM_START);
		mScrollView = refreshableView.getRefreshableView();

		// ListView actualListView = refreshableView.getRefreshableView();
		// Need to use the Actual ListView when registering for Context Menu
		// registerForContextMenu(actualListView);

		// mListItems = new LinkedList<String>();

		// items[1] = getString(R.string.string_global_setting);
		// mListItems.addAll(Arrays.asList(items));

		// mPullRefreshAdapter = new ArrayAdapter<String>(this,
		// android.R.layout.simple_list_item_1, mListItems);

		// SoundPullEventListener<ListView> soundListener = new
		// SoundPullEventListener<ListView>(this);
		// soundListener.addSoundEvent(State.PULL_TO_REFRESH, R.raw.pull_event);
		// soundListener.addSoundEvent(State.RESET, R.raw.reset_sound);
		// soundListener.addSoundEvent(State.REFRESHING,
		// R.raw.refreshing_sound);
		// mPullRefreshListView.setOnPullEventListener(soundListener);

		// You can also just use setListAdapter(mAdapter) or
		// mPullRefreshListView.setAdapter(mAdapter)
		// actualListView.setAdapter(mPullRefreshAdapter);

		/**
		 * Log.i(TAG, "initPullRefreshView"); refreshableView =
		 * (PullRefreshView) findViewById(R.id.refreshable_view); listView =
		 * (ListView) findViewById(R.id.list_view);
		 * 
		 * items[1] = getString(R.string.string_global_setting); //items[2] =
		 * getString(R.string.string_enjoy_fooband);
		 * 
		 * adapter = new ArrayAdapter<String>(this, R.layout.menu_item, items);
		 * listView.setAdapter(adapter);
		 * 
		 * refreshableView.setOnRefreshListener(new PullToRefreshListener() {
		 * 
		 * @Override public void onRefresh() { Log.e(TAG,
		 *           "initPullRefreshView --- onRefresh"); if( mCentralService
		 *           == null ) { Log.e(TAG, "mCentralService == null"); return;
		 *           } if( mLocalStorage.isDeviceActivated() ) {
		 *           mLocalStorage.setFstUseApp(false);
		 *           mCentralService.refreshDatum(); } else {
		 *           mCentralService.broadcastUpdate(CentralService.
		 *           ACTION_ACTIONS_NO_ACTIVATED_DEVICE_WARNNING); } } }, 0);
		 * 
		 * 
		 *           listView.setOnItemClickListener(new OnItemClickListener() {
		 * @Override public void onItemClick(AdapterView<?> parent, View view,
		 *           int position, long id) { Log.e(TAG,
		 *           "onItemClick --- position = " + position); if( position ==
		 *           1 ) { if( mCentralService == null ) { Log.e(TAG,
		 *           "mCentralService == null"); return; } if(
		 *           mLocalStorage.isDeviceActivated() ) {
		 *           enterSettingActivity(); } else {
		 *           mCentralService.broadcastUpdate(CentralService.
		 *           ACTION_ACTIONS_NO_ACTIVATED_DEVICE_WARNNING); } } } }); /
		 **/
	}

	private void openBluetoothReqest() {
		// Initializes Bluetooth adapter.
		final BluetoothManager bluetoothManager = (BluetoothManager) getActivity()
				.getSystemService(Context.BLUETOOTH_SERVICE);
		BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

		// Ensures Bluetooth is available on the device and it is enabled. If
		// not,
		// displays a dialog requesting user permission to enable Bluetooth.
		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			// Log.e(TAG, "bluetoothAdapter.isEnabled() = " +
			// bluetoothAdapter.isEnabled());
		}
		// Robin ----20151116 当拒绝蓝牙连接时，刷新完成
		if (bluetoothAdapter.isEnabled() == false) {
			refreshableView.onRefreshComplete();
		}
	}

	private void initBluetoothLe() {
		Log.i(TAG, "initBluetoothLe");
		if (checkDeviceHardware() == false) {
			// Toast.makeText(getActivity(), "拒绝打开蓝牙",
			// Toast.LENGTH_SHORT).show();
			// finish();
			// refreshableView.onRefreshComplete();
		}

		openBluetoothReqest();
	}

	/**/
	private void initCentralServices() {
		Log.i(TAG, "initCentralServices");
		// IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
		// MyBroadcastReceiver receiver = new MyBroadcastReceiver();
		// registerReceiver(receiver, filter);
		checkCentralServices(getActivity());
		bindCentralService();
	}

	// private class MyBroadcastReceiver extends BroadcastReceiver {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// Log.e(TAG, "RECEIVED ... TIME TICK!");
	// checkBackgroundServices(context);
	// }
	// }

	boolean isServiceRunning = false;

	private void checkCentralServices(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.foogeez.services.CentralService".equals(service.service
					.getClassName())) { // Service的类名
				Log.e(TAG, "Found " + service.service.getClassName()
						+ "has been running!");
				isServiceRunning = true;
			}
		}
		if (!isServiceRunning) {
			Intent it = new Intent(context, CentralService.class);
			context.startService(it);
		}
	}

	private void bindCentralService() {
		Intent it = new Intent(getActivity(), CentralService.class);
		getActivity().bindService(it, mServiceConnection,
				getActivity().BIND_AUTO_CREATE);
	}

	private void unbindCentralService() {
		getActivity().unbindService(mServiceConnection);
	}

	/**
	 * private void connectBindedDevice() { Log.i(TAG,
	 * "attemp to connect binded device" ); if( mCentralService == null ) {
	 * Log.e(TAG, "mCentralService == null"); return; }
	 * 
	 * String bindedDeviceAddr = mCentralService.getBindedDevice(); if(
	 * bindedDeviceAddr == null ) { Log.e(TAG, "bindedDeviceAddr == null");
	 * return; } mCentralService.connectLeDeviceEx(bindedDeviceAddr); } /
	 **/

	private void refreshZeroUI(int type) {
		if (type == ActionsDatum.DATUM_TYPE_SPORT) {
			Log.e(TAG, "refreshUI for SPORT");
			setSportCpbProgress(0);
			setSportTxtTotalCaloric(0);
			setSportTxtDistance(0);
			setSportTxtActiveCaloric(0);
			setSportTxtActiveTime(0);

			mSportHistogramChart.setHistogramValues(null);
			mGuideViewPager.getAdapter().notifyDataSetChanged();
		} else if (type == ActionsDatum.DATUM_TYPE_SLEEP) {
			Log.e(TAG, "refreshUI for SLEEP");
			setSleepCpbProgress(0);
			setSleepTxtLightSleepTime(0);
			setSleepTxtDeepSleepTime(0);
			setSleepTxtAwakeCount(0);
			setSleepTxtAwakeTime(0);

			mSleepHistogramChart.setHistogramSleepValues(null);
			mGuideViewPager.getAdapter().notifyDataSetChanged();
		}
	}

	@SuppressWarnings("unchecked")
	private void refreshUI(ActionsDatum totalDatum, List<?> historyDatum,
			int type) {
		if (totalDatum == null) {
			if (type == ActionsDatum.DATUM_TYPE_SPORT) {
				Log.e(TAG, "没有运动数据！！！");
				refreshZeroUI(type);
			} else if (type == ActionsDatum.DATUM_TYPE_SLEEP) {
				Log.e(TAG, "没有睡眠数据！！！");
				refreshZeroUI(type);
			}
			return;
		}

		if (type == ActionsDatum.DATUM_TYPE_SPORT) {
			// Log.e(TAG, "refreshUI for SPORT");
			setSportCpbProgress(totalDatum.getSportSteps());
			setSportTxtTotalCaloric(totalDatum.getSportActiveCaloric()
					+ totalDatum.getSportIdleCaloric());
			setSportTxtDistance(totalDatum.getSportDistance());
			setSportTxtActiveCaloric(totalDatum.getSportActiveCaloric());
			setSportTxtActiveTime(totalDatum.getSportActiveTime());
			setSportHistogramDatum((List<Integer>) historyDatum);
		} else if (type == ActionsDatum.DATUM_TYPE_SLEEP) {
			// Log.e(TAG, "refreshUI for SLEEP");
			setSleepCpbProgress(totalDatum.getSleepTotalTime());
			setSleepTxtLightSleepTime(totalDatum.getSleepLightTime());
			setSleepTxtDeepSleepTime(totalDatum.getSleepDeepTime());
			setSleepTxtAwakeCount(totalDatum.getSleepAwakeCount());
			setSleepTxtAwakeTime(totalDatum.getSleepAwakeTime());
			setSleepHistogramDatum((List<ActionsDatum>) historyDatum);

			/**
			 * if( mCentralService != null ) { mSleepPieceIndex =
			 * mCentralService.getHistorySleepDatumIndex(); int size =
			 * mCentralService.getHistorySleepDatumSize(); int index =
			 * mCentralService.getHistorySleepDatumIndex(); if( size <= 1 ) {
			 * mImageViewSleepPrvPiece.setImageResource(R.drawable.prv_0);
			 * mImageViewSleepNxtPiece.setImageResource(R.drawable.nxt_0); }
			 * else {
			 * 
			 * } } /
			 **/
		}
	}

	private void refreshUI(ActionsDatum totalDatum,
			List<Integer> historySportDatum,
			List<ActionsDatum> historySleepDatum) {
		if (totalDatum == null) {
			return;
		}
		// Log.e(TAG, "actionDatum.getSportSteps() = " +
		// totalSportDatum.getSportSteps());

		if (totalDatum.getType() == ActionsDatum.DATUM_TYPE_SPORT) {
			// Log.e(TAG, "refreshUI for SPORT");
			setSportCpbProgress(totalDatum.getSportSteps());
			setSportTxtTotalCaloric(totalDatum.getSportActiveCaloric()
					+ totalDatum.getSportIdleCaloric());
			setSportTxtDistance(totalDatum.getSportDistance());
			setSportTxtActiveCaloric(totalDatum.getSportActiveCaloric());
			setSportTxtActiveTime(totalDatum.getSportActiveTime());
			setSportHistogramDatum(historySportDatum);
		} else if (totalDatum.getType() == ActionsDatum.DATUM_TYPE_SLEEP) {
			// Log.e(TAG, "refreshUI for SLEEP");
			setSleepCpbProgress(totalDatum.getSleepTotalTime());
			setSleepTxtLightSleepTime(totalDatum.getSleepLightTime());
			setSleepTxtDeepSleepTime(totalDatum.getSleepDeepTime());
			setSleepTxtAwakeCount(totalDatum.getSleepAwakeCount());
			setSleepTxtAwakeTime(totalDatum.getSleepAwakeTime());
			setSleepHistogramDatum(historySleepDatum);

			/**
			 * if( mCentralService != null ) { mSleepPieceIndex =
			 * mCentralService.getHistorySleepDatumIndex(); int size =
			 * mCentralService.getHistorySleepDatumSize(); int index =
			 * mCentralService.getHistorySleepDatumIndex(); if( size <= 1 ) {
			 * mImageViewSleepPrvPiece.setImageResource(R.drawable.prv_0);
			 * mImageViewSleepNxtPiece.setImageResource(R.drawable.nxt_0); }
			 * else {
			 * 
			 * } } /
			 **/
		}
	}

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			Log.i(TAG, "CentralServiceConnection --- onServiceConnected");
			mCentralService = ((CentralService.LocalBinder) service)
					.getService();

			Calendar calendar = Calendar.getInstance();
			int utc = (int) (calendar.getTimeInMillis() / 1000) - 24 * 3600 * 0;

			ActionsDatum totalSportDatum = mCentralService
					.getTotalSportDatumByDate(
							DatabaseManager.DATUM_TYPE_BY_DAY, utc);
			List<Integer> historySportDatum = mCentralService
					.getHistorySportDatumByDate(
							DatabaseManager.DATUM_TYPE_BY_DAY, utc);
			refreshUI(totalSportDatum, historySportDatum, null);

			ActionsDatum totalSleepDatum = mCentralService
					.getTotalSleepDatumByDate(
							DatabaseManager.DATUM_TYPE_BY_DAY, utc);
			List<ActionsDatum> historySleepDatum = mCentralService
					.getHistorySleepDatumByDate(
							DatabaseManager.DATUM_TYPE_BY_DAY, utc);
			refreshUI(totalSleepDatum, null, historySleepDatum);

			updateUserAndDeviceInfo();

			// mCentralService.requestRefreshDatum();
			mCentralService.requestConnectionCheckOnly();

			if (mCentralService.getBatteryLevel() <= 0) {
				mTextViewBatteryLevel.setText("N/A");
			} else {
				mTextViewBatteryLevel.setText(mCentralService.getBatteryLevel()
						+ "%");
			}

			if (mLocalStorage.getFstUseApp()) {
				Log.e(TAG, "mLocalStorage.getFstUseApp() == true");
				// mTextViewNoticePullDown.setVisibility(TextView.VISIBLE);
			} else {
				Log.e(TAG, "mLocalStorage.getFstUseApp() == false");
				// mTextViewNoticePullDown.setVisibility(TextView.GONE);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			Log.i(TAG, "CentralServiceConnection --- onServiceDisconnected");
			mCentralService = null;
		}
	};

	/**/
	private boolean checkDeviceHardware() {
		if (!getActivity().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			return false;
		}
		return true;
	}

	/**/
	private static IntentFilter makeGattUpdateIntentFilter() {

		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CentralService.ACTION_BLUETOOTH_IS_DISABLE);

		intentFilter.addAction(CentralService.ACTION_BLUETOOTH_SCAN_START);
		intentFilter.addAction(CentralService.ACTION_BLUETOOTH_SCAN_COMPLETE);

		intentFilter
				.addAction(CentralService.ACTION_BLUETOOTH_LE_STATE_CHANGING);
		intentFilter
				.addAction(CentralService.ACTION_BLUETOOTH_LE_GATT_DISCOVERED);

		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_SPORT_DATUM_CHANGED);
		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_SLEEP_DATUM_CHANGED);
		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_DATUM_REFRESH_START);
		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_DATUM_REFRESH_DOING);
		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_DATUM_REFRESH_COMPLETE);

		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_ADAPTER_SCAN_ERROR);
		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_SERVICE_GATT_ERROR);
		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_SERVICE_GATT_READ_ERROR);
		intentFilter.addAction(CentralService.ACTION_ACTIONS_SERVICE_GATT_BUSY);

		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_DATUM_SYNC_PROGRESS_CHANGED);
		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_FUNCTION_CONFIG_SUCCESS);
		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_FUNCTION_CONFIG_FAILURE);
		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_ACTIVITY_IN_STACK_ALL_CLEAR);
		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_NO_ACTIVATED_DEVICE_WARNNING);

		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_ENTER_DFU_IN_MANAGE_ACTIVITY);
		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_CONFIRM_DFU_IN_MANAGE_ACTIVITY);

		intentFilter
				.addAction(CentralService.ACTION_ACTIONS_UPDATE_BATTERY_LEVEL);
		return intentFilter;
	}

	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			Log.i(TAG, action);
			if (action.equals(CentralService.ACTION_BLUETOOTH_IS_DISABLE)) {
				openBluetoothReqest();
				return;
			} else if (action
					.equals(CentralService.ACTION_BLUETOOTH_LE_STATE_CHANGING)) {
				if (mCentralService.getLeConnectState() == BluetoothProfile.STATE_DISCONNECTED) {
					mBluetoothConnectionState.setImageDrawable(getResources()
							.getDrawable(R.drawable.bt_disconnected));
				} else if (mCentralService.getLeConnectState() == BluetoothProfile.STATE_CONNECTED) {
					mBluetoothConnectionState.setImageDrawable(getResources()
							.getDrawable(R.drawable.bt_connected));
				} else {
					mBluetoothConnectionState.setImageDrawable(getResources()
							.getDrawable(R.drawable.bt_state_changing));
				}
				return;
			} else if (action
					.equals(CentralService.ACTION_BLUETOOTH_LE_GATT_DISCOVERED)) {
				return;
			} else if (action
					.equals(CentralService.ACTION_BLUETOOTH_SCAN_START)) {
				return;
			} else if (action
					.equals(CentralService.ACTION_BLUETOOTH_SCAN_COMPLETE)) {
				Map<String, Object> map1 = mCentralService.getMinRssiLeDevice();
				Log.i(TAG,
						"MIN RSSI DEVICE: name = "
								+ map1.get(CentralService.DEVICE_INFO_NAME)
								+ " addr = "
								+ map1.get(CentralService.DEVICE_INFO_ADDR)
								+ " rssi = "
								+ map1.get(CentralService.DEVICE_INFO_RSSI));
				/**
				 * for( Map<String,Object> map:
				 * mCentralService.getLeDevicesInfo() ) { Log.i( TAG,
				 * "LE DEVICE IN RANGE = " +
				 * map.get(CentralService.DEVICE_INFO_NAME) ); if(
				 * mCentralService.getBindedDevice() == null ) continue; if(
				 * mCentralService
				 * .getBindedDevice().equals((String)map.get(CentralService
				 * .DEVICE_INFO_ADDR)) ) { Log.w(TAG, "Binded Device --- " +
				 * map.get(CentralService.DEVICE_INFO_NAME) +
				 * " is in range!!!"); return; } } /
				 **/
				if (mCentralService.getBindedDevice() == null) {
					Log.e(TAG, "Not Binded Device!");
					return;
				} else {
					Log.e(TAG, "Binded Device is not in Range!!!");
					return;
				}
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_SPORT_DATUM_CHANGED)) {
				mDayCount = 0;
				mImageViewTitleNxtRecord
						.setImageResource(R.drawable.nxt_freeze);

				if (mPagerPostion == 0)
					mTextViewTitleDateTime
							.setText(R.string.string_title_sport_datetime);
				else
					mTextViewTitleDateTime
							.setText(R.string.string_title_sleep_datetime);

				Calendar calendar = Calendar.getInstance();
				int utc = (int) (calendar.getTimeInMillis() / 1000) - 24 * 3600 * 0;
				ActionsDatum totalSportDatum = mCentralService
						.getTotalSportDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				List<Integer> historySportDatum = mCentralService
						.getHistorySportDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				refreshUI(totalSportDatum, historySportDatum, null);

				ActionsDatum totalSleepDatum = mCentralService
						.getTotalSleepDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				List<ActionsDatum> historySleepDatum = mCentralService
						.getHistorySleepDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				refreshUI(totalSleepDatum, null, historySleepDatum);
				return;
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_SLEEP_DATUM_CHANGED)) {
				mDayCount = 0;
				mImageViewTitleNxtRecord
						.setImageResource(R.drawable.nxt_freeze);

				if (mPagerPostion == 0)
					mTextViewTitleDateTime
							.setText(R.string.string_title_sport_datetime);
				else
					mTextViewTitleDateTime
							.setText(R.string.string_title_sleep_datetime);

				Calendar calendar = Calendar.getInstance();
				int utc = (int) (calendar.getTimeInMillis() / 1000) - 24 * 3600 * 0;
				ActionsDatum totalSportDatum = mCentralService
						.getTotalSportDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				List<Integer> historySportDatum = mCentralService
						.getHistorySportDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				refreshUI(totalSportDatum, historySportDatum, null);

				ActionsDatum totalSleepDatum = mCentralService
						.getTotalSleepDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				List<ActionsDatum> historySleepDatum = mCentralService
						.getHistorySleepDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				refreshUI(totalSleepDatum, null, historySleepDatum);
				return;
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_DATUM_REFRESH_START)) {
				// refreshableView.startRefreshing();
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_DATUM_REFRESH_COMPLETE)) {
				// mTextViewNoticePullDown.setVisibility(TextView.GONE);
				mProgressBarDatumSync.setVisibility(ProgressBar.GONE);
				refreshableView.onRefreshComplete();

				mDayCount = 0;
				mImageViewTitleNxtRecord
						.setImageResource(R.drawable.nxt_freeze);

				if (mPagerPostion == 0)
					mTextViewTitleDateTime
							.setText(R.string.string_title_sport_datetime);
				else
					mTextViewTitleDateTime
							.setText(R.string.string_title_sleep_datetime);

				Calendar calendar = Calendar.getInstance();
				int utc = (int) (calendar.getTimeInMillis() / 1000) - 24 * 3600 * 0;

				String syncTime = String.format(getActivity().getResources()
						.getString(R.string.string_last_sync_time), Utils
						.utc2DateTime(utc));
				mTextViewLastSyncTime.setText(syncTime);

				// Log.i(TAG, "Robin----最后同步时间222--"+syncTime);

				ActionsDatum totalSportDatum = mCentralService
						.getTotalSportDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				List<Integer> historySportDatum = mCentralService
						.getHistorySportDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				refreshUI(totalSportDatum, historySportDatum, null);

				ActionsDatum totalSleepDatum = mCentralService
						.getTotalSleepDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				List<ActionsDatum> historySleepDatum = mCentralService
						.getHistorySleepDatumByDate(
								DatabaseManager.DATUM_TYPE_BY_DAY, utc);
				refreshUI(totalSleepDatum, null, historySleepDatum);

				ConfigRspDialog nt = new ConfigRspDialog(getActivity(),
						getResources().getString(
								R.string.string_refresh_success));
				nt.show(1000);

				Log.i(TAG, "Robin---------同步成功！！！！");

//				initAlarmSyncTask(); // 开始 设置下次刷新时间
				getLastUploadUtc();
				// uploadDeviceDatum(historySleepDatum,
				// mCentralService.getHistorySportDatumByDate2(DatabaseManager.DATUM_TYPE_BY_DAY,
				// utc));
				return;
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_ADAPTER_SCAN_ERROR)) {
				mProgressBarDatumSync.setVisibility(ProgressBar.GONE);
				refreshableView.onRefreshComplete();
				ConfigRspDialog nt = new ConfigRspDialog(getActivity(),
						getResources().getString(
								R.string.string_refresh_failure_0));
				nt.show(1000);

			} else if (action
					.equals(CentralService.ACTION_ACTIONS_SERVICE_GATT_ERROR)) {
				mProgressBarDatumSync.setVisibility(ProgressBar.GONE);
				refreshableView.onRefreshComplete();

				// int status = intent.getExtras().getInt("GATT_STATUS");
				// String format =
				// String.format(getResources().getString(R.string.string_refresh_failure_1),
				// status);
				String format = getString(R.string.string_refresh_failure_1);
				ConfigRspDialog nt = new ConfigRspDialog(getActivity(), format);
				nt.show(1000);
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_SERVICE_GATT_READ_ERROR)) {
				mProgressBarDatumSync.setVisibility(ProgressBar.GONE);
				refreshableView.onRefreshComplete();

				// int status = intent.getExtras().getInt("GATT_STATUS");
				// String format =
				// String.format(getResources().getString(R.string.string_refresh_failure_2),
				// status);
				String format = getString(R.string.string_refresh_failure_2);
				ConfigRspDialog nt = new ConfigRspDialog(getActivity(), format);
				nt.show(1000);
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_DATUM_SYNC_PROGRESS_CHANGED)) {
				mProgressBarDatumSync.setProgress(mCentralService
						.getSyncProgress());
				mProgressBarDatumSync.setVisibility(ProgressBar.VISIBLE);
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_FUNCTION_CONFIG_SUCCESS)) {
				// Toast.makeText(MainActivity.this,
				// getResources().getString(R.string.string_config_success),
				// Toast.LENGTH_SHORT).show();
				ConfigRspDialog nt = new ConfigRspDialog(getActivity(),
						getResources()
								.getString(R.string.string_config_success));
				nt.show(1000);
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_FUNCTION_CONFIG_FAILURE)) {
				// Toast.makeText(MainActivity.this,
				// getResources().getString(R.string.string_config_failure),
				// Toast.LENGTH_SHORT).show();
				ConfigRspDialog nt = new ConfigRspDialog(getActivity(),
						getResources()
								.getString(R.string.string_config_failure));
				nt.show(1000);
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_ACTIVITY_IN_STACK_ALL_CLEAR)) {
				// finish();
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_SERVICE_GATT_BUSY)) {
				ConfigRspDialog nt = new ConfigRspDialog(getActivity(),
						getResources()
								.getString(R.string.string_config_failure));
				nt.show(1000);
				mProgressBarDatumSync.setVisibility(ProgressBar.GONE);
				refreshableView.onRefreshComplete();
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_UPDATE_BATTERY_LEVEL)) {
				if (mCentralService.getBatteryLevel() <= 0) {
					mTextViewBatteryLevel.setText("N/A");
				} else {
					mTextViewBatteryLevel.setText(mCentralService
							.getBatteryLevel() + "%");
				}
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_NO_ACTIVATED_DEVICE_WARNNING)) {
				refreshableView.onRefreshComplete();
				new AlertDialog.Builder(getActivity())
						.setMessage(R.string.deviceError)
						.setTitle(R.string.exit_notice)
						.setPositiveButton(R.string.string_dialog_positive,
								mConfirmYesNo)
						.setNegativeButton(R.string.string_dialog_negative,
								null).show();
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_ENTER_DFU_IN_MANAGE_ACTIVITY)) {
				Log.e(TAG, "IN ENTER: getFragmentId() = "
						+ ((ManagerActivity) getActivity()).getFragmentId());
				if (((ManagerActivity) getActivity()).getFragmentId() == 0) {
					mCentralService.initUpdateRomDialog(getActivity());
				}
			} else if (action
					.equals(CentralService.ACTION_ACTIONS_CONFIRM_DFU_IN_MANAGE_ACTIVITY)) {
				Log.e(TAG, "IN CONFIRM: getFragmentId() = "
						+ ((ManagerActivity) getActivity()).getFragmentId());
				if (((ManagerActivity) getActivity()).getFragmentId() == 0)
					/**
					 * 有更新时显示红点提示
					 */
					showRedPoint();
				// mCentralService.initConfirmUpdateRomDialog(getActivity());
				// ////////////////////////////

			}
		}
	};

	DialogInterface.OnClickListener mConfirmYesNo = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				enterPairsActivity();
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}
	};

	/**
	 * ViewPager适配器
	 */
	public class MyPagerAdapter extends PagerAdapter {
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public void finishUpdate(View arg0) {

		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(mListViews.get(arg1), 0);
			return mListViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int position) {
			// if(mPreSelectedBt != null){
			// mPreSelectedBt.setBackgroundResource(R.drawable.btn_dynamic_mode_sleep_pressed);
			// }
			/**/
			mPagerPostion = position;
			if (position == 0) {
				Button currentBt = (Button) mNumLayout.getChildAt(position);
				currentBt
						.setBackgroundResource(R.drawable.btn_dynamic_mode_sport);
				currentBt.setAlpha(0.7f);
				Button releaseBt = (Button) mNumLayout.getChildAt(1);
				releaseBt.setBackgroundResource(R.drawable.dummy);

				mTextViewTitleDescript.setTextColor(getResources().getColor(
						R.color.black));
				mTextViewTitleDateTime.setTextColor(getResources().getColor(
						R.color.black));

				mTextViewTitleDescript
						.setText(getString(R.string.string_title_sport_descript));
				if (mDayCount == 0) {
					mTextViewTitleDateTime
							.setText(getString(R.string.string_title_sport_datetime));
				}

				((ManagerActivity) getActivity()).setTabSelectionColor(0);
			} else {
				Button currentBt = (Button) mNumLayout.getChildAt(position);
				currentBt
						.setBackgroundResource(R.drawable.btn_dynamic_mode_sleep);
				currentBt.setAlpha(0.7f);
				Button releaseBt = (Button) mNumLayout.getChildAt(0);
				releaseBt.setBackgroundResource(R.drawable.dummy);

				mTextViewTitleDescript.setTextColor(getResources().getColor(
						R.color.black));
				mTextViewTitleDateTime.setTextColor(getResources().getColor(
						R.color.black));

				mTextViewTitleDescript
						.setText(getString(R.string.string_title_sleep_descript));
				if (mDayCount == 0) {
					mTextViewTitleDateTime
							.setText(getString(R.string.string_title_sleep_datetime));
				}

				((ManagerActivity) getActivity()).setTabSelectionColor(1);
			}

			/**
			 * Calendar calendar = Calendar.getInstance(); int utc =
			 * (int)(calendar.getTimeInMillis()/1000) + 24*3600*mDayCount;
			 * 
			 * if( mPagerPostion == 0 ) { ActionsDatum totalSportDatum =
			 * mCentralService
			 * .getTotalSportDatumByDate(DatabaseManager.DATUM_TYPE_BY_DAY,
			 * utc); List<Integer> historySportDatum =
			 * mCentralService.getHistorySportDatumByDate
			 * (DatabaseManager.DATUM_TYPE_BY_DAY, utc);
			 * refreshUI(totalSportDatum, historySportDatum,
			 * ActionsDatum.DATUM_TYPE_SPORT); return; }
			 * 
			 * ActionsDatum totalSleepDatum =
			 * mCentralService.getTotalSleepDatumByDate
			 * (DatabaseManager.DATUM_TYPE_BY_DAY, utc); List<ActionsDatum>
			 * historySleepDatum =
			 * mCentralService.getHistorySleepDatumByDate(DatabaseManager
			 * .DATUM_TYPE_BY_DAY, utc); refreshUI(totalSleepDatum,
			 * historySleepDatum, ActionsDatum.DATUM_TYPE_SLEEP); /
			 **/
			// Log.i("INFO", "current item:"+position);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

	}

	private void enterPairsActivity() {
		Intent intent = new Intent();
		intent.setClass(getActivity(), SettingPairsActivity.class);
		startActivity(intent);
		getActivity().overridePendingTransition(
				R.anim.translate_slide_in_right,
				R.anim.translate_slide_out_left);
	}

	/**
	 * private void enterSettingActivity() { Intent intent = new Intent();
	 * intent.setClass(MainActivity.this, MainSettingActivity.class);
	 * startActivity(intent); overridePendingTransition(
	 * R.anim.translate_slide_in_right, R.anim.translate_slide_out_left ); } /
	 **/

	private void refreshDID() {
		String deviceNameString = "N/A";
		try {
			deviceNameString = URLEncoder.encode(
					mLocalStorage.getActivatedDeviceName(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		new NetworkUtils(getActivity(), new NetworkCallback() {
			@Override
			public void OnPreExcute() {
				Log.e(TAG, "onPreExcute");
			}

			@Override
			public void OnExcuteSuccess(JSONObject obj) throws JSONException {
				Log.e(TAG, "OnExcuteSuccess:" + obj.toString());
				mLocalStorage.saveDID(obj.getLong("did"));
			}

			@Override
			public void OnExcuteFailure(JSONObject obj) throws JSONException {
				Log.e(TAG, "OnExcuteFailure:" + obj.toString());
				int rs = obj.getInt("rs");
				if (rs == 400) {
					Intent mainIntent = new Intent(getActivity(),
							LoginActivity.class);
					startActivity(mainIntent);
					getActivity().overridePendingTransition(
							R.anim.translate_slide_in_right,
							R.anim.translate_slide_out_left);
					getActivity().finish();
				}
			}

			@Override
			public void OnTimeOut() {

				Log.e(TAG, "OnTimeOut");
			}
		}).request(NetworkUtils.POST, Urls.DEVICE_INFO, mLocalStorage.getSID(),
				mLocalStorage.getActivatedDeviceSrln(), deviceNameString,
				mLocalStorage.getActivatedDeviceHwvr(),
				mLocalStorage.getActivatedDeviceFwvr(), "",
				mLocalStorage.getActivatedDeviceAddr());
	}

	private void getLastUploadUtc() {
		/**/
		new NetworkUtils(getActivity(), new NetworkCallback() {
			@Override
			public void OnPreExcute() {
				Log.e(TAG, "onPreExcute");
			}

			@Override
			public void OnExcuteSuccess(JSONObject obj) throws JSONException {
				Log.e(TAG, "OnExcuteSuccess:" + obj.toString());
				int Lastutc = (int) obj.getLong("dataTimestamp");
				if (Lastutc < 1412092800) {
					Lastutc = 1412092800;
				}
				// mLocalStorage.saveUPLOADUTC(obj.getLong("dataTimestamp"));
				Log.e(TAG, "LAST UPLOAD TIME:" + Utils.utc2DateTime(Lastutc));
				uploadDeviceDatum(mCentralService.getNewSleepDatum(Lastutc),
						mCentralService.getNewSportDatum(Lastutc), Lastutc);
			}

			@Override
			public void OnExcuteFailure(JSONObject obj) {
				Log.e(TAG, "OnExcuteFailure:" + obj.toString());
			}

			@Override
			public void OnTimeOut() {
				Log.e(TAG, "OnTimeOut");
			}
		}).request(NetworkUtils.GET, Urls.GETLASTDATAUTCTIME,
				mLocalStorage.getSID(), mLocalStorage.getDID());
		/**/
	}

	private JSONArray datums2JSONArray(List<ActionsDatum> sleepDatums,
			List<ActionsDatum> sportDatums, int utcFilter) {
		JSONArray DatumArray = new JSONArray();
		try {
			if (sleepDatums != null) {
				for (ActionsDatum datum : sleepDatums) {
					if (datum.getUTC() <= utcFilter)
						continue;
					if (datum.getType() == ActionsDatum.DATUM_TYPE_SPORT)
						continue;
					JSONObject sportDataObject = new JSONObject();
					JSONObject sportDataContent = new JSONObject();
					if (datum.getType() == ActionsDatum.DATUM_TYPE_SLEEP) {
						sportDataContent.put("timeDeeply",
								datum.getSleepDeepTime());
						sportDataContent.put("timeLight",
								datum.getSleepLightTime());
						sportDataContent.put("timeAwake",
								datum.getSleepAwakeTime());
						sportDataContent.put("timeTotal",
								datum.getSleepTotalTime());
						sportDataContent.put("countAwake",
								datum.getSleepAwakeCount());
						sportDataContent.put("savedTS", datum.getUTC());
						sportDataContent.put("sleepStatus",
								datum.getDetailType());
					}
					sportDataObject.put("dataObject", sportDataContent);
					sportDataObject.put("dataType", datum.getType());
					DatumArray.put(sportDataObject);

					Log.e(TAG, "DEEPSLEEP:" + datum.getSleepDeepTime() + ",	"
							+ "LIGHTSLEEP:" + datum.getSleepLightTime() + ",	"
							+ "AWAKETIME:" + datum.getSleepAwakeTime() + ",	"
							+ "TOTALTIME:" + datum.getSleepTotalTime() + ",	"
							+ "AWAKECOUNT:" + datum.getSleepAwakeCount() + ",	"
							+ "UTC:" + datum.getUTC() + ",	" + "SLEEPSTATE:"
							+ datum.getDetailType() + ",	" + "DATETIME:"
							+ datum.getDetailType());
				}
			}

			if (sportDatums != null) {
				for (ActionsDatum datum : sportDatums) {
					if (datum.getUTC() <= utcFilter)
						continue;
					if (datum.getType() == ActionsDatum.DATUM_TYPE_SLEEP)
						continue;
					JSONObject sportDataObject = new JSONObject();
					JSONObject sportDataContent = new JSONObject();
					if (datum.getType() == ActionsDatum.DATUM_TYPE_SPORT) {
						sportDataContent.put("step", datum.getSportSteps());
						sportDataContent.put("distance",
								datum.getSportDistance());
						sportDataContent.put("calorieIdle",
								datum.getSportIdleCaloric());
						sportDataContent.put("calorieActive",
								datum.getSportActiveCaloric());
						sportDataContent.put("timeIdle",
								datum.getSportIdleTime());
						sportDataContent.put("timeActive",
								datum.getSportActiveTime());
						sportDataContent.put("savedTS", datum.getUTC());
						sportDataContent.put("sportsType",
								datum.getDetailType());
					}
					sportDataObject.put("dataObject", sportDataContent);
					sportDataObject.put("dataType", datum.getType());
					DatumArray.put(sportDataObject);

					Log.e(TAG, "STEP:" + datum.getSportSteps() + ",	"
							+ "DISTANCE:" + datum.getSportDistance() + ",	"
							+ "IDLECAL:" + datum.getSportIdleCaloric() + ",	"
							+ "ACTIVECAL:" + datum.getSportActiveCaloric()
							+ ",	" + "IDLETIME:" + datum.getSportIdleTime()
							+ ",	" + "ACTIVETIME:" + datum.getSportActiveTime()
							+ ",	" + "UTC:" + datum.getUTC() + ",	"
							+ "SPORTTYPE:" + datum.getDetailType() + ",	"
							+ "DATATYPE:" + datum.getType());
				}
			}
			/**
			 * int i; for( i = 0; i <
			 * ((DatumArray.toString().length())/(2*1024)); i++ ) { Log.e(TAG,
			 * DatumArray.toString().substring(i*1024*2, (i+1)*1024*2)); } if(
			 * ((DatumArray.toString().length())%(2*1024)) != 0) { Log.e(TAG,
			 * DatumArray.toString().substring(i*1024*2)); } /
			 **/
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		return DatumArray;
	}

	private void uploadDeviceDatum(List<ActionsDatum> sleepDatum,
			List<ActionsDatum> sportdatums, int utcFilter) {
		Log.e(TAG,
				"SQLITE - MIN. DATUM UTC = "
						+ Utils.utc2DateTime(mCentralService
								.getSQLiteOldestUtc()));
		Log.e(TAG,
				"SQLITE - MAX. DATUM UTC = "
						+ Utils.utc2DateTime(mCentralService
								.getSQLiteRecentUtc()));
		JSONArray arrays = datums2JSONArray(sleepDatum, sportdatums, utcFilter);
		if (arrays.length() <= 0) {
			Log.e(TAG, "没有数据需要上传！");
			/**
			 * new NetworkUtils(getActivity(), new NetworkCallback() {
			 * 
			 * @Override public void OnPreExcute() { Log.e(TAG, "onPreExcute");
			 *           }
			 * @Override public void OnExcuteSuccess(JSONObject obj) throws
			 *           JSONException { Log.e(TAG, "OnExcuteSuccess:" +
			 *           obj.toString()); }
			 * @Override public void OnExcuteFailure(JSONObject obj) {
			 *           Log.e(TAG, "OnExcuteFailure"); }
			 * @Override public void OnTimeOut() { Log.e(TAG, "OnTimeOut"); }
			 *           //}).request(NetworkUtils.GET, Urls.GETSLEEPDATADAY,
			 *           mLocalStorage.getSID(), mLocalStorage.getDID(),
			 *           Utils.getTodayDate()); }).request(NetworkUtils.GET,
			 *           Urls.GETSPORTDAY, mLocalStorage.getSID(),
			 *           mLocalStorage.getDID(), Utils.getTodayDate());
			 *           //}).request(NetworkUtils.GET, Urls.GETDAYDATA,
			 *           mLocalStorage.getSID(), mLocalStorage.getDID(),
			 *           Utils.getTodayDate(), Utils.getLanguageCode()); /
			 **/
			return;
		}

		new NetworkUtils(getActivity(), new NetworkCallback() {
			@Override
			public void OnPreExcute() {
				Log.e(TAG, "onPreExcute");
			}

			@Override
			public void OnExcuteSuccess(JSONObject obj) throws JSONException {
				Log.e(TAG, "OnExcuteSuccess");
				// refreshUploadUtc();
			}

			@Override
			public void OnExcuteFailure(JSONObject obj) {
				Log.e(TAG, "OnExcuteFailure");
			}

			@Override
			public void OnTimeOut() {
				Log.e(TAG, "OnTimeOut");
			}
		}).postDatums(Urls.UPLOAD_DATA, mLocalStorage.getSID(),
				mLocalStorage.getDID(), arrays.toString());
	}

	/**
	 * 添加所有的平台</br>
	 */
	private void addCustomPlatforms() {
		// 添加微信平台
//		addWXPlatform();
		// 添加QQ平台
//		addQQQZonePlatform();
		// 添加facebook平台
		addFacebook();
		// 添加新浪SSO授权
		mController.getConfig().setSsoHandler(new SinaSsoHandler());
	}

	/**
	 * @Title: addFacebook
	 * @Description:
	 * @throws
	 */
	private void addFacebook() {
		UMFacebookHandler mFacebookHandler = new UMFacebookHandler(
				getActivity());
		mFacebookHandler.addToSocialSDK();

		UMImage localImage = new UMImage(getActivity(), myShot(getActivity()));
		FaceBookShareContent fbContent = new FaceBookShareContent();
		fbContent.setShareImage(localImage);
		mController.setShareMedia(fbContent);
		// 注意：Facebook在有客户端的时候分享支持的的类型有：纯文本、纯图片（url和本地）、图文（图片必须是url）；
		// 在无客户端的时候仅仅支持纯文本分享。对于纯文本、图文必须设置targetUrl字段。
	}

	/**
	 * @功能描述 : 添加QQ平台支持 QQ分享的内容， 包含四种类型， 即单纯的文字、图片、音乐、视频. 参数说明 : title, summary,
	 *       image url中必须至少设置一个, targetUrl必须设置,网页地址必须以"http://"开头 . title :
	 *       要分享标题 summary : 要分享的文字概述 image url : 图片地址 [以上三个参数至少填写一个] targetUrl
	 *       : 用户点击该分享时跳转到的目标地址 [必填] ( 若不填写则默认设置为友盟主页 )
	 * @return
	 */
//	private void addQQQZonePlatform() {
//		String appId = "801556848";
//		String appKey = "5bc9c2b47e38eb5ab50107193e8dce1a";
//
//		// 添加QZone平台
//		QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(getActivity(),
//				appId, appKey);
//		qZoneSsoHandler.addToSocialSDK();
//		QZoneShareContent qzone = new QZoneShareContent();
//		// 设置分享文字
//		qzone.setShareContent("-++-----++来自友盟社会化组件（SDK）让移动应用快速整合社交分享功能 -- QZone");
//		// 设置点击消息的跳转URL
//		qzone.setTargetUrl("http://www.foogeez.com/");
//		// 设置分享内容的标题
//		qzone.setTitle(getString(R.string.share_title_rank));
//		// 设置分享图片Qzone分享只支持图文形式分享，并且点击图文消息会跳转到预设的链接，图文分享只展示缩略图，**无法查看大图**
//		qzone.setShareImage(new UMImage(getActivity(), R.drawable.ic_launcher));
//		mController.setShareMedia(qzone);
//	}

	/**
	 * @功能描述 : 添加微信平台分享
	 * @return
	 */
//	private void addWXPlatform() {
//		// 注意：在微信授权的时候，必须传递appSecret
//		// wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
//		String appID = "wx05c7a5fbcea2453a";
//		String appSecret = "2819142c447534782df2d53876026d24";
//		// 添加微信平台
//		UMWXHandler wxHandler = new UMWXHandler(getActivity(), appID, appSecret);
//		wxHandler.addToSocialSDK();
//
//		WeiXinShareContent weixinContent = new WeiXinShareContent();
//		weixinContent
//				.setShareContent("来自友盟社会化组件（SDK）让移动应用快速整合社交分享功能-微信。http://www.umeng.com/social");
//		weixinContent.setTitle("友盟社会化分享组件-微信");
//		weixinContent.setTargetUrl("http://www.umeng.com/social");
//		mController.setShareMedia(weixinContent);
//
//		// 支持微信朋友圈
//		UMWXHandler wxCircleHandler = new UMWXHandler(getActivity(), appID,
//				appSecret);
//		wxCircleHandler.setToCircle(true);
//		wxCircleHandler.addToSocialSDK();
//
//		// 设置微信朋友圈分享内容
//		CircleShareContent circleMedia = new CircleShareContent();
//		circleMedia.setShareContent("来自友盟社会化组件（SDK）让移动应用快速整合社交分享功能，朋友圈");
//		// 设置朋友圈title
//		circleMedia.setTitle("友盟社会化分享组件-朋友圈");
//		circleMedia.setShareImage(new UMImage(getActivity(),
//				R.drawable.ic_launcher));
//		circleMedia.setTargetUrl("http://www.baidu.com");
//		mController.setShareMedia(circleMedia);
//
//	}

	/**
	 * 显示您的自定义界面，当用户点击一个平台时，直接调用directShare或者postShare来分享.
	 */
	private void showCustomUI(final boolean isDirectShare) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
				getActivity());
		dialogBuilder.setTitle(R.string.share_dialog_title);
		final CharSequence[] items = { "Facebook" };
		dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// 获取用户点击的平台
				SHARE_MEDIA platform = mPlatformsMap.get(items[which]);
				if (isDirectShare) {
					// 调用直接分享
					mController.directShare(getActivity(), platform,
							mShareListener);
				} else {
					// 调用直接分享, 但是在分享前用户可以编辑要分享的内容
					mController.postShare(getActivity(), platform,
							mShareListener);
				}
			} // end of onClick
		});

		dialogBuilder.create().show();
	}

	/**
	 * 初始化平台map
	 */
	private void initPlatformMap() {
		// mPlatformsMap.put("新浪微博", SHARE_MEDIA.SINA);
		// mPlatformsMap.put("QQ", SHARE_MEDIA.QQ);
		// mPlatformsMap.put("QQ空间", SHARE_MEDIA.QZONE);
		mPlatformsMap.put("Facebook", SHARE_MEDIA.FACEBOOK);

	}

	/**
	 * 分享监听器
	 */
	SnsPostListener mShareListener = new SnsPostListener() {

		@Override
		public void onStart() {
			Toast.makeText(getActivity(), R.string.share_start,
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onComplete(SHARE_MEDIA platform, int stCode,
				SocializeEntity entity) {
			if (stCode == 200) {
				Toast.makeText(getActivity(), R.string.share_complete,
						Toast.LENGTH_SHORT).show();
			} else {
				Log.i(TAG, "分享失败 : error code : " + stCode);
			}
		}
	};

	/**
	 * 显示消息提示的红点
	 */
	private void showRedPoint() {
		BadgeView settingView = new BadgeView(getActivity());
		settingView.setTargetView(redNotice);
		settingView.setBadgeCount(1);
		settingView.setWidth(3);
		settingView.setHeight(3);
		settingView.setTextColor(0x00000000); // 设置字体颜色为透明
		settingView.setBackgroundResource(R.drawable.red_notice);
		settingView.setBadgeGravity(Gravity.CENTER);
		settingView.setVisibility(View.VISIBLE);
		redNotice.setVisibility(View.VISIBLE);
	}

	// 在你的Fragment中覆盖onActivityResult方法，如下：
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(
				requestCode);
		if (ssoHandler != null) {
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	public Bitmap myShot(Activity activity) {
		// 获取windows中最顶层的view
		View view = activity.getWindow().getDecorView();
		view.buildDrawingCache();

		// 获取状态栏高度
		Rect rect = new Rect();
		view.getWindowVisibleDisplayFrame(rect);
		int statusBarHeights = rect.top;
		Display display = activity.getWindowManager().getDefaultDisplay();

		// 获取屏幕宽和高
		int widths = display.getWidth();
		int heights = display.getHeight();

		// 允许当前窗口保存缓存信息
		view.setDrawingCacheEnabled(true);

		// 去掉状态栏
		Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0,
				statusBarHeights, widths, heights - statusBarHeights);

		// 销毁缓存信息
		view.destroyDrawingCache();

		return bmp;
	}

	/** Name:Robin Time:20150928 Function: 友盟session的统计 */

	@Override
	public void onResume() {
		Log.i(TAG, "Robin---------onResume()");
		super.onResume();
		MobclickAgent.onPageStart(mPageName);

		if (!mLocalStorage.isDeviceActivated()) {
			mBindNewDevice.setVisibility(ImageView.VISIBLE);
		} else {
			mBindNewDevice.setVisibility(ImageView.GONE);
			/** Robin 20151029 --> 判断为汉语、韩语时，分享图标的可见性 */
			String locale = Locale.getDefault().getLanguage();
			if (locale.equals("zh") || locale.equals("ko")) {
				share.setVisibility(ImageView.GONE);
			} else {
				share.setVisibility(ImageView.VISIBLE);
			}
		}
	}

}
