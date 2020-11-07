package com.example.als.viewHolder;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.als.R;


public class RaisedEventListFragmentViewHolder extends RecyclerView.ViewHolder {

    public CardView raisedEventListCV;
    public TextView raisedEventListYearTV, raisedEventListDayTV,
            raisedEventListMonthTV, raisedEventListNameTV, raisedEventListProgressTV;
    public ProgressBar raisedEventListPB;

    public RaisedEventListFragmentViewHolder(View itemView){
        super(itemView);

        raisedEventListCV = itemView.findViewById(R.id.raisedEventListCardView);
        raisedEventListYearTV = itemView.findViewById(R.id.raisedEventListYearTextView);
        raisedEventListDayTV = itemView.findViewById(R.id.raisedEventListDayTextView);
        raisedEventListMonthTV = itemView.findViewById(R.id.raisedEventListMonthTextView);
        raisedEventListNameTV = itemView.findViewById(R.id.raisedEventListNameTextView);
        raisedEventListProgressTV = itemView.findViewById(R.id.raisedEventListProgressTextView);
        raisedEventListPB = itemView.findViewById(R.id.raisedEventTargetFundProgressBar);

    }
}
