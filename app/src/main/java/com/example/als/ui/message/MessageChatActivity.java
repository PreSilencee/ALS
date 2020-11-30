package com.example.als.ui.message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.als.R;
import com.example.als.adapter.MessageChatItemAdapter;
import com.example.als.handler.AESCrypt;
import com.example.als.handler.APIService;
import com.example.als.handler.Connectivity;
import com.example.als.handler.GlideApp;
import com.example.als.notification.Client;
import com.example.als.notification.Data;
import com.example.als.notification.MyResponse;
import com.example.als.notification.Sender;
import com.example.als.notification.Token;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageChatActivity extends AppCompatActivity {


    private static final String TAG = "MessageChatAct";

    private ImageView messageChatIV;
    private TextView messageChatUsername;
    private EditText messageChatInputMessageET;
    private ImageButton messageChatSendMessageBtn;
    private Connectivity device;
    private static String messageChatUserId;

    FirebaseUser cUser;
    MessageChatItemAdapter messageChatItemAdapter;
    List<Message> messageList;
    RecyclerView messageChatRV;

    APIService apiService;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_chat);

        device = new Connectivity(MessageChatActivity.this);

        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(), device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            initialize();
        }

    }

    private void initialize(){
        Intent messageChatSession = getIntent();

        if(messageChatSession.hasExtra(Variable.MESSAGE_USER_SESSION_ID)){
            messageChatUserId = messageChatSession.getStringExtra(Variable.MESSAGE_USER_SESSION_ID);
        }
        else{
            Toasty.warning(getApplicationContext(), "Something went wrong. Please Try Again", Toast.LENGTH_SHORT,true).show();
            finish();
        }


        cUser = FirebaseAuth.getInstance().getCurrentUser();
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        if(cUser != null){

            messageChatIV = findViewById(R.id.messageChatProfileImageView);
            messageChatUsername = findViewById(R.id.messageChatProfileNameTextView);
            messageChatInputMessageET = findViewById(R.id.messageChatInputMessageEditText);
            messageChatSendMessageBtn = findViewById(R.id.messageChatSendMessageBtn);

            messageChatRV = findViewById(R.id.messageChatRecyclerView);
            messageChatRV.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setStackFromEnd(true);
            messageChatRV.setLayoutManager(linearLayoutManager);

            if(messageChatUserId != null){
                Variable.ORGANIZATION_REF.child(messageChatUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Organization organization = snapshot.getValue(Organization.class);

                            if(organization != null){

                                if(organization.getOrganizationProfileImageName() != null){
                                    StorageReference imageRef = Variable.ORGANIZATION_SR.child(messageChatUserId)
                                            .child("profile").child(organization.getOrganizationProfileImageName());

                                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Log.d(TAG, "loadImage: success");
                                            GlideApp.with(getApplicationContext())
                                                    .load(uri)
                                                    .placeholder(R.drawable.loading_image)
                                                    .into(messageChatIV);
                                        }
                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "loadImage:Failed");
                                                    messageChatIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                                }
                                            });
                                }

                                if(organization.getOrganizationName() != null){
                                    messageChatUsername.setText(organization.getOrganizationName() );
                                }
                            }
                        }
                        else{
                            Variable.CONTRIBUTOR_REF.child(messageChatUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        Contributor contributor = snapshot.getValue(Contributor.class);

                                        if(contributor != null){
                                            if(contributor.getProfileImageName() != null){
                                                StorageReference imageRef = Variable.CONTRIBUTOR_SR.child(messageChatUserId)
                                                        .child("profile").child(contributor.getProfileImageName());

                                                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        Log.d(TAG, "loadImage: success");
                                                        GlideApp.with(getApplicationContext())
                                                                .load(uri)
                                                                .placeholder(R.drawable.loading_image)
                                                                .into(messageChatIV);
                                                    }
                                                })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d(TAG, "loadImage:Failed");
                                                                messageChatIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                                            }
                                                        });
                                            }

                                            if(contributor.getName() != null){
                                                messageChatUsername.setText(contributor.getName());
                                            }
                                        }
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
                readMessage(cUser.getUid(), messageChatUserId);
            }

            messageChatInputMessageET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(s.length() != 0){
                        messageChatSendMessageBtn.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
                        messageChatSendMessageBtn.setEnabled(true);
                    }
                    else{
                        messageChatSendMessageBtn.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
                        messageChatSendMessageBtn.setEnabled(false);
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.length() != 0){
                        messageChatSendMessageBtn.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
                        messageChatSendMessageBtn.setEnabled(true);
                    }
                    else{
                        messageChatSendMessageBtn.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
                        messageChatSendMessageBtn.setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            messageChatSendMessageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notify = true;
                    String myId = cUser.getUid();
                    sendMessage(myId, messageChatUserId, messageChatInputMessageET.getText().toString().trim());
                }
            });
        }
    }

    private void sendMessage(String myId, String userId, final String content){

        if(myId != null && userId != null && content != null){
            String messageContent = "";
            try{
                messageContent = AESCrypt.encrypt(content);
            }
            catch (Exception e){
                Log.d(TAG, e.toString());
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
            Date dateObj = Calendar.getInstance().getTime();
            final String currentDateTime = simpleDateFormat.format(dateObj);
            DatabaseReference pushedMessage = Variable.MESSAGE_REF.push();

            String id = pushedMessage.getKey();

            final Message message = new Message();
            message.setId(id);
            message.setSender(myId);
            message.setReceiver(userId);
            message.setType(Variable.MESSAGE_TYPE_TEXT);
            message.setDateTimeSent(currentDateTime);
            message.setContent(messageContent);



            Variable.MESSAGE_REF.child(message.getId()).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "sendMessage: success");
                    messageChatInputMessageET.getText().clear();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toasty.error(MessageChatActivity.this, "Send Failed. Please Try Again", Toast.LENGTH_SHORT, true).show();
                    Log.d(TAG, "sendMessage: failed," +
                            "description: "+e.toString());
                }
            });

            Variable.USER_REF.child(cUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);

                    if(user != null){
                        if(user.getRole().equals(Variable.CONTRIBUTOR)){
                            Variable.CONTRIBUTOR_REF.child(cUser.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Contributor contributor = snapshot.getValue(Contributor.class);
                                    if(notify){
                                        sendNotification(message.getReceiver(), contributor.getName(), content);
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                        else{
                            Variable.ORGANIZATION_REF.child(cUser.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Organization organization = snapshot.getValue(Organization.class);
                                    if(notify){
                                        sendNotification(message.getReceiver(), organization.getOrganizationName(), content);
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }



    }

    private void sendNotification(String receiver, final String username, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(cUser.getUid(), R.mipmap.ic_launcher, username+": "+message, "New Message",
                            messageChatUserId);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200){
                                        if(response.body().success != 1){
                                            Toast.makeText(MessageChatActivity.this, "failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessage(final String myId, final String userId){
        messageList = new ArrayList<>();

        Variable.MESSAGE_REF.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Message message = dataSnapshot.getValue(Message.class);
                    assert message != null;
                    if(message.getReceiver().equals(myId) && message.getSender().equals(userId) ||
                            message.getReceiver().equals(userId) && message.getSender().equals(myId)){
                        messageList.add(message);
                    }

                    messageChatItemAdapter = new MessageChatItemAdapter(MessageChatActivity.this, messageList);
                    messageChatRV.setAdapter(messageChatItemAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "databaseError: "+error.getMessage());
            }
        });
    }
}