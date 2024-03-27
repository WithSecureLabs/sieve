package com.withsecure.example.sieve.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.withsecure.example.sieve.database.PWTable;

public class PWDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "database.db";
    public static final int DATABASE_VERSION = 1;

    public PWDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override  // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PWTable.SQL_CREATE_ENTRIES);
        db.execSQL(PWTable.KEY_SQL_CREATE_ENTRIES);
    }

    @Override  // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
    }
}

