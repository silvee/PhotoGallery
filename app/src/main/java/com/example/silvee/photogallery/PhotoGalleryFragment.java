package com.example.silvee.photogallery;

import android.content.Intent;
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
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    public static Fragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        new FetchDataAsyncTask(null).execute();


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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        MenuItem pollItem = menu.findItem(R.id.menu_item_toggle_polling);

        if (PollService.isServiceAlarmOn(getActivity())) {
            pollItem.setTitle(R.string.stop_polling);
        } else {
            pollItem.setTitle(R.string.start_polling);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                QueryPreferences.setPreferencesQuery(getActivity(), query);
                new FetchDataAsyncTask(query).execute();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setQuery(QueryPreferences.getPreferencesQuery(getActivity()),false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setPreferencesQuery(getActivity(), null);
                new FetchDataAsyncTask(null).execute();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean isServiceOn = PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), isServiceOn);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        String query;

        public FetchDataAsyncTask(String query) {
            this.query = query;
        }

        // Do in background thread
        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            Log.d(TAG, "IN BACKGROUND");

            if (query == null) {
                return new ImageFetcher().fetchRandomImages();
            } else {
                return new ImageFetcher().searchImages(query);
            }
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
