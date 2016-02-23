package ca.dmdev.petritrebs.wom.acccount;

import android.util.Log;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

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
    private URL picUrl;

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

    private User(String id, String firstName, String lastName, String email, String gender){
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.gender = gender;
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

    public URL getPicUrl() {
        return picUrl;
    }

    public void setUserFromJSON(JSONObject user){
        try {
            id = user.getString("id");

            try {
                picUrl = new URL("https://graph.facebook.com/" + id + "/picture?width=200&height=150");
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

        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
    }
}
