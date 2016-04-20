package finalproject.homesecurity;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;

import finalproject.homesecurity.Utils.SendMessage;

/**
 * Created by Robbie on 25/11/2015.
 */
public class CommandControlsFragment extends Fragment {
    private String userID,roomName;
    private int position;
    private SwitchCompat lights,motion;
    private ImageView takeVideo;
    private TextView takeVideoText;
    private Messaging messaging;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.command_controls_layout,
                container, false);
        userID = getArguments().getString("user");
        roomName = getArguments().getString("room");
        position = getArguments().getInt("position");

        takeVideoText = (TextView) view.findViewById(R.id.takeVideo_textView);

        lights = (SwitchCompat) view.findViewById(R.id.lights);
        motion = (SwitchCompat) view.findViewById(R.id.motion);
        takeVideo = (ImageView) view.findViewById(R.id.takeVideo);

        if(SecurityFragment.adapter.getItem(position).isTakingVideo())
        {
            takeVideoText.setText(R.string.takingVideoText);
        }
        if(SecurityFragment.adapter.getItem(position).isMotionDetection())
        {
           motion.setChecked(true);
        }
        if(SecurityFragment.adapter.getItem(position).isLights())
        {
            lights.setChecked(true);
        }

        takeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SecurityFragment.adapter.getItem(position).isTakingVideo())
                {
                }
                else
                {
                    takeVideo();
                    takeVideoText.setText(R.string.takingVideoText);
                    SecurityFragment.adapter.getItem(position).setTakingVideo(true);
                }
            }
        });
        lights.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    enableFlashLight();
                    SecurityFragment.adapter.getItem(position).setLights(true);
                }
                else
                {
                    disableFlashLight();
                    SecurityFragment.adapter.getItem(position).setLights(false);
                }
            }
        });

        motion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    enableMotion();
                    SecurityFragment.adapter.getItem(position).setMotionDetection(true);
                }
                else
                {
                    disableMotion();
                    SecurityFragment.adapter.getItem(position).setMotionDetection(false);
                }
            }
        });
        messaging = new Messaging();

        return view;
    }

    private void takeVideo() {
//        try {
//            SendMessage.sendPush("gcm", userID, "TakeVideo" + roomName);
//            System.out.println("SENT PUSH TO TAKE VIDEO");
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("ERROR SENDING PUSH FROM LIST VIEW CLICK HANDLER");
//        }
        messaging.sendMessage("TakeVideo" + roomName,userID);
    }

    private void disableFlashLight() {
//        try {
//            SendMessage.sendPush("gcm", userID, "LightsOff" + roomName);
//            System.out.println("SENT PUSH TO TURN Lights OFF");
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("ERROR SENDING PUSH FROM LIST VIEW CLICK HANDLER");
//        }
        messaging.sendMessage("LightsOff" + roomName,userID);
    }

    private void enableFlashLight() {
//        try {
//            SendMessage.sendPush("gcm", userID, "LightsOn" + roomName);
//            System.out.println("SENT PUSH TO TURN Lights ON");
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("ERROR SENDING PUSH FROM LIST VIEW CLICK HANDLER");
//        }

        messaging.sendMessage("LightsOn" + roomName,userID);
    }

    private void enableMotion() {
//        try {
//            SendMessage.sendPush("gcm", userID, "MotionOn" + roomName);
//            System.out.println("SENT PUSH TO TURN Motion ON");
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("ERROR SENDING PUSH FROM LIST VIEW CLICK HANDLER");
//        }
        messaging.sendMessage("MotionOn" + roomName,userID);
    }

    private void disableMotion() {
//        try {
//            SendMessage.sendPush("gcm", userID, "MotionOff" + roomName);
//            System.out.println("SENT PUSH TO TURN Motion OFF");
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("ERROR SENDING PUSH FROM LIST VIEW CLICK HANDLER");
//        }

        messaging.sendMessage("MotionOff" + roomName,userID);
    }
}
