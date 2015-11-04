package finalproject.homesecurity.Utils;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

import finalproject.homesecurity.Constants;

/**
 * Created by Robbie on 04/11/2015.
 */
public class SendMessage {
    /**
     * This method calls the ASP.NET WebAPI backend to send the notification message
     * to the platform notification service based on the pns parameter.
     *
     * @param pns     The platform notification service to send the notification message to. Must
     *                be one of the following ("wns", "gcm", "apns").
     * @param userTag The tag for the user who will receive the notification message. This string
     *                must not contain spaces or special characters.
     * @param message The notification message string. This string must include the double quotes
     *                to be used as JSON content.
     */
    public static void sendPush(final String pns, final String userTag, final String message)
            throws ClientProtocolException, IOException {
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    String uri = Constants.BACKEND_ENDPOINT + "/api/Notifications";
                    uri += "?pns=" + pns;       //adds parameters the the uri
                    uri += "&to_tag=" + userTag;

                    HttpPost request = new HttpPost(uri);
                    request.addHeader("X-ZUMO-APPLICATION", Constants.APPLICATION_KEY);
                    request.addHeader("Content-Type", "application/json");
                    request.setEntity(new StringEntity("\"" + message + "\"")); //data passed to the controller

                    HttpResponse response = new DefaultHttpClient().execute(request);

                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        Log.e("Notification Error1",
                                response.getStatusLine().toString());
                        throw new RuntimeException("Error sending notification");
                    }
                } catch (Exception e) {
                    Log.e("Notification Error2", e.getMessage());
                    return e;
                }

                return null;
            }
        }.execute(null, null, null);
    }
}
