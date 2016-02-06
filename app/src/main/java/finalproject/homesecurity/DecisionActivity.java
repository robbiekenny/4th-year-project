package finalproject.homesecurity;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import finalproject.homesecurity.Utils.CleanUserId;
import finalproject.homesecurity.Utils.SendMessage;

/**
 * Created by Robbie on 13/08/2015.
 */
public class DecisionActivity extends ActionBarActivity {
    private SharedPreferences.Editor editor,edit; //edit is used for saving dont show me again, editor used for saving whether the device is security or personal
    private SecurityDetailsFragment frag;
    private FragmentManager fragmentManager;
    private RelativeLayout securityLayout,personalLayout;
    private TextView singedInAs;
    private Toolbar toolbar;
    private SharedPreferences sharedPref,sharedPrefs,prefs;
    private CoordinatorLayout coordinatorLayout;
    private AlertDialog b; //this is used to close the custom dialog displayed to the user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decision_activity);
//        toolbar = (Toolbar) findViewById(R.id.tool_bar2);
//        setSupportActionBar(toolbar);

        sharedPrefs = getSharedPreferences("DontShowAgain", Context.MODE_PRIVATE); //save dont show me again value
        sharedPref = getSharedPreferences("AuthenticatedUserDetails", Context.MODE_PRIVATE);
        prefs = this.getSharedPreferences("PhoneMode", Context.MODE_PRIVATE); //indicates whether phone is security device or personal
        editor = prefs.edit();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if(extras.getString("comingFrom").equals("registering"))
            {
                registerDialog();
            }
        }


        if(sharedPrefs.getBoolean("showAgain",true) == true)
        {
            messageDialog();
        }


        securityLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        personalLayout = (RelativeLayout) findViewById(R.id.relativeLayout2);

        singedInAs = (TextView) findViewById(R.id.signedInAs);

        String signedinas = singedInAs.getText().toString() + sharedPref.getString("userId",null);
        singedInAs.setText(signedinas);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
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

        /*
        Put in an option for users to get help with security and personal selection
         */

        if (id == R.id.action_signout) { //Remove user from shared preferences thus making the user signs in the next time

            System.out.println("LOGIN TYPE: " + sharedPref.getString("loginType", null));

            if(sharedPref.getString("loginType",null).equals("facebook"))
            {
                try
                {
                    LoginManager.getInstance().logOut();
                }catch(Exception e)
                {
                    FacebookSdk.sdkInitialize(this);
                    LoginManager.getInstance().logOut();
                }

            }
           sharedPref.edit().putString("userId", null).apply(); //remove userId that was associated with this user
           sharedPrefs.edit().putBoolean("showAgain", true).apply(); //reset the show again boolean so that it should be displayed the next time

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
        if(sharedPref.getBoolean("verified",false) == false)
        {
            displaySnackbar();
        }
        else {
            securityLayout.setVisibility(View.INVISIBLE);
        personalLayout.setVisibility(View.INVISIBLE);

        frag = (SecurityDetailsFragment) getFragmentManager().findFragmentByTag("frag");
        if(frag == null)
        {
            fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            frag = new SecurityDetailsFragment();
            fragmentTransaction.add(R.id.security_details_fragment_container, frag, "frag");
            fragmentTransaction.commit();
        }
        else
        {
            fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.security_details_fragment_container, frag);
            ft.commit();
        }
        /*
        TESTING STREAMING FUNCTIONALITY
         */
//        Intent intent = new Intent(this,RecordActivity.class);
//        startActivity(intent);

        /*
        TESTING SENDING PHOTO FUNCTIONALITY
         */
//            Intent intent = new Intent(this,CameraActivity.class);
//            startActivity(intent);
        }
    }

    public void personal(View v)
    {
        System.out.println("personal method called");
        if(sharedPref.getBoolean("verified",false) == false)
        {
            displaySnackbar();
        }
        else
        {
            editor.putString("DeviceMode", "Personal"); //this device will be listed as personal
            editor.commit();
            Intent it = new Intent(this,PersonalDeviceActivity.class);
            startActivity(it);
        }

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

        securityLayout.setVisibility(View.VISIBLE);
        personalLayout.setVisibility(View.VISIBLE);

    }

    public void messageDialog() //displays a dialog informaing them of the choice between security and personal
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.message_dialog_layout, null);
        dialogBuilder.setView(dialogView);

        final CheckBox cb = (CheckBox) dialogView.findViewById(R.id.dontShowAgainCBox);

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(cb.isChecked())
                {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putBoolean("showAgain", false); //user doesnt want to see the message again
                    editor.commit();
                }
                b.dismiss();
            }
        });

        b = dialogBuilder.create();
        b.show();
    }

    public void registerDialog() { //displays a dialog informing the user they must verify their account

        new AlertDialog.Builder(this)
                .setTitle("Validate Email")
                .setMessage("To verify your account please check your inbox or junk folder for an email from HomeSecurity. " +
                        "Sign out and sign back in once you have verified your account")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    public void displaySnackbar() //very simply notifys the user that they must verify their account before continuing
    {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Verify your email account", Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.parseColor("#0288D1"));
        snackbar.show();
    }
}
