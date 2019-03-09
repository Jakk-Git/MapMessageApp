package edu.temple.mapmessageapp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyPairGeneratorSpi;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class CertificateService extends Service {

    private static String STORE_NAME = "AndroidKeyStore";
    private static String KEY_PUBLIC = "thisiskeypublic";
    private static String KEY_PRIVATE = "thisisakeyprivate";
    private static String KEY_PAIR = "thisisakeypairyes";
    SharedPreferences preferences;

    KeyPairGenerator kpg;
    KeyPair mykp;
    KeyStore myks;

    {
        try {
            kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, STORE_NAME);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        try {
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
           myks = KeyStore.getInstance( STORE_NAME, STORE_NAME);
           myks.load(null);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }


        try {
            if(!(myks.containsAlias(KEY_PAIR)))
            {
                resetMyKeypair();
            }
            else
            {
                Certificate c = myks.getCertificate(KEY_PAIR);
                mykp = new KeyPair(c.getPublicKey(), (PrivateKey) myks.getKey(KEY_PAIR, null));
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();}
            catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        return new CertBinder();
    }

    public KeyPair getMyKeyPair()
    {
        return mykp;
    }

    void storePublicKey(String partnerName, String key) throws KeyStoreException {

        Log.d("TEST1", key);
        preferences.edit().putString(partnerName, key).commit();

    }

    RSAPublicKey getPublicKey(String partnerName) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, InvalidKeySpecException, UnsupportedEncodingException {
        String key = new String();
        key = preferences.getString(partnerName, "ERROR");
        byte[] bytelist = Base64.decode(key.getBytes("UTF-8"), 0);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytelist);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey finalkey = (RSAPublicKey)kf.generatePublic(spec);
        return(finalkey);
    }

    void resetMyKeypair() throws InvalidAlgorithmParameterException {
        KeyGenParameterSpec kgps = new KeyGenParameterSpec.Builder
                (KEY_PAIR, KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_SIGN)
                .setUserAuthenticationRequired(false)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(false)
                .build();
        kpg.initialize(kgps);
        mykp = kpg.generateKeyPair();
    }

    void resetKey(String partnername) throws KeyStoreException {
        myks.deleteEntry(partnername);
    }

    public class CertBinder extends Binder {

        CertificateService getService(){
            return CertificateService.this;
        }

    }

}
