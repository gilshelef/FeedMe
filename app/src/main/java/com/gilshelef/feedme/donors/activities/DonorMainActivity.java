package com.gilshelef.feedme.donors.activities;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
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

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.donors.data.Donor;
import com.gilshelef.feedme.donors.fragments.ProfileDonorFragment;
import com.gilshelef.feedme.nonprofit.data.types.TypeManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gilshe on 3/26/17.
 */

public class DonorMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = DonorMainActivity.class.getSimpleName();
    private Toolbar mAppToolBar;
    private NavigationView navigationView;
    private Map<String, Fragment> mFragments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donors);

        //initialize activity's data
        TypeManager.get();
        Donor.get(this);

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
        TextView businessName = (TextView)  header.findViewById(R.id.business_name);
        businessName.setText(Donor.get(this).getBusinessName());
        TextView contactName = (TextView) header.findViewById(R.id.contact_name);
        contactName.setText(Donor.get(this).getContact());

        //create fragments
        mFragments = new HashMap<>();
        mFragments.put(ProfileDonorFragment.TAG, new ProfileDonorFragment());

        //set first fragment - new donation
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_add_donation));
        navigationView.setCheckedItem(R.id.nav_add_donation);

        //welcome
        String welcomeText = getString(R.string.Hello) + " " + Donor.get(this).getContact();
        Toast.makeText(getApplicationContext(), welcomeText, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_add_donation:
                setFragment(ProfileDonorFragment.TAG);
                getSupportActionBar().setTitle(R.string.add_donation);
                break;
            case R.id.nav_my_donations:
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
}
