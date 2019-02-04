package edu.temple.mapmessageapp;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {

    ServiceConnection sc;
    Intent i;
    CertificateService certbind = new CertificateService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                certbind = ((CertificateService.CertBinder) service).getService();
                Log.d("FLOOP", "CONNECTED");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                certbind = null;
            }
        };
        i = new Intent(this, CertificateService.class);
        getApplicationContext().bindService(i, sc, Context.BIND_AUTO_CREATE);
        getApplicationContext().startService(i);


    }

    public void buttonClickEncrypt(View v) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        TextView input = findViewById(R.id.enterText);
        String s = input.getText().toString();
        Cipher cip = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        cip.init(Cipher.ENCRYPT_MODE, certbind.getMyKeyPair().getPublic());
        byte[] e = cip.doFinal(s.getBytes("UTF-8"));
        String crypted = Base64.encodeToString(e, 0);
        TextView output = findViewById(R.id.encrypted);
        output.setText(crypted);
    }

    public void buttonClickDecrypt(View v) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, UnsupportedEncodingException {
        TextView input = findViewById(R.id.encrypted);
        String s = (String)input.getText();
        Cipher cip2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cip2.init(Cipher.DECRYPT_MODE, certbind.getMyKeyPair().getPrivate());
        byte[] e = cip2.doFinal(Base64.decode(s.getBytes("UTF-8"), 0));
        String crypted = new String(e);
        TextView output = findViewById(R.id.decrypted);
        output.setText(crypted);

    }




}
