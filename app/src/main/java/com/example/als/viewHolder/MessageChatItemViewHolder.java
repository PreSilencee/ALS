package com.example.als.viewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.als.R;

public class MessageChatItemViewHolder extends RecyclerView.ViewHolder {

    public TextView messageChatItem;

    public MessageChatItemViewHolder(View itemView){
        super(itemView);
        messageChatItem = itemView.findViewById(R.id.messageShowChatItem);
    }
}
