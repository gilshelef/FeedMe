package com.gilshelef.feedme.nonprofit.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.nonprofit.data.Donation;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.Collections;
import java.util.List;

/**
 * Created by gilshe on 2/27/17.
 */

public abstract class RecycledBaseAdapter extends  RecyclerView.Adapter<ItemViewHolder> implements Adaptable, ItemTouchHelperAdapter {
    public final String TAG = this.getClass().getSimpleName();
    protected List<Donation> mDataSource;
    protected Activity mActivity;
    protected OnActionEvent mListener;

    public RecycledBaseAdapter(Activity activity, List<Donation> dataSource, OnActionEvent listener) {
        this.mDataSource = dataSource;
        this.mActivity = activity;
        this.mListener = listener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, null);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {
        final Donation donation = mDataSource.get(position);
        holder.bind(mActivity,donation);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDetailsEvent(v.findViewById(R.id.list_thumbnail), donation);
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
        styleListItem(holder, donation);
    }

    protected abstract void styleListItem(ItemViewHolder itemView, Donation donation);

    @Override
    public int getItemCount() {
        return (null != mDataSource ? mDataSource.size() : 0);
    }

    @Override
    public String getName() {
        return TAG;
    }

    protected void setSelected(final ItemViewHolder holder) {
        holder.itemView.setBackground(mActivity.getDrawable(R.color.selected));
        holder.image.setBackground(mActivity.getDrawable(R.color.selected));
    }

    protected void setUnSelected(ItemViewHolder holder) {
        holder.itemView.setBackground(mActivity.getDrawable(R.color.lightPrimaryColor));
        holder.image.setBackground(mActivity.getDrawable(R.color.lightPrimaryColor));
    }


    @Override
    public void onItemDismiss(int position) {
        Donation donation = mDataSource.get(position);
        mDataSource.remove(position);
        onItemDismiss(donation);
        notifyItemRemoved(position);
    }

    protected abstract void onItemDismiss(Donation donation);

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mDataSource, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mDataSource, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }


    public interface OnActionEvent {
        void onSaveEvent(Donation donation);
        void onUnSaveEvent(Donation donation);
        void onDetailsEvent(View v, Donation donation);
    }


}
