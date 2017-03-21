package com.gilshelef.feedme.adapters;

/**
 * Created by gilshe on 3/17/17.
 */

public interface Adaptable {
    void updateDataSource();
    void notifyDataSetChanged();
    String getName();
}
