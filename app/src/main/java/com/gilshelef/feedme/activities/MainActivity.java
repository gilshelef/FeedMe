package com.gilshelef.feedme.activities;

import android.content.Intent;
import android.os.Bundle;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gilshelef.feedme.Constants;
import com.gilshelef.feedme.R;
import com.gilshelef.feedme.adapters.AdapterManager;
import com.gilshelef.feedme.data.Association;
import com.gilshelef.feedme.data.DataManager;
import com.gilshelef.feedme.data.Donation;
import com.gilshelef.feedme.data.types.TypeManager;
import com.gilshelef.feedme.fragments.AccountFragment;
import com.gilshelef.feedme.fragments.BaseFragment;
import com.gilshelef.feedme.fragments.CartFragment;
import com.gilshelef.feedme.fragments.ListFragment;
import com.gilshelef.feedme.fragments.MapFragment;
import com.gilshelef.feedme.fragments.SaveFragment;
import com.gilshelef.feedme.fragments.ToggleHomeBar;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.util.HashMap;
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by gilshe on 3/13/17.
 */

public class MainActivity extends AppCompatActivity implements
        BaseFragment.OnDetailsListener, ToggleHomeBar, NavigationView.OnNavigationItemSelectedListener, MapFragment.OnSearchListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Toolbar mAppToolBar;
    private Button mMapBtn;
    private Button mListBtn;
    private int shoppingCartNumber = 0;
    private TextView shoppingCartUI = null;
    private Map<String, Fragment> mFragments;
    View mapAndList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View main = findViewById(R.id.main);

        //initialize app's data
        Association.get(this);
        DataManager.get(this);
        TypeManager.get();

        // set toolbar
        mAppToolBar = (Toolbar) main.findViewById(R.id.toolbar);
        setSupportActionBar(mAppToolBar);
        getSupportActionBar().setTitle(R.string.app_name);


        //drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mAppToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        TextView associationName = (TextView)  header.findViewById(R.id.association_name);
        associationName.setText(Association.get(this).getName());

        TextView associationEmail = (TextView) header.findViewById(R.id.association_email);
        associationEmail.setText(Association.get(this).getEmail());


        //create fragments
        mFragments = new HashMap<>();
        mFragments.put(MapFragment.TAG, new MapFragment());
        mFragments.put(ListFragment.TAG, new ListFragment());
        mFragments.put(CartFragment.TAG, new CartFragment());
        mFragments.put(SaveFragment.TAG, new SaveFragment());
        mFragments.put(AccountFragment.TAG, new AccountFragment());
        //TODO add filter fragment

        //set first fragment - map
        setFragment(MapFragment.TAG);

        //set buttons
        mMapBtn = (Button) main.findViewById(R.id.map_fragment_btn);
        mListBtn = (Button) main.findViewById(R.id.list_fragment_btn);
        mMapBtn.setSelected(true);
        mMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(MapFragment.TAG);
                setButtonStyle(mMapBtn, mListBtn);
            }
        });
        mListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(ListFragment.TAG);
                setButtonStyle(mListBtn, mMapBtn);
            }
        });

        mapAndList = main.findViewById(R.id.views_toolbar);

    }

    private void setButtonStyle(Button selected, Button unselected) {
        selected.setSelected(true);
        unselected.setSelected(false);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public void setFragment(String fragmentName) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mFragments.get(fragmentName));
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        final View menuShoppingCart = menu.findItem(R.id.menu_shopping_cart).getActionView();
        shoppingCartUI = (TextView) menuShoppingCart.findViewById(R.id.shopping_cart_text);
        updateShoppingCartNumber(shoppingCartNumber);

        menuShoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(CartFragment.TAG);
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void updateShoppingCartNumber(final int newCartNumber) {
        shoppingCartNumber = newCartNumber;
        if (shoppingCartUI == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(shoppingCartNumber < 0)
                    Log.e(TAG, "ERROR!! number of items in bag is = " + shoppingCartNumber);
                if (shoppingCartNumber <= 0)
                    shoppingCartUI.setVisibility(View.INVISIBLE);
                else {
                    shoppingCartUI.setVisibility(View.VISIBLE);
                    shoppingCartUI.setText(Integer.toString(shoppingCartNumber));
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter:
                moveToFilters(); // TODO change to filter fragment
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void moveToFilters() {
        Toast.makeText(getApplicationContext(), "Filters fragment", LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.DETAILS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //get data from result
                String donationId = data.getStringExtra(Constants.DONATION_ID);
                Donation.State state = Donation.State.valueOf(data.getStringExtra(Constants.DONATION_STATE));
                boolean inCart = data.getBooleanExtra(Constants.IN_CART, false);

                // update save events
                if (state == Donation.State.SAVED)
                    DataManager.get(this).saveEvent(donationId);
                else
                    DataManager.get(this).unSaveEvent(donationId);

                //update cart events
                int delta;
                if (inCart)
                    delta = DataManager.get(this).addToCartEvent(donationId);
                else delta = DataManager.get(this).removeFromCartEvent(donationId);
                updateShoppingCartNumber(shoppingCartNumber + delta);

                AdapterManager.get().updateDataSourceAll();
            }
        }
        else if (requestCode == MapFragment.PLACE_AUTOCOMPLETE_REQUEST_CODE)
            mFragments.get(MapFragment.TAG).onActivityResult(requestCode, resultCode, data);

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

    @Override
    public void drawHomeBar(boolean toDraw) {
        if(toDraw)
            mapAndList.setVisibility(View.VISIBLE);
        else
            mapAndList.setVisibility(View.GONE);

    }

    @Override
    public void drawAppBar(boolean toDraw) {
        if(getSupportActionBar() == null)
            return;
        if(toDraw)
            getSupportActionBar().show();
        else
            getSupportActionBar().hide();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_home:
                setFragment(MapFragment.TAG);
                break;
            case R.id.nav_saved:
                setFragment(SaveFragment.TAG);
                break;
            case R.id.nav_my_donations:
                break;
            case R.id.nav_account:
                setFragment(AccountFragment.TAG);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSearch() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(this);
            startActivityForResult(intent, MapFragment.PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }
}
