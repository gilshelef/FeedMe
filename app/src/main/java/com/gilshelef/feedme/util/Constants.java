package com.gilshelef.feedme.util;

/**
 * Created by gilshe on 3/16/17.
 */

public class Constants {

    //map fragment
    public static final int PERMISSIONS_REQUEST_LOCATION = 1;
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;

    //car activity
    public static final int CARTS_REQUEST_CODE = 4;

    public static final String TEL_PREFIX = "tel:";

    // details activity
    public static final int DETAILS_REQUEST_CODE = 3;
    public static final String DONATION_STATE = "state";
    public static final String IN_CART = "cart";
    public static final String DONATION_DESCRIPTION = "description";

    public static final String DONATION_ID = "id";
    public static final String DATE_FORMAT = "HH:mm dd.MM.yyyy";
    public static final String DONATION_TIME = "time";

    //db
    public static final String DB_NON_PROFIT_KEY = "non_profit";

    public static final String DB_DONOR_KEY = "donor";
    public static final String DB_DONOR_COUNT_KEY = "donationCount";

    public static final String DB_DONATION_KEY = "donation";
    public static final String DB_DONATION_STATE_KEY = DONATION_STATE;
    public static final String DB_DONATION_DESC_KEY = "description";
    public static final String DB_DONATION_CAL_KEY = "calendar";
    public static final String DB_IMAGE_KEY = "imageUrl";
}
