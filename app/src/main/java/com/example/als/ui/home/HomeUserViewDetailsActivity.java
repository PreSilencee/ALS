package com.example.als.ui.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.als.R;
import com.example.als.handler.Connectivity;
import com.example.als.handler.GlideApp;
import com.example.als.object.Contributor;
import com.example.als.object.Organization;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import es.dmoral.toasty.Toasty;

public class HomeUserViewDetailsActivity extends AppCompatActivity {

    //tag for console log
    private static final String TAG = "HomeUserViewDetailsAct";
    //private GoogleSignInClient cGoogleSignInClient;

    //connectivity
    private Connectivity device;

    //firebase auth variable
    private FirebaseAuth cAuth;

    //session id
    private String homeUserSessionId, homeUserSessionPosition;

    //imageView
    private ImageView homeUserViewDetailsIV;

    //textView
    private TextView homeUserViewDetailsNameTV, homeUserViewDetailsPositionTitleTV,
            homeUserViewDetailsOrganizationTypeTV, homeUserViewDetailsOrganizationRegistrationNumberTV,
            homeUserViewDetailsEmailTV, homeUserViewDetailsPhoneTV, homeuserViewDetailsOrganizationDescriptionTV,
            homeUserViewDetailsOrganizationAddressTV;

    //linear layout
    private LinearLayout homeUserDetailsOrganizationTypeLL, homeUserDetailsOrganizationRegistrationNumberLL,
            homeUserDetailsOrganizationDescriptionLL, homeUserDetailsOrganizationAddressLL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user_view_details);

        device = new Connectivity(HomeUserViewDetailsActivity.this);

        Intent session = getIntent();

        homeUserViewDetailsIV = findViewById(R.id.homeUserViewDetailsImageView);
        homeUserViewDetailsNameTV = findViewById(R.id.homeUserViewDetailsNameTextView);
        homeUserViewDetailsPositionTitleTV = findViewById(R.id.homeUserViewDetailsPositionTitleTextView);
        homeUserDetailsOrganizationTypeLL = findViewById(R.id.homeUserViewDetailsOrganizationTypeLinearLayout);
        homeUserDetailsOrganizationRegistrationNumberLL = findViewById(R.id.homeUserViewDetailsOrganizationRegistrationNumberLinearLayout);
        homeUserDetailsOrganizationDescriptionLL = findViewById(R.id.homeUserViewDetailsOrganizationDescriptionLinearLayout);
        homeUserDetailsOrganizationAddressLL = findViewById(R.id.homeUserViewDetailsOrganizationAddressLinearLayout);
        homeUserViewDetailsOrganizationTypeTV = findViewById(R.id.homeUserViewOrganizationTypeTextView);
        homeUserViewDetailsOrganizationRegistrationNumberTV = findViewById(R.id.homeUserViewOrganizationRegistrationNumberTextView);
        homeUserViewDetailsEmailTV = findViewById(R.id.homeUserViewEmailTextView);
        homeUserViewDetailsPhoneTV = findViewById(R.id.homeUserViewPhoneTextView);
        homeuserViewDetailsOrganizationDescriptionTV = findViewById(R.id.homeUserViewOrganizationDescriptionTextView);
        homeUserViewDetailsOrganizationAddressTV = findViewById(R.id.homeUserViewOrganizationAddressTextView);
        homeUserViewDetailsNameTV = findViewById(R.id.homeUserViewDetailsNameTextView);
        homeUserViewDetailsPositionTitleTV = findViewById(R.id.homeUserViewDetailsPositionTitleTextView);

        if(session.hasExtra(Variable.HOME_USER_SESSION_ID)){
            homeUserSessionId = session.getStringExtra(Variable.HOME_USER_SESSION_ID);
        }
        else{
            Toasty.warning(getApplicationContext(), "Something went wrong. Please Try Again", Toast.LENGTH_SHORT,true).show();
            finish();
        }

        if(session.hasExtra(Variable.HOME_USER_SESSION_POSITION)){
            homeUserSessionPosition = session.getStringExtra(Variable.HOME_USER_SESSION_POSITION);
        }
        else{
            Toasty.warning(getApplicationContext(), "Something went wrong. Please Try Again",Toast.LENGTH_SHORT,true).show();
            finish();
        }

        if(homeUserSessionId != null){
            if(homeUserSessionPosition != null){
                if(homeUserSessionPosition.equals(Variable.CONTRIBUTOR)){
                    homeUserDetailsOrganizationTypeLL.setVisibility(View.GONE);
                    homeUserDetailsOrganizationRegistrationNumberLL.setVisibility(View.GONE);
                    homeUserDetailsOrganizationDescriptionLL.setVisibility(View.GONE);
                    homeUserDetailsOrganizationAddressLL.setVisibility(View.GONE);
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
                else if(homeUserSessionPosition.equals(Variable.ORGANIZATION)){
                    homeUserViewDetailsPositionTitleTV.setText(Variable.ORGANIZATION);
                    Variable.ORGANIZATION_REF.child(homeUserSessionId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                                        homeuserViewDetailsOrganizationDescriptionTV.setText(organization.getOrganizationDescription());
                                    }
                                    else{
                                        homeuserViewDetailsOrganizationDescriptionTV.setText("-");
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
}