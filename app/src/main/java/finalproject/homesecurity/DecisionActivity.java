package finalproject.homesecurity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import finalproject.homesecurity.Utils.CleanUserId;
import finalproject.homesecurity.Utils.SendMessage;

/**
 * Created by Robbie on 13/08/2015.
 */
public class DecisionActivity extends ActionBarActivity {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private SecurityDetailsFragment frag;
    private FragmentManager fragmentManager;
    private Button pButton,sButton;
    private TextView text1,text2;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decision_activity);
        toolbar = (Toolbar) findViewById(R.id.tool_bar2);
        setSupportActionBar(toolbar);

        prefs = this.getSharedPreferences("PhoneMode", Context.MODE_PRIVATE); //indicates whether phone is security device or personal
        editor = prefs.edit();
        pButton = (Button) findViewById(R.id.personal);
        sButton = (Button) findViewById(R.id.security);
        text1 = (TextView) findViewById(R.id.textView);
        text2 = (TextView) findViewById(R.id.textView2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_signout) { //Remove user from shared preferences thus making the user sign in the next time
            SharedPreferences prefs = getSharedPreferences("AuthenticatedUserDetails", Context.MODE_PRIVATE);
            prefs.edit().putString("userId",null).apply();

            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void security(View v)
    {
        System.out.println("security method called");
//        pButton.setVisibility(View.INVISIBLE);
//        sButton.setVisibility(View.INVISIBLE);
//        text1.setVisibility(View.INVISIBLE);
//        text2.setVisibility(View.INVISIBLE);
//
//        frag = (SecurityDetailsFragment) getFragmentManager().findFragmentByTag("frag");
//        if(frag == null)
//        {
//            fragmentManager = getFragmentManager();
//            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            frag = new SecurityDetailsFragment();
//            fragmentTransaction.add(R.id.security_details_fragment_container, frag, "frag");
//            fragmentTransaction.commit();
//        }
//        else
//        {
//            fragmentManager = getFragmentManager();
//            FragmentTransaction ft = fragmentManager.beginTransaction();
//            ft.replace(R.id.security_details_fragment_container, frag);
//            ft.commit();
//        }
        /*
        TESTING STREAMING FUNCTIONALITY
         */
        Intent intent = new Intent(this,RecordActivity.class);
        startActivity(intent);
    }

    public void personal(View v)
    {
        System.out.println("personal method called");
        editor.putString("DeviceMode", "Personal"); //this device will be listed as personal
        editor.commit();
        Intent it = new Intent(this,PersonalDeviceActivity.class);
        startActivity(it);
    }

    /** Check if this device has a camera
     * this method is used at runtime to determine if there is a camera but ideally i'd like to know before this.
     * investigate further to determine what i can do if i already know the device doesnt have a camera*/
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public void cancelDetails(View v) //remove fragment
    {
        fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(frag);
        fragmentTransaction.commit();

        pButton.setVisibility(View.VISIBLE);
        sButton.setVisibility(View.VISIBLE);
        text1.setVisibility(View.VISIBLE);
        text2.setVisibility(View.VISIBLE);
    }
}
