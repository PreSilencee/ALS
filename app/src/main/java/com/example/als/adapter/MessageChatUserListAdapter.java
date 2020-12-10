package com.example.als.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.als.R;
import com.example.als.handler.AESCrypt;
import com.example.als.handler.GlideApp;
import com.example.als.object.Contributor;
import com.example.als.object.Message;
import com.example.als.object.Organization;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.List;

public class MessageChatUserListAdapter extends RecyclerView.Adapter<MessageChatUserListAdapter.ViewHolder>{

    //console log
    private static final String TAG = "ChatListItemAdapter";

    //array list for user object
    private List<User> userList;

    //context for adapter
    private Context context;

    //onchatlistener
    private OnChatListener onChatListener;

    //constructor(User, context
    public MessageChatUserListAdapter(List<User> users, Context context){
        this.userList = users;
        this.context = context;
    }

    //create view for each Message User list
    @NonNull
    @Override
    public MessageChatUserListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_chat_list_view_layout,parent,false);
        return new MessageChatUserListAdapter.ViewHolder(view);
    }

    //attach data of users to view
    @Override
    public void onBindViewHolder(@NonNull final MessageChatUserListAdapter.ViewHolder holder, int position) {
        //get user position
        final User user = userList.get(position);

        //check if user is contributor
        if(user.getRole().equals(Variable.CONTRIBUTOR)){
            //get data
            Variable.CONTRIBUTOR_REF.child(user.getId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //if exists
                    if(snapshot.exists()){
                        //get contributor object
                        Contributor contributor = snapshot.getValue(Contributor.class);

                        //if contributor not null
                        if(contributor != null){
                            // if contributor name not null
                            if(contributor.getName() != null){
                                holder.messageChatListProfileNameTV.setText(contributor.getName());
                            }
                            else{
                                //set "-" as default
                                holder.messageChatListProfileNameTV.setText("-");
                            }

                            //if contributor profile image name not null
                            if(contributor.getProfileImageName() != null) {

                                //create api of the image
                                StorageReference imageRef = Variable.CONTRIBUTOR_SR.child(contributor.getUserId())
                                        .child("profile").child(contributor.getProfileImageName());

                                //get url and download
                                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.d(TAG, "loadImage: success");
                                        //push image to image view
                                        GlideApp.with(context)
                                                .load(uri)
                                                .placeholder(R.drawable.loading_image)
                                                .into(holder.messageChatListProfileIV);
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "loadImage:Failed");
                                                //show loading image
                                                holder.messageChatListProfileIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                            }
                                        });
                            }
                            else{
                                //show loading image
                                holder.messageChatListProfileIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                            }
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "Database Error: " + error.getMessage());
                }
            });
        }
        //if user is organization
        else{
            Variable.ORGANIZATION_REF.child(user.getId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //if exists
                    if(snapshot.exists()){

                        //get organization object
                        Organization organization = snapshot.getValue(Organization.class);

                        //if organization object not null
                        if(organization != null){

                            //if organization name not null
                            if(organization.getOrganizationName() != null){
                                holder.messageChatListProfileNameTV.setText(organization.getOrganizationName());
                            }
                            else{
                                //set "-" as default
                                holder.messageChatListProfileNameTV.setText("-");
                            }

                            //if organization profile image name not null
                            if(organization.getOrganizationProfileImageName() != null){

                                //create api of image
                                StorageReference imageRef = Variable.ORGANIZATION_SR.child(organization.getUserId())
                                        .child("profile").child(organization.getOrganizationProfileImageName());

                                //get url and download it
                                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.d(TAG, "loadImage: success");

                                        //push image to image view
                                        GlideApp.with(context)
                                                .load(uri)
                                                .placeholder(R.drawable.loading_image)
                                                .into(holder.messageChatListProfileIV);
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "loadImage:Failed");
                                                //show default image
                                                holder.messageChatListProfileIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                            }
                                        });
                            }
                            else{
                                //show default image
                                holder.messageChatListProfileIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                            }


                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "databaseError: "+ error.getMessage());
                }
            });
        }

        //show last message
        lastMessage(user.getId(), holder.messageChatListLastMessageTV);

    }

    //get the size of users list
    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder
    public class ViewHolder extends RecyclerView.ViewHolder{

        //image view
        public ImageView messageChatListProfileIV;

        //text view
        public TextView messageChatListProfileNameTV, messageChatListLastMessageTV;


        //constructor
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //find id for image view
            messageChatListProfileIV = itemView.findViewById(R.id.messageChatListProfileImageView);

            //find id for text view
            messageChatListProfileNameTV = itemView.findViewById(R.id.messageChatListProfileNameTextView);
            messageChatListLastMessageTV = itemView.findViewById(R.id.messageChatListLastMessage);

            //item view on click
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(onChatListener != null){
                        //get current position
                        int position = getAdapterPosition();

                        //if position != -1
                        if(position != RecyclerView.NO_POSITION){
                            onChatListener.onChatClicked(position);
                        }
                    }

                }
            });

        }
    }

    //a method that show last message of user
    public void lastMessage(final String userId, final TextView last_msg) {

        //string
        final String[] content = {""};

        //firebase user
        final FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();

        //message api
        Variable.MESSAGE_REF.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //get each message
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                    //get message object
                    Message message = dataSnapshot.getValue(Message.class);

                    if(message.getMessageReceiver().equals(cUser.getUid()) && message.getMessageSender().equals(userId) ||
                            message.getMessageReceiver().equals(userId) && message.getMessageSender().equals(cUser.getUid())) {
                        //decrypt message
                        try {
                            content[0] = AESCrypt.decrypt(message.getMessageContent());
                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                        }

                    }
                }

                //show last message
                last_msg.setText(content[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "databaseError: "+error.getMessage());
            }
        });
    }

    //create interface listener
    public interface OnChatListener{
        void onChatClicked(int position);
    }

    //create an method on click
    public void setOnClickListener(OnChatListener listener){
        this.onChatListener = listener;
    }
}
