package com.example.als.firstTimeUi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.als.R;
import com.example.als.handler.Connectivity;
import com.example.als.object.Contributor;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import es.dmoral.toasty.Toasty;

public class SetUpContributorDetailsActivity extends AppCompatActivity {

    //tag for console log
    private static final String TAG = "SetUpContributorDAct";

    //connectivity
    private Connectivity device;

    //firebase auth variable
    private FirebaseAuth cAuth;

    //textview
    private TextInputLayout displayNameTIL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_contributor_details);

        device = new Connectivity(SetUpContributorDetailsActivity.this);

        cAuth = FirebaseAuth.getInstance();

        displayNameTIL = findViewById(R.id.accountDisplayNameTextInputLayout);
    }

    public void complete(View view) {
        if(!validateDisplayName()){
            return;
        }

        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            final FirebaseUser cUser = cAuth.getCurrentUser();
            if(cUser != null){
                final String displayName = displayNameTIL.getEditText().getText().toString().trim();

                Variable.CONTRIBUTOR_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Contributor contributor = snapshot.getValue(Contributor.class);

                            if(contributor != null){
                                contributor.setName(displayName);

                                Map<String, Object> contributorValues = contributor.toMap();

                                Variable.CONTRIBUTOR_REF.child(cUser.getUid()).updateChildren(contributorValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "setupprofile: success");
                                        Toasty.success(SetUpContributorDetailsActivity.this, "Set Up Successfully",Toast.LENGTH_SHORT,true).show();
                                        finish();
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "setupprofile: failed");
                                                Toasty.success(SetUpContributorDetailsActivity.this, "Set Up Failed",Toast.LENGTH_SHORT,true).show();
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
        }


    }

    //validate display name
    private boolean validateDisplayName() {
        //get display name
        String displayNameInput = displayNameTIL.getEditText().getText().toString().trim();

        //if display name == null
        if(displayNameInput.isEmpty()){
            displayNameTIL.setError("Field can't be empty");
            return false;
        }
        else{
            displayNameTIL.setError(null);
            return true;
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