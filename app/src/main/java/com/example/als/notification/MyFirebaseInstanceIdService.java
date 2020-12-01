package com.example.als.notification;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseInstanceId";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();

        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        if(cUser != null){
            updateToken(refreshToken);
        }
    }

    private void updateToken(String refreshToken) {
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();

        Token token = new Token(refreshToken);
        Variable.TOKEN_REF.child(cUser.getUid()).setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "Set Token Successfully");
                }
                else
                {
                    Log.d(TAG, "Set Token Failed");
                }
            }
        });
    }
}
