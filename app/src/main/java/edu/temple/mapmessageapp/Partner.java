package edu.temple.mapmessageapp;

import android.location.Location;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class Partner implements Comparable {

    String name;
    double latitude;
    double longitude;
    Location mylocation;

    public Partner(JSONObject myobject, Location mylocation) throws JSONException {
        name = myobject.getString("username");
        latitude = myobject.getDouble("latitude");
        longitude = myobject.getDouble("longitude");
        this.mylocation = mylocation;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        Partner p = (Partner)o;
        if(Math.abs(getTotalLatLong(latitude, longitude) - getTotalLatLong(mylocation.getLatitude(), mylocation.getLongitude()))
            > Math.abs(Math.abs(getTotalLatLong(p.latitude, p.longitude) - getTotalLatLong(mylocation.getLatitude(), mylocation.getLongitude()))))
        {
            return 1;
        }
        else
        {
            return -1;
        }

    }

    public double getTotalLatLong(double lat, double lng)
    {
        return(Math.abs(lat) + Math.abs(lng));
    }
}
