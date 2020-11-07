package com.example.als.viewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.als.R;

public class HomeEventListFragmentViewHolder extends RecyclerView.ViewHolder {
    public TextView homeEventListTitleTV, homeEventListDescriptionTV, homeEventListStartDateTV,
            homeEventListEndDateTV, homeEventListCurrentFundTV, homeEventListTargetFundTV;
    public ImageView homeEventListIV;
    public ProgressBar homeEventListPB;
    public Button homeEventListDonateBtn, homeEventListViewDetailsBtn;

    public HomeEventListFragmentViewHolder(View itemView){
        super(itemView);

        homeEventListTitleTV = itemView.findViewById(R.id.homeEventListTitleTextView);
        homeEventListIV = itemView.findViewById(R.id.homeEventListImageView);
        homeEventListDescriptionTV = itemView.findViewById(R.id.homeEventListDescriptionTextView);
        homeEventListStartDateTV = itemView.findViewById(R.id.homeEventListStartDateTextView);
        homeEventListEndDateTV = itemView.findViewById(R.id.homeEventListEndDateTextView);
        homeEventListPB = itemView.findViewById(R.id.homeEventListProgressBar);
        homeEventListCurrentFundTV = itemView.findViewById(R.id.homeEventListCurrentFundTextView);
        homeEventListTargetFundTV = itemView.findViewById(R.id.homeEventListTargetFundTextView);

        homeEventListDonateBtn = itemView.findViewById(R.id.homeEventListDonateButton);
        homeEventListViewDetailsBtn = itemView.findViewById(R.id.homeEventListViewDetailsButton);

    }
}
