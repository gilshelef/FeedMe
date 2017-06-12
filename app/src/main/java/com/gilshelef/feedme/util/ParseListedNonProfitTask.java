package com.gilshelef.feedme.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gilshe on 6/2/17.
 */

public class ParseListedNonProfitTask extends AsyncTask<Void, Void,Void> {
    private static final String CSV_PATH = "nonProfits.csv";
    private static final String DB_KEY = "listed_non_profit";
    private static final String TAG = ParseListedNonProfitTask.class.getSimpleName();
    private Context context;

    public ParseListedNonProfitTask(Context context){
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream;
        try {
            inputStream = assetManager.open(CSV_PATH);
            CSVFile csvFile = new CSVFile(inputStream);
            List<String[]> nonProfits = csvFile.read();
            UploadToDatabase(nonProfits);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void UploadToDatabase(List<String[]> nonProfits) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(DB_KEY);
        Map<String, Object> nonProfitsMap = new HashMap<>();

        for(String[] nonProfit: nonProfits){
            String id = nonProfit[0];
            String email = nonProfit[1];
            nonProfitsMap.put(id, email);
        }

        ref.updateChildren(nonProfitsMap);
    }

    private class CSVFile {
        InputStream inputStream;

        CSVFile(InputStream inputStream){
            this.inputStream = inputStream;
        }

        List<String[]> read(){
            List<String[]> resultList = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null) {
                    String[] row = csvLine.split(",");
                    resultList.add(row);
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            finally {
                try {
                    inputStream.close();
                }
                catch (IOException e) {
                    throw new RuntimeException("Error while closing input stream: "+e);
                }
            }
            return resultList;
        }

    }

}
