package com.example.silvee.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by silvee on 10.01.2018.
 */

public class QueryPreferences {
    private static final String PREFERENCES_QUERY = "preferencesQuery";

    public static String getPreferencesQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREFERENCES_QUERY, null);
    }

    public static void setPreferencesQuery(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREFERENCES_QUERY, query).apply();
    }
}
