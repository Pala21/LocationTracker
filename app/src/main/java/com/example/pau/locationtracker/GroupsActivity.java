package com.example.pau.locationtracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

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

public class GroupsActivity extends AppCompatActivity {

    private RecyclerView allGroupsList;

    private static String CURRENT_STATE;
    private DatabaseReference allGroupsReference;
    private FirebaseAuth mAuth;
    FloatingActionButton fab;
    FirebaseRecyclerAdapter<Groups, AllGroupsViewHolder> firebaseRecyclerAdapterGroups;
    ArrayList<String> mUsers;
    ArrayList<String> friendsGroup;
    String id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        getSupportActionBar().setTitle("Groups");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        allGroupsList = (RecyclerView) findViewById(R.id.all_groups_list);

        allGroupsReference = FirebaseDatabase.getInstance().getReference().child("groups");
        allGroupsReference.keepSynced(true);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        allGroupsList = findViewById(R.id.all_groups_list);
        allGroupsList.setLayoutManager(new LinearLayoutManager(this));
        mAuth = FirebaseAuth.getInstance();
        id = mAuth.getUid();
        friendsGroup  = new ArrayList<>();

    }

    protected void onStart() {
        super.onStart();

        mUsers = new ArrayList<>();
        firebaseRecyclerAdapterGroups = new FirebaseRecyclerAdapter<Groups, AllGroupsViewHolder>
                (
                        Groups.class,
                        R.layout.all_groups_display_layout,
                        AllGroupsViewHolder.class,
                        allGroupsReference
                )
        {

            @Override
            protected void populateViewHolder(final AllGroupsViewHolder viewHolder, final Groups model, final int position) {

                allGroupsReference.child(model.getKey()).child("usersId").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(id).exists()){
                            System.out.println("ENTRA ");
                            viewHolder.setGroupname(model.getGroupname());
                            if (model.getImage() != null)
                                viewHolder.setImage(model.getImage(), getApplicationContext());

                            viewHolder.setOnClickListener(new AllGroupsViewHolder.ClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    Intent i = new Intent(GroupsActivity.this, AddDeleteFriendsInGroup.class);
                                    i.putExtra("GroupId", model.getKey());
                                    startActivity(i);
                                }

                                @Override
                                public void onItemLongClick(View view, int position) {
                                    //Toast.makeText(GroupsActivity.this, "Item long clicked at " + position + " "+model.groupname , Toast.LENGTH_SHORT).show();
                                }

                            });
                        }else{
                            viewHolder.setVis();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        };

        allGroupsList.setAdapter(firebaseRecyclerAdapterGroups);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GroupsActivity.this, NewGroupActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(GroupsActivity.this, MenuActivity.class);
        startActivity(intent);
    }


    public static class AllGroupsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public TextView groupN;
        private AllGroupsViewHolder.ClickListener mClickListener;

        public AllGroupsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            groupN = (TextView) mView.findViewById(R.id.all_groups_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onItemClick(v, getAdapterPosition());

                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mClickListener.onItemLongClick(v, getAdapterPosition());
                    return true;
                }
            });
        }

        public void setGroupname(String u)
        {
            TextView groupName = (TextView) mView.findViewById(R.id.all_groups_name);
            groupName.setText(u);
        }

        public void setVisibility(final Boolean u, String k) {
            DatabaseReference allGroupsReference = FirebaseDatabase.getInstance().getReference().child("groups").child(k).child("visibility");
            allGroupsReference.setValue(u);
        }

        public void setImage(String groupImage, Context ctx)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.image_group_profile);
            Picasso.with(ctx).load(groupImage).into(image);
        }

        public void setVis() {
            mView.findViewById(R.id.linearDisplayUsers).setVisibility(View.GONE);
        }

        public interface ClickListener{
            public void onItemClick(View view, int position);
            public void onItemLongClick(View view, int position);
        }

        public void setOnClickListener(AllGroupsViewHolder.ClickListener clickListener){
            mClickListener = clickListener;
        }
    }
}
