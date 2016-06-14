package com.foogeez.notification.model;

import android.app.Notification;

public interface MessageParser {
	public MessageBean parse(Notification notification);
}
