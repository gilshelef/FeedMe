package com.gilshelef.feedme.nonprofit.adapters;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gilshe on 2/26/17.
 */
public class AdapterManager {
    private static final String TAG = AdapterManager.class.getSimpleName();
    private Map<String, Adaptable> adapters;
    private static AdapterManager instance;

    private AtomicInteger updateCounter;
    private AdapterManager(){
        adapters = new HashMap<>();
        updateCounter = new AtomicInteger(0);
    }

    public static AdapterManager get() {
        if (instance == null) {
            synchronized (AdapterManager.class) {
                if (instance == null)
                    return build();
            }
        }
        return instance;
    }

    private static AdapterManager build() {
        instance = new AdapterManager();
        return instance;
    }

    public void updateDataSourceAll() {
        Log.d(TAG, "update data source: " + updateCounter.incrementAndGet());
        for(Adaptable a: adapters.values()) {
            a.updateDataSource();
            a.notifyDataSetChanged();
        }
    }

    public void registerAdapter(Adaptable adapter) {
        adapters.put(adapter.getName(), adapter);
    }

    public void updateDataSourceAll(String exclude) {
        Log.d(TAG, "update data source: " + updateCounter.incrementAndGet());
        for(Adaptable a: adapters.values()) {
            if(a.getName().equals(exclude))
                continue;
            a.updateDataSource();
            a.notifyDataSetChanged();
        }
    }

}
