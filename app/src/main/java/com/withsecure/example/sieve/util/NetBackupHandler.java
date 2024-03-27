package com.withsecure.example.sieve.util;

import android.os.AsyncTask;
import android.util.Log;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NetBackupHandler {
    class NetBackup extends AsyncTask {
        private NetBackup() {
        }

        NetBackup(NetBackup netBackupHandler$NetBackup0) {
        }

        @Override  // android.os.AsyncTask
        protected Object doInBackground(Object[] arr_object) {
            return this.doInBackground(((String[])arr_object));
        }

        protected Void doInBackground(String[] ins) {
            OutputStream os = null;
            InputStream is = null;
            byte[] buffer = new byte[0x1000];
            ByteBuffer byteBuffer0 = ByteBuffer.allocate(0x1000);
            byteBuffer0.mark();
            try {
                Socket socket0 = NetBackupHandler.this.getNewHttpConnection();
                if(socket0.isConnected()) {
                    os = socket0.getOutputStream();
                    is = socket0.getInputStream();
                }
                else {
                    Log.e("m_NetBackup", "Socket is NOT Connected!");
                    NetBackupHandler.this.result = OPERATION_FAILED;
                }

                if(os != null && is != null) {
                    os.write((POST_REQUEST + "\r\nContent-Length: " + ins[0].length() + END_HEADER + ins[0]).getBytes());
                    while(true) {
                        int bytesRead = is.read(buffer);
                        if(bytesRead == -1) {
                            byteBuffer0.reset();
                            NetBackupHandler.this.result = BACKUP_SUCCESS;
                            break;
                        }

                        byteBuffer0.put(buffer, 0, bytesRead);
                    }
                }
                else {
                    Log.e("m_NetBackup", "Connection to server failed!");
                    NetBackupHandler.this.result = OPERATION_FAILED;
                }

                socket0.close();
            }
            catch(Exception e) {
                Log.e("m_NetBackup", "Error during connection: " + e.getMessage());
            }

            return null;
        }

        @Override  // android.os.AsyncTask
        protected void onPostExecute(Object out) {
            NetBackupHandler.this.state = NOT_RUNNING;
            int v = NetBackupHandler.this.result;
            NetBackupHandler.this.rl.onTaskFinish(v, null);
        }
    }

    class NetRestore extends AsyncTask {
        private NetRestore() {
        }

        NetRestore(NetRestore netBackupHandler$NetRestore0) {
        }

        @Override  // android.os.AsyncTask
        protected Object doInBackground(Object[] arr_object) {
            return this.doInBackground(((Void[])arr_object));
        }

        protected String doInBackground(Void[] arg0) {
            byte[] buffer = new byte[0x1000];
            ByteBuffer byteBuffer0 = ByteBuffer.allocate(0x1000);
            byteBuffer0.mark();
            try {
                Socket socket0 = NetBackupHandler.this.getNewHttpConnection();
                if(!socket0.isConnected()) {
                    Log.e("m_NetBackup", "Socket is NOT Connected!");
                    NetBackupHandler.this.result = OPERATION_FAILED;
                    return null;
                }

                OutputStream os = socket0.getOutputStream();
                InputStream is = socket0.getInputStream();
                if(os != null) {
                    os.write((GET_REQUEST + END_HEADER).getBytes());
                    while(true) {
                        int bytesRead = is.read(buffer);
                        if(bytesRead == -1) {
                            byteBuffer0.reset();
                            String out = new String(byteBuffer0.array()).split(END_HEADER)[1].split("\u0000")[0];
                            NetBackupHandler.this.result = RESTORE_SUCCESS;
                            return out;
                        }

                        byteBuffer0.put(buffer, 0, bytesRead);
                    }
                }
                Log.e("m_NetBackup", "Socket is NOT Connected!");
                NetBackupHandler.this.result = OPERATION_FAILED;
                return null;
            }
            catch(Exception e) {
                Log.e("m_NetBackup", "Error during connection: " + e.getMessage());
                NetBackupHandler.this.result = OPERATION_FAILED;
            }

            return null;
        }

        @Override  // android.os.AsyncTask
        protected void onPostExecute(Object object0) {
            this.onPostExecute(((String)object0));
        }

        protected void onPostExecute(String out) {
            NetBackupHandler.this.state = NOT_RUNNING;
            NetBackupHandler.this.rl.onTaskFinish(NetBackupHandler.this.result, out);
        }
    }

    public interface ResultListener {
        void onTaskFinish(int arg1, String arg2);
    }

    public static final int BACKUP_SUCCESS = 0x7043B;
    private static final String END_HEADER = "\r\n\r\n";
    private static final String GET_REQUEST = "GET /Backup.xml HTTP/1.1";
    private static final int NOT_RUNNING = 0x3B7C4;
    public static final int OPERATION_FAILED = 0x5BBD;
    private static final String POST_REQUEST = "POST /Backup.xml HTTP/1.1";
    public static final int RESTORE_SUCCESS = 0xB6111;
    private static final int RUNNING_BACKUP = 0x252C8B;
    private static final int RUNNING_RESTORE = 0x4054CEA;
    private static final String TAG = "m_NetBackup";
    private String port;
    private int result;
    private ResultListener rl;
    private int state;
    private String url;

    public NetBackupHandler(String iurl, String iport, ResultListener irl) {
        this.state = NOT_RUNNING;
        this.result = -1;
        this.url = "localhost";
        this.port = "8000";
        this.url = iurl;
        this.port = iport;
        this.rl = irl;
    }

    private Socket getNewHttpConnection() {
        TrustManager tm = new X509TrustManager() {
            @Override  // javax.net.ssl.X509TrustManager
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override  // javax.net.ssl.X509TrustManager
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override  // javax.net.ssl.X509TrustManager
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        try {
            SSLContext sSLContext0 = SSLContext.getInstance("TLS");
            SecureRandom secureRandom0 = new SecureRandom();
            sSLContext0.init(new KeyManager[0], new TrustManager[]{tm}, secureRandom0);
            return sSLContext0.getSocketFactory().createSocket(this.url, Integer.parseInt(this.port));
        }
        catch(Exception e) {
            Log.e("m_NetBackup", "ERROR: Socket createion error: " + e.getMessage());
            return null;
        }
    }

    public void performNetBackup(String data) {
        synchronized(this) {
            if(this.state == NOT_RUNNING) {
                this.state = RUNNING_BACKUP;
                new NetBackup(null).execute(new String[]{data});
            }
        }
    }

    public void performNetRestore() {
        synchronized(this) {
            Log.i(TAG, "performNetRestore");
            if(this.state == NOT_RUNNING) {
                this.state = RUNNING_RESTORE;
                new NetRestore(null).execute(new Void[0]);
            }
        }
    }

}

