package com.gilshelef.feedme.nonprofit.data.types;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by gilshe on 3/10/17.
 */

public interface Type extends Serializable {
    String K_HEBREW = "hebrewName";
    String OTHER = "אחר";
    String K_COLOR = "color";
    String K_THUMBNAIL = "defaultThumbnail";
    String K_NAME = "name";

    String hebrew();
    String english();
    int defaultThumbnail();
    float color();
    void build();
    Map<String,Object> toMap();
}
