package edu.temple.mapmessageapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ChatActivity extends AppCompatActivity {


    private final String CHAT_APP_MESSAGE_ACTION = "ksljtfndgrlk3j54hn6k3j4n5trflkdjfg";
    final String NAME_OF_USER = "aslfnaklrjgnogkgb;flkgmh";
    Intent i;
    ServiceConnection sc;
    CertificateService certbind;
    boolean isConnected;
    String partnername;
    RecyclerView thisrecycler;
    List<Message> messageList;
    Button sendmessagebut;
    TextView sendmessagetext;
    MessageReceiver mr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        partnername = getIntent().getExtras().getString("username");
        Log.d("PARTNERNAME", partnername);
        messageList = new ArrayList<Message>();
        thisrecycler = findViewById(R.id.messageRecycler);
        sendmessagebut = findViewById(R.id.messageButton);
        sendmessagetext = findViewById(R.id.messageText);
        setUpButton(sendmessagebut);
        mr = new MessageReceiver();
        i = new Intent(this, CertificateService.class);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
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


        getApplicationContext().bindService(i, sc, Context.BIND_AUTO_CREATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i);
        }
        else {
            startService(i);
        }

        updateRecycler();

    }

    @Override
    protected void onResume() {
        IntentFilter intfilt = new IntentFilter();
        intfilt.addAction(CHAT_APP_MESSAGE_ACTION);
        this.registerReceiver(mr, intfilt);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mr);
        super.onPause();
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TEST", "IN ONRECEIVE");
                try {
                    String messageinjson = intent.getExtras().getString("message");
                    JSONObject js = new JSONObject(messageinjson);
                    if(js.getString("from").equals(partnername)) {
                        String finalmessage = js.getString("message");
                        finalmessage = finalmessage.replace("==", "");


                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-4:00"));
                        Date currentLocalTime = cal.getTime();
                        DateFormat date = new SimpleDateFormat("HH:mm");
                        date.setTimeZone(TimeZone.getTimeZone("GMT-4:00"));
                        String localTime = date.format(currentLocalTime);


                        messageList.add(new Message("" + decryptString(finalmessage) + " - " +
                        localTime + " - " + partnername, false));
                        updateRecycler();
                    }
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

        }

    }

    public String encryptString(String s, String username) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException, UnsupportedEncodingException, UnrecoverableKeyException, KeyStoreException, InvalidKeySpecException {
        Cipher cip = Cipher.getInstance("RSA/ECB/NoPadding");
        cip.init(Cipher.ENCRYPT_MODE, certbind.getPublicKey(username));
        byte[] e = cip.doFinal(s.getBytes("UTF-8"));
        String crypted = Base64.encodeToString(e, 0);
        return crypted;
    }

    public String decryptString(String s) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, UnsupportedEncodingException, UnrecoverableKeyException, KeyStoreException {
        Cipher cip2 = Cipher.getInstance("RSA/ECB/NoPadding");
        cip2.init(Cipher.DECRYPT_MODE, certbind.getMyKeyPair().getPrivate());
        byte[] e = cip2.doFinal(Base64.decode(s.getBytes("UTF-8"), 0));
        String uncrypted = new String(e);
        return uncrypted;
    }

    public void updateRecycler()
    {
        ChatListAdapter chatList = new ChatListAdapter(messageList.toArray(new Message[messageList.size()]), this);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        thisrecycler.setAdapter(chatList);
        thisrecycler.setLayoutManager(llm);
        thisrecycler.setHasFixedSize(true);
    }

    public void setUpButton(Button b)
    {
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                RequestQueue rq = Volley.newRequestQueue(getApplicationContext());
                final Map<String, String> data = new HashMap<String, String>();
                try {
                    data.put("user", PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(NAME_OF_USER, "no name"));
                    data.put("partneruser", partnername);
                    data.put("message", encryptString(sendmessagetext.getText().toString(), partnername));
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }

                Log.d("INFO", data.toString());


                StringRequest jor = new StringRequest(Request.Method.POST, "https://kamorris.com/lab/send_message.php", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("CHECKING", response.toString());
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
                rq.add(jor);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messageList.add(new Message(sendmessagetext.getText().toString(), true));
                        updateRecycler();
                    }
                });


                Log.d("TEST", "ONCLICK FINISHED");

            }
        });
    }
}
