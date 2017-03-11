package com.gilshelef.feedme.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.data.Donation;
import com.squareup.picasso.Picasso;

/**
 * Created by gilshe on 3/4/17.
 */
public class PopupActivity extends AppCompatActivity {

    public static final String EXTRA_DONATION = "DONATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_main);

        Donation donation = getIntent().getParcelableExtra(EXTRA_DONATION);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width*0.6), (int)(height*0.4));
        TextView tv = (TextView) findViewById(R.id.popup_type);
        ImageView im = (ImageView) findViewById(R.id.popup_image);

        if(!donation.getImageUrl().isEmpty())
            Picasso.with(getApplicationContext())
                    .load(donation.getImageUrl())
                    .fit()
                    .error(donation.getType().defaultThumbnail())
                    .into(im);

        else
            Picasso.with(getApplicationContext())
                    .load(donation.getType().defaultThumbnail())
                    .fit()
                    .into(im);
        tv.setText(donation.getType().hebrew());
    }


}
