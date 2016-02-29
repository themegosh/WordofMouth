package ca.dmdev.petritrebs.wom;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class AddReviewActivity extends AppCompatActivity {

    Toolbar toolbar;
    DialogInterface.OnClickListener dialogBackConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);

        toolbar = (Toolbar) findViewById(R.id.add_review_toolbar);
        toolbar.setTitle("Review: asd");



        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
