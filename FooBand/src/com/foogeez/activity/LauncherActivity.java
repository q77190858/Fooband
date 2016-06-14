package com.foogeez.activity;

import java.util.Locale;

import com.foogeez.configuration.LocalStorage;
import com.foogeez.dialog.ConfigRspDialog;
import com.foogeez.fooband.R;
import com.grdn.util.DrawableUtil;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class LauncherActivity extends Activity {
    private final static String TAG = LauncherActivity.class.getSimpleName();
    
    private ImageView mImageViewWelcome = null;
	private LocalStorage mLocalStorage = null;
	private TextView mTxtVersionNumber;
	private TextView mThanks;
//	private ImageView mImageViewWelcomeNew;
	
    @SuppressLint("ShowToast")
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_launcher);
        
        Log.i(TAG, "Robin--------onCreate");
        
        /** 设置是否对日志信息进行加密, 默认false(不加密). */
        AnalyticsConfig.enableEncrypt(true);
        
        mLocalStorage = new LocalStorage(LauncherActivity.this);
        
        mImageViewWelcome = (ImageView) findViewById(R.id.id_iv_welcome_l);
        mTxtVersionNumber = (TextView) findViewById(R.id.versionNumber);
        mThanks = (TextView) findViewById(R.id.id_tv_thanks_alot);
//        mImageViewWelcomeNew = (ImageView) findViewById(R.id.id_iv_welcome_launcher);
        
        Log.i(TAG, "Robin--------改变图片颜色");
        
        /**Robin 20150919 --> 判断为汉语时，启动画面*/
//        String locale = Locale.getDefault().getLanguage();
//        if (locale.equals("zh")) {
//        	mImageViewWelcomeNew.setVisibility(View.GONE);
//        	mImageViewWelcome.setVisibility(View.VISIBLE);
//        	mTxtVersionNumber.setVisibility(View.VISIBLE);
//        	mThanks.setVisibility(View.VISIBLE);
//        }
//        
        Log.i(TAG, "Robin--------判断为汉语时，启动画面");
        
        //Display the current version number
        mTxtVersionNumber.setText(getAppVersionName());
//        PackageManager pm = getPackageManager();
//        try 
//        {
//            PackageInfo pi = pm.getPackageInfo("com.foogeez.fooband", 0);
//            TextView versionNumber = (TextView) findViewById(R.id.versionNumber);
//            versionNumber.setText("Version " + pi.versionName);// + " 内测版");
//        }
//        catch (NameNotFoundException e) 
//        {
//            e.printStackTrace();
//        }

        boolean supportedBLE = (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE));
        
        if( android.os.Build.VERSION.SDK_INT < 18 ) {
        	new ConfigRspDialog(LauncherActivity.this, getResources().getString(R.string.error_android_version_too_low)).show(5000);
        }
        else if( !supportedBLE ) {
        	new ConfigRspDialog(LauncherActivity.this, getResources().getString(R.string.error_bluetooth_version_too_low)).show(5000);
        }
        
        if( (android.os.Build.VERSION.SDK_INT < 18)||(!supportedBLE) ) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
		        	finish();
				}
            }, 3000);
        	return;
        } else {
	        new Handler().postDelayed(new Runnable() {
	            public void run() {
//	            	Log.e(TAG, "account: " + mLocalStorage.getAccount());
//	            	Log.e(TAG, "passwrd: " + mLocalStorage.getPassword());
	            	
	            	//取得从上一个Activity当中传递过来的Intent对象  
	                Intent intent = getIntent();  
	                //从Intent当中根据key取得value  
	                String value = intent.getStringExtra("testIntent");  
//	                Log.i(TAG, "Robin---------进行判断--String value" + value);
	            	if ("123".equals(value)) {
	            		Intent intentSet = new Intent(LauncherActivity.this, SettingUsrInfoActBasic.class);
		                startActivity(intentSet);
		        		overridePendingTransition( R.anim.translate_slide_in_right, R.anim.translate_slide_out_left );
		                finish();
					}else{
						if( mLocalStorage.hasAnyAccount() && mLocalStorage.hasPasswordByAccount() ) {
			                Intent mainIntent = new Intent(LauncherActivity.this, ManagerActivity.class);
//			                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//别忘了这行，否则退出不起作用
			                startActivity(mainIntent);
			        		overridePendingTransition( R.anim.translate_slide_in_right, R.anim.translate_slide_out_left );
			                finish();
		            	}else {
			                Intent mainIntent = new Intent(LauncherActivity.this, LoginActivity.class);
			                startActivity(mainIntent);
			        		overridePendingTransition( R.anim.translate_slide_in_right, R.anim.translate_slide_out_left );
			                finish();
		            	}
					}
	                
	            	
	            }
	        }, 1000); //2900 for release
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
    
  
    /** Name:Robin  Time:20150928  Function: 友盟session的统计  */
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
