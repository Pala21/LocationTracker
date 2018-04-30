package com.example.pau.locationtracker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFragment extends Fragment {

    private View myMainView;
    private RecyclerView allRequestsList;
    private static DatabaseReference FriendRequestReference;
    private FirebaseAuth mAuth;
    static String sender_user_id;
    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment


        myMainView = inflater.inflate(R.layout.fragment_request, container, false);
        allRequestsList = myMainView.findViewById(R.id.all_requests_list);
        allRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();
        FriendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(sender_user_id);
        FriendRequestReference.keepSynced(true);

        return myMainView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseRecyclerAdapter<Users, RequestFragment.UsersViewHolder> firebaseRecyclerAdapter2
                =new FirebaseRecyclerAdapter<Users, RequestFragment.UsersViewHolder>
                (
                        Users.class,
                        R.layout.all_requests_display_layout,
                        RequestFragment.UsersViewHolder.class,
                        FriendRequestReference
                )
        {
            @Override
            protected void populateViewHolder(final UsersViewHolder viewHolder, final Users model, int position) {
                final View view = viewHolder.mView;
                final Button acceptBtn = view.findViewById(R.id.AcceptBtn);
                final Button denyBtn = view.findViewById(R.id.DenyBtn);

                String list_user_id = getRef(position).getKey();
                final String receiver_user_id = getRef(position).getKey();

                FriendRequestReference.child(receiver_user_id).child("request_type").
                        addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    String val = dataSnapshot.getValue().toString();
                                    if(val.equals("sent"))
                                    {
                                        viewHolder.setVisibility();
                                    }else{
                                        viewHolder.searchInformationFromContact(receiver_user_id, getContext());
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });

                acceptBtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        viewHolder.addToDatabaseAndRemoveToRequests(receiver_user_id);
                    }
                });

                denyBtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        viewHolder.removeToRequests(receiver_user_id);
                    }
                });
            }
        };
        allRequestsList.setAdapter(firebaseRecyclerAdapter2);

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        View mView;


        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setVisibility() {
            mView.findViewById(R.id.linearDisplayUsers).setVisibility(View.GONE);
        }


        public void searchInformationFromContact(String receiver_user_id, final Context ctx) {
            final TextView username = (TextView) mView.findViewById(R.id.all_users_username);
            final TextView fullname = (TextView) mView.findViewById(R.id.all_users_fullname);
            final CircleImageView image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);

            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("users").child(receiver_user_id);

            userReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println("KEY ::" + dataSnapshot.getValue());
                    for (DataSnapshot internFields : dataSnapshot.getChildren()) {
                        if (internFields.getKey().equals("fullName")) {
                            fullname.setText(internFields.getValue().toString());
                        }
                        if (internFields.getKey().equals("username")) {
                            username.setText(internFields.getValue().toString());
                        }
                        if (internFields.getKey().equals("image")) {
                            Picasso.with(ctx).load(internFields.getValue().toString()).into(image);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }

        public void addToDatabaseAndRemoveToRequests(final String receiver_user_id) {

            DatabaseReference userReferenceReceiver = FirebaseDatabase.getInstance().getReference().child("users").child(receiver_user_id);

            userReferenceReceiver.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println("KEY ::" + dataSnapshot.getValue());
                    String fullnameReceiver = null;
                    String usernameReceiver = null;
                    String imageReceiver = null;
                    for (DataSnapshot internFields : dataSnapshot.getChildren()) {
                        if (internFields.getKey().equals("fullName")) {
                            fullnameReceiver = internFields.getValue().toString();
                        }
                        if (internFields.getKey().equals("username")) {
                            usernameReceiver = internFields.getValue().toString();
                        }
                        if (internFields.getKey().equals("image")) {
                            imageReceiver = internFields.getValue().toString();
                        }
                    }
                    DatabaseReference FriendsReferenceSender = FirebaseDatabase.getInstance().getReference().child("Friends").child(sender_user_id);
                    FriendsReferenceSender.child(receiver_user_id).child("fullname").setValue(fullnameReceiver);
                    FriendsReferenceSender.child(receiver_user_id).child("username").setValue(usernameReceiver);
                    FriendsReferenceSender.child(receiver_user_id).child("image").setValue(imageReceiver);
                    FriendRequestReference.child(receiver_user_id).removeValue();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });


            DatabaseReference userReferenceSender = FirebaseDatabase.getInstance().getReference().child("users").child(sender_user_id);

            userReferenceSender.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println("KEY ::" + dataSnapshot.getValue());
                    String fullnameSender = null;
                    String usernameSender = null;
                    String imageSender = null;
                    for (DataSnapshot internFields : dataSnapshot.getChildren()) {
                        if (internFields.getKey().equals("fullName")) {
                            fullnameSender = internFields.getValue().toString();
                        }
                        if (internFields.getKey().equals("username")) {
                            usernameSender = internFields.getValue().toString();
                        }
                        if (internFields.getKey().equals("image")) {
                            imageSender = internFields.getValue().toString();
                        }
                    }
                    DatabaseReference FriendsReferenceReceiver = FirebaseDatabase.getInstance().getReference().child("Friends").child(receiver_user_id);
                    FriendsReferenceReceiver.child(sender_user_id).child("fullname").setValue(fullnameSender);
                    FriendsReferenceReceiver.child(sender_user_id).child("username").setValue(usernameSender);
                    FriendsReferenceReceiver.child(sender_user_id).child("image").setValue(imageSender);
                    DatabaseReference FriendRequestReferenceReceiver = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(receiver_user_id);
                    FriendRequestReferenceReceiver.child(sender_user_id).removeValue();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

        }

        public void removeToRequests(String receiver_user_id)
        {
            FriendRequestReference.child(receiver_user_id).removeValue();
            DatabaseReference FriendRequestReferenceReceiver = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(receiver_user_id);
            FriendRequestReferenceReceiver.child(sender_user_id).removeValue();
        }
    }
}
