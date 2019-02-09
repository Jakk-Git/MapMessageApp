package edu.temple.mapmessageapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;


public class PersonListAdapter extends RecyclerView.Adapter<PersonListAdapter.MyViewHolder> {


    private JSONArray personlist;
    Context activitycontext;

    public JSONArray getTourlist() {
        return personlist;
    }

    public void setTourlist(JSONArray tourlist) {
        this.personlist = tourlist;
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tourname;
        public MyViewHolder(TextView v) {

            super(v);
            tourname = v;
        }
    }

    public PersonListAdapter(JSONArray tourlist, Context activitycontext)
    {
        this.personlist = tourlist;
        this.activitycontext = activitycontext;
    }

    @Override
    public int getItemCount() {
        return personlist.length();
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
        try {
            if(personlist.get(position) != null) {
                holder.tourname.setText(personlist.getJSONObject(position).getString("username"));
                holder.tourname.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MapListActivity tempcallactivity = (MapListActivity) activitycontext;
                        //put stuff here
                        //
                        //
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }




}
