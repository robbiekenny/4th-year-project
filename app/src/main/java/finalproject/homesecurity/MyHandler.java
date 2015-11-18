package finalproject.homesecurity;

import com.microsoft.windowsazure.notifications.NotificationsHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;;import java.io.IOException;
import java.security.Policy;

import finalproject.homesecurity.Utils.CleanUserId;
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
        if(substringPersonalMessage.equals("Details"))
        {
            System.out.println("Message sub string: " + message.substring(7, message.length()));
            Room r = new Room(message.substring(7,message.length()));
            PersonalDeviceActivity.adapter.add(r);
            PersonalDeviceActivity.spinner.setVisibility(View.GONE);
            PersonalDeviceActivity.listView.setVisibility(View.VISIBLE);
            PersonalDeviceActivity.adapter.notifyDataSetChanged();
        }
    }


    public void SecurityDevice(String message)
    {
        System.out.println("IN SECURITY METHOD");
        userID = prefs.getString("userId",null);
        roomName = settings.getString("RoomName",null);
        if(message.equals("Retrieve"))//personal device is looking for security devices
        {
            //should send device details
            System.out.println("IN retrieve case");
            //Toast.makeText(ctx,"RETRIEVE",Toast.LENGTH_LONG);
            if(userID == null || roomName == null)
                Toast.makeText(ctx,"An error has occured please close the application and start again", Toast.LENGTH_LONG).show();
            else {
                /*
                MESSAGE BEING SENT TO PERSONAL DEVICE WILL BE IN THE FORMAT
                DETAILS,ROOM NAME e.g DetailsKitchen
                 */
                try {
                    SendMessage.sendPush("gcm", CleanUserId.RemoveSpecialCharacters(userID), "Details" + roomName);
                    //Toast.makeText(ctx, "SENT", Toast.LENGTH_LONG);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("COULD NOT SEND MESSAGE TO PERSONAL DEVICE");
                }
            }
        }
        else //message needs to be sub stringed to see whats in it
        {

            substringSecurityMessage = message.substring(0,8); //should produce either MotionOn or LightsOn
            System.out.println(substringSecurityMessage);
            /*
            AFTER THIS PART I NEED TO CHECK IF THE MESSAGE IS FOR THIS DEVICE OR NOT BY CHECKING THE ROOM NAME
            FOR NOW THOUGH THIS IS IGNORED
             */
            if(substringSecurityMessage.equals("MotionOn") || substringSecurityMessage.equals("LightsOn"))
            {
                if(substringSecurityMessage.equals("MotionOn"))
                    CameraActivity.setDetectMotion(true); //turn motion detection on WORKING
                else
                {
                    //turn flash light on
                /*
                THIS COMMAND MAY DIFFER ON PHONES SO IL HAVE TO RESEARCH THIS
                 */
                    if (ctx.getPackageManager().hasSystemFeature( //WORKING ON MY DEVICE
                            PackageManager.FEATURE_CAMERA_FLASH)) { //if this device has a flash light
                        System.out.println("SUPPORTS FLASH LIGHT FUNCTION");
                        Camera.Parameters p = CameraActivity.getCamera().getParameters();
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        CameraActivity.getCamera().setParameters(p);
                    }
                    else
                    {
                        System.out.println("DOESN'T SUPPORT FLASH LIGHT FUNCTION");
                    }
                }
            }
            else
            {
                substringSecurityMessage = message.substring(0,9); //should produce either MotionOff or LightsOff
                System.out.println(substringSecurityMessage);
                if(substringSecurityMessage.equals("MotionOff"))
                    CameraActivity.setDetectMotion(false); //turn motion detection off
                else
                {
                    //turn flash light off
                    Camera.Parameters p = CameraActivity.getCamera().getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    CameraActivity.getCamera().setParameters(p);
                }

            }
        }
    }
}
