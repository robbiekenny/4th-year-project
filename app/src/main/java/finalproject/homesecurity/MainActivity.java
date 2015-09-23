package finalproject.homesecurity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import java.net.MalformedURLException;

import finalproject.homesecurity.model.User;

public class MainActivity extends ActionBarActivity {
    private boolean signedIn = true;
    private MobileServiceClient mClient;
    private Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


//        h.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                User item = new User("test","test");
//                mClient.getTable(User.class).insert(item, new TableOperationCallback<User>() {
//                    public void onCompleted(User entity, Exception exception, ServiceFilterResponse response) {
//                        if (exception == null) {
//                            // Insert succeeded
//                            System.out.println("SUCCESS");
//                        } else {
//                            // Insert failed
//                            System.out.println("FAILED");
//                            exception.printStackTrace();
//                        }
//                    }
//                });
//            }
//        }, 3000);



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
}
