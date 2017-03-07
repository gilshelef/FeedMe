package com.gilshelef.feedme;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by gilshe on 2/27/17.
 */

abstract class RecycledBaseAdapter extends  RecyclerView.Adapter<RecycledBaseAdapter.ViewHolder>  {
    private final String TAG = this.getClass().getSimpleName();
    List<Donation> mDataSource;
    Context mContext;
    private OnActionEvent mListener;
    private boolean selectedView = false;


    RecycledBaseAdapter(Context context, List<Donation> dataSource, OnActionEvent listener) {
        this.mDataSource = dataSource;
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    public RecycledBaseAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        int layout = getListItemLayout();
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layout, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecycledBaseAdapter.ViewHolder holder, final int position) {
        final Donation donation = mDataSource.get(position);
        holder.bind(donation);
    }


    @Override
    public int getItemCount() {
        return (null != mDataSource ? mDataSource.size() : 0);
    }
    void clearSelectedView(){
        selectedView = false;
    }
    abstract int getListItemLayout();
    abstract void updateDataSource();

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {

        private final com.squareup.picasso.Transformation mTransformation;
        private Donation mDonation;
        private ImageView image;
        private TextView type;
        private TextView description;
        private TextView contact;
        private ImageView save;
        private ImageView call;
        private TextView distance;
        private CardView cardView;
        private View cardSeparator;

        ViewHolder(View view) {
            super(view);

            mTransformation = new RoundedTransformationBuilder()
                    .cornerRadiusDp(4)
                    .oval(false)
                    .build();

            this.image = (ImageView) view.findViewById(R.id.list_thumbnail);
            this.type = (TextView) view.findViewById(R.id.list_type);
            this.description = (TextView) view.findViewById(R.id.list_description);
            this.distance = (TextView) view.findViewById(R.id.list_distance);
            this.call = (ImageView) view.findViewById(R.id.list_call_thumbnail);
            this.contact = (TextView) view.findViewById(R.id.list_contact);
            this.save = (ImageView) view.findViewById(R.id.list_save_thumbnail);
            this.cardView = (CardView) view.findViewById(R.id.list_card_view);
            this.cardSeparator = view.findViewById(R.id.list_line);

        }

        void bind(Donation donation) {
            mDonation = donation;
            loadSave();
            loadSelected();

            //Setting text views
            type.setText(donation.getType());
            description.setText(donation.getDescription());
            contact.setText(donation.getContactInfo());

            //distance
            double distance = Association.calcDistance(mDonation.getLocation());
            String text = String.format(mContext.getString(R.string.distance), distance);
            this.distance.setText(text);

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {mListener.onSaveEvent(mDonation);
                }
            });
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {mListener.onCallEvent(mDonation.getPhone());
                }
            });
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }

        private void loadSave() {
            int resource = R.drawable.take_unavailable;
            if (mDonation.isAvailable())
                resource = R.drawable.not_saved;
            else if (mDonation.isSaved())
                resource = R.drawable.saved;
            save.setImageResource(resource);
        }

        @Override
        public boolean onLongClick(View v) {
            selectedView = true;
            mListener.onSelectEvent(mDonation);
            return true;
        }

        @Override
        public void onClick(View v) {
            if (selectedView)
                onLongClick(v);
            else
                mListener.onZoomEvent(v.findViewById(R.id.list_thumbnail), mDonation);
        }

        private void setUnselected() {
            if(!mDonation.getImageUrl().isEmpty())
                Picasso.with(mContext)
                    .load(mDonation.getImageUrl())
                    .fit()
                    .transform(mTransformation)
                    .error(mDonation.getDefaultImage())
                    .into(image);
            else image.setImageResource(mDonation.getDefaultImage());
            cardView.setCardBackgroundColor(Color.WHITE);
            cardSeparator.setVisibility(View.VISIBLE);
        }

        private void setSelected() {
            image.setImageResource(R.drawable.checked);
            cardView.setCardBackgroundColor(Color.LTGRAY);
            cardSeparator.setVisibility(View.GONE);
        }

        private void loadSelected() {
            if (mDonation.isSelected())
                setSelected();
            else setUnselected();
        }

    }

    interface OnActionEvent {
        void onSaveEvent(Donation donation);
        void onCallEvent(String phone);
        void onSelectEvent(Donation donation);
        void onZoomEvent(View v, Donation donation);
    }

}
