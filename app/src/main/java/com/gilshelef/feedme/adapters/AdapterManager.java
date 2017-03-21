package com.gilshelef.feedme.adapters;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gilshe on 2/26/17.
 */
public class AdapterManager {
    private Map<String, Adaptable> adapters;
    private static AdapterManager instance;

    private AdapterManager(){
        adapters = new HashMap<>();
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
        for(Adaptable a: adapters.values()) {
            a.updateDataSource();
            a.notifyDataSetChanged();
        }
    }

    public void setAdapter(Adaptable adapter) {
        adapters.put(adapter.getName(), adapter);
    }

    public void updateDataSourceAll(String exclude) {
        for(Adaptable a: adapters.values()) {
            if(a.getName().equals(exclude))
                continue;
            a.updateDataSource();
            a.notifyDataSetChanged();
        }
    }

}
