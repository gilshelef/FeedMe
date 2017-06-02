package com.gilshelef.feedme.nonprofit.activities;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.donors.fragments.TimePickerFragment;
import com.gilshelef.feedme.launcher.RegistrationHandler;
import com.gilshelef.feedme.nonprofit.data.Donation;
import com.gilshelef.feedme.util.Constants;
import com.gilshelef.feedme.util.Util;
import com.google.android.gms.maps.model.LatLng;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by gilshe on 3/4/17.
 */
public class DetailsActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    private static final String TAG = DetailsActivity.class.getSimpleName();
    public static final String EXTRA_DONATION = "donation";
    public static final String EXTRA_IS_DONOR = "donor_activity";
    private static final String UNNAMED_ROAD = "Unnamed Road";
    public static final int DETAILS_REQUEST_CODE = Constants.REQUEST_2;
    private final View.OnClickListener exitListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendDataAndFinish();
        }
    };
    private final View.OnClickListener callListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(Constants.TEL_PREFIX + donation.getPhone()));
            startActivity(intent);
        }
    };

    private final View.OnClickListener addToCartListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            donation.setInCart(!donation.getInCart());
            styleCartBtn();
        }
    };

    private Donation donation;
    private ImageButton exit;
    private LikeButton save;
    private ImageView thumbnail;
    private TextView business;
    private TextView description;
    private View contactBtn;
    private TextView contactInfo;
    private TextView timeInfo;
    private View timeBtn;
    private View addressBtn;
    private TextView addressInfo;
    private Button addToCartBtn;
    private ImageButton editDescription;
    private CheckBox takenCheckBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deatils);
        Intent intent = getIntent();
        donation = intent.getParcelableExtra(EXTRA_DONATION);
        boolean isDonor = intent.getBooleanExtra(EXTRA_IS_DONOR, false);
        extractContainers();

        //thumbnail
        if(!donation.getImageUrl().isEmpty())
            Picasso.with(getApplicationContext())
                    .load(donation.getImageUrl())
                    .placeholder(donation.getType().defaultThumbnail())
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
        String contactStr = donation.getContactInfo() + " " + donation.getPhone();
        contactInfo.setText(contactStr);
        String text = String.format(getString(R.string.time_to_pick), donation.calenderToString());
        timeInfo.setText(text);

        if(donation.isSaved())
            save.setLiked(true);

        if(donation.isOwned() || isDonor) {
            save.setVisibility(View.GONE);
            addToCartBtn.setVisibility(View.GONE);
            if(donation.isOwned()) findViewById(R.id.taken_container).setVisibility(View.VISIBLE);
        }
        else styleCartBtn();

        if(isDonor) {
            editDescription.setVisibility(View.VISIBLE);
            editDescription.setOnClickListener(descriptionListener);
            timeBtn.setOnClickListener(timeListener);
        }
        else contactBtn.setOnClickListener(callListener);

        new ListenerTask().execute();
    }

    private void styleCartBtn() {
        if(donation.getInCart())
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
        intent.putExtra(Donation.K_ID, donation.getId());
        intent.putExtra(Donation.K_STATE, donation.getState().name());
        intent.putExtra(Donation.K_CART, donation.getInCart());
        intent.putExtra(Donation.K_DESCRIPTION, donation.getDescription());
        intent.putExtra(Donation.K_CALENDAR, donation.calenderToString());
        intent.putExtra(Donation.K_TAKEN, takenCheckBox.isChecked());
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
        editDescription = (ImageButton) findViewById(R.id.edit_description);

        //contact
        contactBtn = findViewById(R.id.contact_container);
        contactInfo = (TextView) findViewById(R.id.details_contact_info);

        //time
        timeInfo = (TextView) findViewById(R.id.details_time_info);
        timeBtn = findViewById(R.id.time_container);

        //address
        addressBtn = findViewById(R.id.address_container);
        addressInfo = (TextView) findViewById(R.id.details_address_info);

        //taken
        takenCheckBox = (CheckBox) findViewById(R.id.mark_taken_checkbox);

        addToCartBtn = (Button) findViewById(R.id.details_add_to_cart);

    }

    @Override
    public void onBackPressed () {
        sendDataAndFinish();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        donation.calendar = cal;
        timeInfo.setText(donation.calenderToString());
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
            addToCartBtn.setOnClickListener(addToCartListener);

            Geocoder geocoder;
            List<Address> addresses;
            Locale aLocale = new Locale.Builder().setLanguage(Constants.HEBREW).build();
            geocoder = new Geocoder(getApplicationContext(), aLocale);
            LatLng latlng = donation.getPosition();
            String addressDetails = "";
            try {
                addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1); // Here 1 represent max position result to returned, by documents it recommended 1 to 5
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

    private View.OnClickListener timeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentManager fm = getSupportFragmentManager();
            DialogFragment dialog = new TimePickerFragment();
            dialog.show(fm, "dialog");
        }
    };


    private final View.OnClickListener descriptionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
            TextView title = Util.buildTitleView(DetailsActivity.this, getString(R.string.donation_description));
            builder.setCustomTitle(title);

            final EditText input = Util.buildInputView(DetailsActivity.this, getString(R.string.description_hint));
            builder.setView(input);

            builder.setPositiveButton(R.string.update,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String newDescription = input.getText().toString();
                            if (!RegistrationHandler.isEmpty(input)) {
                                donation.setDescription(newDescription);
                                description.setText(newDescription);
                            }
                        }
                    });

            builder.setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            builder.show();
        }
    };

    private void launchNavigationApp() {
        LatLng latLng = donation.getPosition();
        String format = "waze://?ll="+latLng.latitude+", " +latLng.longitude + "&navigate=yes";
        Uri uri = Uri.parse(format);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
