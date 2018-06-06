package com.example.pau.locationtracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private DatabaseReference allUsersReference;
    private DatabaseReference friendsReference;
    private DatabaseReference requestsReference;
    private DatabaseReference UsersReferenceReceiver;
    private final static int Gallery_pick = 1;
    private StorageReference storeProfileImagestorageRef;

    private long numberOfFriends;
    private long numberOfRequests;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
       //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
       //set content view AFTER ABOVE sequence (to avoid crash)

        storeProfileImagestorageRef = FirebaseStorage.getInstance().getReference().child("Profile_Images");

        this.setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final String userid = FirebaseAuth.getInstance().getUid();
        final CircleImageView image = (CircleImageView) findViewById(R.id.image_profile);
        final TextView u = (TextView) findViewById(R.id.username);
        final TextView f = (TextView) findViewById(R.id.fullname);
        final TextView friendsNumber = (TextView) findViewById(R.id.textview2);
        final TextView requestsNumber = (TextView) findViewById(R.id.textview1);
        numberOfRequests = 0;
        numberOfFriends = 0;
        allUsersReference = FirebaseDatabase.getInstance().getReference().child("users");
        allUsersReference.keepSynced(true);
        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(userid);
        requestsReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(userid);
        UsersReferenceReceiver  = FirebaseDatabase.getInstance().getReference().child("users").child(userid);

        requestsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot d1 : dataSnapshot.getChildren())
                {
                    if(d1.exists()){
                        for(DataSnapshot d : d1.getChildren()){
                            if(d.exists()){
                                if(d.getValue().equals("receiver")){
                                    numberOfRequests += 1;
                                }
                            }
                        }
                    }
                }
                requestsNumber.setText(String.valueOf(numberOfRequests));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });


        friendsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                numberOfFriends = dataSnapshot.getChildrenCount();
                friendsNumber.setText(String.valueOf(numberOfFriends));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });


        UsersReferenceReceiver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot d : dataSnapshot.getChildren())
                    {
                        if(d.getKey().equals("image")){
                            Picasso.with(getApplicationContext()).load((String) d.getValue()).into(image);

                        }

                        if(d.getKey().equals("username")){
                            u.setText((String)d.getValue());
                            u.setTextColor(getResources().getColor(R.color.white));
                            u.animate().setDuration(2000).setStartDelay(3000);
                            u.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.fade_in));
                        }

                        if(d.getKey().equals("fullname")){
                            f.setText((String)d.getValue());
                            f.setTextColor(getResources().getColor(R.color.white));
                            f.animate().setDuration(2000).setStartDelay(3000);
                            f.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.fade_in));
                        }
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                //intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, Gallery_pick);
            }
        });

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

                String userid = FirebaseAuth.getInstance().getUid();
                StorageReference filePath = storeProfileImagestorageRef.child(userid+".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(ProfileActivity.this, "Saving your profile Image...", Toast.LENGTH_SHORT).show();

                            String downloadUrl = task.getResult().getDownloadUrl().toString();
                            UsersReferenceReceiver.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(ProfileActivity.this, "Image Updated Successfully...", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(ProfileActivity.this, "Error ocurred, while uploading your profile Image...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
