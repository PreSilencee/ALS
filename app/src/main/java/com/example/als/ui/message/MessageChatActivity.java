package com.example.als.ui.message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.als.CreateEventActivity;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.io.File;
import java.io.FileFilter;
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
    private static final int PERMISSION_CODE = 151;
    private static final int PERMISSION_FILE_CODE = 152;

    private ImageView messageChatIV;
    private TextView messageChatUsername;
    private EditText messageChatInputMessageET;
    private ImageButton messageChatSendMessageBtn, messageChatSendFilesBtn, messageSendDocumentBtn, messageSendImageBtn;
    private Connectivity device;
    private static String messageChatUserId;
    private CardView sendFilesCardView;

    FirebaseUser cUser;
    MessageChatItemAdapter messageChatItemAdapter;
    List<Message> messageList;
    RecyclerView messageChatRV;
    RelativeLayout messageChatRelativeLayout;

    APIService apiService;
    boolean notify = false;
    private Uri imageUri, fileUri;

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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(Variable.MESSAGE_CHANNEL_ID,
                    Variable.MESSAGE_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(Variable.MESSAGE_CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        cUser = FirebaseAuth.getInstance().getCurrentUser();
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        if(cUser != null){

            messageChatRelativeLayout = findViewById(R.id.messageChatRelativeLayout);
            messageChatIV = findViewById(R.id.messageChatProfileImageView);
            messageChatUsername = findViewById(R.id.messageChatProfileNameTextView);
            messageChatInputMessageET = findViewById(R.id.messageChatInputMessageEditText);
            messageChatSendMessageBtn = findViewById(R.id.messageChatSendMessageBtn);
            messageChatSendFilesBtn = findViewById(R.id.messageChatSendFilesBtn);
            sendFilesCardView = findViewById(R.id.sendFilesCardView);
            messageSendImageBtn = findViewById(R.id.messageSendImageButton);
            messageSendDocumentBtn =findViewById(R.id.messageSendDocumentButton);

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
                                            if(contributor.getProfileImageUrl() != null){
                                                Uri photoUri = Uri.parse(contributor.getProfileImageUrl());
                                                Log.d(TAG, "loadProfileImage: success");
                                                //push image into image view
                                                GlideApp.with(getApplicationContext())
                                                        .load(photoUri)
                                                        .placeholder(R.drawable.loading_image)
                                                        .into(messageChatIV);
                                            }
                                            else if(contributor.getProfileImageName() != null){
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
                                            else{
                                                messageChatIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                            }

                                            if(contributor.getName() != null){
                                                messageChatUsername.setText(contributor.getName());
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

            messageChatRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(sendFilesCardView.isShown()){
                        sendFilesCardView.setVisibility(View.GONE);
                    }
                }
            });

            messageChatInputMessageET.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(sendFilesCardView.isShown()){
                        sendFilesCardView.setVisibility(View.GONE);
                    }
                }
            });

            messageChatRV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(sendFilesCardView.isShown()){
                        sendFilesCardView.setVisibility(View.GONE);
                    }
                }
            });

            messageChatSendMessageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(sendFilesCardView.isShown()){
                        sendFilesCardView.setVisibility(View.GONE);
                    }
                    notify = true;
                    String myId = cUser.getUid();
                    sendMessage(myId, messageChatUserId, messageChatInputMessageET.getText().toString().trim());
                    //sendN();
                }
            });

            messageSendDocumentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(intent.createChooser(intent, "Select a file"), PERMISSION_FILE_CODE);
                }
            });

//            Animation fadeIn = new AlphaAnimation(0, 1);
//            fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
//            fadeIn.setDuration(1000);
//
//            Animation fadeOut = new AlphaAnimation(1, 0);
//            fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
//            fadeOut.setStartOffset(1000);
//            fadeOut.setDuration(1000);
//
//            AnimationSet animation = new AnimationSet(false); //change to false
//            animation.addAnimation(fadeIn);
//            animation.addAnimation(fadeOut);
//            sendFilesCardView.setAnimation(animation);

            messageChatSendFilesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(sendFilesCardView.getVisibility() == View.GONE){
                        sendFilesCardView.setVisibility(View.VISIBLE);
                    }
                    else{
                        sendFilesCardView.setVisibility(View.GONE);
                    }
                }
            });

            messageSendImageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Build.VERSION.SDK_INT >=23)
                    {
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                        {
                            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
                        }
                        else
                        {
                            CropImage.activity()
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .setAspectRatio(1,1)
                                    .start(MessageChatActivity.this);
                        }
                    }
                    else{
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(MessageChatActivity.this);
                    }
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
            message.setMessageId(id);
            message.setMessageSender(myId);
            message.setMessageReceiver(userId);
            message.setMessageType(Variable.MESSAGE_TYPE_TEXT);
            message.setMessageDateTimeSent(currentDateTime);
            message.setMessageContent(messageContent);



            Variable.MESSAGE_REF.child(message.getMessageId()).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "sendMessage: success");
                    messageChatInputMessageET.getText().clear();
                    Variable.USER_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);

                            if(user != null){
                                if(user.getRole().equals(Variable.CONTRIBUTOR)){
                                    Variable.CONTRIBUTOR_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            Contributor contributor = snapshot.getValue(Contributor.class);
                                            if(notify){
                                                sendNotification(message.getMessageReceiver(), contributor.getName(), content);
                                            }
                                            notify = false;
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.d(TAG, "databaseError: "+error.getMessage());
                                        }
                                    });

                                }
                                else{
                                    Variable.ORGANIZATION_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            Organization organization = snapshot.getValue(Organization.class);
                                            if(notify){
                                                sendNotification(message.getMessageReceiver(), organization.getOrganizationName(), content);
                                            }
                                            notify = false;
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.d(TAG, "databaseError: "+error.getMessage());
                                        }
                                    });
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, "databaseError: "+error.getMessage());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toasty.error(MessageChatActivity.this, "Send Failed. Please Try Again", Toast.LENGTH_SHORT, true).show();
                    Log.d(TAG, "sendMessage: failed," +
                            "description: "+e.toString());
                }
            });


        }
    }

    private void sendNotification(String receiver, final String username, final String message){
        Query query = Variable.TOKEN_REF.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Token token = dataSnapshot.getValue(Token.class);
                    Log.d(TAG, messageChatUserId);
                    Data data = new Data(cUser.getUid(), R.mipmap.ic_logo, username+": "+message, "New Message",
                            messageChatUserId);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200){
                                        if(response.body().success != 1){
                                            Log.d(TAG, "Notify failed!");
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.d(TAG, "Notify failed!");
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "databaseError: "+error.getMessage());
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
                    if(message.getMessageReceiver().equals(myId) && message.getMessageSender().equals(userId) ||
                            message.getMessageReceiver().equals(userId) && message.getMessageSender().equals(myId)){
                        messageList.add(message);
                    }

                    messageChatItemAdapter = new MessageChatItemAdapter(messageList, MessageChatActivity.this);
                    messageChatRV.setAdapter(messageChatItemAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "databaseError: "+error.getMessage());
            }
        });
    }

    private void sendImageMessage(final String myId, final String userId, final Uri i){
        //a progress dialog to view progress of create account
        final ProgressDialog progressDialog = new ProgressDialog(MessageChatActivity.this);

        //set message for progress dialog
        progressDialog.setMessage("Sending Image...");

        //show dialog
        progressDialog.show();

        if(myId != null && userId != null && i != null){
            //initialize pattern of date
            SimpleDateFormat imageSimpleDateFormat = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.US);
            //initialize date object
            Date imageDateObj = Calendar.getInstance().getTime();
            //initialize string for current date time use the pattern above
            ContentResolver cR = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = mime.getExtensionFromMimeType(cR.getType(i));
            final String imageDateTime = imageSimpleDateFormat.format(imageDateObj);
            final String messageImageName = "image"+imageDateTime+type;

            final StorageReference messageImageSR = Variable.MESSAGE_SR.child(messageImageName);

            messageImageSR.putFile(i).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){

                        messageImageSR.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if(uri != null){
                                    String messageContent = "";
                                    try{
                                        messageContent = AESCrypt.encrypt(messageImageName);
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
                                    message.setMessageId(id);
                                    message.setMessageSender(myId);
                                    message.setMessageReceiver(userId);
                                    message.setMessageType(Variable.MESSAGE_TYPE_IMAGE);
                                    message.setMessageDateTimeSent(currentDateTime);
                                    message.setMessageContent(messageContent);
                                    message.setMessageUrl(uri.toString());

                                    Variable.MESSAGE_REF.child(message.getMessageId()).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "sendMessage: success");
                                            messageChatInputMessageET.getText().clear();
                                            Variable.USER_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    User user = snapshot.getValue(User.class);

                                                    if(user != null){
                                                        if(user.getRole().equals(Variable.CONTRIBUTOR)){
                                                            Variable.CONTRIBUTOR_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    Contributor contributor = snapshot.getValue(Contributor.class);
                                                                    if(notify){
                                                                        sendNotification(message.getMessageReceiver(), contributor.getName(), messageImageName);
                                                                    }
                                                                    notify = false;
                                                                    progressDialog.dismiss();
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                    Log.d(TAG, "databaseError: "+error.getMessage());
                                                                }
                                                            });

                                                        }
                                                        else{
                                                            Variable.ORGANIZATION_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    Organization organization = snapshot.getValue(Organization.class);
                                                                    if(notify){
                                                                        sendNotification(message.getMessageReceiver(), organization.getOrganizationName(), messageImageName);
                                                                    }
                                                                    notify = false;
                                                                    progressDialog.dismiss();
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                    Log.d(TAG, "databaseError: "+error.getMessage());
                                                                    progressDialog.dismiss();
                                                                }
                                                            });
                                                        }
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Log.d(TAG, "databaseError: "+error.getMessage());
                                                    progressDialog.dismiss();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toasty.error(MessageChatActivity.this, "Send Failed. Please Try Again", Toast.LENGTH_SHORT, true).show();
                                            Log.d(TAG, "sendMessage: failed," +
                                                    "description: "+e.toString());
                                        }
                                    });
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Log.d(TAG, "sendImage: failed");
                                Toasty.error(getApplicationContext(), "Send image failed. Please Try Again",Toast.LENGTH_LONG).show();
                            }
                        });



                    }
                    else{
                        progressDialog.dismiss();
                        Log.d(TAG, "sendImage: failed");
                        Toasty.error(getApplicationContext(), "Send image failed. Please Try Again",Toast.LENGTH_LONG).show();
                    }
                }
            });


        }
    }

    private void sendFileMessage(final String myId, final String userId, final Uri i){
        //a progress dialog to view progress of create account
        final ProgressDialog progressDialog = new ProgressDialog(MessageChatActivity.this);

        //set message for progress dialog
        progressDialog.setMessage("Sending File...");

        //show dialog
        progressDialog.show();

        if(myId != null && userId != null && i != null){
            //initialize pattern of date
            SimpleDateFormat fileSimpleDateFormat = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.US);
            //initialize date object
            Date imageDateObj = Calendar.getInstance().getTime();
            //initialize string for current date time use the pattern above
            ContentResolver cR = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = mime.getExtensionFromMimeType(cR.getType(i));
            final String fileDateTime = fileSimpleDateFormat.format(imageDateObj);
            final String messageFileName = "file"+fileDateTime+type;

            final StorageReference messageImageSR = Variable.MESSAGE_SR.child(messageFileName);

            messageImageSR.putFile(i).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){

                        messageImageSR.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if(uri != null){
                                    String messageContent = "";
                                    try{
                                        messageContent = AESCrypt.encrypt(messageFileName);
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
                                    message.setMessageId(id);
                                    message.setMessageSender(myId);
                                    message.setMessageReceiver(userId);
                                    message.setMessageType(Variable.MESSAGE_TYPE_FILE);
                                    message.setMessageDateTimeSent(currentDateTime);
                                    message.setMessageContent(messageContent);
                                    message.setMessageUrl(uri.toString());

                                    Variable.MESSAGE_REF.child(message.getMessageId()).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "sendMessage: success");
                                            messageChatInputMessageET.getText().clear();
                                            Variable.USER_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    User user = snapshot.getValue(User.class);

                                                    if(user != null){
                                                        if(user.getRole().equals(Variable.CONTRIBUTOR)){
                                                            Variable.CONTRIBUTOR_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    Contributor contributor = snapshot.getValue(Contributor.class);
                                                                    if(notify){
                                                                        sendNotification(message.getMessageReceiver(), contributor.getName(), messageFileName);
                                                                    }
                                                                    notify = false;
                                                                    progressDialog.dismiss();
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                    Log.d(TAG, "databaseError: "+error.getMessage());
                                                                }
                                                            });

                                                        }
                                                        else{
                                                            Variable.ORGANIZATION_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    Organization organization = snapshot.getValue(Organization.class);
                                                                    if(notify){
                                                                        sendNotification(message.getMessageReceiver(), organization.getOrganizationName(), messageFileName);
                                                                    }
                                                                    notify = false;
                                                                    progressDialog.dismiss();
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                    Log.d(TAG, "databaseError: "+error.getMessage());
                                                                    progressDialog.dismiss();
                                                                }
                                                            });
                                                        }
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Log.d(TAG, "databaseError: "+error.getMessage());
                                                    progressDialog.dismiss();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toasty.error(MessageChatActivity.this, "Send Failed. Please Try Again", Toast.LENGTH_SHORT, true).show();
                                            Log.d(TAG, "sendMessage: failed," +
                                                    "description: "+e.toString());
                                        }
                                    });
                                }
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Log.d(TAG, "sendImage: failed");
                                        Toasty.error(getApplicationContext(), "Send image failed. Please Try Again",Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                    else{
                        progressDialog.dismiss();
                        Log.d(TAG, "sendImage: failed");
                        Toasty.error(getApplicationContext(), "Send image failed. Please Try Again",Toast.LENGTH_LONG).show();
                    }
                }
            });


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK && result.getUri() != null) {
                imageUri = result.getUri();
                if(imageUri != null){
                    sendImageMessage(cUser.getUid(), messageChatUserId, imageUri);
                }
                sendFilesCardView.setVisibility(View.GONE);
                //Toasty.info(getApplicationContext(), imageUri.toString(), Toast.LENGTH_SHORT).show();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, String.valueOf(error));
            }
        }
        else if (requestCode == PERMISSION_FILE_CODE){
            if(resultCode == RESULT_OK && data != null){
                fileUri = data.getData();
                if(fileUri != null){
                    //initialize pattern of date
                    ContentResolver cR = getContentResolver();
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String type = mime.getExtensionFromMimeType(cR.getType(fileUri));
                    String typeLowerCase = type.toLowerCase();
                    if(typeLowerCase.equals("png") || typeLowerCase.equals("jpg") || typeLowerCase.equals("gif") || typeLowerCase.equals("jpeg")){
                        sendImageMessage(cUser.getUid(), messageChatUserId, fileUri);
                    }
                    else{
                        sendFileMessage(cUser.getUid(), messageChatUserId, fileUri);
                    }
                }
                sendFilesCardView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "permission: granted");
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            } else {
                Log.d(TAG, "permission: denied");
            }
        }
    }
}