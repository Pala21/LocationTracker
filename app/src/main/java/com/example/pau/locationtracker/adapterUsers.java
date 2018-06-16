package com.example.pau.locationtracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.pau.locationtracker.RequestFragment.sender_user_id;

/**
 * Created by pau on 01/05/2018.
 */

public class adapterUsers extends RecyclerView.Adapter<AddFriendFragment.UsersViewHolder>{
    private List<Users> mUsers;
    Context context;
    private static DatabaseReference FriendRequestReference;
    private DatabaseReference allUsersReference;
    private DatabaseReference FriendsReference;
    private FirebaseAuth mAuth;

    public adapterUsers(List<Users> mUsers, Context context) {

        this.mUsers = mUsers;
        this.context = context;
    }

    @Override
    public AddFriendFragment.UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);
        FriendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        FriendRequestReference.keepSynced(true);
        allUsersReference = FirebaseDatabase.getInstance().getReference().child("users");
        allUsersReference.keepSynced(true);
        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();
        return new AddFriendFragment.UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AddFriendFragment.UsersViewHolder holder, int position) {
        final Users user = mUsers.get(position);

        final Users model = mUsers.get(position);
        final View view = holder.mView;
        final Button btn = view.findViewById(R.id.btn);
        String list_user_id = mUsers.get(position).getKey();
        final String receiver_user_id =  mUsers.get(position).getKey();
        System.out.println("RECEIVER USER ID:: "+receiver_user_id);

        if (allUsersReference.child(list_user_id).getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            holder.setVis();
        } else {
            holder.setUsername(model.getUsername());
            holder.setFullname(model.getFullname());

            holder.setButtonStateUnfollow(receiver_user_id, sender_user_id );


            System.out.println("MODEL FULLNAME:: " + model.getFullname());
            if (model.getUserImage() != null)
                holder.setUserImage(model.getUserImage(), context);

            System.out.println("Sender user id " +sender_user_id);


            FriendRequestReference.child(sender_user_id).child(receiver_user_id).child("request_type").
                    addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String val = dataSnapshot.getValue().toString();
                                if (val.equals("sent")) {
                                    holder.setButtonText();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

            FriendsReference.child(sender_user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot d2 : dataSnapshot.getChildren()) {
                            if (d2.getKey().equals(receiver_user_id)) {
                                holder.setButtonTextToUnfollow();
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
                        holder.SendFriendRequestToAFriend(receiver_user_id, sender_user_id);
                        notifyDataSetChanged();

                    }
                    else if(btn.getText().equals("Pending")){
                        holder.RemoveFriendRequestToAFriend(receiver_user_id, sender_user_id);
                        notifyDataSetChanged();

                    }
                    else
                    {
                        AlertDialog.Builder builder;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
                        } else {
                            builder = new AlertDialog.Builder(context);
                        }
                        builder.setTitle("Unfollow Friend")
                                .setMessage("Are you sure you want to Unfollow "+model.getUsername()+ "?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        holder.RemoveFriendToFriends(receiver_user_id,sender_user_id);
                                        notifyDataSetChanged();

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
                }
            });

            //això ha de quedar comentat, sinò la lupa falla.
            // holder.bind(user);
        }
    }



    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void setFilter(List<Users> users) {
        mUsers = new ArrayList<>();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }

}