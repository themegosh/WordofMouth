package ca.dmdev.petritrebs.wom;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    public static CallbackManager callbackManager;
    LoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_login);

        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Call private method
                onFblogin();
            }
        });


    }

    // Private method to handle Facebook login and callback
    private void onFblogin()
    {
        callbackManager = CallbackManager.Factory.create();

        // Set permissions
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile", "user_friends"));

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

                                            String email = json.getString("email");
                                            String id = json.getString("id");
                                            String fullName = json.getString("name");

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


}
