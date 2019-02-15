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
    Location thislocation;

    public Partner(JSONObject myobject, Location mylocation) throws JSONException {
        name = myobject.getString("username");
        latitude = myobject.getDouble("latitude");
        longitude = myobject.getDouble("longitude");
        this.mylocation = mylocation;
        this.thislocation = new Location("thisLocation");
        this.thislocation.setLatitude(latitude);
        this.thislocation.setLongitude(longitude);
    }

    @Override
    public int compareTo(@NonNull Object o) {
        Partner p = (Partner)o;
        Location thatlocation = new Location("thatLocation");
        thatlocation.setLongitude(p.longitude);
        thatlocation.setLatitude(p.latitude);
        if(Math.abs(mylocation.distanceTo(thislocation)) > Math.abs(mylocation.distanceTo(thatlocation)))
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
