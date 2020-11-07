package com.example.als.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.als.R;
import com.example.als.object.Event;
import com.example.als.viewHolder.RaisedEventListFragmentViewHolder;

import java.util.List;

public class RaisedEventListFragmentAdapter extends RecyclerView.Adapter<RaisedEventListFragmentAdapter.ViewHolder> {
    private List<Event> raisedEventListData;
    private Context alsContext;


    public RaisedEventListFragmentAdapter(List<Event> list, Context context){
        raisedEventListData = list;
        alsContext = context;
    }

    @NonNull
    @Override
    public RaisedEventListFragmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.raised_event_view_layout,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RaisedEventListFragmentAdapter.ViewHolder holder, int position) {
        Event event = raisedEventListData.get(position);
        if(event.getEventDateTimeCreated() != null){

            String[] separatedDateAndTime = event.getEventDateTimeCreated().split(" ");
            String[] separatedDate = separatedDateAndTime[0].split("/");
            holder.raisedEventListYearTV.setText(separatedDate[2]);
            holder.raisedEventListDayTV.setText(separatedDate[0]);
            holder.raisedEventListMonthTV.setText(separatedDate[1]);
        }

        if(event.getEventTitle() != null){
            holder.raisedEventListNameTV.setText(event.getEventTitle());
        }
        else{
            holder.raisedEventListNameTV.setText("-");
        }

        String currentAmount;
        String targetAmount;
        double cAmount;
        double tAmount;

        if(event.getEventCurrentAmount() >= 0){
            currentAmount = "RM " + event.getEventCurrentAmount();
            cAmount = event.getEventCurrentAmount();
        }
        else{
            currentAmount = "RM 0";
            cAmount = 0.0;
        }

        if(event.getEventTargetAmount() > 0){
            targetAmount = "RM " + event.getEventTargetAmount();
            tAmount = event.getEventTargetAmount();
        }
        else {
            targetAmount = "RM 0";
            tAmount = 0.0;
        }

        String currentProgressTV = currentAmount + "/" +targetAmount;
        holder.raisedEventListProgressTV.setText(currentProgressTV);

        double progress = (cAmount/tAmount)*100;
        holder.raisedEventListPB.setProgress((int) progress);
    }

    @Override
    public int getItemCount() {
        return raisedEventListData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView raisedEventListYearTV, raisedEventListDayTV,
                raisedEventListMonthTV, raisedEventListNameTV, raisedEventListProgressTV;
        public ProgressBar raisedEventListPB;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            raisedEventListYearTV = itemView.findViewById(R.id.raisedEventListYearTextView);
            raisedEventListDayTV = itemView.findViewById(R.id.raisedEventListDayTextView);
            raisedEventListMonthTV = itemView.findViewById(R.id.raisedEventListMonthTextView);
            raisedEventListNameTV = itemView.findViewById(R.id.raisedEventListNameTextView);
            raisedEventListProgressTV = itemView.findViewById(R.id.raisedEventListProgressTextView);
            raisedEventListPB = itemView.findViewById(R.id.raisedEventTargetFundProgressBar);
        }
    }
}
