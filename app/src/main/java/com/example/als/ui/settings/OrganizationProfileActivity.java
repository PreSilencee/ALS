package com.example.als.ui.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.als.R;
import com.example.als.handler.Connectivity;
import com.example.als.object.Organization;
import com.example.als.object.Variable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import es.dmoral.toasty.Toasty;

public class OrganizationProfileActivity extends AppCompatActivity {

    private static final String TAG = "OrganizationProfile";

    private Connectivity device;
    private FirebaseAuth cAuth;

    private TextView organizationNameTV, organizationTypeTV, organizationRNTV,
            organizationDescriptionTV, organizationAddressTV, organizationPhoneTV, organizationVerifiedStatusTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_profile);

        //initialize connectivity
        device = new Connectivity(OrganizationProfileActivity.this);

        organizationNameTV = findViewById(R.id.organizationNameTextView);
        organizationTypeTV = findViewById(R.id.organizationTypeTextView);
        organizationRNTV = findViewById(R.id.organizationRegistrationNumberTextView);
        organizationDescriptionTV = findViewById(R.id.organizationDescriptionTextView);
        organizationAddressTV = findViewById(R.id.organizationAddressTextView);
        organizationPhoneTV = findViewById(R.id.organizationPhoneTextView);
        organizationVerifiedStatusTV = findViewById(R.id.organizationVerifiedTextView);

        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            //initialize firebaseauth
            cAuth = FirebaseAuth.getInstance();

            final FirebaseUser cUser = cAuth.getCurrentUser();

            if(cUser != null){
                Variable.ORGANIZATION_REF.child(cUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Organization organization = snapshot.getValue(Organization.class);

                            if(organization != null){
                                if(organization.getOrganizationName() != null){
                                    organizationNameTV.setText(organization.getOrganizationName());
                                }else{
                                    organizationNameTV.setText("-");
                                }

                                if(organization.getOrganizationType() != null){
                                    organizationTypeTV.setText(organization.getOrganizationType());
                                }else{
                                    organizationTypeTV.setText("-");
                                }

                                if(organization.getOrganizationRegistrationNumber() != null){
                                    organizationRNTV.setText(organization.getOrganizationRegistrationNumber());
                                }else{
                                    organizationRNTV.setText("-");
                                }

                                if(organization.getOrganizationDescription() != null){
                                    organizationDescriptionTV.setText(organization.getOrganizationDescription());
                                }else{
                                    organizationDescriptionTV.setText("-");
                                }

                                if(organization.getOrganizationAddress() != null){
                                    organizationAddressTV.setText(organization.getOrganizationAddress());
                                }else{
                                    organizationAddressTV.setText("-");
                                }

                                if(organization.getOrganizationPhone() != null){
                                    organizationPhoneTV.setText(organization.getOrganizationPhone());
                                }else{
                                    organizationPhoneTV.setText("-");
                                }

                                if(organization.isOrganizationVerifyStatus()){
                                    organizationVerifiedStatusTV.setText(Variable.VERIFIED_ORGANIZATION);
                                }else{
                                    organizationVerifiedStatusTV.setText(Variable.PENDING);
                                }

                            }
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

    @Override
    protected void onStart() {
        super.onStart();
        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
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
}