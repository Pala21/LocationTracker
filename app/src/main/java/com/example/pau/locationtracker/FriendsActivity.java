package com.example.pau.locationtracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {

    MyCustomAdapter adapterFriends;
    MyCustomAdapter adapterUsers;
    private DatabaseReference mDatabase;
    ArrayList<String> friends = new ArrayList<>();
    final ArrayList<String> people = new ArrayList<>();
    ArrayList<Friend> aFriends = new ArrayList<>();
    ListView lv;
    boolean trobat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("ON_CREATE");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        lv = (ListView) findViewById(R.id.listFriendsActivity);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        adapterFriends = new MyCustomAdapter(friends, this, aFriends);
        adapterUsers = new MyCustomAdapter(people, this, aFriends);
        lv.setAdapter(adapterFriends);
        friendsMethod(lv);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        System.out.println("ON_MENUUUUUU "+ menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        final MenuItem item = menu.findItem(R.id.menuSearch);
        final SearchView searchView = (SearchView) item.getActionView();


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                lv.setAdapter(adapterUsers);
                adapterUsers.getFilter().filter(newText);
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener(){
            @Override
            public boolean onClose() {
                lv.setAdapter(adapterFriends);
                return false;
            }
        });

        //lv.setAdapter(adapterFriends);
        //return super.onPrepareOptionsMenu(menu);
        return true;
    }




    public void friendsMethod(final ListView lv){
        System.out.println("ENTRA FRIENDS METHOD ");

        mDatabase.child("Friendships").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByChild("friendName").
            addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println("ENTRA 1 A");
                    for (DataSnapshot a : dataSnapshot.getChildren()) {
                        String name = (String) a.child("friendName").getValue();
                        if (!friends.contains(name)) {
                            friends.add(name);
                            System.out.println("A VEURE ELS NOMS :: "+name);
                            System.out.println("A VEURE LA LLISTA :: "+friends);
                        }
                    }
                    for (Integer i = 0; i < friends.size(); i++) {
                        for(Friend a : aFriends){
                            if(a.friendName.equals(friends.get(i))){
                                trobat = true;

                            }
                        }
                        if(!trobat)
                            aFriends.add(new Friend(friends.get(i), false));
                    }
                    System.out.println("A VEURE ELS AFRIENDS :: "+aFriends);
                    ((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("ENTRA 2 ");
                for (DataSnapshot a : dataSnapshot.getChildren()) {
                    String val = (String) a.child("username").getValue();
                    people.add(val);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });


    }
}
