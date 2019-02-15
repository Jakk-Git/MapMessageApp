package edu.temple.mapmessageapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MapListActivity extends AppCompatActivity implements OnMapReadyCallback {


    GoogleMap currentmap;
    LocationManager locationmanager;
    SharedPreferences preferences;
    Location location;
    JSONArray peoplelist;
    Timer updatepeople = new Timer();
    TimerTask timerupdate;
    final String NAME_OF_USER = "aslfnaklrjgnogkgb;flkgmh";
    Boolean hasusername = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);
        getGoogleMapReady();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(preferences.getString(NAME_OF_USER, "NOT_A_USERNAME").compareTo("NOT_A_USERNAME") == 0)
        {
            final Button submitname = findViewById(R.id.submitname);
            submitname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    TextView entername = findViewById(R.id.enternametext);
                    preferences.edit().putString(NAME_OF_USER, entername.getText().toString()).commit();
                    Log.d("LOOK", entername.getText().toString());
                    sendUserDataToServer(entername.getText().toString());
                    submitname.setClickable(false);
                    hasusername = true;


                }
            });

        }
        else
        {
            hasusername = true;

        }






    }

    @Override
    protected void onDestroy() {
        timerupdate.cancel();
        super.onDestroy();
    }

    public void sendUserDataToServer(String name)
    {
        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());
        final Map<String, String> data = new HashMap<String, String>();
        data.put("user", name);
        data.put("latitude", Double.toString(location.getLatitude()));
        data.put("longitude", Double.toString(location.getLongitude()));





        StringRequest jor = new StringRequest(Request.Method.POST,"https://kamorris.com/lab/register_location.php",  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), "SENT, RESPONSE FROM SERVER: " + response, Toast.LENGTH_LONG).show();
                Log.d("CHECKING", data.toString());
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR", error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                return data;
            }
        };


        rq.add(jor);
    }

    public void getGoogleMapReady()
    {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        currentmap = googleMap;
        currentmap.getUiSettings().setMyLocationButtonEnabled(false);
        centerMap();

    }


//    public class ReceiveProximityChanger extends BroadcastReceiver
//    {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//           String key = LocationManager.KEY_PROXIMITY_ENTERING;
//           Boolean entering = intent.getBooleanExtra(key, false);
//            if (!entering) {
//                updateLocation();
//                locationmanager.removeProximityAlert(mainpend);
//                locationmanager.addProximityAlert(location.getLatitude(), location.getLongitude(), 10, -1, mainpend);
//                if(hasusername) {
//                    sendUserDataToServer(preferences.getString(NAME_OF_USER, "NOT_A_USERNAME"));
//                }
//            }
//
//        }
//    }

    public void centerMap()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            centerMap();
        }
        else
        {

            currentmap.setMyLocationEnabled(true);
            locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            updateLocation();
            //sending user data to server
            if(hasusername) {
                sendUserDataToServer((preferences.getString(NAME_OF_USER, "NOT_A_USERNAME")));
            }
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            locationmanager.requestLocationUpdates(
                    locationmanager.getBestProvider(new Criteria(), false)
                    , 0, 10, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    updateLocation();
                    if(hasusername) {
                        sendUserDataToServer(preferences.getString(NAME_OF_USER, "NOT_A_USERNAME"));
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    //do nothing
                }

                @Override
                public void onProviderEnabled(String provider) {
                    //do nothing
                }

                @Override
                public void onProviderDisabled(String provider) {
                    //do nothing
                }
            });
            LatLng coordinate = new LatLng(lat, lng);

            CameraUpdate zoom = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
            currentmap.animateCamera(zoom);
            createTimer();
        }
    }

    public void updateLocation()
    {
        location = locationmanager.getLastKnownLocation(locationmanager.getBestProvider(new Criteria(), false));
    }

    public void createTimer()
    {
        timerupdate = new TimerTask() {

            Partner[] partnerlist;
            String url = "https://kamorris.com/lab/get_locations.php";

            public void updateRecyclerView() throws JSONException {
                 partnerlist = new Partner[peoplelist.length()];
                 updateLocation();
                for(int i = 0; i < peoplelist.length(); i++)
                {
                        partnerlist[i] = new Partner(peoplelist.getJSONObject(i), location);

                }
                Arrays.sort(partnerlist);
                RecyclerView rv = findViewById(R.id.recyclerView2);
                PersonListAdapter personList = new PersonListAdapter(partnerlist, getApplicationContext());
                LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
                rv.setAdapter(personList);
                rv.setLayoutManager(llm);
                rv.hasFixedSize();
            }

            public void updateMap()
            {
                currentmap.clear();
                for(Partner p : partnerlist)
                {
                        MarkerOptions mo = new MarkerOptions();
                        mo.position(new LatLng(p.latitude, p.longitude));
                        mo.title(p.name);
                        currentmap.addMarker(mo);

                }

            }


            @Override
            public void run() {
                    RequestQueue rq = Volley.newRequestQueue(getApplicationContext());
                    JsonArrayRequest jar = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            peoplelist = response;
                            boolean changedbool = false;
                            int uservalue = 0;
                            for(int e = 0; e < peoplelist.length(); e++)
                            {
                                try {
                                    if(peoplelist.getJSONObject(e).getString("username").compareTo((preferences.getString(NAME_OF_USER, "NOT_A_USERNAME"))) == 0)
                                    {
                                        changedbool = true;
                                        uservalue = e;
                                    }
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                            if(changedbool == true) {
                                peoplelist.remove(uservalue);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        updateRecyclerView();
                                        updateMap();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error

                        }
                    });
                    rq.add(jar);
                }

        };
        updatepeople.schedule(timerupdate, 0, 30000);
    }

}
