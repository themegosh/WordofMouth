package ca.dmdev.petritrebs.wom;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

import ca.dmdev.petritrebs.wom.acccount.User;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

            //open the new activity, passing the user's data
            mainActivity = new Intent(this, MainActivity.class);
            startActivityForResult(mainActivity, REQUEST_LOGOUT);
        }

        //if we get this far, it means the user has either logged out or is new

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
                if (currentAccessToken == null) {
                    Log.d(TAG, "onCurrentAccessTokenChanged: currentAccessToken == null");
                } else { //we have a token
                    Log.d(TAG, "onCurrentAccessTokenChanged: " + currentAccessToken.getToken());
                    updateFacebookData(currentAccessToken);//we need to do this threaded
                    mainActivity = new Intent(getApplicationContext(), MainActivity.class); //prep starting main activity
                    startActivityForResult(mainActivity, REQUEST_LOGOUT); //start it
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

    //this is triggered when the main activity is finish();ed or when the facebook activity returns here after login
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

        //set up graph request for user info
        GraphRequest userInfo = GraphRequest.newMeRequest(
            accessToken, new GraphRequest.GraphJSONObjectCallback() { //passing access token, and callback?
                @Override
                public void onCompleted(JSONObject json, GraphResponse response) {
                    if (response.getError() != null) {
                        // handle error
                        Log.e(TAG, "onCompleted ERROR" + response.getError().toString());
                    } else {
                        Log.d(TAG, "onCompleted Success");
                        Log.d(TAG, "Response: " + response.toString());
                        Log.d(TAG, "json: " + json.toString());
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
                                        Log.d(TAG, "=================FRIENDS=====================");
                                        facebookFriends = response.getJSONObject();
                                        Log.d(TAG, facebookFriends.toString());

                                        //this code will need to go elsewhere
                                        /*try {
                                            Log.d(TAG, response.getJSONObject().getJSONArray("data").toString());
                                        }
                                        catch (Exception e)
                                        {
                                            Log.e(TAG, e.getMessage());
                                        }*/


                                        new UpdateExternalDb().execute(facebookUser, facebookFriends);
                                        User.getInstance().setUserFromJSON(facebookUser);
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

    }



    private class UpdateExternalDb extends AsyncTask<JSONObject, Void, String> {
        private static final String TAG = "UpdateExternalDb";
        public final String SERVER_URL = "http://wom.dmdev.ca/process.php";
        //public final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(JSONObject... params) {

            try {

                if (params.length == 2){
                    if (params[0] == null || params[1] == null){
                        Log.e(TAG, "This shouldn't happen. Both facebookUser and/or facebookFriends is empty!!!!");
                    } else {
                        Log.d(TAG, "==== BEGIN UPLOADING TO WEB SERVER ====");
                        Log.d(TAG, "json data to send: " + params[0].toString());

                        //send the user info to the server
                        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("action", "add_update_user")
                                .addFormDataPart("user_data", params[0].toString())
                                .addFormDataPart("friend_data", params[1].toString())
                                .build();

                        Request request = new Request.Builder()
                                .url(SERVER_URL)
                                .post(requestBody)
                                .build();

                        Response response = client.newCall(request).execute();
                        Log.d(TAG, "Response: " + response.body().string());


                    }

                    //params[0].getString("first_name");
                }




                //Create an HTTP client
                /*HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(SERVER_URL);

                //Perform the request and check the status code
                HttpResponse response = client.execute(post);
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();

                    try {
                        //Read the server response and attempt to parse it as JSON
                        Reader reader = new InputStreamReader(content);

                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.setDateFormat("M/d/yy hh:mm a");
                        Gson gson = gsonBuilder.create();
                        List<Post> posts = new ArrayList<Post>();
                        posts = Arrays.asList(gson.fromJson(reader, Post[].class));
                        content.close();

                        handlePostsList(posts);
                    } catch (Exception ex) {
                        Log.e(TAG, "Failed to parse JSON due to: " + ex);
                        failedLoadingPosts();
                    }
                } else {
                    Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
                    failedLoadingPosts();
                }*/
            } catch(Exception ex) {
                Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
                ex.printStackTrace();
            }
            return null;
        }
    }

    /*private Bundle getFacebookData(JSONObject object) {

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
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }*/

}
