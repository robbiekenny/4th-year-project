package finalproject.homesecurity.UI.Personal;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import finalproject.homesecurity.R;
import finalproject.homesecurity.model.Video;

/**
 * Created by Robbie on 01/04/2016.
 *
 * RETRIEVE ALL OF A USERS VIDEOS AND DISPLAY THEM IN A LIST
 * CLICKING ON THEM WILL INVOKE THE EXOMEDIA FRAGMENT FOR PLAYING OF THE VIDEO
 */
public class VideoFragment extends Fragment {
    private VideosAdapter adapter;
    private ArrayList<Video> arrayOfVideos = new ArrayList<Video>();
    private ListView listView;
    private SharedPreferences sharedPreferences;
    private ProgressBar pb;
    private Video video;
    private PlayVideoFragment frag;
    private FragmentManager fragmentManager;
    private TextView progressText;
    private SwipeRefreshLayout swipeContainer;
    private CoordinatorLayout coordinatorLayout;
    private RequestQueue queue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.video_fragment_layout,
                container, false);
        queue = Volley.newRequestQueue(getActivity());
        sharedPreferences = getActivity().getSharedPreferences("AuthenticatedUserDetails", Context.MODE_PRIVATE);

        pb = (ProgressBar) view.findViewById(R.id.videosProgressBar);
        progressText = (TextView) view.findViewById(R.id.progressText);
        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.video_coordinator_layout);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.video_swipeContainer);

        // Create the adapter to convert the array to views
        adapter = new VideosAdapter(getActivity(), arrayOfVideos);
        // Attach the adapter to a ListView
        listView = (ListView) view.findViewById(R.id.videos);
        listView.setAdapter(adapter);

        handleListViewItemClick();
        getVideos();

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources( R.color.ColorAccent,
                R.color.ColorPrimary,
                R.color.ColorPrimaryDark,
                R.color.ColorPrimaryDarker);
        return view;
    }

    private void refresh() {
        adapter.clear();
        getVideos();
        swipeContainer.setRefreshing(false);
    }

    private void getVideos() { //get a users videos
        final String url = "http://homesecurityservice.azurewebsites.net/api/videos?email="
                + sharedPreferences.getString("userId",null);

        // prepare the Request
        //if a user has videos then the result will be a JSONArray
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response) {
                        // display response
                        Log.d("Response", response.toString());
                        pb.setVisibility(View.INVISIBLE);
                        progressText.setVisibility(View.INVISIBLE);
                        JSONObject data;
                        Video v;
                        try
                        {
                            for(int i= 0; i < response.length(); i++) //go through each item in the JSONArray and create a video
                            {                                          // object to store in our adapter
                                data = response.getJSONObject(i);
                                v = new Video(data.getString("vid"),data.getString("roomName"),data.getString("createdAt"));
                                adapter.add(v);
                                adapter.notifyDataSetChanged();
                            }
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("Error.Response", error.toString());
                        pb.setVisibility(View.INVISIBLE);
                        progressText.setVisibility(View.INVISIBLE);
                        displaySnackBar();
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("ZUMO-API-VERSION","2.0.0"); //used to authenticate our request
                return params;
            }
        };

        getRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
// add it to the RequestQueue
        queue.add(getRequest);
    }

    private void displaySnackBar() {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, R.string.noVideos, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pb.setVisibility(View.VISIBLE);
                        progressText.setVisibility(View.VISIBLE);
                        getVideos();
                    }
                });

        snackbar.setActionTextColor(Color.GREEN);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.parseColor("#3F51B5"));
        snackbar.show();
    }

    public void handleListViewItemClick()
    {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                video = (Video) parent.getItemAtPosition(position);

                //listView.setVisibility(View.INVISIBLE);
                Bundle bundle = new Bundle();
                bundle.putString("video", video.getVideo());

                frag = (PlayVideoFragment) getActivity().getFragmentManager().findFragmentByTag("video_frag");
                if(frag == null)
                {
                    fragmentManager = getActivity().getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    frag = new PlayVideoFragment();
                    frag.setArguments(bundle);
                    fragmentTransaction.add(R.id.video_player_container, frag, "video_frag");
                    fragmentTransaction.commit();
                }
                else
                {
                    fragmentManager = getActivity().getFragmentManager();
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.video_player_container, frag);
                    ft.commit();
                }
            }
        });
    }
}

/***************************************************************************************
 *
 *
 *    Adapter used to display video objects in a list view
 *
 *
 ***************************************************************************************/
class VideosAdapter extends ArrayAdapter<Video> {

    // View lookup cache  -- used to improve performance
    private static class ViewHolder {
        TextView roomName,date;
    }


    public VideosAdapter(Context context, ArrayList<Video> videos) {
        super(context, R.layout.video_listview_layout, videos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Video video = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.video_listview_layout, parent, false);
            viewHolder.roomName = (TextView) convertView.findViewById(R.id.video_room_name);
            viewHolder.date = (TextView) convertView.findViewById(R.id.video_date);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object

        viewHolder.roomName.setText(video.getRoomName());
        viewHolder.date.setText(video.getCreatedAt());

        // Return the completed view to render on screen
        return convertView;
    }
}

