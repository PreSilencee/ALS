package com.example.als.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.als.LoginActivity;
import com.example.als.R;
import com.example.als.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.dmoral.toasty.Toasty;

public class SettingFragment extends Fragment {

    //console log
    private static final String TAG = "SettingFragment";

    Button settingAccountBtn, settingChangePasswordBtn, settingAboutUsBtn, settingLogOutBtn;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_setting, container, false);

        settingAccountBtn = root.findViewById(R.id.settingAccountButton);
        settingChangePasswordBtn = root.findViewById(R.id.settingChangePasswordButton);
        settingAboutUsBtn = root.findViewById(R.id.settingAboutUsButton);
        settingLogOutBtn = root.findViewById(R.id.settingLogOutButton);

        settingAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        settingChangePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        settingAboutUsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        settingLogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //a progress dialog to view progress of create account
                final ProgressDialog progressDialog = new ProgressDialog(requireContext());

                //set message for progress dialog
                progressDialog.setMessage("Signing Out...");

                //show dialog
                progressDialog.show();

                FirebaseAuth cAuth = FirebaseAuth.getInstance();

                cAuth.signOut();
                Log.d(TAG, "signOut: success");
                Intent i = new Intent(requireActivity(), LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                progressDialog.dismiss();
                Toasty.success(requireActivity(),"Log Out Successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

}