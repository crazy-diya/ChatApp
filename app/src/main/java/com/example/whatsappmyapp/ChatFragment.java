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

public class ChatFragment extends Fragment {

    private View PrivateChatsView;
    private RecyclerView chatList;

    private DatabaseReference ChatsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;


    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView =  inflater.inflate(R.layout.fragment_chat, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        chatList = (RecyclerView) PrivateChatsView.findViewById(R.id.chat_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));

        return PrivateChatsView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> option = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ChatViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatViewHolder>(option) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull Contacts model) {
                        final String userIDs = getRef(position).getKey();
                        final String[] retImage = {"default_image"};
                        UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){

                                    if (snapshot.hasChild("image")){
                                        retImage[0] = snapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).into(holder.profileImage);
                                    }

                                    final String retName = snapshot.child("name").getValue().toString();
                                    final String retStatus = snapshot.child("status").getValue().toString();

                                    holder.userName.setText(retName);


                                    if (snapshot.child("userState").hasChild("state")){
                                        String state = snapshot.child("userState").child("state").getValue().toString();
                                        String date = snapshot.child("userState").child("date").getValue().toString();
                                        String time = snapshot.child("userState").child("time").getValue().toString();

                                        if (state.equals("online")){
                                            holder.userStatus.setText("online" );
                                        }
                                        else if (state.equals("offline")){
                                            holder.userStatus.setText("Last Seen: " + date + " " + time );
                                        }
                                    }
                                    else{
                                        holder.userStatus.setText("offline" );
                                    }


                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id",userIDs);
                                            chatIntent.putExtra("visit_user_name",retName);
                                            chatIntent.putExtra("visit_user_image", retImage[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);

                        return new ChatViewHolder(view);
                    }
                };
        chatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImage;
        TextView userStatus, userName;


        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.user_profile_image);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
        }
    }
}