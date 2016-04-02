package finalproject.homesecurity;

import android.app.Fragment;
import android.os.Bundle;
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
    private ImageView lights,motion,takeVideo;
    private TextView lightText,motionText,takeVideoText;
    private Messaging messaging;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.command_controls_layout,
                container, false);
        userID = getArguments().getString("user");
        roomName = getArguments().getString("room");
        position = getArguments().getInt("position");

        lightText = (TextView) view.findViewById(R.id.lights_textView);
        motionText = (TextView) view.findViewById(R.id.motion_textView);
        takeVideoText = (TextView) view.findViewById(R.id.takeVideo_textView);

        lights = (ImageView) view.findViewById(R.id.lights);
        motion = (ImageView) view.findViewById(R.id.motion);
        takeVideo = (ImageView) view.findViewById(R.id.takeVideo);

        if(SecurityFragment.adapter.getItem(position).isTakingVideo())
        {
            takeVideoText.setText(R.string.takingVideoText);
        }
        if(SecurityFragment.adapter.getItem(position).isMotionDetection())
        {
            motion.setImageResource(R.drawable.motion_off);
            motionText.setText(R.string.mdoff);
        }
        if(SecurityFragment.adapter.getItem(position).isLights())
        {
            lights.setImageResource(R.drawable.lightoff);
            lightText.setText(R.string.lightoff);
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

        lights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SecurityFragment.adapter.getItem(position).isLights())
                {
                    lights.setImageResource(R.drawable.lighton);
                    lightText.setText(R.string.lighton);
                    disableFlashLight();
                    SecurityFragment.adapter.getItem(position).setLights(false);
                }
                else
                {
                    lights.setImageResource(R.drawable.lightoff);
                    lightText.setText(R.string.lightoff);
                    enableFlashLight();
                    SecurityFragment.adapter.getItem(position).setLights(true);
                }
            }
        });

        motion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SecurityFragment.adapter.getItem(position).isMotionDetection())
                {
                    motion.setImageResource(R.drawable.motion_on);
                    motionText.setText(R.string.mdon);
                    disableMotion();
                    SecurityFragment.adapter.getItem(position).setMotionDetection(false);
                }
                else
                {
                    motion.setImageResource(R.drawable.motion_off);
                    motionText.setText(R.string.mdoff);
                    enableMotion();
                    SecurityFragment.adapter.getItem(position).setMotionDetection(true);
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
