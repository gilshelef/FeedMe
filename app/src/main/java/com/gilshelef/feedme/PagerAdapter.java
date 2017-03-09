package com.gilshelef.feedme;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

class PagerAdapter extends FragmentPagerAdapter {
    private static final int NUM_PAGES = 4;
    private final int[] TABS_TITLES = {R.string.map_tab, R.string.list_tab, R.string.saved_tab, R.string.account_tab};
    private Context mContext;
    private List<Fragment> myPages;

    PagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.mContext = context;
        myPages = new ArrayList<>();
        myPages.add(new MapFragment());
        myPages.add(new ListFragment());
        myPages.add(new CartFragment());
        myPages.add(new AccountFragment());
    }

    @Override
    public Fragment getItem(int position) {
       if(position < myPages.size())
           return myPages.get(position);
        return null;
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