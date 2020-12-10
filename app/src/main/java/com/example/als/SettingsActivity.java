package com.example.als;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.als.handler.Connectivity;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.example.als.ui.settings.AccountActivity;
import com.example.als.ui.settings.ChangePasswordActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class SettingsActivity extends AppCompatActivity {

    //console log
    private static final String TAG = "SettingsActivity";
    private Connectivity device;
    private FirebaseAuth cAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //initialize connectivity
        device = new Connectivity(SettingsActivity.this);

        //initialize authentication
        cAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!device.haveNetwork())
        {
            Toasty.error(SettingsActivity.this,device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!device.haveNetwork())
        {
            Toasty.error(SettingsActivity.this,device.NetworkError(),Toast.LENGTH_SHORT,true).show();
        }
    }

    @Override
    protected void onStop() {
        if(!device.haveNetwork())
        {
            Toasty.error(SettingsActivity.this,device.NetworkError(),Toast.LENGTH_SHORT,true).show();
        }
        super.onStop();
    }

    //log out button
    public void logOutButton(View view) {
        AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(SettingsActivity.this);
        alertDialogBuider.setMessage("Are you sure want to log out?")
                .setCancelable(false)
                .setPositiveButton("LOG OUT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //delay 0.3 sec
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //a progress dialog to view progress of create account
                                final ProgressDialog progressDialog = new ProgressDialog(SettingsActivity.this);

                                //set message for progress dialog
                                progressDialog.setMessage("Signing Out... " +
                                        "Please wait awhile, we are processing");

                                //show dialog
                                progressDialog.show();

                                final FirebaseUser cUser = cAuth.getCurrentUser();

                                if(cUser != null){
                                    Variable.USER_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                User user = snapshot.getValue(User.class);

                                                if(user != null){
                                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
                                                    Date dateObj = Calendar.getInstance().getTime();
                                                    final String currentDateTime = simpleDateFormat.format(dateObj);
                                                    user.setLoggedOutDateTime(currentDateTime);

                                                    Map<String, Object> userValues = user.userMap();

                                                    Variable.USER_REF.child(cUser.getUid()).setValue(userValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            cAuth.signOut();
                                                            Log.d(TAG, "signOut: success");
                                                            Intent i = new Intent(SettingsActivity.this, LoginActivity.class);
                                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(i);
                                                            progressDialog.dismiss();
                                                            Toasty.success(SettingsActivity.this,"Log Out Successfully!",Toast.LENGTH_SHORT,true).show();
                                                        }
                                                    })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d(TAG, "signOut: failed");
                                                                    progressDialog.dismiss();
                                                                    Toasty.error(SettingsActivity.this,"Log Out Failed. Please Try Again Later",Toast.LENGTH_SHORT,true).show();
                                                                }
                                                            });
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.d(TAG, "userfound: failed");
                                        }
                                    });
                                }



                            }
                        },300);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuider.create();
        alertDialog.setTitle("Logging Out");
        alertDialog.show();

    }

    public void aboutUsBtn(View view) {
        startActivity(new Intent(SettingsActivity.this, AboutUsActivity.class));
    }

    public void changePasswordBtn(View view) {
        startActivity(new Intent(SettingsActivity.this, ChangePasswordActivity.class));
    }

    public void viewAccountBtn(View view) {
        startActivity(new Intent(SettingsActivity.this, AccountActivity.class));
    }
}