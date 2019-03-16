package edu.temple.mapmessageapp;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


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
    FragmentStatePagerAdapter fspa1;
    FragmentStatePagerAdapter fspa2;
    RecyclerFragment rf;
    ViewPager vp;
    ViewPager vp2;
    SupportMapFragment smf;
    FragmentManager fm = getSupportFragmentManager();

    ServiceConnection sc;
    Intent i1;
    Intent i2;
    PendingIntent pi;
    CertificateService certbind = new CertificateService();
    NfcAdapter nfc;
    Boolean isConnected = false;
    MapListActivity.sendKeyCallback mainsender;
    String lastUserKeyExchanged;
    Activity thisactivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);
        rf = new RecyclerFragment();
        smf = SupportMapFragment.newInstance();
        vp = findViewById(R.id.myviewPager);
        setUpFragments();
        getGoogleMapReady();
        thisactivity = this;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setUpMessagePassing();

        if (preferences.getString(NAME_OF_USER, "NOT_A_USERNAME").compareTo("NOT_A_USERNAME") == 0) {
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

        } else {
            hasusername = true;

        }


    }


    public void setUpFragments()
    {
        if(findViewById(R.id.framesecond) == null || getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
        {
           fspa1 = new FragmentStatePagerAdapter(fm) {
                @Override
                public Fragment getItem(int position) {
                    if(position == 0)
                    {
                        return rf;
                    }
                    else if(position == 1)
                    {
                        return smf;
                    }
                    else
                    {
                        return rf;
                    }
                }

                @Override
                public int getCount() {
                    return 2;
                }
            };
            vp.setAdapter(fspa1);
            vp.setCurrentItem(0);
        }
        else
        {
            fspa1 = new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) {
                return rf;
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
            fspa2 = new FragmentStatePagerAdapter(fm) {
                @Override
                public Fragment getItem(int position) {
                    return smf;
                }

                @Override
                public int getCount() {
                    return 1;
                }
            };


            vp.setAdapter(fspa1);
            vp2 = findViewById(R.id.framesecond);
            vp2.setAdapter(fspa2);
            vp.setCurrentItem(0);
            vp2.setCurrentItem(0);

        }
    }

    @Override
    protected void onPause() {
        nfc.disableForegroundDispatch(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //updatepeople.cancel();
    }

    @Override
    protected void onResume() {
        nfc.enableForegroundDispatch(this, pi, null, null);
        super.onResume();

    }

    public void sendUserDataToServer(String name) {
        final String MY_TOKEN = "asdasflfkjglsgm;lksdjrg044i";
        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());
        final Map<String, String> data = new HashMap<String, String>();
        data.put("user", name);
        data.put("latitude", Double.toString(location.getLatitude()));
        data.put("longitude", Double.toString(location.getLongitude()));


        StringRequest jor = new StringRequest(Request.Method.POST, "https://kamorris.com/lab/register_location.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("CHECKING", data.toString());
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                return data;
            }
        };

        final Map<String, String> firebaseinfo = new HashMap<>();
        firebaseinfo.put("user", name);
        firebaseinfo.put("token", FirebaseInstanceId.getInstance().getToken());


        StringRequest firebaseregister = new StringRequest(Request.Method.POST, "https://kamorris.com/lab/fcm_register.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String>getParams() throws AuthFailureError{
                return data;
            }
        };

        if(FirebaseInstanceId.getInstance().getToken() != null)
        {
            rq.add(firebaseregister);
        }
        else
        {
            Log.d("ERROR", "ERROR GETTING TOKEN");
        }
        rq.add(jor);
    }

    public void getGoogleMapReady() {
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        smf.getMapAsync(this);
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

    public void centerMap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            centerMap();
        } else {

            currentmap.setMyLocationEnabled(true);

            locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


            locationmanager.requestLocationUpdates(
                    locationmanager.getBestProvider(new Criteria(), false)
                    , 0, 10, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            updateLocation();
                            if (hasusername) {
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
            updateLocation();
            //sending user data to server
            if (hasusername) {
                sendUserDataToServer((preferences.getString(NAME_OF_USER, "NOT_A_USERNAME")));
            }
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            LatLng coordinate = new LatLng(lat, lng);

            CameraUpdate zoom = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
            currentmap.animateCamera(zoom);
            createTimer();
        }
    }

    public void updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = locationmanager.getLastKnownLocation(locationmanager.getBestProvider(new Criteria(), false));
        if(location == null)
        {
            location = locationmanager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
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
                Log.d("TAGGED", "GOT TO UPDATERECYCLER");
                RecyclerView recycletemp = findViewById(R.id.myrecycler);
                rf.setReyclerView(recycletemp);
                if(rf.getRecyclerView() != null)
                {
                    PersonListAdapter personList = new PersonListAdapter(partnerlist, thisactivity);
                    LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
                    rf.getRecyclerView().setAdapter(personList);
                    rf.getRecyclerView().setLayoutManager(llm);
                    rf.getRecyclerView().setHasFixedSize(true);
                }

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

    public void setUpMessagePassing()
    {
        i1 = new Intent(this, CertificateService.class);
        i1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i2 = new Intent(this, MapListActivity.class);
        i2.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pi = PendingIntent.getActivity(this, 0, i2, 0);
        nfc = NfcAdapter.getDefaultAdapter(this);
        sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                certbind = ((CertificateService.CertBinder) service).getService();
                Log.d("FLOOP", "CONNECTED");
                isConnected = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                certbind = null;
            }
        };


        getApplicationContext().bindService(i1, sc, Context.BIND_AUTO_CREATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i1);
        }
        else {
            startService(i1);
        }
        mainsender = new MapListActivity.sendKeyCallback();
        nfc.setNdefPushMessageCallback(mainsender, this);
    }

    public void startKeyExchange(Intent intent) throws JSONException, IOException, FormatException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, InvalidKeySpecException {
        Log.d("TAG", "KEY EXCHANGE STARTED");

        String userdata = new String(((NdefMessage)intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)[0])
                .getRecords()[0]
                .getPayload());

        userdata = userdata.substring(3);
        JSONObject js = new JSONObject(userdata);

        String key = js.getString("key");
        key = key.replace("-----BEGIN PUBLIC KEY-----", "");
        key = key.replace("\n-----END PUBLIC KEY-----", "");
        Log.d("TAG", key);
        certbind.storePublicKey(js.getString("user"), key);
        lastUserKeyExchanged = js.getString("user");
        Toast.makeText(this, "Got user key!", Toast.LENGTH_SHORT).show();

        //lastUserKeyExchanged = username;




    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {


                try {
                    startKeyExchange(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (FormatException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
            }
    }

    public class sendKeyCallback implements NfcAdapter.CreateNdefMessageCallback
    {

        @Override
        public NdefMessage createNdefMessage(NfcEvent event) {
            if(hasusername == true) {
                Log.d("TAG", "SENDING NDEF");
                Log.d("TAG", "SENDING NDEF 2");
                X509EncodedKeySpec spec = null;
                String keystring = null;
                try {
                    KeyFactory fact = KeyFactory.getInstance("RSA");
                    Log.d("TAG", "SENDING NDEF 3");
                    spec = fact.getKeySpec(certbind.getMyKeyPair().getPublic(), X509EncodedKeySpec.class);
                    Log.d("TAG", "SENDING NDEF 4");
                    keystring = Base64.encodeToString(spec.getEncoded(), Base64.DEFAULT);

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }

                String encodedString = String.format("-----BEGIN PUBLIC KEY-----%s-----END PUBLIC KEY-----", keystring);


                final JSONObject mykeyinfo = new JSONObject();
                try {
                    mykeyinfo.put("user", preferences.getString(NAME_OF_USER, "NOT_A_USERNAME"));
                    mykeyinfo.put("key", encodedString);
                } catch (JSONException e) {

                    e.printStackTrace();
                }
                Log.d("TAG", mykeyinfo.toString());
                String save = mykeyinfo.toString();
                final NdefRecord textrec = NdefRecord.createTextRecord(null, save);
                //mykeyinfo.toString());
                final NdefRecord apprec = NdefRecord.createApplicationRecord(getPackageName());
                final NdefMessage n = new NdefMessage(new NdefRecord[]{textrec, apprec});
                Log.d("TAG", "SENT");

                return n;
            }
            else
            {
                return null;
            }
        }
    }

}
