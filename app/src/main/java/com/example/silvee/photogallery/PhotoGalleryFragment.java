package com.example.silvee.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public Fragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchDataAsyncTask().execute();
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

    private void setupAdapter() {
        if (isAdded()) {
            recyclerView.setAdapter(new PhotoAdapter(galleryItems));
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;

        
        public PhotoHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView;
        }

        public void bindItem(GalleryItem item) {
            titleTextView.setText(item.getTitle());
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> items;

        public PhotoAdapter(List<GalleryItem> items) {
            this.items = items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PhotoHolder(new TextView(getActivity()));
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = items.get(position);
            holder.bindItem(galleryItem);
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
