package finalproject.homesecurity;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

/**
 * Created by Robbie on 11/11/2015.
 */
public class SecurityDetailsFragment  extends Fragment {
    private EditText roomName;
    private CoordinatorLayout coordinatorLayout;
    private TextInputLayout roomNameInputLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.securit_details_layout,
                container, false);

        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.security_details_coordinatorLayout);

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
            Intent it = new Intent(getActivity(),MotionDetectionActivity.class);
            //UUID only uses the following characters abcdefABCDEF1234567890-
            //inserting an @ symbol will allow me to seperate the room name and UUID
            it.putExtra("roomName", roomName.getText().toString().trim()
                    + "@" + UUID.randomUUID().toString()); //send the name and the UUID of the room to the camera activity
            startActivity(it);                                      //camera activity will save it to shared preferences
        }
    }
}
