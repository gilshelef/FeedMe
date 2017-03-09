package com.gilshelef.feedme;

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

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
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
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by gilshe on 2/23/17.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private MapView mMapView;
    private static final String TAG = MapFragment.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;
    private List<Donation> mDataSource;

    public MapFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
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
        mDataSource = DataManager.get(getActivity()).getAll(getActivity());
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.search:
                searchEvent();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory");
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
        mMap.setOnMarkerClickListener(this);
        Association association = Association.get(getActivity());
        displayMark(association.getBasePosition(), association.getName());

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
                    PERMISSIONS_REQUEST_LOCATION);
            Log.i(TAG, "no permission for my location enable");
            return;
        }
        //TODO why not showing?
        new DrawDonationTask().execute();
    }

//    private void displayDonations() {
//
//        List<Donation> donationList = DataManager.get(getActivity()).getAll(getActivity());
//        List<Marker> markersList = new ArrayList<>();
//
//        for(Donation d: donationList) {
//            MarkerOptions options = new MarkerOptions();
//            options.position(d.getPosition());
//            options.title(d.getType().hebrew());
//            options.snippet(d.getDescription());
//            options.icon(BitmapDescriptorFactory.defaultMarker(d.getType().getColor()));
//            Marker m = mMap.addMarker(options);
//            m.setTag(d);
//            markersList.add(m);
//        }
//
//        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//        for (Marker m : markersList)
//            builder.include(m.getPosition());
//
//        LatLngBounds bounds = builder.build();
//        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
//        mMap.animateCamera(cu);
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST_LOCATION){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }

        }
    }

    private void searchEvent() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(getActivity());
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
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
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin_circle_black_48dp));
        Marker marker = mMap.addMarker(markerOptions);
        marker.showInfoWindow();

        CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
        mMap.moveCamera(center);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        mMap.animateCamera(zoom);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick");
        Donation d = (Donation) marker.getTag();
        return false;

    }

    private class DrawDonationTask extends AsyncTask<Void, Void, Void> {

        Map<MarkerOptions, Donation> optionToTag;

        @Override
        protected Void doInBackground(Void... params) {
            optionToTag = new HashMap<>();
            for (Donation d : mDataSource) {
                MarkerOptions options = new MarkerOptions();
                options.position(d.getPosition());
                options.title(d.getType().hebrew());
                options.snippet(d.getDescription());
                options.icon(BitmapDescriptorFactory.defaultMarker(d.getType().getColor()));
                optionToTag.put(options, d);

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d(TAG, "drawing markers");
            for (Map.Entry<MarkerOptions, Donation> entry : optionToTag.entrySet()) {
                Marker m = mMap.addMarker(entry.getKey());
                m.setTag(entry.getValue());
            }
        }
    }
}

