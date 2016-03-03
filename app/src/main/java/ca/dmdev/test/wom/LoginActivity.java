package ca.dmdev.test.wom;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONObject;

import java.util.Arrays;

import ca.dmdev.test.wom.acccount.User;

public class LoginActivity extends AppCompatActivity {

    protected WordOfMouth wom;

    private static CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private Intent mainActivity;
    private JSONObject facebookUser;
    private JSONObject facebookFriends;

    private static final String TAG = LoginActivity.class.getName();
    private static final int REQUEST_LOGOUT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext()); //set up facebook SDK before setContentView!
        callbackManager = CallbackManager.Factory.create(); //callback manager too? seems to crash otherwise
        setContentView(R.layout.activity_login);

        wom = (WordOfMouth)getApplication();

        //try to get the old access token
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        //test if this is a returning user,
        if (accessToken != null){
            //update current data with the data on the server
            updateFacebookData(accessToken);
        }

        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set permissions and try to login
                LoginManager.getInstance().logInWithReadPermissions((Activity) v.getContext(), Arrays.asList("email", "public_profile", "user_friends"));
            }
        });


        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                // Set the access token using
                // currentAccessToken when it's loaded or set.
                if (currentAccessToken != null) {
                    updateFacebookData(currentAccessToken);//we need to do this threaded
                }
            }
        };

        //do we even need this?
        /*ProfileTracker profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                // App code
                if (currentProfile == null) {
                    Log.d(TAG, "onCurrentProfileChanged: currentProfile == null");
                } else {
                    Log.d(TAG, "onCurrentProfileChanged" + currentProfile.toString());
                }
            }
        };*/


        //this is what handles the callback for the facebook login activity
        LoginManager.getInstance().registerCallback(callbackManager,
            new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d(TAG, "onSuccess");
                    Log.d(TAG, "Access Token: " + loginResult.getAccessToken().getToken());
                    //This triggers onCurrentAccessTokenChanged() so code for handling it goes there
                    Snackbar.make(findViewById(R.id.login_view), "Logged in successfully!", Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void onCancel() {
                    Snackbar.make(findViewById(R.id.login_view), "Login Cancelled!", Snackbar.LENGTH_LONG).show();
                    Log.d(TAG, "On cancel");
                }

                @Override
                public void onError(FacebookException error) {
                    Snackbar.make(findViewById(R.id.login_view), "Login Error!", Snackbar.LENGTH_LONG).show();
                    Log.e(TAG, "FacebookCallback onError: " + error.toString());
                }
            }
        );
    }

    //this is triggered when the toolbar_menu activity is finish();ed or when the facebook activity returns here after login
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { //result of facebook login activity
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                if (data.getBooleanExtra("logout", false)) {
                    Log.d(TAG, "Logout intent from MainActivity");
                    LoginManager.getInstance().logOut();
                    Toast.makeText(getApplicationContext(), "Logged out.", Toast.LENGTH_LONG).show();
                }
                if (data.getBooleanExtra("backPressed", false)){
                    finish();
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                mainActivity = new Intent(this, MainActivity.class);
                startActivityForResult(mainActivity, REQUEST_LOGOUT);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    //update facebook data
    private void updateFacebookData(final AccessToken accessToken){

        final ProgressDialog dialog = new ProgressDialog(this); // this = YourActivity
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Loading. Please wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();

        //set up graph request for user info
        GraphRequest userInfo = GraphRequest.newMeRequest(
            accessToken, new GraphRequest.GraphJSONObjectCallback() { //passing access token, and callback?
                @Override
                public void onCompleted(JSONObject json, GraphResponse response) {
                    if (response.getError() != null) {
                        // handle error
                        Log.e(TAG, "onCompleted ERROR" + response.getError().toString());
                    } else {
                        facebookUser = json;

                        //request friends data
                        GraphRequest friendsGraph = new GraphRequest(
                            accessToken,
                            "me/friends",
                            null,
                            HttpMethod.GET,
                            new GraphRequest.Callback() {
                                public void onCompleted(GraphResponse response) {
                                    if (response.getError() != null) {
                                        // handle error
                                        Log.e(TAG, "onCompleted ERROR" + response.getError().toString());
                                    } else {
                                        facebookFriends = response.getJSONObject();

                                        //initialize the user
                                        User.getInstance().setUserFromJSON(facebookUser, facebookFriends);

                                        mainActivity = new Intent(getApplicationContext(), MainActivity.class); //prep starting toolbar_menu activity
                                        startActivityForResult(mainActivity, REQUEST_LOGOUT); //start it
                                        dialog.cancel();
                                    }
                                }
                            }
                        );
                        friendsGraph.executeAsync();
                    }
                }
            }
        );
        Bundle parameters = new Bundle(); //add params to request from facebook
        parameters.putString("fields", "id, first_name, last_name, email, gender");
        userInfo.setParameters(parameters);
        userInfo.executeAsync(); //execute
        //result of the async is processed by onCompleted() above
    }

}
