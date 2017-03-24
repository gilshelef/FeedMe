package com.gilshelef.feedme.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.gilshelef.feedme.Constants;
import com.gilshelef.feedme.R;
import com.gilshelef.feedme.data.Donation;
import com.google.android.gms.maps.model.LatLng;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by gilshe on 3/4/17.
 */
public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = DetailsActivity.class.getSimpleName();
    public static final String EXTRA_DONATION = "donation";
    private static final String UNNAMED_ROAD = "Unnamed Road";
    private final View.OnClickListener exitListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendDataAndFinish();
        }
    };
    private final View.OnClickListener contactListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(donation.getPhone()));
            startActivity(intent);
        }
    };

    private final View.OnClickListener addToCartListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            donation.setInCart(!donation.inCart());
            styleCartBtn();
        }
    };

    Donation donation;
    ImageButton exit;
    LikeButton save;
    ImageView thumbnail;
    TextView business;
    TextView description;
    View contactBtn;
    TextView contactInfo;
    TextView timeInfo;
    View addressBtn;
    TextView addressInfo;
    Button addToCartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deatils);
        donation = getIntent().getParcelableExtra(EXTRA_DONATION);
        extractContainers();

        if(donation.isSaved())
            save.setLiked(true);

        //thumbnail
        if(!donation.getImageUrl().isEmpty())
            Picasso.with(getApplicationContext())
                    .load(donation.getImageUrl())
                    .fit()
                    .error(donation.getType().defaultThumbnail())
                    .into(thumbnail);
        else
            Picasso.with(getApplicationContext())
                    .load(donation.getType().defaultThumbnail())
                    .fit()
                    .into(thumbnail);

        //text
        business.setText(donation.getBusinessName());
        description.setText(donation.getDescription());

        //contact
        contactInfo.setText(donation.getContactInfo());
        timeInfo.setText(donation.getTime());

        styleCartBtn();

        if(donation.isOwned()) {
            save.setVisibility(View.GONE);
            addToCartBtn.setVisibility(View.GONE);
        }

        new ListenerTask().execute();
    }

    private void styleCartBtn() {
        if(donation.inCart())
            setCartBtnStyle(R.string.remove_from_cart, R.drawable.rounded_shape_accent);
        else setCartBtnStyle(R.string.add_to_cart, R.drawable.rounded_shape_primary);
    }

    private void setCartBtnStyle(int text, int color) {
        addToCartBtn.setText(text);
        Drawable drawable = getResources().getDrawable(color);
        addToCartBtn.setBackground(drawable);
    }

    private void sendDataAndFinish() {
        Intent intent = new Intent();
        intent.putExtra(Constants.DONATION_ID, donation.getId());
        intent.putExtra(Constants.DONATION_STATE, donation.getState().name());
        intent.putExtra(Constants.IN_CART, donation.inCart());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void extractContainers() {
        //header
        exit = (ImageButton) findViewById(R.id.details_exit_btn);
        save = (LikeButton) findViewById(R.id.details_save_image);

        //thumbnail
        thumbnail = (ImageView) findViewById(R.id.details_image);

        //text
        business = (TextView) findViewById(R.id.details_business_name);
        description = (TextView) findViewById(R.id.details_description);

        //contact
        contactBtn = findViewById(R.id.details_contact);
        contactInfo = (TextView) findViewById(R.id.details_contact_info);

        //time
        timeInfo = (TextView) findViewById(R.id.details_time_info);

        //address
        addressBtn = findViewById(R.id.details_address);
        addressInfo = (TextView) findViewById(R.id.details_address_info);

        addToCartBtn = (Button) findViewById(R.id.details_add_to_cart);

    }

    @Override
    public void onBackPressed () {
        sendDataAndFinish();
    }

    private class ListenerTask extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... params) {
            exit.setOnClickListener(exitListener);
            save.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    donation.setState(Donation.State.SAVED);
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    donation.setState(Donation.State.AVAILABLE);
                }
            });
            contactBtn.setOnClickListener(contactListener);
            addToCartBtn.setOnClickListener(addToCartListener);

            Geocoder geocoder;
            List<Address> addresses;
            Locale aLocale = new Locale.Builder().setLanguage("he").build();
            geocoder = new Geocoder(getApplicationContext(), aLocale);
            LatLng latlng = donation.getPosition();
            String addressDetails = "";
            try {
                addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String country = addresses.get(0).getCountryName();
                if(!address.equals(UNNAMED_ROAD))
                    addressDetails = address + ", ";
                addressDetails += city + ", " + country;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                //TODO
            }

            addressBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchNavigationApp();
                }
            });
            return addressDetails;
        }

        protected void onPostExecute(String addressDetails){
            addressInfo.setText(addressDetails);
        }
    }

    private void launchNavigationApp() {
        LatLng latLng = donation.getPosition();
        String format = "waze://?ll="+latLng.latitude+", " +latLng.longitude + "&navigate=yes";
        Uri uri = Uri.parse(format);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.popup_main);
//
//        Donation donation = getIntent().getParcelableExtra(EXTRA_DONATION);
//
//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//
//        int width = dm.widthPixels;
//        int height = dm.heightPixels;
//
//        getWindow().setLayout((int) (width*0.6), (int)(height*0.4));
//        TextView tv = (TextView) findViewById(R.id.popup_type);
//        ImageView im = (ImageView) findViewById(R.id.popup_image);
//
//        if(!donation.getImageUrl().isEmpty())
//            Picasso.with(getApplicationContext())
//                    .load(donation.getImageUrl())
//                    .fit()
//                    .error(donation.getType().defaultThumbnail())
//                    .into(im);
//
//        else
//            Picasso.with(getApplicationContext())
//                    .load(donation.getType().defaultThumbnail())
//                    .fit()
//                    .into(im);
//        tv.setText(donation.getType().hebrew());
//
//
//        //TODO when finish pass back the donation and flag if added to cart
//    }


}
