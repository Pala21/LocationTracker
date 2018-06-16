package com.example.pau.locationtracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

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
    String k;
    String id;
    String groupId;
    private final static int Gallery_pick = 1;
    private StorageReference storeProfileImagestorageRef;


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
        storeProfileImagestorageRef = FirebaseStorage.getInstance().getReference().child("Group_Profile_Image");
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
                                    AlertDialog.Builder builder;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        builder = new AlertDialog.Builder(GroupsActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                                    } else {
                                        builder = new AlertDialog.Builder(GroupsActivity.this);
                                    }
                                    builder.setTitle("Delete Group")
                                            .setMessage("Are you sure you want to delete "+model.getGroupname()+ "?")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    viewHolder.deleteGroup(model.getKey());
                                                }
                                            })
                                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    return;
                                                }
                                            })
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show();
                                }

                            });

                            CircleImageView image = viewHolder.getImage();

                            image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    groupId = model.getKey();
                                    System.out.println("ENTRA:: "+groupId);
                                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    intent.setType("image/*");
                                    startActivityForResult(intent, Gallery_pick);
                                }
                            });

                        }else{
                            viewHolder.setVis();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
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
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if(requestCode == Gallery_pick && resultCode == RESULT_OK && data != null )
        {
            Uri ImageUri = Uri.parse(data.getData().toString());
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                StorageReference filePath = storeProfileImagestorageRef.child(groupId+".jpg");
                System.out.println("ENTRA2:: "+groupId);
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(GroupsActivity.this, "Saving your new group profile Image...", Toast.LENGTH_SHORT).show();

                            String downloadUrl = task.getResult().getDownloadUrl().toString();
                            allGroupsReference.child(groupId).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(GroupsActivity.this, "Image Updated Successfully...", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(GroupsActivity.this, "Error ocurred, while uploading your profile Image...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
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

        public void deleteGroup(String k){
            DatabaseReference allGroupsReferences = FirebaseDatabase.getInstance().getReference().child("groups").child(k);
            allGroupsReferences.removeValue();
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

        public CircleImageView getImage() {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.image_group_profile);
            return image;
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
