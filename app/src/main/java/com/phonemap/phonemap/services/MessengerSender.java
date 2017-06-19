package com.phonemap.phonemap.services;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

@SuppressWarnings("WeakerAccess")
public class MessengerSender {
    private static final String LOG_TAG = "MessengerSender";
    private Message message;
    private Messenger messenger;

    public MessengerSender(Messenger recipient) {
        this.messenger = recipient;
    }

    public MessengerSender setMessage(int messageCode) {
        this.message = Message.obtain(null, messageCode);
        return this;
    }

    public MessengerSender setMessage(Message message) {
        this.message = message;
        return this;
    }

    public MessengerSender setData(Bundle bundle) {
        message.setData(bundle);
        return this;
    }

    public MessengerSender sendRepliesTo(Messenger recipientOfReplies) {
        message.replyTo = recipientOfReplies;
        return this;
    }

    public void send() {
        if (messenger == null) {
            Log.e(LOG_TAG, "Should not be sending when there is no one to send to");
            return;
        }

        try {
            messenger.send(message);
        } catch (RemoteException e) {
            // Todo: Handle this properly. Probably ought to be handled in the layer above, ie, this method should THROW this exception.
            e.printStackTrace();
            Log.e(LOG_TAG, "Failed to send message");
        }
    }
}
