package ca.dmdev.test.wom;

import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

public class AddReviewActivity extends AppCompatActivity {

    private static final String TAG = WordOfMouth.class.getName();

    Toolbar toolbar;
    DialogInterface.OnClickListener dialogBackConfirm;
    WordOfMouth wom;

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
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
}
