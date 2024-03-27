package com.withsecure.example.sieve.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;

public class FileBackupProvider extends ContentProvider {
    private static final int DATABASE = 0x23CDD8;
    public static Uri FILE_DATABASE = null;
    private static final String TAG = "m_FileBackupProvider";
    private final UriMatcher sUriMatcher;

    static {
        FileBackupProvider.FILE_DATABASE = Uri.parse("content://com.withsecure.example.sieve.provider.FileBackupProvider");
    }

    public FileBackupProvider() {
        this.sUriMatcher = new UriMatcher(-1);
    }

    @Override  // android.content.ContentProvider
    public int delete(Uri arg0, String arg1, String[] arg2) {
        return 0;
    }

    @Override  // android.content.ContentProvider
    public String getType(Uri arg0) {
        return null;
    }

    @Override  // android.content.ContentProvider
    public Uri insert(Uri arg0, ContentValues arg1) {
        return null;
    }

    @Override  // android.content.ContentProvider
    public boolean onCreate() {
        this.sUriMatcher.addURI("com.withsecure.example.sieve.provider.FileBackupProvider", "*", DATABASE);
        return false;
    }

    @Override  // android.content.ContentProvider
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        int modeCode;
        if(mode.equals("r")) {
            modeCode = 0x10000000; // 268435456
            return ParcelFileDescriptor.open(new File(Objects.requireNonNull(uri.getPath())), modeCode);
        }

        if(mode.equals("rw")) {
            modeCode = 0x30000000; // 805306368
            return ParcelFileDescriptor.open(new File(Objects.requireNonNull(uri.getPath())), modeCode);
        }

        if(mode.equals("rwt")) {
            modeCode = 0x30000000; // 805306368
            try {
                return ParcelFileDescriptor.open(new File(Objects.requireNonNull(uri.getPath())), modeCode);
            }
            catch(FileNotFoundException e) {
                Log.e(TAG, "ERROR: unable to open file: " + e.getMessage());
                return null;
            }
        }

        Log.w(TAG, "Unrecognised code to open file: " + mode);
        return null;
    }

    @Override  // android.content.ContentProvider
    public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3, String arg4) {
        return null;
    }

    @Override  // android.content.ContentProvider
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        return 0;
    }
}

