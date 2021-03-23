package com.example.als.ui.message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.als.CreateEventActivity;
import com.example.als.LoginActivity;
import com.example.als.R;
import com.example.als.adapter.HomeOrganizationListFragmentAdapter;
import com.example.als.adapter.MessageSearchUserListAdapter;
import com.example.als.handler.Connectivity;
import com.example.als.object.Follow;
import com.example.als.object.Organization;
import com.example.als.object.Variable;
import com.example.als.widget.AlsRecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class SearchUserMessageActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "SearchUserMessage";
    private Connectivity device;
    FirebaseUser cUser;
    Toolbar customizeSearchUserMessageToolbar;

    SwipeRefreshLayout followListSRL;
    List<Follow> followList;
    MessageSearchUserListAdapter followListAdapter;
    AlsRecyclerView followListRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user_message);

        device = new Connectivity(SearchUserMessageActivity.this);

        customizeSearchUserMessageToolbar = findViewById(R.id.customizeSearchUserMessageToolbar);
        setSupportActionBar(customizeSearchUserMessageToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Select User");

        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            cUser = FirebaseAuth.getInstance().getCurrentUser();
        }

        if(cUser == null){
            //show error message to user
            Toasty.error(getApplicationContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

            //intent user to login page (relogin)
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);

            //clear the background task
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }

        followListSRL = findViewById(R.id.searchUserMessageSwipeRefreshLayout);
        View emptyFollow = findViewById(R.id.emptySearchUserMessageList);
        //recycler view
        followListRV = findViewById(R.id.searchUserMessageRecyclerView);
        followListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        followListRV.setLayoutManager(layoutManager);
        followListRV.showIfEmpty(emptyFollow);

        //swipeRefreshLayout function
        followListSRL.setOnRefreshListener(this);
        followListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        followListSRL.post(new Runnable() {
            @Override
            public void run() {
                followListSRL.setRefreshing(true);
                loadFollowList();
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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

            //if user != null
            if(cUser != null)
            {
                loadFollowList();
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
                Intent i = new Intent(SearchUserMessageActivity.this, LoginActivity.class);

                //clear the background task
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if device no network
        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{

            //if user != null
            if(cUser != null)
            {
                loadFollowList();
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
                Intent i = new Intent(SearchUserMessageActivity.this, LoginActivity.class);

                //clear the background task
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
    }

    @Override
    protected void onStop() {
        //if device no network
        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{


            //if user != null
            if(cUser != null)
            {
                Variable.FOLLOW_REF.child(cUser.getUid()).removeEventListener(followListValueEventListener);
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
                Intent i = new Intent(SearchUserMessageActivity.this, LoginActivity.class);

                //clear the background task
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
        super.onStop();

    }

    private void loadFollowList(){
        followList = new ArrayList<>();
        Variable.FOLLOW_REF.child(cUser.getUid()).addListenerForSingleValueEvent(followListValueEventListener);
    }

    @Override
    public void onRefresh() {
        loadFollowList();
    }

    private final ValueEventListener followListValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            followList.clear();

            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Follow follow = dataSnapshot.getValue(Follow.class);

                if(follow != null) {
                    followList.add(follow);
                }
            }

            followListAdapter = new MessageSearchUserListAdapter(followList, getApplicationContext());
            followListAdapter.notifyDataSetChanged();
            followListRV.setAdapter(followListAdapter);
            followListSRL.setRefreshing(false);

            followListAdapter.setOnClickListener(new MessageSearchUserListAdapter.OnUserListener() {
                @Override
                public void onUserClicked(int position) {
                    Intent i = new Intent(getApplicationContext(), MessageChatActivity.class);
                    i.putExtra(Variable.MESSAGE_USER_SESSION_ID, followList.get(position).getFollowId());
                    startActivity(i);
                    finish();
                }
            });
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("FollowList", "Database Error: " + error.getMessage());
        }
    };
}