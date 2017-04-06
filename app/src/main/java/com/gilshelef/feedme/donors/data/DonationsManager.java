package com.gilshelef.feedme.donors.data;

import android.content.Context;
import android.os.AsyncTask;

import com.gilshelef.feedme.nonprofit.adapters.AdapterManager;
import com.gilshelef.feedme.nonprofit.data.Donation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gilshe on 3/31/17.
 */

public class DonationsManager {

    private Map<String, Donation> donations;
    private static DonationsManager instance;

    private DonationsManager(Context context){
        donations = new LinkedHashMap<>();
        new FetchDataTask(context).execute();
    }

    public static DonationsManager get(Context context) {
        if (instance == null) {
            synchronized (DonationsManager.class) {
                if (instance == null)
                    return build(context);
            }
        }
        return instance;
    }

    public static DonationsManager get() {
        return instance;
    }

    private static DonationsManager build(Context context) {
        instance = new DonationsManager(context);
        return instance;
    }

    private void getDonationsFromFile(Context context){
        try {
            // Load data
            String jsonString = loadJsonFromAsset("my_donations.json", context);
            JSONObject json = new JSONObject(jsonString);
            JSONArray donations = json.getJSONArray("donations");

            // Get Donation objects from data
            for(int i = 0; i < donations.length(); i++){
                JSONObject obj = donations.getJSONObject(i);
                Donation donation = new Donation(obj);
                donation.setState(Donation.State.DONOR);
                String id = donation.getId();
                this.donations.put(id, donation);
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

    public List<Donation> getAll() {
        final List<Donation> all = new ArrayList<>();
        all.addAll(donations.values());
        return all;
    }

    public void newDonationEvent(Donation donation) {
        donations.put(donation.getId(), donation);
    }

    public void returnDonation(Donation donation) {
        donations.remove(donation.getId());
    }

    public void update(String donationId, String description, String calenderStr) {
        Donation donation = donations.get(donationId);
        donation.setDescription(description);
        donation.setCalendar(calenderStr);
        AdapterManager.get().updateDataSourceAll();
    }

    private class FetchDataTask extends AsyncTask<String, Void, Integer> {
        private final Context mContext;

        FetchDataTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected Integer doInBackground(String... params) {
            getDonationsFromFile(mContext);
            if(donations.size() == 0)
                return 0;
            return 1; // successful
        }
    }
}
