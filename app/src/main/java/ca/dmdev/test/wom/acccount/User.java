package ca.dmdev.test.wom.acccount;

import android.util.Log;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import ca.dmdev.test.wom.api.SendUserUpdate;

/**
 * Created by mathe_000 on 2016-02-08.
 */
public class User {

    public static User instance; //singleton implementation

    private static final String TAG = User.class.getName();

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private String picUrl;

    public static void initInstance(){
        if (instance == null){
            instance = new User();
        }
    }

    public static User getInstance(){
        return instance;
    }

    private User(){
        id = "";
        firstName = "";
        lastName = "";
        email = "";
        gender = "";
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setUserFromJSON(JSONObject user, JSONObject friends){
        try {
            id = user.getString("id");

            try {
                picUrl = new URL("https://graph.facebook.com/" + id + "/picture?width=150&height=150").toString();
            } catch (MalformedURLException e) {
                Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }

            if (user.has("first_name"))
                firstName = user.getString("first_name");
            if (user.has("last_name"))
                lastName = user.getString("last_name");
            if (user.has("email"))
                email = user.getString("email");
            if (user.has("gender"))
                gender = user.getString("gender");


            //initialize async update to API backend
            new SendUserUpdate().execute(user, friends);

        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
    }


}
