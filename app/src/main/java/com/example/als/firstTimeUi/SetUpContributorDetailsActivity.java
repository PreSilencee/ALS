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

        //initialize connectivity
        device = new Connectivity(SetUpContributorDetailsActivity.this);

        //initialize firebase auth
        cAuth = FirebaseAuth.getInstance();

        //find id for text input layout
        displayNameTIL = findViewById(R.id.accountDisplayNameTextInputLayout);
    }

    //complete button
    public void complete(View view) {
        //check the text input layout
        if(!validateDisplayName()){
            return;
        }

        //check connectivity
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            //initialize firebase user
            final FirebaseUser cUser = cAuth.getCurrentUser();

            //if user not null
            if(cUser != null){

                //get string display name
                final String displayName = displayNameTIL.getEditText().getText().toString().trim();


                Variable.CONTRIBUTOR_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Contributor contributor = snapshot.getValue(Contributor.class);

                            if(contributor != null){
                                //set display name
                                contributor.setName(displayName);

                                //create map that store all contributor values
                                Map<String, Object> contributorValues = contributor.contributorMap();

                                //update children
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
    public void onBackPressed() {
        Toasty.warning(getApplicationContext(), "Please set up the details first", Toast.LENGTH_SHORT, true).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //check connectivity
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //check connectivity
        if(!device.haveNetwork())
        {
            Toasty.error(getApplicationContext(),device.NetworkError(),Toast.LENGTH_SHORT,true).show();
        }
    }

    @Override
    protected void onStop() {
        //check connectivity
        if(!device.haveNetwork())
        {
            Toasty.error(getApplicationContext(),device.NetworkError(),Toast.LENGTH_SHORT,true).show();
        }
        super.onStop();
    }
}