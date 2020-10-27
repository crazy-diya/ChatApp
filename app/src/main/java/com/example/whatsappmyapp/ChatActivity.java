package com.example.whatsappmyapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String getmessageReciverId, getMessageReciverName, getMessageReciverImage, messageSenderId;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ImageButton SendMessageButton, SendFilesButton;
    private EditText MessageInputText;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    private String saveCurrentTime, saveCurrentDate;
    private String checker = "", myUrl;
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        getmessageReciverId = getIntent().getStringExtra("visit_user_id");
        getMessageReciverName = getIntent().getStringExtra("visit_user_name");
        getMessageReciverImage = getIntent().getStringExtra("visit_user_image");


        InitializeControllers();

        userName.setText(getMessageReciverName);
        Picasso.get().load(getMessageReciverImage).placeholder(R.drawable.profile_image).into(userImage);

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

        DisplayLastSeen();

        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence option[] = new CharSequence[]{
                        "Images",
                        "PDF Files",
                        "MS Word Files"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File :");
                builder.setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 1);
                        }
                        if (which == 1){
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF Files"), 1);
                        }
                        if (which == 2){
                            checker = "docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select MS Word Files"), 1);
                        }
                    }
                });
                builder.show();
            }
        });

    }



    private void InitializeControllers() {

        ChatToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        /**Custome Bar ekak add kalee meeka hodata balanna*/
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        getSupportActionBar().setCustomView(actionBarView);

        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        MessageInputText = (EditText) findViewById(R.id.input_message);
        SendFilesButton = (ImageButton) findViewById(R.id.send_file_btn);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_message_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        loadingBar = new ProgressDialog(this);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Plese wait, we are sending that file...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();

            if (!checker.equals("image")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef = "Messages/" + messageSenderId + "/" + getmessageReciverId;
                final String messageReceiverRef = "Messages/" + getmessageReciverId + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderId).child(getmessageReciverId).push();

                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId + "." + checker);

                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){

                            Task<Uri> uriTask = task.getResult().getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadUrl = uriTask.getResult();
                            String fileurl = downloadUrl.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message",fileurl);
                            System.out.println(downloadUrl);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderId);
                            messageTextBody.put("to", getmessageReciverId);
                            messageTextBody.put("messageID", messagePushId);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap(); /**Meeka wenuwata mulin than waladi baawitha kala wage puluwan database ekata add karanna*/
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails);
                            loadingBar.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, "Error is : "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0*taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + " % Uploading.. ");

                    }
                });

            }
            else if(checker.equals("image")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Messages/" + messageSenderId + "/" + getmessageReciverId;
                final String messageReceiverRef = "Messages/" + getmessageReciverId + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderId).child(getmessageReciverId).push();

                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId + "." + "jpg");
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message",myUrl);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderId);
                            messageTextBody.put("to", getmessageReciverId);
                            messageTextBody.put("messageID", messagePushId);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap(); /**Meeka wenuwata mulin than waladi baawitha kala wage puluwan database ekata add karanna*/
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message Sent Succesfully!", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                    }
                                    MessageInputText.setText(null);
                                }
                            });
                        }
                    }
                });
            }else{
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing Selected, Error!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void DisplayLastSeen(){
        RootRef.child("Users").child(getmessageReciverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child("userState").hasChild("state")){
                            String state = snapshot.child("userState").child("state").getValue().toString();
                            String date = snapshot.child("userState").child("date").getValue().toString();
                            String time = snapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online")){
                                userLastSeen.setText("online" );
                            }
                            else if (state.equals("offline")){
                                userLastSeen.setText("Last Seen: " + date + " " + time );
                            }
                        }
                        else{
                            userLastSeen.setText("offline" );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        RootRef.child("Messages").child(messageSenderId).child(getmessageReciverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Messages messages = snapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void SendMessage() {
        String messageText = MessageInputText.getText().toString();
        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "fist write your message...", Toast.LENGTH_SHORT).show();
        }
        else{
            String messageSenderRef = "Messages/" + messageSenderId + "/" + getmessageReciverId;
            String messageReceiverRef = "Messages/" + getmessageReciverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderId).child(getmessageReciverId).push();

            String messagePushId = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderId);
            messageTextBody.put("to", getmessageReciverId);
            messageTextBody.put("messageID", messagePushId);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetails = new HashMap(); /**Meeka wenuwata mulin than waladi baawitha kala wage puluwan database ekata add karanna*/
            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message Sent Succesfully!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(ChatActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                    MessageInputText.setText(null);
                }
            });

        }
    }
}


