package com.withsecure.example.sieve.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.withsecure.example.sieve.database.PWTable;
import com.withsecure.example.sieve.R;
import com.withsecure.example.sieve.provider.DBContentProvider;
import com.withsecure.example.sieve.service.CryptoService;
import com.withsecure.example.sieve.service.CryptoServiceConnector;
import com.withsecure.example.sieve.util.PasswordEntry;

public class PWList extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, CryptoServiceConnector.ResponseListener {
    public static final int FOR_ACCESS = 653;
    public static final int FOR_EDIT = 468;
    public static final int FOR_INSERT = 0x2DF;
    private String MainPassword;
    private static final int PWLIST_ADD = 1;
    private static final int PWLIST_EDIT = 2;
    private static final int PWLIST_SETTINGS = 3;
    public static final String REQUEST_TYPE = "com.withsecure.example.sieve.REQUEST_TYPE";
    private static final String TAG = "m_PWList";
    private SimpleCursorAdapter adapter;
    private AlertDialog.Builder connectionError;
    private TextView prompt;
    private CryptoServiceConnector serviceConnection;
    private PasswordEntry workingEntry;
    private int workingRow;

    public PWList() {
        this.adapter = null;
        this.workingRow = -1;
    }

    private void addEntry() {
        Intent intent = new Intent(this, AddEntryActivity.class);
        intent.putExtra(AddEntryActivity.REQUEST, AddEntryActivity.REQUEST_ADD);
        intent.putExtra(REQUEST_TYPE, FOR_INSERT);
        this.startActivityForResult(intent, PWLIST_ADD);
    }

    @Override  // com.withsecure.example.sieve.service.CryptoServiceConnector$ResponseListener
    public void connected() {
        this.populateList();
    }

    @Override  // com.withsecure.example.sieve.service.CryptoServiceConnector$ResponseListener
    public void decryptionReturned(String result, int code) {
        switch(code) {
            case FOR_EDIT: {
                this.workingEntry.password = result;
                Intent intent = new Intent(this, AddEntryActivity.class);
                intent.putExtra(AddEntryActivity.ENTRY, this.workingEntry);
                intent.putExtra(AddEntryActivity.REQUEST, AddEntryActivity.REQUEST_EDIT);
                intent.putExtra(REQUEST_TYPE, FOR_EDIT);
                this.startActivityForResult(intent, PWLIST_EDIT);
                return;
            }
            case FOR_ACCESS: {
                ((ClipboardManager)this.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(PWTable.KEY_COLUMN_NAME_MAIN, result));
                Toast.makeText(this.getApplicationContext(), "Password Copied to clipboard", Toast.LENGTH_SHORT).show();
                return;
            }
            default: {
            }
        }
    }

    private void deleteEntry(int id) {
        this.getContentResolver().delete(DBContentProvider.PASSWORDS_URI, "_id = ?", new String[]{String.valueOf(id)});
        Toast.makeText(this.getApplicationContext(), "Entry deleted", Toast.LENGTH_SHORT).show();
        this.populateList();
    }

    @Override  // com.withsecure.example.sieve.service.CryptoServiceConnector$ResponseListener
    public void encryptionReturned(byte[] result, int code) {
        if(this.finaliseEntry(result, code)) {
            this.populateList();
            if(code == FOR_INSERT) {
                Toast.makeText(this.getApplicationContext(), "Entry Added", Toast.LENGTH_SHORT).show();
                return;
            }

            if(code == FOR_EDIT) {
                Toast.makeText(this.getApplicationContext(), "Entry Updated", Toast.LENGTH_SHORT).show();
                return;
            }

            return;
        }

        Log.e(TAG, "entry to db failed!");
    }

    private boolean finaliseEntry(byte[] encryptedString, int code) {
        switch(code) {
            case FOR_EDIT: {
                return this.finaliseUpdate(encryptedString);
            }
            case FOR_INSERT: {
                return this.finaliseInsert(encryptedString);
            }
            default: {
                Log.e(TAG, "unrecognised MSG_TYPE for finalisedEntry: " + code);
                return false;
            }
        }
    }

    private boolean finaliseInsert(byte[] encryptedString) {
        ContentValues out = new ContentValues();
        out.put(PWTable.COLUMN_NAME_SERVICE, this.workingEntry.service);
        out.put(PWTable.COLUMN_NAME_USERNAME, this.workingEntry.username);
        out.put(PWTable.COLUMN_NAME_PASSWORD, encryptedString);
        out.put(PWTable.COLUMN_NAME_EMAIL, this.workingEntry.email);
        return this.getContentResolver().insert(DBContentProvider.PASSWORDS_URI, out) != null;
    }

    private boolean finaliseUpdate(byte[] encryptedString) {
        String[] arr_s = {String.valueOf(this.workingRow)};
        if(this.getContentResolver().delete(DBContentProvider.PASSWORDS_URI, "_id = ?", arr_s) == PWLIST_ADD) {
            return this.finaliseInsert(encryptedString);
        }

        Log.e(TAG, "failed to delete entry from database");
        return false;
    }

    @Override  // android.app.Activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case PWLIST_ADD:
            case PWLIST_EDIT: {
                break;
            }
            default: {
                return;
            }
        }

        if(resultCode == -1) {
            PasswordEntry temp = data.getParcelableExtra(AddEntryActivity.ENTRY);
            if(temp == null) {
                this.deleteEntry(this.workingRow);
                return;
            }

            this.workingEntry = temp;
            if(requestCode == PWLIST_ADD) {
                this.serviceConnection.sendForEncryption(this.MainPassword, this.workingEntry.password, FOR_INSERT);
                return;
            }

            this.serviceConnection.sendForEncryption(this.MainPassword, this.workingEntry.password, FOR_EDIT);
        }
    }

    @Override  // android.app.Activity
    public void onBackPressed() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);
    }

    @Override  // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_pwlist);

        findViewById(R.id.addentry_button_add).setOnClickListener(v -> {
            addEntry();
        });

        findViewById(R.id.button_goto_settings).setOnClickListener(v -> {
            openSettings();
        });

        this.MainPassword = this.getIntent().getStringExtra(CryptoService.KEY);
        this.serviceConnection = new CryptoServiceConnector(this);
        this.adapter = new SimpleCursorAdapter(this, R.layout.format_pwlist, null, new String[]{PWTable.COLUMN_NAME_SERVICE, PWTable.COLUMN_NAME_USERNAME}, new int[]{R.id.format_pwlist_service, R.id.format_pwlist_username}, 2);
        ListView pwList = this.findViewById(R.id.pwlist_list_pwlist);
        pwList.setAdapter(this.adapter);
        pwList.setOnItemClickListener(this);
        pwList.setOnItemLongClickListener(this);
        this.prompt = this.findViewById(R.id.pwlist_textview_prompt);
        this.connectionError = new AlertDialog.Builder(this);
        this.connectionError.setMessage(R.string.service_error_cantconnect).setTitle("Error");
        this.connectionError.setPositiveButton("OK", (arg0, arg1) -> {
        });
        this.connectionError.create();
    }

    @SuppressLint("Range")
    @Override  // android.widget.AdapterView$OnItemClickListener
    public void onItemClick(AdapterView adapterView0, View view, int element, long b) {
        Cursor cursor0 = this.adapter.getCursor();
        cursor0.moveToPosition(element);
        this.serviceConnection.sendForDecryption(this.MainPassword, cursor0.getBlob(cursor0.getColumnIndex(PWTable.COLUMN_NAME_PASSWORD)), FOR_ACCESS);
    }

    @SuppressLint("Range")
    @Override  // android.widget.AdapterView$OnItemLongClickListener
    public boolean onItemLongClick(AdapterView adapterView0, View view, int element, long b) {
        Cursor cursor0 = this.adapter.getCursor();
        cursor0.moveToPosition(element);
        this.workingRow = cursor0.getInt(cursor0.getColumnIndex("_id"));
        this.workingEntry = new PasswordEntry(cursor0.getString(cursor0.getColumnIndex(PWTable.COLUMN_NAME_SERVICE)), cursor0.getString(cursor0.getColumnIndex(PWTable.COLUMN_NAME_USERNAME)), cursor0.getString(cursor0.getColumnIndex(PWTable.COLUMN_NAME_EMAIL)), null);
        this.serviceConnection.sendForDecryption(this.MainPassword, cursor0.getBlob(cursor0.getColumnIndex(PWTable.COLUMN_NAME_PASSWORD)), FOR_EDIT);
        return false;
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

    private void openSettings() {
        this.startActivityForResult(new Intent(this, SettingsActivity.class), PWLIST_SETTINGS);
    }

    private void populateList() {
        Cursor cursor0 = new CursorLoader(this, DBContentProvider.PASSWORDS_URI, null, null, null, PWTable.COLUMN_NAME_SERVICE).loadInBackground();
        this.adapter.changeCursor(cursor0);
        if(cursor0.getCount() > 0) {
            this.prompt.setVisibility(View.INVISIBLE);
            return;
        }

        this.prompt.setVisibility(View.VISIBLE);
    }

    @Override  // com.withsecure.example.sieve.service.CryptoServiceConnector$ResponseListener
    public void sendFailed() {
        this.connectionError.show();
    }

    private void unbind() {
        this.unbindService(this.serviceConnection);
    }
}

