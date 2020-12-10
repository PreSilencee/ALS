package com.example.als.ui.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.example.als.object.Organization;
import com.example.als.object.User;
import com.example.als.object.Variable;
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

import java.util.Map;

import es.dmoral.toasty.Toasty;

public class HomeUserViewDetailsActivity extends AppCompatActivity {

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
    private Button homeUserSendMessageBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user_view_details);

        //initialize connectivity
        device = new Connectivity(HomeUserViewDetailsActivity.this);

        //check connectivity
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(), device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            initialize();
        }
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
            //find id for image view
            homeUserViewDetailsIV = findViewById(R.id.homeUserViewDetailsImageView);

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

            homeUserSendMessageBtn = findViewById(R.id.homeUserSendMessageButton);


            if(homeUserSessionId != null){

                if(cUser.getUid().equals(homeUserSessionId)){
                    homeUserSendMessageBtn.setVisibility(View.GONE);
                }

                Variable.USER_REF.child(homeUserSessionId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            User user = snapshot.getValue(User.class);

                            if(user != null){
                                if(user.getRole().equals(Variable.CONTRIBUTOR)){
                                    //set visibility to gone for unnecessary view
                                    homeUserViewDetailsOrganizationTypeLL.setVisibility(View.GONE);
                                    homeUserViewDetailsOrganizationRegistrationNumberLL.setVisibility(View.GONE);
                                    homeUserViewDetailsOrganizationDescriptionLL.setVisibility(View.GONE);
                                    homeUserViewDetailsOrganizationAddressLL.setVisibility(View.GONE);
                                    homeUserViewDetailsOrganizationTypeV.setVisibility(View.GONE);
                                    homeUserViewDetailsOrganizationRegistrationNumberV.setVisibility(View.GONE);
                                    homeUserViewDetailsOrganizationDescriptionV.setVisibility(View.GONE);
                                    homeUserViewDetailsOrganizationAddressV.setVisibility(View.GONE);

                                    //set position to the view
                                    homeUserViewDetailsPositionTitleTV.setText(Variable.CONTRIBUTOR);
                                    Variable.CONTRIBUTOR_REF.child(homeUserSessionId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
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
                                                    else{
                                                        homeUserViewDetailsPhoneTV.setText("-");
                                                    }



                                                    if(contributor.getProfileImageName() != null){
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
                                    });
                                }
                                else{
                                    Variable.ORGANIZATION_REF.child(homeUserSessionId).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                    });
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

        homeUserSendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeUserViewDetailsActivity.this, MessageChatActivity.class);
                i.putExtra(Variable.MESSAGE_USER_SESSION_ID, homeUserSessionId);
                startActivity(i);
            }
        });
    }

}