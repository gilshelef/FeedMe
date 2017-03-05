package com.gilshelef.feedme;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class PagerAdapter extends FragmentPagerAdapter {
    private static final int NUM_PAGES = 4;
    private final int[] TABS_TITLES = {R.string.map_tab, R.string.list_tab, R.string.saved_tab, R.string.account_tab};
    private Context mContext;

    PagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.mContext = context;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new MapFragment();
            case 1:
                return new ListFragment();
            case 2:
                return new CartFragment();
            case 3:
                return new AccountFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position < TABS_TITLES.length)
            return mContext.getString(R.string.account_tab);
        else return mContext.getString(R.string.app_name);
    }
}