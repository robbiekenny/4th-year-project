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
    private ImageView lights,motion;
    private boolean lightsOn = false,motionOn = false;
    private TextView lightText,motionText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.command_controls_layout,
                container, false);
        userID = getArguments().getString("user");
        roomName = getArguments().getString("room");

        lightText = (TextView) view.findViewById(R.id.lights_textView);
        motionText = (TextView) view.findViewById(R.id.motion_textView);

        lights = (ImageView) view.findViewById(R.id.lights);
        lights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lightsOn)
                {
                    lightsOn = false;
                    lights.setImageResource(R.drawable.lighton);
                    lightText.setText(R.string.lighton);
                    disableFlashLight();
                }
                else
                {
                    lightsOn = true;
                    lights.setImageResource(R.drawable.lightoff);
                    lightText.setText(R.string.lightoff);
                    enableFlashLight();
                }
            }
        });

        motion = (ImageView) view.findViewById(R.id.motion);
        motion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(motionOn)
                {
                    motionOn = false;
                    motion.setImageResource(R.drawable.motion_on);
                    motionText.setText(R.string.mdon);
                    disableMotion();
                }
                else
                {
                    motionOn = true;
                    motion.setImageResource(R.drawable.motion_off);
                    motionText.setText(R.string.mdoff);
                    enableMotion();
                }
            }
        });

        Button vid = (Button) view.findViewById(R.id.videoButton);
        vid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeVideo();
            }
        });

        return view;
    }

    private void takeVideo() {
        try {
            //room name needs to be unique behind the scenes
            SendMessage.sendPush("gcm", userID, "TakeVideo" + roomName);
            System.out.println("SENT PUSH TO TAKE VIDEO");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR SENDING PUSH FROM LIST VIEW CLICK HANDLER");
        }
    }

    private void disableFlashLight() {
        try {
            //room name needs to be unique behind the scenes
            SendMessage.sendPush("gcm", userID, "LightsOff" + roomName);
            System.out.println("SENT PUSH TO TURN Lights OFF");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR SENDING PUSH FROM LIST VIEW CLICK HANDLER");
        }
    }

    private void enableFlashLight() {
        try {
            SendMessage.sendPush("gcm", userID, "LightsOn" + roomName);
            System.out.println("SENT PUSH TO TURN Lights ON");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR SENDING PUSH FROM LIST VIEW CLICK HANDLER");
        }
    }

    private void enableMotion() {
        try {
            SendMessage.sendPush("gcm", userID, "MotionOn" + roomName);
            System.out.println("SENT PUSH TO TURN Motion ON");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR SENDING PUSH FROM LIST VIEW CLICK HANDLER");
        }
    }

    private void disableMotion() {
        try {
            SendMessage.sendPush("gcm", userID, "MotionOff" + roomName);
            System.out.println("SENT PUSH TO TURN Motion OFF");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR SENDING PUSH FROM LIST VIEW CLICK HANDLER");
        }
    }
}
