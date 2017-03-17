package com.gilshelef.feedme.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.data.Association;
import com.gilshelef.feedme.data.DataManager;
import com.gilshelef.feedme.data.types.TypeManager;
import com.gilshelef.feedme.fragments.BaseFragment;
import com.gilshelef.feedme.fragments.CartFragment;
import com.gilshelef.feedme.fragments.ListFragment;
import com.gilshelef.feedme.fragments.MapFragment;

import java.util.HashMap;
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by gilshe on 3/13/17.
 */

public class MainActivity extends AppCompatActivity implements BaseFragment.OnCartEvent {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Toolbar mAppToolBar;
    private Button mMapBtn;
    private Button mListBtn;
    private int shoppingCartNumber = 0;
    private TextView shoppingCartUI = null;
    private Map<String, Fragment> mFragments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize app's data
        Association.get(this);
        DataManager.get(this);
        TypeManager.get();

        // set toolbar
        mAppToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mAppToolBar);
        getSupportActionBar().setTitle(R.string.app_name);

        //create fragments
        mFragments = new HashMap<>();
        mFragments.put(MapFragment.TAG, new MapFragment());
        mFragments.put(ListFragment.TAG, new ListFragment());
        mFragments.put(CartFragment.TAG, new CartFragment());
        //TODO add filter fragment

        //set first fragment - map
        setFragment(MapFragment.TAG);

        //set buttons
        mMapBtn = (Button) findViewById(R.id.map_fragment_btn);
        mListBtn = (Button) findViewById(R.id.list_fragment_btn);
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

    }

    private void setButtonStyle(Button selected, Button unselected) {
        selected.setSelected(true);
        unselected.setSelected(false);
    }

    public void setFragment(String fragmentName) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mFragments.get(fragmentName));
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
                moveToShoppingCart();
                //TODO change to set fragment
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

    private void moveToShoppingCart() {
        //TODO move to shopping cart fragment
        Toast.makeText(getApplicationContext(), "Shopping cart fragment", LENGTH_SHORT).show();
    }

    @Override
    public void addToCartEvent(String donationId) {
        boolean added = DataManager.get(this).addToCartEvent(donationId);
        if(added)
            updateShoppingCartNumber(shoppingCartNumber+1);
    }

    @Override
    public void removeFromCartEvent(String donationId) {
        boolean removed = DataManager.get(this).removeFromCartEvent(donationId);
        if(removed)
            updateShoppingCartNumber(shoppingCartNumber-1);
    }
}
