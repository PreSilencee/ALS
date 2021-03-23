package com.example.als.adapter;

import android.app.Activity;
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

import com.bumptech.glide.Glide;
import com.example.als.R;
import com.example.als.handler.GlideApp;
import com.example.als.object.Contributor;
import com.example.als.object.Follow;
import com.example.als.object.Organization;
import com.example.als.object.Variable;
import com.example.als.ui.message.MessageChatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class MessageSearchUserListAdapter extends RecyclerView.Adapter<MessageSearchUserListAdapter.ViewHolder> {

    //console tag
    private static final String TAG = "MessageSearchUser";

    //create an array list for Event object
    private List<Follow> followList;

    //create a context for the adapter
    private Context context;

    //onchatlistener
    //private OnUserListener onUserListener;
    private OnUserListener onUserListener;

    //constructor (home event list, context)
    public MessageSearchUserListAdapter(List<Follow> followList, Context context) {
        this.followList = followList;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageSearchUserListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_organization_list_view_layout, parent, false);
        return new MessageSearchUserListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageSearchUserListAdapter.ViewHolder holder, final int position) {
        final Follow follow = followList.get(position);

        if (follow.getFollowId() != null) {
            Variable.CONTRIBUTOR_REF.child(follow.getFollowId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Contributor contributor = snapshot.getValue(Contributor.class);

                        if (contributor != null) {
                            if (contributor.getName() != null) {
                                holder.homeOrganizationListNameTV.setText(contributor.getName());
                            } else {
                                holder.homeOrganizationListNameTV.setText("-");
                            }

                            if (contributor.getProfileImageUrl() != null) {
                                Log.d(TAG, "loadImage: success");
                                Uri photoUri = Uri.parse(contributor.getProfileImageUrl());
                                GlideApp.with(context)
                                        .load(photoUri)
                                        .placeholder(R.drawable.loading_image)
                                        .into(holder.homeOrganizationListProfileIV);
                            } else if (contributor.getProfileImageName() != null) {

                                StorageReference imageRef = Variable.CONTRIBUTOR_SR.child(contributor.getUserId())
                                        .child("profile").child(contributor.getProfileImageName());

                                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.d(TAG, "loadImage: success");
                                        GlideApp.with(context)
                                                .load(uri)
                                                .placeholder(R.drawable.loading_image)
                                                .into(holder.homeOrganizationListProfileIV);
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "loadImage:Failed");
                                                holder.homeOrganizationListProfileIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                            }
                                        });
                            } else {
                                holder.homeOrganizationListProfileIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                            }
                        }
                    } else {
                        Variable.ORGANIZATION_REF.child(follow.getFollowId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Organization organization = snapshot.getValue(Organization.class);

                                    if (organization != null) {
                                        if (organization.getOrganizationName() != null) {
                                            holder.homeOrganizationListNameTV.setText(organization.getOrganizationName());
                                        } else {
                                            holder.homeOrganizationListNameTV.setText("-");
                                        }

                                        //if organization profile image name not null
                                        if (organization.getOrganizationProfileImageName() != null) {

                                            //go to the firebase storage reference
                                            StorageReference profileImageRef = Variable.ORGANIZATION_SR.child(organization.getUserId())
                                                    .child("profile").child(organization.getOrganizationProfileImageName());

                                            //get download url
                                            profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    Log.d(TAG, "loadProfileImage: success");
                                                    //push image into image view
                                                    Glide.with(context)
                                                            .load(uri)
                                                            .placeholder(R.drawable.loading_image)
                                                            .into(holder.homeOrganizationListProfileIV);
                                                }
                                            })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d(TAG, "loadProfileImage:Failed");
                                                            //show loading image view
                                                            holder.homeOrganizationListProfileIV.setImageResource(R.drawable.loading_image);
                                                        }
                                                    });
                                        } else {
                                            //show loading image view
                                            holder.homeOrganizationListProfileIV.setImageResource(R.drawable.loading_image);
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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "Database Error: " + error.getMessage());
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return followList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView homeOrganizationListProfileIV;
        private TextView homeOrganizationListNameTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            homeOrganizationListProfileIV = itemView.findViewById(R.id.homeOrganizationProfileImageView);
            homeOrganizationListNameTV = itemView.findViewById(R.id.homeOrganizationProfileNameTextView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onUserListener != null){
                        //get current position
                        int position = getAdapterPosition();

                        //if position != -1
                        if(position != RecyclerView.NO_POSITION){
                            onUserListener.onUserClicked(position);
                        }
                    }
                }
            });

        }
    }

    public interface OnUserListener{
        void onUserClicked(int position);
    }

    public void setOnClickListener(OnUserListener listener){
        this.onUserListener = listener;
    }

//    //create interface listener
//    public interface OnUserListener{
//        void onUserClicked(int position);
//    }
//
//    //create an method on click
//    public void setOnClickListener(OnUserListener listener){
//        this.onUserListener = listener;
//    }
}
