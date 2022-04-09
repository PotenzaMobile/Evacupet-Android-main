package com.evacupet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.evacupet.model.ChatModel;
import com.evacupet.utility.SessionManager;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class ChatAdapter extends RecyclerView.Adapter {

    private ArrayList<ParseObject> dataList;
    private Context mContext;
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;


    public ChatAdapter(Context context) {
        this.mContext = context;

    }

    public void setData(ArrayList<ParseObject> dataList) {
        this.dataList = dataList;
    }


    @Override
    public int getItemCount() {
        return null != dataList ? dataList.size() : 0;
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        final ParseObject object = (ParseObject)dataList.get(position);

        if (object.getString("from_user").equalsIgnoreCase(ParseUser.getCurrentUser().getObjectId())) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_send, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_recevie, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ParseObject object = (ParseObject)dataList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(object);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(object);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.row_message_send);
        }

        void bind(ParseObject message) {
            messageText.setText(message.getString("message"));
            String inputPattern = "yyyy-MM-dd HH:mm:ss";
            String outputPattern = "HH:mm";
            SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
            SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
            Date date = null;
            String str = null;

        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText =  itemView.findViewById(R.id.row_message_receive);
        }

        void bind(ParseObject message) {
            messageText.setText(message.getString("message"));
            String inputPattern = "yyyy-MM-dd HH:mm:ss";
            String outputPattern = "HH:mm";
            SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
            SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
            Date date = null;
            String str = null;


        }
    }
}
