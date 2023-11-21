package com.example.smogapp;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.link.LinkHandler;
import com.github.barteksc.pdfviewer.model.LinkTapEvent;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import androidx.appcompat.widget.SearchView;

public class PdfViewerActivity extends AppCompatActivity {

    public static final String PAGE_NUMBER_KEY = "page_number";
    public static final String PAGE_NUMBERS_KEY = "page_numbers";
    private SearchView searchView;
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        searchView = findViewById(R.id.searchView);
        setupSearchView();

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        PDFView pdfView = findViewById(R.id.pdfView);

        MyPdf myPdf = (MyPdf) getApplication();
        byte[] pdfByteArray = myPdf.getPdfByteArray();

        DefaultLinkHandler linkHandler = new DefaultLinkHandler(pdfView); // Create link handler instance

        int[] pageNumbers = getIntent().getIntArrayExtra(PAGE_NUMBERS_KEY);
        if (pageNumbers != null && pageNumbers.length > 0) {
            pdfView.fromBytes(pdfByteArray) // Use fromBytes to load PDF from byte array
                    .scrollHandle(new DefaultScrollHandle(this))
                    .spacing(8)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .pages(pageNumbers)
                    .enableAnnotationRendering(true)
                    .linkHandler(new LoggingLinkHandler(linkHandler)) // Set the link handler with logging
                    .load();
        }
    }
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query; // Save the search query

                // Log the query to check if it's captured correctly
                Log.d("SearchQuery", "Submitted query: " + searchQuery);

                openProtocolListActivity();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle text changes if needed
                return false;
            }
        });
    }

    private void openProtocolListActivity() {
        Intent intent = new Intent(PdfViewerActivity.this, ProtocolListActivity.class);
        intent.putExtra("searchQuery", searchQuery); // Pass the search query
        startActivity(intent);
    }

    public void navigateToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear back stack
        startActivity(intent);
    }

    private static class DefaultLinkHandler implements LinkHandler {

        private static final String TAG = DefaultLinkHandler.class.getSimpleName();
        private final PDFView pdfView;

        public DefaultLinkHandler(PDFView pdfView) {
            this.pdfView = pdfView;
        }

        @Override
        public void handleLinkEvent(LinkTapEvent event) {
            String uri = event.getLink().getUri();
            Integer page = event.getLink().getDestPageIdx();
            if (uri != null && !uri.isEmpty()) {
                handleUri(uri);
            } else if (page != null) {
                handlePage(page);
            }
        }

        @SuppressLint("QueryPermissionsNeeded")
        private void handleUri(String uri) {
            Uri parsedUri = Uri.parse(uri);
            Intent intent = new Intent(Intent.ACTION_VIEW, parsedUri);
            Context context = pdfView.getContext();
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Log.w(TAG, "No activity found for URI: " + uri);
            }
        }

        private void handlePage(int page) {
                openPdfViewerActivity(page);
                    }

        private void openPdfViewerActivity(int page) {
            // Create an Intent to open PdfViewerActivity
            Intent intent = new Intent(pdfView.getContext(), PdfViewerActivity.class);
            // Pass the page number to the intent
            intent.putExtra(PAGE_NUMBER_KEY, page);
            pdfView.getContext().startActivity(intent);
        }
    }

    private static class LoggingLinkHandler implements LinkHandler {

        private static final String TAG = LoggingLinkHandler.class.getSimpleName();
        private final LinkHandler originalHandler;

        public LoggingLinkHandler(LinkHandler originalHandler) {
            this.originalHandler = originalHandler;
        }

        @Override
        public void handleLinkEvent(LinkTapEvent event) {
            String uri = event.getLink().getUri();
            Integer page = event.getLink().getDestPageIdx();
            if (uri != null && !uri.isEmpty()) {
                Log.d(TAG, "Tapped URI link: " + uri);
            } else if (page != null) {
                Log.d(TAG, "Tapped page link: " + page);
            }
            // Call the original link handler to ensure normal behavior
            originalHandler.handleLinkEvent(event);
        }

    }
    public void navigateBack(View view) {
        onBackPressed(); // This will navigate to the previous screen
    }
}
