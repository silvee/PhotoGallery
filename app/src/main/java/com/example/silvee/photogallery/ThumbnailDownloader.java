package com.example.silvee.photogallery;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by silvee on 09.01.2018.
 */

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0; //handler message for downloading

    private boolean hasQuited = false;
    private Handler requestHandler;
    private ConcurrentMap<T, String> requestMap = new ConcurrentHashMap<>();

    public ThumbnailDownloader() {
        super(TAG);
    }

    @Override
    public boolean quit() {
        hasQuited = true;
        return super.quit();
    }

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);
        if (url == null) {
            requestMap.remove(target);
        } else {
            requestMap.put(target, url);
            requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }
    }
}
