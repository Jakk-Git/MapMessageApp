package edu.temple.mapmessageapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;


public class PersonListAdapter extends RecyclerView.Adapter<PersonListAdapter.MyViewHolder> {


    private Partner[] personlist;
    Context activitycontext;

    public Partner[] getTourlist() {
        return personlist;
    }

    public void setTourlist(Partner[] tourlist) {
        this.personlist = tourlist;
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView personname;
        public MyViewHolder(TextView v) {

            super(v);
            personname = v;
        }
    }

    public PersonListAdapter(Partner[] personlist, Activity activitycontext)
    {
        this.personlist = personlist;
        this.activitycontext = activitycontext;
    }

    @Override
    public int getItemCount() {
        return personlist.length;
    }




    @Override
    public PersonListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.person_list_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final int tempposition = position;
        if(personlist[position] != null)
        {
            holder.personname.setText(personlist[position].name);
            holder.personname.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MapListActivity tempcallactivity = (MapListActivity) activitycontext;
                        if(!tempcallactivity.preferences.getString(personlist[position].name, "false").equals("false"))
                        {
                            Intent i = new Intent(activitycontext, ChatActivity.class);
                            i.putExtra("username", personlist[position].name);
                            tempcallactivity.startActivity(i);
                        }
                        else
                        {
                            Toast.makeText(tempcallactivity, "You haven't added this person!", Toast.LENGTH_SHORT).show();
                        }
                        //put stuff here
                        //
                        //
                    }
                });
            }
    }




}
