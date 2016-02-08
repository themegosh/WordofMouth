package ca.dmdev.petritrebs.wom;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AddLocation extends AppCompatActivity {

    private static final int IPC_ID = 1123;
    private ListView lv;
    private ListView lvfriend;
    private Button mAddButton;
    private EditText mlocation;
    private TextView mFriendMsg;
    private RelativeLayout rl;
    private RelativeLayout rl2;

    private RadioButton mPrivate;
    private RadioButton mPublic;
    private RadioButton mFriends;




    //private EditText mTagField;
    static ArrayAdapter<String> arrayAdapterFriends;
    public static ArrayList<String> tagListStrings;
    public static ArrayList<String> friendListStrings;
    private AutoCompleteTextView mTagField;
    RelativeLayout.LayoutParams params;
    RelativeLayout.LayoutParams params2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        lv = (ListView) findViewById(R.id.listView);
        lvfriend = (ListView) findViewById(R.id.listView1);
        mAddButton = (Button) findViewById(R.id.button);
        mlocation = (EditText) findViewById(R.id.editText);
        mTagField = (AutoCompleteTextView) findViewById(R.id.editText2);
        mPrivate = (RadioButton) findViewById(R.id.radioButton);
        mPublic = (RadioButton) findViewById(R.id.radioButton2);
        mFriends = (RadioButton) findViewById(R.id.radioButton3);
        mFriendMsg = (TextView) findViewById(R.id.textView5);
        rl = (RelativeLayout) findViewById(R.id.relativeLayout3);
        rl2 = (RelativeLayout) findViewById(R.id.relativeLayout);

        //array to fill lists
        tagListStrings = new ArrayList<>();
        friendListStrings= new ArrayList<>();

        // Create The Adapter with passing ArrayList as 3rd parameter
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.mylist, tagListStrings);
        arrayAdapterFriends = new ArrayAdapter<String>(this,R.layout.mylist, friendListStrings);
        // Set The Adapter
        lv.setAdapter(arrayAdapter);
        lvfriend.setAdapter(arrayAdapterFriends);


        String[] tags = {"tasty", "cocktails", "fun", "cheap", "relaxed", "expensive", "nice", "free", "awesome", "ridiculous", "rad", "sick",
                "sweet", "bomb", "outragous", "amazing", "cool", "dancing", "pool", "basketball"};


        /*
        friendListStrings.add("John Smith");
        friendListStrings.add("James Doe");
        friendListStrings.add("Mike Molt");
        friendListStrings.add("Sam Star");
*/
        params = (RelativeLayout.LayoutParams) rl.getLayoutParams();
        params2 = (RelativeLayout.LayoutParams) rl2.getLayoutParams();
        // Changes the height and width to the specified *pixels*
        params.height = params.height +90;
        mFriendMsg.setText("No friends will see this marker");


        arrayAdapterFriends.notifyDataSetChanged();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,tags);
        mTagField.setAdapter(adapter);
        mTagField.setThreshold(1);


        mlocation.setText("Bobs Burgers, 123 Example St. Toronto");
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTagField.getText().toString().equals("")) {

                } else {
                    String a = "" + mTagField.getText().toString().charAt(0);
                    if (a.equals("#")) {
                        if(!tagListStrings.contains(mTagField.getText().toString())){
                            tagListStrings.add(mTagField.getText().toString());
                        }
                    } else {
                        if(!tagListStrings.contains("#" + mTagField.getText().toString())){
                            tagListStrings.add("#" + mTagField.getText().toString());
                        }
                    }
                    //Update the list
                    arrayAdapter.notifyDataSetChanged();
                    arrayAdapter.notifyDataSetChanged();
                    mTagField.setText("");
                    if(tagListStrings.size()<3){
                        params.height = params.height +90;
                    }

                }
            }
        });
        mPrivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendListStrings.clear();
                mFriendMsg.setText("No friends will see this marker");
                arrayAdapterFriends.notifyDataSetChanged();
            }
        });
        mPublic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        friendListStrings.clear();
                        mFriendMsg.setText("All of your friends will see this marker");
                        arrayAdapterFriends.notifyDataSetChanged();
            }
        });
        mFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                friendListStrings.add("John Smith");
                friendListStrings.add("James Doe");
                friendListStrings.add("Mike Molt");
                friendListStrings.add("Sam Star");
*/
                try {
                    Intent myIntentA1A2 = new Intent(AddLocation.this,
                            MyFriends.class);

                    startActivityForResult(myIntentA1A2,IPC_ID);
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                mFriendMsg.setText("");
                arrayAdapterFriends.notifyDataSetChanged();
            }
        });

        // register onClickListener to handle click events on each item
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {

                int pos = position;
                tagListStrings.remove(position);
                arrayAdapter.notifyDataSetChanged();
                if(tagListStrings.size()>3){
                    //params.height = params.height -90;
                    //params2.height = params2.height -90;
                }
            }
        });
        lvfriend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {

                int pos = position;
                friendListStrings.remove(position);
                arrayAdapterFriends.notifyDataSetChanged();
                if(friendListStrings.size()==0){
                    mFriendMsg.setText("No friends have been selected");
                }
                if(friendListStrings.size()>0){
                    mFriendMsg.setText("");
                }
            }
        });





    }

    public void onResume() {
        super.onResume();

        arrayAdapterFriends.notifyDataSetChanged();

    }

}
