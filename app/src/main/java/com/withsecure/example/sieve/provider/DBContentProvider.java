package com.withsecure.example.sieve.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.withsecure.example.sieve.util.PWDBHelper;
import com.withsecure.example.sieve.database.PWTable;

public class DBContentProvider extends ContentProvider {
    public static final int KEY = 200;
    public static Uri KEYS_URI = null;
    public static final int KEY_ID = 230;
    public static final int KEY_PASSWORD = 210;
    public static final int KEY_PIN = 220;
    public static final int PASSWORDS = 100;
    public static final int PASSWORDS_EMAIL = 140;
    public static final int PASSWORDS_ID = 110;
    public static final int PASSWORDS_PASSWORD = 150;
    public static final int PASSWORDS_SERVICE = 120;
    public static Uri PASSWORDS_URI = null;
    public static final int PASSWORDS_USERNAME = 130;
    PWDBHelper pwdb;
    private UriMatcher sUriMatcher;

    static {
        DBContentProvider.PASSWORDS_URI = Uri.parse("content://com.withsecure.example.sieve.provider.DBContentProvider/Passwords");
        DBContentProvider.KEYS_URI = Uri.parse("content://com.withsecure.example.sieve.provider.DBContentProvider/Keys");
    }

    public DBContentProvider() {
        this.sUriMatcher = new UriMatcher(-1);
    }

    @Override  // android.content.ContentProvider
    public int delete(Uri in, String selection, String[] selectionArgs) {
        int v = this.sUriMatcher.match(in);
        if(v == PASSWORDS) {
            return this.pwdb.getWritableDatabase().delete(PWTable.TABLE_NAME, selection, selectionArgs);
        }

        return v == KEY ? this.pwdb.getWritableDatabase().delete(PWTable.KEY_TABLE_NAME, selection, selectionArgs) : -1;
    }

    @Override  // android.content.ContentProvider
    public String getType(Uri arg0) {
        return null;
    }

    @Override  // android.content.ContentProvider
    public Uri insert(Uri in, ContentValues values) {
        int v = this.sUriMatcher.match(in);
        if(v == PASSWORDS) {
            return ContentUris.withAppendedId(in, this.pwdb.getWritableDatabase().insert(PWTable.TABLE_NAME, null, values));
        }

        return v == KEY ? ContentUris.withAppendedId(in, this.pwdb.getWritableDatabase().insert(PWTable.KEY_TABLE_NAME, null, values)) : ContentUris.withAppendedId(in, -1L);
    }

    @Override  // android.content.ContentProvider
    public boolean onCreate() {
        this.pwdb = new PWDBHelper(this.getContext());
        this.sUriMatcher.addURI("com.withsecure.example.sieve.provider.DBContentProvider", PWTable.TABLE_NAME, PASSWORDS);
        this.sUriMatcher.addURI("com.withsecure.example.sieve.provider.DBContentProvider", "Keys", KEY);
        return false;
    }

    @Override  // android.content.ContentProvider
    public Cursor query(Uri in, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int v = this.sUriMatcher.match(in);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        if(v >= PASSWORDS && v < KEY) {
            queryBuilder.setTables(PWTable.TABLE_NAME);
            return queryBuilder.query(this.pwdb.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
        }

        if(v >= KEY) {
            queryBuilder.setTables(PWTable.KEY_TABLE_NAME);
            return queryBuilder.query(this.pwdb.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
        }

        return queryBuilder.query(this.pwdb.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override  // android.content.ContentProvider
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int v = this.sUriMatcher.match(uri);
        if(v == PASSWORDS) {
            return this.pwdb.getWritableDatabase().update(PWTable.TABLE_NAME, values, selection, selectionArgs);
        }

        return v == KEY ? this.pwdb.getWritableDatabase().update(PWTable.KEY_TABLE_NAME, values, selection, selectionArgs) : -1;
    }
}

