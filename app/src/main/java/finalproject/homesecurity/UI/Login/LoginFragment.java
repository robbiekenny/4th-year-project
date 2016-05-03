package finalproject.homesecurity.UI.Login;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.util.Log;
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

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import finalproject.homesecurity.R;
import finalproject.homesecurity.UI.SetUpSecurityOrPersonal.DecisionActivity;
import finalproject.homesecurity.UI.MainActivity;
import finalproject.homesecurity.UI.Register.RegisterFragment;
import finalproject.homesecurity.model.User;

/**
 * Created by Robbie on 19/01/2016.
 * THIS CLASS TAKES THE USERS EMAIL AND PASSWORD AND COMPARES IT AGAINST THE DATABASE TO LOOK FOR A MATCH
 * IF A MATCH IS FOUND THEN WE WILL RETURN THE EMAIL ADDRESS,A TOKEN, AND WHETHER OR NOT THIS USERS EMAIL ADDRESS
 * HAS BEEN VERIFIED
 */
public class LoginFragment extends Fragment {
    private EditText email,password;
    private Button signin,signup;
    private ProgressDialog progress;
    private RegisterFragment frag;
    private FragmentManager fragmentManager;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private SharedPreferences sharedPref;
    private TextInputLayout emailTextInput,passwordTextInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());

        View view = inflater.inflate(R.layout.login_layout,
                container, false);
        sharedPref = getActivity().getSharedPreferences("AuthenticatedUserDetails", Context.MODE_PRIVATE);

        callbackManager = CallbackManager.Factory.create();

        emailTextInput = (TextInputLayout) view.findViewById(R.id.emailTextInput);
        passwordTextInput = (TextInputLayout) view.findViewById(R.id.passwordTextInput);


        email = (EditText) view.findViewById(R.id.email);
        password = (EditText) view.findViewById(R.id.password);
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    errorCheck();
                    handled = true;
                }
                return handled;
            }
        });
        //allow user to see their password
        password.setOnTouchListener(new View.OnTouchListener() { //allows user to see their password
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(event.getRawX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        return true;
                    }
                }
                else
                    password.setInputType(129); //129 is the input type set when setting android:inputType="textPassword"

                return false;
            }
        });
        signin = (Button) view.findViewById(R.id.signIn);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorCheck();
            }
        });




        signup = (Button) view.findViewById(R.id.signUp);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        //Facebook Login
        loginButton = (LoginButton) view.findViewById(R.id.fb_login_button);
        loginButton.setReadPermissions("email");
        // If using in a fragment
        loginButton.setFragment(this);
        // Other app specific specialization

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("FACEBOOK LOGIN", loginResult.getAccessToken().toString() + "," + loginResult.getAccessToken().getUserId());

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                // Application code
                                Log.v("LoginActivity", response.toString());
                                try {
                                    if (object.getString("email") != null || object.getString("email") != "") {
                                        Log.v("EMAIL", object.getString("email"));

                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putString("userId", object.getString("email"));
                                        editor.putString("loginType", "facebook");
                                        editor.putBoolean("verified", true);
                                        editor.commit();

                                        startNextActivity();
                                    } else {
                                        //turn this into a alert dialog of some sort
                                        Toast.makeText(getActivity(), "Could not sign in with Facebook", Toast.LENGTH_LONG).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.e("FB LOGIN CANCELLED", "Login cancelled");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e("FACEBOOK LOGIN ERROR", exception.toString());
                Toast.makeText(getActivity(), "Could not sign in with Facebook", Toast.LENGTH_LONG).show();
            }

        });

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 64206)
            callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    private void createAccount() {  //displays the register fragment
        frag = (RegisterFragment) getFragmentManager().findFragmentByTag("frag");
        if(frag == null)
        {
            fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            //fragmentTransaction.setCustomAnimations(R.xml.enter_from_left, R.xml.exit_to_right);
            frag = new RegisterFragment();
            fragmentTransaction.replace(R.id.fragment_container, frag, "frag");
            fragmentTransaction.addToBackStack(null); //allows user to press back button on phone to get rid of fragment
            fragmentTransaction.commit();
        }
        else
        {
            fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            //ft.setCustomAnimations(R.xml.enter_from_left, R.xml.exit_to_right);
            ft.replace(R.id.fragment_container, frag,"frag");
            ft.commit();
        }
    }

    public void errorCheck() //simple error checks on email and password fields
    {
        if(isValidEmail(email.getText().toString()) && isValidPassword(password.getText().toString()))
            signIn();
        else
        {
            if(!isValidEmail(email.getText().toString()))
                emailTextInput.setError("Valid email required");
            else
                emailTextInput.setError(null);
            if(!isValidPassword(password.getText().toString()))
                passwordTextInput.setError("Password must be greater than 5 characters");
            else
                passwordTextInput.setError(null);

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

    public void signIn() //sends credentials to server
    {
        progress = ProgressDialog.show(getActivity(), "Logging in",
                "Please Wait..", true);
        final User newUser = new User(email.getText().toString(), password.getText().toString());


        ListenableFuture<JsonElement> result = MainActivity.mClient.invokeApi( "CustomLogin", newUser, JsonElement.class );
        Futures.addCallback(result, new FutureCallback<JsonElement>() {
            @Override
            public void onFailure(Throwable exc) {
                System.out.println("FAILED");
                System.out.println(exc.getMessage().toString());
                //this gets rid of the json format so i am left with just the string message received from the Mobile Service
                signinResult(exc.getMessage().substring(12, exc.getMessage().length() - 2), "");
            }

            @Override
            public void onSuccess(JsonElement result) {
                if (result.isJsonObject()) {
                    JsonObject resultObj = result.getAsJsonObject();
                    System.out.println(resultObj.get("userId").getAsString());
                    System.out.println(resultObj.get("mobileServiceAuthenticationToken").getAsString());
                    System.out.println(resultObj.get("verified").getAsBoolean());

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("verified", resultObj.get("verified").getAsBoolean());
                    editor.commit();

                    signinResult("SignedIn", resultObj.get("mobileServiceAuthenticationToken").getAsString());
                } else
                    signinResult("SignedIn", "");
            }
        });
    }

    public void signinResult(String message,String token) //determines what happens on successful or failed sign in
    {
        progress.dismiss();
        if(message.equals("SignedIn"))
        {
            if(token != "")
            {

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("userId", email.getText().toString());
                editor.putString("mobileServiceAuthenticationToken", token);
                editor.putString("loginType", "custom");
                editor.commit();

                startNextActivity();
            }


        }
        else if(message.equals("Fail"))
            Toast.makeText(getActivity(),"Incorrect email or password",Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getActivity(),"Please check your internet connection",Toast.LENGTH_LONG).show();
    }

    public void startNextActivity() //starts the next activity and finishes the current one
    { //using this method avoids duplicate code
        Toast.makeText(getActivity(),"Successful sign in",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(),DecisionActivity.class);
        intent.putExtra("comingFrom", "loggingIn");
        startActivity(intent);
        getActivity().finish();
    }
}
