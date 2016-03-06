package ca.dmdev.test.wom;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.like.LikeButton;
import com.like.OnLikeListener;

import ca.dmdev.test.wom.acccount.User;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddReviewActivity extends AppCompatActivity {

    private static final String TAG = WordOfMouth.class.getName();

    Toolbar toolbar;
    DialogInterface.OnClickListener dialogBackConfirm;
    WordOfMouth wom;

    EditText txtTitle;
    EditText txtDescription;
    LikeButton btnLike;
    boolean isLiked;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);
        wom = (WordOfMouth)getApplication();

        toolbar = (Toolbar) findViewById(R.id.add_review_toolbar);
        toolbar.setTitle("Review: "+wom.getSelectedPlace().getName());

        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        txtTitle = (EditText) findViewById(R.id.txtAddReviewTitle);
        txtDescription = (EditText) findViewById(R.id.txtAddReviewDescription);
        btnSubmit = (Button) findViewById(R.id.btnAddReviewSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidInputData()) {
                    new SendReview().execute(
                            wom.getSelectedPlace().getId(),
                            User.getInstance().getId(),
                            txtTitle.getText().toString().trim(),
                            txtDescription.getText().toString().trim(),
                            String.valueOf(isLiked)
                    );
                }
            }
        });
        btnLike = (LikeButton) findViewById(R.id.btnAddReviewLike);
        btnLike.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                isLiked = true;
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                isLiked = false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();

        Log.d(TAG, "AddReviewActivity onOptionsItemSelected id: " + String.valueOf(id));

        if (id == android.R.id.home) {
            dialogBackConfirm = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            finish();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //do nothing
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(AddReviewActivity.this);
            builder.setMessage("Are you sure you want to discard this review?")
                    .setPositiveButton("Yes", dialogBackConfirm)
                    .setNegativeButton("No", dialogBackConfirm)
                    .show();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private boolean isValidInputData(){
        boolean isValid = true;

        if (txtTitle.getText().toString().trim().isEmpty()) {
            txtTitle.setError("You must enter a title!");
            isValid = false;
        }

        if (txtDescription.getText().toString().trim().isEmpty()){
            txtDescription.setError("You must enter a description!");
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void onBackPressed() {
        dialogBackConfirm = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //do nothing
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to discard this review?")
                .setPositiveButton("Yes", dialogBackConfirm)
                .setNegativeButton("No", dialogBackConfirm)
                .show();
    }


    public class SendReview extends AsyncTask<String, Void, String> {
        private static final String TAG = "UpdateExternalDb";
        public final String SERVER_URL = "http://wom.dmdev.ca/process.php";
        //public final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        OkHttpClient client = new OkHttpClient();
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = new ProgressDialog(AddReviewActivity.this); // this = YourActivity
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Saving review...");
            dialog.setIndeterminate(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                if (params.length == 5) {
                    Log.d(TAG, "==== BEGIN UPLOADING TO WEB SERVER ====");
                    //Log.d(TAG, "json data to send: " + params);

                    //send the user info to the server
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("action", "add_review")
                            .addFormDataPart("placeId", params[0])
                            .addFormDataPart("ownerId", params[1])
                            .addFormDataPart("title", params[2])
                            .addFormDataPart("description", params[3])
                            .addFormDataPart("liked", params[4])
                            .build();

                    Request request = new Request.Builder()
                            .url(SERVER_URL)
                            .post(requestBody)
                            .build();

                    Response response = client.newCall(request).execute();
                    Log.d(TAG, "Response: " + response.body().string());

                    response.body().close();

                    return response.body().string();
                }
            } catch (Exception ex) {
                Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
                ex.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            Log.d(TAG, "sending review completed. Result: " + result);
            dialog.cancel();
            Toast.makeText(AddReviewActivity.this, "Review saved!", Toast.LENGTH_LONG).show();
            finish();
            //showDialog("Downloaded " + result + " bytes");
        }
    }
}
