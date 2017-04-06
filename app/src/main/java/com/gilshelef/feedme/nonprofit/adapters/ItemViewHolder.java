package com.gilshelef.feedme.nonprofit.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.nonprofit.data.NonProfit;
import com.gilshelef.feedme.nonprofit.data.Donation;
import com.like.LikeButton;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;

/**
 * Created by gilshe on 3/17/17.
 */

public class ItemViewHolder extends RecyclerView.ViewHolder {

    private final com.squareup.picasso.Transformation mTransformation;
    private Donation mDonation;
    public ImageView image;
    private TextView type;
    private TextView description;
    public LikeButton save;
    public TextView distance;

    ItemViewHolder(View view) {
        super(view);

        mTransformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(4)
                .oval(false)
                .build();

        this.image = (ImageView) view.findViewById(R.id.list_thumbnail);
        this.type = (TextView) view.findViewById(R.id.list_type);
        this.description = (TextView) view.findViewById(R.id.list_description);
        this.distance = (TextView) view.findViewById(R.id.list_distance);
        this.save = (LikeButton) view.findViewById(R.id.save_button);
    }

    void bind(Activity activity, Donation donation) {
        mDonation = donation;
        loadSave();

        //image
        if(!mDonation.getImageUrl().isEmpty())
            Picasso.with(activity.getApplicationContext())
                    .load(mDonation.getImageUrl())
                    .fit()
                    .transform(mTransformation)
                    .error(mDonation.getType().defaultThumbnail())
                    .placeholder(R.drawable.ic_placeholder)
                    .into(image);
        else image.setImageResource(mDonation.getType().defaultThumbnail());

        //Setting text views
        type.setText(donation.getType().hebrew());
        description.setText(donation.getDescription());

        //distance
        double distance = NonProfit.get(activity).calcDistance(mDonation.getPosition());
        String text = String.format(activity.getResources().getString(R.string.distance), distance);
        this.distance.setText(text);
    }

    private void loadSave() {
        if (mDonation.isAvailable())
            save.setLiked(false);
        else if (mDonation.isSaved())
            save.setLiked(true);
    }


}

