package com.gilshelef.feedme.nonprofit.data.types;

import com.gilshelef.feedme.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * Created by gilshe on 3/10/17.
 */

public class Other extends BaseType{

    public static final String TAG = "אחר";

    @Override
    void setColor() {
        color = BitmapDescriptorFactory.HUE_AZURE;
    }

    @Override
    void setDefaultThumbnail() {
        defaultThumbnail = R.drawable.ic_placeholder;
    }

    @Override
    void setHebrewName() {
        hebrewName = "אחר";
    }

    @Override
    void setName() {
        name = Other.class.getSimpleName();
    }


    @Override
    protected boolean onEqual(Object o) {
        return o instanceof Other;

    }
}
