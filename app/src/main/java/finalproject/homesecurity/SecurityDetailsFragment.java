package finalproject.homesecurity;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Robbie on 11/11/2015.
 */
public class SecurityDetailsFragment  extends Fragment {
    private EditText roomName;
    private CoordinatorLayout coordinatorLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.securit_details_layout,
                container, false);

        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.security_details_coordinatorLayout);

        Button saveButton = (Button) view.findViewById(R.id.saveButton);
        roomName = (EditText) view.findViewById(R.id.roomNameInput);

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
        System.out.println("Room name: " + roomName);
        if(roomName.getText().toString() == null || roomName.getText().toString().isEmpty())
        {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Room name cannot be empty", Snackbar.LENGTH_LONG);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.parseColor("#0288D1"));
            snackbar.show();
            roomName.setText("");
        }
        else
        {
            Intent it = new Intent(getActivity(),CameraActivity.class);
            it.putExtra("roomName", roomName.getText().toString().trim()); //send the name of the room to the camera activity
            startActivity(it);                                      //camera activity will save it to shared preferences
        }
    }
}
