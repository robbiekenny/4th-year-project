package finalproject.homesecurity;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.io.UnsupportedEncodingException;


import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import java.net.MalformedURLException;

import finalproject.homesecurity.Utils.GCMRegistration;

public class MainActivity extends ActionBarActivity {
     //deals with sign in and register (should split the two later on for cleaner code)
    public static MobileServiceClient mClient;
    private RegisterFragment frag;
    private LoginFragment loginFrag;
    private FragmentManager fragmentManager;
    private FrameLayout container;
    //private ProgressDialog progress;
    public static RegisterClient registerClient;
    public static GoogleCloudMessaging gcm;
    private Toolbar toolbar;
    private GCMRegistration gcmReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        // text view label
        TextView title = (TextView) findViewById(R.id.textView2);

        // Loading Font Face
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Candice.ttf");

        // Applying font
        title.setTypeface(tf);

        try {
            mClient = new MobileServiceClient(
                    "https://homesecurity.azure-mobile.net/",
                    Constants.APPLICATION_KEY,
                    this
            );

        } catch (MalformedURLException e) {
            System.out.println("ERROR STARTING MOBILE SERVICE CLIENT");
            e.printStackTrace();
        }

        NotificationsManager.handleNotifications(this, Constants.SENDER_ID, MyHandler.class);
        gcm = GoogleCloudMessaging.getInstance(this);
        registerClient = new RegisterClient(this, Constants.BACKEND_ENDPOINT);

        SharedPreferences sharedPref = getSharedPreferences("AuthenticatedUserDetails", Context.MODE_PRIVATE);
        String user = sharedPref.getString("userId",null);

        if(user != null) //checking to see if this user is already signed in
        {
            gcmReg = new GCMRegistration();

            try {
                gcmReg.registerClientForGCM(registerClient,user,this,gcm);
            } catch (UnsupportedEncodingException e) {
                System.out.println("FAILED TO REGISTER FOR GCM");
                e.printStackTrace();
            }
            Intent intent = new Intent(this,DecisionActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {

//            toolbar = (Toolbar) findViewById(R.id.tool_bar);
//            setSupportActionBar(toolbar);
            container = (FrameLayout) findViewById(R.id.fragment_container);

            loginFrag = (LoginFragment) getFragmentManager().findFragmentByTag("loginFrag");
            if(loginFrag == null)
            {
                fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                loginFrag = new LoginFragment();
                fragmentTransaction.add(R.id.fragment_container, loginFrag, "loginFrag");
                //fragmentTransaction.addToBackStack(null); //allows user to press back button on phone to get rid of fragment
                fragmentTransaction.commit();
            }
            else
            {
                fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();

                //CHECK TO SEE WHETHER THE USER WAS ON THE CREATE ACCOUNT PAGE
                frag = (RegisterFragment) getFragmentManager().findFragmentByTag("frag");
                if(frag != null) //DISPLAY REGISTER FRAG
                {
                    ft.replace(R.id.fragment_container, frag,"frag");
                    ft.commit();
                }
                else {
                    ft.replace(R.id.fragment_container, loginFrag);
                    //ft.addToBackStack(null); //allows user to press back button on phone to get rid of fragment
                    ft.commit();
                }
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the fragment, which will then pass the result to the login
        // button.
        loginFrag = (LoginFragment) getFragmentManager().findFragmentByTag("loginFrag");
        if (loginFrag != null) {
            loginFrag.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
