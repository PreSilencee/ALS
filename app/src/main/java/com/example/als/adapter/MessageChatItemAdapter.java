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

    //console log
    private static final String TAG = "MessageChatItemAdapter";

    //array list for message object
    private List<Message> messageList;

    //context for adapter
    private Context context;

    //firebase user
    FirebaseUser cUser;

    //constructor(context, message);
    public MessageChatItemAdapter(List<Message> messages, Context context){
        this.messageList = messages;
        this.context = context;
    }

    //create view for each Message object
    @NonNull
    @Override
    public MessageChatItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //if view type equal to right
        if(viewType == Variable.MSG_TYPE_RIGHT){
            //show right layout
            View view = LayoutInflater.from(context).inflate(R.layout.message_chat_item_right,parent,false);
            return new MessageChatItemAdapter.ViewHolder(view);
        }
        else{
            //show left layout
            View view = LayoutInflater.from(context).inflate(R.layout.message_chat_item_left,parent,false);
            return new MessageChatItemAdapter.ViewHolder(view);
        }
    }

    //attach data to the view
    @Override
    public void onBindViewHolder(@NonNull MessageChatItemAdapter.ViewHolder holder, int position) {

        //get message position
        Message message = messageList.get(position);

        //decrypt message
        String decryptedMessage ="";
        try{
            decryptedMessage = AESCrypt.decrypt(message.getMessageContent());
        }
        catch (Exception e){
            Log.d(TAG, e.toString());
        }

        //set decrypted message
        holder.messageChatItem.setText(decryptedMessage);
    }

    //get size of message list
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    //view holder class
    public static class ViewHolder extends RecyclerView.ViewHolder{

        //text view
        public TextView messageChatItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //find id for text view
            messageChatItem = itemView.findViewById(R.id.messageShowChatItem);
        }
    }

    //get item view type
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
