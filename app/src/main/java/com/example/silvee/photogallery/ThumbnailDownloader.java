package com.example.silvee.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
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
    private Handler responseHandler; // link the the Handler in main thread
    private ConcurrentMap<T, String> requestMap = new ConcurrentHashMap<>();
    private ThumbnailDownloaderListener<T> thumbnailDownloaderListener;

    public interface ThumbnailDownloaderListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloaderListener(ThumbnailDownloaderListener<T> listener) {
        thumbnailDownloaderListener = listener;
    }

    // Constructor
    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        this.responseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        requestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + requestMap.
                            get(target));
                    handleRequest(target);
                }
            }
        };
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

    public void clearQueue() {
        requestHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }

    private void handleRequest(final T target) {
        try {
            final String url = requestMap.get(target);
            if (url == null) {
                return;
            }
            byte[] bitmapBytes = new ImageFetcher().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory
                    .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");

            // send task for updating UI to main thread
            responseHandler.post(new Runnable() {
                    public void run() {
                        if (requestMap.get(target) != url ||
                                hasQuited) {
                            return;
                        }
                        requestMap.remove(target);
                        thumbnailDownloaderListener.onThumbnailDownloaded(target,
                                bitmap);
                    }
                }
            );
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }
}
