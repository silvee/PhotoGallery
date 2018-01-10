package com.example.silvee.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by silvee on 08.01.2018.
 */

public class PhotoGalleryFragment extends Fragment {
    public static final String TAG = "PhotoGalleryFragment";

    private RecyclerView recyclerView;
    private List<GalleryItem> galleryItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> thumbnailDownloader;

    public Fragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchDataAsyncTask().execute();

        Handler responseHandler = new Handler(); // handler in main thread
        thumbnailDownloader = new ThumbnailDownloader<>(responseHandler);

        thumbnailDownloader.setThumbnailDownloaderListener(new ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                target.bindItem(drawable);
            }
        });

        thumbnailDownloader.start();
        thumbnailDownloader.getLooper();
        Log.i(TAG, "thumbnailDownloader thread started");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        recyclerView = view.findViewById(R.id.photo_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        setupAdapter();
        return view;
    }

    // Clear message queue if fragment destroyed
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        thumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        thumbnailDownloader.quit();
        Log.i(TAG, "thumbnailDownloader thread finished");
    }

    private void setupAdapter() {
        if (isAdded()) {
            recyclerView.setAdapter(new PhotoAdapter(galleryItems));
        }
    }

    // ViewHolder
    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        
        public PhotoHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image_view);
        }

        public void bindItem(Drawable drawable) {
            imageView.setImageDrawable(drawable);
        }
    }

    // Adapter
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> items;

        public PhotoAdapter(List<GalleryItem> items) {
            this.items = items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = items.get(position);
            Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher_foreground);
            holder.bindItem(drawable);
            thumbnailDownloader.queueThumbnail(holder, galleryItem.getUrlString());

        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private class FetchDataAsyncTask extends AsyncTask<Void, Void, List<GalleryItem>> {

        // Do in background thread
        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            Log.d(TAG, "IN BACKGROUND");
            return new ImageFetcher().fetchItems();
        }

        // Do in main thread
        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            galleryItems = items;
            Log.d(TAG, "ON POST EXECUTE");
            setupAdapter();
        }
    }
}
