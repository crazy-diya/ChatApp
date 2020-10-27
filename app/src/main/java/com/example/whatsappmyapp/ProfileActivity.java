package com.example.whatsappmyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId,senderUserId, Current_State;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, DeclineMessageRequestButton;
    private Toolbar mToolbar;

    private DatabaseReference UserRef, ChatRequestRef, ContactsRef, NotificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(null);


        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notification");

        receiverUserId = getIntent().getStringExtra("visit_user_id");
        senderUserId = mAuth.getCurrentUser().getUid();

        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        sendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);
        Current_State = "new";
        DeclineMessageRequestButton = (Button) findViewById(R.id.decline_message_request_button);

        RetriveUserInfo();
    }

    private void RetriveUserInfo() {
        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("image"))){
                    String userImage = snapshot.child("image").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }else{
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequest() {

        ChatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(receiverUserId)){
                            String request_type = snapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if (request_type.equals("sent")){
                                Current_State = "request_sent";
                                sendMessageRequestButton.setText("Cancel Chat Request");
                            }
                            else if(request_type.equals( "received")){
                                Current_State = "request_received";
                                sendMessageRequestButton.setText("Accept Chat Request");

                                DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                                DeclineMessageRequestButton.setEnabled(true);

                                DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }
                        else{
                            ContactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild(receiverUserId)){
                                                Current_State = "friends";
                                                sendMessageRequestButton.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if (!senderUserId.equals(receiverUserId)){
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);

                    if (Current_State.equals("new")){
                        SendChatRequest();
                    }
                    if (Current_State.equals("request_sent")){
                        CancelChatRequest();
                    }
                    if (Current_State.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if (Current_State.equals("friends")){
                        RemoveSpecificContact();
                    }
                }
            });

        }else{
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact() {
        ContactsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            ContactsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State = "new";
                                                sendMessageRequestButton.setText("Send Message");


                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        ContactsRef.child(senderUserId).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            ContactsRef.child(receiverUserId).child(senderUserId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                ChatRequestRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    ChatRequestRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                     sendMessageRequestButton.setEnabled(true);
                                                                                     Current_State = "friends";
                                                                                     sendMessageRequestButton.setText("Remove this Contact");

                                                                                     DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                     DeclineMessageRequestButton.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelChatRequest() {
        ChatRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            ChatRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State = "new";
                                                sendMessageRequestButton.setText("Send Message");


                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest() {
        ChatRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            ChatRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                HashMap<String, String> chatNotification = new HashMap<>();
                                                chatNotification.put("from", senderUserId);
                                                chatNotification.put("type", "request");

                                                NotificationRef.child(receiverUserId).push()
                                                        .setValue(chatNotification)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            sendMessageRequestButton.setEnabled(true);
                                                            Current_State = "request_sent";
                                                            sendMessageRequestButton.setText("Cancel Chat Request");
                                                        }
                                                    }
                                                });

                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State = "request_sent";
                                                sendMessageRequestButton.setText("Cancel Chat Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}