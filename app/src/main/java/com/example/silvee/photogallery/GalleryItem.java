package com.example.silvee.photogallery;

import java.net.URL;

/**
 * Created by silvee on 08.01.2018.
 */

public class GalleryItem {
    private String id;
    private String title;
    private String urlString;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }
}
