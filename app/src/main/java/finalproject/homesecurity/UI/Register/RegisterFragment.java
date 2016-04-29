package finalproject.homesecurity.UI.Register;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import finalproject.homesecurity.R;
import finalproject.homesecurity.UI.SetUpSecurityOrPersonal.DecisionActivity;
import finalproject.homesecurity.UI.MainActivity;
import finalproject.homesecurity.model.User;

/**
 * Created by Robbie on 30/09/2015.
 *RESPONSIBLE FOR REGISTERING USERS
 */
public class RegisterFragment extends Fragment {
    private Button signup;
    private EditText regEmail,regPass;
    private ProgressDialog progress;
    private SharedPreferences sharedPref;
    private TextInputLayout emailTextInput,passwordTextInput;

    /***************************************************************************************
     *    Title: Android to detect when you are holding down a button
     *    Author: Sam
     *    Date: 20/2/2016
     *    Code version: 1
     *    Availability: http://stackoverflow.com/questions/11713642/android-to-detect-when-you-are-holding-down-a-button
     *
     ***************************************************************************************/

    /***************************************************************************************
     *    Title: Show the password with EditText
     *    Author: josephus
     *    Date: 20/2/2016
     *    Code version: 1
     *    Availability: http://stackoverflow.com/questions/9307680/show-the-password-with-edittext
     *
     ***************************************************************************************/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_layout,
                container, false);

        sharedPref = getActivity().getSharedPreferences("AuthenticatedUserDetails", Context.MODE_PRIVATE);

        emailTextInput = (TextInputLayout) view.findViewById(R.id.register_emailTextInput);
        passwordTextInput = (TextInputLayout) view.findViewById(R.id.register_passwordTextInput);

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

        //alows the user to see their password if they want
        regPass.setOnTouchListener(new View.OnTouchListener() { //allows user to see their password
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (event.getRawX() >= (regPass.getRight() - regPass.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        regPass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        return true;
                    }
                } else
                    regPass.setInputType(129); //129 is the input type set when setting android:inputType="textPassword"

                return false;
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
                    emailTextInput.setError("Invalid email address");
                else
                    emailTextInput.setError(null);

                if(!isValidPassword(regPass.getText().toString())) //returns true if password is valid
                    passwordTextInput.setError("Invalid password\nPassword must be greater than 5 characters");
                else
                    passwordTextInput.setError(null);
            }
        }

    public void insertUser(final String email,String password) //sends users credentials to server to be assessed
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
                displayRegistrationMessage(exc.getMessage().substring(12, exc.getMessage().length() - 2), email);
            }

            @Override
            public void onSuccess(JsonElement result) {
                if (result.isJsonObject()) {
                    JsonObject resultObj = result.getAsJsonObject();
                    displayRegistrationMessage(resultObj.get("message").getAsString(), email);

                } else
                    displayRegistrationMessage(result.getAsString().toString(), email);
            }
        });
    }


    public void displayRegistrationMessage(String message,String email) //displays appropriate message to user after registration
    {
        progress.dismiss();
        if(message.equals("Created"))
        {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("userId", email);
            editor.putString("loginType", "custom");
            editor.putBoolean("verified",false);
            editor.commit();

            Intent intent = new Intent(getActivity(),DecisionActivity.class);
            intent.putExtra("comingFrom", "registering"); //allows next activity to display register dialog
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
