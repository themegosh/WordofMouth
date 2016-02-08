package ca.dmdev.petritrebs.wom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class MyFriends extends AppCompatActivity {

    private ListView lv;
    private ListView lvadded;
    //private Button mFinishButton;
    ArrayAdapter<String> adapter;
    EditText inputSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);

        // Listview Data
        final String friends[] = {"John", "James", "Jane", "Jackie", "Mike","Bob", "Bil",
                "Bailey", "Fred", "Sam", "Steph", "Ari", "Nicky", "Morty", "Rick", "Rachel"};

        lv = (ListView) findViewById(R.id.listViewfriends);
        lvadded = (ListView) findViewById(R.id.listViewadded);
        inputSearch = (EditText) findViewById(R.id.search);
        //mFinishButton = (Button) findViewById(R.id.buttonfinish);

        // Adding items to listview
        adapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.friend_name, friends);
        lv.setAdapter(adapter);

        final ArrayAdapter<String> arrayAdapterAdded = new ArrayAdapter<String>(this,R.layout.mylist, AddLocation.friendListStrings);
        lvadded.setAdapter(arrayAdapterAdded);

        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                MyFriends.this.adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {


                if (!AddLocation.friendListStrings.contains(friends[position])) {
                    AddLocation.friendListStrings.add(friends[position]);
                    arrayAdapterAdded.notifyDataSetChanged();
                } else {
                    //the friend has already been added
                }


            }
        });
        lvadded.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {


                AddLocation.friendListStrings.remove(position);
                arrayAdapterAdded.notifyDataSetChanged();


            }
        });
        /*
        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        */

    }
}
