package com.gilshelef.feedme.util;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by gilshe on 4/28/17.
 */

public class DataBaseHelper {
    private static final String REFRENCE = "donation";
    private DatabaseReference mDatabase;

    public DataBaseHelper(){
        // Write a message to the database
        mDatabase = FirebaseDatabase.getInstance().getReference();

    }






}
