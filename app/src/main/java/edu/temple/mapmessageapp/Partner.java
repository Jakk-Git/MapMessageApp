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
    }

    @Override
    public int compareTo(@NonNull Object o) {
        Partner p = (Partner)o;
        float[] thisdistance = new float[10];
        float[] thatdistance = new float[10];
        mylocation.distanceBetween(mylocation.getLatitude(), mylocation.getLongitude(), latitude, longitude, thisdistance);
        mylocation.distanceBetween(mylocation.getLatitude(), mylocation.getLongitude(), p.latitude, p.longitude, thatdistance);
        if( thisdistance[0] > thatdistance[0])
        {
            return 1;
        }
        else if(thisdistance[0] == thatdistance[0])
        {
            return 0;
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
