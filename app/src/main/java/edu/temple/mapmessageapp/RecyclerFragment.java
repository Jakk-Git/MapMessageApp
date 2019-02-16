package edu.temple.mapmessageapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RecyclerFragment extends Fragment {

    RecyclerView rv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_fragment_layout, container, false);
        rv = view.findViewById(R.id.myrecycler);
        super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    public void setReyclerView(RecyclerView gotrecycle)
    {
        this.rv = gotrecycle;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rv = new RecyclerView(getContext());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        rv = getActivity().findViewById(R.id.myrecycler);
    }

    public RecyclerView getRecyclerView()
    {
        return rv;
    }

    public RecyclerView getRecycler()
    {
        return rv;
    }

    public void updateRecycler(Partner[] partnerlist)
    {
        if(rv != null) {
            PersonListAdapter personList = new PersonListAdapter(partnerlist, getContext());
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            rv.setAdapter(personList);
            rv.setLayoutManager(llm);
            rv.setHasFixedSize(true);
        }

    }

}
