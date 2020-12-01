package com.example.als.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.als.R;
import com.example.als.object.Donation;
import com.example.als.object.Event;

import java.util.List;

public class DonationHistoryListFragmentAdapter extends RecyclerView.Adapter<DonationHistoryListFragmentAdapter.ViewHolder>{

    private List<Donation> donationsHistoryListListData;
    private Context alsContext;

    public DonationHistoryListFragmentAdapter(Context alsContext, List<Donation> donationsHistoryListListData) {
        this.alsContext = alsContext;
        this.donationsHistoryListListData = donationsHistoryListListData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.donation_history_list_view,parent,false);

        return new DonationHistoryListFragmentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Donation donation = donationsHistoryListListData.get(position);

        if(donation.getDonationDateTime() != null){
            String[] separatedDateAndTime = donation.getDonationDateTime().split(" ");
            String[] separatedDate = separatedDateAndTime[0].split("/");
            holder.donationHistoryListYearTV.setText(separatedDate[2]);
            holder.donationHistoryListDayTV.setText(separatedDate[0]);
            holder.donationHistoryListMonthTV.setText(separatedDate[1]);
        }

        if(donation.getDonationId() != null){
            holder.donationHistoryListIdTV.setText(donation.getDonationId());
        }
        else{
            holder.donationHistoryListIdTV.setText("-");
        }

        if(donation.getDonationAmount() != 0){
            holder.donationHistoryListAmountTV.setText(String.valueOf(donation.getDonationAmount()));
        }
    }

    @Override
    public int getItemCount() {
        return donationsHistoryListListData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView donationHistoryListYearTV, donationHistoryListDayTV,
                donationHistoryListMonthTV, donationHistoryListIdTV, donationHistoryListAmountTV;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            donationHistoryListYearTV = itemView.findViewById(R.id.donationHistoryListYearTextView);
            donationHistoryListDayTV = itemView.findViewById(R.id.donationHistoryListDayTextView);
            donationHistoryListMonthTV = itemView.findViewById(R.id.donationHistoryListMonthTextView);
            donationHistoryListIdTV = itemView.findViewById(R.id.donationHistoryListIdTextView);
            donationHistoryListAmountTV = itemView.findViewById(R.id.donationHistoryListAmountTextView);
        }
    }
}
