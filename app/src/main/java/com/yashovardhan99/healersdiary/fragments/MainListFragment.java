package com.yashovardhan99.healersdiary.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yashovardhan99.healersdiary.activities.MainActivity;
import com.yashovardhan99.healersdiary.R;

/**
 * Created by Yashovardhan99 on 05-08-2018 as a part of HealersDiary.
 */
public class MainListFragment extends Fragment {

    private RecyclerView.Adapter mAdapter;

    public MainListFragment(){
        //required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_main_list,container,false);
        //
        //Recycler view setup
        mAdapter = ((MainActivity) getActivity()).getAdapter();
        RecyclerView mRecyclerView;
        mRecyclerView = RootView.findViewById(R.id.recycler_main);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        //displays the divider line bw each item
        DividerItemDecoration itemLine = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemLine);

        return RootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
