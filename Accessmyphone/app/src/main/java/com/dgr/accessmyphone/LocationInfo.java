package com.dgr.accessmyphone;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

public class LocationInfo {

    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";

     //Stores the location updates state in SharedPreferences.

    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    //Recturns the location as a string

    static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                location.getLatitude() + ", " + location.getLongitude();
    }
}

