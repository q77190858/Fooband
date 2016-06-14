package com.foogeez.notification.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class MessageBean implements Parcelable {
    public interface MessageListener {
        public void onMessage(MessageBean message);
    }

    public String app = null;
    public String time = null;
    public String sender = null;
    public String message = null;
    public int dataType;

    @Override
    public String toString() {
        return "Sent from " + this.sender + ": '" + this.message + "'" + " dataType=" + dataType+" app="+app;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();

        result.put("app", this.app);
        result.put("time", this.time);
        result.put("sender", this.sender);
        result.put("message", this.message);

        return result;
    }

    public static MessageBean fromJSON(JSONObject object) throws JSONException {
        MessageBean result = new MessageBean();

        result.app = object.getString("app");
        result.time = object.getString("time");
        result.sender = object.getString("sender");
        result.message = object.getString("message");

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(app);
        dest.writeString(time);
        dest.writeString(sender);
        dest.writeString(message);
        dest.writeInt(dataType);
    }

    public static final Parcelable.Creator<MessageBean> CREATOR = new Parcelable.Creator<MessageBean>()
    {
        public MessageBean createFromParcel(Parcel in)
        {
            return new MessageBean(in);
        }

        public MessageBean[] newArray(int size)
        {
            return new MessageBean[size];
        }
    };

    private MessageBean(Parcel in)
    {
        app = in.readString();
        time = in.readString();
        sender = in.readString();
        message = in.readString();
        dataType = in.readInt();
    }

    public MessageBean() {
    }
}
