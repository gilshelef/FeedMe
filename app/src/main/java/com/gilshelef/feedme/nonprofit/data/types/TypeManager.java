package com.gilshelef.feedme.nonprofit.data.types;

import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

    public List<Type> getAll() {
        List<Type> types = new ArrayList<>();
        types.addAll(dataSources.values());
        return types;
    }

    public Type getTypeFromString(String type) {
        return dataSources.containsKey(type) ? dataSources.get(type) : null;
    }

    public static class TypeComparator implements Comparator<Type> {
        private final Type donationType;

        public TypeComparator(Type donationType) {
            this.donationType = donationType;
        }

        @Override
        public int compare(Type t1, Type t2) {
            if(t1.equals(donationType))
                return -1;
            if(t2.equals(donationType))
                return 1;
            if(t1 instanceof Other)
                return 1;
            if(t2 instanceof Other)
                return -1;

            return 0;
        }
    }
}