package com.example.als.ui.raised_event;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.example.als.CreateEventActivity;
import com.example.als.R;
import com.example.als.adapter.RaisedEventListFragmentAdapter;
import com.example.als.adapter.ViewPagerAdapter;
import com.example.als.handler.Connectivity;
import com.example.als.handler.GlideApp;
import com.example.als.object.Contributor;
import com.example.als.object.Event;
import com.example.als.object.Organization;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.example.als.widget.AlsRecyclerView;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class EventFragment extends Fragment{

    private static final String TAG = "EventFragment";
    private Connectivity device;
    private FirebaseAuth cAuth;

    FirebaseUser cUser;

    ImageView eventProfileImageView;
    Button startEventBtn;
    AccessToken accessToken;
    GoogleSignInAccount googleSignInAccount;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Log.d(TAG, "running: success");
        View root = inflater.inflate(R.layout.fragment_event, container, false);

        device = new Connectivity(getContext());

        ViewPager createEventViewPager = root.findViewById(R.id.createEventViewPager);
        setupViewPager(createEventViewPager);
        TabLayout createEventTabLayout = root.findViewById(R.id.createEventTabLayout);
        createEventTabLayout.setupWithViewPager(createEventViewPager);
        eventProfileImageView = root.findViewById(R.id.eventProfileImageView);
        accessToken = AccessToken.getCurrentAccessToken();
        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(requireActivity());

        cAuth = FirebaseAuth.getInstance();
        cUser = cAuth.getCurrentUser();

        if(cUser != null){
            if(cUser.getPhotoUrl() != null){
                GlideApp.with(this)
                        .load(cUser.getPhotoUrl())
                        .placeholder(R.drawable.loading_image)
                        .into(eventProfileImageView);
            }
            else{
                Variable.USER_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            User user = snapshot.getValue(User.class);

                            if(user != null){
                                if(user.getRole().equals(Variable.CONTRIBUTOR)){
                                    Variable.CONTRIBUTOR_REF.child(user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                Contributor contributor = snapshot.getValue(Contributor.class);

                                                if(contributor != null){
                                                    if(contributor.getProfileImageName() != null){
                                                        //go to the firebase storage reference
                                                        StorageReference profileImageRef = Variable.CONTRIBUTOR_SR.child(cUser.getUid())
                                                                .child("profile").child(contributor.getProfileImageName());

                                                        //get download url
                                                        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                Log.d(TAG, "loadProfileImage: success");
                                                                //push image into image view
                                                                GlideApp.with(requireActivity())
                                                                        .load(uri)
                                                                        .placeholder(R.drawable.loading_image)
                                                                        .into(eventProfileImageView);
                                                            }
                                                        })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d(TAG, "loadProfileImage:Failed");
                                                                        //show loading image view
                                                                        eventProfileImageView.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                                                    }
                                                                });
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.d(TAG, "databaseError: " + error.getMessage());
                                        }
                                    });
                                }
                                else{
                                    Variable.ORGANIZATION_REF.child(user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                Organization organization = snapshot.getValue(Organization.class);

                                                if(organization != null){
                                                    if(organization.getOrganizationProfileImageName() != null){
                                                        //go to the firebase storage reference
                                                        StorageReference profileImageRef = Variable.ORGANIZATION_SR.child(cUser.getUid())
                                                                .child("profile").child(organization.getOrganizationProfileImageName());

                                                        //get download url
                                                        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                Log.d(TAG, "loadProfileImage: success");
                                                                //push image into image view
                                                                GlideApp.with(requireActivity())
                                                                        .load(uri)
                                                                        .placeholder(R.drawable.loading_image)
                                                                        .into(eventProfileImageView);
                                                            }
                                                        })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d(TAG, "loadProfileImage:Failed");
                                                                        //show loading image view
                                                                        eventProfileImageView.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                                                    }
                                                                });
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.d(TAG, "databaseError: " + error.getMessage());
                                        }
                                    });
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "databaseError: " + error.getMessage());
                    }
                });
            }

        }

        startEventBtn = root.findViewById(R.id.startCreateEventButton);

        startEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), CreateEventActivity.class));
            }
        });

        return root;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager(), 0);
        adapter.addFragment(new RaisedAllEventFragment(), "All");
        adapter.addFragment(new RaisedAvailableEventFragment(), "Available");
        adapter.addFragment(new RaisedUpcomingEventFragment(), "Upcoming");
        adapter.addFragment(new RaisedPendingEventFragment(), "Pending");
        adapter.addFragment(new RaisedDeclinedEventFragment(), "Declined");

        viewPager.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}