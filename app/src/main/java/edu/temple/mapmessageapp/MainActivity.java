package edu.temple.mapmessageapp;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {

    ServiceConnection sc;
    Intent i;
    Intent i2;
    PendingIntent pi;
    CertificateService certbind = new CertificateService();
    NfcAdapter nfc;
    SharedPreferences preferences;
    final String NAME_OF_USER = "aslfnaklrjgnogkgb;flkgmh";
    Button changemode;
    Boolean inkeymode = true;
    TextView displayText;
    TextView userText;
    Boolean isConnected = false;
    sendKeyCallback mainsender;
    String lastUserKeyExchanged;
    String name = "DEFAULT";
    KeyPair mykeypair;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        changemode = findViewById(R.id.changeModeButton);
        displayText = findViewById(R.id.decrypted);
        userText = findViewById(R.id.enterText);
        changemode.setOnClickListener(new KeyExchangeModeListener());
        i = new Intent(this, CertificateService.class);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i2 = new Intent(this, MainActivity.class);
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


        getApplicationContext().bindService(i, sc, Context.BIND_AUTO_CREATE);
        getApplicationContext().startService(i);

        mainsender = new sendKeyCallback();
        nfc.setNdefPushMessageCallback(mainsender, this);


    }

    @Override
    protected void onResume() {
        nfc.enableForegroundDispatch(this, pi, null, null);
        super.onResume();
    }

    @Override
    protected void onPause() {
        nfc.disableForegroundDispatch(this);
        super.onPause();
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
            TextView decryp = findViewById(R.id.decrypted);
            decryp.setText("KEY EXCHANGED");
            lastUserKeyExchanged = js.getString("user");

        //lastUserKeyExchanged = username;




    }

    public void getMessage(Intent intent) throws JSONException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException, KeyStoreException, NoSuchProviderException, IllegalBlockSizeException {
        String userdata = new String(((NdefMessage)intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)[0])
                .getRecords()[0]
                .getPayload());

        userdata = userdata.substring(3);
        JSONObject js = new JSONObject(userdata);
        String message = js.getString("message");
        message = decryptString(message);
        displayText.setText(message);


    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);


        if(inkeymode) {
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
        else
        {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
                try {
                    getMessage(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class sendKeyCallback implements NfcAdapter.CreateNdefMessageCallback
    {

        @Override
        public NdefMessage createNdefMessage(NfcEvent event) {
            Log.d("TAG", "SENDING NDEF");
            if(inkeymode) {
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
                    mykeyinfo.put("user", name);
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
            else if(lastUserKeyExchanged != null)
            {

                StringWriter sw = new StringWriter();
                String encodedString = userText.getText().toString();
                try {
                    encodedString = encryptString(encodedString, lastUserKeyExchanged);
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
                final JSONObject mykeyinfo = new JSONObject();
                try {
                    mykeyinfo.put("to", lastUserKeyExchanged);
                    mykeyinfo.put("from", name);
                    mykeyinfo.put("message", encodedString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String save = mykeyinfo.toString();
                final NdefRecord textrec = NdefRecord.createTextRecord(null, save);
                final NdefRecord apprec = NdefRecord.createApplicationRecord(getPackageName());
                final NdefMessage n = new NdefMessage(new NdefRecord[]{textrec, apprec});
                return n;
            }
            else
            {
                Log.d("TEST", "COULDN'T CREATE NDEF MESSAGE, PROBABLY NO LASTUSER");
                return null;
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



    public class KeyExchangeModeListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            inkeymode = !inkeymode;
            if(inkeymode == true)
            {
                changemode.setText("KEY EXCHANGE MODE");
            }
            else
            {
                changemode.setText("MESSAGE EXCHANGE MODE");
            }
        }
    }

    public void submitName(View v)
    {
        TextView textname = findViewById(R.id.editTextName);
        name = textname.getText().toString();

    }




}
