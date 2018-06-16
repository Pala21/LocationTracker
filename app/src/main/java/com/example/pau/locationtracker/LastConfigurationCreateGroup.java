package com.example.pau.locationtracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LastConfigurationCreateGroup extends AppCompatActivity {
    private final static int Gallery_pick = 1;
    ArrayList<String> mUsers;
    private DatabaseReference allUsersReference;
    private StorageReference storeProfileImagestorageRef;
    private DatabaseReference GroupsReference;
    private DatabaseReference GroupReferenceReceiver;
    private FloatingActionButton fab;
    private EditText ed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_configuration_create_group);
        getSupportActionBar().setTitle("Group Configuration");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        fab = findViewById(R.id.fab);
        ed = findViewById(R.id.groupName);

        allUsersReference = FirebaseDatabase.getInstance().getReference().child("users");
        storeProfileImagestorageRef = FirebaseStorage.getInstance().getReference().child("Group_Profile_Image");
        Intent i = getIntent();
        mUsers = i.getStringArrayListExtra("USERS");

        GroupsReference = FirebaseDatabase.getInstance().getReference().child("groups");

        final List<Users> u = new ArrayList<Users>();
        final String key = GroupsReference.push().getKey();

        System.out.println("MUSEEERS:: "+mUsers);
        allUsersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (String user : mUsers){

                    for(DataSnapshot d2 : dataSnapshot.getChildren()){
                        //System.out.println("D1:: "+d2.getKey());
                        //System.out.println("D1User:: "+user);

                        if(d2.getKey().equals(user)){
                            System.out.println("D1D2:: "+d2.getChildren());
                            for(DataSnapshot d3 : d2.getChildren()){
                                if(d3.getKey().equals("image")){
                                    GroupsReference.child(key).child("usersId").child(user).child("image").setValue(d3.getValue());
                                }
                                if(d3.getKey().equals("fullname")){
                                    GroupsReference.child(key).child("usersId").child(user).child("fullname").setValue(d3.getValue());
                                }
                                if(d3.getKey().equals("username")){
                                    GroupsReference.child(key).child("usersId").child(user).child("username").setValue(d3.getValue());
                                }
                                boolean visibility = false;
                                GroupsReference.child(key).child("usersId").child(user).child("visibility").setValue(visibility);

                                GroupsReference.child(key).child("usersId").child(user).child("key").setValue(user);
                            }

                        }

                    }
                }

                String userAdminId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                GroupsReference.child(key).child("key").setValue(key);
                GroupsReference.child(key).child("adminUser").setValue(userAdminId);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        GroupReferenceReceiver  = FirebaseDatabase.getInstance().getReference().child("groups").child(key);
        final CircleImageView image = (CircleImageView) findViewById(R.id.image_group_profile);
        Picasso.with(getApplicationContext()).load(R.drawable.ic_shortcut_people_outline).into(image);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, Gallery_pick);
            }
        });

        GroupReferenceReceiver  = FirebaseDatabase.getInstance().getReference().child("groups").child(key).child("image");

        GroupReferenceReceiver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Picasso.with(getApplicationContext()).load(String.valueOf(dataSnapshot.getValue())).into(image);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ed.getText().length()>0){
                    String groupName = String.valueOf(ed.getText());
                    GroupReferenceReceiver.child("groupname").setValue(groupName);
                    Intent i = new Intent(LastConfigurationCreateGroup.this, GroupsActivity.class);
                    startActivity(i);
                    finish();
                }else{
                    Toast.makeText(LastConfigurationCreateGroup.this,"The group can not be created if a name does not exist.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        GroupReferenceReceiver  = FirebaseDatabase.getInstance().getReference().child("groups").child(key);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GroupReferenceReceiver.removeValue();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Gallery_pick && resultCode == RESULT_OK && data != null )
        {
            Uri ImageUri = data.getData();
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                StorageReference filePath = storeProfileImagestorageRef.child(GroupReferenceReceiver.getKey()+".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(LastConfigurationCreateGroup.this, "Saving group profile Image...", Toast.LENGTH_SHORT).show();

                            String downloadUrl = task.getResult().getDownloadUrl().toString();
                            GroupReferenceReceiver.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(LastConfigurationCreateGroup.this, "Image Updated Successfully...", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(LastConfigurationCreateGroup.this, "Error ocurred, while uploading group profile Image...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
