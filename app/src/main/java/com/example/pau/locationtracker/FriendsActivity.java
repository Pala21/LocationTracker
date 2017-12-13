package com.example.pau.locationtracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {
    //ArrayAdapter<String> adapterFriends;
    //ArrayAdapter<String> adapterUsers;
    MyCustomAdapter adapterFriends;
    MyCustomAdapter adapterUsers;
    private DatabaseReference mDatabase;
    final ArrayList<String> friends = new ArrayList<>();
    final ArrayList<String> people = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ListView lv = (ListView) findViewById(R.id.listFriendsActivity);

        mDatabase.child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot a : dataSnapshot.getChildren()) {
                    String val = (String) a.child("email").child("email").getValue();
                    friends.add(val);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot a : dataSnapshot.getChildren()){
                    String val = (String)a.child("email").child("email").getValue();
                    people.add(val);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        //adapterUsers = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, people);
        //adapterFriends = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, friends);
        adapterUsers = new MyCustomAdapter(people, this);
        adapterFriends = new MyCustomAdapter(friends, this);

        //lv.setAdapter(adapterFriends);
        lv.setAdapter(adapterFriends);
        String TAG = FriendsActivity.class.getSimpleName();
        Log.d(TAG, "Entraaaaa");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        final ListView lv = (ListView) findViewById(R.id.listFriendsActivity);
        final MenuItem item = menu.findItem(R.id.menuSearch);
        final SearchView searchView = (SearchView) item.getActionView();
        String TAG = FriendsActivity.class.getSimpleName();
        Log.d(TAG, "Entraaaaa3");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                lv.setAdapter(adapterUsers);
                //adapterUsers.getFilter().filter(newText);
                adapterUsers.getFilter().filter(newText);
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener(){
            @Override
            public boolean onClose() {
                //lv.setAdapter(adapterFriends);
                lv.setAdapter(adapterFriends);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}
