package com.jaydeep.whatsapp;


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


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View PrivateChatsView;
    private RecyclerView chatList;
    private DatabaseReference ChatRef,UsersRef;
    private FirebaseAuth fAuth;
    private String currentUserID;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);


        fAuth = FirebaseAuth.getInstance();
        currentUserID = fAuth.getCurrentUser().getUid();
        ChatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatList = PrivateChatsView.findViewById(R.id.chat_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));

        return PrivateChatsView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ChatViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull Contacts model) {

                final String userIDs = getRef(position).getKey();
                final String[] retImage = {"default_image"};

                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){

                            if(snapshot.hasChild("image")) {
                                retImage[0] = snapshot.child("image").getValue().toString();

                                Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }

                            final String retName = snapshot.child("name").getValue().toString();
                            final String retStatus = snapshot.child("status").getValue().toString();

                            holder.userName.setText(retName);

                            if(snapshot.child("userState").hasChild("state")){

                                String date = snapshot.child("userState").child("date").getValue().toString();
                                String time = snapshot.child("userState").child("time").getValue().toString();
                                String status = snapshot.child("userState").child("state").getValue().toString();

                                if(status.equals("online")) {
                                    holder.userStatus.setText("online");
                                }
                                else if(status.equals("offline")) {
                                    holder.userStatus.setText("Last Seen: " +  date + " " + time);
                                }

                            }
                            else {
                                holder.userStatus.setText("offline");
                            }


                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id",userIDs);
                                    chatIntent.putExtra("visit_user_name",retName);
                                    chatIntent.putExtra("visit_image", retImage[0]);
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

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
        }
    }
}
