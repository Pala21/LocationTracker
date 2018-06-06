package com.example.pau.locationtracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddDeleteFriendsInGroup2 extends AppCompatActivity {

    private RecyclerView allFriendsInGroupList;

    private FirebaseAuth mAuth;
    FloatingActionButton fab;
    FirebaseRecyclerAdapter<Friends, AllFriendsInGroupViewHolder> firebaseRecyclerAdapterGroups;
    ArrayList<String> mUsers;
    ArrayList<String> friendsGroup;
    private DatabaseReference allFriendsReference;
    private DatabaseReference groupReference;
    String groupId;
    DatabaseReference groupMembersReference;
    String userAdminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_delete_friends_in_group2);

        getSupportActionBar().setTitle("Add Friends to the Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        allFriendsInGroupList = findViewById(R.id.all_friends_in_group);
        allFriendsInGroupList.setLayoutManager(new LinearLayoutManager(this));
        mAuth = FirebaseAuth.getInstance();
        userAdminId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        allFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(userAdminId);
        allFriendsReference.keepSynced(true);
        groupId = (String) getIntent().getExtras().get("GroupId");
        groupReference = FirebaseDatabase.getInstance().getReference().child("groups").child(groupId);
        groupReference.keepSynced(true);
        System.out.println("GROUP ID:: "+groupId);

        mUsers = new ArrayList<>();
        groupMembersReference = FirebaseDatabase.getInstance().getReference().child("groups").child(groupId).child("usersId");
        groupMembersReference.keepSynced(true);

    }

    protected void onStart()
    {
        super.onStart();

        friendsGroup = new ArrayList<>();

        groupMembersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot d2 : dataSnapshot.getChildren()){
                    friendsGroup.add(d2.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}


        });

        mUsers = new ArrayList<>();
        firebaseRecyclerAdapterGroups = new FirebaseRecyclerAdapter<Friends,AllFriendsInGroupViewHolder>
                (
                        Friends.class,
                        R.layout.all_users_in_group_display_layout,
                        AllFriendsInGroupViewHolder.class,
                        allFriendsReference
                )
        {

            @Override
            protected void populateViewHolder(final AllFriendsInGroupViewHolder viewHolder, final Friends model, final int position) {
                final View view = viewHolder.mView;
                final CheckBox chekcb = view.findViewById(R.id.checkBox);
                System.out.println("FRIENDS GROUP:: "+friendsGroup);
                if (!friendsGroup.contains(model.getKey())) {
                    chekcb.setChecked(false);
                    viewHolder.setUsername(model.getUsername());
                    viewHolder.setFullname(model.getFullname());
                    if (model.getUserImage() != null)
                        viewHolder.setUserImage(model.getUserImage(), getApplicationContext());


                    chekcb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            groupReference.child("adminUser").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(chekcb.isChecked()){
                                        groupReference.child("usersId").child(model.getKey()).child("image").setValue(model.getUserImage());
                                        groupReference.child("usersId").child(model.getKey()).child("fullname").setValue(model.getFullname());
                                        groupReference.child("usersId").child(model.getKey()).child("username").setValue(model.getUsername());
                                        groupReference.child("usersId").child(model.getKey()).child("key").setValue(model.getKey());
                                        groupReference.child("usersId").child(model.getKey()).child("visibility").setValue(false);
                                        mUsers.add(model.getKey());
                                    }else{
                                        groupReference.child("usersId").child(model.getKey()).removeValue();
                                        mUsers.remove(model.getKey());
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            });
                        }
                    });
                }else{
                    viewHolder.setVis();
                }
            }
        };
        allFriendsInGroupList.setAdapter(firebaseRecyclerAdapterGroups);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUsers.size() > 0){
                    Intent intent = new Intent(AddDeleteFriendsInGroup2.this, GroupsActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(AddDeleteFriendsInGroup2.this,"You must select at least one friend to continue ...",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        for(String m : mUsers){
            groupReference = FirebaseDatabase.getInstance().getReference().child("groups").child(groupId);
            groupReference =  groupReference.child("usersId").child(m);
            groupReference.removeValue();
        }
    }

    public static class AllFriendsInGroupViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public TextView userN;
        public TextView fullN;

        public AllFriendsInGroupViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            userN = (TextView) mView.findViewById(R.id.all_users_username);
            fullN = (TextView) mView.findViewById(R.id.all_users_fullname);
        }

        public void setUsername(String u)
        {
            TextView username = (TextView) mView.findViewById(R.id.all_users_username);
            username.setText(u);
        }

        public void setFullname(String f)
        {
            TextView fullname = (TextView) mView.findViewById(R.id.all_users_fullname);
            fullname.setText(f);
        }

        public void setUserImage(String userImage, Context ctx)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(userImage).into(image);
        }

        public void setVis()
        {
            mView.findViewById(R.id.linearDisplayUsers).setVisibility(View.GONE);
        }
    }

}
