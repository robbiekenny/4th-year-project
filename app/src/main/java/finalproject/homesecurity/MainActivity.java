package finalproject.homesecurity;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.net.MalformedURLException;
import java.util.ArrayList;

import finalproject.homesecurity.Utils.CleanUserId;
import finalproject.homesecurity.Utils.GCMRegistration;
import finalproject.homesecurity.model.User;

public class MainActivity extends ActionBarActivity { //deals with sign in and register (should split the two later on for cleaner code)
    public static MobileServiceClient mClient;
    private RegisterFragment frag;
    private LoginFragment loginFrag;
    private FragmentManager fragmentManager;
    private FrameLayout container;
    private ProgressDialog progress;
    public static RegisterClient registerClient;
    public static GoogleCloudMessaging gcm;
    private Toolbar toolbar;
    private GCMRegistration gcmReg;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

//            try { //Disabled because notification hubs costs loads
//                gcmReg.registerClientForGCM(registerClient,user,this,gcm);
//            } catch (UnsupportedEncodingException e) {
//                System.out.println("FAILED TO REGISTER FOR GCM");
//                e.printStackTrace();
//            }
            Intent intent = new Intent(this,DecisionActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            //http://www.android4devs.com/2014/12/how-to-make-material-design-app.html
            toolbar = (Toolbar) findViewById(R.id.tool_bar);
            setSupportActionBar(toolbar);
            container = (FrameLayout) findViewById(R.id.fragment_container);
            loginFrag = (LoginFragment) getFragmentManager().findFragmentByTag("loginFrag");
            if(loginFrag == null)
            {
                fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                loginFrag = new LoginFragment();
                fragmentTransaction.add(R.id.fragment_container, loginFrag, "loginFrag");
                fragmentTransaction.addToBackStack(null); //allows user to press back button on phone to get rid of fragment
                fragmentTransaction.commit();
            }
            else
            {
                fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.fragment_container, loginFrag);
                ft.addToBackStack(null); //allows user to press back button on phone to get rid of fragment
                ft.commit();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
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
                fragmentTransaction.setCustomAnimations(R.xml.enter_from_left, R.xml.exit_to_right);
                fragmentTransaction.replace(R.id.fragment_container, loginFrag,"loginFrag");
                //fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
