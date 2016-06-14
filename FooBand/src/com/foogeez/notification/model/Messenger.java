package com.foogeez.notification.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Notification;
import android.util.Log;

import com.foogeez.notification.view.DefaultPhone;
import com.foogeez.notification.view.DefaultTicker;
import com.foogeez.notification.view.DefaultView;
import com.foogeez.notification.view.SumsungNote3Phone;

public enum Messenger {
    SMS("com.android.mms", new DefaultTicker(), 0x00),
    FACEBOOK("com.facebook.orca", new DefaultView(), 0x03),
    SYKPE("com.skype.raider", new DefaultView(), 0x04),
    WECHAT("com.tencent.mm", new DefaultView(), 0x02),
//    ANDROIDPHONE("com.android.phone", new DefaultPhone()),
//    SAMSUNGNOTE3PHONE("com.android.incallui", new SumsungNote3Phone()),
//    NEXUS("com.android.dialer", new SumsungNote3Phone()),
    MOBILEQQ("com.tencent.mobileqq", new DefaultView(), 0x01);

    private static final String TAG = Messenger.class.getName();

    private String packageName;
    private MessageParser parser;
    private int dataType;

    Messenger(String packageName, MessageParser parser) {
        this.packageName = packageName.toLowerCase(Locale.ENGLISH);
        this.parser = parser;
    }

    Messenger(String packageName, MessageParser parser, int dataType) {
        this.packageName = packageName.toLowerCase(Locale.ENGLISH);
        this.parser = parser;
        this.dataType = dataType;
    }

    public static MessageBean getMessage(String packageName, Notification notification) {
        String notificationPackage = packageName.toLowerCase(
                Locale.ENGLISH);

        for (Messenger messenger : Messenger.values()) {
            if (!messenger.packageName.equals(notificationPackage))
                continue;

            Log.d(TAG, "Found matching messenger: " + messenger.packageName);

            MessageBean result = messenger.parser.parse(notification);

            // if (result == null)
            // return null;

            // if result parse null,just add base package info to satisfy our inform needs
            if (result == null) {
                result = new MessageBean();
            }

            result.dataType = messenger.dataType;
            result.app = packageName;
            result.time = new SimpleDateFormat("yyy-MM-dd HH:mm", Locale.US).format(new Date());

            return result;
        }

        return null;
    }

    public static Messenger getSourceMessenger(String pkg) {
        for (Messenger source : Messenger.values()) {
            if (source.packageName.equals(pkg)) {
                return source;
            }
        }

        return null;
    }

    public static int getDataType(String pkg) {
        for (Messenger source : Messenger.values()) {
            if (source.packageName.equals(pkg)) {
                return source.dataType;
            }
        }

        return -1;
    }

}
