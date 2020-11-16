package com.example.als.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.example.als.object.Payment;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.example.als.ui.home.HomeUserViewDetailsActivity;
import com.example.als.ui.message.MessageChatActivity;
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
    private static final String TAG = "MessageChatListItemAdapter";
    private List<User> userList;
    private Context context;

    private OnChatListener onChatListener;

    public MessageChatUserListAdapter(Context context, List<User> users){
        this.context = context;
        this.userList = users;
    }

    @NonNull
    @Override
    public MessageChatUserListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_chat_list_view_layout,parent,false);
        return new MessageChatUserListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageChatUserListAdapter.ViewHolder holder, int position) {
        final User user = userList.get(position);

        if(user.getRole().equals(Variable.CONTRIBUTOR)){
            Variable.CONTRIBUTOR_REF.child(user.getId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Contributor contributor = snapshot.getValue(Contributor.class);

                        if(contributor != null){

                            if(contributor.getName() != null){
                                holder.messageChatListProfileNameTV.setText(contributor.getName());
                            }
                            else{
                                holder.messageChatListProfileNameTV.setText("-");
                            }

                            if(contributor.getProfileImageName() != null) {
                                StorageReference imageRef = Variable.CONTRIBUTOR_SR.child(contributor.getUserId())
                                        .child("profile").child(contributor.getProfileImageName());

                                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.d(TAG, "loadImage: success");
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
                                                holder.messageChatListProfileIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                            }
                                        });
                            }
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            Variable.ORGANIZATION_REF.child(user.getId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Organization organization = snapshot.getValue(Organization.class);

                        if(organization != null){
                            if(organization.getOrganizationName() != null){
                                holder.messageChatListProfileNameTV.setText(organization.getOrganizationName());
                            }
                            else{
                                holder.messageChatListProfileNameTV.setText("-");
                            }

                            if(organization.getOrganizationProfileImageName() != null){
                                StorageReference imageRef = Variable.ORGANIZATION_SR.child(organization.getUserId())
                                        .child("profile").child(organization.getOrganizationProfileImageName());

                                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.d(TAG, "loadImage: success");
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
                                                holder.messageChatListProfileIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                            }
                                        });
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

        lastMessage(user.getId(), holder.messageChatListLastMessageTV);



    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView messageChatListProfileIV;
        public TextView messageChatListProfileNameTV, messageChatListLastMessageTV;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageChatListProfileIV = itemView.findViewById(R.id.messageChatListProfileImageView);
            messageChatListProfileNameTV = itemView.findViewById(R.id.messageChatListProfileNameTextView);
            messageChatListLastMessageTV = itemView.findViewById(R.id.messageChatListLastMessage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onChatListener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            onChatListener.onChatClicked(position);
                        }
                    }

                }
            });


        }
    }

    public void lastMessage(final String userId, final TextView last_msg) {
        final String[] content = {""};
        final FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        final int[] count = {0};
        Variable.MESSAGE_REF.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                    Message message = dataSnapshot.getValue(Message.class);

                    if(message.getReceiver().equals(cUser.getUid()) && message.getSender().equals(userId) ||
                            message.getSender().equals(userId) && message.getReceiver().equals(cUser.getUid())) {
                        try {
                            content[0] = AESCrypt.decrypt(message.getContent());
                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                        }

                    }
                    count[0] = count[0] + 1;
                }

                last_msg.setText(content[0]);
                Log.d(TAG, "count: "+ Arrays.toString(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "databaseError: "+error.getMessage());
            }
        });
    }

    public interface OnChatListener{
        void onChatClicked(int position);
    }

    public void setOnClickListener(OnChatListener listener){
        this.onChatListener = listener;
    }
}
