package com.example.smogapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProtocolListActivity extends AppCompatActivity {

    private RecyclerView ProtocolRecyclerView;
    private ProtocolListAdapter adapter;

    private DatabaseHelper databaseHelper;

    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocol_list);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Find the SearchView
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setVisibility(View.GONE); // Hide the SearchView by default

        // Find the LinearLayout containing the Toolbar and SearchView
        LinearLayout toolbarLayout = findViewById(R.id.toolbar_layout);

        // Set the height of the LinearLayout to 0dp and hide it
        ViewGroup.LayoutParams layoutParams = toolbarLayout.getLayoutParams();
        layoutParams.height = 160; // Set the height to 0dp
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize the database and insert data from CSV
        databaseHelper = new DatabaseHelper(this);
        db = databaseHelper.getReadableDatabase();

        // Retrieve the search query from the Intent extras
        String searchQuery = getIntent().getStringExtra("searchQuery");
        String newSearchQuery = getIntent().getStringExtra("newSearchQuery");

        ProtocolRecyclerView = findViewById(R.id.protocolRecyclerView);
        ProtocolRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String> protocols = getProtocolsFromDatabase(searchQuery); // Get protocols from the database

        if (protocols.isEmpty()) {
            Toast.makeText(this, "No protocols found.", Toast.LENGTH_SHORT).show();
        } else {
            adapter = new ProtocolListAdapter(protocols, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    String selectedProtocol = protocols.get(position);
                    // Query the database to get the page number or page range
                    String pageNumberOrRange = getPageNumberFromDatabase(selectedProtocol);

                    // Check if it's empty or a page range
                    if (pageNumberOrRange.isEmpty()) {
                        // Handle the case where pageNumberOrRange is empty (no page number found)
                    } else {
                        // Replace spaces with commas and then split into individual numbers
                        String[] pageNumbersArray = pageNumberOrRange.replace(" ", ",").split(",");

                        // Convert the array of strings to an array of integers
                        int[] pageNumbers = new int[pageNumbersArray.length];
                        for (int i = 0; i < pageNumbersArray.length; i++) {
                            String trimmedValue = pageNumbersArray[i].trim();
                            pageNumbers[i] = Integer.parseInt(trimmedValue);
                        }

                        Intent intent = new Intent(ProtocolListActivity.this, PdfViewerActivity.class);
                        intent.putExtra(PdfViewerActivity.PAGE_NUMBERS_KEY, pageNumbers);
                        startActivity(intent);
                    }
                }

                @Override
                public void onLongItemClick(View view, int position) {
                    // Handle long click actions if needed
                }
            });
            ProtocolRecyclerView.setAdapter(adapter);
        }
    }
    public void navigateToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear back stack
        startActivity(intent);
    }

    // Modify the method signature to accept the search query
    private List<String> getProtocolsFromDatabase(String searchQuery) {
        List<String> protocols = new ArrayList<>();

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define the columns you want to retrieve
        String[] projection = {DatabaseHelper.COLUMN_PROTOCOL};

        // Define the WHERE clause and its arguments based on the search query
        String selection = DatabaseHelper.COLUMN_PROTOCOL + " LIKE ?";
        String[] selectionArgs = {"%" + searchQuery + "%"};

        Cursor cursor = db.query(
                true, // Use DISTINCT
                DatabaseHelper.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null,
                null
        );

        // Extract protocols from the cursor
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int columnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PROTOCOL);
                String protocol = cursor.getString(columnIndex);
                protocols.add(protocol);
            }
            cursor.close();
        }

        db.close();
        return protocols;

    }

    @SuppressLint("Range")
    private String getPageNumberFromDatabase(String protocol) {
        // Read data from the database created earlier
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Query the database to get the page numbers based on the protocol
        String[] projection = {DatabaseHelper.COLUMN_PAGE};
        String selection = DatabaseHelper.COLUMN_PROTOCOL + " = ?";
        String[] selectionArgs = {protocol};

        Cursor cursor = db.query(
                true, // Use DISTINCT
                DatabaseHelper.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null,
                null
        );
        Set<String> pageNumbersSet = new HashSet<>(); // Use a set to store unique page numbers

        // Check if the query returned any rows
        if (cursor.moveToFirst()) {
            do {
                // Get the page number from the cursor
                String pageNumber = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PAGE));

                // Add the page number to the set
                pageNumbersSet.add(pageNumber);
            } while (cursor.moveToNext());
        }

        // Close the cursor and database
        cursor.close();
        db.close();

        // Build the comma-separated string from the set of unique page numbers
        StringBuilder pageNumbers = new StringBuilder();
        for (String pageNumber : pageNumbersSet) {
            pageNumbers.append(pageNumber).append(", ");
        }

        // Remove the trailing comma and space
        if (pageNumbers.length() > 2) {
            pageNumbers.setLength(pageNumbers.length() - 2);
        }

        return pageNumbers.toString();
    }
    public void navigateBack(View view) {
        onBackPressed(); // This will navigate to the previous screen
    }
}
