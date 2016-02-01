package finalproject.homesecurity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;

import finalproject.homesecurity.Utils.GCMRegistration;
import finalproject.homesecurity.model.User;

/**
 * Created by Robbie on 19/01/2016.
 */
public class LoginFragment extends Fragment {
    private EditText email,password;
    private Button signin;
    private ProgressDialog progress;
    private RegisterClient registerClient;
    private RegisterFragment frag;
    private GoogleCloudMessaging gcm;
    private FragmentManager fragmentManager;
    private GCMRegistration gcmReg; //responsible for invoking the registerClientForGCM method
    private FloatingActionButton fab;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());

        View view = inflater.inflate(R.layout.login_layout,
                container, false);

        callbackManager = CallbackManager.Factory.create();

        registerClient = MainActivity.registerClient;
        gcm = MainActivity.gcm;

        gcmReg = new GCMRegistration();

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
        signin = (Button) view.findViewById(R.id.signIn);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorCheck();
            }
        });


        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
                fab.hide();
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
                                        SharedPreferences sharedPref = getActivity().
                                                getSharedPreferences("AuthenticatedUserDetails", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putString("userId", object.getString("email"));
                                        editor.putString("loginType", "facebook");
                                        editor.commit();

                                        Toast.makeText(getActivity(), "Successful sign in", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getActivity(), DecisionActivity.class);
                                        startActivity(intent);
                                        getActivity().finish();
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

        //Google Sign in
        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
               GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
// options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();

        SignInButton signInButton = (SignInButton) view.findViewById(R.id.g_sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("HERE");
                googleSignIn();
            }
        });
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());

        return view;
    }

    private void googleSignIn() {
        System.out.println("HERE2");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println("HERE3 request code: " + requestCode);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            System.out.println("HERE3");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        else
            callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("GOOGLE LOG IN", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.d("GOOGLE LOG IN", "handleSignInResult:" + acct.getDisplayName());
        } else {
            // Signed out, show unauthenticated UI.
            Log.d("GOOGLE LOG OUT", "handleSignInResult:" + result.getStatus());
        }
    }

    private void createAccount() {  //displays the register fragment
        frag = (RegisterFragment) getFragmentManager().findFragmentByTag("frag");
        if(frag == null)
        {
            fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.xml.enter_from_left, R.xml.exit_to_right);
            frag = new RegisterFragment();
            fragmentTransaction.replace(R.id.fragment_container, frag, "frag");
            //fragmentTransaction.addToBackStack(null); //allows user to press back button on phone to get rid of fragment
            fragmentTransaction.commit();
        }
        else
        {
            fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.setCustomAnimations(R.xml.enter_from_left, R.xml.exit_to_right);
            ft.replace(R.id.fragment_container, frag);
            //ft.addToBackStack(null); //allows user to press back button on phone to get rid of fragment
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
                email.setError("Valid email required");
            if(!isValidPassword(password.getText().toString()))
                password.setError("Password must be greater than 5 characters");
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
        System.out.println("HELLO-------------------");
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
//            try { //Disabled because notification hubs costs loads
//                gcmReg.registerClientForGCM(registerClient, email.getText().toString(), getActivity(), gcm);
//            } catch (UnsupportedEncodingException e) {
//                System.out.println("FAILED TO REGISTER FOR GCM");
//                e.printStackTrace();
//            }
            if(token != "")
            {
                SharedPreferences sharedPref = getActivity().getSharedPreferences("AuthenticatedUserDetails", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("userId", email.getText().toString());
                editor.putString("mobileServiceAuthenticationToken", token);
                editor.putString("loginType", "custom");
                editor.commit();

                Toast.makeText(getActivity(),"Successful sign in",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(),DecisionActivity.class);
                startActivity(intent);
                getActivity().finish();
            }


        }
        else if(message.equals("Fail"))
            Toast.makeText(getActivity(),"Incorrect email or password",Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getActivity(),"Please check your internet connection",Toast.LENGTH_LONG).show();
    }
}
