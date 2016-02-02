package ca.dmdev.petritrebs.wom;

import android.content.Intent;
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
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    public static CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;
    AccessToken accessToken;
    ProfileTracker profileTracker;
    TextView lblName;
    TextView lblID;
    TextView lblEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);

        lblName = (TextView) findViewById(R.id.lblName);
        lblID = (TextView) findViewById(R.id.lblID);
        lblEmail = (TextView) findViewById(R.id.lblEmail);

        Profile p;
        if ((p = Profile.getCurrentProfile()) != null){
            lblName.setText("Name: "+p.getName());
            lblID.setText("ID: "+p.getId());
            lblEmail.setText("Email: ");
            ((ProfilePictureView) findViewById(R.id.profilePicture)).setProfileId(p.getId());
        }

        //there is no checking if the user is logged in yet
        Button btnLogin = (Button)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFblogin();
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
            }
        };
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                // App code
            }
        };


        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        System.out.println("Success");
                        GraphRequest.newMeRequest(
                                loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject json, GraphResponse response) {
                                        if (response.getError() != null) {
                                            // handle error
                                            System.out.println("ERROR");
                                        } else {
                                            System.out.println("Success");
                                            System.out.println(response.toString());
                                            try {

                                                String jsonresult = String.valueOf(json);
                                                System.out.println("JSON Result" + jsonresult);

                                                String id = json.getString("id");
                                                lblID.setText("ID: "+id);
                                                String fullName = json.getString("name");
                                                lblName.setText("Name: "+fullName);

                                                String email = json.getString("email"); //this isnt working????
                                                lblEmail.setText("Email"+email);

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }).executeAsync();

                        Snackbar.make(findViewById(R.id.login_view), "Login Success!", Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancel() {
                        Snackbar.make(findViewById(R.id.login_view), "Login Cancelled!", Snackbar.LENGTH_LONG).show();
                        Log.d("WOM","On cancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Snackbar.make(findViewById(R.id.login_view), "Login Errored!", Snackbar.LENGTH_LONG).show();
                        Log.d("WOM", error.toString());
                    }
                });

    }

    // Private method to handle Facebook login and callback
    private void onFblogin()
    {
        // Set permissions and try to login
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile", "user_friends"));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

}
