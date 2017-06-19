package com.phonemap.phonemap.requests;


import android.graphics.Bitmap;

interface AsyncBitmapDownloadListener {
    void onBitmapDownloaded(Bitmap bitmap);
}
