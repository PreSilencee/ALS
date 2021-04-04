package com.example.als.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.als.R;
import com.example.als.handler.GlideApp;
import com.example.als.object.Event;
import com.example.als.object.Variable;
import com.example.als.ui.home.HomeUserViewDetailsActivity;
import com.example.als.ui.more.AccountActivity;
import com.example.als.ui.raised_event.RaisedEventDetailsActivity;
import com.example.als.ui.search.SearchEventDetailsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class SearchEventListFragmentAdapter extends RecyclerView.Adapter<SearchEventListFragmentAdapter.ViewHolder> {

    //console tag
    private static final String TAG = "SearchEventListFragment";

    //create an array for Event object
    private List<Event> eventList;

    //create context
    private Context context;

    //constructor for eventlist and context
    public SearchEventListFragmentAdapter(List<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    @NonNull
    @Override
    public SearchEventListFragmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_event_view_layout, parent, false);
        return new SearchEventListFragmentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SearchEventListFragmentAdapter.ViewHolder holder, int position) {
        final Event event = eventList.get(position);

        //if event title not null
        if (event.getEventTitle() != null) {
            holder.searchEventListTitleTV.setText(event.getEventTitle());
        } else {
            //set "-" as default value
            holder.searchEventListTitleTV.setText("-");
        }

        //if event description not null
        if (event.getEventDescription() != null) {
            holder.searchEventListDescriptionTV.setText(event.getEventDescription());
        } else {
            //set "-" as default value
            holder.searchEventListDescriptionTV.setText("-");
        }

        //if event image name not null
        if (event.getEventImageName() != null) {

            //define the url for image
            final StorageReference eventImageRef = Variable.EVENT_SR.child(event.getEventImageName());

            //get url and download it
            eventImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d(TAG, "loadEventImage: success");

                    //push image to image view
                    GlideApp.with(context)
                            .load(uri)
                            .placeholder(R.drawable.loading_image)
                            .into(holder.searchEventListIV);
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "loadEventImage:Failed");
                            //show loading image
                            holder.searchEventListIV.setImageResource(R.drawable.loading_image);
                        }
                    });
        } else {
            //show loading image
            holder.searchEventListIV.setImageResource(R.drawable.loading_image);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
                if(cUser != null){
                    if(event.getEventHandler().equals(cUser.getUid())){
                        Intent n = new Intent(context, RaisedEventDetailsActivity.class);
                        n.putExtra(Variable.EVENT_SESSION_ID, event.getEventId());
                        context.startActivity(n);
                    }
                    else{
                        Intent i = new Intent(context, SearchEventDetailsActivity.class);
                        i.putExtra(Variable.SEARCH_EVENT_SESSION_ID, event.getEventId());
                        context.startActivity(i);
                    }
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    //view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        //text view
        public TextView searchEventListTitleTV, searchEventListDescriptionTV;

        //progress bar
        public ImageView searchEventListIV;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //find id for text view
            searchEventListTitleTV = itemView.findViewById(R.id.searchEventListNameTextView);
            searchEventListDescriptionTV = itemView.findViewById(R.id.searchEventListDescriptionTextView);
            searchEventListIV = itemView.findViewById(R.id.searchEventListImageView);
        }
    }
}
