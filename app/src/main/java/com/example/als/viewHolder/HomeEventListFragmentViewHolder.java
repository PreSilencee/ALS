package com.example.als.viewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.als.R;

public class HomeEventListFragmentViewHolder extends RecyclerView.ViewHolder {

    public CardView homeEventListCV;
    public LinearLayout homeEventListEventHandlerLL;
    public TextView homeEventListProfileNameTV, homeEventListEventCreatedDate, homeEventListTitleTV, homeEventListDescriptionTV, homeEventListDurationTV,
             homeEventListCurrentFundTV, homeEventListTargetFundTV;
    public ImageView homeEventListProfileIV, homeEventListIV;
    public ProgressBar homeEventListPB;
    public Button homeEventListDonateBtn;

    public HomeEventListFragmentViewHolder(View itemView){
        super(itemView);

        homeEventListCV = itemView.findViewById(R.id.homeEventListCardView);
        homeEventListEventHandlerLL = itemView.findViewById(R.id.homeEventListEventHandlerLinearLayout);
        homeEventListProfileNameTV = itemView.findViewById(R.id.homeEventListProfileNameTextView);
        homeEventListEventCreatedDate = itemView.findViewById(R.id.homeEventListEventCreatedTextView);
        homeEventListProfileIV = itemView.findViewById(R.id.homeEventListProfileImageView);
        homeEventListTitleTV = itemView.findViewById(R.id.homeEventListTitleTextView);
        homeEventListIV = itemView.findViewById(R.id.homeEventListImageView);
        homeEventListDescriptionTV = itemView.findViewById(R.id.homeEventListDescriptionTextView);
        homeEventListDurationTV = itemView.findViewById(R.id.homeEventListDurationTextView);
        homeEventListPB = itemView.findViewById(R.id.homeEventListProgressBar);
        homeEventListCurrentFundTV = itemView.findViewById(R.id.homeEventListCurrentFundTextView);
        homeEventListTargetFundTV = itemView.findViewById(R.id.homeEventListTargetFundTextView);

        homeEventListDonateBtn = itemView.findViewById(R.id.homeEventListDonateButton);

    }
}
