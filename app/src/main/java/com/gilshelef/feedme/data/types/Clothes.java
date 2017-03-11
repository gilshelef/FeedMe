package com.gilshelef.feedme.data.types;

import com.gilshelef.feedme.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * Created by gilshe on 3/10/17.
 */

public class Clothes extends BaseType {


    @Override
    void setColor() {
        color = BitmapDescriptorFactory.HUE_RED;
    }

    @Override
    void setDefaultThumbnail() {
        defaultThumbnail =  R.drawable.ic_clothes;
    }

    @Override
    void setHebrewName() {
        hebrewName = "בגדים";
    }

    @Override
    void setName() {
        name = "Clothes";
    }


    @Override
    protected boolean onEqual(Object o) {
        return o instanceof Clothes;
    }
}
