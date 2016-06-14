package com.foogeez.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.foogeez.configuration.LocalStorage;
import com.foogeez.fooband.R;
import com.foogeez.https.Urls;
import com.foogeez.network.NetworkUtils;
import com.foogeez.network.NetworkUtils.NetworkCallback;
import com.grdn.pulltorefresh.library.PullToRefreshBase;
import com.grdn.pulltorefresh.library.PullToRefreshListView;
import com.grdn.pulltorefresh.library.PullToRefreshBase.Mode;
import com.grdn.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.facebook.controller.UMFacebookHandler;
import com.umeng.socialize.facebook.media.FaceBookShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RankFragment extends Fragment {
	private final static String TAG = RankFragment.class.getSimpleName();

	private ListView mListView;
	private CustomAdapter mAdapter;
	private int mScrollState;
	private View mFooter;

	private TextView id = null;
	private TextView name = null;
	private TextView rank = null;
	private TextView step = null;
	private TextView distance = null;
	private ImageView avatar = null;
	private TextView nameMy = null;
	private TextView rankMy = null;
	private TextView stepMy = null;

	JSONArray mRankList = null;
	private LocalStorage mLocalStorage = null;

	private ImageView mImageViewBtSync = null;
	private PullToRefreshListView refreshableView;

	/** Name:Robin Time:20150925 Function:发送请求数据的等待过程 */
	private ProgressDialog progressDialog = null;
	private ImageButton share;
	// 首先在您的Activity中添加如下成员变量
	final UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.share");

	Map<String, SHARE_MEDIA> mPlatformsMap = new HashMap<String, SHARE_MEDIA>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "----------RankFragment --- onCreate");
		
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, "---------RankFragment --- onCreateView");
		View RankView = inflater.inflate(R.layout.activity_rank,container, false);
		
		mImageViewBtSync = (ImageView) RankView.findViewById(R.id.id_iv_btn_rank_sync);
		// 设置分享面板上显示的平台
		share = (ImageButton) RankView.findViewById(R.id.img_btn_share_rank);
		/**Robin 20151029 --> 判断为汉语、韩语时，分享图标的可见性*/
        String locale = Locale.getDefault().getLanguage();
        if (locale.equals("zh") || locale.equals("ko")) {
        	share.setVisibility(ImageView.GONE);
		} 			
		return RankView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		Log.i(TAG, "---------RankFragment --- onActivityCreated----");
		
		initPullRefreshView();
		
		mLocalStorage = new LocalStorage(getActivity());
		Log.i(TAG, "SID:" + mLocalStorage.getSID());
		Log.i(TAG, "SNR:" + mLocalStorage.getActivatedDeviceSrln());
		Log.i(TAG, "NME:" + mLocalStorage.getActivatedDeviceName());
		Log.i(TAG, "HVR:" + mLocalStorage.getActivatedDeviceHwvr());
		Log.i(TAG, "FVR:" + mLocalStorage.getActivatedDeviceFwvr());
		Log.i(TAG, "MAC:" + mLocalStorage.getActivatedDeviceAddr());
		
		mListView = refreshableView.getRefreshableView();
		mAdapter = new CustomAdapter();
		mListView.setAdapter(mAdapter);
		
		refreshDID();
		updateTotalRankList();
		refreshableView.setRefreshing();
		
		mImageViewBtSync.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshableView.setRefreshing();
			}
		});
		
		share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addCustomPlatforms();
				showCustomUI(true);
				// mController.openShare(getActivity(), false);
			}
		});
		initPlatformMap();
		
	}
	
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			mAdapter.count = msg.what;// mRankList.length(); //增加Item数量
			mAdapter.notifyDataSetChanged(); // 通知数据集变化

			refreshableView.onRefreshComplete();
		}
	};


	@Override
	public void onStart() {
		super.onStart();
		
		Log.i(TAG, "----------RankFragment --- onStart");
	}

	private void initPullRefreshView() {
		refreshableView = (PullToRefreshListView) getActivity().findViewById(
				R.id.id_lst_ranklist_content);
		refreshableView.setOnDragListener(new OnDragListener() {
			@Override
			public boolean onDrag(View v, DragEvent event) {
				Log.e(TAG, "---------->  onDrag");
				return false;
			}
		});

		refreshableView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				Log.e(TAG, "initPullRefreshView --- onRefresh");
				if (isNetworkAvailable(getActivity())) {
					updateTotalRankList();
				}else {
					refreshableView.onRefreshComplete();
					Toast toast = Toast.makeText(getActivity(), R.string.no_network_state_notice, Toast.LENGTH_SHORT);
//					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			}
		});

		refreshableView.setMode(Mode.PULL_FROM_START);
	}


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

	private void updateTotalRankList() {
		new NetworkUtils(getActivity(), new NetworkCallback() {
			@Override
			public void OnPreExcute() {
				Log.e(TAG, "onPreExcute----updateTotalRankList");												
				progressDialog = ProgressDialog.show(getActivity(), "",
						getResources().getString(R.string.string_loading_data),
						true);
				//点击ProgressDialog以外的区域--返回键 --》 消失
				progressDialog.setCancelable(true);
			}

			@Override
			public void OnExcuteSuccess(JSONObject obj) throws JSONException {
				Log.e(TAG, "------OnExcuteSuccess:" + obj.toString());

				JSONObject nRankUser = null;
				if (obj.getString("userRanking").length() <= 0) {
					Log.e(TAG, "USER RANKING IS NULL!");
					nRankUser = new JSONObject();
					nRankUser.put("displayName", mLocalStorage
							.restoreUsrInfoConfig().getNickName());
					nRankUser.put("userId", mLocalStorage
							.restoreUsrInfoConfig().getId());
					nRankUser.put("rank", 0);
					nRankUser.put("step", 0);
					nRankUser.put("distance", 0);
				} else {
					nRankUser = obj.getJSONObject("userRanking");
				}

				JSONArray nRankList = obj.getJSONArray("rankingStepDay");
				mRankList = new JSONArray();
				mRankList.put(0, nRankUser);
				for (int i = 0; i < ((nRankList.length() < 50) ? nRankList
						.length() : 50); i++) {
					mRankList.put(i + 1, nRankList.get(i));

					Log.e(TAG, "AVATAR:"
							+ nRankList.getJSONObject(i).getString("avatar"));

					String imageName = nRankList.getJSONObject(i).getString(
							"avatar");
					String imageDate = nRankList.getJSONObject(i).getString(
							"avatarTS");

					if (imageName.length() > 0) {
						Log.e(TAG, "REQUEST AVATAR[" + i + "]:" + imageName);
						try {
							updateUsrImage(
									URLEncoder.encode(imageName, "UTF-8"),
									URLEncoder.encode(imageDate, "UTF-8"));
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}
				}
				handler.sendEmptyMessage(((nRankList.length() < 50) ? nRankList
						.length() : 50) + 1);
				progressDialog.dismiss();
			}

			@Override
			public void OnExcuteFailure(JSONObject obj) {
				refreshableView.onRefreshComplete();
				Log.e(TAG, "OnExcuteFailure:" + obj.toString());
				progressDialog.dismiss();
			}

			@Override
			public void OnTimeOut() {
				refreshableView.onRefreshComplete();
				Log.e(TAG, "OnTimeOut");
				progressDialog.dismiss();
			}
		}).request(NetworkUtils.GET, Urls.GETTOTALLIST, mLocalStorage.getSID());
			
	}

	private void updateUsrImage(String avatar, String ts) {

		String savepath = android.os.Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/fooband/avatar/";
		new NetworkUtils(getActivity(), new NetworkCallback() {
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
		}).getAvatar(Urls.GET_FRIEND_IMAGE, mLocalStorage.getSID(), savepath,
				avatar, ts);
	}
	

	private class CustomAdapter extends BaseAdapter {
		// 初始列表项数量
		int count = 0;

		@Override
		public int getCount() {
			return count;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			RelativeLayout result = (RelativeLayout) convertView;
			if (mRankList.length() <= 0)
				return result;

			// 动态创建TextView添加早ListView中
			if (position == 0) {
				result = (RelativeLayout) getActivity().getLayoutInflater()
						.inflate(R.layout.rank_list_header, null);
				rankMy = (TextView) (result.findViewById(R.id.id_tv_my_rank));
				nameMy = (TextView) (result.findViewById(R.id.id_tv_my_name));
				// id = (TextView)(result.findViewById(R.id.id_tv_my_id));
				stepMy = (TextView) (result.findViewById(R.id.id_tv_my_step));

				try {
					String nameString = "N/A";
					try {
						nameString = URLDecoder.decode(
								mRankList.getJSONObject(position).getString(
										"displayName"), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					nameMy.setText(nameString
							+ "(ID:"
							+ mRankList.getJSONObject(position)
									.getInt("userId") + ")");

					int rankNumber = mRankList.getJSONObject(position).getInt(
							"rank");
					if (rankNumber >= 50) {
						rankNumber = 0;
					}
					String format = getString(R.string.string_my_rank);
					format = String.format(format, rankNumber);
					rankMy.setText("" + format);

					// id.setText("(ID:" +
					// mRankList.getJSONObject(position).getInt("userId")+")");

					String distanceUnit = getString(R.string.string_distance_unit);
					float dist = ((float) mRankList.getJSONObject(position)
							.getLong("distance")) / 100;
					DecimalFormat decimalFormat = new DecimalFormat("0.00"); // 构造方法的字符格式这里如果小数不足2位,会以0补足.
					String p = decimalFormat.format(dist);// format 返回的是字符串

					String stepUnit = getString(R.string.string_step_unit);
					stepMy.setText(mRankList.getJSONObject(position).getInt(
							"step")
							+ stepUnit + "/" + p + distanceUnit);

				} catch (JSONException e) {
					e.printStackTrace();
				}

			} else {
				result = (RelativeLayout) getActivity().getLayoutInflater()
						.inflate(R.layout.rank_list_item, null);
				rank = (TextView) (result
						.findViewById(R.id.list_usr_rank_number));
				name = (TextView) (result.findViewById(R.id.list_usr_name));
				id = (TextView) (result.findViewById(R.id.list_usr_id));
				step = (TextView) (result.findViewById(R.id.list_usr_step));
				distance = (TextView) (result
						.findViewById(R.id.list_usr_distance));
				avatar = (ImageView) (result.findViewById(R.id.list_usr_image));

				try {
					String nameString = "N/A";
					try {
						nameString = URLDecoder.decode(
								mRankList.getJSONObject(position).getString(
										"displayName"), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					int rankNum = mRankList.getJSONObject(position).getInt(
							"rank");
					if (rankNum == 1) {
						id.setTextColor(getResources().getColor(
								R.color.color_copper_rank));
						rank.setTextColor(getResources().getColor(
								R.color.color_copper_rank));
						name.setTextColor(getResources().getColor(
								R.color.color_copper_rank));
						step.setTextColor(getResources().getColor(
								R.color.color_copper_rank));
						distance.setTextColor(getResources().getColor(
								R.color.color_copper_rank));
					} else if (rankNum == 2) {
						id.setTextColor(getResources().getColor(
								R.color.color_gold_rank));
						rank.setTextColor(getResources().getColor(
								R.color.color_gold_rank));
						name.setTextColor(getResources().getColor(
								R.color.color_gold_rank));
						step.setTextColor(getResources().getColor(
								R.color.color_gold_rank));
						distance.setTextColor(getResources().getColor(
								R.color.color_gold_rank));
					} else if (rankNum == 3) {
						id.setTextColor(getResources().getColor(
								R.color.color_silver_rank));
						rank.setTextColor(getResources().getColor(
								R.color.color_silver_rank));
						name.setTextColor(getResources().getColor(
								R.color.color_silver_rank));
						step.setTextColor(getResources().getColor(
								R.color.color_silver_rank));
						distance.setTextColor(getResources().getColor(
								R.color.color_silver_rank));
					}

					name.setText(nameString);
					rank.setText("" + rankNum);
					id.setText("ID:"
							+ mRankList.getJSONObject(position)
									.getInt("userId"));

					String distanceUnit = getString(R.string.string_distance_unit);
					float dist = ((float) mRankList.getJSONObject(position)
							.getLong("distance")) / 100;
					DecimalFormat decimalFormat = new DecimalFormat("0.00"); // 构造方法的字符格式这里如果小数不足2位,会以0补足.
					String p = decimalFormat.format(dist);// format 返回的是字符串
					distance.setText(p + distanceUnit);

					String stepUnit = getString(R.string.string_step_unit);
					step.setText(mRankList.getJSONObject(position).getInt(
							"step")
							+ stepUnit);

					String imageName = mRankList.getJSONObject(position)
							.getString("avatar");

					if (imageName.length() > 0) {
						String savepath = android.os.Environment
								.getExternalStorageDirectory()
								.getAbsolutePath()
								+ "/fooband/avatar/";
						Bitmap bm = BitmapFactory.decodeFile(savepath
								+ imageName, null);
						avatar.setImageBitmap(bm);
					} else {
						avatar.setImageResource(R.drawable.rank_unkown);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return result;
		}
	}

	/**
	 * 添加所有的平台
	 */
	private void addCustomPlatforms() {
		// 添加微信平台
//		addWXPlatform();
		// 添加QQ平台
		addQQQZonePlatform();
		// 添加facebook平台
		addFacebook();
		// 添加新浪SSO授权
		mController.getConfig().setSsoHandler(new SinaSsoHandler());

		UMImage localImage = new UMImage(getActivity(), myShot(getActivity()));
		SinaShareContent sinaContent = new SinaShareContent();
		// 设置分享到腾讯微博的文字内容
		sinaContent.setTitle("新浪微博");
		sinaContent.setShareContent("来自友盟社会化组件（SDK）让移动应用快速整合社交分享功能，腾讯微博");
		sinaContent.setShareImage(localImage);
		// 设置分享到腾讯微博的多媒体内容
		mController.setShareMedia(sinaContent);
		// mController.registerListener(new SnsPostListener() {
		//
		// @Override
		// public void onStart() {
		// Toast.makeText(getActivity(), R.string.share_start, 0).show();
		// }
		//
		// @Override
		// public void onComplete(SHARE_MEDIA platform, int eCode,
		// SocializeEntity entity) {
		// // TODO Auto-generated method stub
		// // Toast.makeText(getActivity(), "code : " + eCode, 0).show();
		// Toast.makeText(getActivity(), R.string.share_complete, 0)
		// .show();
		// }
		// });

		// mController.getConfig().setPlatforms(SHARE_MEDIA.FACEBOOK);
		// mController.getConfig().setPlatformOrder(SHARE_MEDIA.FACEBOOK);
		// mController.openShare(getActivity(), false);

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
	private void addQQQZonePlatform() {
		String appId = "801556848";
		String appKey = "5bc9c2b47e38eb5ab50107193e8dce1a";

		// 添加QZone平台
		QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(getActivity(),
				appId, appKey);
		qZoneSsoHandler.addToSocialSDK();
		QZoneShareContent qzone = new QZoneShareContent();
		// 设置分享文字
		qzone.setShareContent("-++-----++来自友盟社会化组件（SDK）让移动应用快速整合社交分享功能 -- QZone");
		// 设置点击消息的跳转URL
		qzone.setTargetUrl("http://www.foogeez.com/");
		// 设置分享内容的标题
		qzone.setTitle(getString(R.string.share_title_rank));
		// 设置分享图片Qzone分享只支持图文形式分享，并且点击图文消息会跳转到预设的链接，图文分享只展示缩略图，**无法查看大图**
		qzone.setShareImage(new UMImage(getActivity(), myShot(getActivity())));
		mController.setShareMedia(qzone);
	}

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
		final CharSequence[] items = { "Facebook" };//,"微信","微信-朋友圈","新浪微博","QQ","QQ空间"
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
		mPlatformsMap.put("Facebook", SHARE_MEDIA.FACEBOOK);
//		mPlatformsMap.put("微信", SHARE_MEDIA.WEIXIN);
//		mPlatformsMap.put("微信-朋友圈", SHARE_MEDIA.WEIXIN_CIRCLE);
//		mPlatformsMap.put("新浪微博", SHARE_MEDIA.SINA);
//		 mPlatformsMap.put("QQ", SHARE_MEDIA.QQ);
//		 mPlatformsMap.put("QQ空间", SHARE_MEDIA.QZONE);
		

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
				Log.i(TAG, "分享失败 : error code : "+ stCode);
			}
		}
	};

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

	// /////////////////////////////////////////////////////Fragment////
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		Log.i(TAG, "--------onDestroyView--------");
		// 如果onSaveInstanceState没被调用，这里也可以保存数据

	}
	// /////////////////////////////////////////////////////Fragment////
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
	private String mPageName;

	@Override
	public void onResume() {
		Log.i(TAG, "------------ > onResume");
		super.onResume();
		MobclickAgent.onPageStart(mPageName);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(mPageName);
	}

}
