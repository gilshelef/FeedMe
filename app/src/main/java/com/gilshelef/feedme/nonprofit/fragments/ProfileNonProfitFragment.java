package com.gilshelef.feedme.nonprofit.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.launcher.RegistrationActivity;
import com.gilshelef.feedme.launcher.RegistrationHandler;
import com.gilshelef.feedme.nonprofit.data.DataManager;
import com.gilshelef.feedme.nonprofit.data.Donation;
import com.gilshelef.feedme.nonprofit.data.NonProfit;
import com.gilshelef.feedme.nonprofit.data.OnBooleanResult;
import com.gilshelef.feedme.util.Constants;
import com.gilshelef.feedme.util.Logger;
import com.gilshelef.feedme.util.OnInfoUpdateListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by gilshe on 3/2/17.
 */
public class ProfileNonProfitFragment extends Fragment {
    public static final String TAG = ProfileNonProfitFragment.class.getSimpleName();
    private DatabaseReference mNonProfitRef;

    TextView nonProfitName;
    TextView address;
    TextView contactName;
    TextView phone;
    Button removeRegistration;
    NonProfit mNonProfit;
    private Logger mLogger;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mNonProfit = NonProfit.get(getActivity());
        mNonProfitRef = FirebaseDatabase.getInstance().getReference().child(Constants.DB_NON_PROFIT).child(mNonProfit.getId());
        mLogger = Logger.get(getContext());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_filter);
        item.setVisible(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.non_profit_fragment_profile, container, false);
        ((ToggleHomeBar) getActivity()).drawHomeBar(false);

        //views
        nonProfitName = (TextView) rootView.findViewById(R.id.non_profit_name);
        address = (TextView) rootView.findViewById(R.id.non_profit_address);
        contactName = (TextView) rootView.findViewById(R.id.contact_name);
        phone = (TextView) rootView.findViewById(R.id.contact_phone);
        removeRegistration = (Button) rootView.findViewById(R.id.remove_registration_btn);

        //set values
        nonProfitName.setText(mNonProfit.getName());
        address.setText(mNonProfit.getAddress());
        contactName.setText(mNonProfit.getContact());
        phone.setText(mNonProfit.getPhone());
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        nonProfitName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNonProfitDialog();
            }


        });
        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAddressDialog();
            }
        });
        contactName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createContactDialog();
            }
        });
        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPhoneDialog();
            }
        });
        removeRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeRegistration();
            }
        });
    }

    private void removeRegistration() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getString(R.string.remove_registration));
        alertDialog.setMessage(R.string.remove_registration_confirmation);

        alertDialog.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefs = getContext().getSharedPreferences(RegistrationActivity.PREFS, Context.MODE_PRIVATE);
                        prefs.edit().putBoolean(RegistrationActivity.NON_PROFIT, false).apply();

                        updateDataBase();

                        Intent intent = new Intent(getActivity(), RegistrationActivity.class);
                        Toast.makeText(getContext(), R.string.remove_registration_successfully, Toast.LENGTH_LONG).show();
                        startActivity(intent);
                    }

                    private void updateDataBase() {
                        final DatabaseReference donationRef = FirebaseDatabase.getInstance().getReference().child(Constants.DB_DONATION);

                        mNonProfitRef.child(Constants.DB_DONATION).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.d(TAG, "finished update on all donations");
                                Log.d(TAG, "removing nonProfit: " + mNonProfit.getId() + "from database");
                                mNonProfitRef.removeValue();
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("New_Donations");
                                mLogger.removeRegistration(Logger.EVENT.NON_PROFIT, mNonProfit.getId());
                                NonProfit.clear();
                                DataManager.clear();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        mNonProfitRef.child(Constants.DB_DONATION).addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                final String donationId = dataSnapshot.getKey();

                                donationRef.child(donationId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //if donation exists, change state to available
                                        Donation donation = dataSnapshot.getValue(Donation.class);
                                        if (donation == null)
                                            return;

                                        donationRef.child(donationId)
                                                .child(Donation.K_STATE)
                                                .setValue(Donation.State.AVAILABLE);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }



    private void createNonProfitDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("שם עמותה");
        alertDialog.setMessage("הכנס שם של עמותה");

        final EditText input = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("עדכן",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String newNonProfitName = input.getText().toString();
                        if (!RegistrationHandler.isEmpty(input)) {
                            OnBooleanResult callBack = new OnBooleanResult() {
                                @Override
                                public void onResult(boolean listed) {
                                    if(listed){
                                        nonProfitName.setText(newNonProfitName);
                                        mNonProfit.setName(getActivity(), newNonProfitName);
                                        ((OnInfoUpdateListener)getActivity()).onBusinessChange(newNonProfitName);
                                        updateDataBase("name", newNonProfitName);
                                        Toast.makeText(getActivity(), "שם עמותה שונה בהצלחה", Toast.LENGTH_LONG).show();
                                    }
                                }
                            };
                            new RegistrationHandler.CheckForNonProfitListingTask(getActivity(), newNonProfitName, callBack).execute();
                        }
                    }
                });

        alertDialog.setNegativeButton("בטל",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();


    }

    private void createPhoneDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.phone);
        alertDialog.setMessage(R.string.enter_phone);

        final EditText input = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_call_black_24dp);

        alertDialog.setPositiveButton(R.string.update,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String newPhone = input.getText().toString();
                        if (!RegistrationHandler.isEmpty(input)) {
                            if(RegistrationHandler.checkPhone(getContext(), newPhone)) {
                                mNonProfit.setPhone(getContext(), newPhone);
                                phone.setText(newPhone);
                                updateDataBase("phone", newPhone);
                                Toast.makeText(getActivity(), R.string.contact_phone_changed_successfully, Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                });

        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    private void createContactDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getString(R.string.contact_name));
        alertDialog.setMessage(R.string.enter_contact_name);

        final EditText input = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_person_black_24dp);

        alertDialog.setPositiveButton(R.string.update,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String newContact = input.getText().toString();
                        if (!RegistrationHandler.isEmpty(input)) {
                            mNonProfit.setContact(getContext(), newContact);
                            contactName.setText(newContact);
                            updateDataBase("contact", newContact);
                            ((OnInfoUpdateListener)getActivity()).onContactChange(newContact);
                            Toast.makeText(getActivity(), R.string.contact_changed_successfully, Toast.LENGTH_LONG).show();

                        }

                    }
                });

        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    private void createAddressDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.address);
        alertDialog.setMessage(R.string.enter_address);

        final EditText input = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_room_black_24dp);

        alertDialog.setPositiveButton(R.string.update,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!RegistrationHandler.isEmpty(input)) {
                            String newAddress = input.getText().toString();
                            LatLng latLng = RegistrationHandler.getLocationFromAddress(getContext(), input);
                            if(latLng != null){
                                mNonProfit.setAddress(getContext(), latLng, newAddress);
                                address.setText(newAddress);
                                updateDataBase("address", newAddress);

                                mNonProfitRef.child("basePosition").child("latitude").setValue(latLng.latitude);
                                mNonProfitRef.child("basePosition").child("longitude").setValue(latLng.longitude);
                                Toast.makeText(getActivity(), R.string.address_changes_successfully, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });

        alertDialog.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    private void updateDataBase(String key, String value) {
        mNonProfitRef.child(key).setValue(value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ToggleHomeBar) getActivity()).drawHomeBar(true);
    }

}
