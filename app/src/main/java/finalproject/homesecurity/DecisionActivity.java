package finalproject.homesecurity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import finalproject.homesecurity.Utils.CleanUserId;
import finalproject.homesecurity.Utils.SendMessage;

/**
 * Created by Robbie on 13/08/2015.
 */
public class DecisionActivity extends ActionBarActivity {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decision_activity);
        prefs = this.getSharedPreferences("PhoneMode", Context.MODE_PRIVATE); //indicates whether phone is security device or personal
        editor = prefs.edit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void security(View v)
    {
        System.out.println("security method called");
        //save device mode to phone to be read later
        editor.putString("DeviceMode","Security");
        editor.commit();

        //enable this functionality when you are able to retrieve a list of security devices
        //Intent it = new Intent(this,CameraActivity.class);
        //startActivity(it);
        Toast.makeText(this,"You are now a security device",Toast.LENGTH_LONG).show();
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


}
