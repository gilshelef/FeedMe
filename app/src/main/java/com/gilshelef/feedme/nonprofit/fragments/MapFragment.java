package com.gilshelef.feedme.nonprofit.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.nonprofit.adapters.Adaptable;
import com.gilshelef.feedme.nonprofit.adapters.AdapterManager;
import com.gilshelef.feedme.nonprofit.data.DataManager;
import com.gilshelef.feedme.nonprofit.data.Donation;
import com.gilshelef.feedme.nonprofit.data.NonProfit;
import com.gilshelef.feedme.util.Constants;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by gilshe on 2/23/17.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, Adaptable {

    private static final Object CURRENT_POSITION = "position";
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;
    private GoogleMap mMap;
    private MapView mMapView;
    public static final String TAG = MapFragment.class.getSimpleName();
    private List<Donation> mDataSource;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);
        mDataSource = DataManager.get(getActivity()).getAll();
        AdapterManager.get().setAdapter(this);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.non_profit_map_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                ((OnSearchListener)getActivity()).onSearch();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;
        setUpMap();
    }

    private void setUpMap() {
        Log.d(TAG, "setUpMap");
        mDataSource = DataManager.get(getActivity()).getAll();

        mMap.setOnMarkerClickListener(this);
        NonProfit nonProfit = NonProfit.get(getActivity());
        displayMark(nonProfit.getPosition(), nonProfit.getName());

        if (ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //TODO add dialog box when explanation is needed
//            ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
//                    android.Manifest.permission.ACCESS_FINE_LOCATION);
//
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    Constants.PERMISSIONS_REQUEST_LOCATION);
            Log.i(TAG, "no permission for my position enable");
            return;
        }
        new DrawDonationTask().execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == Constants.PERMISSIONS_REQUEST_LOCATION){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                displayMark(place.getLatLng(), place.getName().toString());
                Log.i(TAG, "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void displayMark(LatLng latLng, String title) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(title);

        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_person));
        Marker marker = mMap.addMarker(markerOptions);
        marker.setTag(CURRENT_POSITION);
        marker.showInfoWindow();

        CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
        mMap.moveCamera(center);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        mMap.animateCamera(zoom);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick");
        if(!(marker.getTag() instanceof Donation))
            marker.showInfoWindow();

        else {
            Donation donation = (Donation) marker.getTag();
            ((BaseFragment.OnDetailsListener) getActivity()).onDetails(null, donation);
        }
        return false;
    }

    @Override
    public void updateDataSource() {
        Log.d(TAG,"updateDataSource");
        mDataSource.clear();
        mDataSource.addAll(DataManager.get(getActivity()).getAll());
    }

    @Override
    public void notifyDataSetChanged() {
        new DrawDonationTask().execute();
    }

    @Override
    public String getName() {
        return TAG;
    }

    public interface OnSearchListener {
        void onSearch();
    }

    private class DrawDonationTask extends AsyncTask<Void, Void, Void> {

        Map<MarkerOptions, Donation> optionToTag;

        @Override
        protected Void doInBackground(Void... params) {
            optionToTag = new HashMap<>();
            List<Donation> toDraw = new LinkedList<>(mDataSource);
            for (Donation d : toDraw) {
                MarkerOptions options = new MarkerOptions();
                options.position(d.getPosition());
                options.title(d.getType().hebrew());
                options.snippet(d.getDescription());
                options.icon(BitmapDescriptorFactory.defaultMarker(d.getType().color()));
                optionToTag.put(options, d);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            for (Map.Entry<MarkerOptions, Donation> entry : optionToTag.entrySet()) {
                Marker m = mMap.addMarker(entry.getKey());
                m.setTag(entry.getValue());
            }
        }
    }
}

