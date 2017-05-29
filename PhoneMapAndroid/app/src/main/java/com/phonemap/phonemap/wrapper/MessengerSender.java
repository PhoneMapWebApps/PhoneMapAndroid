package com.phonemap.phonemap.wrapper;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class MessengerSender {
    private static final String LOG_TAG = "MessengerSender";
    private final Message message;

    public MessengerSender(int what) {
        message = Message.obtain(null, what);
    }

    public MessengerSender setData(Bundle bundle) {
        message.setData(bundle);
        return this;
    }

    public MessengerSender replyTo(Messenger replyTo) {
        message.replyTo = replyTo;
        return this;
    }

    public void send(Messenger messenger) {
        try {
            messenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Failed to send message");
        }
    }
}
