package com.foogeez.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.foogeez.fooband.R;
import com.foogeez.services.CentralService;
import com.grdn.util.DrawableUtil;
import com.umeng.analytics.MobclickAgent;

public class ManagerActivity extends Activity implements OnClickListener {
	private static final String TAG = ManagerActivity.class.getSimpleName();

	FragmentManager mFragmentManager = null;

	MainFragment mMainFragment = null;
	RankFragment mRankFragment = null;
	SettingFragment mSettingFragment = null;

	View mMenuItemLayoutSport = null;
	View mMenuItemLayoutSleep = null;
	View mMenuItemLayoutRank = null;
	View mMenuItemLayoutSetting = null;

	TextView mMenuItemTextSport = null;
	TextView mMenuItemTextSleep = null;
	TextView mMenuItemTextRank = null;
	TextView mMenuItemTextSetting = null;

	ImageView mMenuItemImageSport = null;
	ImageView mMenuItemImageSleep = null;
	ImageView mMenuItemImageRank = null;
	ImageView mMenuItemImageSetting = null;

	private int mMenuItemId = 0;

	public int getFragmentId() {
		return mMenuItemId;
	}
	
	public static ManagerActivity instance = null;

//	private long mExitTime = 0; // 记录按返回键时间
	
	/** 20151029 Robin ----设置消息提示的红点 */
//	BadgeView settingBv = new BadgeView(this);  //设置的
//	BadgeView sportBv = new BadgeView(this); //运动的

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);  Robin-------无效设置
		setContentView(R.layout.activity_manager);

		initViews();

		mFragmentManager = getFragmentManager();
		setTabSelection(0);

		instance = this;
		
		Log.e(TAG, "ManagerActivity onCreate");
	}

	private void initViews() {
		mMenuItemLayoutSport = findViewById(R.id.sport_layout);
		mMenuItemLayoutSleep = findViewById(R.id.sleep_layout);
		mMenuItemLayoutRank = findViewById(R.id.rank_layout);
		mMenuItemLayoutSetting = findViewById(R.id.setting_layout);

		mMenuItemTextSport = (TextView) findViewById(R.id.id_tv_menu_sport);
		mMenuItemTextSleep = (TextView) findViewById(R.id.id_tv_menu_sleep);
		mMenuItemTextRank = (TextView) findViewById(R.id.id_tv_menu_rank);
		mMenuItemTextSetting = (TextView) findViewById(R.id.id_tv_menu_setting);

		mMenuItemImageSport = (ImageView) findViewById(R.id.id_iv_menu_sport);
		mMenuItemImageSleep = (ImageView) findViewById(R.id.id_iv_menu_sleep);
		mMenuItemImageRank = (ImageView) findViewById(R.id.id_iv_menu_rank);
		mMenuItemImageSetting = (ImageView) findViewById(R.id.id_iv_menu_setting);

		mMenuItemLayoutSport.setOnClickListener(this);
		mMenuItemLayoutSleep.setOnClickListener(this);
		mMenuItemLayoutRank.setOnClickListener(this);
		mMenuItemLayoutSetting.setOnClickListener(this);
	}

	public void setTabSelectionColor(int i) {
		clearTabSelection();
		switch (i) {
		case 0:
			mMenuItemTextSport.setTextColor(getResources().getColor(
					R.color.orangered));
			DrawableUtil.setImageViewColor(mMenuItemImageSport, getResources()
					.getColor(R.color.orangered));
			break;
		case 1:
			mMenuItemTextSleep.setTextColor(getResources().getColor(
					R.color.orangered));
			DrawableUtil.setImageViewColor(mMenuItemImageSleep, getResources()
					.getColor(R.color.orangered));
			break;
		}
	}

	private void setTabSelection(int i) {

		clearTabSelection();

		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		hideFragments(transaction);

		mMenuItemId = i;
		switch (i) {
		case 0:
			mMenuItemTextSport.setTextColor(getResources().getColor(
					R.color.orangered));
			DrawableUtil.setImageViewColor(mMenuItemImageSport, getResources()
					.getColor(R.color.orangered));

			if (mMainFragment == null) {
				// 如果NewsFragment为空，则创建一个并添加到界面上
				mMainFragment = new MainFragment();
				transaction.add(R.id.content, mMainFragment);
			} else {
				// 如果NewsFragment不为空，则直接将它显示出来
				mMainFragment.refreshViewPager(0);
				transaction.show(mMainFragment);
			}
			break;
		case 1:
			mMenuItemTextSleep.setTextColor(getResources().getColor(
					R.color.orangered));
			DrawableUtil.setImageViewColor(mMenuItemImageSleep, getResources()
					.getColor(R.color.orangered));

			if (mMainFragment == null) {
				// 如果NewsFragment为空，则创建一个并添加到界面上
				mMainFragment = new MainFragment();
				transaction.add(R.id.content, mMainFragment);
			} else {
				// 如果NewsFragment不为空，则直接将它显示出来
				mMainFragment.refreshViewPager(1);
				transaction.show(mMainFragment);
			}
			break;
		case 2:
			mMenuItemTextRank.setTextColor(getResources().getColor(
					R.color.orangered));
			DrawableUtil.setImageViewColor(mMenuItemImageRank, getResources()
					.getColor(R.color.orangered));

			if (mRankFragment == null) {
				// 如果NewsFragment为空，则创建一个并添加到界面上
				mRankFragment = new RankFragment();
				transaction.add(R.id.content, mRankFragment);
			} else {
				// 如果NewsFragment不为空，则直接将它显示出来
				transaction.show(mRankFragment);
			}
			break;
		case 3:
			break;
		case 4:
			mMenuItemTextSetting.setTextColor(getResources().getColor(
					R.color.orangered));
			DrawableUtil.setImageViewColor(mMenuItemImageSetting,
					getResources().getColor(R.color.orangered));

			if (mSettingFragment == null) {
				// 如果NewsFragment为空，则创建一个并添加到界面上
				mSettingFragment = new SettingFragment();
				transaction.add(R.id.content, mSettingFragment);
			} else {
				// 如果NewsFragment不为空，则直接将它显示出来
				transaction.show(mSettingFragment);
			}
			break;
		}

		transaction.commit();

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.sport_layout:
			setTabSelection(0);
			break;

		case R.id.sleep_layout:
			setTabSelection(1);
			break;

		case R.id.rank_layout:
			setTabSelection(2);
			break;

		case R.id.setting_layout:
			setTabSelection(4);
			break;
		}
	}

	private void clearTabSelection() {
		mMenuItemTextSport.setTextColor(getResources().getColor(R.color.gray));
		mMenuItemTextSleep.setTextColor(getResources().getColor(R.color.gray));
		mMenuItemTextRank.setTextColor(getResources().getColor(R.color.gray));
		mMenuItemTextSetting
				.setTextColor(getResources().getColor(R.color.gray));

		DrawableUtil.setImageViewColor(mMenuItemImageSport, getResources()
				.getColor(R.color.gray));
		DrawableUtil.setImageViewColor(mMenuItemImageSleep, getResources()
				.getColor(R.color.gray));
		DrawableUtil.setImageViewColor(mMenuItemImageRank, getResources()
				.getColor(R.color.gray));
		DrawableUtil.setImageViewColor(mMenuItemImageSetting, getResources()
				.getColor(R.color.gray));
	}

	private void hideFragments(FragmentTransaction transaction) {
		if (mMainFragment != null) {
			transaction.hide(mMainFragment);
		}
		if (mSettingFragment != null) {
			transaction.hide(mSettingFragment);
		}
		if (mRankFragment != null) {
			transaction.hide(mRankFragment);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		Log.e(TAG, "ManagerActivity onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		Log.i(TAG, "ManagerActivity onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		Log.i(TAG, "ManagerActivity onPause");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Log.i(TAG, "ManagerActivity onDestroy");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			new AlertDialog.Builder(ManagerActivity.this)
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
											ManagerActivity.this,
											CentralService.class);
									stopService(it);
									finish();
									System.exit(0);
								}
							}).show();// 在按键响应事件中显示此对话框

		}
		return true;
	}
	
}
