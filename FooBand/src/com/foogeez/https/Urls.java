package com.foogeez.https;

public class Urls {
	
	//正式的公网服务器
	//public static final String BASIC_URL = "http://113.108.221.253/geezcloud";e74f3497daed565b465f421eeaf80213  3473fbd2c6545b34b685e75e969d1a5
	public static final String BASIC_URL = "http://www.foogeez.com/geezcloud";
	//public static final String BASIC_URL = "http://113.108.221.253/geezcloud";
	//android:服务器为每个应用分配的app_key
	public static final String APP_KEY = "&apk=63825babb8528c2e784a72c674599dd3";
	public static final String APP_KEY2 = "?apk=63825babb8528c2e784a72c674599dd3";
	public static final String mapk = "63825babb8528c2e784a72c674599dd3";
	//20151012新修改的
	public static final String APP_KEY_NEW = "&apk=4bfa73a5417b7b6992cc8d7e5bdbe10f";
	
	//隐私声明页
	public static final String REG_SIGN = BASIC_URL + "/util/app/privacy.html" + APP_KEY2 + "&lang=%d";
	//户手册页
	public static final String COMMON_QUESTION = BASIC_URL + "/util/app/manual.html" + APP_KEY2 + "&lang=%d";
	//检查更新页
	public static final String ABOUT_SOFT = BASIC_URL + "/util/app/update.html" + APP_KEY2 + "&lang=%d";
	//检查版本更新
	public static final String UPDATA_SOFT = BASIC_URL + "/util/app/update"+ APP_KEY2 + "&lang=%d";
	
	//用户登录
	public static final String USER_LOGIN = BASIC_URL + "/user/signin?account=%s&password=%s"+ APP_KEY;
	//用户退出
	public static final String USER_LOGOUT = BASIC_URL + "/user/logout?sid=%s"+ APP_KEY;
	//获取短信验证码
	public static final String USER_VERICODE = BASIC_URL + "/user/password/retrieve?account=%s" + APP_KEY;
	//找回密码
//	public static final String USER_PASSWORD = BASIC_URL + "/user/password/retrieve?account=%s&securityCode=%s" + APP_KEY;
	public static final String USER_PASSWORD = BASIC_URL + "/user/password/retrieveNew?account=%s" + APP_KEY_NEW;
	//注册阶段账号实时校验
	public static final String USER_CHECK = BASIC_URL + "/user/check?account=%s" + APP_KEY;
	//创建新账号
	public static final String USER_REGISTER = BASIC_URL + "/user/signup?account=%s&password=%s&lang=%d" + APP_KEY;

	//提交新密码
	public static final String USER_NEWPWD = BASIC_URL + "/user/update/password?content=%s&sid=%s" + APP_KEY;
	//提交用户性别
	public static final String USERINFO_SEX = BASIC_URL + "/user/update/gender?content=%s&sid=%s" + APP_KEY;
	//提交用户生日
	public static final String USERINFO_BIRTHDAY = BASIC_URL + "/user/update/birthday?content=%s&sid=%s" + APP_KEY;
	//提交用户身高
	public static final String USERINFO_HEIGHT = BASIC_URL + "/user/update/height?content=%s&sid=%s" + APP_KEY;
	//提交用户体重
	public static final String USERINFO_WEIGHT = BASIC_URL + "/user/update/weight?content=%s&sid=%s" + APP_KEY;
	//提交用户昵称
	public static final String USERINFO_NICKNAME = BASIC_URL + "/user/update/nickname?content=%s&sid=%s" + APP_KEY;
	//提交用户名
	public static final String USERINFO_FIRSTNAME = BASIC_URL + "/user/update/firstname?content=%s&sid=%s" + APP_KEY;
	//提交用户姓
	public static final String USERINFO_LASTNAME = BASIC_URL + "/user/update/lastname?content=%s&sid=%s" + APP_KEY;
	//获取用户信息
	public static final String USERINFO_INFO = BASIC_URL + "/user/%s" + APP_KEY2;
	//提交clientid
	public static final String USERINFO_CLIENTID = BASIC_URL + "/user/update/clientid?content=%s&sid=%s" + APP_KEY;
	//获取用户头像
	public static final String USER_HEAD = BASIC_URL + "/user/avatar/%s" + APP_KEY2;
	//提交用户界面语言
	public static final String USERINFO_LAN = BASIC_URL + "/user/update/lang?content=%d&sid=%s" + APP_KEY;
	//验证手机号（获取验证码）
	public static final String USERINFO_GETPHONE = BASIC_URL + "/user/validate/mobile?sid=%s" + APP_KEY;
	//验证手机号（提交验证码）
	public static final String USERINFO_SENDPHONE = BASIC_URL + "/user/validate/mobile?sid=%s&activationCode=%s" + APP_KEY;
	//验证邮箱地址（获取验证码）
	public static final String USERINFO_GETEMAIL = BASIC_URL + "/user/validate/email?sid=%s" + APP_KEY;
	//验证邮箱地址（提交验证码）
	public static final String USERINFO_SENDEMAIL = BASIC_URL + "/user/validate/email?sid=%s&activationCode=%s" + APP_KEY;
	
	//获取标准时间 get
	public static final String STDTIME = BASIC_URL + "/util/stdtime" + APP_KEY2;
	//获取时区列表 get
	public static final String TIMEZONE = BASIC_URL + "/util/timezones" + APP_KEY2;
	
	//上传设备信息
	public static final String DEVICE_URL = BASIC_URL + "/device/connect" + APP_KEY2;
	public static final String DEVICE_INFO = BASIC_URL + "/device/connect?sid=%s&serialno=%s&devicename=%s&hwversion=%s&fwversion=%s&uuid=%s&mac=%s" + APP_KEY;
	//修改设备信息
	public static final String DEVICE_NAME = BASIC_URL + "/device/update?sid=%s&did=%d&devicename=%s" + APP_KEY;
	//解除设备关联
	public static final String DEVICE_DEL = BASIC_URL + "/device/disconnect?sid=%s&did=%d" + APP_KEY;
	//获取设备新固件
	public static final String DEVICE_FIRMWARE = BASIC_URL + "/device/firmware?sid=%s&lang=%d" + APP_KEY;
	
	
	public static final String UPLOAD_SPORT_DATA = BASIC_URL + "/data/sports?sid=%s&did=%d&sportsData=%s" + APP_KEY;
	//获取运动数据最后上传utc时间
	public static final String GETLASSPORTTUTCTIME = BASIC_URL + "/data/sports/timestamp?sid=%s&did=%d" + APP_KEY;
	//获取运动数据（天）
	public static final String GETDAYDATA = BASIC_URL + "/data/sports/day?sid=%s&did=%d&date=%s&lang=%d" + APP_KEY;
	//获取运动数据（周）
	public static final String GETWEEKDATA = BASIC_URL + "/data/sports/week?sid=%s&did=%d&fromDate=%s&toDate=%s&lang=%d" + APP_KEY;
	//获取运动数据（月）
	public static final String GETMONTHDATA = BASIC_URL + "/data/sports/month?sid=%s&did=%d&date=%s" + APP_KEY;
	//获取运动最高纪录
	public static final String GETRECORD = BASIC_URL + "/data/sports/record?sid=%s&did=%d" + APP_KEY;
	//获取运动总计
	public static final String GETTOTAL = BASIC_URL + "/data/sports/total?sid=%s&did=%d" + APP_KEY;
	//获取运动数据（24小时）
	public static final String GETSPORTDAY = BASIC_URL + "/data/sports/hours?sid=%s&did=%d&date=%s" + APP_KEY;
	//获取运动数据（31天）
	public static final String GETSPORTMONTH = BASIC_URL + "/data/sports/monthday?sid=%s&did=%d&date=%s&lang=%d" + APP_KEY;
	//获取运动步数排行榜（当天）
	public static final String GETTOTALLIST = BASIC_URL + "/data/ranking/step/day?sid=%s" + APP_KEY;
	//获取用户运动步数的排名（当天）
	public static final String GETUSERLIST = BASIC_URL + "/data/position/step/day?sid=%s" + APP_KEY;
	//获取好友运动步数排行榜（当天）
	public static final String GETFRIENDLIST = BASIC_URL + "/sns/ranking/step/day?sid=%s" + APP_KEY;
	
	//获取睡眠数据最后上传utc时间
	public static final String GETLASTSLEEPUTCTIME = BASIC_URL + "/data/sleep/timestamp?sid=%s&did=%d" + APP_KEY;
	//上传睡眠数据
	public static final String UPLOAD_SLEEPDATA = BASIC_URL + "/data/sleep?sid=%s&did=%d&sleepData=%s" + APP_KEY;
	//获取睡眠数据（次）
	public static final String GETSLEEPDATA = BASIC_URL + "/data/sleep/duration?sid=%s&did=%d&page=%d" + APP_KEY;
	//获取睡眠数据（详细）
	public static final String GETSLEEPDATADAY = BASIC_URL + "/data/sleep/data?sid=%s&did=%d&date=%s" + APP_KEY;
	//获取睡眠数据（日）
	public static final String GETSLEEPDAY = BASIC_URL + "/data/sleep/date?sid=%s&did=%d&date=%s&lang=%d" + APP_KEY;
	
	//获取数据最后上传utc时间
	public static final String GETLASTDATAUTCTIME = BASIC_URL + "/data/timestamp?sid=%s&did=%d" + APP_KEY;
	
	//上传设备数据（同时包括运动和睡眠）
	public static final String UPLOAD_DATA = BASIC_URL + "/data/upload" + APP_KEY2;
	//上传设备数据（同时包括运动和睡眠）
	public static final String UPLOAD_MYDATA = BASIC_URL + "/data/upload?sid=%s&did=%d&deviceData=%s" + APP_KEY;
	
	//提交意见反馈
	public static final String UPLOAD_FEEDBACK = BASIC_URL + "/util/feedback?sid=%s&content=%s" + APP_KEY;
	
	//获取好友列表
	public static final String GET_FRIENDLIST = BASIC_URL + "/sns/friends?sid=%s" + APP_KEY;
	//搜索好友（获取陌生人资料）
	public static final String SEARCH_FRIEND = BASIC_URL + "/sns/search?sid=%s&keyword=%s" + APP_KEY;
	//发送好友请求
	public static final String SEND_REQUEST = BASIC_URL + "/sns/request/init?sid=%s&userId=%d&rtypeId=%d&remark=%s" + APP_KEY;
	//获取好友请求列表
	public static final String GET_REQUESTFRIENDLIST = BASIC_URL + "/sns/friends/pending?sid=%s" + APP_KEY;
	//处理好友请求
	public static final String SET_REQUEST = BASIC_URL + "/sns/request/manage?sid=%s&userId=%d&status=%d&remark=%s" + APP_KEY;
	//解除好友关系
	public static final String DISSMISS_FRIEND = BASIC_URL + "/sns/friend/dismiss?sid=%s&userId=%d" + APP_KEY;
	//获取用户活力值
	public static final String GET_VITALITY = BASIC_URL + "/sns/interact/vitality/%s" + APP_KEY2;
	//发送交互信息（炸弹或示爱）
	public static final String SEND_BOMB = BASIC_URL + "/sns/interact?sid=%s&userId=%d&scodeId=%d" + APP_KEY;
	//读取交互信息（炸弹或示爱）
	public static final String GET_BOMB = BASIC_URL + "/sns/interact/%d?sid=%s&interactId=%d" + APP_KEY;
	//发送推送clientid
	public static final String SEND_ID = BASIC_URL + "/user/update/clientid" + APP_KEY2;
	//修改好友备注名
	public static final String SET_REMARK = BASIC_URL + "/sns/friend/remark?sid=%s&userId=%d&remark=%s" + APP_KEY;
	//获取用户交互数量总计
	public static final String GET_TOTAL = BASIC_URL + "/sns/interact/sum/%s" + APP_KEY2;
	
	public static final String GET_FRIEND_IMAGE = BASIC_URL + "/sns/friend/avatar?sid=%s&avatar=%s&ts=%s" + APP_KEY;
	
	public static final String POST_MY_IMAGE = BASIC_URL + "/user/avatar";
	
}

