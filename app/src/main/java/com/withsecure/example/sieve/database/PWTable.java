package com.withsecure.example.sieve.database;

import android.provider.BaseColumns;

public class PWTable implements BaseColumns {
    public static final String BLOB_TYPE = " BLOB";
    public static final String COLUMN_NAME_EMAIL = "email";
    public static final String COLUMN_NAME_PASSWORD = "password";
    public static final String COLUMN_NAME_SERVICE = "service";
    public static final String COLUMN_NAME_USERNAME = "username";
    public static final String COMMA_SEP = ",";
    public static final String KEY_COLUMN_NAME_MAIN = "Password";
    public static final String KEY_COLUMN_NAME_SHORT = "pin";
    public static final String KEY_SQL_CREATE_ENTRIES = "CREATE TABLE Key (Password TEXT PRIMARY KEY,pin TEXT )";
    public static final String KEY_TABLE_NAME = "Key";
    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE Passwords (_id INTEGER PRIMARY KEY,service TEXT,username TEXT,password BLOB,email )";
    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS Passwords";
    public static final String TABLE_NAME = "Passwords";
    public static final String TEXT_TYPE = " TEXT";

}

