package com.example.pau.locationtracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
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

public class SelectGroupMap extends AppCompatActivity {

    private RecyclerView allGroupsList;

    private DatabaseReference allGroupsReference;
    private FirebaseAuth mAuth;
    FirebaseRecyclerAdapter<Groups, SelectGroupAllGroupsViewHolder> firebaseRecyclerAdapterGroups;
    ArrayList<String> mUsers;
    ArrayList<String> friendsGroup;
    DatabaseReference groupMembersReference;
    String id;
    Boolean isChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group_map);
        getSupportActionBar().setTitle("See friends on map");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        allGroupsList = (RecyclerView) findViewById(R.id.all_groups_list);

        allGroupsReference = FirebaseDatabase.getInstance().getReference().child("groups");
        allGroupsReference.keepSynced(true);

        allGroupsList = findViewById(R.id.all_groups_list);
        allGroupsList.setLayoutManager(new LinearLayoutManager(this));
        mAuth = FirebaseAuth.getInstance();
        id = mAuth.getUid();
        friendsGroup  = new ArrayList<>();

        groupMembersReference = FirebaseDatabase.getInstance().getReference().child("groups");
        groupMembersReference.keepSynced(true);
        mUsers = new ArrayList<>();
    }

    protected void onStart() {
        super.onStart();


        firebaseRecyclerAdapterGroups = new FirebaseRecyclerAdapter<Groups, SelectGroupAllGroupsViewHolder>(
                Groups.class,
                R.layout.all_groups_display_for_map_layout,
                SelectGroupAllGroupsViewHolder.class,
                allGroupsReference
        ) {

            @Override
            protected void populateViewHolder(final SelectGroupAllGroupsViewHolder viewHolder, final Groups model, final int position) {
                final View view = viewHolder.mView;
                viewHolder.setGroupname(model.getGroupname());
                final CheckBox chekcb = view.findViewById(R.id.checkBox);

                if (model.getImage() != null)
                    viewHolder.setImage(model.getImage(), getApplicationContext());


                groupMembersReference.child(model.getKey()).child("usersId").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(id).exists()){
                            groupMembersReference.child(model.getKey()).child("usersId").child(id).child("visibility").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(final DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        isChecked = (Boolean) dataSnapshot.getValue();
                                        System.out.println("VALUE:: "+ isChecked);
                                        if(!isChecked){
                                            chekcb.setChecked(false);
                                        }else{
                                            chekcb.setChecked(true);
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            });
                        }else{
                            viewHolder.setVis();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                chekcb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!isChecked){
                            groupMembersReference.child(model.getKey()).child("usersId").child(id).child("visibility").setValue(true);
                        }else{
                            groupMembersReference.child(model.getKey()).child("usersId").child(id).child("visibility").setValue(false);
                        }
                    }
                });

                viewHolder.setOnClickListener(new SelectGroupAllGroupsViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(SelectGroupMap.this, MapsActivity.class);
                        System.out.println("mUSERSSS:: "+model.getKey());
                        intent.putExtra("GROUPKEY", model.getKey());
                        startActivity(intent);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        //Toast.makeText(GroupsActivity.this, "Item long clicked at " + position + " "+model.groupname , Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        allGroupsList.setAdapter(firebaseRecyclerAdapterGroups);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SelectGroupMap.this, MenuActivity.class);
        startActivity(intent);
    }


    public static class SelectGroupAllGroupsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public TextView groupN;
        private SelectGroupMap.SelectGroupAllGroupsViewHolder.ClickListener mClickListener;

        public SelectGroupAllGroupsViewHolder(View itemView) {
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
            DatabaseReference allGroupsReferences = FirebaseDatabase.getInstance().getReference().child("groups").child(k).child("visibility");
            allGroupsReferences.setValue(u);
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

        public void setOnClickListener(SelectGroupMap.SelectGroupAllGroupsViewHolder.ClickListener clickListener){
            mClickListener = clickListener;
        }
    }
}

