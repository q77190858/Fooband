package com.foogeez.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.foogeez.fooband.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseHelper {
    private final static String TAG = DatabaseHelper.class.getSimpleName();
    
	//得到SD卡路径
	private final Context mContext;
	private final String DATABASE_PATH; 
	private final String DATABASE_DIR;
	private final String DATABASE_FILENAME;	//数据库名
	
	public DatabaseHelper(Context context, final String userdir, final String filename ) {
		DATABASE_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/fooband";
		DATABASE_FILENAME = filename;
		DATABASE_DIR = userdir;
		mContext = context;
	}
	
	public String getDatabasePath() {
		return DATABASE_PATH;
	}
	
	//得到操作数据库的对象
	public  SQLiteDatabase openDatabase() {
		try {
			//boolean b = false;
			String databaseFilename = DATABASE_PATH + "/" + DATABASE_DIR + "/" + DATABASE_FILENAME; //得到数据库的完整路径名
			
			//将数据库文件从资源文件放到合适地方（资源文件也就是数据库文件放在项目的res下的raw目录中）
			//将数据库文件复制到SD卡中   
			File path = new File(DATABASE_PATH);
			if( !path.exists() ) {
				//b = 
				path.mkdir();
			}
			
			File dir = new File(DATABASE_PATH + "/" + DATABASE_DIR);
			if( !dir.exists() ) {
				//b = 
				dir.mkdir();
			}
			
			//判断是否存在该文件
			if( !(new File(databaseFilename)).exists() ) {     
				InputStream is = mContext.getResources().openRawResource(R.raw.jokebook);	//不存在得到数据库输入流对象
				FileOutputStream fos = new FileOutputStream(databaseFilename); 				//创建输出流
				
				//将数据输出
				byte[] buffer = new byte[8192];
				int count = 0;
				while( (count = is.read(buffer)) > 0 ) {
					fos.write(buffer, 0, count);
				}
				//关闭资源
				fos.close();
				is.close();
			}
			
			//得到SQLDatabase对象
			SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFilename, null);
			Log.e(TAG, "openOrCreateDatabase: " + databaseFilename);
			return database;
	  }
	  catch (Exception e) {
		  System.out.println(e.getMessage());
	  }
	  return null;
	}

}
