package com.gilshelef.feedme.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.adapters.AdapterManager;
import com.gilshelef.feedme.adapters.RecycledBaseAdapter;
import com.gilshelef.feedme.data.DataManager;
import com.gilshelef.feedme.data.Donation;

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

    protected abstract RecycledBaseAdapter getAdapter();
    protected abstract List<Donation> getDataSource();
    protected abstract View inflate(LayoutInflater inflater, ViewGroup container);

    @Override
    public void onSaveEvent(Donation donation) {
        DataManager.get(getActivity()).saveEvent(donation.getId());
    }

    @Override
    public void onUnSaveEvent(Donation donation) {
        DataManager.get(getActivity()).unSaveEvent(donation.getId());
    }

    @Override
    public void onClickEvent(View v, Donation donation){
        ((OnDetailsListener)getActivity()).onDetails(v, donation);
    }


    public interface OnDetailsListener {
        void onDetails(View v, Donation donation);
    }


}
