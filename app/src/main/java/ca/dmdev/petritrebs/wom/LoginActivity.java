package ca.dmdev.petritrebs.wom;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookActivity;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.FacebookException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    public static CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;
    AccessToken accessToken;
    ProfileTracker profileTracker;

    private static final String TAG = LoginActivity.class.getName();
    private static final String SHARED_FACEBOOK_TOKEN = "FacebookToken";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext()); //set up facebook SDK before setContentView!
        callbackManager = CallbackManager.Factory.create(); //callback manager too? seems to crash otherwise
        setContentView(R.layout.activity_login);

        //there is no checking if the user is logged in yet
        Button btnLogin = (Button)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set permissions and try to login
                LoginManager.getInstance().logInWithReadPermissions((Activity) v.getContext(), Arrays.asList("email", "public_profile", "user_friends"));
            }
        });

        //there is no checking if the user is logged in yet
        Button btnLogout = (Button)findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call private method
                LoginManager.getInstance().logOut();
                Snackbar.make(findViewById(R.id.login_view), "Logged out!", Snackbar.LENGTH_LONG).show();
            }
        });


        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                // Set the access token using
                // currentAccessToken when it's loaded or set.
                if (currentAccessToken == null){
                    Log.d(TAG, "onCurrentAccessTokenChanged: currentAccessToken == null");
                }
                else { //we have a token

                    Log.d(TAG, "onCurrentAccessTokenChanged: " + currentAccessToken.getToken());

                    updateFacebookData(currentAccessToken);

                }
            }
        };
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();
        updateFacebookData(accessToken);

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                // App code
                if (currentProfile == null){
                    Log.d(TAG, "onCurrentProfileChanged: currentProfile == null");
                }
                else {
                    Log.d(TAG, "onCurrentProfileChanged" + currentProfile.toString());
                }
            }
        };


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
                });



    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { //result of facebook login activity
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
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
    private void updateFacebookData(AccessToken accessToken){
        //set up graph request
        GraphRequest request = GraphRequest.newMeRequest(
            accessToken, new GraphRequest.GraphJSONObjectCallback() { //passing access token, and callback?
                @Override
                public void onCompleted(JSONObject json, GraphResponse response) {
                    if (response.getError() != null) {
                        // handle error
                        Log.e(TAG, "onCompleted ERROR" + response.getError().toString());
                    } else {
                        Log.d(TAG, "onCompleted Success");
                        Log.d(TAG, "Response: " + response.toString());
                        Bundle loginData = getFacebookData(json);
                        Log.d(TAG, "Bundle: " + loginData.toString());

                        ///todo: Add/update login info from bFacebookData

                        ///todo: get friends list

                        new GraphRequest(
                                AccessToken.getCurrentAccessToken(),
                                "me/friends",
                                null,
                                HttpMethod.GET,
                                new GraphRequest.Callback() {
                                    public void onCompleted(GraphResponse response) {
                                        Log.d(TAG, "=================FRIENDS=====================");
                                        Log.d(TAG, response.toString());
                                        Log.d(TAG, "=================FRIENDS JSON OBJ=====================");
                                        Log.d(TAG, response.getJSONObject().toString());
                                        try {
                                            Log.d(TAG, response.getJSONObject().getJSONArray("data").toString());
                                        }
                                        catch (Exception e)
                                        {
                                            Log.e(TAG, e.getStackTrace().toString());
                                        }
                                    }
                                }
                        ).executeAsync();

                    }
                }
            }
        );

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, last_name, email, gender");
        request.setParameters(parameters);
        request.executeAsync();
    }

    /*private class FriendsRequestListener implements RequestListener {
        String friendData;

        //Method runs when request is complete
        public void onComplete(String response, Object state) {
            Log.v("", "FriendListRequestONComplete");
            //Create a copy of the response so i can be read in the run() method.
            friendData = response;
            Log.v("friendData--", ""+friendData);
            //Create method to run on UI thread
            LoginActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        //Parse JSON Data
                        JSONObject json;
                        json = Util.parseJson(friendData);

                        //Get the JSONArry from our response JSONObject
                        JSONArray friendArray = json.getJSONArray("data");

                        Log.v("friendArray--", ""+friendArray);

                        for(int i = 0; i< friendArray.length(); i++)
                        {
                            JSONObject frnd_obj = friendArray.getJSONObject(i);
                            friends.add(frnd_obj.getString("name")+"~~~"+frnd_obj.getString("id"));
                        }

                        Intent ide = new Intent(LoginActivity.this,FrndActivity.class);
                        ide.putStringArrayListExtra("friends", friends);
                        //  ide.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(ide);

                        //  ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1,android.R.id.text1, friends_list);
                        //   lv.setAdapter(adapter);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
            });
        }*/

    private Bundle getFacebookData(JSONObject object) {

        try {
            Bundle bundle = new Bundle();
            String id = object.getString("id");

            try {
                URL profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?width=200&height=150");
                bundle.putString("profile_pic", profile_pic.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            bundle.putString("id", id);
            if (object.has("first_name"))
                bundle.putString("first_name", object.getString("first_name"));
            if (object.has("last_name"))
                bundle.putString("last_name", object.getString("last_name"));
            if (object.has("email"))
                bundle.putString("email", object.getString("email"));
            if (object.has("gender"))
                bundle.putString("gender", object.getString("gender"));

            return bundle;
        } catch (Exception e) {
            Log.d(TAG, e.getStackTrace().toString());
            return null;
        }
    }

}
