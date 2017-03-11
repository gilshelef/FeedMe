package com.gilshelef.feedme.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gilshelef.feedme.activities.PopupActivity;
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

//    protected ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflate(inflater, container);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDataSource = getDataSource();
        mAdapter = getAdapter();
        AdapterManager.get().setAdapter(TAG, mAdapter);
        mRecyclerView.setAdapter(mAdapter);
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
    public void onCallEvent(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(phone));
        getActivity().startActivity(intent);
    }

    @Override
    public void onSelectEvent(Donation donation){
        DataManager.get(getActivity()).selectEvent(donation.getId());
        ((OnSelectedEvent)getActivity()).onSelectedEvent(donation.getId(), donation.isSelected());
    }

    @Override
    public void onZoomEvent(View v, Donation donation){
        Intent intent = new Intent(getActivity(), PopupActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putParcelable(PopupActivity.EXTRA_DONATION, donation);
        intent.putExtras(mBundle);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), v, "profile");
        startActivity(intent, options.toBundle());
    }

    public interface OnSelectedEvent{
        void onSelectedEvent(String id, boolean selected);
    }



}
