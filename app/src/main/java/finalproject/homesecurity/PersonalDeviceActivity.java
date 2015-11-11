package finalproject.homesecurity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.ArrayList;

import finalproject.homesecurity.Utils.CleanUserId;
import finalproject.homesecurity.Utils.SendMessage;
import finalproject.homesecurity.model.Room;
import finalproject.homesecurity.model.User;

/**
 * Created by Robbie on 04/11/2015.
 */
public class PersonalDeviceActivity extends ActionBarActivity {
    public static ProgressBar spinner;
    private ArrayList<Room> arrayOfRooms = new ArrayList<Room>();
    public static ListView listView;
    public static RoomsAdapter adapter; //static makes this adapter accessible in the notifications handler
    private SharedPreferences settings;
    private String userID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_activity_layout);
        spinner = (ProgressBar)findViewById(R.id.spinner);
        settings = getSharedPreferences("AuthenticatedUserDetails", Context.MODE_PRIVATE);

        // Create the adapter to convert the array to views
         adapter = new RoomsAdapter(this, arrayOfRooms);
        // Attach the adapter to a ListView
         listView = (ListView) findViewById(R.id.rooms);
        listView.setAdapter(adapter);
        handleListViewItemClick();
        getSecurityDevices();
    }

    public void getSecurityDevices() //retrieve security devices linked to this account
    {
        userID = settings.getString("userId",null); //default value is null
        if(userID != null) //send message to all devices linked to this account
        {
            System.out.println("GETTING SECURITY DEVICES");
            try {
                SendMessage.sendPush("gcm", CleanUserId.RemoveSpecialCharacters(userID),"Retrieve");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("ERROR SENDING PUSH FOR SECURITY DEVICE RETRIEVAL");
            }
        }
        else
            System.out.println("USERID IS NULL");

    }

    public void handleListViewItemClick()
    {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Room r = (Room) parent.getItemAtPosition(position);
                System.out.println(r.getRoomName());
                try {
                    //should probably use shared preferences to get the room name
                    //room name needs to be unique behind the scenes
                    SendMessage.sendPush("gcm",CleanUserId.RemoveSpecialCharacters(userID),"MotionOn" + r.getRoomName());
                    System.out.println("SENT PUSH TO TURN MOTION ON");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("ERROR SENDING PUSH FROM LIST VIEW CLICK HANDLER");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //THIS WAS JUST TO TEST THAT IT WORKS
            //IT DOES :D
            try {
                SendMessage.sendPush("gcm",CleanUserId.RemoveSpecialCharacters(userID),"LightsOn");
                System.out.println("SENT PUSH TO TURN LIGHTS ON");
            } catch (IOException e) {
                System.out.println("COULD NOT SEND PUSH TO TURN LIGHTS ON");
                e.printStackTrace();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
/***************************************************************************************
 *
 *
 *    Adapter used to display room objects in a list view
 *
 *
 ***************************************************************************************/
     class RoomsAdapter extends ArrayAdapter<Room> { //this will use users as an example for now

         // View lookup cache  -- used to improve performance
         private static class ViewHolder {
             TextView name;

         }


         public RoomsAdapter(Context context, ArrayList<Room> rooms) {
             super(context, R.layout.objects_inside_of_listview_layout, rooms);
         }

         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             // Get the data item for this position
             Room room = getItem(position);
             // Check if an existing view is being reused, otherwise inflate the view
             ViewHolder viewHolder; // view lookup cache stored in tag
             if (convertView == null) {
                 viewHolder = new ViewHolder();
                 LayoutInflater inflater = LayoutInflater.from(getContext());
                 convertView = inflater.inflate(R.layout.objects_inside_of_listview_layout, parent, false);
                 viewHolder.name = (TextView) convertView.findViewById(R.id.roomName);

                 convertView.setTag(viewHolder);
             } else {
                 viewHolder = (ViewHolder) convertView.getTag();
             }
             // Populate the data into the template view using the data object
             viewHolder.name.setText(room.getRoomName());

             // Return the completed view to render on screen
             return convertView;
         }
}