package com.example.smogapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean isSublistShown = false;
    private String selectedMainItem;

    private DatabaseHandler databaseHandler;
    private DatabaseHelper databaseHelper;

    private SearchView searchView;
    private String searchQuery = "";

    private List<String> mainListItems; // Store main list items
    private byte[] pdfByteArray; // Store PDF as byte array

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDatabase();
        initPdfByteArray(); // Load PDF as byte array
        initViews();
        setupSearchView();
        setupToolbar();

        // Load and cache the PDF byte array if not already cached
        if (pdfByteArray == null) {
            pdfByteArray = loadPdfFileAsByteArray();
        }
    }

    private void initDatabase() {
        databaseHandler = DatabaseHandler.getInstance(this);
        databaseHelper = new DatabaseHelper(this);

        if (!databaseHandler.isDatabaseInitialized()) {
            databaseHelper.getWritableDatabase(); // Triggers database creation if not done
            databaseHandler.insertDataFromCSV(this);
            databaseHandler.setDatabaseInitialized();
        }

        mainListItems = databaseHandler.getMainListItems(); // Fetch main list items once
    }

    private void initPdfByteArray() {
        pdfByteArray = loadPdfFileAsByteArray();
    }

    private byte[] loadPdfFileAsByteArray() {
        try {
            InputStream inputStream = getAssets().open("smog.pdf");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            inputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initViews() {
        RecyclerView mainRecyclerView = findViewById(R.id.mainRecyclerView);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        MainListAdapter mainListAdapter = new MainListAdapter(mainListItems.toArray(new String[0]), this::onMainItemClick);
        mainRecyclerView.setAdapter(mainListAdapter);

        searchView = findViewById(R.id.searchView); // Use androidx.appcompat.widget.SearchView
    }


    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                openProtocolListActivity();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void openProtocolListActivity() {
        MyPdf myPdf = (MyPdf) getApplication();
        myPdf.setPdfByteArray(pdfByteArray);
        Intent intent = new Intent(MainActivity.this, ProtocolListActivity.class);
        intent.putExtra("searchQuery", searchQuery);
        startActivity(intent);
    }

    public void navigateToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear back stack
        startActivity(intent);
    }

    private void showSublist(String[] sublist) {
        RecyclerView sublistRecyclerView = findViewById(R.id.sublistRecyclerView);
        sublistRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        SubListAdapter sublistAdapter = new SubListAdapter(sublist, this::onSublistItemClick);
        sublistRecyclerView.setAdapter(sublistAdapter);

        findViewById(R.id.mainRecyclerView).setVisibility(View.GONE);
        sublistRecyclerView.setVisibility(View.VISIBLE);

        isSublistShown = true;
    }

    private void hideSublist() {
        findViewById(R.id.mainRecyclerView).setVisibility(View.VISIBLE);
        findViewById(R.id.sublistRecyclerView).setVisibility(View.GONE);

        isSublistShown = false;
    }

    private void onSublistItemClick(int position) {
        String clickedSublistItem;
        String mainListItem = getSelectedMainItem();
        List<String> sublist = databaseHandler.getSublistForMainItem(mainListItem);

        if (sublist != null && position >= 0 && position < sublist.size()) {
            clickedSublistItem = sublist.get(position);
            String pageNumberOrRange = databaseHandler.getPageNumberFromDatabase(clickedSublistItem);

            MyPdf myPdf = (MyPdf) getApplication();
            myPdf.setPdfByteArray(pdfByteArray);

            if (pageNumberOrRange.isEmpty()) {
                // Handle the case where pageNumberOrRange is empty
                // ...
            } else {
                String[] pageNumbersArray = pageNumberOrRange.replace(" ", ",").split(",");
                int[] pageNumbers = new int[pageNumbersArray.length];

                for (int i = 0; i < pageNumbersArray.length; i++) {
                    String trimmedValue = pageNumbersArray[i].trim();
                    pageNumbers[i] = Integer.parseInt(trimmedValue);
                }

                Intent intent = new Intent(this, PdfViewerActivity.class);
                intent.putExtra(PdfViewerActivity.PAGE_NUMBERS_KEY, pageNumbers);
                startActivity(intent);
            }
        }
    }

    private String getSelectedMainItem() {
        return selectedMainItem;
    }

    private void onMainItemClick(int position) {
        if (position >= 0 && position < mainListItems.size()) {
            String clickedItem = mainListItems.get(position);
            selectedMainItem = clickedItem;
            List<String> sublist = databaseHandler.getSublistForMainItem(clickedItem);

            if (sublist != null) {
                showSublist(sublist.toArray(new String[0]));
            } else {
                hideSublist();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHandler.closeDatabase();
    }

    public void navigateBack(View view) {
        if (isSublistShown) {
            hideSublist();
        }
    }
}
