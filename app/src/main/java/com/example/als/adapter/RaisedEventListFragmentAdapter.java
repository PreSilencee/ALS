package com.example.als.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.als.R;
import com.example.als.object.Event;
import com.example.als.object.Variable;
import com.example.als.ui.raised_event.RaisedEventDetailsActivity;


import java.util.List;

public class RaisedEventListFragmentAdapter extends RecyclerView.Adapter<RaisedEventListFragmentAdapter.ViewHolder> {

    //create an array for Event object
    private List<Event> eventList;

    //create context
    private Context context;

    //constructor for eventlist and context
    public RaisedEventListFragmentAdapter(List<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    //create each view for event
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.raised_event_view_layout,parent,false);
        return new RaisedEventListFragmentAdapter.ViewHolder(view);
    }

    //attach the data to the view
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Event event = eventList.get(position);

        //if event date time created not null
        if(event.getEventDateTimeCreated() != null){
            //separate the date and time
            String[] separatedDateAndTime = event.getEventDateTimeCreated().split(" ");
            //get date
            String[] separatedDate = separatedDateAndTime[0].split("/");
            //separate the year, day, month of date to three text view
            holder.raisedEventListYearTV.setText(separatedDate[2]);
            holder.raisedEventListDayTV.setText(separatedDate[0]);
            holder.raisedEventListMonthTV.setText(separatedDate[1]);
        }
        else{
            //set default value
            holder.raisedEventListYearTV.setText(R.string.year);
            holder.raisedEventListDayTV.setText(R.string.day);
            holder.raisedEventListMonthTV.setText(R.string.month);
        }

        //if event title not null
        if(event.getEventTitle() != null){
            holder.raisedEventListNameTV.setText(event.getEventTitle());
        }
        else{
            //set "-" as default value
            holder.raisedEventListNameTV.setText("-");
        }

        //calculate the progress
        double fundProgress = (event.getEventCurrentAmount() / event.getEventTargetAmount()) * 100;
        //apply to progress bar
        holder.raisedEventListPB.setProgress((int) fundProgress);
        //set string to text view
        String progress = (int) fundProgress + "%";
        holder.raisedEventListProgressTV.setText(progress);

        if(event.getEventStatus() != null){

            switch (event.getEventStatus()) {
                case Variable.PENDING:
                    holder.raisedEventListStatusTV.setTextColor(context.getColor(R.color.colorGray));
                    break;
                case Variable.AVAILABLE:
                    holder.raisedEventListStatusTV.setTextColor(context.getColor(R.color.colorGreen));
                    break;
                case Variable.DECLINED:
                    holder.raisedEventListStatusTV.setTextColor(context.getColor(R.color.colorRed));
                    break;
            }
            holder.raisedEventListStatusTV.setText(event.getEventStatus());
        }

        holder.raisedEventListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, RaisedEventDetailsActivity.class);
                i.putExtra(Variable.EVENT_SESSION_ID, event.getEventId());
                context.startActivity(i);
            }
        });

//        //itemView onclick
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    //get size of event list
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    //view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        //text view
        public TextView raisedEventListYearTV, raisedEventListDayTV,
                raisedEventListMonthTV, raisedEventListNameTV, raisedEventListProgressTV, raisedEventListStatusTV;

        //progress bar
        public ProgressBar raisedEventListPB;

        public Button raisedEventListBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //find id for text view
            raisedEventListYearTV = itemView.findViewById(R.id.raisedEventListYearTextView);
            raisedEventListDayTV = itemView.findViewById(R.id.raisedEventListDayTextView);
            raisedEventListMonthTV = itemView.findViewById(R.id.raisedEventListMonthTextView);
            raisedEventListNameTV = itemView.findViewById(R.id.raisedEventListNameTextView);
            raisedEventListProgressTV = itemView.findViewById(R.id.raisedEventListProgressTextView);
            raisedEventListStatusTV = itemView.findViewById(R.id.raisedEventListStatusTextView);

            //find id for progress bar
            raisedEventListPB = itemView.findViewById(R.id.raisedEventTargetFundProgressBar);

            raisedEventListBtn = itemView.findViewById(R.id.raisedEventListViewButton);

        }
    }
}
