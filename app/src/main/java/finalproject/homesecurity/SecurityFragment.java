package finalproject.homesecurity;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import finalproject.homesecurity.model.Room;

/**
 * Created by Robbie on 01/04/2016.
 */
public class SecurityFragment extends Fragment {
    private ArrayList<Room> arrayOfRooms = new ArrayList<Room>();
    public static ListView listView;
    public static RoomsAdapter adapter;
    private String userID;
    public static ProgressBar pd;
    private CommandControlsFragment frag;
    private FragmentManager fragmentManager;
    private Room room;
    private Messaging messagingService;
    private CoordinatorLayout coordinatorLayout;
    private SharedPreferences settings;
    private TextView toolbarTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.security_fragment_layout,
                container, false);

        messagingService = new Messaging();
        settings = getActivity().getSharedPreferences("AuthenticatedUserDetails", Context.MODE_PRIVATE);

        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.security_coordinator_layout);
        pd = (ProgressBar) view.findViewById(R.id.roomsProgressBar);
        toolbarTitle = (TextView) getActivity().findViewById(R.id.toolbar_title);

        // Create the adapter to convert the array to views
        adapter = new RoomsAdapter(getActivity(), arrayOfRooms);
        // Attach the adapter to a ListView
        listView = (ListView) view.findViewById(R.id.rooms);
        listView.setAdapter(adapter);

        handleListViewItemClick();

        getSecurityDevices();

        return view;
    }



    public void getSecurityDevices() //retrieve security devices linked to this account
    {
        adapter.clear();
        userID = settings.getString("userId",null); //default value is null
        if(userID != null) //send message to all devices linked to this account
        {
            System.out.println("GETTING SECURITY DEVICES");
            try {
                // SendMessage.sendPush("gcm", userID,"Retrieve");
                messagingService.sendMessage("Retrieve",userID);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("ERROR SENDING PUSH FOR SECURITY DEVICE RETRIEVAL");
            }
        }
        else
            System.out.println("USERID IS NULL");


        new Handler().postDelayed(new Runnable() {
            public void run() {
                if(adapter.isEmpty()) //if no devices have been found
                {
                    pd.setVisibility(View.INVISIBLE);
                    displaySnackbar();
                }
            }
        }, 10000); //after 10 seconds prompt the user to try search for security devices again
    }

    public void handleListViewItemClick()
    {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                room = (Room) parent.getItemAtPosition(position);
                System.out.println(room.getRoomName());

                listView.setVisibility(View.INVISIBLE);
                Bundle bundle = new Bundle();
                bundle.putString("user", userID);
                bundle.putString("room", room.getRoomName());
                bundle.putInt("position",position);
                frag = (CommandControlsFragment) getActivity().getFragmentManager().findFragmentByTag("frag");
                if(frag == null)
                {
                    fragmentManager = getActivity().getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    frag = new CommandControlsFragment();
                    frag.setArguments(bundle);
                    fragmentTransaction.add(R.id.command_controls_fragment_container, frag, "frag");
                    fragmentTransaction.commit();
                    String[] roomName = room.getRoomName().split("@"); //get room name without UUID
                    toolbarTitle.setText(roomName[0]);
                }
                else
                {
                    fragmentManager = getActivity().getFragmentManager();
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.command_controls_fragment_container, frag);
                    ft.commit();
                }
            }
        });
    }


    public void displaySnackbar() //Notify the user that no security devices were found and allow them to retry
    {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, R.string.failedToFindDevices, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pd.setVisibility(View.VISIBLE);
                        getSecurityDevices();
                    }
                });

        snackbar.setActionTextColor(Color.GREEN);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.parseColor("#0288D1"));
        snackbar.show();
    }
}

/***************************************************************************************
 *
 *
 *    Adapter used to display room objects in a list view
 *
 *
 ***************************************************************************************/
class RoomsAdapter extends ArrayAdapter<Room> {

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
        String[] roomName = room.getRoomName().split("@"); //get room name without
        viewHolder.name.setText(roomName[0]);

        // Return the completed view to render on screen
        return convertView;
    }
}
