package com.gilshelef.feedme;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity  implements BaseFragment.OnSelectedEvent, View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final int[] TABS_TITLES = {R.string.map_tab, R.string.list_tab, R.string.saved_tab, R.string.account_tab};

    private Toolbar mToolBar;
    private TabLayout mTabLayout;
    private CustomViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private Menu mMenu;
    private FloatingActionButton mFAB;
    private SelectedDonationHandler mSelectedHandler;
    private CoordinatorLayout mCoordinator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Association.get(this);
        DataManager.get(this);
        mSelectedHandler = new SelectedDonationHandler();

        // set toolbar
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mToolBar.setTitle(getTitle(0));
        setSupportActionBar(mToolBar);
        mToolBar.setNavigationIcon(R.drawable.donate_icon);

        // set tabs
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.map_icon));
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.list_icon));
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.heart_icon));
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.account_icon));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // set viewPager and pageAdapter
        mViewPager = (CustomViewPager) findViewById(R.id.pager);
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(),this);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.lightPrimaryColor);
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                mToolBar.setTitle(getTitle(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.iconsUnselected);
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //FAB
        mFAB = (FloatingActionButton) findViewById(R.id.fab);
        mCoordinator = (CoordinatorLayout) findViewById(R.id.coordinator);
        mFAB.setOnClickListener(this);
    }


    private int getTitle(int position) {
        if(position < TABS_TITLES.length)
            return TABS_TITLES[position];
        else return R.string.app_name;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.association_menu, menu);
        this.mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter:
                Toast.makeText(getApplicationContext(), "Filter Selected", Toast.LENGTH_LONG).show();
                return true;
            case R.id.item:
                Toast.makeText(getApplicationContext(), "Item Selected", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSelectedEvent(String donationId, boolean selected) {
        if(selected)
            mSelectedHandler.selectedEvent(donationId);
        else mSelectedHandler.unSelectEvent(donationId);

    }

    @Override
    public void onClick(View v) { // fab click

        Snackbar snackbar = Snackbar
                .make(mCoordinator, "Donation taken :)", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSelectedHandler.unSelectEventAll();
                        Snackbar returned = Snackbar.make(mCoordinator, "Donations returned!", Snackbar.LENGTH_SHORT);
                        returned.show();
                    }});

        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if(event != DISMISS_EVENT_ACTION) { // take action!
                    if (!mSelectedHandler.isEmpty()) {
                        // TODO notify data base!
                        // TODO add to profile
                        // TODO add to service for timeout check
                        DataManager.get(getApplicationContext()).removeAll(mSelectedHandler.getSelected());
                    }
                }
                mSelectedHandler.end();
            }

        });
        snackbar.show();
    }

    private class SelectedDonationHandler {

        private int count;
        private Set<String> selected;

        SelectedDonationHandler(){
            count = 0;
            selected = new HashSet<>();

        }

        void selectedEvent(String donationId) {
            if(isEmpty()) // first event
                start();

            count++;
            selected.add(donationId);
            mToolBar.setTitle(getCount());

        }

        void unSelectEvent(String donationId) {
            if(isEmpty()) //unselected when there are no selected item!
                Log.e(TAG, "unselected event when there are no selected item!");

            count--;
            selected.remove(donationId);
            mToolBar.setTitle(getCount());

            if(isEmpty())
                mSelectedHandler.end();

        }

        String getCount() {
            return count+"";
        }

        boolean isEmpty() {
            return count <= 0; // should always be >=0
        }

        Set<String> getSelected() {
            return selected;
        }

        void start() {
            mViewPager.setPagingEnabled(false);
            Animation showFAB = AnimationUtils.loadAnimation(getApplication(), R.anim.show_fab);
            mFAB.startAnimation(showFAB); // display fab
            mFAB.setVisibility(View.VISIBLE);
            mTabLayout.setVisibility(View.GONE); // hide tabs
            mViewPager.setClickable(false);
            mMenu.findItem(R.id.filter).setEnabled(false).setVisible(false); // hide filter
        }

        void end() {
            mViewPager.setPagingEnabled(true);
            Animation hideFAB = AnimationUtils.loadAnimation(getApplication(), R.anim.hide_fab);
            mFAB.startAnimation(hideFAB); // hide fab
            mTabLayout.setVisibility(View.VISIBLE);
            mMenu.findItem(R.id.filter).setEnabled(true).setVisible(true); // show filter
            mToolBar.setTitle(getTitle(mTabLayout.getSelectedTabPosition()));
            clear();
            AdapterManager.get().clearSelectedViewAll();
        }

        void clear() {
            count = 0;
            selected.clear();
        }

        void unSelectEventAll() {
            DataManager.get(getApplicationContext()).returnAll(getSelected());
            clear();
        }

    }
}
