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

    //image view
    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_account_image);

        device = new Connectivity(SetUpAccountImageActivity.this);

        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            cAuth = FirebaseAuth.getInstance();
            profileImageView = findViewById(R.id.profileImageView);
            profileImageTitleTV = findViewById(R.id.setUpProfileImageTitle);

            FirebaseUser cUser = cAuth.getCurrentUser();

            if(cUser != null){
                Variable.USER_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Log.d(TAG, "userfoundIndatabase: failed");
                            User user = snapshot.getValue(User.class);

                            if(user != null && user.getRole().equals(Variable.ORGANIZATION)){
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

    public void skip(View view) {
        AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(SetUpAccountImageActivity.this);
        alertDialogBuider.setMessage("Are you sure want to skip? You can edit profile later in the other page")
                .setCancelable(false)
                .setPositiveButton("SKIP", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuider.create();
        alertDialog.setTitle("SKIP");
        alertDialog.show();
    }

    public void next(View view) {

        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(),Toast.LENGTH_SHORT,true).show();
        }
        else{
            if(imageUri != null){
                final FirebaseUser cUser = cAuth.getCurrentUser();

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
                            if(snapshot.exists()){
                                Log.d(TAG, "userfoundInDatabase: success");
                                User user = snapshot.getValue(User.class);

                                if(user != null){
                                    if(user.getRole().equals(Variable.CONTRIBUTOR)){
                                        final StorageReference profileImageSTR = Variable.CONTRIBUTOR_SR.child(cUser.getUid())
                                                .child("profile").child(profileImageName);
                                        profileImageSTR.putFile(imageUri)
                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        Log.d(TAG, "uploadImage: success");

                                                        Variable.CONTRIBUTOR_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if(snapshot.exists()){
                                                                    Contributor contributor = snapshot.getValue(Contributor.class);

                                                                    if(contributor != null){
                                                                        contributor.setUserId(cUser.getUid());
                                                                        contributor.setProfileImageName(profileImageName);

                                                                        Map<String, Object> contributorValues = contributor.toMap();

                                                                        Variable.CONTRIBUTOR_REF.child(cUser.getUid()).updateChildren(contributorValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                Log.d(TAG, "saveImageNameToDatabase: success");
                                                                                progressDialog.dismiss();
                                                                                finish();
                                                                                startActivity(new Intent(SetUpAccountImageActivity.this, SetUpContributorDetailsActivity.class));
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

                                    if(user.getRole().equals(Variable.ORGANIZATION)){
                                        final StorageReference profileImageSTR = Variable.ORGANIZATION_SR.child(cUser.getUid())
                                                .child("profile").child(profileImageName);
                                        profileImageSTR.putFile(imageUri)
                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        Log.d(TAG, "uploadImage: success");

                                                        Variable.ORGANIZATION_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if(snapshot.exists()){
                                                                    Organization organization = snapshot.getValue(Organization.class);

                                                                    if(organization != null){

                                                                        organization.setUserId(cUser.getUid());
                                                                        organization.setOrganizationProfileImageName(profileImageName);
                                                                        Map<String, Object> organizationValues = organization.toMap();

                                                                        Variable.ORGANIZATION_REF.child(cUser.getUid()).updateChildren(organizationValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                progressDialog.dismiss();
                                                                                finish();
                                                                                startActivity(new Intent(SetUpAccountImageActivity.this, SetUpOrganizationDetailsActivity.class));
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK && result.getUri() != null) {
                imageUri = result.getUri();
                profileImageView.setImageURI(imageUri);

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

    @Override
    protected void onStart() {
        super.onStart();

        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!device.haveNetwork())
        {
            Toasty.error(getApplicationContext(),device.NetworkError(),Toast.LENGTH_SHORT,true).show();
        }
    }

    @Override
    protected void onStop() {
        if(!device.haveNetwork())
        {
            Toasty.error(getApplicationContext(),device.NetworkError(),Toast.LENGTH_SHORT,true).show();
        }
        super.onStop();
    }
}