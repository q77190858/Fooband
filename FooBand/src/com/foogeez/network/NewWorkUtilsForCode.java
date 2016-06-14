package com.foogeez.network;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.BitmapFactory.Options;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.foogeez.https.HttpUtils;
import com.grdn.util.Utils;

public class NewWorkUtilsForCode {
	
	  private final static int ON_PRE_EXCUTE = 0;
	    private final static int ON_EXCUTE_SUCCESS = 1;
	    private final static int ON_EXCUTE_FAILURE = 2;
	    private final static int ON_TIME_OUT = 3;

	    private boolean mDoingFlag = false;

	    private Context mContext;
	    private NetworkCallCodeback mCallback;
	    private Handler mHandler;

	    public final static int GET = 0;
	    public final static int POST = 1;

	    public NewWorkUtilsForCode(Context context, NetworkCallCodeback rsp) {
	        mContext = context;
	        mCallback = rsp;

	        mHandler = new LocalHandler(this);
	    }

	    public interface NetworkCallCodeback {
	        void OnPreExcute();

//	        void OnExcuteSuccess(JSONObject obj) throws JSONException;

//	        void OnExcuteFailure(JSONObject obj) throws JSONException;

//	        void OnTimeOut();
	    }

	    private static class LocalHandler extends Handler {
	        private WeakReference<NewWorkUtilsForCode> mContext;

	        public LocalHandler(NewWorkUtilsForCode context) {
	            this.mContext = new WeakReference<NewWorkUtilsForCode>(context);
	        }

	        @Override
	        public void handleMessage(Message msg) {
	            NewWorkUtilsForCode context = mContext.get();
	            if (context != null) {
	                switch (msg.what) {
	                case ON_PRE_EXCUTE:
	                    context.mCallback.OnPreExcute();
	                    break;
//	                case ON_EXCUTE_SUCCESS:
//	                    try {
//	                        context.mCallback.OnExcuteSuccess((JSONObject) msg.obj);
//	                    } catch (JSONException e) {
//	                        e.printStackTrace();
//	                    }
//	                    break;
//	                case ON_EXCUTE_FAILURE:
//	                    try {
//	                        context.mCallback.OnExcuteFailure((JSONObject) msg.obj);
//	                    } catch (JSONException e) {
//	                        e.printStackTrace();
//	                    }
//	                    break;
//	                case ON_TIME_OUT:
//	                    context.mCallback.OnTimeOut();
//	                    break;
	                }
	            }
	        }
	    }

	    // Urls.USER_CHECK
	    public void request(final int type, final String format, final Object... keyword) {

	        Message msg = new Message();
	        msg.what = ON_PRE_EXCUTE;
	        mHandler.sendMessage(msg);

	        mDoingFlag = true;
	        mHandler.postDelayed(new Runnable() {
	            @Override
	            public void run() {
	                if (mDoingFlag == true) {
	                    mDoingFlag = false;
	                    Message msg = new Message();
	                    msg.what = ON_TIME_OUT;
	                    mHandler.sendMessage(msg);
	                }
	            }
	        }, 10000);

	        new Thread(new Runnable() {
	            @Override
	            public void run() {
	                try {
	                    String url = String.format(format, keyword);
	                    Log.i("NewWorkUtilsForCode", ((type == POST) ? "PST" : "GET") + ":" + url);

	                    String result = null;
	                    if (type == POST) {
	                        result = HttpUtils.postByHttpClient(mContext, url);
	                    }
	                    else {
	                        result = HttpUtils.getByHttpClient(mContext, url);
	                    }

	                    JSONObject jsonObj = new JSONObject(result);
	                    mDoingFlag = false;
	                    if (jsonObj.getInt("rs") == 100) {
	                        Message msg = new Message();
	                        msg.what = ON_EXCUTE_SUCCESS;
	                        msg.obj = jsonObj;
	                        mHandler.sendMessage(msg);
	                    }
	                    else {
	                        Message msg = new Message();
	                        msg.what = ON_EXCUTE_FAILURE;
	                        msg.obj = jsonObj;
	                        mHandler.sendMessage(msg);
	                    }
	                }
	                catch (Exception e) {
	                    Log.e("NewWorkUtilsForCode", "error:" + e.toString());

	                    Message msg = new Message();
	                    msg.what = ON_EXCUTE_FAILURE;
	                    JSONObject jsonobj = new JSONObject();
	                    try {
	                        jsonobj.put("rs", 500);
	                    }
	                    catch (JSONException e1) {
	                        e1.printStackTrace();
	                    }
	                    msg.obj = jsonobj;
	                    mHandler.sendMessage(msg);

	                }

	            }
	        }).start();
	    }

	    // Urls.USER_CHECK
	    public void postDatums(final String url, final String sid, final Long did, final String datum) {
	        Message msg = new Message();
	        msg.what = ON_PRE_EXCUTE;
	        mHandler.sendMessage(msg);

	        mDoingFlag = true;
	        mHandler.postDelayed(new Runnable() {
	            @Override
	            public void run() {
	                if (mDoingFlag == true) {
	                    mDoingFlag = false;
	                    Message msg = new Message();
	                    msg.what = ON_TIME_OUT;
	                    mHandler.sendMessage(msg);
	                }
	            }
	        }, 10000);

	        new Thread(new Runnable() {
	            @Override
	            public void run() {
	                try {
	                    // String url = String.format(format, keyword);
	                    Log.i("NewWorkUtilsForCode", "PST:" + url);
	                    String result = null;
	                    result = HttpUtils.postByHttpClient(mContext, url, new BasicNameValuePair("sid", sid),
	                            new BasicNameValuePair("did", String.valueOf(did)),
	                            new BasicNameValuePair("deviceData", datum));
	                    JSONObject jsonObj = new JSONObject(result);
	                    mDoingFlag = false;
	                    if (jsonObj.getInt("rs") == 100) {
	                        Message msg = new Message();
	                        msg.what = ON_EXCUTE_SUCCESS;
	                        msg.obj = jsonObj;
	                        mHandler.sendMessage(msg);
	                    }
	                    else {
	                        Message msg = new Message();
	                        msg.what = ON_EXCUTE_FAILURE;
	                        msg.obj = jsonObj;
	                        mHandler.sendMessage(msg);
	                    }
	                }
	                catch (Exception e) {
	                    Log.e("NewWorkUtilsForCode", "error:" + e.toString());
	                }

	            }
	        }).start();
	    }

	    public void getAvatar(final String url, final String sid, final String dstpath, final String avatar, final String ts) {
	        Message msg = new Message();
	        msg.what = ON_PRE_EXCUTE;
	        mHandler.sendMessage(msg);

	        mDoingFlag = true;
	        mHandler.postDelayed(new Runnable() {
	            @Override
	            public void run() {
	                if (mDoingFlag == true) {
	                    mDoingFlag = false;
	                    Message msg = new Message();
	                    msg.what = ON_TIME_OUT;
	                    mHandler.sendMessage(msg);
	                }
	            }
	        }, 10000);

	        new Thread(new Runnable() {
	            @Override
	            public void run() {
	                try {

	                    /**
	                     * String format = String.format(url, sid); Log.i("NewWorkUtilsForCode", "GET:" + url); String result =
	                     * null; result = HttpUtils.postByHttpClient(mContext, format); ByteArrayInputStream is = new
	                     * ByteArrayInputStream(result.getBytes()); /
	                     **/
	                    final String imgUrl = String.format(url, sid, avatar, ts);
	                    URL url = new URL(imgUrl);
	                    URLConnection conn = url.openConnection();
	                    conn.setConnectTimeout(5000);
	                    conn.setReadTimeout(5000);
	                    conn.connect();
	                    InputStream is = conn.getInputStream();

	                    BitmapFactory.Options options = new Options();
	                    options.inSampleSize = 1;
	                    Bitmap bitmap = BitmapFactory.decodeStream(is, new Rect(0, 0, 0, 0), options);

	                    mDoingFlag = false;
	                    if (bitmap != null) {
	                        String absDir = dstpath;
	                        Utils.saveBitmap(bitmap, absDir, avatar);

	                        Message msg = new Message();
	                        msg.what = ON_EXCUTE_SUCCESS;
	                        JSONObject jsonobj = new JSONObject();
	                        try {
	                            jsonobj.put("rs", 100);
	                            jsonobj.put("name", avatar);
	                        }
	                        catch (JSONException e1) {
	                            e1.printStackTrace();
	                        }
	                        msg.obj = jsonobj;
	                        mHandler.sendMessage(msg);
	                    }
	                    else {
	                        Message msg = new Message();
	                        msg.what = ON_EXCUTE_FAILURE;
	                        JSONObject jsonobj = new JSONObject();
	                        try {
	                            jsonobj.put("rs", 500);
	                            jsonobj.put("name", "null.png");
	                        }
	                        catch (JSONException e1) {
	                            e1.printStackTrace();
	                        }
	                        msg.obj = jsonobj;
	                        mHandler.sendMessage(msg);
	                    }
	                }
	                catch (Exception e) {
	                    Log.e("NewWorkUtilsForCode", "error:" + e.toString());
	                }

	            }
	        }).start();
	    }

	    public void getMyAvatar(final String url, final String sid, final String dstpath, final String avatar) {
	        Message msg = new Message();
	        msg.what = ON_PRE_EXCUTE;
	        mHandler.sendMessage(msg);

	        mDoingFlag = true;
	        mHandler.postDelayed(new Runnable() {
	            @Override
	            public void run() {
	                if (mDoingFlag == true) {
	                    mDoingFlag = false;
	                    Message msg = new Message();
	                    msg.what = ON_TIME_OUT;
	                    mHandler.sendMessage(msg);
	                }
	            }
	        }, 10000);

	        new Thread(new Runnable() {
	            @Override
	            public void run() {
	                try {
	                    /**
	                     * String format = String.format(url, sid); Log.i("NewWorkUtilsForCode", "GET:" + url); String result =
	                     * null; result = HttpUtils.postByHttpClient(mContext, format); ByteArrayInputStream is = new
	                     * ByteArrayInputStream(result.getBytes()); /
	                     **/
	                    final String imgUrl = String.format(url, sid);
	                    URL url = new URL(imgUrl);
	                    URLConnection conn = url.openConnection();
	                    conn.setConnectTimeout(5000);
	                    conn.setReadTimeout(5000);
	                    conn.connect();
	                    InputStream is = conn.getInputStream();

	                    BitmapFactory.Options options = new Options();
	                    options.inSampleSize = 1;
	                    Bitmap bitmap = BitmapFactory.decodeStream(is, new Rect(0, 0, 0, 0), options);

	                    mDoingFlag = false;
	                    if (bitmap != null) {
	                        String absDir = dstpath;
	                        Utils.saveBitmap(bitmap, absDir, avatar);

	                        Message msg = new Message();
	                        msg.what = ON_EXCUTE_SUCCESS;
	                        JSONObject jsonobj = new JSONObject();
	                        try {
	                            jsonobj.put("rs", 100);
	                            jsonobj.put("name", avatar);
	                        }
	                        catch (JSONException e1) {
	                            e1.printStackTrace();
	                        }
	                        msg.obj = jsonobj;
	                        mHandler.sendMessage(msg);
	                    }
	                    else {
	                        Message msg = new Message();
	                        msg.what = ON_EXCUTE_FAILURE;
	                        JSONObject jsonobj = new JSONObject();
	                        try {
	                            jsonobj.put("rs", 500);
	                            jsonobj.put("name", "null.png");
	                        }
	                        catch (JSONException e1) {
	                            e1.printStackTrace();
	                        }
	                        msg.obj = jsonobj;
	                        mHandler.sendMessage(msg);
	                    }
	                }
	                catch (Exception e) {
	                    Log.e("NewWorkUtilsForCode", "error:" + e.toString());
	                }

	            }
	        }).start();
	    }

	    private static final String BOUNDARY = UUID.randomUUID().toString();

	    public void postMyAvatar(final String requestUrl, final File file, final String sid, final String apk) {
	        Message msg = new Message();
	        msg.what = ON_PRE_EXCUTE;
	        mHandler.sendMessage(msg);

	        mDoingFlag = true;
	        mHandler.postDelayed(new Runnable() {
	            @Override
	            public void run() {
	                if (mDoingFlag == true) {
	                    mDoingFlag = false;
	                    Message msg = new Message();
	                    msg.what = ON_TIME_OUT;
	                    mHandler.sendMessage(msg);
	                }
	            }
	        }, 10000);

	        new Thread(new Runnable() {
	            @Override
	            public void run() {
	                try {
	                    URL url = new URL(requestUrl);
	                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	                    conn.setReadTimeout(5000);
	                    conn.setConnectTimeout(5000);
	                    conn.setDoInput(true); // 允许输入流
	                    conn.setDoOutput(true); // 允许输出流
	                    conn.setUseCaches(false); // 不允许使用缓存
	                    conn.setRequestMethod("POST"); // 请求方式
	                    conn.setRequestProperty("Charset", "utf-8"); // 设置编码
	                    conn.setRequestProperty("connection", "keep-alive");
	                    conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
	                    conn.setRequestProperty("Content-Type", "multipart/form-data" + ";boundary=" + BOUNDARY);
	                    // conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

	                    /**
	                     * 当文件不为空，把文件包装并且上传
	                     */
	                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

	                    // add parameter 1;
	                    StringBuffer sb = new StringBuffer();
	                    sb.append("--").append(BOUNDARY).append("\r\n");
	                    sb.append("Content-Disposition: form-data; name=\"").append("sid").append("\"").append("\r\n")
	                            .append("\r\n");
	                    sb.append(sid).append("\r\n");
	                    dos.write(sb.toString().getBytes());

	                    // add parameter 2;
	                    sb = new StringBuffer();
	                    sb.append("--").append(BOUNDARY).append("\r\n");
	                    sb.append("Content-Disposition: form-data; name=\"").append("apk").append("\"").append("\r\n")
	                            .append("\r\n");
	                    sb.append(apk).append("\r\n");
	                    dos.write(sb.toString().getBytes());

	                    // add file stream;
	                    sb = new StringBuffer();
	                    sb.append("--").append(BOUNDARY).append("\r\n");
	                    sb.append("Content-Disposition:form-data; name=\"" + "file" + "\"; filename=\"" + file.getName()
	                            + "\"" + "\r\n");
	                    sb.append("Content-Type:image/pjpeg" + "\r\n"); // 这里配置的Content-type很重要的 ，用于服务器端辨别文件的类型的
	                    sb.append("\r\n");
	                    dos.write(sb.toString().getBytes());

	                    InputStream is = new FileInputStream(file);
	                    byte[] bytes = new byte[1024];
	                    int len = 0;
	                    int curLen = 0;
	                    while ((len = is.read(bytes)) != -1) {
	                        curLen += len;
	                        dos.write(bytes, 0, len);
	                    }
	                    is.close();

	                    dos.write("\r\n".getBytes());
	                    byte[] end_data = ("--" + BOUNDARY + "--" + "\r\n").getBytes();
	                    dos.write(end_data);
	                    dos.flush();

	                    /**
	                     * 获取响应码 200=成功 当响应成功，获取响应的流
	                     */
	                    int res = conn.getResponseCode();
	                    if (res == 200) {
	                        Message msg = new Message();
	                        msg.what = ON_EXCUTE_SUCCESS;
	                        JSONObject jsonobj = new JSONObject();
	                        try {
	                            jsonobj.put("rs", 100);
	                            jsonobj.put("filename", file.getName());
	                        }
	                        catch (JSONException e1) {
	                            e1.printStackTrace();
	                        }
	                        msg.obj = jsonobj;
	                        mHandler.sendMessage(msg);
	                        return;
	                    }
	                    else {
	                        Message msg = new Message();
	                        msg.what = ON_EXCUTE_FAILURE;
	                        JSONObject jsonobj = new JSONObject();
	                        try {
	                            jsonobj.put("rs", 500);
	                            jsonobj.put("name", "null.png");
	                        }
	                        catch (JSONException e1) {
	                            e1.printStackTrace();
	                        }
	                        msg.obj = jsonobj;
	                        mHandler.sendMessage(msg);
	                        return;
	                    }
	                }
	                catch (MalformedURLException e) {
	                    // sendMessage(UPLOAD_SERVER_ERROR_CODE,"上传失败：error=" + e.getMessage());
	                    e.printStackTrace();
	                    return;
	                }
	                catch (IOException e) {
	                    // sendMessage(UPLOAD_SERVER_ERROR_CODE,"上传失败：error=" + e.getMessage());
	                    e.printStackTrace();
	                    return;
	                }
	            }
	        }).start();
	    }


}
