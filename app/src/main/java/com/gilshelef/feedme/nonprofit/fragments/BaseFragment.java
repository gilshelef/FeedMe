package com.gilshelef.feedme.nonprofit.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.nonprofit.adapters.AdapterManager;
import com.gilshelef.feedme.nonprofit.adapters.RecycledBaseAdapter;
import com.gilshelef.feedme.nonprofit.adapters.SimpleItemTouchHelperCallback;
import com.gilshelef.feedme.nonprofit.data.DataManager;
import com.gilshelef.feedme.nonprofit.data.Donation;

import java.util.List;

/**
 * Created by gilshe on 2/27/17.
 */
public abstract class BaseFragment extends Fragment implements RecycledBaseAdapter.OnActionEvent {
    public final String TAG = this.getClass().getSimpleName();

    protected List<Donation> mDataSource;
    protected RecyclerView mRecyclerView;
    protected RecycledBaseAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflate(inflater, container);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDataSource = getDataSource();
        mAdapter = getAdapter();
        mRecyclerView.setAdapter(mAdapter);

        //set adapter in manager
        AdapterManager.get().setAdapter(mAdapter);

        //draw divider line between list items
        Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.divider);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(dividerDrawable);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);
    }

    protected abstract RecycledBaseAdapter getAdapter();
    protected abstract List<Donation> getDataSource();
    protected abstract View inflate(LayoutInflater inflater, ViewGroup container);

    @Override
    public void onSaveEvent(Donation donation) {
        DataManager.get(getActivity()).saveEvent(donation.getId());
        ((OnCounterChangeListener)getActivity()).updateViewCounters();
    }

    @Override
    public void onUnSaveEvent(Donation donation) {
        DataManager.get(getActivity()).unSaveEvent(donation.getId());
        ((OnCounterChangeListener)getActivity()).updateViewCounters();
    }

    @Override
    public void onDetailsEvent(View v, Donation donation){
        ((OnDetailsListener)getActivity()).onDetails(v, donation);
    }


    public interface OnDetailsListener {
        void onDetails(View v, Donation donation);
    }


}
