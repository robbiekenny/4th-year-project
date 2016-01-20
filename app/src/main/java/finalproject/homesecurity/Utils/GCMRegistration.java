package finalproject.homesecurity.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;

import finalproject.homesecurity.Constants;
import finalproject.homesecurity.RegisterClient;

/**
 * Created by Robbie on 20/01/2016.
 */
/*
CLASS CONTAINING THE METHOD THAT INVOKES GCM REGISTRATION
 */
public class GCMRegistration {

    public GCMRegistration(){}

    public void registerClientForGCM(final RegisterClient registerClient,String email,
                                     final Context context,final GoogleCloudMessaging gcm) throws UnsupportedEncodingException {
        registerClient.setUserID(email);


        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    String regid = gcm.register(Constants.SENDER_ID);
                    System.out.println("GCM ID: " + regid);
                    registerClient.register(regid, new HashSet<String>());
                } catch (Exception e) {
                    Log.e("Failed to register", e.getMessage()); //failed to register for GCM
                    return e;
                }
                return null;
            }

            protected void onPostExecute(Object result) { //registered for GCM
                Toast.makeText(context, "Logged in and registered.",
                        Toast.LENGTH_LONG).show();

            }
        }.execute(null, null, null);
    }
}
