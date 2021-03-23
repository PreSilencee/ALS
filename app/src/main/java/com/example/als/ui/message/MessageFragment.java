package com.example.als.ui.message;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.als.R;
import com.example.als.adapter.MessageChatUserListAdapter;
import com.example.als.notification.Token;
import com.example.als.object.Message;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.example.als.ui.home.HomeUserViewDetailsActivity;
import com.example.als.widget.AlsRecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class MessageFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "MessageFragment";
    private MessageChatUserListAdapter messageChatUserListAdapter;
    private List<User> aUsers;

    private SwipeRefreshLayout messageChatListSRL;
    private AlsRecyclerView messageChatListRV;

    FirebaseUser cUser;

    private List<String> usersList;

    FloatingActionButton newMessageFAB;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_message, container, false);

        messageChatListSRL = root.findViewById(R.id.messageChatListSwipeRefreshLayout);
        messageChatListRV = root.findViewById(R.id.messageChatListRecyclerView);
        messageChatListRV.setHasFixedSize(true);
        messageChatListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        messageChatListRV.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
        View messageChatEmptyView = root.findViewById(R.id.emptyMessageChatList);
        messageChatListRV.showIfEmpty(messageChatEmptyView);
        newMessageFAB = root.findViewById(R.id.newMessageFloatingActionButton);
        cUser = FirebaseAuth.getInstance().getCurrentUser();

        newMessageFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), SearchUserMessageActivity.class));
            }
        });

        //swipeRefreshLayout function
        messageChatListSRL.setOnRefreshListener(this);
        messageChatListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        messageChatListSRL.post(new Runnable() {
            @Override
            public void run() {
                messageChatListSRL.setRefreshing(true);
                readChatList();
            }
        });

        usersList = new ArrayList<>();

        if(cUser != null){
            Variable.MESSAGE_REF.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    usersList.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Message message = dataSnapshot.getValue(Message.class);

                        if(message.getMessageSender().equals(cUser.getUid())){
                            usersList.add(message.getMessageReceiver());
                        }

                        if(message.getMessageReceiver().equals(cUser.getUid())){
                            usersList.add(message.getMessageSender());
                        }
                    }

                    readChatList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "databaseError: "+error.getMessage());
                }
            });
            updateToken(FirebaseInstanceId.getInstance().getToken());
        }

        return root;
    }

    private void updateToken(String token){
        Token token1 = new Token(token);
        Variable.TOKEN_REF.child(cUser.getUid()).setValue(token1);
    }

    private void readChatList(){
        aUsers = new ArrayList<>();

        Variable.USER_REF.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                aUsers.clear();

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);

                    for(String id: usersList){
                        if(user.getId().equals(id)){
                            if(aUsers.size() != 0){
                                for(User user1 : aUsers){
                                    if(!user.getId().equals(user1.getId())){
                                        aUsers.add(user);
                                    }
                                }
                            }
                            else{
                                aUsers.add(user);
                            }
                        }
                    }
                }

                messageChatUserListAdapter = new MessageChatUserListAdapter(aUsers,getContext());
                messageChatListRV.setAdapter(messageChatUserListAdapter);
                messageChatListSRL.setRefreshing(false);
                messageChatUserListAdapter.setOnClickListener(new MessageChatUserListAdapter.OnChatListener() {
                    @Override
                    public void onChatClicked(int position) {
                        Intent i = new Intent(requireActivity(), MessageChatActivity.class);
                        i.putExtra(Variable.MESSAGE_USER_SESSION_ID, aUsers.get(position).getId());
                        startActivity(i);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "databaseError: "+error.getMessage());
            }
        });
    }

    @Override
    public void onRefresh() {
        readChatList();
    }

}