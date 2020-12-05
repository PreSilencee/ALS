package com.example.als.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.als.R;
import com.example.als.handler.AESCrypt;
import com.example.als.object.Message;
import com.example.als.object.Variable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageChatItemAdapter extends RecyclerView.Adapter<MessageChatItemAdapter.ViewHolder> {

    private static final String TAG = "MessageChatItemAdapter";
    private List<Message> messageList;
    private Context context;

    FirebaseUser cUser;

    public MessageChatItemAdapter(Context context, List<Message> messages){
        this.context = context;
        this.messageList = messages;
    }

    @NonNull
    @Override
    public MessageChatItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == Variable.MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.message_chat_item_right,parent,false);
            return new MessageChatItemAdapter.ViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.message_chat_item_left,parent,false);
            return new MessageChatItemAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageChatItemAdapter.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        String decryptedMessage ="";
        try{
            decryptedMessage = AESCrypt.decrypt(message.getMessageContent());
        }
        catch (Exception e){
            Log.d(TAG, e.toString());
        }

        holder.messageChatItem.setText(decryptedMessage);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView messageChatItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageChatItem = itemView.findViewById(R.id.messageShowChatItem);
        }
    }

    @Override
    public int getItemViewType(int position) {
        cUser = FirebaseAuth.getInstance().getCurrentUser();
        if(messageList.get(position).getMessageSender().equals(cUser.getUid())){
            return Variable.MSG_TYPE_RIGHT;
        }
        else{
            return Variable.MSG_TYPE_LEFT;
        }
    }
}
