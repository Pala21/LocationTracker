package com.example.pau.locationtracker;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
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

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddFriendFragment extends Fragment implements SearchView.OnQueryTextListener{


    private static String CURRENT_STATE;
    private RecyclerView allUsersList;
    private DatabaseReference allUsersReference;
    private static DatabaseReference FriendsReference;
    private static DatabaseReference FriendRequestReference;
    private View myMainView;
    private FirebaseAuth mAuth;
    String sender_user_id;
    private List<Users> mUsers;
    private adapterUsers adapter;
    FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter;


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

        firebaseRecyclerAdapter =new FirebaseRecyclerAdapter<Users, UsersViewHolder>
                (
                        Users.class,
                        R.layout.all_users_display_layout,
                        UsersViewHolder.class,
                        allUsersReference
                )
        {
            @Override
            protected void populateViewHolder(final UsersViewHolder viewHolder, final Users model, int position)
            {
                final View view = viewHolder.mView;
                final Button btn = view.findViewById(R.id.btn);
                String list_user_id = getRef(position).getKey();
                final String receiver_user_id = getRef(position).getKey();

                if (allUsersReference.child(list_user_id).getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    viewHolder.setVis();
                }else{
                    viewHolder.setUsername(model.getUsername());
                    viewHolder.setFullname(model.getFullname());
                    System.out.println("MODEL FULLNAME:: "+model.getFullname());
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


                    /*Unfollow => usuaris afegits a amics*/

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


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        mUsers = new ArrayList<>();
        allUsersReference = FirebaseDatabase.getInstance().getReference().child("users");

        allUsersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot a : dataSnapshot.getChildren()) {
                    if(a.child("username").exists() && a.child("fullname").exists() && a.child("image").exists()){
                        mUsers.add(new Users(a.child("username").getValue().toString(),
                                a.child("fullname").getValue().toString(), a.child("image").getValue().toString(), a.getKey()));
                    }else if(a.child("username").exists() && a.child("fullname").exists())
                    {
                        mUsers.add(new Users(a.child("username").getValue().toString(),
                                a.child("fullname").getValue().toString(), a.child("noprofileimage").getValue().toString(),a.getKey() ));
                    }else if(a.child("username").exists())
                    {
                        mUsers.add(new Users(a.child("username").getValue().toString(),null, a.child("noprofileimage").getValue().toString(), a.getKey()));
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.options_menu, menu);
        final MenuItem item = menu.findItem(R.id.menuSearch);

        final SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(this); //OnQueryTextSubmit, onQueryTextChange
        adapter = new adapterUsers(mUsers, getContext());

        searchView.setOnCloseListener(new SearchView.OnCloseListener(){
            @Override
            public boolean onClose() {
                allUsersList.setAdapter(firebaseRecyclerAdapter);
                return false;
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        final List<Users> filteredModelList = filter(mUsers, s);
        System.out.println("MUSERSSSSS:: "+filteredModelList);
        adapter.setFilter(filteredModelList);
        allUsersList.setAdapter(adapter);

        return true;
    }

    private List<Users> filter(List<Users> models, String query) {
        query = query.toLowerCase();
        final List<Users> filteredModelList = new ArrayList<>();
        for (Users model : models) {
            final String text = model.getUsername().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }

        System.out.println("FilteredModelList :: " + filteredModelList);
        return filteredModelList;
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public TextView userN;
        public TextView fullN;

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

        public void setButtonStateUnfollow(final String receiver_user_id, final String sender_user_id)
        {
            final Button btn = (Button) mView.findViewById(R.id.btn);
            FriendsReference.child(sender_user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        for(DataSnapshot d2 :dataSnapshot.getChildren())
                        {
                            if(d2.getKey().equals(receiver_user_id))
                            {
                                Button btn = (Button) mView.findViewById(R.id.btn);
                                btn.setBackgroundResource(R.drawable.unfollow_friend_button);
                                btn.setTextColor(Color.parseColor("#0080ff"));
                                btn.setText("Unfollow");
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setButtonStateToPending(final String sender_user_id){
            Button btn = (Button) mView.findViewById(R.id.btn);
            FriendRequestReference.child(sender_user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot d2 : dataSnapshot.getChildren()) {
                            System.out.println("TOOOOOOOOOOOOOOOOOOOOOOOOOO "+d2.child("request_type").getValue());
                            if(d2.child("request_type").getValue().equals("sent"))
                            {
                                Button btn = (Button) mView.findViewById(R.id.btn);
                                //CURRENT_STATE = "request_sent";
                                btn.setText("Pending");
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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

        public void bind(Users user) {
            userN.setText(user.getUsername());
            fullN.setText(user.getFullname());
            setUserImage(user.getUserImage(),mView.getContext());
        }
    }

}