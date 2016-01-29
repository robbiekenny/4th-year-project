package finalproject.homesecurity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.io.IOException;

import finalproject.homesecurity.Utils.CleanUserId;
import finalproject.homesecurity.Utils.SendMessage;

/**
 * Created by Robbie on 25/11/2015.
 */
public class CommandControlsFragment extends Fragment {
    private String userID,roomName;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.command_controls_layout,
                container, false);
        userID = getArguments().getString("user");
        roomName = getArguments().getString("room");

        Switch motion = (Switch) view.findViewById(R.id.motionDetectionSwitch);
        motion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if (isChecked)//switched on
                    enableMotion();
                else
                    disableMotion();
            }
        });

        Switch flashlight = (Switch) view.findViewById(R.id.flashlightSwitch);
        flashlight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if (isChecked)//switched on
                    enableFlashLight();
                else
                    disableFlashLight();
            }
        });
        return view;
    }

    private void disableFlashLight() {
        try {
            //room name needs to be unique behind the scenes
            SendMessage.sendPush("gcm", CleanUserId.RemoveSpecialCharacters(userID), "LightsOff" + roomName);
            System.out.println("SENT PUSH TO TURN Lights OFF");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR SENDING PUSH FROM LIST VIEW CLICK HANDLER");
        }
    }

    private void enableFlashLight() {
        try {
            SendMessage.sendPush("gcm", CleanUserId.RemoveSpecialCharacters(userID), "LightsOn" + roomName);
            System.out.println("SENT PUSH TO TURN Lights ON");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR SENDING PUSH FROM LIST VIEW CLICK HANDLER");
        }
    }

    private void enableMotion() {
        try {
            SendMessage.sendPush("gcm", CleanUserId.RemoveSpecialCharacters(userID), "MotionOn" + roomName);
            System.out.println("SENT PUSH TO TURN Motion ON");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR SENDING PUSH FROM LIST VIEW CLICK HANDLER");
        }
    }

    private void disableMotion() {
        try {
            SendMessage.sendPush("gcm", CleanUserId.RemoveSpecialCharacters(userID), "MotionOff" + roomName);
            System.out.println("SENT PUSH TO TURN Motion OFF");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR SENDING PUSH FROM LIST VIEW CLICK HANDLER");
        }
    }
}
