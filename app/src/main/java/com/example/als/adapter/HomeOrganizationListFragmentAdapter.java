package com.example.als.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.als.R;
import com.example.als.object.Organization;
import com.example.als.object.Variable;
import com.example.als.ui.home.HomeUserViewDetailsActivity;
import com.example.als.ui.more.AccountActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class HomeOrganizationListFragmentAdapter extends RecyclerView.Adapter<HomeOrganizationListFragmentAdapter.ViewHolder> {

    //console tag
    private static final String TAG = "HomeOrganizationListFr";

    //create an array list for Event object
    private List<Organization> homeOrganizationList;

    //create a context for the adapter
    private Context context;

    //constructor (home event list, context)
    public HomeOrganizationListFragmentAdapter(List<Organization> homeOrganizationList, Context context) {
        this.homeOrganizationList = homeOrganizationList;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_organization_list_view_layout,parent,false);
        return new HomeOrganizationListFragmentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Organization organization = homeOrganizationList.get(position);

        //if organization profile image name not null
        if(organization.getOrganizationProfileImageName() != null){

            //go to the firebase storage reference
            StorageReference profileImageRef = Variable.ORGANIZATION_SR.child(organization.getUserId())
                    .child("profile").child(organization.getOrganizationProfileImageName());

            //get download url
            profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d(TAG, "loadProfileImage: success");
                    //push image into image view
                    Glide.with(context)
                            .load(uri)
                            .placeholder(R.drawable.loading_image)
                            .into(holder.homeOrganizationListProfileIV);
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "loadProfileImage:Failed");
                            //show loading image view
                            holder.homeOrganizationListProfileIV.setImageResource(R.drawable.loading_image);
                        }
                    });
        }
        else{
            //show loading image view
            holder.homeOrganizationListProfileIV.setImageResource(R.drawable.loading_image);
        }

        if(organization.getOrganizationName() != null){
            holder.homeOrganizationListNameTV.setText(organization.getOrganizationName());
        }
        else{
            holder.homeOrganizationListNameTV.setText("-");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
                if(cUser != null){
                    if(organization.getUserId().equals(cUser.getUid())){
                        context.startActivity(new Intent(context, AccountActivity.class));
                    }
                    else{
                        Intent i = new Intent(context, HomeUserViewDetailsActivity.class);
                        i.putExtra(Variable.HOME_USER_SESSION_ID, organization.getUserId());
                        context.startActivity(i);
                    }
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return homeOrganizationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView homeOrganizationListProfileIV;
        private TextView homeOrganizationListNameTV;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            homeOrganizationListProfileIV = itemView.findViewById(R.id.homeOrganizationProfileImageView);
            homeOrganizationListNameTV = itemView.findViewById(R.id.homeOrganizationProfileNameTextView);

        }
    }
}
