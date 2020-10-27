package com.example.whatsappmyapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsFragment extends Fragment {

    private RecyclerView myContactsList;

    private View ContactsView;

    private DatabaseReference ContactRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public ContactsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        myContactsList = (RecyclerView) ContactsView.findViewById(R.id.contact_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        ContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, final int position, @NonNull Contacts model) {
                final String userIds = getRef(position).getKey();

                /*holder.itemView.setOnClickListener(new View.OnClickListener() {*//**Click karanawa Friend kenekwa*//*
                @Override
                public void onClick(View v) {
                    String visit_user_id = getRef(position).getKey(); //Get Id to the FirebaseDatabase Users positions... really remmebber it ok
                    Intent profileIntent = new Intent(getContext() ,ProfileActivity.class);
                    profileIntent.putExtra("visit_user_id",visit_user_id);
                    startActivity(profileIntent);
                }
                });*/

                UsersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){

                            if (snapshot.child("userState").hasChild("state")){
                                String state = snapshot.child("userState").child("state").getValue().toString();
                                String date = snapshot.child("userState").child("date").getValue().toString();
                                String time = snapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online")){
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                                else if (state.equals("offline")){
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);                                }
                            }
                            else{
                                holder.onlineIcon.setVisibility(View.INVISIBLE);                            }

                            if (snapshot.hasChild("image")){
                                String userImage = snapshot.child("image").getValue().toString();
                                String profileName = snapshot.child("name").getValue().toString();
                                String profileStatus = snapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }
                            else{
                                String profileName = snapshot.child("name").getValue().toString();
                                String profileStatus = snapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }
    }
}