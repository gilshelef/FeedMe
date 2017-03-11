package com.gilshelef.feedme.data.types;

import com.gilshelef.feedme.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * Created by gilshe on 3/10/17.
 */

public class Other extends BaseType{

    @Override
    void setColor() {
        color = BitmapDescriptorFactory.HUE_AZURE;
    }

    @Override
    void setDefaultThumbnail() {
        defaultThumbnail = R.drawable.placeholder;
    }

    @Override
    void setHebrewName() {
        hebrewName = "תרומה";
    }

    @Override
    void setName() {
        name = "Donation";
    }


    @Override
    protected boolean onEqual(Object o) {
        return o instanceof Other;

    }
}
