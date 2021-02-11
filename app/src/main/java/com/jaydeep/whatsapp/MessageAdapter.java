package com.jaydeep.whatsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    private FirebaseAuth fAuth;
    private DatabaseReference usersRef;




    public MessageAdapter(List<Messages> userMessagesList) {

        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);

        fAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        String messageSenderID = fAuth.getCurrentUser().getUid();

        Messages messages = userMessagesList.get(position);


        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("image")) {
                    Log.d("ROCK", "complete");
                    String receiverProfileImage = snapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverProfileImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);

        if(fromMessageType.equals("text")) {

            if(fromUserID.equals(messageSenderID)) {
                holder.senderMessageText.setVisibility(View.VISIBLE);

                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(messages.getMessage() + "\n\n" + messages.getTime() + " - " + messages.getDate());
            }
            else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(messages.getMessage() + "\n\n" + messages.getTime() + " - " + messages.getDate());

            }
        }
        else if(fromMessageType.equals("image")) {

            if(fromUserID.equals(messageSenderID)) {

                holder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);
            }
            else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);
            }
        }
        else if(fromMessageType.equals("pdf") || fromMessageType.equals("docx")){
            if(fromUserID.equals(messageSenderID)) {

                holder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/whatsapp-4cfed.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=a6a7abad-fc95-4b68-928f-8a80775534a7")
                        .into(holder.messageSenderPicture);
            }
            else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/whatsapp-4cfed.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=a6a7abad-fc95-4b68-928f-8a80775534a7")
                        .into(holder.messageReceiverPicture);

            }

        }

        if(fromUserID.equals(messageSenderID)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("pdf")) {

                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "Download and View This Document",
                                        "Cancel",
                                        "Delete for Everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0) {

                                    deleteSentMessage(i,holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if(i==1) {

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if(i==2) {

                                }
                                else if(i==3) {

                                    deleteMessagesForEveryone(i,holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });

                        builder.show();
                    }

                    else if(userMessagesList.get(position).getType().equals("text")) {

                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "Cancel",
                                        "Delete for Everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0) {

                                    deleteSentMessage(i,holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if(i==1) {

                                }
                                else if(i==2) {
                                    deleteMessagesForEveryone(i,holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });

                        builder.show();
                    }

                    else if(userMessagesList.get(position).getType().equals("image")) {

                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "View This Image",
                                        "Cancel",
                                        "Delete for Everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0) {
                                    deleteSentMessage(i,holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==1) {
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==2) {

                                }
                                else if(i==3) {
                                    deleteMessagesForEveryone(i,holder);

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
        else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("pdf")) {

                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "Download and View This Document",
                                        "Cancel",
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0) {
                                    deleteReceiveMessage(i,holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if(i==1) {

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if(i==2) {

                                }
                            }
                        });

                        builder.show();
                    }

                    else if(userMessagesList.get(position).getType().equals("text")) {

                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0) {
                                    deleteReceiveMessage(i,holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if(i==1) {

                                }
                            }
                        });

                        builder.show();
                    }

                    else if(userMessagesList.get(position).getType().equals("image")) {

                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "View This Image",
                                        "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0) {
                                    deleteReceiveMessage(i,holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if(i==1) {
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==2) {

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

    private void deleteSentMessage(final int position, final MessageViewHolder holder) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                
                if(task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Deleted.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error : " + task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void deleteReceiveMessage(final int position, final MessageViewHolder holder) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Deleted.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error : " + task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void deleteMessagesForEveryone(final int position, final MessageViewHolder holder) {

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()) {
                    rootRef.child("Message")
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(holder.itemView.getContext(), "Deleted.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(holder.itemView.getContext(), "Error : " + task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error : " + task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
