package ca.dmdev.test.wom.api;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by mathe_000 on 2016-02-23.
 */
public class SendUserUpdate extends AsyncTask<JSONObject, Void, String> {
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
                    //Log.d(TAG, "==== BEGIN UPLOADING TO WEB SERVER ====");
                    //Log.d(TAG, "json data to send: " + params[0].toString());

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
                    //Log.d(TAG, "Response: " + response.body().string());

                    response.body().close();

                }
            }
        } catch(Exception ex) {
            Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
            ex.printStackTrace();
        }
        return null;
    }
}