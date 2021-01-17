package com.example.als.firstTimeUi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.als.LoginActivity;
import com.example.als.MainActivity;
import com.example.als.R;
import com.example.als.handler.Connectivity;
import com.example.als.object.Contributor;
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

public class SetUpAccountImageActivity extends AppCompatActivity {

    //console log tag
    private static final String TAG = "SetUpProfileImage";

    //connectivity
    private Connectivity device;

    //firebase auth variable
    private FirebaseAuth cAuth;

    //permission code
    private static final int PERMISSION_CODE = 150;

    //textview
    private TextView profileImageTitleTV;

    //image url
    private Uri imageUri;

    //button
    private Button setUpAccountImageDoneBtn;

    //image view
    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_account_image);

        //initialize connectivity
        device = new Connectivity(SetUpAccountImageActivity.this);

        //check connectivity of device
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            //initialize firebase auth
            cAuth = FirebaseAuth.getInstance();

            //find id for image view
            profileImageView = findViewById(R.id.profileImageView);

            //find id for text view
            profileImageTitleTV = findViewById(R.id.setUpProfileImageTitle);

            //find id for button
            setUpAccountImageDoneBtn = findViewById(R.id.setUpProfileImageDoneButton);

            //initialize firebase user
            FirebaseUser cUser = cAuth.getCurrentUser();

            //if user not null
            if(cUser != null){
                //go to api
                Variable.USER_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //if exists
                        if(snapshot.exists()){
                            Log.d(TAG, "userfoundIndatabase: failed");

                            //get user object
                            User user = snapshot.getValue(User.class);

                            //if user not null and role is organization
                            if(user != null && user.getRole().equals(Variable.ORGANIZATION)){
                                //set new title
                                String title = "SET UP ORGANIZATION PROFILE IMAGE";
                                profileImageTitleTV.setText(title);
                            }
                        }
                        else{
                            Log.d(TAG, "userfoundIndatabase: failed");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "databaseerror: "+ error.getMessage());
                    }
                });
            }

        }
    }

    //skip method which can let user dismiss this activity
    public void skip(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {
        Toasty.warning(getApplicationContext(), "Please set up the profile image first", Toast.LENGTH_SHORT, true).show();
    }

    //next method which can let user go to next page
    public void done(View view) {

        //check connectivity
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(),Toast.LENGTH_SHORT,true).show();
        }
        else{
            //if image uri not null
            if(imageUri != null){

                //initialize firebase user
                final FirebaseUser cUser = cAuth.getCurrentUser();

                //if user not null
                if(cUser != null){

                    //initialize progress dialog
                    final ProgressDialog progressDialog = new ProgressDialog(SetUpAccountImageActivity.this);
                    //set title for progress dialog
                    progressDialog.setTitle("Uploading...");
                    //show dialog
                    progressDialog.show();

                    //initialize pattern of date
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.US);
                    //initialize date object
                    Date dateObj = Calendar.getInstance().getTime();
                    //initialize string for current date time use the pattern above
                    final String currentDateTime = simpleDateFormat.format(dateObj);

                    //get extension from image uri
                    String extension = imageUri.toString().substring(imageUri.toString().lastIndexOf("."));

                    //set image name
                    final String profileImageName = "profileImage"+currentDateTime+extension;

                    Variable.USER_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //if exists
                            if(snapshot.exists()){
                                Log.d(TAG, "userfoundInDatabase: success");

                                //get user object
                                User user = snapshot.getValue(User.class);

                                //if user not null
                                if(user != null){
                                    //if user role is contributor
                                    if(user.getRole().equals(Variable.CONTRIBUTOR)){

                                        //create new api
                                        final StorageReference profileImageSTR = Variable.CONTRIBUTOR_SR.child(cUser.getUid())
                                                .child("profile").child(profileImageName);

                                        //store the image to the api
                                        profileImageSTR.putFile(imageUri)
                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        Log.d(TAG, "uploadImage: success");
                                                        //if success, store data to database
                                                        Variable.CONTRIBUTOR_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                //if exists
                                                                if(snapshot.exists()){
                                                                    //get contributor object
                                                                    Contributor contributor = snapshot.getValue(Contributor.class);

                                                                    //if contributor object not null
                                                                    if(contributor != null){

                                                                        //set contributor profile image name
                                                                        contributor.setProfileImageName(profileImageName);

                                                                        //create an map which can store contributor values
                                                                        Map<String, Object> contributorValues = contributor.contributorMap();

                                                                        //update children
                                                                        Variable.CONTRIBUTOR_REF.child(cUser.getUid()).updateChildren(contributorValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                Log.d(TAG, "saveImageNameToDatabase: success");
                                                                                progressDialog.dismiss();
                                                                                finish();
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
                                                                    else{
                                                                        Log.d(TAG, "contributorFound: failed");
                                                                    }
                                                                }
                                                                else{
                                                                    Log.d(TAG, "invalidcontributorfound: failed");
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {
                                                                Log.d(TAG, "databaseerror: " + error.getMessage());
                                                            }
                                                        });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d(TAG, "uploadImage: failed");
                                                        progressDialog.dismiss();
                                                        Toasty.error(SetUpAccountImageActivity.this,
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
                                    else{
                                        //user role is organization
                                        //create new api
                                        final StorageReference profileImageSTR = Variable.ORGANIZATION_SR.child(cUser.getUid())
                                                .child("profile").child(profileImageName);

                                        //store image to the api
                                        profileImageSTR.putFile(imageUri)
                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        Log.d(TAG, "uploadImage: success");

                                                        Variable.ORGANIZATION_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                                //if exists
                                                                if(snapshot.exists()){

                                                                    //get organization object
                                                                    Organization organization = snapshot.getValue(Organization.class);

                                                                    //if organization not null
                                                                    if(organization != null){
                                                                        //set organization profile image name
                                                                        organization.setOrganizationProfileImageName(profileImageName);

                                                                        //create a map that can store organization values
                                                                        Map<String, Object> organizationValues = organization.organizationMap();

                                                                        //update children
                                                                        Variable.ORGANIZATION_REF.child(cUser.getUid()).updateChildren(organizationValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                progressDialog.dismiss();
                                                                                finish();
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
                                                                    else{
                                                                        Log.d(TAG, "organizationFound: failed");
                                                                    }
                                                                }
                                                                else{
                                                                    Log.d(TAG, "invalidorganizationfound: failed");
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {
                                                                Log.d(TAG, "databaseerror: " + error.getMessage());
                                                            }
                                                        });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d(TAG, "uploadImage: failed");
                                                        progressDialog.dismiss();
                                                        Toasty.error(SetUpAccountImageActivity.this,
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
                            else{
                                Log.d(TAG, "userfoundInDatabase: failed");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, "databaseerror: " + error.getMessage());
                        }
                    });
                }
            }
            else{
                Toasty.error(SetUpAccountImageActivity.this, "No Image has found",Toast.LENGTH_SHORT,true).show();
            }


        }
    }

    public void setUpImage(View view) {

        //check permission
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

    //on activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //when the request code is equal to crop image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            //get data from the crop image activity
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK && result.getUri() != null) {
                imageUri = result.getUri();

                if(imageUri != null){
                    profileImageView.setImageURI(imageUri);
                    setUpAccountImageDoneBtn.setEnabled(true);
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, String.valueOf(error));
            }
        }
    }

    //on request permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //if request code equal to permission code
        if (requestCode == PERMISSION_CODE) {
            if (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "permission: granted");
                //show crop image activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            } else {
                Log.d(TAG, "permission: denied");
            }
        }
    }

    //on start method
    @Override
    protected void onStart() {
        super.onStart();
        //check connectivity
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
    }

    //on resume method
    @Override
    protected void onResume() {
        super.onResume();
        //check connectivity
        if(!device.haveNetwork())
        {
            Toasty.error(getApplicationContext(),device.NetworkError(),Toast.LENGTH_SHORT,true).show();
        }
    }

}