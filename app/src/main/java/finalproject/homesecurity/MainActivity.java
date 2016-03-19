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
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.Button;
import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;
import java.io.UnsupportedEncodingException;
import android.content.Context;
import java.util.HashSet;
import android.widget.Toast;


import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;
import com.microsoft.windowsazure.notifications.NotificationsManager;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.net.MalformedURLException;

import finalproject.homesecurity.Utils.GCMRegistration;

public class MainActivity extends ActionBarActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "bQI11wMVmjerVzK3fUtE0Mz3N";
    private static final String TWITTER_SECRET = "yhnuJBfI8GD2FEwTyhSwXs9n83vfhfvQeIO9HImEXAj0nkl1n9";
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
//    private TwitterLoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
//        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
//        Fabric.with(this, new Twitter(authConfig));
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
        else if(id == android.R.id.home)
        {
            frag = (RegisterFragment) getFragmentManager().findFragmentByTag("frag");
            loginFrag = (LoginFragment) getFragmentManager().findFragmentByTag("loginFrag");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            if(frag != null)
            {
                fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                //fragmentTransaction.setCustomAnimations(R.xml.enter_from_left, R.xml.exit_to_right);
                fragmentTransaction.replace(R.id.fragment_container, loginFrag,"loginFrag");
                //fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
