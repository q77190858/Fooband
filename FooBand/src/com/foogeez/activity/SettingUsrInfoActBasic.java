package com.foogeez.activity;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.foogeez.configuration.LocalStorage;
import com.foogeez.configuration.Configuration.UsrInfoConfiguration;
import com.foogeez.dialog.UsrInfoNickNameSettingDialog;
import com.foogeez.fooband.R;
import com.foogeez.https.Urls;
import com.foogeez.network.NetworkUtils;
import com.foogeez.network.NetworkUtils.NetworkCallback;
import com.foogeez.services.CentralService;
import com.grdn.util.Utils;
import com.umeng.analytics.MobclickAgent;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

public class SettingUsrInfoActBasic extends SettingActivity {

	private final static String TAG = SettingUsrInfoActBasic.class
			.getSimpleName();

	private ImageView mMyAvatar = null;
	private File mCurrentPhotoFile;
	private Bitmap imageBitmap;

	private LocalStorage mLocalStorage = new LocalStorage(
			SettingUsrInfoActBasic.this);

	private Button btnNext;
	private TextView nickNameTxt;
	private ImageView imgViewVisible;
	private View avatarJudgeView;

	private static final int PHOTO_PICKED_WITH_DATA = 3000;
	private static final int CAMERA_WITH_DATA = 3001;
	private static final int CAMERA_CROP_RESULT = 3002;
	private static final int PHOTO_CROP_RESOULT = 3003;
	
	private CentralService mCentralService = null;
	private UsrInfoNickNameSettingDialog mUsrInfoNickNameSettingDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_usr_info_basic);

		Log.i(TAG, "Robin----------onCreate");

		initSettingTitle(R.string.setting_usr_info_act_basic_title);
		imgViewVisible = (ImageView) findViewById(R.id.id_iv_setting_back);
		imgViewVisible.setVisibility(View.GONE);
		
		avatarJudgeView = findViewById(R.id.view_judge_avatar);
		mMyAvatar = (ImageView) findViewById(R.id.id_iv_my_avatar_basic);
		mMyAvatar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 拍照获取
				doTakePhoto();
			}
		});
	
		nickNameTxt = (TextView) findViewById(R.id.id_tv_setting_usr_info_nickname_content);
		btnNext = (Button) findViewById(R.id.id_btn_usr_info_basic_next);
		btnNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				Log.i(TAG, "mMyAvatar------" +mMyAvatar.getBackground());
//				if(avatarJudgeView.getVisibility() == View.INVISIBLE){
//					Toast.makeText(getApplicationContext(), R.string.please_set_the_avatar, Toast.LENGTH_SHORT).show();
//					return;
//				}
				if(nickNameTxt.getText().equals("")){
//					Log.i(TAG, "nickNameTxt------22222" + nickNameTxt.getText());
					Toast.makeText(getApplicationContext(), R.string.please_set_the_nickname, Toast.LENGTH_SHORT).show();
					return;
				}
				enterSettingUsrInfoActDetail();
			}
		});
		
		((RelativeLayout) findViewById(R.id.id_rl_setting_usr_info_nickname))
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mUsrInfoNickNameSettingDialog = new UsrInfoNickNameSettingDialog(
						SettingUsrInfoActBasic.this, mLocalStorage
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
								nickNameTxt.setText(formatString4);
								mLocalStorage.saveUsrInfoConfig(config);
								updateUsrNickInfo(formatString4);

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

		
		/** Robin  --执行此方法只是为了下载图片名的ID */
		downloadUsrInfo();

	}

	/**
	 * 调用系统相机拍照
	 */
	protected void doTakePhoto() {
		try {
			String picName = mLocalStorage.restoreUsrInfoConfig().getId()
					+ ".jpg";
//			Log.i(TAG, "调用系统相机拍照"+picName);
			String savepath = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/fooband/avatar/";
//			Log.i(TAG, "调用系统相机拍照"+savepath);
			// Launch camera to take photo for selected contact
			File file = new File(savepath);
			if (!file.exists()) {
				file.mkdirs();
			}
			mCurrentPhotoFile = new File(file, picName);
			final Intent intent = getTakePickIntent(mCurrentPhotoFile);
			startActivityForResult(intent, CAMERA_WITH_DATA);
		} catch (ActivityNotFoundException e) {
			// Toast.makeText(this, R.string.photoPickerNotFoundText,
			// Toast.LENGTH_LONG).show();
			Log.e(TAG, "no camera");
			e.printStackTrace();
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
				uploadMyImage(savepath, picName);
				avatarJudgeView.setVisibility(View.GONE);
				Toast.makeText(this, R.string.avatar_update_success,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
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

	private void updateUsrNickInfo(String nickname) {
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

	/**
	 * 相机剪切图片
	 */
	protected void doCropPhoto(File f) {
		Log.i(TAG, "Robin------doCropPhoto");
		try {
			// Add the image to the media store
			MediaScannerConnection.scanFile(this,
					new String[] { f.getAbsolutePath() },
					new String[] { null }, null);

			// Launch gallery to crop the photo
			final Intent intent = getCropImageIntent(Uri.fromFile(f));
			startActivityForResult(intent, CAMERA_CROP_RESULT);
		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(this, R.string.photoPickerNotFoundText,
			// Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 获取系统剪裁图片的Intent.
	 */
	public static Intent getCropImageIntent(Uri photoUri) {
		Log.i(TAG, "Robin------getCropImageIntent");
		Intent intent = new Intent("com.android.camera.action.CROP");
		// Intent intent = new Intent("com.grdn.image.oprt.CropImage");
		Log.i(TAG, "Robin----------getCropImageIntent");
		intent.setDataAndType(photoUri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 234);
		intent.putExtra("outputY", 234);
		intent.putExtra("return-data", true);
		return intent;
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

	

	private void enterSettingUsrInfoActDetail() {
		Intent intent = new Intent(SettingUsrInfoActBasic.this,
				SettingUsrInfoActDetail.class);
		startActivity(intent);
		overridePendingTransition(R.anim.translate_slide_in_right,
				R.anim.translate_slide_out_left);
		finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			new AlertDialog.Builder(SettingUsrInfoActBasic.this)
					.setTitle(R.string.exit_notice)
					// 设置对话框标题
					.setMessage(R.string.exit_content)
					// 设置显示的内容
					.setPositiveButton(R.string.exit_select_back,
							new DialogInterface.OnClickListener() {// 添加确定按钮
								@Override
								public void onClick(DialogInterface dialog,
										int which) { // 后台运行
									// moveTaskToBack(false);
									finish();
								}
							})
					.setNegativeButton(R.string.exit_select_exit,
							new DialogInterface.OnClickListener() {// 添加返回按钮
								@Override
								public void onClick(DialogInterface dialog,
										int which) { // 退出
									Intent it = new Intent(
											SettingUsrInfoActBasic.this,
											CentralService.class);
									stopService(it);
									finish();
									System.exit(0);
								}
							}).show();// 在按键响应事件中显示此对话框

		}
		return true;
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
