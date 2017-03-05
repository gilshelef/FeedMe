package com.gilshelef.feedme;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gilshe on 2/26/17.
 */
class AdapterManager {
    private Map<String, RecycledBaseAdapter> adapters;
    private static AdapterManager instance;

    private AdapterManager(){
        adapters = new HashMap<>();
    }

    static AdapterManager get() {
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

    void updateDataSourceAll() {
        for(RecycledBaseAdapter a: adapters.values()) {
            a.updateDataSource();
            a.notifyDataSetChanged();
        }
    }

    void setAdapter(String name, RecycledBaseAdapter adapter) {
        adapters.put(name, adapter);
    }

    void clearSelectedViewAll() {
        for(RecycledBaseAdapter a: adapters.values()) {
            a.clearSelectedView();
        }
    }
}
