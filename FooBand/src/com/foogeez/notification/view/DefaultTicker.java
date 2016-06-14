package com.foogeez.notification.view;

import android.app.Notification;
import android.text.TextUtils;

import com.foogeez.notification.model.MessageBean;
import com.foogeez.notification.model.MessageParser;

public class DefaultTicker implements MessageParser {
    @Override
    public MessageBean parse(Notification notification) {
        MessageBean result = new MessageBean();

        String ticker = notification.tickerText.toString();
        if (TextUtils.isEmpty(ticker)) {
            return new DefaultView().parse(notification);
        }

        int indexDelimiter = ticker.indexOf(':');

        if (indexDelimiter == -1)
            return new DefaultView().parse(notification);

        result.sender = ticker.substring(0, indexDelimiter);
        result.message = ticker.substring(indexDelimiter + 2);

        return result;
    }
}
