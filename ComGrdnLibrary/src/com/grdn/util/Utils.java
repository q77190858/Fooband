package com.grdn.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

@SuppressLint("SimpleDateFormat")
public class Utils {

    private final static String TAG = Utils.class.getSimpleName();
    
    public static final int BIG_ENDIUM = 0;
    public static final int LITTLE_ENDIUM = 1;
	
	
	public static Uri fileToUri( String name ) {
		return fileToUri(new File(name));
	}
	 
	public static Uri fileToUri( File file ) {
		return Uri.fromFile(file);
	}
    
    public static String bytesToHexString(byte[] src){  
        StringBuilder stringBuilder = new StringBuilder("");  
        if (src == null || src.length <= 0) {  
            return null;  
        }  
        for (int i = 0; i < src.length; i++) {  
            int v = src[i] & 0xFF;  
            String hv = Integer.toHexString(v);  
            if (hv.length() < 2) {  
                stringBuilder.append(0);  
            }  
            stringBuilder.append(hv);  
        }  
        return stringBuilder.toString();  
    }
    
    private static byte charToByte(char c) {  
        return (byte) "0123456789ABCDEF".indexOf(c);  
    }  
    
    @SuppressLint("DefaultLocale")
	public static byte[] hexStringToBytes(String hexString) {  
        if (hexString == null || hexString.equals("")) {  
            return null;  
        }  
        hexString = hexString.toUpperCase();  
        int length = hexString.length() / 2;  
        char[] hexChars = hexString.toCharArray();  
        byte[] d = new byte[length];  
        for (int i = 0; i < length; i++) {  
            int pos = i * 2;  
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));  
        }  
        return d;  
    }
	
	
	public static String inputStreamToString (InputStream in) {   
        StringBuffer out = new StringBuffer();   
        byte[]  b = new byte[1024];   
        int n;  
        
        try {
	        while ((n = in.read(b))!= -1){   
	            out.append(new String(b,0,n));   
	        }
        }
    	catch (Exception e) {
			e.printStackTrace();
		}
        
        //Log.i("String的长度", new Integer(out.length()).toString());  
        return  out.toString();   
    }
	
    public static String downloadFile2String( String dstPath, String URL ) {
    	String urlDownload = URL;									// 要下载的文件路径
    	String dirName = dstPath + "/";								// 获得存储卡路径，构成 保存文件的目标路径
    	File dir = new File(dirName);
    	if(!dir.exists()) {
    		dir.mkdir();
    	}
    	
    	String newFilename = urlDownload.substring(urlDownload.lastIndexOf("/")+1, urlDownload.indexOf("?"));
    	newFilename = dirName + newFilename;
    	
    	//Log.e("CentralService", "FILE NAME = " + newFilename);
    	
    	File file = new File(newFilename);
    	if(file.exists()) {											// 如果目标文件已经存在，则删除。产生覆盖旧文件的效果
    	    file.delete();
    	}
    	
        StringBuffer out = new StringBuffer();   
    	
    	try {
            URL url = new URL(urlDownload);    						// 构造URL   
            URLConnection con = url.openConnection();				// 打开连接   
            int contentLength = con.getContentLength(); 			// 获得文件的长度
            System.out.println("长度 :"+contentLength);
            InputStream is = con.getInputStream();  				// 输入流   
            
            int len;												// 读取到的数据长度
            byte[] bs = new byte[1024];    							// 1K的数据缓冲   
            OutputStream os = new FileOutputStream(newFilename);	// 输出的文件流   
            while ((len = is.read(bs)) != -1) {   					// 开始读取   
                os.write(bs, 0, len);   
                out.append(new String(bs,0,len)); 
            }
            
            os.close();  
            is.close();												// 完毕，关闭所有链接   
		} 
    	catch (Exception e) {
			e.printStackTrace();
			Log.e("Utils.downloadFile2String", "DOWNLOAD ERR = " + e.toString());
			return null;
		}
    	
    	return  out.toString();  
    }
    
    //URL: ROM_IMAGE
    public static String downloadFileAndCaculateMd5( String dstPath, String URL ) {
    	String urlDownload = URL;													// 要下载的文件路径
    	String dirName = dstPath + "/"; 											// 获得存储卡路径，构成 保存文件的目标路径
    	File dir = new File(dirName);
    	if(!dir.exists()) {
    		dir.mkdir();
    	}
    	
    	String newFilename = urlDownload.substring(urlDownload.lastIndexOf("/")+1, urlDownload.indexOf("?"));
    	newFilename = dirName + newFilename;
    	
    	//Log.e("CentralService", "FILE NAME = " + newFilename);
    	
    	File file = new File(newFilename);
    	
    	if(file.exists()) {															// 如果目标文件已经存在，则删除。产生覆盖旧文件的效果
    	    file.delete();
    	}
    	
    	MessageDigest digest = null;
    	
    	try {
            digest = MessageDigest.getInstance("MD5");
            URL url = new URL(urlDownload);   										// 构造URL   
            URLConnection con = url.openConnection(); 								// 打开连接   
            int contentLength = con.getContentLength(); 							// 获得文件的长度
            System.out.println("长度 :"+contentLength);    //app大小
            InputStream is = con.getInputStream();									// 输入流   
            
            int len;   																// 读取到的数据长度
            byte[] bs = new byte[1024];												// 1K的数据缓冲   
            OutputStream os = new FileOutputStream(newFilename);    				// 输出的文件流   
            while ((len = is.read(bs)) != -1) {   									// 开始读取   
                os.write(bs, 0, len);   
                digest.update(bs, 0, len);
            }
            os.close();  
            is.close(); 															// 完毕，关闭所有链接   
		} 
    	catch (Exception e) {
			e.printStackTrace();
			Log.e("Utils.downloadFileAndCaculateMd5", "DOWNLOAD ERR = " + e.toString());
			return null;
		}
    	
    	//BigInteger bigInt = new BigInteger(1, digest.digest());
    	//return bigInt.toString(16);
    	return bytesToHexString(digest.digest());
    }
    
    // ???
    public static short crc16_compute(byte[] p_data, int size, short[] p_crc) {
        int i;
        short crc = (short) ((p_crc == null) ? 0xffff : p_crc[0]);

        for (i = 0; i < size; i++) {
            crc  = (short) ((crc >> 8)|(crc << 8));
            crc ^= p_data[i];
            crc ^= (byte)(crc&0xff) >> 4;
            crc ^= (crc << 8) << 4;
            crc ^= ((crc & 0xff) << 4) << 1;
        }
        return crc;
    }
    
    public static int getUtc() {
    	Calendar gc = GregorianCalendar.getInstance();  
    	System.out.println("gc.getTime():"+gc.getTime());  
    	return (int)(gc.getTimeInMillis()/1000);
    }
    
    public static String
    addVersionDot( String version ) {
    	int len = version.length();
    	char[] s = version.toCharArray();
    	char[] result = new char[len*2-1];
    	for( int i = 0; i < len; i++ ) {
    		result[i*2] = s[i];
    	}
    	for( int i = 0; i < len-1; i++ ) {
    		result[i*2+1] = '.';
    	}
    	return new String(result);
    }
    
    public static String
    utc2DateTime(int utcInSeconds) {
    	return new SimpleDateFormat("yyyy-MM-dd E HH:mm").format(new Date((long)utcInSeconds*1000));
    }
    
    public static String
    utc2Birthday(int utcInSeconds) {
    	return new SimpleDateFormat("yyyy-MM-dd").format(new Date((long)utcInSeconds*1000));
    }
    
    public static int
    birthday2Utc(String date) {
    	int rslt = 0;
    	try {
    		rslt = (int)(new SimpleDateFormat("yyyy-MM-dd").parse(date).getTime()/1000);
		} 
    	catch (ParseException e) {
			e.printStackTrace();
		}
    	return rslt;
    }
    
    public static String
    getTodayDate() {
    	Calendar gc = Calendar.getInstance();  
    	return new SimpleDateFormat("yyyy-MM-dd").format(gc.getTime());
    }
	
    public static int 
    getLanguageCode() {
     	String lan = Locale.getDefault().toString();
        
		if(lan.equals("zh_CN")){
			return 1;
		}
		else if(lan.equals("zh_TW")){
			return 2;
		}
		else {
			return 3;
		}
    }
    
	public static String utc2DateTime(String format, Date datetime) {
    	return new SimpleDateFormat(format).format(datetime);
    }
	
	public static long TodayZeroUtc() {
		Calendar calendar = Calendar.getInstance(); 
		int Year = calendar.get(Calendar.YEAR);
		int Month = calendar.get(Calendar.MONTH);
		int DayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		
		calendar.clear(); 
		calendar.set(Calendar.YEAR, Year); 			//year年
		calendar.set(Calendar.MONTH, Month);		//Calendar对象默认一月为0,month月      
		calendar.set(Calendar.DAY_OF_MONTH, DayOfMonth);
		
		return calendar.getTimeInMillis();
	}
	
    public static int bytes2Int(byte[] src, int offset, int size, final int type) {  
	    int value = 0;
	    
	    if( (size <= 0)||(size>=5) ) {
	    	Log.e(TAG, "size error:" + size);
	    	return 0;
	    }
	    
	    if( (type != BIG_ENDIUM)&&(type != LITTLE_ENDIUM) ) {
	    	Log.e(TAG, "type error:" + type);
	    	return 0;
	    }
	    
	    if( type == BIG_ENDIUM ) {
		    for( int i = 0; i < size; i++ ) {
			    value |= (int)((src[offset+i]&0xFF)<<i*8);
		    }
	    }
	    else {
		    for( int i = 0; i < size; i++ ) {
			    value |= (int)((src[offset+i]&0xFF)<<(size-1-i)*8);
		    }
	    }
	    
	    return value;
	}
	
	public static byte[] int2Bytes(int src, final int type) {
		byte[] result = new byte[4];
		
		if( type == BIG_ENDIUM ) {
			for( int i = 0; i < 4; i++ ) {
				result[i] = (byte)((src>>i*8)&0xFF);
			}
		}
		else {
			for( int i = 0; i < 4; i++ ) {
				result[i] = (byte)((src>>((4-1-i)*8))&0xFF);
			}
		}
		
		return result;
	}
	
	public static byte[] long2Bytes(long num) {  
	    byte[] byteNum = new byte[8];  
	    for (int ix = 0; ix < 8; ++ix) {  
	        int offset = 64 - (ix + 1) * 8;  
	        byteNum[ix] = (byte) ((num >> offset) & 0xff);  
	    }  
	    return byteNum;  
	}  
	  
	public static long bytes2Long(byte[] byteNum) {  
	    long num = 0;  
	    for (int ix = 0; ix < 8; ++ix) {  
	        num <<= 8;  
	        num |= (byteNum[ix] & 0xff);  
	    }  
	    return num;  
	}
	
	public static boolean compareBytes(byte[] dst, byte[] src) {
		if( src.length != dst.length ) return false;
		for( int i = 0; i < src.length; i++ ) {
			if( src[i] != dst[i] ) {
				return false;
			}
		}
		return true;
	}
	
	
	/****
	 * 
	 * 
	 * MD5
	 * 
	 */
	private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5',
		'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String toHexString(byte[] b) { // String to byte
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
			sb.append(HEX_DIGITS[b[i] & 0x0f]);
		}
		return sb.toString();
	}

	public static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
	
			return toHexString(messageDigest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	
		return "";
	}

	public static String md5sum(String filename) {  
	    InputStream fis;  
	    byte[] buffer = new byte[1024];  
	    int numRead = 0;  
	    MessageDigest md5;  
	    try{  
	        fis = new FileInputStream(filename);  
	        md5 = MessageDigest.getInstance("MD5");  
	        while((numRead=fis.read(buffer)) > 0) {  
	            md5.update(buffer,0,numRead);  
	        }  
	        fis.close();  
	        return toHexString(md5.digest());     
	    } catch (Exception e) {  
	        System.out.println("error");  
	        return null;  
	    }  
	}
	
	public static boolean isEmail(String email) {
		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);

		return m.matches();
	}
	
	public static boolean isPhone(String phoneNumber) {
		boolean isValid = false;
		String expression = "((^(13|15|17|18)[0-9]{9}$)|(^0[1,2]{1}\\d{1}-?\\d{8}$)|(^0[3-9] {1}\\d{2}-?\\d{7,8}$)|(^0[1,2]{1}\\d{1}-?\\d{8}-(\\d{1,4})$)|(^0[3-9]{1}\\d{2}-? \\d{7,8}-(\\d{1,4})$))";
		CharSequence inputStr = phoneNumber;
		Pattern pattern = Pattern.compile(expression);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()) {
		isValid = true;
		}

		return isValid;
	}
	
	/**
	 * Name:Robin  Time:20150919  Function:正则表达式可以用，但是速度似乎有点慢
	 */
	public static boolean checkPassword(String code){
		Pattern pattern = Pattern.compile("[0-9a-zA-Z\u4E00-\u9FA5\\! ” § $ % & / ( ) ! ? # + * = [ ] { } ä Ä ü Ü ö Ö]+");  
	    Matcher matcher = pattern.matcher(code);
	    if(code.length()>=6 && code.length()<=16){
	    	if (!matcher.matches() ) {
		    	//showLongToast("code error");
		    	return false;  
		    } 
	    	else {  
		    	Log.i("checkcode", "密码正确");
		    	return true;  
		    }
	    }
	    else{
	    	return false;
	    }				
	}
	
	public static boolean checkRePassword(String code, String recode){
		if(!(TextUtils.isEmpty(recode))){
			if( (code.length() < 6)||(code.length() > 16) ) return false;
			if(code.equals(recode)){
				Log.i("checkrecode", "再次密码确认");
				return true;
			}
			else{
				Log.i("checkrecode", "密码不相同");
				return false;
			}
		}
		else{
			return false;
		}				
	}
	
	/**
	uint16_t crc16_compute(const uint8_t * p_data, uint32_t size, const uint16_t * p_crc)
	{
	    uint32_t i;
	    uint16_t crc = (p_crc == NULL) ? 0xffff : *p_crc;

	    for (i = 0; i < size; i++)
	    {
	        crc  = (unsigned char)(crc >> 8) | (crc << 8);
	        crc ^= p_data[i];
	        crc ^= (unsigned char)(crc & 0xff) >> 4;
	        crc ^= (crc << 8) << 4;
	        crc ^= ((crc & 0xff) << 4) << 1;
	    }

	    return crc;
	}
	/**/
	
	public static int computeCRC16(InputStream stream, byte[] initCRC) {
		int crc = 0;
		int size = 0;
		
		if( initCRC == null ) {
			crc = 0xffff;
		}
		else {
			crc = (initCRC[1]<<8) + initCRC[0];
		}
		
		try {
			size = stream.available();
		} 
		catch (IOException e1) {
			e1.printStackTrace();
		}
		
		byte[] buffer = new byte[size];
		
		try {
			stream.read(buffer);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		for( int i = 0; i < size; i++ ) {
			crc =  ((crc>>8)&0x00ff)|((crc<<8)&0xffff);
			crc ^= ((int)buffer[i])&0x00ff;
			crc ^= ((crc&0x00ff)>>4);
			crc ^= (((crc<<8)&0xffff)<<4)&0xffff;
			crc ^= ((((crc&0xff)<<4)&0xffff)<<1)&0xffff;
		}
		
		return crc&0xffff;
	}
	
	public static void saveBitmap(Bitmap bm, String Absdir, String picName) {
    	File dir = new File(Absdir);
    	if(!dir.exists()) {
    		dir.mkdir();
    	}
		
		File f = new File(Absdir, picName);
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    
}
