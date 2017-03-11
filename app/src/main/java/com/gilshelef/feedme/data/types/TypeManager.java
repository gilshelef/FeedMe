package com.gilshelef.feedme.data.types;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gilshe on 3/10/17.
 */

public class TypeManager {
    private static final String TAG = TypeManager.class.getSimpleName();
    private static TypeManager instance = null;
    private Map<String, Type> dataSources = new HashMap<>();
    public static final String OTHER_DONATION = "Other";

    void register(String hebrewName, Type type) {
        dataSources.put(hebrewName, type);
    }

    public static TypeManager get() {
        if (instance == null) {
            synchronized (TypeManager.class) {
                if (instance == null) {
                    instance = new TypeManager();
                }
            }
        }
        return instance;
    }

    private TypeManager(){
        TypeRegistry.registerAll(this);
        Log.d(TAG, "ALL REGISTERED");
    }

    public Type getType(String type) {
        if(dataSources.containsKey(type))
            return dataSources.get(type);
        return dataSources.get(OTHER_DONATION);
    }

}