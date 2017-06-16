package com.phonemap.phonemap.services;


import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

public class SocketServiceConnection implements ServiceConnection {
    private static final String LOG_TAG = "SocketServiceConnection";
    private JSRunner runner;

    public SocketServiceConnection(JSRunner runner) {
        this.runner = runner;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        runner.setMessengerSender(new MessengerSender(new Messenger(service)));
        runner.requestNewSubtask();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.e(LOG_TAG, "SocketConnectionManager stopped unexpectedly.");
        runner.stopSelf();
    }
}
