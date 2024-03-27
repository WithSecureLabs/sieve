package com.withsecure.example.sieve.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class CryptoServiceConnector extends Handler implements ServiceConnection {
    class MessageHandler extends Handler {
        @Override  // android.os.Handler
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_RESULT: {
                    switch(msg.arg1) {
                        case TYPE_ENCRYPT: {
                            CryptoServiceConnector.this.activity.encryptionReturned(msg.getData().getByteArray(CryptoService.RESULT), msg.arg2);
                            return;
                        }
                        case TYPE_DECRYPT: {
                            break;
                        }
                        default: {
                            return;
                        }
                    }

                    CryptoServiceConnector.this.activity.decryptionReturned(msg.getData().getString(CryptoService.RESULT), msg.arg2);
                    return;
                }
                case AuthServiceConnector.MSG_ERROR: {
                    CryptoServiceConnector.this.activity.sendFailed();
                    return;
                }
                default: {
                }
            }
        }
    }

    public interface ResponseListener {
        void connected();

        void decryptionReturned(String arg1, int arg2);

        void encryptionReturned(byte[] arg1, int arg2);

        void sendFailed();
    }

    static final int MSG_RESULT = 9;
    private static final String TAG = "m_CryptServiceConnector";
    static final int TYPE_DECRYPT = 92;
    static final int TYPE_ENCRYPT = 91;
    ResponseListener activity;
    private boolean bound;
    private Messenger responseHandler;
    private Messenger serviceMessenger;

    public CryptoServiceConnector(ResponseListener activity) {
        this.activity = activity;
    }

    @Override  // android.content.ServiceConnection
    public void onServiceConnected(ComponentName className, IBinder service) {
        this.serviceMessenger = new Messenger(service);
        this.responseHandler = new Messenger(new MessageHandler());
        this.bound = true;
        this.activity.connected();
    }

    @Override  // android.content.ServiceConnection
    public void onServiceDisconnected(ComponentName className) {
        this.activity.sendFailed();
        this.bound = false;
    }

    public void sendForDecryption(String key, byte[] string, int code) {
        Bundle data = new Bundle();
        data.putString(CryptoService.KEY, key);
        data.putByteArray(AuthService.PASSWORD, string);
        this.sendToServer(Message.obtain(null, CryptoService.MSG_DECRYPT, code, 0, data));
    }

    public void sendForEncryption(String key, String password, int code) {
        Bundle data = new Bundle();
        data.putString(CryptoService.KEY, key);
        data.putString(CryptoService.STRING, password);
        this.sendToServer(Message.obtain(null, CryptoService.MSG_ENCRYPT, code, 0, data));
    }

    private void sendToServer(Message msg) {
        if(this.bound) {
            try {
                msg.replyTo = this.responseHandler;
                this.serviceMessenger.send(msg);
            }
            catch(RemoteException e) {
                Log.e(TAG, "Unable to send message to Service");
                this.activity.sendFailed();
                this.bound = false;
            }

            return;
        }

        Log.e(TAG, "ERROR: We are not bound to Crypto!");
        this.activity.sendFailed();
    }
}

