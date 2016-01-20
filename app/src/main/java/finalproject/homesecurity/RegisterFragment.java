package finalproject.homesecurity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import finalproject.homesecurity.model.User;

/**
 * Created by Robbie on 30/09/2015.
 */
public class RegisterFragment extends Fragment {
    private Button back,signup;
    private FragmentManager fragmentManager;
    private RegisterFragment frag;
    private LoginFragment loginFrag;
    private EditText regEmail,regPass;
    private ProgressDialog progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_layout,
                container, false);

        regEmail = (EditText) view.findViewById(R.id.register_email);
        regPass = (EditText) view.findViewById(R.id.register_password);
        regPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    signUp();
                    return true;
                }
                return false;
            }
        });

        back = (Button) view.findViewById(R.id.cancel_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });
        signup = (Button) view.findViewById(R.id.signup_button);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });
        return view;
    }

    private void signUp() {//handles error checking on the users register details
            if(isValidEmail(regEmail.getText()) && isValidPassword(regPass.getText().toString()))
            {
                progress = ProgressDialog.show(getActivity(), "Confirming Details",
                        "Please Wait..", true);
                insertUser(regEmail.getText().toString(),regPass.getText().toString());
            }
            else
            {
                if(!isValidEmail(regEmail.getText()))  //returns true if email is valid
                    regEmail.setError("Invalid email address");

                if(!isValidPassword(regPass.getText().toString())) //returns true if password is valid
                    regPass.setError("Invalid password\nPassword must be greater than 5 characters");
            }
        }

    public void insertUser(String email,String password) //sends users credentials to server to be assessed
    {
        final User newUser = new User(email, password);

        ListenableFuture<JsonElement> result = MainActivity.mClient.invokeApi( "CustomRegistration", newUser, JsonElement.class );
        System.out.println("HELLO-------------------");
        Futures.addCallback(result, new FutureCallback<JsonElement>() {
            @Override
            public void onFailure(Throwable exc) {
                System.out.println("FAILED");
                System.out.println(exc.getMessage().toString());
                //this gets rid of the json format so i am left with just the string message received from the Mobile Service
                displayRegistrationMessage(exc.getMessage().substring(12, exc.getMessage().length() - 2));
            }

            @Override
            public void onSuccess(JsonElement result) {
                if (result.isJsonObject()) {
                    JsonObject resultObj = result.getAsJsonObject();
                    displayRegistrationMessage(resultObj.get("message").getAsString());
                } else
                    displayRegistrationMessage(result.getAsString().toString());
            }
        });
    }


    public void displayRegistrationMessage(String message) //displays appropriate message to user after registration
    {
        progress.dismiss();
        if(message.equals("Created"))
        {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(),DecisionActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
        else if(message.equals("Fail"))
        {
            Toast.makeText(getActivity(),"This email already exists",Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(getActivity(),"A problem occurred and your request could not be completed\n" +
                    "Please check your internet connection",Toast.LENGTH_LONG).show();
    }


    public void goBack() { //gets rid of register fragment
        frag = (RegisterFragment) getFragmentManager().findFragmentByTag("frag");
        loginFrag = (LoginFragment) getFragmentManager().findFragmentByTag("loginFrag");
        if(frag != null)
        {
            fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.xml.enter_from_left, R.xml.exit_to_right);
            fragmentTransaction.replace(R.id.fragment_container, loginFrag,"loginFrag");
            fragmentTransaction.commit();
        }

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
