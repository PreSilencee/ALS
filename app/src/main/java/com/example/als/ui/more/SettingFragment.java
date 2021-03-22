package com.example.als.ui.more;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.als.AboutUsActivity;
import com.example.als.LoginActivity;
import com.example.als.R;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class SettingFragment extends Fragment {

    //console log
    private static final String TAG = "SettingFragment";

    Button viewAccountBtn, viewDonationHistoryBtn, viewChangePasswordBtn, viewAboutUsBtn, logOutBtn;

    AccessToken accessToken;
    GoogleSignInAccount googleSignInAccount;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_setting, container, false);


        viewAccountBtn = root.findViewById(R.id.viewAccountButton);
        viewDonationHistoryBtn = root.findViewById(R.id.viewDonationHistoryButton);
        viewChangePasswordBtn = root.findViewById(R.id.viewChangePasswordButton);
        viewAboutUsBtn = root.findViewById(R.id.viewAboutUsButton);
        logOutBtn = root.findViewById(R.id.logOutButton);

        accessToken = AccessToken.getCurrentAccessToken();
        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(requireActivity());

        viewAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), AccountActivity.class));
            }
        });

        viewDonationHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), DonationHistoryActivity.class));
            }
        });

        if(accessToken != null | googleSignInAccount != null){
            viewChangePasswordBtn.setVisibility(View.GONE);
        }
        else{
            viewChangePasswordBtn.setVisibility(View.VISIBLE);
            viewChangePasswordBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(requireActivity(), ChangePasswordActivity.class));
                }
            });
        }


        viewAboutUsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), AboutUsActivity.class));
            }
        });

        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //a progress dialog to view progress of create account
                final ProgressDialog progressDialog = new ProgressDialog(requireContext());

                //set message for progress dialog
                progressDialog.setMessage("Signing Out...");

                //show dialog
                progressDialog.show();

                final FirebaseAuth cAuth = FirebaseAuth.getInstance();
                final FirebaseUser cUser = cAuth.getCurrentUser();


                if(accessToken != null){
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

                                        Variable.USER_REF.child(cUser.getUid()).setValue(userValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    LoginManager.getInstance().logOut();
                                                    cAuth.signOut();
                                                    Log.d(TAG, "signOut: success");
                                                    Intent i = new Intent(requireActivity(), LoginActivity.class);
                                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(i);
                                                    progressDialog.dismiss();
                                                    Toasty.success(requireActivity(),"Log Out Successfully!", Toast.LENGTH_SHORT).show();
                                                }
                                                else{
                                                    Log.d(TAG, "signOut: failed");
                                                    progressDialog.dismiss();
                                                    Toasty.error(requireActivity(),"Log Out Failed. Please Try Again Later",Toast.LENGTH_SHORT,true).show();
                                                }
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
                else if(googleSignInAccount != null){
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

                                        Variable.USER_REF.child(cUser.getUid()).setValue(userValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                            .requestIdToken(getString(R.string.default_web_client_id))
                                                            .requestEmail()
                                                            .build();

                                                    GoogleSignInClient cGoogleSignInClient= GoogleSignIn.getClient(requireActivity(), gso);
                                                    cGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                cAuth.signOut();
                                                                Log.d(TAG, "signOut: success");
                                                                Intent i = new Intent(requireActivity(), LoginActivity.class);
                                                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                startActivity(i);
                                                                progressDialog.dismiss();
                                                                Toasty.success(requireActivity(),"Log Out Successfully!", Toast.LENGTH_SHORT).show();
                                                            }
                                                            else{
                                                                Log.d(TAG, "signOut: failed");
                                                                progressDialog.dismiss();
                                                                Toasty.error(requireActivity(),"Log Out Failed. Please Try Again Later",Toast.LENGTH_SHORT,true).show();
                                                            }
                                                        }
                                                    });

                                                }
                                                else{
                                                    Log.d(TAG, "signOut: failed");
                                                    progressDialog.dismiss();
                                                    Toasty.error(requireActivity(),"Log Out Failed. Please Try Again Later",Toast.LENGTH_SHORT,true).show();
                                                }
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
                else{
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

                                        Variable.USER_REF.child(cUser.getUid()).setValue(userValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    cAuth.signOut();
                                                    Log.d(TAG, "signOut: success");
                                                    Intent i = new Intent(requireActivity(), LoginActivity.class);
                                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(i);
                                                    progressDialog.dismiss();
                                                    Toasty.success(requireActivity(),"Log Out Successfully!", Toast.LENGTH_SHORT).show();
                                                }
                                                else{
                                                    Log.d(TAG, "signOut: failed");
                                                    progressDialog.dismiss();
                                                    Toasty.error(requireActivity(),"Log Out Failed. Please Try Again Later",Toast.LENGTH_SHORT,true).show();
                                                }
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
            }
        });

        return root;
    }

}