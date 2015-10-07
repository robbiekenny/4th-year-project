package finalproject.homesecurity;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import java.net.MalformedURLException;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {
    //private static final String SENDER_ID = "1017315118157";
    public static MobileServiceClient mClient;
    private RegisterFragment frag;
    private FragmentManager fragmentManager;
    private EditText email;
    private EditText password;
    private Button signin;
    private Button register;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         email = (EditText) findViewById(R.id.email);
         password = (EditText) findViewById(R.id.password);
         signin = (Button) findViewById(R.id.signIn);
         register = (Button) findViewById(R.id.register);

        try {
            mClient = new MobileServiceClient(
                    "https://homesecurityapp.azure-mobile.net/",
                    "rKdicilUIMYXfBBlUwydBGGiNqzJPZ93",
                    MainActivity.this
            );
        } catch (MalformedURLException e) {
            System.out.println("ERROR STARTING MOBILE SERVICE CLIENT");
            e.printStackTrace();
        }

        //NotificationsManager.handleNotifications(this, SENDER_ID, MyHandler.class);
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

    public void signInErrorChecking(View v) //simple error checks on email and password fields
    {
        if(isValidEmail(email.getText().toString()) && isValidPassword(password.getText().toString()))
            signIn();
        else
        {
            if(!isValidEmail(email.getText().toString()))
                email.setError("Valid email required");
            if(!isValidPassword(password.getText().toString()))
                password.setError("Password must be greater than 5 characters");
        }
    }

    public void signIn() //sends credentials to server
    {
        progress = ProgressDialog.show(this, "Logging in",
                "Please Wait..", true);
        ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<>("id", email.getText().toString()));
        parameters.add(new Pair<>("password", password.getText().toString()));
        ListenableFuture<JsonElement> result = mClient.invokeApi("login", null, "POST", parameters);

        Futures.addCallback(result, new FutureCallback<JsonElement>() {
            @Override
            public void onFailure(Throwable exc) {
                //could be a bad internet connection
                System.out.println("FAILED---------------------");
                signinResult("Failed");
            }

            @Override
            public void onSuccess(JsonElement result) {
                System.out.println(result);
                signinResult(result.getAsString().toString());
            }
        });
    }

    public void signinResult(String message) //determines what happens on successful or failed sign in
    {
        progress.dismiss();
        if(message.equals("SignedIn"))
        {
            Toast.makeText(this,"Successful sign in",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this,DecisionActivity.class);
            startActivity(intent);
            finish();
        }
        else if(message.equals("Fail"))
            Toast.makeText(this,"Incorrect email or password",Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this,"Please check your internet connection",Toast.LENGTH_LONG).show();
    }

    public void register(View v) //displays the register fragment while hiding other GUI components
    {
        email.setVisibility(View.INVISIBLE);
        password.setVisibility(View.INVISIBLE);
        signin.setVisibility(View.INVISIBLE);
        register.setVisibility(View.INVISIBLE);

        frag = (RegisterFragment) getFragmentManager().findFragmentByTag("frag");
        if(frag == null)
        {
            fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            frag = new RegisterFragment();
            fragmentTransaction.add(R.id.fragment_container, frag, "frag");
            fragmentTransaction.commit();
        }
        else
        {
            fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.fragment_container, frag);
            ft.commit();
        }
    }

    public void cancel(View v){
        //remove register fragment from view
        fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(frag);
        fragmentTransaction.commit();

        email.setVisibility(View.VISIBLE);
        password.setVisibility(View.VISIBLE);
        signin.setVisibility(View.VISIBLE);
        register.setVisibility(View.VISIBLE);
    } //gets rid of the register fragment

    public void errorCheckingForSignup(View v) //handles error checking on the users register details
    {
        EditText ed = (EditText) findViewById(R.id.register_email);
        EditText pa = (EditText) findViewById(R.id.register_password);

        if(isValidEmail(ed.getText()) && isValidPassword(pa.getText().toString()))
        {
            System.out.println("HERE-------------------");

            progress = ProgressDialog.show(this, "Confirming Details",
                    "Please Wait..", true);
            insertUser(ed.getText().toString(),pa.getText().toString());
        }
        else
        {
            if(!isValidEmail(ed.getText()))  //returns true if email is valid
                ed.setError("Invalid email address");

            if(!isValidPassword(pa.getText().toString())) //returns true if password is valid
                pa.setError("Invalid password\nPassword must be greater than 5 characters");
        }
    }

    public void insertUser(String email,String password) //sends users credentials to server to be assessed
    {
        ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<>("id", email));
        parameters.add(new Pair<>("password", password));
        ListenableFuture<JsonElement> result = mClient.invokeApi("register", null, "POST", parameters);

        Futures.addCallback(result, new FutureCallback<JsonElement>() {
            @Override
            public void onFailure(Throwable exc) {
                //could be a bad internet connection
                System.out.println("FAILED---------------------");
                displayRegistrationMessage("Failed");
            }

            @Override
            public void onSuccess(JsonElement result) {
                System.out.println(result);
                displayRegistrationMessage(result.getAsString().toString());
            }
        });
    }


    public void displayRegistrationMessage(String message) //displays appropriate message to user after registration
    {
        progress.dismiss();
        if(message.equals("Success"))
        {
            Toast.makeText(MainActivity.this,message,Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this,DecisionActivity.class);
            startActivity(intent);
            finish();
        }
        else if(message.equals("Fail"))
        {
            Toast.makeText(this,"This email already exists",Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(this,"A problem occurred and your request could not be completed\n" +
                    "Please check your internet connection",Toast.LENGTH_LONG).show();
    }

    public boolean isValidPassword(String pass) //password must be greater than 5 characters
    {
        boolean isValid = true;
        if(pass == "" || pass == null || pass.length() < 6)
            isValid =  false;
        return isValid;
    }

    public boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

}
