package com.example.pau.locationtracker;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView allFriendsList;

    private DatabaseReference allFriendsReference;

    private Toolbar mToolbar;
    private View myMainView;
    private FirebaseAuth mAuth;
    String sender_user_id;
    String receiver_user_id;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        myMainView = inflater.inflate(R.layout.fragment_friends2, container, false);
        allFriendsList = myMainView.findViewById(R.id.all_friends_list);

        allFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        allFriendsReference.keepSynced(true);

        allFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();

        return myMainView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsFragment.FriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                        Friends.class,
                        R.layout.all_friends_display_layout,
                        FriendsFragment.FriendsViewHolder.class,
                        allFriendsReference
                ) {
            @Override
            protected void populateViewHolder(final FriendsFragment.FriendsViewHolder viewHolder, final Friends model, int position)
            {
                View view = viewHolder.mView;
                final Button btn = view.findViewById(R.id.btn);
                final String receiver_user_id = getRef(position).getKey();

                viewHolder.setUsername(model.getUsername());
                viewHolder.setFullname(model.getFullname());
                if (model.getUserImage() != null)
                    viewHolder.setUserImage(model.getUserImage(), getContext());

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
                        } else {
                            builder = new AlertDialog.Builder(getContext());
                        }
                        builder.setTitle("Unfollow Friend")
                                .setMessage("Are you sure you want to Unfollow "+model.getUsername()+ "?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        viewHolder.unfollowUser(sender_user_id, receiver_user_id);
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
            }
        };
        allFriendsList.setAdapter(firebaseRecyclerAdapter);

    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;


        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setVis() {
            mView.findViewById(R.id.linearDisplayUsers).setVisibility(View.GONE);
        }

        public void setUsername(String u) {
            TextView username = (TextView) mView.findViewById(R.id.all_users_username);
            username.setText(u);
        }

        public void setFullname(String f) {
            TextView fullname = (TextView) mView.findViewById(R.id.all_users_fullname);
            fullname.setText(f);
        }

        public void setUserImage(String userImage, Context ctx) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(userImage).into(image);
        }

        public void unfollowUser(String sender_user_id, String receiver_user_id) {
            DatabaseReference FriendsReferenceReceiver = FirebaseDatabase.getInstance().getReference().child("Friends").child(receiver_user_id);
            FriendsReferenceReceiver.child(sender_user_id).removeValue();
            DatabaseReference FriendsReferenceSender = FirebaseDatabase.getInstance().getReference().child("Friends").child(sender_user_id);
            FriendsReferenceSender.child(receiver_user_id).removeValue();
        }
    }

}
