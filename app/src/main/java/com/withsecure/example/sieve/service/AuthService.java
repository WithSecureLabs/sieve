package com.withsecure.example.sieve.service;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat.Builder;

import com.withsecure.example.sieve.provider.DBContentProvider;
import com.withsecure.example.sieve.activity.MainLoginActivity;
import com.withsecure.example.sieve.database.PWTable;
import com.withsecure.example.sieve.R;

public class AuthService extends Service {
    final class MessageHandler extends Handler {
        public MessageHandler(Looper looper) {
            super(looper);
        }

        @Override  // android.os.Handler
        public void handleMessage(Message msg) {
            int returnVal;
            int responseCode;
            AuthService.this.responseHandler = msg.replyTo;
            Bundle returnBundle = (Bundle)msg.obj;
            switch(msg.what) {
                case MSG_FIRST_LAUNCH: {
                    if(!AuthService.this.checkKeyExists()) {
                        responseCode = AuthServiceConnector.TYPE_NO_KEY_NO_PIN;
                    }
                    else if(AuthService.this.checkPinExists()) {
                        responseCode = AuthServiceConnector.TYPE_HAS_KEY_HAS_PIN;
                    }
                    else {
                        responseCode = AuthServiceConnector.TYPE_HAS_KEY_NO_PIN;
                    }

                    this.sendResponseMessage(3, responseCode, 1, null);
                    return;
                }
                case MSG_CHECK: {
                    switch(msg.arg1) {
                        case TYPE_KEY: {
                            responseCode = AuthServiceConnector.TYPE_KEY;
                            String recievedString = returnBundle.getString(PASSWORD);
                            if(AuthService.this.verifyKey(recievedString)) {
                                AuthService.this.showNotification();
                                returnVal = 0;
                            }
                            else {
                                returnVal = MSG_SAY_HELLO;
                            }

                            this.sendResponseMessage(5, responseCode, returnVal, returnBundle);
                            return;
                        }
                        case TYPE_PIN: {
                            break;
                        }
                        default: {
                            this.sendUnrecognisedMessage();
                            return;
                        }
                    }

                    responseCode = AuthServiceConnector.TYPE_PIN;
                    String recievedString = returnBundle.getString(PIN);
                    if(AuthService.this.verifyPin(recievedString)) {
                        returnBundle = new Bundle();
                        returnBundle.putString(PASSWORD, AuthService.this.getKey());
                        returnVal = 0;
                    }
                    else {
                        returnVal = MSG_SAY_HELLO;
                    }

                    this.sendResponseMessage(5, responseCode, returnVal, returnBundle);
                    return;
                }
                case MSG_SET: {
                    break;
                }
                default: {
                    Log.e(TAG, "Error: unrecognized command: " + msg.what);
                    this.sendUnrecognisedMessage();
                    super.handleMessage(msg);
                    return;
                }
            }

            switch(msg.arg1) {
                case TYPE_KEY: {
                    responseCode = AuthServiceConnector.TYPE_KEY;
                    String recievedString = returnBundle.getString(PASSWORD);
                    returnVal = AuthService.this.setKey(recievedString) ? 0 : 1;
                    this.sendResponseMessage(4, responseCode, returnVal, null);
                    return;
                }
                case TYPE_PIN: {
                    responseCode = AuthServiceConnector.TYPE_PIN;
                    String recievedString = returnBundle.getString(PIN);
                    returnVal = AuthService.this.setPin(recievedString) ? 0 : 1;
                    this.sendResponseMessage(4, responseCode, returnVal, null);
                    return;
                }
                default: {
                    this.sendUnrecognisedMessage();
                }
            }
        }

        private void sendResponseMessage(int command, int arg1, int arg2, Bundle bundle) {
            try {
                Message message0 = Message.obtain(null, command, arg1, arg2);
                if(bundle != null) {
                    message0.setData(bundle);
                }

                AuthService.this.responseHandler.send(message0);
            }
            catch(RemoteException e) {
                Log.e(TAG, "Unable to send message: " + command);
            }
        }

        private void sendUnrecognisedMessage() {
            try {
                Message message0 = Message.obtain(null, AuthServiceConnector.MSG_ERROR, AuthServiceConnector.TYPE_MSG_UNDEFINED, 1, null);
                AuthService.this.responseHandler.send(message0);
            }
            catch(RemoteException e) {
                Log.e(TAG, "Unable to send message");
            }
        }
    }

    static final int MSG_CHECK = 0x932; // 2534
    static final int MSG_CHECK_IF_INITALISED = 2;
    static final int MSG_FIRST_LAUNCH = 4;
    static final int MSG_SAY_HELLO = 1;
    static final int MSG_SET = 6345; // 6345
    static final int MSG_UNREGISTER = -1;
    private final int NOTIFICATION;
    public static final String PASSWORD = "com.withsecure.example.sieve.PASSWORD";
    public static final String PIN = "com.withsecure.example.sieve.PIN";
    private static final String TAG = "m_AuthService";
    static final int TYPE_KEY = 7452;
    static final int TYPE_PIN = 9234;
    private NotificationManager nManager;
    private Messenger responseHandler;
    private Messenger serviceHandler;

    public AuthService() {
        this.NOTIFICATION = R.string.app_name;
    }

    public boolean checkKeyExists() {
        return new CursorLoader(this, DBContentProvider.KEYS_URI, new String[]{PWTable.KEY_COLUMN_NAME_MAIN}, null, null, null).loadInBackground().getCount() > 0;
    }

    @SuppressLint("Range")
    public boolean checkPinExists() {
        Cursor cursor0 = new CursorLoader(this, DBContentProvider.KEYS_URI, new String[]{PWTable.KEY_COLUMN_NAME_SHORT}, null, null, null).loadInBackground();
        boolean exists = false;
        cursor0.moveToFirst();
        for(int i = 0; i < cursor0.getCount(); ++i) {
            exists = cursor0.getString(cursor0.getColumnIndex(PWTable.KEY_COLUMN_NAME_SHORT)) != null;
        }

        return exists;
    }

    @SuppressLint("Range")
    private String getKey() {
        Cursor cursor0 = new CursorLoader(this, DBContentProvider.KEYS_URI, new String[]{PWTable.KEY_COLUMN_NAME_MAIN}, null, null, null).loadInBackground();
        cursor0.moveToFirst();
        return cursor0.getString(cursor0.getColumnIndex(PWTable.KEY_COLUMN_NAME_MAIN));
    }

    @Override  // android.app.Service
    public IBinder onBind(Intent arg0) {
        return this.serviceHandler.getBinder();
    }

    @Override  // android.app.Service
    public void onCreate() {
        this.nManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        HandlerThread thread = new HandlerThread(TAG, 10);
        thread.start();
        Looper serviceLooper = thread.getLooper();
        this.serviceHandler = new Messenger(new MessageHandler(serviceLooper));
    }

    @Override  // android.app.Service
    public void onDestroy() {
        this.nManager.cancelAll();
    }

    @Override  // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    private boolean setKey(String key) {
        ContentValues out = new ContentValues();
        out.put(PWTable.KEY_COLUMN_NAME_MAIN, key);
        return this.getContentResolver().insert(DBContentProvider.KEYS_URI, out) != null;
    }

    private boolean setPin(String key) {
        ContentValues out = new ContentValues();
        out.put(PWTable.KEY_COLUMN_NAME_SHORT, key);
        return this.getContentResolver().update(DBContentProvider.KEYS_URI, out, "pin IS NULL", null) > 0;
    }

    private void showNotification() {
        Builder notificationCompat$Builder0 = new Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(this.getText(R.string.app_name)).setOngoing(true).setContentText("Click to access your passwords");
        Intent resultIntent = new Intent(this, MainLoginActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        notificationCompat$Builder0.setContentIntent(PendingIntent.getActivity(this.getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        this.nManager.notify(this.NOTIFICATION, notificationCompat$Builder0.build());
    }

    private boolean verifyKey(String key) {
        return new CursorLoader(this, DBContentProvider.KEYS_URI, new String[]{PWTable.KEY_COLUMN_NAME_MAIN}, "Password = ?", new String[]{key}, null).loadInBackground().getCount() == 1;
    }

    private boolean verifyPin(String PIN) {
        return new CursorLoader(this, DBContentProvider.KEYS_URI, new String[]{PWTable.KEY_COLUMN_NAME_SHORT}, "pin = ?", new String[]{PIN}, null).loadInBackground().getCount() == 1;
    }
}

