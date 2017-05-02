package com.gilshelef.feedme.donors.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.donors.data.DonationsManager;
import com.gilshelef.feedme.donors.data.Donor;
import com.gilshelef.feedme.donors.fragments.AddDonationFragment;
import com.gilshelef.feedme.donors.fragments.MyDonationsFragment;
import com.gilshelef.feedme.donors.fragments.ProfileDonorFragment;
import com.gilshelef.feedme.nonprofit.activities.DetailsActivity;
import com.gilshelef.feedme.nonprofit.data.Donation;
import com.gilshelef.feedme.nonprofit.data.types.TypeManager;
import com.gilshelef.feedme.nonprofit.fragments.BaseFragment;
import com.gilshelef.feedme.nonprofit.fragments.OnCounterChangeListener;
import com.gilshelef.feedme.util.Constants;
import com.gilshelef.feedme.util.OnInfoUpdateListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gilshe on 3/26/17.
 */

public class DonorMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AddDonationFragment.OnCameraEvent, OnCounterChangeListener, OnInfoUpdateListener, BaseFragment.OnDetailsListener {

    private static final String TAG = DonorMainActivity.class.getSimpleName();
    private Toolbar mAppToolBar;
    private NavigationView navigationView;
    private Map<String, Fragment> mFragments;
    private TextView businessName;
    private TextView contactName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donors);

        //initialize activity's data
        TypeManager.get();
        Donor.get(this);
        DonationsManager.get(this);

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
        onBusinessChange(Donor.get(this).getBusinessName());
        businessName.setText(Donor.get(this).getBusinessName());
        contactName = (TextView) header.findViewById(R.id.contact_name);
        onContactChange(Donor.get(this).getContactInfo());

        //create fragments
        mFragments = new HashMap<>();
        mFragments.put(ProfileDonorFragment.TAG, new ProfileDonorFragment());
        mFragments.put(AddDonationFragment.TAG, new AddDonationFragment());
        mFragments.put(MyDonationsFragment.TAG, new MyDonationsFragment());

        //set first fragment - new donation
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_add_donation));
        navigationView.setCheckedItem(R.id.nav_add_donation);

        //welcome
        String welcomeText = getString(R.string.Hello) + " " + Donor.get(this).getContactInfo();
        Toast.makeText(getApplicationContext(), welcomeText, Toast.LENGTH_LONG).show();
        updateViewCounters();
        Log.d("BUG", "onCreate");
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d("BUG", "onStart");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("BUG", "onResume");
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
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(takePictureIntent, AddDonationFragment.REQUEST_IMAGE_CAPTURE);
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
        if (requestCode == Constants.DETAILS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //get data from result
                String donationId = data.getStringExtra(Constants.DONATION_ID);
                String description = data.getStringExtra(Constants.DONATION_DESCRIPTION);
                String calenderStr = data.getStringExtra(Constants.DONATION_TIME);

                DonationsManager.get().update(donationId, description, calenderStr);
            }
        }
        else if (requestCode == AddDonationFragment.REQUEST_IMAGE_CAPTURE)
            mFragments.get(AddDonationFragment.TAG).onActivityResult(requestCode, resultCode, data);


    }
    @Override
    public void onBackPressed () {
    }

    @Override
    public void onDetails(View v, Donation donation) {
        Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putParcelable(DetailsActivity.EXTRA_DONATION, donation);
        intent.putExtras(mBundle);
        if(v != null) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, v, "profile");
            startActivityForResult(intent, Constants.DETAILS_REQUEST_CODE, options.toBundle());
        }
        else startActivityForResult(intent, Constants.DETAILS_REQUEST_CODE);
    }

}
