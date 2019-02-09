package edu.temple.mapmessageapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;

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
    final String NAME_BOOL = "aslfnaklrjgnogkgb;flkgmh";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);
        getGoogleMapReady();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(preferences.getBoolean(NAME_BOOL, false) == false)
        {
            Button submitname = findViewById(R.id.submitname);
            submitname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView entername = findViewById(R.id.enternametext);

                }
            });
        }
        createTimer();





    }

    @Override
    protected void onDestroy() {
        timerupdate.cancel();
        super.onDestroy();
    }

    public void submitName()
    {

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
            Criteria criteria = new Criteria();
            locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            String provider = locationmanager.getBestProvider(criteria, false);
            location = locationmanager.getLastKnownLocation(provider);
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            LatLng coordinate = new LatLng(lat, lng);

            CameraUpdate zoom = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
            currentmap.animateCamera(zoom);
        }
    }

    public void createTimer()
    {
        timerupdate = new TimerTask() {
            public void updateRecyclerView()
            {
                RecyclerView rv = findViewById(R.id.recyclerView2);
                PersonListAdapter personList = new PersonListAdapter(peoplelist, getApplicationContext());
                LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
                rv.setAdapter(personList);
                rv.setLayoutManager(llm);
                rv.hasFixedSize();
            }

            String url = "https://kamorris.com/lab/get_locations.php";
            @Override
            public void run() {
                    RequestQueue rq = Volley.newRequestQueue(getApplicationContext());
                    JsonArrayRequest jar = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            peoplelist = response;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateRecyclerView();
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
