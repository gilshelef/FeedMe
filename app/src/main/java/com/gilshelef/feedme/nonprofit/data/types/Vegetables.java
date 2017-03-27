package com.gilshelef.feedme.nonprofit.data.types;

import com.gilshelef.feedme.R;

/**
 * Created by gilshe on 3/10/17.
 */

class Vegetables extends BaseType {

    @Override
    void setColor() {
        color = 115f;
    }

    @Override
    void setDefaultThumbnail() {
        defaultThumbnail =  R.drawable.ic_vegetable;
    }

    @Override
    void setHebrewName() {
        hebrewName =  "ירקות";

    }

    @Override
    void setName() {
        name = Vegetables.class.getSimpleName();
    }

    @Override
    protected boolean onEqual(Object o) {
        return o instanceof Pasty;
    }
}
