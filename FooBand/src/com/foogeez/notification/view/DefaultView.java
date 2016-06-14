package com.foogeez.notification.view;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.app.Notification;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.foogeez.notification.Util;
import com.foogeez.notification.model.MessageBean;
import com.foogeez.notification.model.MessageParser;

public class DefaultView implements MessageParser {
    private static final String TAG = DefaultView.class.getName();

    // private static final int ID_TITLE = 16908310; // com.android.internal.R.id.title
    private static int ID_TITLE;
    private static int ID_TEXT;

    static {

        ID_TITLE = Util.getResourceIdForName("com.android.internal.R.id.title");
        if (ID_TITLE == -1) {
            ID_TITLE = 16908310; // com.android.internal.R.id.title
        }

        // com.android.internal.R.id.text
        ID_TEXT = Util.getResourceIdForName("com.android.internal.R.id.text");
        if (ID_TEXT == -1) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                ID_TEXT = 16908352;
            else
                ID_TEXT = 16908358; // on 4.0 / 14 and above

            // TODO: Extend further API version resource IDs
        }
    }

    @Override
    public MessageBean parse(Notification notification) {
        MessageBean result = new MessageBean();

        try {
            RemoteViews views = notification.contentView;
            Class<?> rvClass = views.getClass();

            Field field = null;
            try {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    field = rvClass.getDeclaredField("mActions");
                } else {
                    // class android.app.Notification$BuilderRemoteViews
                    field = rvClass.getSuperclass().getDeclaredField("mActions"); // Notication BuilderRemoteViews
                }
                field.setAccessible(true);
            } catch (NoSuchFieldException e1) {
                e1.printStackTrace();

                return null;
            }

            @SuppressWarnings("unchecked")
            ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field
                    .get(views);

            for (Parcelable action : actions) {
                try {
                    // create parcel from action
                    Parcel parcel = Parcel.obtain();
                    action.writeToParcel(parcel, 0);
                    parcel.setDataPosition(0);

                    // check if is 2 / ReflectionAction
                    int tag = parcel.readInt();
                    if (tag != 2)
                        continue;

                    int viewId = parcel.readInt();

                    String methodName = parcel.readString();
                    if (methodName == null || !methodName.equals("setText")) {
                        Log.w(TAG, "# Not setText: " + methodName);
                        continue;
                    }

                    // should be 10 / Character Sequence, here
                    parcel.readInt();

                    // Store the actual string
                    CharSequence value = TextUtils.CHAR_SEQUENCE_CREATOR
                            .createFromParcel(parcel);

                    Log.d(TAG, "viewId is " + viewId);
                    Log.d(TAG, "Found value: " + value.toString());

                    if (viewId == ID_TITLE)
                        result.sender = value.toString();
                    else if (viewId == ID_TEXT)
                        result.message = value.toString();

                    parcel.recycle();
                } catch (Exception e) {
                    Log.e(TAG, "Error accessing object!", e);
                }
            }

            if (result.sender == null && result.message == null)
                return null;

            return result;
        } catch (Exception e) {
            Log.e(TAG, "Could not access mActions!", e);

            return null;
        }
    }
}
