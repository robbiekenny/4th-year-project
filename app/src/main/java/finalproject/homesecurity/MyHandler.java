package finalproject.homesecurity;

import com.microsoft.windowsazure.notifications.NotificationsHandler;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;;

/**
 * Created by Robbie on 30/09/2015.
 */
public class MyHandler extends NotificationsHandler {
    private Context ctx;
    private String deviceMode;
    private SharedPreferences settings;

//    @Override
//    public void onRegistered(Context context,  final String gcmRegistrationId) {
//        super.onRegistered(context, gcmRegistrationId);
//
//        new AsyncTask<Void, Void, Void>() {
//
//            protected Void doInBackground(Void... params) {
//                try {
//                    MainActivity.mClient.getPush().register(gcmRegistrationId, null);
//                    System.out.println("HERE " + gcmRegistrationId);
//                    return null;
//                }
//                catch(Exception e) {
//                    // handle error
//                    e.printStackTrace();
//                }
//                return null;
//            }
//        }.execute();
//    }

    @Override
    public void onReceive(Context context, Bundle bundle) {
        ctx = context;
        settings = ctx.getSharedPreferences("PhoneMode", Context.MODE_PRIVATE);
        String message = bundle.getString("message");
        Log.e("MESSAGE: ", "----------------------" + message);
        deviceMode = settings.getString("DeviceMode",null); //default value is null

        if(deviceMode != null) {
            //device may not have selected the security or personal option
            if (deviceMode.equals("Security")) //this device is listed as a security device
            {
                SecurityDevice(message);
            } else if (deviceMode.equals("Personal")) //this device is listed as a personal device
            {
                PersonalDevice(message);
            }
        }
    }

    private void PersonalDevice(String message) {
        switch (message)
        {
            //case "Retrieval": //personal device is looking for security devices

        }
    }


    public void SecurityDevice(String message)
    {
        switch (message)
        {
            case "Retrieve": //personal device is looking for security devices
                //should send device details
                Toast.makeText(ctx,message,Toast.LENGTH_LONG).show();
                break;

        }
    }
}
