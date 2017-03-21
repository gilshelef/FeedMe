package com.gilshelef.feedme.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.data.Donation;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.List;

/**
 * Created by gilshe on 2/27/17.
 */

public abstract class RecycledBaseAdapter extends  RecyclerView.Adapter<ViewHolder> implements Adaptable {
    public final String TAG = this.getClass().getSimpleName();
    protected List<Donation> mDataSource;
    protected Activity mActivity;
    protected OnActionEvent mListener;

    RecycledBaseAdapter(Activity activity, List<Donation> dataSource, OnActionEvent listener) {
        this.mDataSource = dataSource;
        this.mActivity = activity;
        this.mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Donation donation = mDataSource.get(position);
        holder.bind(mActivity,donation);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClickEvent(v.findViewById(R.id.list_thumbnail), donation);
            }
        });
        holder.save.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                mListener.onSaveEvent(donation);
                AdapterManager.get().updateDataSourceAll(TAG);
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                mListener.onUnSaveEvent(donation);
                AdapterManager.get().updateDataSourceAll(TAG);
            }
        });
        styleSelectedItem(holder, donation);
    }

    protected abstract void styleSelectedItem(ViewHolder itemView, Donation donation);

    @Override
    public int getItemCount() {
        return (null != mDataSource ? mDataSource.size() : 0);
    }

    @Override
    public String getName() {
        return TAG;
    }

    public interface OnActionEvent {
        void onSaveEvent(Donation donation);
        void onUnSaveEvent(Donation donation);
        void onClickEvent(View v, Donation donation);
    }

}
