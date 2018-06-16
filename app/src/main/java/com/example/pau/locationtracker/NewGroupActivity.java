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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class NewGroupActivity extends AppCompatActivity {


    private static String CURRENT_STATE;
    private RecyclerView allUsersList;
    private DatabaseReference allFriendsReference;
    private FirebaseAuth mAuth;
    FloatingActionButton fab;
    FirebaseRecyclerAdapter<Friends, AllUsersViewHolder> firebaseRecyclerAdapterGroups;
    ArrayList<String> mUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        getSupportActionBar().setTitle("Add Friends in Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        String userAdminId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        allFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(userAdminId);
        allFriendsReference.keepSynced(true);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        allUsersList = findViewById(R.id.all_users_group_list);
        allUsersList.setLayoutManager(new LinearLayoutManager(this));
        mAuth = FirebaseAuth.getInstance();
        mUsers = new ArrayList<>();
    }

    protected void onStart()
    {
        super.onStart();
        firebaseRecyclerAdapterGroups = new FirebaseRecyclerAdapter<Friends, AllUsersViewHolder>
                (
                        Friends.class,
                        R.layout.all_users_in_group_display_layout,
                        AllUsersViewHolder.class,
                        allFriendsReference
                )
        {

            @Override
            protected void populateViewHolder(final AllUsersViewHolder viewHolder, final Friends model, final int position) {

                final View view = viewHolder.mView;
                final CheckBox chekcb = view.findViewById(R.id.checkBox);
                viewHolder.setUsername(model.getUsername());
                viewHolder.setFullname(model.getFullname());
                if (model.getUserImage() != null)
                    viewHolder.setUserImage(model.getUserImage(), getApplicationContext());

                System.out.println("Model:: "+ model.getUsername() +" "+model.getFullname()+" "+model.getKey());
                chekcb.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (chekcb.isChecked()) {
                            mUsers.add(model.getKey());
                            System.out.println("mUseeers:: "+mUsers);
                        } else {
                            mUsers.remove(model.getKey());
                        }
                    }
                });
            }
        };
        allUsersList.setAdapter(firebaseRecyclerAdapterGroups);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUsers.size() > 0){
                    mUsers.add(mAuth.getUid());
                    System.out.println("MUSERS SIZEEE:: "+mUsers.size()+ " "+mUsers);
                    Intent intent = new Intent(NewGroupActivity.this, LastConfigurationCreateGroup.class);
                    intent.putStringArrayListExtra("USERS", mUsers);
                    startActivity(intent);
                }else{
                    Toast.makeText(NewGroupActivity.this,"You must select at least one friend to continue ...",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    public static class AllUsersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public TextView userN;
        public TextView fullN;

        public AllUsersViewHolder(View itemView) {
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
