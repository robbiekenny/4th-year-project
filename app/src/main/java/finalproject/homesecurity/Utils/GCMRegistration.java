//package finalproject.homesecurity.Utils;
//
//import android.content.Context;
//import android.os.AsyncTask;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.google.android.gms.gcm.GoogleCloudMessaging;
//
//import java.io.UnsupportedEncodingException;
//import java.util.HashSet;
//
//import finalproject.homesecurity.Constants;
//import finalproject.homesecurity.RegisterClient;
//
///**
// * Created by Robbie on 20/01/2016.
// */
///*
//THIS CLASS USE TO BE RESPONSIBLE FOR REGISTERING CLIENTS FOR GCM BUT I HAVE SINCE MOVED TO PUBNUB FOR THIS FUNCTIONALITY
// */
//public class GCMRegistration {
//
//    public GCMRegistration(){}
//
//    public void registerClientForGCM(final RegisterClient registerClient,String email,
//                                     final Context context,final GoogleCloudMessaging gcm) throws UnsupportedEncodingException {
//        registerClient.setUserID(email);
//
//
//        new AsyncTask<Void, Void, String>() {
//            @Override
//            protected String doInBackground(Void... params) {
//                try {
//                    String regid = gcm.register(Constants.SENDER_ID);
//                    System.out.println("GCM ID: " + regid);
//                    registerClient.register(regid, new HashSet<String>());
//                    return "Success";
//                } catch (Exception e) {
//                    Log.e("Failed to register", e.getMessage()); //failed to register for GCM
//                    return "Fail";
//                }
//            }
//
//            protected void onPostExecute(String result) { //registered for GCM
//                if(result.equals("Success"))
//                {
//                    Toast.makeText(context, "This device has been added to your group of devices",
//                            Toast.LENGTH_LONG).show();
//                }
//                else
//                {
//                    Toast.makeText(context, "Unable to add this device to your group of devices. Log out and try again",
//                            Toast.LENGTH_LONG).show();
//                }
//            }
//        }.execute(null, null, null);
//    }
//}
