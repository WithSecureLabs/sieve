package com.withsecure.example.sieve.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class CryptoService extends Service {
    final class MessageHandler extends Handler {
        public MessageHandler(Looper looper) {
            super(looper);
        }

        @Override  // android.os.Handler
        public void handleMessage(Message msg) {
            CryptoService.this.responseHandler = msg.replyTo;
            Bundle recievedBundle = (Bundle)msg.obj;
            switch(msg.what) {
                case MSG_ENCRYPT: {
                    String recievedKey = recievedBundle.getString(KEY);
                    String recievedString = recievedBundle.getString(STRING);
                    recievedBundle.putByteArray(RESULT, CryptoService.this.encrypt(recievedKey, recievedString));
                    this.sendResponseMessage(CryptoServiceConnector.MSG_RESULT, CryptoServiceConnector.TYPE_ENCRYPT, msg.arg1, recievedBundle);
                    super.handleMessage(msg);
                    return;
                }
                case MSG_DECRYPT: {
                    String recievedKey = recievedBundle.getString(KEY);
                    byte[] recievedData = recievedBundle.getByteArray(AuthService.PASSWORD);
                    recievedBundle.putString(RESULT, CryptoService.this.decrypt(recievedKey, recievedData));
                    this.sendResponseMessage(CryptoServiceConnector.MSG_RESULT, CryptoServiceConnector.TYPE_DECRYPT, msg.arg1, recievedBundle);
                    super.handleMessage(msg);
                    return;
                }
                default: {
                    Log.e(TAG, "Error: unrecognized command: " + msg.what);
                    this.sendUnrecognizedMessage();
                    super.handleMessage(msg);
                }
            }
        }

        private void sendResponseMessage(int command, int arg1, int arg2, Bundle bundle) {
            try {
                Message message0 = Message.obtain(null, command, arg1, arg2);
                if(bundle != null) {
                    message0.setData(bundle);
                }

                CryptoService.this.responseHandler.send(message0);
            }
            catch(RemoteException e) {
                Log.e(TAG, "Unable to send message: " + command);
            }
        }

        private void sendUnrecognizedMessage() {
            Message message0 = Message.obtain(null, AuthServiceConnector.MSG_ERROR, null);
            try {
                CryptoService.this.responseHandler.send(message0);
            }
            catch(RemoteException remoteException0) {
            }
        }
    }

    public static final String KEY = "com.withsecure.example.sieve.KEY";
    public static final int MSG_DECRYPT = 0x34A4;
    public static final int MSG_ENCRYPT = 0xD7C;
    public static final String RESULT = "com.withsecure.example.sieve.RESULT";
    public static final String STRING = "com.withsecure.example.sieve.STRING";
    private static final String TAG = "m_CryptoService";
    private Messenger responseHandler;
    private Messenger serviceHandler;

    static {
        System.loadLibrary("sieve");
    }

    private String decrypt(String key, byte[] string) {
        try {
            return this.runNDKdecrypt(key, string);
        }
        catch(Exception e) {
            Log.e(TAG, "ERROR: Error during decrytion: " + e.getMessage());
            return null;
        }
    }

    private byte[] encrypt(String key, String string) {
        try {
            return this.runNDKencrypt(key, string);
        }
        catch(Exception e) {
            Log.e(TAG, "ERROR: Error during encrytion: " + e.getMessage());
            return null;
        }
    }

    @Override  // android.app.Service
    public IBinder onBind(Intent arg0) {
        return this.serviceHandler.getBinder();
    }

    @Override  // android.app.Service
    public void onCreate() {
        HandlerThread thread = new HandlerThread(TAG, 10);
        thread.start();
        Looper serviceLooper = thread.getLooper();
        this.serviceHandler = new Messenger(new MessageHandler(serviceLooper));
    }

    private native String runNDKdecrypt(String arg1, byte[] arg2);

    private native byte[] runNDKencrypt(String arg1, String arg2);
}

