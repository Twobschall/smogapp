package com.example.smogapp;


import android.app.Application;
import android.util.Log;

public class MyPdf extends Application {
    private byte[] pdfByteArray;

    public byte[] getPdfByteArray() {
        Log.d("MyPdf", "Getting pdfByteArray: " + (pdfByteArray != null ? pdfByteArray.length : "null"));
        return pdfByteArray;
    }

    public void setPdfByteArray(byte[] pdfByteArray) {
        this.pdfByteArray = pdfByteArray;
        Log.d("MyPdf", "Setting pdfByteArray: " + (pdfByteArray != null ? pdfByteArray.length : "null"));
    }
}
