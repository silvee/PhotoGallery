package com.example.silvee.photogallery;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PhotoGalleryActivity extends AppCompatActivity {
    Fragment galleryFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        galleryFragment = createFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, galleryFragment).commit();
    }

    private Fragment createFragment() {
        return new PhotoGalleryFragment();
    }
}
