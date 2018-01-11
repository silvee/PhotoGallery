package com.example.silvee.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by silvee on 10.01.2018.
 */

public class QueryPreferences {
    private static final String PREFERENCES_QUERY = "preferencesQuery";
    private static final String PREFERENCES_POLL = "preferencesPoll";

    public static String getPreferencesQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREFERENCES_QUERY, null);
    }

    public static void setPreferencesQuery(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREFERENCES_QUERY, query).apply();
    }

    public static boolean getPreferencesPoll(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFERENCES_POLL, false);
    }

    public static void setPreferencesPoll(Context context, boolean pollEnabled) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREFERENCES_POLL, pollEnabled);
    }

}
