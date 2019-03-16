package edu.temple.mapmessageapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;


public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MyViewHolder> {


    private Message[] messagelist;
    Context activitycontext;

    public Message[] getTourlist() {
        return messagelist;
    }

    public void setMessagelist(Message[] tourlist) {
        this.messagelist = tourlist;
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView message;
        public MyViewHolder(TextView v) {

            super(v);
            message = v;
        }
    }

    public ChatListAdapter(Message[] messagelist, Activity activitycontext)
    {
        this.messagelist = messagelist;
        this.activitycontext = activitycontext;
    }

    @Override
    public int getItemCount() {
        return messagelist.length;
    }




    @Override
    public ChatListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final int tempposition = position;
        if(messagelist[position] != null)
        {
            holder.message.setText(messagelist[position].getText());
            if(messagelist[position].isFromme())
            {

                holder.message.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                holder.message.setTextColor(Color.DKGRAY);

            }
            else
            {
                holder.message.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);

            }
        }
    }




}