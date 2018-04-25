package com.example.pau.locationtracker;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class AddFriendFragment extends Fragment {


    private RecyclerView allUsersList;
    private DatabaseReference allUsersReference;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private View myMainView;

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
        allUsersList.setLayoutManager(new LinearLayoutManager(getContext()));
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
                viewHolder.setUsername(model.getUsername());
                viewHolder.setFullname(model.getFullname());
                if(model.getUserImage() != null)
                    viewHolder.setUserImage(model.getUserImage(), getContext());
                System.out.println("Imageeee:: "+model.getUserImage());
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
    }

}
