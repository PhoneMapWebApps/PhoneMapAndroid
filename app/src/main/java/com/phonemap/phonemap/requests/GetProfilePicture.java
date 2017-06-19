package com.phonemap.phonemap.requests;

import android.graphics.Bitmap;
import android.widget.ImageView;

import static com.phonemap.phonemap.constants.Requests.GET_PROFILE_PICTURE;
import static com.phonemap.phonemap.constants.Server.HTTP_URL;

public class GetProfilePicture implements  AsyncBitmapDownloadListener {
    private final ImageView view;

    public GetProfilePicture(ImageView view, int ownedID){
        this.view = view;
        new DownloadPicture(this).execute(HTTP_URL + GET_PROFILE_PICTURE + "/" + ownedID);
    }

    @Override
    public void onBitmapDownloaded(Bitmap bitmap) {
        view.setImageBitmap(bitmap);
    }
}
