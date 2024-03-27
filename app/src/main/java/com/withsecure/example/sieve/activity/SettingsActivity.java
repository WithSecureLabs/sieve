package com.withsecure.example.sieve.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.withsecure.example.sieve.R;
import com.withsecure.example.sieve.database.DBParser;
import com.withsecure.example.sieve.database.PWTable;
import com.withsecure.example.sieve.provider.DBContentProvider;
import com.withsecure.example.sieve.provider.FileBackupProvider;
import com.withsecure.example.sieve.service.AuthService;
import com.withsecure.example.sieve.service.CryptoService;
import com.withsecure.example.sieve.service.CryptoServiceConnector;
import com.withsecure.example.sieve.util.NetBackupHandler;
import com.withsecure.example.sieve.util.PasswordEntry;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

public class SettingsActivity extends Activity implements CryptoServiceConnector.ResponseListener, NetBackupHandler.ResultListener {
    private static final String BACKUP_FILENAME = "Backup";
    private static final int BACKUP_TO_NET = 0xFC0B;
    private static final int BACKUP_TO_SD = 0x983;
    public static final int FILE = 293;
    private static final String NO = "Cancel";
    private static final int NOT_RUNNING = 0xCBBFC;
    private String PIN;
    public static final int PIN_REQUEST = 456;
    private static final String TAG = "m_Settings";
    private static final String YES = "Yes";
    AlertDialog.Builder backupNet;
    AlertDialog.Builder backupSD;
    private AlertDialog.Builder connectionError;
    AlertDialog.Builder deleteALL;
    AlertDialog.Builder errorCantChangePin;
    AlertDialog.Builder errorCantRead;
    AlertDialog.Builder errorDBEmpty;
    private LinkedList<PasswordEntry> ll;
    private NetBackupHandler netBackup;
    AlertDialog.Builder netRestore;
    private StringBuffer out;
    AlertDialog.Builder restore;
    private int runningState;
    private CryptoServiceConnector serviceConnection;
    private static final int writeable = 1;

    public SettingsActivity() {
        this.serviceConnection = new CryptoServiceConnector(this);
        this.runningState = NOT_RUNNING;
    }

    // Detected as a lambda impl.
    private void backupToNet() {
        if(this.runningState == NOT_RUNNING) {
            this.runningState = BACKUP_TO_NET;
            this.getBackup();
        }
    }

    public void backupToNet(View view) {
        this.backupNet.show();
    }

    // Detected as a lambda impl.
    private void backupToSD() {
        if(this.runningState == NOT_RUNNING && (this.checkSDState(writeable))) {
            this.runningState = BACKUP_TO_SD;
            this.getBackup();
        }
    }

    public void backupToSD(View view) {
        this.backupSD.show();
    }

    private boolean changePIN(String pin) {
        ContentValues out = new ContentValues();
        out.put(PWTable.KEY_COLUMN_NAME_SHORT, pin);
        String[] arr_s = {this.PIN};
        return this.getContentResolver().update(DBContentProvider.KEYS_URI, out, "pin = ?", arr_s) > 0;
    }

    @SuppressLint("Range")
    public void changePIN(View view) {
        Cursor cursor0 = new CursorLoader(this, DBContentProvider.KEYS_URI, null, null, null, null).loadInBackground();
        cursor0.moveToFirst();
        this.PIN = cursor0.getString(cursor0.getColumnIndex(PWTable.KEY_COLUMN_NAME_SHORT));
        Intent intent = new Intent(this, PINActivity.class);
        intent.putExtra(AuthService.PIN, this.PIN);
        intent.putExtra(AddEntryActivity.REQUEST, PINActivity.REQUEST_EDIT);
        this.startActivityForResult(intent, PIN_REQUEST);
    }

    private boolean checkSDState(int code) {
        return "mounted".equals(Environment.getExternalStorageState());
    }

    @Override  // com.withsecure.example.sieve.service.CryptoServiceConnector$ResponseListener
    public void connected() {
    }

    @Override  // com.withsecure.example.sieve.service.CryptoServiceConnector$ResponseListener
    public void decryptionReturned(String result, int code) {
        this.finaliseEntry(result);
    }

    // Detected as a lambda impl.
    private void deleteAll() {
        this.getContentResolver().delete(DBContentProvider.PASSWORDS_URI, null, null);
        Toast.makeText(this.getApplicationContext(), "Database deleted", Toast.LENGTH_SHORT).show();
    }

    public void deleteAll(View view) {
        this.deleteALL.show();
    }

    @Override  // com.withsecure.example.sieve.service.CryptoServiceConnector$ResponseListener
    public void encryptionReturned(byte[] result, int code) {
        this.finaliseInsert(result);
    }

    private void finaliseBackupToSD() {
        File file = new File(this.getExternalFilesDir(null), "Backup (2023-11-01 11-39-55.364).xml");
        try {
            if(!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream os = new FileOutputStream(file);
            os.write(this.out.toString().getBytes());
            os.close();
            Toast.makeText(this.getApplicationContext(), "Database exported", Toast.LENGTH_SHORT).show();
        }
        catch(IOException e) {
            Log.e(TAG, "ERROR: Unable to write to file");
            Log.e(TAG, e.getMessage());
        }
    }

    private void finaliseEntry(String password) {
        PasswordEntry pe = this.ll.poll();
        pe.password = password;
        this.out.append(DBParser.processElement(pe));
        if(this.ll.isEmpty()) {
            this.finaliseOutput();
        }
    }

    private void finaliseInsert(byte[] password) {
        PasswordEntry pe = this.ll.poll();
        ContentValues out = new ContentValues();
        out.put(PWTable.COLUMN_NAME_SERVICE, pe.service);
        out.put(PWTable.COLUMN_NAME_USERNAME, pe.username);
        out.put(PWTable.COLUMN_NAME_PASSWORD, password);
        out.put(PWTable.COLUMN_NAME_EMAIL, pe.email);
        this.getContentResolver().insert(DBContentProvider.PASSWORDS_URI, out);
        if(this.ll.isEmpty()) {
            this.finaliseRestore();
        }
    }

    private void finaliseOutput() {
        this.out.append("</Passwords>");
        switch(this.runningState) {
            case BACKUP_TO_SD: {
                this.finaliseBackupToSD();
                this.runningState = NOT_RUNNING;
                return;
            }
            case BACKUP_TO_NET: {
                this.netBackup.performNetBackup(this.out.toString());
                this.runningState = NOT_RUNNING;
                return;
            }
            default: {
                this.runningState = NOT_RUNNING;
            }
        }
    }

    private void finaliseRestore() {
        Toast.makeText(this.getApplicationContext(), "Database Restored", Toast.LENGTH_SHORT).show();
        this.startActivity(new Intent(this, MainLoginActivity.class));
    }

    private void getBackup() {
        this.ll = new LinkedList<>();
        this.out = new StringBuffer("<Passwords");
        Cursor cursor0 = new CursorLoader(this, DBContentProvider.PASSWORDS_URI, null, null, null, null).loadInBackground();
        Cursor cursor1 = new CursorLoader(this, DBContentProvider.KEYS_URI, null, null, null, null).loadInBackground();
        cursor1.moveToFirst();
        @SuppressLint("Range") String s = cursor1.getString(cursor1.getColumnIndex(PWTable.KEY_COLUMN_NAME_MAIN));
        @SuppressLint("Range") String s1 = cursor1.getString(cursor1.getColumnIndex(PWTable.KEY_COLUMN_NAME_SHORT));
        this.out.append(" Key=\"").append(s).append("\" Pin=\"").append(s1).append("\">");
        if(cursor0.getCount() > 0) {
            cursor0.moveToFirst();
            for(int i = 0; i < cursor0.getCount(); ++i) {
                @SuppressLint("Range") String s2 = cursor0.getString(cursor0.getColumnIndex(PWTable.COLUMN_NAME_SERVICE));
                @SuppressLint("Range") String s3 = cursor0.getString(cursor0.getColumnIndex(PWTable.COLUMN_NAME_USERNAME));
                @SuppressLint("Range") String s4 = cursor0.getString(cursor0.getColumnIndex(PWTable.COLUMN_NAME_SERVICE));
                @SuppressLint("Range") byte[] arr_b = cursor0.getBlob(cursor0.getColumnIndex(PWTable.COLUMN_NAME_PASSWORD));
                this.ll.offer(new PasswordEntry(s2, s3, s4, null));
                this.serviceConnection.sendForDecryption(s.substring(0, 16), arr_b, 0);
                cursor0.moveToNext();
            }

            return;
        }

        Toast.makeText(this.getApplicationContext(), "Database exported", Toast.LENGTH_SHORT).show();
    }

    private void getRestore(InputStream is) throws Exception {
        BufferedInputStream bis = new BufferedInputStream(is);
        bis.mark(0x5F5E0FF);
        String mainPassword = DBParser.getKey(bis);
        bis.reset();
        this.PIN = DBParser.getPIN(bis);
        bis.reset();
        this.ll = (LinkedList)DBParser.readFile(bis);
        this.getContentResolver().delete(DBContentProvider.KEYS_URI, null, null);
        this.getContentResolver().delete(DBContentProvider.PASSWORDS_URI, null, null);
        ContentValues out = new ContentValues();
        out.put(PWTable.KEY_COLUMN_NAME_MAIN, mainPassword);
        out.put(PWTable.KEY_COLUMN_NAME_SHORT, this.PIN);
        this.getContentResolver().insert(DBContentProvider.KEYS_URI, out);
        for(int i = 0; i < this.ll.size(); ++i) {
            this.serviceConnection.sendForEncryption(mainPassword.substring(0, 16), (this.ll.get(i)).password, 0);
        }
    }

    @Override  // android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case FILE: {
                if(resultCode == -1) {
                    this.restoreFromFile(data.getStringExtra(FileSelectActivity.FILE));
                    return;
                }

                return;
            }
            case PIN_REQUEST: {
                if(resultCode == -1 && !this.changePIN(data.getStringExtra(AuthService.PIN))) {
                    this.errorCantChangePin.show();
                    return;
                }

                return;
            }
            default: {
            }
        }
    }

    @Override  // android.app.Activity
    public void onBackPressed() {
        this.finish();
    }

    @Override  // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);
        this.netBackup = new NetBackupHandler("10.0.2.2", "8001", this);

        this.backupSD = new AlertDialog.Builder(this);
        this.backupSD.setMessage(R.string.settings_confirm_backup).setTitle("Confirm");
        this.backupSD.setPositiveButton(YES, (arg0, arg1) -> SettingsActivity.this.backupToSD());
        this.backupSD.setNegativeButton(NO, (arg0, arg1) -> {
        });
        this.backupSD.create();

        this.deleteALL = new AlertDialog.Builder(this);
        this.deleteALL.setMessage(R.string.settings_confirm_delete).setTitle("Confirm");
        this.deleteALL.setPositiveButton(YES, (arg0, arg1) -> SettingsActivity.this.deleteAll());
        this.deleteALL.setNegativeButton(NO, (arg0, arg1) -> {
        });
        this.deleteALL.create();

        this.restore = new AlertDialog.Builder(this);
        this.restore.setMessage(R.string.settings_confirm_restore).setTitle("Restore");
        this.restore.setPositiveButton(YES, (arg0, arg1) -> SettingsActivity.this.restoreFromSD());
        this.restore.setNegativeButton(NO, (arg0, arg1) -> {
        });
        this.restore.create();

        this.netRestore = new AlertDialog.Builder(this);
        this.netRestore.setMessage(R.string.settings_confirm_netrestore).setTitle("Restore");
        this.netRestore.setPositiveButton(YES, (arg0, arg1) -> SettingsActivity.this.restoreFromNet());
        this.netRestore.setNegativeButton(NO, (arg0, arg1) -> {
        });
        this.netRestore.create();

        this.backupNet = new AlertDialog.Builder(this);
        this.backupNet.setMessage(R.string.settings_confirm_netbackup).setTitle(BACKUP_FILENAME);
        this.backupNet.setPositiveButton(YES, (arg0, arg1) -> SettingsActivity.this.backupToNet());
        this.backupNet.setNegativeButton(NO, (arg0, arg1) -> {
        });
        this.backupNet.create();

        this.errorDBEmpty = new AlertDialog.Builder(this);
        this.errorDBEmpty.setMessage(R.string.settings_error_dbempty).setTitle("Error");
        this.errorDBEmpty.setPositiveButton("OK", (arg0, arg1) -> {
        });
        this.errorDBEmpty.create();

        this.errorCantRead = new AlertDialog.Builder(this);
        this.errorCantRead.setMessage(R.string.settings_error_cantread).setTitle("Error");
        this.errorCantRead.setPositiveButton("OK", (arg0, arg1) -> {
        });
        this.errorCantRead.create();

        this.errorCantChangePin = new AlertDialog.Builder(this);
        this.errorCantChangePin.setMessage(R.string.settings_error_cantchangepin).setTitle("Error");
        // android.content.DialogInterface$OnClickListener
        this.errorCantChangePin.setPositiveButton("OK", (arg0, arg1) -> {
        });
        this.errorCantChangePin.create();

        this.connectionError = new AlertDialog.Builder(this);
        this.connectionError.setMessage(R.string.service_error_cantconnect).setTitle("Error");
        // android.content.DialogInterface$OnClickListener
        this.connectionError.setPositiveButton("OK", (arg0, arg1) -> {
        });
        this.connectionError.create();
        //this.getActionBar().setHomeButtonEnabled(true);

    }

    @Override  // android.app.Activity
    public void onPause() {
        super.onPause();
        this.unbind();
    }

    @Override  // android.app.Activity
    public void onResume() {
        super.onResume();
        this.bindService(new Intent(this, CryptoService.class), this.serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override  // com.withsecure.example.sieve.util.NetBackupHandler$ResultListener
    public void onTaskFinish(int task, String result) {
        switch(task) {
            case NetBackupHandler.OPERATION_FAILED: {
                Log.e(TAG, "Error during backup.");
                return;
            }
            case NetBackupHandler.BACKUP_SUCCESS: {
                Toast.makeText(this.getApplicationContext(), "Database exported", Toast.LENGTH_SHORT).show();
                return;
            }
            case NetBackupHandler.RESTORE_SUCCESS: {
                this.restoreFromString(result);
                return;
            }
            default: {
            }
        }
    }

    private void restoreFromFile(String filename) {
        try {
            this.getRestore(this.getContentResolver().openInputStream(Uri.withAppendedPath(FileBackupProvider.FILE_DATABASE, filename.substring(writeable))));
        }
        catch(Exception e) {
            this.errorCantRead.show();
            Log.d("yaylogyay2", String.valueOf(e.getMessage()));
            Log.d("yaylogyay2", String.valueOf(e.getStackTrace()));
            Log.d("yaylogyay2", String.valueOf(filename));
        }
    }

    // Detected as a lambda impl.
    private void restoreFromNet() {
        this.netBackup.performNetRestore();
    }

    public void restoreFromNet(View view) {
        this.netRestore.show();
    }

    // Detected as a lambda impl.
    private void restoreFromSD() {
        this.startActivityForResult(new Intent(this, FileSelectActivity.class), FILE);
    }

    public void restoreFromSD(View view) {
        this.restore.show();
    }

    private void restoreFromString(String data) {
        try {
            this.getRestore(new ByteArrayInputStream(data.getBytes()));
        }
        catch(Exception e) {
            Log.e(TAG, "ERROR: Unable to read / process data from server: " + e.getMessage());
            this.errorCantRead.show();
        }
    }

    @Override  // com.withsecure.example.sieve.service.CryptoServiceConnector$ResponseListener
    public void sendFailed() {
        this.connectionError.show();
    }

    private void unbind() {
        this.unbindService(this.serviceConnection);
    }
}

