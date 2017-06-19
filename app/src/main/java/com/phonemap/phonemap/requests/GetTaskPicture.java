package com.phonemap.phonemap.requests;

import android.graphics.Bitmap;
import android.widget.ImageView;

import static com.phonemap.phonemap.constants.Requests.GET_TASK_PICTURE;
import static com.phonemap.phonemap.constants.Server.HTTP_URL;

public class GetTaskPicture implements  AsyncBitmapDownloadListener {
    private final ImageView view;

    public GetTaskPicture(ImageView view, int taskID){
        this.view = view;
        new DownloadPicture(this).execute(HTTP_URL + GET_TASK_PICTURE + "/" + taskID);
    }

    @Override
    public void onBitmapDownloaded(Bitmap bitmap) {
        if (bitmap != null) {
            view.setImageBitmap(bitmap);
        }
    }
}
