package com.gilshelef.feedme;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by gilshe on 2/25/17.
 */
 class DataManager {

    private static Map<String, Donation> donations; // holding only available and saved items
    private static DataManager instance;
    private static final String AVAILABLE = "0";
    private static boolean initialized;

    private DataManager(){
        donations = new LinkedHashMap<>();
        initialized = false;
    }

    static DataManager get(Context context) {
        if (instance == null) {
            synchronized (DataManager.class) {
                if (instance == null)
                    return build(context);
            }
        }
        return instance;
    }

    private static DataManager build(Context context) {
        instance = new DataManager();
        new FetchDataTask(context, null).execute();
        return instance;
    }

    private static void getDonationsFromFile(Context context){
        initialized = true;
        try {
            // Load data
            String jsonString = loadJsonFromAsset("donations.json", context);
            JSONObject json = new JSONObject(jsonString);
            JSONArray donations = json.getJSONArray("donations");

            // Get Donation objects from data
            for(int i = 0; i < donations.length(); i++){
                JSONObject obj = donations.getJSONObject(i);
                Donation donation = new Donation();
                donation.type = obj.getString("type");
                donation.description = obj.getString("description");
                donation.phone = obj.getString("phone");
                donation.firstName = obj.getString("firstName");
                donation.lastName = obj.getString("lastName");
                donation.imageUrl = obj.getString("imageUrl");
                donation.date = obj.getString("date");
                donation.setId(obj.getString("id"));

                String state = obj.getString("state");
                if(state.equals(AVAILABLE))
                    donation.setState(Donation.State.AVAILABLE);
                else donation.setState(Donation.State.SAVED);

                Location l = new Location("donation's location");
                l.setLatitude(obj.getDouble("latitude"));
                l.setLongitude(obj.getDouble("longitude"));
                donation.location = l;
                donation.defaultImage = getImageByType(donation.type);
                String id = donation.getId();
                DataManager.donations.put(id, donation);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String loadJsonFromAsset(String filename, Context context) {
        String json;

        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        }
        catch (java.io.IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
    }

    private static int getImageByType(String type) {
        switch (type){
            case "ירקות":
                return R.drawable.ic_vegetable;
            case "מאפים":
                return R.drawable.ic_cake;
            case "בגדים":
                return R.drawable.ic_clothes;
            default:
                return R.drawable.placeholder;
        }
    }

    List<Donation> getSaved(Context context) {
        final List<Donation> saved = new ArrayList<>();

        OnResult callback = new OnResult(){
            @Override
            public void onResult() {
                for(Donation d: donations.values())
                    if(d.isSaved())
                        saved.add(d);
            }
        };

        if(!initialized)
            new FetchDataTask(context, callback).execute();

        else callback.onResult();
        return saved;

    }

    List<Donation> getAll(Context context) {
        final List<Donation> all = new ArrayList<>();

        OnResult callback = new OnResult(){
            @Override
            public void onResult() {
                all.addAll(donations.values());
            }
        };

        if(!initialized)
            new FetchDataTask(context, callback).execute();

        else callback.onResult();
        return all;
    }

    void saveEvent(String id) {
        Donation d = donations.get(id);
        if(d.isAvailable())  // available => saved
            d.setState(Donation.State.SAVED);
        else if(d.isSaved())  // saved => available
            d.setState(Donation.State.AVAILABLE);

        AdapterManager.get().updateDataSourceAll();
    }

    void removeAll(Set<String> items) {
        donations.keySet().removeAll(items);
        AdapterManager.get().updateDataSourceAll();
    }

    void returnAll(Set<String> selected) {
        for(String id: selected)
            donations.get(id).setSelected(false);
        AdapterManager.get().updateDataSourceAll();

    }


    private static class FetchDataTask extends AsyncTask<String, Void, Integer> {
        private final Context mContext;
        private final OnResult mCallback;

        FetchDataTask(Context context, OnResult callback) {
            this.mContext = context;
            this.mCallback = callback;
        }

        @Override
        protected Integer doInBackground(String... params) {
            getDonationsFromFile(mContext);
            if(donations.size() == 0)
                return 0;
            return 1; // successful
        }

        @Override
        protected void onPostExecute(Integer result) {
            if(mCallback != null)
                mCallback.onResult();
        }
    }
}
