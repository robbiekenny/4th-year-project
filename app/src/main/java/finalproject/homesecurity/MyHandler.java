package finalproject.homesecurity;

import com.microsoft.windowsazure.notifications.NotificationsHandler;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;;

/**
 * Created by Robbie on 30/09/2015.
 */
public class MyHandler extends NotificationsHandler {
    Context ctx;

    @Override
    public void onRegistered(Context context,  final String gcmRegistrationId) {
        super.onRegistered(context, gcmRegistrationId);

        new AsyncTask<Void, Void, Void>() {

            protected Void doInBackground(Void... params) {
                try {
                    MainActivity.mClient.getPush().register(gcmRegistrationId, null);
                    System.out.println("HERE " + gcmRegistrationId);
                    return null;
                }
                catch(Exception e) {
                    // handle error
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void onReceive(Context context, Bundle bundle) {
        ctx = context;
        String nhMessage = bundle.getString("message");
        System.out.println("MESSAGE: " + nhMessage);
    }
}
