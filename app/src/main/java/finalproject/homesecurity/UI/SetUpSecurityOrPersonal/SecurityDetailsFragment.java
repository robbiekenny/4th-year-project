package finalproject.homesecurity.UI.SetUpSecurityOrPersonal;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.Permission;
import java.util.UUID;

import finalproject.homesecurity.R;
import finalproject.homesecurity.UI.Security.MotionDetectionActivity;

/**
 * Created by Robbie on 11/11/2015.
 * PRESENTS THE USER WITH A TEXT FIELD FOR ENTERING THE NAME OF THE ROOM
 * ROOM NAMES ARE UNIQUE BEHIND THE SCENES SO THE USERS DOESNT HAVE TO WORRY ABOUT THAT
 * BEFORE WE CAN PROGRESS TO THE MOTION DETECTION ACTIVITY WE MUST MAKE SURE WE HAVE ACCESS TO THE CAMERA AND OTHER PERMISSIONS
 * FOR POST LOLLIPOP DEVICES WHICH IS HANDLED IN THIS FRAGMENT
 */
public class SecurityDetailsFragment  extends Fragment {
    private EditText roomName;
    private TextInputLayout roomNameInputLayout;
    private final int MULTIPLE_PERMISSIONS_REQUEST_CODE = 255;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.securit_details_layout,
                container, false);

        Button saveButton = (Button) view.findViewById(R.id.saveButton);
        roomName = (EditText) view.findViewById(R.id.roomNameInput);
        roomNameInputLayout = (TextInputLayout) view.findViewById(R.id.roomNameTextInput);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        return view;
    }

    public void save()
    {
        System.out.println("SAVE");
        if(roomName.getText().toString() == null || roomName.getText().toString().isEmpty())
        {
            roomNameInputLayout.setError("Room name cannot be empty");
            roomName.setText("");
        }
        else
        {
            roomNameInputLayout.setError(null);

            /*Before we go to the motion detection activity we've to make sure the camera permission is enabled for post lollipop devices
            * */
            if(isPostLollipop())
            {
                if(!hasPermissions()) //check we can use the camera,external storage and record audio
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //required to use the next piece of code
                        this.requestPermissions(
                                new String[]{Manifest.permission.CAMERA ,Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MULTIPLE_PERMISSIONS_REQUEST_CODE);
                    }

                }
                else
                    startMotionDetectionActivity();
            }
            else
                startMotionDetectionActivity();
        }
    }

    private void startMotionDetectionActivity()
    {
        Intent it = new Intent(getActivity(),MotionDetectionActivity.class);
        //UUID only uses the following characters abcdefABCDEF1234567890-
        //inserting an @ symbol will allow me to seperate the room name and UUID
        it.putExtra("roomName", roomName.getText().toString().trim()
                + "@" + UUID.randomUUID().toString()); //send the name and the UUID of the room to the motion detection activity
        startActivity(it);                                      //motion detection activity will save it to shared preferences
    }

    private boolean isPostLollipop(){

        return(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);

    }

    private boolean hasPermissions()
    {
        //check we have the camera permission
        int cameraPermission = ContextCompat.checkSelfPermission(getActivity(),
               Manifest.permission.CAMERA);

        //check we can write to external storage
        int storagePermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //check we can record audio
        int recordAudioPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.RECORD_AUDIO);

        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                storagePermission == PackageManager.PERMISSION_GRANTED &&
                recordAudioPermission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {

                    //all permissions were granted
                    startMotionDetectionActivity();

                } else {

                    // permission denied
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Permissions necessary for security device were not granted",Toast.LENGTH_LONG).show();

                }
                return;
            }
        }
    }
}
