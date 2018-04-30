package com.example.pau.locationtracker;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddFriendFragment extends Fragment {


    private static String CURRENT_STATE;
    private RecyclerView allUsersList;
    private DatabaseReference allUsersReference;
    private DatabaseReference FriendsReference;
    private static DatabaseReference FriendRequestReference;
    private Toolbar mToolbar;
    private View myMainView;
    private FirebaseAuth mAuth;
    String sender_user_id;
    String receiver_user_id;

    public AddFriendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        myMainView = inflater.inflate(R.layout.fragment_add_friend, container, false);
        allUsersList = myMainView.findViewById(R.id.all_users_list);
        allUsersReference = FirebaseDatabase.getInstance().getReference().child("users");
        allUsersReference.keepSynced(true);

        FriendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        FriendRequestReference.keepSynced(true);

        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        allUsersList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();

        return myMainView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Users, UsersViewHolder>
                (
                        Users.class,
                        R.layout.all_users_display_layout,
                        UsersViewHolder.class,
                        allUsersReference
                )
        {
            @Override
            protected void populateViewHolder(final UsersViewHolder viewHolder, Users model, int position)
            {
                View view = viewHolder.mView;
                final Button btn = view.findViewById(R.id.btn);
                String list_user_id = getRef(position).getKey();
                final String receiver_user_id = getRef(position).getKey();

                if (allUsersReference.child(list_user_id).getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    viewHolder.setVis();
                }else{
                    viewHolder.setUsername(model.getUsername());
                    viewHolder.setFullname(model.getFullname());
                    if(model.getUserImage() != null)
                        viewHolder.setUserImage(model.getUserImage(), getContext());


                    FriendRequestReference.child(sender_user_id).child(receiver_user_id).child("request_type").
                            addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                String val = dataSnapshot.getValue().toString();
                                if(val.equals("sent"))
                                {
                                    viewHolder.setButtonText();
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });

                    FriendsReference.child(sender_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                for(DataSnapshot d2 :dataSnapshot.getChildren())
                                {
                                    if(d2.getKey().equals(receiver_user_id))
                                    {
                                        viewHolder.setButtonTextToUnfollow();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                    btn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            System.out.println("SENDER ::"+sender_user_id+" RECEIVER:: "+receiver_user_id);
                            System.out.println("FOLLOW BTN: "+ btn.getText());
                            if(btn.getText().equals("Follow"))
                            {
                                viewHolder.SendFriendRequestToAFriend(receiver_user_id, sender_user_id);
                            }
                            else if(btn.getText().equals("Pending")){
                                viewHolder.RemoveFriendRequestToAFriend(receiver_user_id, sender_user_id);
                            }
                            else
                            {
                                viewHolder.RemoveFriendToFriends(receiver_user_id,sender_user_id);
                            }

                        }
                    });
                }
            }
        };
        allUsersList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder
    {
        View mView;


        public UsersViewHolder(View itemView)
        {
            super(itemView);

            mView = itemView;
        }

        public void setVis()
        {
            mView.findViewById(R.id.linearDisplayUsers).setVisibility(View.GONE);
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

        public void SendFriendRequestToAFriend(final String receiver_user_id, final String sender_user_id)
        {
            System.out.println("SENDER ::"+sender_user_id+" RECEIVER:: "+receiver_user_id);

            final Button btn = (Button) mView.findViewById(R.id.btn);
            FriendRequestReference.child(sender_user_id).child(receiver_user_id)
                    .child("request_type").setValue("sent")
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                FriendRequestReference.child(receiver_user_id).child(sender_user_id)
                                        .child("request_type").setValue("receiver")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    //btn.setEnabled(true);
                                                    //CURRENT_STATE = "request_sent";
                                                    btn.setText("Pending");
                                                }
                                            }
                                        });
                            }
                        }
                    });

        }

        public void setButtonText()
        {
            Button btn = (Button) mView.findViewById(R.id.btn);
            //CURRENT_STATE = "request_sent";
            btn.setText("Pending");
        }

        public void RemoveFriendRequestToAFriend(final String receiver_user_id, final String sender_user_id) {
            System.out.println("SENDER ::"+sender_user_id+" RECEIVER:: "+receiver_user_id);

            final Button btn = (Button) mView.findViewById(R.id.btn);
            FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                FriendRequestReference.child(receiver_user_id).child(sender_user_id)
                                        .child("request_type").removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    //btn.setEnabled(true);
                                                    //CURRENT_STATE = "request_sent";
                                                    btn.setText("Follow");
                                                }
                                            }
                                        });
                            }
                        }
                    });
        }

        public void setButtonTextToUnfollow() {
            Button btn = (Button) mView.findViewById(R.id.btn);
            btn.setBackgroundResource(R.drawable.unfollow_friend_button);
            btn.setTextColor(Color.parseColor("#0080ff"));
            btn.setText("Unfollow");
        }

        public void RemoveFriendToFriends(String receiver_user_id, String sender_user_id) {
            Button btn = (Button) mView.findViewById(R.id.btn);
            DatabaseReference FriendsReferenceReceiver = FirebaseDatabase.getInstance().getReference().child("Friends").child(receiver_user_id);
            FriendsReferenceReceiver.child(sender_user_id).removeValue();
            DatabaseReference FriendsReferenceSender = FirebaseDatabase.getInstance().getReference().child("Friends").child(sender_user_id);
            FriendsReferenceSender.child(receiver_user_id).removeValue();
            btn.setText("Follow");
        }
    }

}