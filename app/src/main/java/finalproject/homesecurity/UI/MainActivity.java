package finalproject.homesecurity.UI;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import finalproject.homesecurity.Constants;
import finalproject.homesecurity.R;
import finalproject.homesecurity.UI.Login.LoginFragment;
import finalproject.homesecurity.UI.Register.RegisterFragment;
import finalproject.homesecurity.UI.SetUpSecurityOrPersonal.DecisionActivity;
import io.fabric.sdk.android.Fabric;

import com.microsoft.windowsazure.mobileservices.*;

import java.net.MalformedURLException;

/*
CONTAINS THE LOG IN AND REGISTER FRAGMENTS
 */

public class MainActivity extends ActionBarActivity {
    public static MobileServiceClient mClient;
    private RegisterFragment frag;
    private LoginFragment loginFrag;
    private FragmentManager fragmentManager;

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

        SharedPreferences sharedPref = getSharedPreferences("AuthenticatedUserDetails", Context.MODE_PRIVATE);
        String user = sharedPref.getString("userId",null);

        if(user != null) //checking to see if this user is already signed in
        {
            Intent intent = new Intent(this,DecisionActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {

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
}
