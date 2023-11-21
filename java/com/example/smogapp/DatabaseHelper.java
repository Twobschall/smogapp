package com.example.smogapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "data.db";
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "Data";
    public static final String COLUMN_ITEMORDER = "ItemOrder";
    public static final String COLUMN_CONTENT = "Content";
    public static final String COLUMN_PROTOCOL = "Protocol";
    public static final String COLUMN_PAGE = "Page";
    public static final String COLUMN_KEYWORDS = "Keywords";

    // Create the table query
    public static final String CREATE_TABLE_QUERY =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ITEMORDER + " INTEGER, " +
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_PROTOCOL + " TEXT, " +
                    COLUMN_PAGE + " TEXT, " +
                    COLUMN_KEYWORDS + " TEXT);";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Drop the table if it exists and then create it
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if needed
    }

    public void setDatabaseInitialized() {
        // This method can be used for initialization flags if needed
    }
}
