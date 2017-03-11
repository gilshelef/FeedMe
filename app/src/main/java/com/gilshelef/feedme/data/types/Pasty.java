package com.gilshelef.feedme.data.types;

import com.gilshelef.feedme.R;

/**
 * Created by gilshe on 3/10/17.
 */

public class Pasty extends BaseType {

    @Override
    void setColor() {
        color = 45f;
    }

    @Override
    void setDefaultThumbnail() {
        defaultThumbnail = R.drawable.ic_cake;
    }

    @Override
    void setHebrewName() {
        hebrewName = "מאפים";
    }

    @Override
    void setName() {
        name = "Pastry";
    }

    @Override
    protected boolean onEqual(Object o) {
        return o instanceof Pasty;
    }

}
