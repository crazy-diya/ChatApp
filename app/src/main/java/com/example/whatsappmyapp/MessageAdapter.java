package com.example.whatsappmyapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessageAdapter (List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView sendMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            sendMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.reciver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageSenderPicture = (ImageView) itemView.findViewById(R.id.message_sender_image_viewer);
            messageReceiverPicture = (ImageView) itemView.findViewById(R.id.message_receiver_image_viewer);
        }
    }



    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_message_layot, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String formUserID = messages.getFrom();
        String formMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(formUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("image")){
                    String receiverImage = snapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.sendMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);


        if (formMessageType.equals("text")){

            if (formUserID.equals(messageSenderId)){
                holder.sendMessageText.setVisibility(View.VISIBLE);

                holder.sendMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.sendMessageText.setTextColor(Color.BLACK);
                holder.sendMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
            }else{


                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.reciver_messages_layout);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
            }
        }
        else if (formMessageType.equals("image")){

            if (formUserID.equals(messageSenderId)){
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);
            }
            else{
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);
            }
        }
        else if (formMessageType.equals("pdf") || formMessageType.equals("docx")){
            if (formUserID.equals(messageSenderId)){

                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsapp-my-app.appspot.com/o/Image%20Files%2Fdocument.png?alt=media&token=46f9b9da-1133-4092-82d2-5cab461143e7").into(holder.messageSenderPicture);


            }else{

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsapp-my-app.appspot.com/o/Image%20Files%2Fdocument.png?alt=media&token=46f9b9da-1133-4092-82d2-5cab461143e7").into(holder.messageReceiverPicture);

            }
        }



        if (formUserID.equals(messageSenderId)){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")){

                        CharSequence sequence[] = new CharSequence[]{
                                "Delete for me",
                                "Download and view this Document",
                                "Cancel",
                                "Delete for Everyone"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(sequence, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if (which == 1){

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 3){
                                    deleteMessagesForEveryOne(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();

                    }
                    else if (userMessagesList.get(position).getType().equals("text")){

                        CharSequence sequence[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel",
                                "Delete for Everyone"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(sequence, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if (which == 2){
                                    deleteMessagesForEveryOne(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();

                    }
                    else if (userMessagesList.get(position).getType().equals("image")){

                        CharSequence sequence[] = new CharSequence[]{
                                "Delete for me",
                                "view this Image",
                                "Cancel",
                                "Delete for Everyone"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(sequence, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if (which == 1){
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }

                                else if (which == 3){
                                    deleteMessagesForEveryOne(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();

                    }
                }
            });
        }
        else{

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")){

                        CharSequence sequence[] = new CharSequence[]{
                                "Delete for me",
                                "Download and view this Document",
                                "Cancel"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(sequence, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSReceiveMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if (which == 1){

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();

                    }
                    else if (userMessagesList.get(position).getType().equals("text")){

                        CharSequence sequence[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(sequence, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSReceiveMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }

                            }
                        });
                        builder.show();

                    }
                    else if (userMessagesList.get(position).getType().equals("image")){

                        CharSequence sequence[] = new CharSequence[]{
                                "Delete for me",
                                "view this Image",
                                "Cancel"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(sequence, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSReceiveMessages(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if (which == 1){

                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                }


                            }
                        });
                        builder.show();

                    }
                }
            });

        }

    }



    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


    private void deleteSentMessages(final int position, final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred!", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }



    private void deleteSReceiveMessages(final int position, final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred!", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


    private void deleteMessagesForEveryOne(final int position, final MessageViewHolder holder)
    {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    rootRef.child("Messages").child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully!", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred!", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

}
