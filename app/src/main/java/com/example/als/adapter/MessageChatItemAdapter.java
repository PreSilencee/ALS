package com.example.als.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.als.R;
import com.example.als.handler.AESCrypt;
import com.example.als.handler.GlideApp;
import com.example.als.object.Contributor;
import com.example.als.object.Message;
import com.example.als.object.Organization;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class MessageChatItemAdapter extends RecyclerView.Adapter<MessageChatItemAdapter.ViewHolder> implements ActivityCompat.OnRequestPermissionsResultCallback{

    //console log
    private static final String TAG = "MessageChatItemAdapter";

    private static final int PERMISSION_CODE = 1501;

    //array list for message object
    private List<Message> messageList;

    //context for adapter
    private Context context;

    //firebase user
    FirebaseUser cUser;

    //constructor(context, message);
    public MessageChatItemAdapter(List<Message> messages, Context context){
        this.messageList = messages;
        this.context = context;
    }

    //create view for each Message object
    @NonNull
    @Override
    public MessageChatItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //if view type equal to right
        if(viewType == Variable.MSG_TYPE_RIGHT){
            //show right layout
            View view = LayoutInflater.from(context).inflate(R.layout.message_chat_item_right,parent,false);
            return new MessageChatItemAdapter.ViewHolder(view);
        }
        else{
            //show left layout
            View view = LayoutInflater.from(context).inflate(R.layout.message_chat_item_left,parent,false);
            return new MessageChatItemAdapter.ViewHolder(view);
        }
    }

    //attach data to the view
    @Override
    public void onBindViewHolder(@NonNull final MessageChatItemAdapter.ViewHolder holder, final int position) {

        //get message position
        final Message message = messageList.get(position);

        if(getItemViewType(position) == Variable.MSG_TYPE_LEFT){
            if(message.getMessageSender() != null){
                Variable.CONTRIBUTOR_REF.child(message.getMessageSender()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Contributor contributor = snapshot.getValue(Contributor.class);

                            if(contributor != null){
                                if(contributor.getProfileImageUrl() != null){
                                    Log.d(TAG, "loadImage: success");
                                    Uri photoUri = Uri.parse(contributor.getProfileImageUrl());
                                    GlideApp.with(context)
                                            .load(photoUri)
                                            .placeholder(R.drawable.loading_image)
                                            .into(holder.receiverMessageImageView);
                                }
                                else if(contributor.getProfileImageName() != null){
                                    StorageReference imageRef = Variable.CONTRIBUTOR_SR.child(message.getMessageSender())
                                            .child("profile").child(contributor.getProfileImageName());

                                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Log.d(TAG, "loadImage: success");
                                            GlideApp.with(context)
                                                    .load(uri)
                                                    .placeholder(R.drawable.loading_image)
                                                    .into(holder.receiverMessageImageView);
                                        }
                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "loadImage:Failed");
                                                    holder.receiverMessageImageView.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                                }
                                            });

                                }
                            }
                        }
                        else{
                            Variable.ORGANIZATION_REF.child(message.getMessageSender()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        Organization organization = snapshot.getValue(Organization.class);

                                        if(organization != null){
                                            if(organization.getOrganizationProfileImageName() != null){

                                                StorageReference imageRef = Variable.ORGANIZATION_SR.child(message.getMessageSender())
                                                        .child("profile").child(organization.getOrganizationProfileImageName());

                                                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        Log.d(TAG, "loadImage: success");
                                                        GlideApp.with(context)
                                                                .load(uri)
                                                                .placeholder(R.drawable.loading_image)
                                                                .into(holder.receiverMessageImageView);
                                                    }
                                                })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d(TAG, "loadImage:Failed");
                                                                holder.receiverMessageImageView.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                                            }
                                                        });

                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    //
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //
                    }
                });
            }


        }

        if(message.getMessageType().equals(Variable.MESSAGE_TYPE_TEXT)){
            //decrypt message
            String decryptedMessage ="";
            try{
                decryptedMessage = AESCrypt.decrypt(message.getMessageContent());
            }
            catch (Exception e){
                Log.d(TAG, e.toString());
            }

            //set decrypted message
            holder.messageChatItem.setText(decryptedMessage);
            holder.messageChatItem.setVisibility(View.VISIBLE);
        }
        else if(message.getMessageType().equals(Variable.MESSAGE_TYPE_IMAGE)){
            Glide.with(context)
                    .load(message.getMessageUrl())
                    .placeholder(R.drawable.loading_image)
                    .into(holder.messageChatItemImageView);
            holder.messageChatItemImageView.setVisibility(View.VISIBLE);
            holder.messageChatItemImageDownloadBtn.setVisibility(View.VISIBLE);

            holder.messageChatItemImageDownloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Build.VERSION.SDK_INT >- Build.VERSION_CODES.M){
                        if(context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                            String [] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            ActivityCompat.requestPermissions((Activity) context, permissions, PERMISSION_CODE);
                        }
                        else{
                            startDownload(message.getMessageUrl());
                        }
                    }
                    else{
                        startDownload(message.getMessageUrl());
                    }

                }
            });
        }
        else if(message.getMessageType().equals(Variable.MESSAGE_TYPE_FILE)){

            holder.messageChatItemDocument.setText(message.getMessageFileName());
            holder.messageChatItemDocument.setVisibility(View.VISIBLE);
            holder.messageChatItemDocumentDownloadBtn.setVisibility(View.VISIBLE);
            holder.messageChatItemDocumentDownloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Build.VERSION.SDK_INT >- Build.VERSION_CODES.M){
                        if(context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                            String [] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            ActivityCompat.requestPermissions((Activity) context, permissions, PERMISSION_CODE);
                        }
                        else{
                            startDownload(message.getMessageUrl());
                        }
                    }
                    else{
                        startDownload(message.getMessageUrl());
                    }
                }
            });
        }
    }

    private void startDownload(String url){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("Download");
        request.setDescription("Downloading file...");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, ""+System.currentTimeMillis());

        DownloadManager manager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    //get size of message list
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toasty.success(context, "Permission Granted. Please download again", Toast.LENGTH_LONG).show();
            } else {
                Toasty.error(context, "Permission Denied.", Toast.LENGTH_LONG).show();
            }
        }
    }

    //view holder class
    public static class ViewHolder extends RecyclerView.ViewHolder{

        //text view
        public TextView messageChatItem,  messageChatItemDocument;
        public ImageView receiverMessageImageView, messageChatItemImageView,
                messageChatItemImageDownloadBtn,messageChatItemDocumentDownloadBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //find id for text view
            receiverMessageImageView = itemView.findViewById(R.id.messageChatOtherUserProfileImageView);
            messageChatItem = itemView.findViewById(R.id.messageShowChatItem);
            messageChatItemImageView = itemView.findViewById(R.id.messageShowChatItemImage);
            messageChatItemImageDownloadBtn = itemView.findViewById(R.id.messageImageDownloadButton);
            messageChatItemDocument = itemView.findViewById(R.id.messageShowChatItemDocumentName);
            messageChatItemDocumentDownloadBtn = itemView.findViewById(R.id.messageDocumentDownloadButton);
        }
    }

    //get item view type
    @Override
    public int getItemViewType(int position) {
        cUser = FirebaseAuth.getInstance().getCurrentUser();
        if(messageList.get(position).getMessageSender().equals(cUser.getUid())){
            return Variable.MSG_TYPE_RIGHT;
        }
        else{
            return Variable.MSG_TYPE_LEFT;
        }
    }
}
