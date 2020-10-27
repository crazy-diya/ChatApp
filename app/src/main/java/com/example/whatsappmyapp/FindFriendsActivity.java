package com.example.whatsappmyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView FindFriendRecyclerList;
    private DatabaseReference UserRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);/**Toolbar eke back Button eka me code 2n*/
        getSupportActionBar().setDisplayShowHomeEnabled(true);/** thama access karanne*/
        getSupportActionBar().setTitle("Find Friend");/**Toolbar Name eka*/

        /**Resycler View ekak Innitiate karanne*/
        FindFriendRecyclerList = (RecyclerView) findViewById(R.id.find_friend_recycler_list);
        FindFriendRecyclerList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {/**this run after onCreate() method. so its automaticaly run*/
        super.onStart();

        /** Get FirebaseDatabase data to RecyclerView list............*/
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(UserRef,Contacts.class).build(); /** Firebase database eke thiyena data Contact class ekata gannawa*/

        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {
                /**Holder kiyala api ara thiyenne friends lage eka friend kenekwa....  ee widiye holdersla hugak innawa findfriend eke..*/
                holder.userName.setText(model.getName());/*Methanadi wenne FindFriendViewHolder thiyene xml object walata Contact class ekata assign wenawa*/
                holder.userStatus.setText(model.getStatus());
                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {/**Click karanawa Friend kenekwa*/
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey(); //Get Id to the FirebaseDatabase Users positions... really remmebber it ok
                        Intent profileIntent = new Intent(FindFriendsActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileIntent);
                    }
                });

            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                return viewHolder;
            }
        };

        FindFriendRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class FindFriendViewHolder extends RecyclerView.ViewHolder{  //Recycler view Holder eka hadanawa
        TextView userName, userStatus;/**Wenne Innitiate wenawa Resycler View ekata yana eewa*/
        CircleImageView profileImage;
        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = (TextView) itemView.findViewById(R.id.user_profile_name);
            userStatus = (TextView) itemView.findViewById(R.id.user_status);
            profileImage = (CircleImageView) itemView.findViewById(R.id.user_profile_image);
        }
    } /** Me class eken karanne hadala thiena xml layout eke object tika gannawa.*/


}