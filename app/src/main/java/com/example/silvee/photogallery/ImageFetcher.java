package com.example.silvee.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by silvee on 08.01.2018.
 */

public class ImageFetcher {
    private static final String TAG = "FlickrFetchr";
    private static final String CLIENT_ID = "52ffd721097c4a0";

    private static final String URL_SEARCH = "https://api.imgur.com/3/gallery/search/";
    private static final String URL_RANDOM = "https://api.imgur.com/3/gallery/random/random/";

    private static final Uri ENDPOINT = Uri
            .parse("https://api.imgur.com/3/gallery/")
            .buildUpon()
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    public byte[] getUrlBytes(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlString);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String url) throws IOException {
        return new String(getUrlBytes(url));
    }

    private String buildUrl(String method, String query) {
        String url = method;

        if (method.equals(URL_SEARCH)) {
            url = Uri.parse(method)
                   .buildUpon()
                   .appendQueryParameter("q", query)
                   .appendQueryParameter("client_id", CLIENT_ID)
                   .build().toString();
        }

        if (method.equals(URL_RANDOM)) {
            url = Uri.parse(method)
                    .buildUpon()
                    .appendQueryParameter("client_id", CLIENT_ID)
                    .build().toString();
        }
        return url;
    }

    public List<GalleryItem> fetchRandomImages() {
        String url = buildUrl(URL_RANDOM, null);
        return fetchItems(url);
    }
    public List<GalleryItem> searchImages(String query) {
        String url = buildUrl(URL_SEARCH, query);
        return fetchItems(url);
    }

    private List<GalleryItem> fetchItems(String url) {
        List<GalleryItem> items = new ArrayList<>();

        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            Log.i(TAG, "JSONString length: " + jsonString.length());
            JSONObject jsonObject = new JSONObject(jsonString);
            parseItems(items, jsonObject);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to create JSONObject", je);
        }
        return items;
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody)
            throws IOException, JSONException {
        JSONArray itemJsonArray = jsonBody.getJSONArray("data");
        Log.d(TAG, "photoJsonArray.length = " + itemJsonArray.length());
        for (int i = 0; i < itemJsonArray.length(); i++) {
            Log.d(TAG, "item i = " + i);
            JSONObject itemJsonObject = itemJsonArray.getJSONObject(i);

            if (itemJsonObject.getBoolean("is_album")) {
                JSONArray imageJsonArray = itemJsonObject.getJSONArray("images");
                Log.d(TAG, "images count = " + itemJsonObject.getInt("images_count"));
                Log.d(TAG, "imagejsonarray size = " + imageJsonArray.length());
                for (int j = 0; j < imageJsonArray.length(); j++) {
                    Log.d(TAG, "image of album j = " + j);
                    JSONObject imageJsonObject = imageJsonArray.getJSONObject(j);
                    GalleryItem item = new GalleryItem();
                    item.setId(imageJsonObject.getString("id"));
                    item.setTitle(itemJsonObject.getString("title"));
                    item.setUrlString(imageJsonObject.getString("link"));
                    items.add(item);
                    Log.d(TAG, "image added: " + item.getId() + " " + item.getTitle());
                }
            } else {
                GalleryItem item = new GalleryItem();
                item.setId(itemJsonObject.getString("id"));
                item.setTitle(itemJsonObject.getString("title"));
                item.setUrlString(itemJsonObject.getString("link"));
                items.add(item);
                Log.d(TAG, "image added: " + item.getId() + " " + item.getTitle());
            }

        }
    }
}
