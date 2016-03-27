package finalproject.homesecurity;

import com.microsoft.windowsazure.notifications.NotificationsHandler;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import finalproject.homesecurity.Utils.SendMessage;
import finalproject.homesecurity.model.Room;

/**
 * Created by Robbie on 30/09/2015.
 */
public class MyHandler extends NotificationsHandler {
    private Context ctx;
    private String deviceMode;
    private SharedPreferences settings,prefs;
    private String userID,roomName, substringPersonalMessage,substringSecurityMessage;

    @Override
    public void onReceive(Context context, Bundle bundle) {
        ctx = context;
        settings = ctx.getSharedPreferences("PhoneMode", Context.MODE_PRIVATE);
        prefs = ctx.getSharedPreferences("AuthenticatedUserDetails", Context.MODE_PRIVATE);
        String message = bundle.getString("message");
        Log.e("MESSAGE: ", "----------------------" + message);
        deviceMode = "";
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
        //Toast.makeText(ctx,"RECIEVED",Toast.LENGTH_LONG);
        System.out.println("RECIEVED");
        substringPersonalMessage = message.substring(0,7);
        System.out.println(substringPersonalMessage + "------------");
        if(substringPersonalMessage.equals("Details")) //take in the details of the security device
        {
            System.out.println("Message sub string: " + message.substring(7, message.length()));
            Room r = new Room(message.substring(7,message.length()));
            try
            {
                PersonalDeviceActivity.adapter.add(r);
                PersonalDeviceActivity.pd.dismiss();
                PersonalDeviceActivity.listView.setVisibility(View.VISIBLE);
                PersonalDeviceActivity.adapter.notifyDataSetChanged();
            }catch(Exception e)
            {
                System.out.println("ERROR ADDING ROOM TO ADAPTER");
                e.printStackTrace();
            }
        }
        else if(substringPersonalMessage.equals("Unable "))
        {
            System.out.println("Displaying error message");
            String[] messageWithoutUUID = message.split("@"); //get message without UUID attached to room name
            NotificationCompat.Builder mBuilder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(ctx)
                            .setSmallIcon(R.drawable.ic_stat_name)
                            .setContentTitle("HomeSecurity")
                            .setContentText(messageWithoutUUID[0]);
// Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(ctx, DecisionActivity.class);

            int notificationID = 1;
// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
// Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(DecisionActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
            mNotificationManager.notify(notificationID, mBuilder.build());
        }
    }


    public void SecurityDevice(String message)
    {
        System.out.println("IN SECURITY METHOD");
        userID = prefs.getString("userId",null);
        roomName = settings.getString("RoomName",null);
        if(message.equals("Retrieve"))//personal device is looking for security devices details so all security devices respond
        {
            //should send device details
            System.out.println("IN retrieve case" + "," + message);
            //Toast.makeText(ctx,"RETRIEVE",Toast.LENGTH_LONG);
            if(userID == null || roomName == null)
                Toast.makeText(ctx,"An error has occured please close the application and start again", Toast.LENGTH_LONG).show();
            else {
                /*
                MESSAGE BEING SENT TO PERSONAL DEVICE WILL BE IN THE FORMAT
                DETAILS,ROOM NAME e.g DetailsKitchen
                Appended to each room name is the @ symbol and a UUID
                 */
                try {
                    SendMessage.sendPush("gcm", userID, "Details" + roomName);
                    //Toast.makeText(ctx, "SENT", Toast.LENGTH_LONG);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("COULD NOT SEND MESSAGE TO PERSONAL DEVICE");
                }
            }
        }
        else //these messages are device specific and messages are in the format [COMMAND][ROOMNAME]
        {
//            System.out.println("IN specific case" + "," + message);
//            System.out.println(message.substring(9, message.length()).equals(roomName));
//            System.out.println(message.substring(8, message.length()).equals(roomName));
//            System.out.println(message.substring(7, message.length()).equals(roomName));
//
//            System.out.println(message.substring(9, message.length()));
//            System.out.println(message.substring(8, message.length()));
//            System.out.println(message.substring(7, message.length()));

            if(message.substring(8, message.length()).equals(roomName) || message.substring(9, message.length()).equals(roomName)) {
            //find out whether or not this message is for this device
                //by taking away either 8 or 9 letters from the message
                //the reason its 8 or 9 is because everything after MotionOn or MotionOff is the room name and the UUID

                substringSecurityMessage = message.substring(0, 8); //should produce either MotionOn or LightsOn
                System.out.println(substringSecurityMessage);

                if (substringSecurityMessage.equals("MotionOn") || substringSecurityMessage.equals("LightsOn")) {
                    if (substringSecurityMessage.equals("MotionOn"))
                        MotionDetectionActivity.setDetectMotion(true); //turn motion detection on WORKING
                    else {
                        //turn flash light on
                /*
                THIS COMMAND MAY DIFFER ON PHONES SO IL HAVE TO RESEARCH THIS
                 */
                        if (ctx.getPackageManager().hasSystemFeature( //WORKING ON MY DEVICE
                                PackageManager.FEATURE_CAMERA_FLASH)) { //if this device has a flash light
                            System.out.println("SUPPORTS FLASH LIGHT FUNCTION");
                            //need to check if motion detection is already enabled
                            //if it is set it to false and set it back to true once the light has been turned on
                            int motion = 0;
                            if(MotionDetectionActivity.getMotionDetection()) {
                                motion = 1;
                                MotionDetectionActivity.setDetectMotion(false);
                            }

                            Camera.Parameters p = MotionDetectionActivity.getCamera().getParameters();
                            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            MotionDetectionActivity.getCamera().setParameters(p);

                            if(motion == 1) {
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        MotionDetectionActivity.setDetectMotion(false);
                                    }
                                }, 1000);
                            }

                        } else {
                            System.out.println("DOESN'T SUPPORT FLASH LIGHT FUNCTION");
                        }
                    }
                } else {
                    substringSecurityMessage = message.substring(0, 9); //handle MotionOff, LightsOff and TakeVideo
                    System.out.println(substringSecurityMessage);
                    if (substringSecurityMessage.equals("MotionOff"))
                        MotionDetectionActivity.setDetectMotion(false); //turn motion detection off
                    else if(substringSecurityMessage.equals("LightsOff")){
                        //turn flash light off

                        int motion = 0;
                        if(MotionDetectionActivity.getMotionDetection()) {
                            motion = 1;
                            MotionDetectionActivity.setDetectMotion(false);
                        }
                        Camera.Parameters p = MotionDetectionActivity.getCamera().getParameters();
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        MotionDetectionActivity.getCamera().setParameters(p);

                        if(motion == 1) {
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    MotionDetectionActivity.setDetectMotion(false);
                                }
                            }, 1000);
                        }
                    }
                    else if(substringSecurityMessage.equals("TakeVideo")) //Record a 30 second video
                    {
                        Intent intent = new Intent(ctx,RecordVideoActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(intent);
                    }
                }
            }
        }
    }
}
