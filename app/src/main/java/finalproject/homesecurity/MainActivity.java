package finalproject.homesecurity;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import java.net.MalformedURLException;

import finalproject.homesecurity.model.User;

public class MainActivity extends ActionBarActivity {
    private boolean signedIn = true;
    private Handler h = new Handler();
    private static final String SENDER_ID = "1017315118157";
    public static MobileServiceClient mClient;
    private RegisterFragment frag;
    private FragmentManager fragmentManager;
    EditText email;
    EditText password;
    Button signin;
    Button register;

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

    public void signIn(View v)
    {
        System.out.println("Sign in method called");
        if(signedIn)
        {
            Intent intent = new Intent(this,DecisionActivity.class);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this,"Could not sign in",Toast.LENGTH_LONG).show();
        }
    }

    public void register(View v)
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
    }

    public void signup(View v){
        EditText ed = (EditText) findViewById(R.id.register_email);
        EditText pa = (EditText) findViewById(R.id.register_password);

        if(!isValidEmail(ed.getText()))  //returns true if email is valid
            ed.setError("Invalid email address");

       if(!isValidPassword(pa.getText().toString())) //returns true if password is valid
            pa.setError("Invalid password\nPassword must be greater than 5 characters");

    }

    public boolean isValidPassword(String pass)
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
