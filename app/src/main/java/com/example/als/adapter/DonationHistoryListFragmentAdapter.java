package com.example.als.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.als.R;
import com.example.als.object.Donation;

import java.util.List;

public class DonationHistoryListFragmentAdapter extends RecyclerView.Adapter<DonationHistoryListFragmentAdapter.ViewHolder>{

    //create an array list for Donation object
    private List<Donation> donationList;

    //create a context for the adapter
    private Context context;

    //constructor (donation list, context)
    public DonationHistoryListFragmentAdapter(List<Donation> donationList, Context context) {
        this.donationList = donationList;
        this.context = context;
    }

    //create view for each Donation object
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.donation_history_list_view,parent,false);

        return new DonationHistoryListFragmentAdapter.ViewHolder(view);
    }

    //attach the data of the Donation object
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //get position of donation
        Donation donation = donationList.get(position);

        //if donation date time not null
        if(donation.getDonationDateTime() != null){

            //separate the date and time
            String[] separatedDateAndTime = donation.getDonationDateTime().split(" ");

            //get date
            String[] separatedDate = separatedDateAndTime[0].split("/");

            //separate the year, day, month of date to three text view
            holder.donationHistoryListYearTV.setText(separatedDate[0]);
            holder.donationHistoryListDayTV.setText(separatedDate[2]);
            holder.donationHistoryListMonthTV.setText(separatedDate[1]);
        }
        else{
            //set default value
            holder.donationHistoryListYearTV.setText(R.string.year);
            holder.donationHistoryListDayTV.setText(R.string.day);
            holder.donationHistoryListMonthTV.setText(R.string.month);
        }

        //if id not null
        if(donation.getDonationId() != null){
            holder.donationHistoryListIdTV.setText(donation.getDonationId());
        }
        else{

            //set "-" as default
            holder.donationHistoryListIdTV.setText("-");
        }

        //if amount not 0
        if(donation.getDonationAmount() != 0){
            holder.donationHistoryListAmountTV.setText(String.valueOf(donation.getDonationAmount()));
        }
        else{
            //set "-" as default
            holder.donationHistoryListAmountTV.setText("-");
        }
    }

    //get the size of array list for donationList
    @Override
    public int getItemCount() {
        return donationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        //text view
        public TextView donationHistoryListYearTV, donationHistoryListDayTV,
                donationHistoryListMonthTV, donationHistoryListIdTV, donationHistoryListAmountTV;

        //constructor
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //find id for text view
            donationHistoryListYearTV = itemView.findViewById(R.id.donationHistoryListYearTextView);
            donationHistoryListDayTV = itemView.findViewById(R.id.donationHistoryListDayTextView);
            donationHistoryListMonthTV = itemView.findViewById(R.id.donationHistoryListMonthTextView);
            donationHistoryListIdTV = itemView.findViewById(R.id.donationHistoryListIdTextView);
            donationHistoryListAmountTV = itemView.findViewById(R.id.donationHistoryListAmountTextView);
        }
    }
}
