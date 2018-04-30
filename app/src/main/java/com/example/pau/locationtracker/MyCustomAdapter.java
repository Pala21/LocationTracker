/*package com.example.pau.locationtracker;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyCustomAdapter  extends BaseAdapter implements ListAdapter, Filterable {
    private ArrayList<String> list;
    List<String> mOriginalValues;
    private Context context;
    private AlphaAnimation buttonClick = new AlphaAnimation(10F, 0.8F);
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    ArrayList<Friends> myFriends;
    boolean noentris = false;


    public MyCustomAdapter(ArrayList<String> list, Context context, ArrayList<Friends> friends) {
        this.list = list;
        this.context = context;
        this.myFriends = friends;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        //return list.get(pos).getId();
        return 0;
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.custom_layout, null);
        }

        //Handle TextView and display string from your list
        TextView contactfriend = view.findViewById(R.id.contactFriend);
        contactfriend.setText(list.get(position));

        //Handle buttons and add onClickListeners
        final Button callbtn = view.findViewById(R.id.btn);

        callbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                noentris = false;
                v.startAnimation(buttonClick);

                final String TAG = FriendsActivity.class.getSimpleName();
                mDatabase.child("Friendships").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByChild("friendName").
                        equalTo(list.get(position)).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(!(dataSnapshot!=null && dataSnapshot.getChildren()!=null &&
                                dataSnapshot.getChildren().iterator().hasNext())) {
                            if(!noentris ){
                                Friends f = new Friends(list.get(position), false);
                                Log.d(TAG, "ENTRA FILTER  1 "+dataSnapshot.getKey() + "value "+dataSnapshot.getValue());
                                myFriends.add(f);
                                mDatabase.child("Friendships").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(myFriends);
                                Log.d(TAG, "ENTRA FILTER 1 "+myFriends);
                                System.out.println(myFriends);
                                noentris = true;
                                callbtn.setText("Pending");
                              
                            }
                        }else{
                            if(!noentris){
                                for(DataSnapshot a : dataSnapshot.getChildren()){
                                    String name = (String) a.child("friendName").getValue();
                                    if(name.equals(list.get(position))){
                                        String key = a.getKey();
                                        Log.d(TAG, "ENTRA FILTER 2 POSITIOOOON!"+position);
                                        mDatabase.child("Friendships").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(key).removeValue();
                                        for(Iterator<Friends> f = myFriends.iterator(); f.hasNext();){
                                            Friends friend = f.next();
                                            if(friend.friendName.equals(list.get(position))){
                                                System.out.println("entraaaaaaaaaaaaaaa delete");
                                                f.remove();
                                                callbtn.setText("Follow");
                                            }
                                        }
                                        noentris = true;
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
}
                });
            }
        });
        return view;
    }

    @Override
    public Filter getFilter() {
        final String TAG = FriendsActivity.class.getSimpleName();

        Filter filter = new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                list = (ArrayList<String>) results.values; // has the filtered values
                Log.d(TAG, "ENTRA FILTER "+results.values);
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                List<String> FilteredArrList = new ArrayList<String>();

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<String>(list); // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
          /*      if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mOriginalValues.size(); i++) {
                        String data = mOriginalValues.get(i);
                        if (data.toLowerCase().startsWith(constraint.toString())) {
                            FilteredArrList.add(data);
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }
}*/