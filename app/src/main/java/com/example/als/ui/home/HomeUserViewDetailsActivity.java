package com.example.als.ui.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.als.LoginActivity;
import com.example.als.MainActivity;
import com.example.als.R;
import com.example.als.handler.Connectivity;
import com.example.als.handler.GlideApp;
import com.example.als.notification.Token;
import com.example.als.object.Contributor;
import com.example.als.object.Follow;
import com.example.als.object.Organization;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.example.als.ui.SearchActivity;
import com.example.als.ui.message.MessageChatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class HomeUserViewDetailsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    //tag for console log
    private static final String TAG = "HomeUserViewDetailsAct";

    //connectivity
    private Connectivity device;

    //firebase auth variable
    private FirebaseUser cUser;

    //session id
    private String homeUserSessionId;

    //imageView
    private ImageView homeUserViewDetailsIV;

    //textView
    private TextView homeUserViewDetailsNameTV, homeUserViewDetailsPositionTitleTV,
            homeUserViewDetailsOrganizationTypeTV, homeUserViewDetailsOrganizationRegistrationNumberTV,
            homeUserViewDetailsEmailTV, homeUserViewDetailsPhoneTV, homeUserViewDetailsOrganizationDescriptionTV,
            homeUserViewDetailsOrganizationAddressTV;

    //linear layout
    private LinearLayout homeUserViewDetailsOrganizationTypeLL, homeUserViewDetailsOrganizationRegistrationNumberLL,
            homeUserViewDetailsOrganizationDescriptionLL, homeUserViewDetailsOrganizationAddressLL;

    //view
    private View homeUserViewDetailsOrganizationTypeV, homeUserViewDetailsOrganizationRegistrationNumberV,
            homeUserViewDetailsOrganizationDescriptionV, homeUserViewDetailsOrganizationAddressV;

    //button
    Button becameFriendSendMessageBtn, noFriendLayoutAddFriendBtn, followLayoutBtn;

    //image button
    ImageButton becameFriendImageBtn, noFriendLayoutSendMessageImageBtn, followLayoutSendMessageImageBtn;

    //linear layout
    LinearLayout becameFriendLayout, noFriendLayout, followLayout;

    Toolbar homeCustomizeSearchViewToolbar;
    Button homeCustomizeSearchBtn;

    SwipeRefreshLayout homeUserViewSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user_view_details);

        //initialize connectivity
        device = new Connectivity(HomeUserViewDetailsActivity.this);

        homeCustomizeSearchViewToolbar = findViewById(R.id.customizeHomeUserToolbar);
        homeCustomizeSearchBtn = findViewById(R.id.customizeHomeUserSearchButton);

        setSupportActionBar(homeCustomizeSearchViewToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        homeCustomizeSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeUserViewDetailsActivity.this, SearchActivity.class));
            }
        });

        //find id for image view
        homeUserViewDetailsIV = findViewById(R.id.homeUserViewDetailsImageView);

        becameFriendLayout = findViewById(R.id.becameFriendLayout);
        noFriendLayout = findViewById(R.id.noFriendLayout);
        followLayout = findViewById(R.id.followLayout);

        becameFriendSendMessageBtn = findViewById(R.id.becameFriendSendMessageButton);
        noFriendLayoutAddFriendBtn = findViewById(R.id.noFriendLayoutAddFriendButton);
        followLayoutBtn = findViewById(R.id.followButton);
        becameFriendImageBtn = findViewById(R.id.becameFriendImageButton);
        noFriendLayoutSendMessageImageBtn = findViewById(R.id.noFriendLayoutSendMessageImageButton);
        followLayoutSendMessageImageBtn = findViewById(R.id.followLayoutSendMessageImageButton);

        //find id for text view
        homeUserViewDetailsNameTV = findViewById(R.id.homeUserViewDetailsNameTextView);
        homeUserViewDetailsPositionTitleTV = findViewById(R.id.homeUserViewDetailsPositionTitleTextView);
        homeUserViewDetailsOrganizationTypeTV = findViewById(R.id.homeUserViewOrganizationTypeTextView);
        homeUserViewDetailsOrganizationRegistrationNumberTV = findViewById(R.id.homeUserViewOrganizationRegistrationNumberTextView);
        homeUserViewDetailsEmailTV = findViewById(R.id.homeUserViewEmailTextView);
        homeUserViewDetailsPhoneTV = findViewById(R.id.homeUserViewPhoneTextView);
        homeUserViewDetailsOrganizationDescriptionTV = findViewById(R.id.homeUserViewOrganizationDescriptionTextView);
        homeUserViewDetailsOrganizationAddressTV = findViewById(R.id.homeUserViewOrganizationAddressTextView);
        homeUserViewDetailsNameTV = findViewById(R.id.homeUserViewDetailsNameTextView);
        homeUserViewDetailsPositionTitleTV = findViewById(R.id.homeUserViewDetailsPositionTitleTextView);

        //find id for linear layout
        homeUserViewDetailsOrganizationTypeLL = findViewById(R.id.homeUserViewDetailsOrganizationTypeLinearLayout);
        homeUserViewDetailsOrganizationRegistrationNumberLL = findViewById(R.id.homeUserViewDetailsOrganizationRegistrationNumberLinearLayout);
        homeUserViewDetailsOrganizationDescriptionLL = findViewById(R.id.homeUserViewDetailsOrganizationDescriptionLinearLayout);
        homeUserViewDetailsOrganizationAddressLL = findViewById(R.id.homeUserViewDetailsOrganizationAddressLinearLayout);

        //find id for view
        homeUserViewDetailsOrganizationTypeV = findViewById(R.id.homeUserViewDetailsOrganizationTypeView);
        homeUserViewDetailsOrganizationRegistrationNumberV = findViewById(R.id.homeUserViewDetailsOrganizationRegistrationNumberView);
        homeUserViewDetailsOrganizationDescriptionV = findViewById(R.id.homeUserViewDetailsOrganizationDescriptionView);
        homeUserViewDetailsOrganizationAddressV = findViewById(R.id.homeUserViewDetailsOrganizationAddressView);

        //swipeRefreshLayout function
        homeUserViewSwipeRefreshLayout = findViewById(R.id.homeUserViewSwipeRefreshLayout);
        homeUserViewSwipeRefreshLayout.setOnRefreshListener(this);
        homeUserViewSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        homeUserViewSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                homeUserViewSwipeRefreshLayout.setRefreshing(true);
                loadHomeUserDetails();
            }
        });

        //homeUserSendMessageBtn = findViewById(R.id.homeUserSendMessageButton);

        //check connectivity
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(), device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            initialize();
        }
    }

    private void loadHomeUserDetails(){
        initialize();
    }

    private void follow(String userId, String followId){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
        Date dateObj = Calendar.getInstance().getTime();
        final String currentDateTime = simpleDateFormat.format(dateObj);
        Follow follow = new Follow();
        follow.setFollowId(followId);
        follow.setFollowDateTime(currentDateTime);

        Variable.FOLLOW_REF.child(userId).child(followId).setValue(follow).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Variable.FOLLOW_REF.child(cUser.getUid()).child(homeUserSessionId).addListenerForSingleValueEvent(followValueEventListener);
                    Toasty.success(getApplicationContext(), "Follow Successfully", Toast.LENGTH_LONG).show();
                }
                else{
                    Toasty.success(getApplicationContext(), "Follow Failed. Please Try Again", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void unfollow(final String userId, final String followId){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to unfollow this organization?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Variable.FOLLOW_REF.child(userId).child(followId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    snapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Variable.FOLLOW_REF.child(cUser.getUid()).child(homeUserSessionId).addListenerForSingleValueEvent(followValueEventListener);
                                            Toasty.success(getApplicationContext(), "Unfollow Successfully", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                                else{
                                    Toasty.success(getApplicationContext(), "Something went wrong. Please try again", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "databaseError: "+error.getMessage());
                            }
                        });
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle("Unfollow");
        alertDialog.show();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        //if device no network
        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            //get current user
            cUser = FirebaseAuth.getInstance().getCurrentUser();

            //if user != null
            if(cUser == null)
            {
                //show error message to console log
                Log.d(TAG, "getCurrentUser: failed");

                //show error message to user
                Toasty.error(getApplicationContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

                //intent user to login page (relogin)
                Intent i = new Intent(HomeUserViewDetailsActivity.this, LoginActivity.class);

                //clear the background task
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
            else{
                loadHomeUserDetails();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if device no network
        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            //get current user
            cUser = FirebaseAuth.getInstance().getCurrentUser();

            //if user != null
            if(cUser == null)
            {
                //show error message to console log
                Log.d(TAG, "getCurrentUser: failed");

                //show error message to user
                Toasty.error(getApplicationContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

                //intent user to login page (relogin)
                Intent i = new Intent(HomeUserViewDetailsActivity.this, LoginActivity.class);

                //clear the background task
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
            else{
                loadHomeUserDetails();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    //initialize the view
    private void initialize(){

        //get session
        Intent session = getIntent();

        //check session id
        if(session.hasExtra(Variable.HOME_USER_SESSION_ID)){
            homeUserSessionId = session.getStringExtra(Variable.HOME_USER_SESSION_ID);
        }
        else{
            Toasty.warning(getApplicationContext(), "Something went wrong. Please Try Again", Toast.LENGTH_SHORT,true).show();
            finish();
        }

        //get current user
        cUser = FirebaseAuth.getInstance().getCurrentUser();

        //if user not null
        if(cUser != null){

            if(homeUserSessionId != null){

                Variable.USER_REF.child(homeUserSessionId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            User user = snapshot.getValue(User.class);

                            if(user != null){
                                if(user.getRole().equals(Variable.CONTRIBUTOR)){
                                    becameFriendLayout.setVisibility(View.VISIBLE);
                                    //set visibility to gone for unnecessary view
                                    homeUserViewDetailsOrganizationTypeLL.setVisibility(View.GONE);
                                    homeUserViewDetailsOrganizationRegistrationNumberLL.setVisibility(View.GONE);
                                    homeUserViewDetailsOrganizationDescriptionLL.setVisibility(View.GONE);
                                    homeUserViewDetailsOrganizationAddressLL.setVisibility(View.GONE);
                                    homeUserViewDetailsOrganizationTypeV.setVisibility(View.GONE);
                                    homeUserViewDetailsOrganizationRegistrationNumberV.setVisibility(View.GONE);
                                    homeUserViewDetailsOrganizationDescriptionV.setVisibility(View.GONE);
                                    homeUserViewDetailsOrganizationAddressV.setVisibility(View.GONE);
                                    Variable.CONTRIBUTOR_REF.child(homeUserSessionId).addListenerForSingleValueEvent(contributorValueEventListener);
                                    homeUserViewSwipeRefreshLayout.setRefreshing(false);
                                }
                                else{
                                    Variable.FOLLOW_REF.child(cUser.getUid()).child(homeUserSessionId).addListenerForSingleValueEvent(followValueEventListener);
                                    Variable.ORGANIZATION_REF.child(homeUserSessionId).addListenerForSingleValueEvent(organizationValueEventListener);
                                    followLayout.setVisibility(View.VISIBLE);
                                    followLayoutBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if(followLayoutBtn.getText().equals("Follow")){
                                                follow(cUser.getUid(), homeUserSessionId);
                                            }
                                            else if(followLayoutBtn.getText().equals("Unfollow")){
                                                unfollow(cUser.getUid(), homeUserSessionId);
                                            }
                                        }
                                    });

                                    followLayoutSendMessageImageBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent i = new Intent(HomeUserViewDetailsActivity.this, MessageChatActivity.class);
                                            i.putExtra(Variable.MESSAGE_USER_SESSION_ID, homeUserSessionId);
                                            startActivity(i);
                                        }
                                    });

                                    homeUserViewSwipeRefreshLayout.setRefreshing(false);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "databaseError: "+error.getMessage());
                    }
                });

            }
        }
        else{
            //show error message to console log
            Log.d(TAG, "getCurrentUser: failed");

            //show error message to user
            Toasty.error(getApplicationContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

            //intent user to login page (relogin)
            Intent i = new Intent(HomeUserViewDetailsActivity.this, LoginActivity.class);

            //clear the background task
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }

    private final ValueEventListener contributorValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.exists()){
                //set position to the view
                homeUserViewDetailsPositionTitleTV.setText(Variable.CONTRIBUTOR);
                Contributor contributor = snapshot.getValue(Contributor.class);

                if(contributor != null){

                    if(contributor.getName() != null){
                        homeUserViewDetailsNameTV.setText(contributor.getName());
                    }
                    else{
                        homeUserViewDetailsNameTV.setText("-");
                    }

                    if(contributor.getEmail() != null){
                        homeUserViewDetailsEmailTV.setText(contributor.getEmail());
                    }
                    else{
                        homeUserViewDetailsEmailTV.setText("-");
                    }



                    if(contributor.getPhone() != null){
                        homeUserViewDetailsPhoneTV.setText(contributor.getPhone());
                    }
                    else if(contributor.getPhone().equals(""))
                    {
                        homeUserViewDetailsPhoneTV.setText("-");
                    }
                    else{
                        homeUserViewDetailsPhoneTV.setText(contributor.getPhone());
                    }

                    if(contributor.getProfileImageUrl() != null){
                        Log.d(TAG, "loadImage: success");
                        Uri photoUri = Uri.parse(contributor.getProfileImageUrl());
                        GlideApp.with(getApplicationContext())
                                .load(photoUri)
                                .placeholder(R.drawable.loading_image)
                                .into(homeUserViewDetailsIV);
                    }
                    else if(contributor.getProfileImageName() != null){
                        StorageReference imageRef = Variable.CONTRIBUTOR_SR.child(homeUserSessionId)
                                .child("profile").child(contributor.getProfileImageName());

                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.d(TAG, "loadImage: success");
                                GlideApp.with(getApplicationContext())
                                        .load(uri)
                                        .placeholder(R.drawable.loading_image)
                                        .into(homeUserViewDetailsIV);
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "loadImage:Failed");
                                        homeUserViewDetailsIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                    }
                                });

                    }
                }

            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d(TAG, "databaseError: "+error.getMessage());
        }
    };

    private final ValueEventListener followValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.exists()){
                followLayoutBtn.setText(R.string.unfollow);
                followLayoutBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_person_remove_24, 0, 0, 0);
            }
            else{
                followLayoutBtn.setText(R.string.follow);
                followLayoutBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_person_add_alt_1_24, 0, 0, 0);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d(TAG, "databaseError: "+error.getMessage());
        }
    };

    private final ValueEventListener organizationValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            homeUserViewDetailsPositionTitleTV.setText(Variable.ORGANIZATION);
            if(snapshot.exists()){
                Organization organization = snapshot.getValue(Organization.class);

                if(organization != null){

                    if(organization.isOrganizationVerifyStatus()){

                        if(organization.getOrganizationName() != null){
                            homeUserViewDetailsNameTV.setText(organization.getOrganizationName());
                            homeUserViewDetailsNameTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_circle_accent_24, 0);
                        }
                        else{
                            homeUserViewDetailsNameTV.setText("-");
                        }
                    }
                    else{
                        if(organization.getOrganizationName() != null){
                            homeUserViewDetailsNameTV.setText(organization.getOrganizationName());
                            homeUserViewDetailsNameTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_circle_24, 0);
                        }
                        else{
                            homeUserViewDetailsNameTV.setText("-");
                        }
                    }


                    if(organization.getOrganizationProfileImageName() != null){
                        StorageReference imageRef = Variable.ORGANIZATION_SR.child(homeUserSessionId)
                                .child("profile").child(organization.getOrganizationProfileImageName());

                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.d(TAG, "loadImage: success");
                                GlideApp.with(getApplicationContext())
                                        .load(uri)
                                        .placeholder(R.drawable.loading_image)
                                        .into(homeUserViewDetailsIV);
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "loadImage:Failed");
                                        homeUserViewDetailsIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                    }
                                });

                    }

                    if(organization.getOrganizationType() != null){
                        homeUserViewDetailsOrganizationTypeTV.setText(organization.getOrganizationType());
                    }
                    else{
                        homeUserViewDetailsOrganizationTypeTV.setText("-");
                    }

                    if(organization.getOrganizationRegistrationNumber() != null){
                        homeUserViewDetailsOrganizationRegistrationNumberTV.setText(organization.getOrganizationRegistrationNumber());
                    }
                    else{
                        homeUserViewDetailsOrganizationRegistrationNumberTV.setText("-");
                    }

                    if(organization.getOrganizationEmail() != null){
                        homeUserViewDetailsEmailTV.setText(organization.getOrganizationEmail());
                    }
                    else{
                        homeUserViewDetailsEmailTV.setText("-");
                    }

                    if(organization.getOrganizationPhone() != null){
                        homeUserViewDetailsPhoneTV.setText(organization.getOrganizationPhone());
                    }
                    else{
                        homeUserViewDetailsPhoneTV.setText("-");
                    }

                    if(organization.getOrganizationDescription() != null){
                        homeUserViewDetailsOrganizationDescriptionTV.setText(organization.getOrganizationDescription());
                    }
                    else{
                        homeUserViewDetailsOrganizationDescriptionTV.setText("-");
                    }

                    if(organization.getOrganizationAddress() != null){
                        homeUserViewDetailsOrganizationAddressTV.setText(organization.getOrganizationAddress());
                    }
                    else{
                        homeUserViewDetailsOrganizationAddressTV.setText("-");
                    }
                }

            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d(TAG, "databaseError: "+error.getMessage());
        }
    };



    @Override
    public void onRefresh() {
        loadHomeUserDetails();
    }
}