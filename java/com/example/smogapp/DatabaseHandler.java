package com.example.smogapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseHandler {

    private static DatabaseHandler instance;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;

    private DatabaseHandler(Context context) {
        databaseHelper = new DatabaseHelper(context);
        db = databaseHelper.getWritableDatabase();
    }

    public static DatabaseHandler getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHandler(context);
        }
        return instance;
    }

    public void closeDatabase() {
        db.close();
    }

    private boolean isDatabaseInitialized = false;

    public boolean isDatabaseInitialized() {
        return isDatabaseInitialized;
    }

    public void setDatabaseInitialized() {
        isDatabaseInitialized = true;
    }

    @SuppressLint("Range")
    public List<String> getMainListItems() {
        List<String> items = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String[] projection = {DatabaseHelper.COLUMN_CONTENT};
        Cursor cursor = db.query(
                true,
                DatabaseHelper.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                DatabaseHelper.COLUMN_ITEMORDER + " ASC", // Ordering by item order ascending
                null
        );

        if (cursor.moveToFirst()) {
            do {
                String content = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTENT));
                items.add(content);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return items;
    }



    @SuppressLint("Range")
    public List<String> getSublistForMainItem(String mainItem) {
        List<String> sublist = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String[] projection = {DatabaseHelper.COLUMN_PROTOCOL};
        String selection = DatabaseHelper.COLUMN_CONTENT + " = ?";
        String[] selectionArgs = {mainItem};

        Cursor cursor = db.query(
                true,
                DatabaseHelper.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseHelper.COLUMN_ITEMORDER + " ASC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                String protocol = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PROTOCOL));
                sublist.add(protocol);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return sublist;
    }

    @SuppressLint("Range")
    public String getPageNumberFromDatabase(String protocol) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String[] projection = {DatabaseHelper.COLUMN_PAGE};
        String selection = DatabaseHelper.COLUMN_PROTOCOL + " = ?";
        String[] selectionArgs = {protocol};

        Cursor cursor = db.query(
                true,
                DatabaseHelper.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null,
                null
        );

        Set<String> pageNumbersSet = new HashSet<>();

        if (cursor.moveToFirst()) {
            do {
                String pageNumber = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PAGE));
                pageNumbersSet.add(pageNumber);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        StringBuilder pageNumbers = new StringBuilder();
        for (String pageNumber : pageNumbersSet) {
            pageNumbers.append(pageNumber).append(", ");
        }

        if (pageNumbers.length() > 2) {
            pageNumbers.setLength(pageNumbers.length() - 2);
        }

        return pageNumbers.toString();
    }

    public void insertDataFromCSV(Context context) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        InputStream inputStream = context.getResources().openRawResource(R.raw.data); // Replace "your_csv_file" with the actual name of your CSV file in res/raw folder.
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                if (data.length >= 5) {
                    int itemorder = Integer.parseInt(data[0].trim());
                    String content = data[1].trim();
                    String protocol = data[2].trim();
                    String page = data[3].trim();
                    String keywords = data[4].trim();

                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_ITEMORDER, itemorder);
                    values.put(DatabaseHelper.COLUMN_CONTENT, content);
                    values.put(DatabaseHelper.COLUMN_PROTOCOL, protocol);
                    values.put(DatabaseHelper.COLUMN_PAGE, page);
                    values.put(DatabaseHelper.COLUMN_KEYWORDS, keywords);

                    db.insert(DatabaseHelper.TABLE_NAME, null, values);
                } else {
                    Log.e("CSV Data", "Invalid CSV line: " + line);
                }
            }

            reader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

