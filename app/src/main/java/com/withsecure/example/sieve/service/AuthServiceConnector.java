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

public class AuthServiceConnector extends Handler implements ServiceConnection {
    class MessageHandler extends Handler {
        @Override  // android.os.Handler
        public void handleMessage(Message msg) {
            boolean z = true;
            switch(msg.what) {
                case MSG_FIRST_LAUNCH: {
                    AuthServiceConnector.this.activity.firstLaunchResult(msg.arg1);
                    return;
                }
                case MSG_SET: {
                    switch(msg.arg1) {
                        case TYPE_PIN: {
                            ResponseListener authServiceConnector$ResponseListener0 = AuthServiceConnector.this.activity;
                            if(msg.arg2 != 0) {
                                z = false;
                            }

                            authServiceConnector$ResponseListener0.setPinResult(z);
                            return;
                        }
                        case TYPE_KEY: {
                            break;
                        }
                        default: {
                            return;
                        }
                    }

                    ResponseListener authServiceConnector$ResponseListener1 = AuthServiceConnector.this.activity;
                    if(msg.arg2 != 0) {
                        z = false;
                    }

                    authServiceConnector$ResponseListener1.setKeyResult(z);
                    return;
                }
                case MSG_CHECK: {
                    switch(msg.arg1) {
                        case TYPE_PIN: {
                            ResponseListener authServiceConnector$ResponseListener2 = AuthServiceConnector.this.activity;
                            if(msg.arg2 != 0) {
                                z = false;
                            }

                            authServiceConnector$ResponseListener2.checkPinResult(z);
                            return;
                        }
                        case TYPE_KEY: {
                            break;
                        }
                        default: {
                            return;
                        }
                    }

                    ResponseListener authServiceConnector$ResponseListener3 = AuthServiceConnector.this.activity;
                    if(msg.arg2 != 0) {
                        z = false;
                    }

                    authServiceConnector$ResponseListener3.checkKeyResult(z);
                    return;
                }
                case MSG_ERROR: {
                    AuthServiceConnector.this.activity.sendFailed();
                    Log.e(TAG, "Error: Recieved unrecognised Message, what: " + msg.what + ", arg1: " + msg.arg1);
                    return;
                }
                default: {
                    Log.e(TAG, "Error: Recieved unrecognised Message, what: " + msg.what + ", arg1: " + msg.arg1);
                }
            }
        }
    }

    public interface ResponseListener {
        void checkKeyResult(boolean arg1);

        void checkPinResult(boolean arg1);

        void connected();

        void firstLaunchResult(int arg1);

        void sendFailed();

        void setKeyResult(boolean arg1);

        void setPinResult(boolean arg1);
    }

    static final int MSG_CHECK = 5;
    static final int MSG_ERROR = 0x1B207;
    static final int MSG_FIRST_LAUNCH = 3;
    static final int MSG_SET = 4;
    private static final String TAG = "m_AuthServiceConnector";
    public static final int TYPE_HAS_KEY_HAS_PIN = 0x1F;
    public static final int TYPE_HAS_KEY_NO_PIN = 0x20;
    static final int TYPE_KEY = 42;
    static final int TYPE_MSG_UNDEFINED = 0x1DD6E;
    public static final int TYPE_NO_KEY_NO_PIN = 33;
    static final int TYPE_PIN = 41;
    ResponseListener activity;
    private boolean bound;
    private Messenger responseHandler;
    private Messenger serviceMessenger;

    public AuthServiceConnector(ResponseListener activity) {
        this.activity = activity;
    }

    public void checkFirstLaunch() {
        this.sendToServer(Message.obtain(null, MSG_SET));
    }

    public void checkKey(String key) {
        Bundle data = new Bundle();
        data.putString(AuthService.PASSWORD, key);
        this.sendToServer(Message.obtain(null, AuthService.MSG_CHECK, 7452, 0, data));
    }

    public void checkPin(String pin) {
        Bundle data = new Bundle();
        data.putString(AuthService.PIN, pin);
        this.sendToServer(Message.obtain(null, AuthService.MSG_CHECK, 9234, 0, data));
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
        this.bound = false;
        this.activity.sendFailed();
    }

    private void sendToServer(Message msg) {
        if(this.bound) {
            try {
                msg.replyTo = this.responseHandler;
                this.serviceMessenger.send(msg);
            }
            catch(RemoteException e) {
                Log.e(TAG, "Unable to send message: " + msg.what);
                this.activity.sendFailed();
            }

            return;
        }

        Log.e(TAG, "ERROR: We are not bound to Crypto!");
    }

    public void setKey(String key) {
        Bundle data = new Bundle();
        data.putString(AuthService.PASSWORD, key);
        this.sendToServer(Message.obtain(null, 6345, 7452, 0, data));
    }

    public void setPin(String pin) {
        Bundle data = new Bundle();
        data.putString(AuthService.PIN, pin);
        this.sendToServer(Message.obtain(null, 6345, 9234, 0, data));
    }
}

