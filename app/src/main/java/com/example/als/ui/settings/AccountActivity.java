package com.example.als.ui.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.als.LoginActivity;
import com.example.als.R;
import com.example.als.handler.Connectivity;
import com.example.als.handler.GlideApp;
import com.example.als.object.Contributor;
import com.example.als.object.Organization;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class AccountActivity extends AppCompatActivity {

    //tag for console log
    private static final String TAG = "AccountActivity";

    //permission
    private static final int PERMISSION_CODE = 1001;

    //variable connectivity
    private Connectivity device;

    //variable firebaseauth
    private FirebaseAuth cAuth;


    //variable imageview
    private ImageView accountIV;

    //variable textview
    private TextView uidTV, positionTitleTV, usernameTV, emailTV, phoneTV;

    //view
    private View accountNameV, accountPhoneV;

    //linear layout
    private LinearLayout accountNameLL, accountPhoneLL;

    //button
    private Button accountProfileBtn;

    //uri
    private Uri updateImageUri;

    //user
    private User user;

    //contributor
    private Contributor existedContributor;

    //organization
    private Organization existedOrganization;

    private static final String editProfileText = "EDIT PROFILE";

    private static final String organizationProfileText = "ORGANIZATION PROFILE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        //initialize connectivity
        device = new Connectivity(AccountActivity.this);



        //initialize image view
        accountIV = findViewById(R.id.accountImageView);

        //initialize text view
        uidTV = findViewById(R.id.uidTextView);
        positionTitleTV = findViewById(R.id.positionTitleTextView);
        usernameTV = findViewById(R.id.usernameTextView);
        emailTV = findViewById(R.id.emailTextView);
        phoneTV = findViewById(R.id.phoneTextView);
        accountNameV = findViewById(R.id.accountNameView);
        accountNameLL = findViewById(R.id.accountNameLinearLayout);
        accountPhoneV = findViewById(R.id.phoneViewLine);
        accountPhoneLL = findViewById(R.id.phoneLinearLayoutView);

        accountProfileBtn = findViewById(R.id.accountProfileButton);

        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            //initialize firebaseauth
            cAuth = FirebaseAuth.getInstance();

            final FirebaseUser cUser = cAuth.getCurrentUser();

            if(cUser != null){
                emailTV.setText(cUser.getEmail());

                Variable.USER_REF.child(cUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            user = snapshot.getValue(User.class);

                            if(user != null) {
                                String uidtext = "UID: "+cUser.getUid();
                                uidTV.setText(uidtext);

                                if(user.getRole() != null){
                                    String positionText = "POSITION: "+user.getRole();
                                    positionTitleTV.setText(positionText);
                                }
                                else{
                                    positionTitleTV.setText("-");
                                }

                                if(user.getRole().equals(Variable.CONTRIBUTOR)){
                                    accountProfileBtn.setText(editProfileText);
                                    Variable.CONTRIBUTOR_REF.child(cUser.getUid()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                Log.d(TAG, "userfoundinDatabse: success");
                                                existedContributor = snapshot.getValue(Contributor.class);

                                                if(existedContributor != null){

                                                    if(existedContributor.getProfileImageName() != null){

                                                        StorageReference imageRef = Variable.CONTRIBUTOR_SR.child(cUser.getUid())
                                                                .child("profile").child(existedContributor.getProfileImageName());

                                                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                Log.d(TAG, "loadImage: success");
                                                                GlideApp.with(getApplicationContext())
                                                                        .load(uri)
                                                                        .placeholder(R.drawable.loading_image)
                                                                        .into(accountIV);
                                                            }
                                                        })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d(TAG, "loadImage:Failed");
                                                                        accountIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                                                    }
                                                                });


                                                    }

                                                    if(existedContributor.getName() == null){
                                                        usernameTV.setText("-");
                                                    }
                                                    else{
                                                        usernameTV.setText(existedContributor.getName());
                                                    }

                                                    if(existedContributor.getPhone() == null){
                                                        phoneTV.setText("-");
                                                    }
                                                    else
                                                    {
                                                        phoneTV.setText(existedContributor.getPhone());
                                                    }
                                                }
                                            }
                                            else{
                                                Log.d(TAG, "contributorfoundinDatabse: failed");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.d(TAG, "contributor database error: " + error.getMessage());
                                        }
                                    });
                                }
                                else if(user.getRole().equals(Variable.ORGANIZATION)){
                                    accountProfileBtn.setText(organizationProfileText);
                                    Variable.ORGANIZATION_REF.child(cUser.getUid()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                Log.d(TAG, "userfoundinDatabse: success");
                                                existedOrganization = snapshot.getValue(Organization.class);

                                                if(existedOrganization != null){

                                                    if(existedOrganization.getOrganizationProfileImageName() != null){

                                                        StorageReference imageRef = Variable.ORGANIZATION_SR.child(cUser.getUid())
                                                                .child("profile").child(existedOrganization.getOrganizationProfileImageName());

                                                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                Log.d(TAG, "loadImage: success");
                                                                GlideApp.with(getApplicationContext())
                                                                        .load(uri)
                                                                        .placeholder(R.drawable.loading_image)
                                                                        .into(accountIV);
                                                            }
                                                        })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d(TAG, "loadImage:Failed");
                                                                        accountIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                                                    }
                                                                });


                                                    }

                                                    if(existedOrganization.getOrganizationName() == null){
                                                        usernameTV.setText("-");
                                                    }
                                                    else{
                                                        usernameTV.setText(existedOrganization.getOrganizationName());
                                                    }

                                                    accountNameV.setVisibility(View.GONE);
                                                    accountNameLL.setVisibility(View.GONE);
                                                    accountPhoneV.setVisibility(View.GONE);
                                                    phoneTV.setVisibility(View.GONE);
                                                    accountPhoneLL.setVisibility(View.GONE);
                                                }
                                            }
                                            else{
                                                Log.d(TAG, "contributorfoundinDatabse: failed");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.d(TAG, "contributor database error: " + error.getMessage());
                                        }
                                    });
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "user database error: " + error.getMessage());
                    }
                });
            }

            accountIV.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    configureImage();
                    return true;
                }
            });

            accountProfileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(user.getRole().equals(Variable.CONTRIBUTOR)){
                        openEditDialog();
                    }
                    else if(user.getRole().equals(Variable.ORGANIZATION)){
                        startActivity(new Intent(AccountActivity.this, OrganizationProfileActivity.class));
                    }
                }
            });



        }
    }

    private void configureImage(){
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
                        .start(this);
            }
        }
        else{
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
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
            FirebaseUser cUser = cAuth.getCurrentUser();

            //if user != null
            if(cUser != null)
            {
                //show success message to console log
                Log.d(TAG, "getCurrentUser: success");
            }
            else
            {
                //show error message to console log
                Log.d(TAG, "getCurrentUser: failed");

                //show error message to user
                Toasty.error(getApplicationContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

                //intent user to login page (relogin)
                Intent i = new Intent(AccountActivity.this, LoginActivity.class);

                //clear the background task
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
    }

    @Override
    protected void onStop() {
        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        super.onStop();
    }

    private void openEditDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
        LayoutInflater inflater = getLayoutInflater();

        View setUpProfileDialog = inflater.inflate(R.layout.set_up_profile_dialog, null);

        final TextInputLayout updateDisplayNameTIL = setUpProfileDialog.findViewById(R.id.updateDisplayName);
        final TextInputLayout updateDisplayPhoneTIL = setUpProfileDialog.findViewById(R.id.updatePhone);
        Button confirmProfileBtn = setUpProfileDialog.findViewById(R.id.confirmProfile);
        Button cancelProfileBtn = setUpProfileDialog.findViewById(R.id.cancelProfile);

        if(existedContributor.getName() != null){
            updateDisplayNameTIL.getEditText().setText(existedContributor.getName());
        }

        if(existedContributor.getPhone() != null){
            updateDisplayPhoneTIL.getEditText().setText(existedContributor.getPhone());
        }

        builder.setView(setUpProfileDialog)
                .setTitle("Update Profile")
                .setCancelable(false);

        final AlertDialog updateProfileDialog = builder.create();
        updateProfileDialog.show();


        confirmProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog progressDialog = new ProgressDialog(AccountActivity.this);
                progressDialog.setTitle("Updating...");
                progressDialog.show();

                String updateDisplayName = updateDisplayNameTIL.getEditText().getText().toString().trim();
                String updatePhone = updateDisplayPhoneTIL.getEditText().getText().toString().trim();

                FirebaseUser cUser = cAuth.getCurrentUser();

                boolean displayNameStatus = false;
                boolean phoneStatus = false;

                if(cUser != null){
                    if(!updateDisplayName.isEmpty() && !existedContributor.getName().equals(updateDisplayName)){
                        existedContributor.setName(updateDisplayName);
                        displayNameStatus = true;
                    }

                    if(existedContributor.getPhone() != null){
                        if(!updatePhone.isEmpty() && !existedContributor.getPhone().equals(updatePhone)){
                            existedContributor.setPhone(updatePhone);
                            phoneStatus = true;
                        }
                    }
                    else if(!updatePhone.isEmpty()){
                        existedContributor.setPhone(updatePhone);
                        phoneStatus = true;
                    }
                    Log.d(TAG, "displayNameStatus: "+displayNameStatus);
                    Log.d(TAG, "displayPhoneStatus: "+phoneStatus);
                    if(displayNameStatus || phoneStatus){
                        Map<String, Object> contributorValues = existedContributor.toMap();
                        Variable.CONTRIBUTOR_REF.child(cUser.getUid()).setValue(contributorValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "updateProfile: success");
                                Toasty.success(AccountActivity.this, "Update Successfully", Toast.LENGTH_SHORT,true).show();
                                progressDialog.dismiss();
                                updateProfileDialog.dismiss();
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toasty.error(AccountActivity.this, "Update Failed. Please Try Again", Toast.LENGTH_SHORT,true).show();
                                        Log.d(TAG, "updateProfile: failed");
                                        progressDialog.dismiss();
                                    }
                                });
                    }
                    else
                    {
                        Toasty.error(AccountActivity.this, "Nothing to update. Please check carefully.",Toast.LENGTH_SHORT,true).show();
                        progressDialog.dismiss();
                    }


                }

            }
        });

        cancelProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfileDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK && result.getUri() != null) {

                AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
                LayoutInflater inflater = getLayoutInflater();

                View setUpImageDialog = inflater.inflate(R.layout.set_up_image_dialog, null);

                ImageView updateIV = setUpImageDialog.findViewById(R.id.updateImageView);
                Button confirmImageBtn = setUpImageDialog.findViewById(R.id.confirmImage);
                Button cancelImageBtn = setUpImageDialog.findViewById(R.id.cancelImage);

                builder.setView(setUpImageDialog)
                        .setTitle("Update Image")
                        .setCancelable(false);

                final AlertDialog updateImageDialog = builder.create();
                updateImageDialog.show();

                updateImageUri = result.getUri();
                updateIV.setImageURI(updateImageUri);

                updateIV.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        updateImageDialog.dismiss();
                        configureImage();
                        return true;
                    }
                });

                confirmImageBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(updateImageUri != null){
                            final ProgressDialog progressDialog = new ProgressDialog(AccountActivity.this);
                            progressDialog.setTitle("Uploading...");
                            progressDialog.show();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.US);
                            Date dateObj = Calendar.getInstance().getTime();
                            final String currentDateTime = simpleDateFormat.format(dateObj);
                            String extension = updateImageUri.toString().substring(updateImageUri.toString().lastIndexOf("."));
                            final String profileImageName = "profileImage"+currentDateTime+extension;

                            final FirebaseUser cUser = cAuth.getCurrentUser();

                            if(cUser != null){

                                Variable.USER_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            User user = snapshot.getValue(User.class);

                                            if(user != null){
                                                if(user.getRole().equals(Variable.CONTRIBUTOR)){
                                                    final StorageReference profileImageSTR = Variable.CONTRIBUTOR_SR.child(cUser.getUid())
                                                            .child("profile").child(profileImageName);
                                                    profileImageSTR.putFile(updateImageUri)
                                                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                    Log.d(TAG, "uploadImage: success");
                                                                    existedContributor.setProfileImageName(profileImageName);
                                                                    Map<String, Object> contributorValues = existedContributor.toMap();
                                                                    Variable.CONTRIBUTOR_REF.child(cUser.getUid()).setValue(contributorValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Log.d(TAG, "saveImageNameToDatabase: success");
                                                                            Toasty.success(AccountActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT,true).show();
                                                                            progressDialog.dismiss();
                                                                            updateImageDialog.dismiss();
                                                                        }
                                                                    })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Log.d(TAG, "saveImageNameToDatabase: failed");
                                                                                    progressDialog.dismiss();
                                                                                }
                                                                            });
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d(TAG, "uploadImage: failed");
                                                                    progressDialog.dismiss();
                                                                    Toasty.error(AccountActivity.this,
                                                                            "Upload Image Failed. Please Try Again",
                                                                            Toast.LENGTH_SHORT,
                                                                            true).show();
                                                                }
                                                            })
                                                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                                                    Log.d(TAG, "uploadImage: processing");
                                                                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                                                    progressDialog.setMessage("Uploading " + (int)progress+"%");
                                                                }
                                                            });
                                                }
                                                else if(user.getRole().equals(Variable.ORGANIZATION)){
                                                    final StorageReference profileImageSTR = Variable.ORGANIZATION_SR.child(cUser.getUid()).child("profile")
                                                            .child(profileImageName);
                                                    profileImageSTR.putFile(updateImageUri)
                                                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                    Log.d(TAG, "uploadImage: success");
                                                                    existedOrganization.setOrganizationProfileImageName(profileImageName);
                                                                    Map<String, Object> organizationValues = existedOrganization.toMap();
                                                                    Variable.ORGANIZATION_REF.child(cUser.getUid()).setValue(organizationValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Log.d(TAG, "saveImageNameToDatabase: success");
                                                                            Toasty.success(AccountActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT,true).show();
                                                                            progressDialog.dismiss();
                                                                            updateImageDialog.dismiss();
                                                                        }
                                                                    })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Log.d(TAG, "saveImageNameToDatabase: failed");
                                                                                    progressDialog.dismiss();
                                                                                }
                                                                            });
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d(TAG, "uploadImage: failed");
                                                                    progressDialog.dismiss();
                                                                    Toasty.error(AccountActivity.this,
                                                                            "Upload Image Failed. Please Try Again",
                                                                            Toast.LENGTH_SHORT,
                                                                            true).show();
                                                                }
                                                            })
                                                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                                                    Log.d(TAG, "uploadImage: processing");
                                                                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                                                    progressDialog.setMessage("Uploading " + (int)progress+"%");
                                                                }
                                                            });
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.d(TAG, "databaseerror:" + error.getMessage());
                                    }
                                });


                            }
                            else{
                                Toasty.error(AccountActivity.this, "User not found", Toast.LENGTH_SHORT,true).show();
                                progressDialog.dismiss();
                            }
                        }
                        else{
                            Toasty.error(AccountActivity.this, "No Image has found",Toast.LENGTH_SHORT,true).show();
                        }
                    }
                });

                cancelImageBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateImageDialog.dismiss();
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, String.valueOf(error));
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