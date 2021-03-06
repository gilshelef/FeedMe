package com.gilshelef.feedme.donors.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.gilshelef.feedme.R;
import com.gilshelef.feedme.donors.data.DonationsManager;
import com.gilshelef.feedme.donors.data.Donor;
import com.gilshelef.feedme.donors.fragments.AddDonationFragment;
import com.gilshelef.feedme.donors.fragments.MyDonationsFragment;
import com.gilshelef.feedme.donors.fragments.ProfileDonorFragment;
import com.gilshelef.feedme.nonprofit.activities.DetailsActivity;
import com.gilshelef.feedme.nonprofit.adapters.AdapterManager;
import com.gilshelef.feedme.nonprofit.data.Donation;
import com.gilshelef.feedme.nonprofit.data.types.TypeManager;
import com.gilshelef.feedme.nonprofit.fragments.BaseFragment;
import com.gilshelef.feedme.nonprofit.fragments.OnCounterChangeListener;
import com.gilshelef.feedme.util.ImagePicker;
import com.gilshelef.feedme.util.Logger;
import com.gilshelef.feedme.util.OnInfoUpdateListener;
import com.gilshelef.feedme.util.Util;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

/**
 * Created by gilshe on 3/26/17.
 */

public class DonorMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AddDonationFragment.OnCameraEvent, OnCounterChangeListener, OnInfoUpdateListener, BaseFragment.OnDetailsListener {

    private static final String TAG = DonorMainActivity.class.getSimpleName();
    public static final String ACTION_REMOVE_DONATION = "actionRemoveDonation";
    public static final String ACTION_UPDATE_TIME = "actionUpdateTime";
    private Toolbar mAppToolBar;
    private NavigationView navigationView;
    private Map<String, Fragment> mFragments;
    private TextView businessName;
    private TextView contactName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donors);
        Fabric.with(this, new Crashlytics());

        Util.loadPreference(this);

        //initialize activity's data
        TypeManager.get();
        final Donor donor = Donor.get(this);
        DonationsManager.get(this);
        Logger.get(this);

        View main = findViewById(R.id.main);

        // set toolbar
        mAppToolBar = (Toolbar) main.findViewById(R.id.toolbar);
        setSupportActionBar(mAppToolBar);

        //drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mAppToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //drawer header
        View header = navigationView.getHeaderView(0);
        businessName = (TextView)  header.findViewById(R.id.business_name);
        contactName = (TextView) header.findViewById(R.id.contact_name);

        //create fragments
        mFragments = new HashMap<>();
        mFragments.put(ProfileDonorFragment.TAG, new ProfileDonorFragment());
        mFragments.put(AddDonationFragment.TAG, new AddDonationFragment());
        mFragments.put(MyDonationsFragment.TAG, new MyDonationsFragment());

        //set first fragment - new donation
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_add_donation));
        navigationView.setCheckedItem(R.id.nav_add_donation);

        //handle time related actions
        String action = getIntent().getAction();
        if(action != null)
            handleAction(action);

        //welcome
        else {
            String welcomeText = getString(R.string.Hello) + " " + Donor.get(this).getContactInfo();
            Toast.makeText(getApplicationContext(), welcomeText, Toast.LENGTH_SHORT).show();
        }

        updateViewCounters();

        onBusinessChange(donor.getBusinessName());
        onContactChange(donor.getContactInfo());

    }

    private void handleAction(String action) {
        Bundle bundle = getIntent().getExtras();
        String donationId = bundle.getString(Donation.K_ID);
        if(donationId == null) return;

        DonationsManager instance = DonationsManager.get(this);
        if(!instance.hasDonation(donationId))
            return;

        if(action.equals(ACTION_REMOVE_DONATION)) {
            Donor.get(this).updateDonationCount(this, -1);
            instance.returnDonation(donationId);
            Toast.makeText(this, "תרומתך ירדה מהמאגר", Toast.LENGTH_LONG).show();
        }

        else if(action.equals(ACTION_UPDATE_TIME)){
            String calendarStr = bundle.getString(Donation.K_CALENDAR);
            instance.updateDonationInformation(this, donationId, DonationsManager.NO_UPDATE, calendarStr);
            Toast.makeText(this, "זמן התרומה עודכן", Toast.LENGTH_LONG).show();
        }

        AdapterManager.get().updateDataSourceAll();

    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_add_donation:
                setFragment(AddDonationFragment.TAG);
                getSupportActionBar().setTitle(R.string.add_donation);
                break;
            case R.id.nav_my_donations:
                setFragment(MyDonationsFragment.TAG);
                getSupportActionBar().setTitle(R.string.my_donations);
                break;
            case R.id.nav_profile:
                setFragment(ProfileDonorFragment.TAG);
                getSupportActionBar().setTitle(R.string.profile);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setFragment(String fragmentName) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mFragments.get(fragmentName));
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onCameraEvent() {
        ImagePicker.pickImage(this);
    }

    @Override
    public void updateViewCounters() {
        int donationsCount = DonationsManager.get(DonorMainActivity.this).getAll().size();
        setMenuCounter(R.id.nav_my_donations, donationsCount);
        ((OnCounterChangeListener)mFragments.get(ProfileDonorFragment.TAG)).updateViewCounters();
    }

    private void setMenuCounter(@IdRes int itemId, int count) {
        TextView view = (TextView) navigationView.getMenu().findItem(itemId).getActionView();
        view.setText(count > 0 ? String.valueOf(count) : null);
    }

    @Override
    public void onContactChange(String contact) {
        contactName.setText(contact);
    }

    @Override
    public void onBusinessChange(String businessName) {
        this.businessName.setText(businessName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DetailsActivity.DETAILS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                DonationsManager manager = DonationsManager.get(this);
                //get data from result
                String donationId = data.getStringExtra(Donation.K_ID);
                String description = data.getStringExtra(Donation.K_DESCRIPTION);
                String calenderStr = data.getStringExtra(Donation.K_CALENDAR);
                boolean removed = data.getBooleanExtra(Donation.K_REMOVE, false);

                if(removed) {
                    Donor.get(this).updateDonationCount(this, -1);
                    manager.returnDonation(donationId);
                    Logger.get(this).returnDonation(donationId);
                    Toast.makeText(this, R.string.remove_donation_successfully, Toast.LENGTH_LONG).show();
                    Util.unScheduleAlarm(this, donationId);
                    updateViewCounters();
                }
                else manager.updateDonationInformation(this, donationId, description, calenderStr);
                AdapterManager.get().updateDataSourceAll();
            }
        }

        else if (requestCode == ImagePicker.PICK_IMAGE_REQUEST_CODE){
            if(resultCode != RESULT_CANCELED) {
                Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                if (bitmap != null)
                    ((OnImageResult) mFragments.get(AddDonationFragment.TAG)).onImageResult(bitmap);
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onDetails(View v, Donation donation) {
        Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putParcelable(DetailsActivity.EXTRA_DONATION, donation);
        mBundle.putBoolean(DetailsActivity.EXTRA_IS_DONOR, true);
        intent.putExtras(mBundle);
        if(v != null) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, v, "profile");
            startActivityForResult(intent, DetailsActivity.DETAILS_REQUEST_CODE, options.toBundle());
        }
        else startActivityForResult(intent, DetailsActivity.DETAILS_REQUEST_CODE);
    }

}
