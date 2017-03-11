package com.gilshelef.feedme.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.gilshelef.feedme.fragments.ListFragment;
import com.gilshelef.feedme.R;
import com.gilshelef.feedme.fragments.AccountFragment;
import com.gilshelef.feedme.fragments.CartFragment;
import com.gilshelef.feedme.fragments.MapFragment;

import java.util.ArrayList;
import java.util.List;

public class PagerAdapter extends FragmentPagerAdapter {
    private static final int NUM_PAGES = 4;
    private final int[] TABS_TITLES = {R.string.map_tab, R.string.list_tab, R.string.saved_tab, R.string.account_tab};
    private Context mContext;
    private List<Fragment> myPages;

    public PagerAdapter(FragmentManager fm, Context context) {
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