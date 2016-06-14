package com.foogeez.activity;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.commons.io.FileUtils;

import android.app.Application;
import android.os.Environment;
import android.os.Handler;

/** 
 * 全局处理异常. 
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类 来接管程序,并记录 发送错误报告.. 註冊方式
 * CrashHandler crashHandler = CrashHandler.getInstance(); //注册crashHandler
 * crashHandler.init(getApplicationContext()); //发送以前没发送的报告(可选)
 * crashHandler.sendPreviousReportsToServer(); 
 */  
public class CrashHandler implements UncaughtExceptionHandler {

    private final Application mApplication;
    private Handler mUIHandler;
    private Thread mUiThread;

    public CrashHandler(Application app) {
        mApplication = app;
        mUIHandler = new Handler();
        mUiThread = Thread.currentThread();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            cause = cause.getCause();
        }

        writeCrashInfoToFile(e);

        if (Thread.currentThread() != mUiThread) {
            mUIHandler.post(new Runnable() {

                @Override
                public void run() {
                    mApplication.onTerminate();
                }
            });
        } else {
            mApplication.onTerminate();
        }
    }

    private void writeCrashInfoToFile(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        Throwable cause = t.getCause();
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }
        String crashInfo = sw.toString();
        pw.close();

        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File file = mApplication.getApplicationContext().getExternalCacheDir();
                if (file != null) {
                    file = FileUtils.getFile(file, "crash");
                    file.mkdirs();
                    FileUtils.writeStringToFile(FileUtils.getFile(file, "crash.log"), crashInfo);
                }
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }
}
