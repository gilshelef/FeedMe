package com.gilshelef.feedme.nonprofit.data.types;

import java.io.Serializable;

/**
 * Created by gilshe on 3/10/17.
 */

public interface Type extends Serializable {
    String hebrew();
    String english();
    int defaultThumbnail();
    float color();
    void build();
}
