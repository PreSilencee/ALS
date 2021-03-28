package com.example.als.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.als.R;
import com.example.als.handler.GlideApp;
import com.example.als.object.Contributor;
import com.example.als.object.Event;
import com.example.als.object.Follow;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class SearchContributorListFragmentAdapter extends RecyclerView.Adapter<SearchContributorListFragmentAdapter.ViewHolder> {

    //console tag
    private static final String TAG = "searchContributorList";

    //create an array list for Event object
    private List<Contributor> contributorList;

    //create a context for the adapter
    private Context context;

    //private OnUserListener onUserListener;

    //constructor (home event list, context)
    public SearchContributorListFragmentAdapter(List<Contributor> contributorList, Context context) {
        this.contributorList = contributorList;
        this.context = context;
    }

    @NonNull
    @Override
    public SearchContributorListFragmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_organization_list_view_layout, parent, false);
        return new SearchContributorListFragmentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SearchContributorListFragmentAdapter.ViewHolder holder, int position) {
        Contributor contributor = contributorList.get(position);

        //if event title not null
        if (contributor.getName() != null) {
            holder.searchContributorTV.setText(contributor.getName());
        } else {
            //set "-" as default value
            holder.searchContributorTV.setText("-");
        }

        if (contributor.getProfileImageUrl() != null) {
            Log.d(TAG, "loadImage: success");
            Uri photoUri = Uri.parse(contributor.getProfileImageUrl());
            GlideApp.with(context)
                    .load(photoUri)
                    .placeholder(R.drawable.loading_image)
                    .into(holder.searchContributorIV);
        } else if (contributor.getProfileImageName() != null) {

            StorageReference imageRef = Variable.CONTRIBUTOR_SR.child(contributor.getUserId())
                    .child("profile").child(contributor.getProfileImageName());

            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d(TAG, "loadImage: success");
                    GlideApp.with(context)
                            .load(uri)
                            .placeholder(R.drawable.loading_image)
                            .into(holder.searchContributorIV);
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "loadImage:Failed");
                            holder.searchContributorIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                        }
                    });
        } else {
            holder.searchContributorIV.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
        }
    }

    @Override
    public int getItemCount() {
        return contributorList.size();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView searchContributorIV;
        private TextView searchContributorTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            searchContributorIV = itemView.findViewById(R.id.homeOrganizationProfileImageView);
            searchContributorTV = itemView.findViewById(R.id.homeOrganizationProfileNameTextView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (onUserListener != null) {
//                        //get current position
//                        int position = getAdapterPosition();
//
//                        //if position != -1
//                        if (position != RecyclerView.NO_POSITION) {
//                            onUserListener.onUserClicked(position);
//                        }
//                    }
//                }
//            });

        }
    }
}
