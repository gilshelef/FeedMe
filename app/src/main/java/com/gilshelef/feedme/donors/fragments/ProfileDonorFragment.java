package com.gilshelef.feedme.donors.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.donors.data.DonationsManager;
import com.gilshelef.feedme.donors.data.Donor;
import com.gilshelef.feedme.launcher.RegistrationActivity;
import com.gilshelef.feedme.launcher.RegistrationHandler;
import com.gilshelef.feedme.nonprofit.data.OnResult;
import com.gilshelef.feedme.nonprofit.data.types.Type;
import com.gilshelef.feedme.nonprofit.data.types.TypeManager;
import com.gilshelef.feedme.nonprofit.fragments.OnCounterChangeListener;
import com.gilshelef.feedme.util.Constants;
import com.gilshelef.feedme.util.Logger;
import com.gilshelef.feedme.util.OnInfoUpdateListener;
import com.gilshelef.feedme.util.Util;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gilshe on 3/26/17.
 */

public class ProfileDonorFragment extends Fragment implements AdapterView.OnItemSelectedListener, OnCounterChangeListener {
    public static final String TAG = ProfileDonorFragment.class.getSimpleName();
    private TextView businessName;
    private TextView contactName;
    private TextView address;
    private TextView phone;
    private Spinner spinner;
    private TextView medalCount;
    private static Donor mDonor;
    private static DatabaseReference mDonorRef;
    private static Logger mLogger;


    @Override
    public void onCreate(Bundle savedInstanceState){
        mDonor = Donor.get(getActivity());
        super.onCreate(savedInstanceState);
        mDonorRef = FirebaseDatabase.getInstance().getReference()
                .child(Constants.DB_DONOR)
                .child(mDonor.getId());

        mLogger = Logger.get(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.donors_fragment_profile, container, false);

        //views
        businessName = (TextView) rootView.findViewById(R.id.business_name);
        contactName = (TextView) rootView.findViewById(R.id.donor_name);
        address = (TextView) rootView.findViewById(R.id.donor_address);
        phone = (TextView) rootView.findViewById(R.id.contact_phone);
        spinner = (Spinner) rootView.findViewById(R.id.donation_type_spinner);

        //spinner
        List<Type> typesArray = TypeManager.get().getAll();
        Collections.sort(typesArray, new TypeManager.TypeComparator(Donor.get(getActivity()).getDonationType()));
        ArrayAdapter<Type> adapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, typesArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        spinner.post(new Runnable() {
            public void run() {
                spinner.setOnItemSelectedListener(ProfileDonorFragment.this);
            }
        });

        final Donor donor = Donor.get(getActivity());
        businessName.setText(donor.getBusinessName());
        contactName.setText(donor.getContactInfo());
        address.setText(donor.getAddress());
        phone.setText(donor.getPhone());
        return rootView;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState){

        Log.d(TAG, "onViewCreated");
        businessName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBusinessDialog();
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

        Button removeRegistration = (Button) view.findViewById(R.id.remove_registration_btn);
        removeRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeRegistration();
            }
        });

        TextView tvSpinner = (TextView) view.findViewById(R.id.donation_type);
        tvSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.performClick();
            }
        });

        medalCount = (TextView) view.findViewById(R.id.medal_counter);
        updateViewCounters();
    }

    private void removeRegistration() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        TextView title = Util.buildTitleView(getContext(), getString(R.string.remove_registration));
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error_black_24dp, 0, 0, 0);
        builder.setCustomTitle(title);
        builder.setMessage(R.string.remove_registration_confirmation);
        builder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "remove donor: " + mDonor.getId());
                        new RemoveDonorTask(getContext(), new OnResult() {
                            @Override
                            public void onResult() {
                                Intent intent = new Intent(getActivity(), RegistrationActivity.class);
                                startActivity(intent);
                            }
                        }).execute();
                    }
                });

        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.show();
    }

    private void createPhoneDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        TextView title = Util.buildTitleView(getContext(), getString(R.string.edit_phone));
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_black_24dp, 0, 0, 0);
        builder.setCustomTitle(title);

        final EditText input = Util.buildInputView(getContext(), "");
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

        builder.setPositiveButton(R.string.update,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String newPhone = input.getText().toString();
                        if (!RegistrationHandler.isEmpty(input)) {
                            if(RegistrationHandler.checkPhone(getContext(), newPhone)) {
                                Donor.get(getActivity()).setPhone(getContext(), newPhone);
                                phone.setText(newPhone);
                                DonationsManager.get().updateProfile(getContext());
                                updateDataBase(Donor.K_PHONE, newPhone);
                                Toast.makeText(getActivity(), R.string.contact_phone_changed_successfully, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });

        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.show();
    }

    private void createContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        TextView title = Util.buildTitleView(getContext(),getString(R.string.edit_contact_name));
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_black_24dp, 0, 0, 0);
        builder.setCustomTitle(title);

        String hint = String.format("%s וגם %s", getString(R.string.first_name), getString(R.string.last_name));
        final EditText input = Util.buildInputView(getContext(), hint);
        builder.setView(input);

        builder.setPositiveButton(R.string.update,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String newContact = input.getText().toString();
                        if (!RegistrationHandler.isEmpty(input)) {
                            String[] names = newContact.split(" +");
                            if(names.length == 2) {
                                Donor.get(getActivity()).setContactInfo(getContext(), newContact);
                                contactName.setText(newContact);
                                ((OnInfoUpdateListener) getActivity()).onContactChange(newContact); // update drawer view

                                Map<String, Object> updates = new HashMap<>();
                                updates.put(Donor.K_FIRST_NAME, names[0]);
                                updates.put(Donor.K_LAST_NAME, names[1]);
                                mDonorRef.updateChildren(updates);

                                DonationsManager.get().updateProfile(getContext());
                                Toast.makeText(getActivity(), R.string.contact_changed_successfully, Toast.LENGTH_LONG).show();
                            }
                            else{
                                Toast.makeText(getActivity(), "הכנס שם פרטי וגם שם משפחה", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });

        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.show();
    }

    private void createAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        TextView title = Util.buildTitleView(getContext(), getString(R.string.enter_address));
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_room_black_24dp, 0, 0, 0);
        builder.setCustomTitle(title);

        final EditText input = Util.buildInputView(getContext(), getString(R.string.address_hint));
        builder.setView(input);

        builder.setPositiveButton(R.string.update,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!RegistrationHandler.isEmpty(input)) {
                            String newAddress = input.getText().toString();
                            LatLng latLng = RegistrationHandler.getLocationFromAddress(getContext(), input);
                            if(latLng != null){
                                updateDataBase(Donor.K_ADDRESS, newAddress);
                                mDonorRef.child(Donor.K_POSITION).child(Donor.K_LAT).setValue(latLng.latitude);
                                mDonorRef.child(Donor.K_POSITION).child(Donor.K_LNG).setValue(latLng.longitude);

                                Donor.get(getActivity()).setAddress(getContext(), latLng, newAddress);
                                address.setText(newAddress);
                                DonationsManager.get().updateProfile(getContext());
                                Toast.makeText(getActivity(), R.string.address_changes_successfully, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });

        builder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.show();
    }

    private void createBusinessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        TextView title = Util.buildTitleView(getContext(), getString(R.string.edit_bussiness_name));
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_work_black_24dp, 0, 0, 0);

        builder.setCustomTitle(title);

        final EditText input = Util.buildInputView(getContext(), getString(R.string.business_name));
        builder.setView(input);

        builder.setPositiveButton(R.string.update,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String newBusinessName = input.getText().toString();
                        if (!RegistrationHandler.isEmpty(input)) {
                            Donor.get(getActivity()).setBusinessName(getContext(), newBusinessName);
                            businessName.setText(newBusinessName);
                            ((OnInfoUpdateListener)getActivity()).onBusinessChange(newBusinessName);
                            updateDataBase(Donor.K_BUSINESS, newBusinessName);
                            DonationsManager.get().updateProfile(getContext());
                            Toast.makeText(getActivity(), R.string.business_name_changed_successfully, Toast.LENGTH_LONG).show();
                        }
                    }
                });

        builder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.show();

    }

    private void updateDataBase(String key, String value) {
        mDonorRef.child(key).setValue(value);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String donationType = parent.getItemAtPosition(position).toString();
        Donor donor = Donor.get(getActivity());
        donor.setTypeByString(getContext(), donationType);
        mDonorRef.child(Donor.K_TYPE).setValue(donor.getDonationType());
        DonationsManager.get((OnCounterChangeListener) getActivity()).updateProfile(getContext());
        Toast.makeText(getActivity(), R.string.donation_type_changed_successfully, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(getContext(), "נא לבחור סוג תרומה אחד לפחות", Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateViewCounters() {
        final Donor donor = Donor.get(getActivity());
        Log.d(TAG, "donation Count: " + donor.getDonationCount());
        if(medalCount != null)
            medalCount.setText(String.valueOf(donor.getDonationCount()));
    }


    private static class RemoveDonorTask extends AsyncTask<Void, Void, Void>{

        private final Context mContext;
        private OnResult mCallback;

        private RemoveDonorTask(Context context, OnResult callback) {
            this.mContext = context;
            this.mCallback = callback;
        }


        @Override
        protected Void doInBackground(Void... params) {
            final DatabaseReference donorDonationRef = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.DB_DONOR_DONATION)
                    .child(mDonor.getId());

            ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DatabaseReference mDonationRef = FirebaseDatabase.getInstance().getReference().child(Constants.DB_DONATION);

                    final Map<String, Object> donationToRemove = new HashMap<>();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        String donationId = child.getKey();
                        donationToRemove.put(donationId, null); // remove /donation/donorId
                        Util.unScheduleAlarm(mContext, donationId); //unscheduled alarm
                    }

                    mDonationRef.updateChildren(donationToRemove, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            mDonorRef.removeValue(); // remove /donor
                            donorDonationRef.removeValue(); //remove /donor_donation

                            DonationsManager.get().removeImages(donationToRemove.keySet());
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(mDonor.getId());
                            mLogger.removeRegistration(Logger.EVENT.DONOR, mDonor.getId());

                            Donor.clear();
                            DonationsManager.clear();
                            mCallback.onResult();
                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };

             donorDonationRef
                     .child(Constants.DB_DONATION)
                     .addListenerForSingleValueEvent(listener);


            SharedPreferences prefs = mContext.getSharedPreferences(RegistrationActivity.PREFS, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(RegistrationActivity.DONOR, false).apply();
            return null;
        }

        @Override
        protected void onPostExecute(Void p){
            Toast.makeText(mContext, R.string.remove_registration_successfully, Toast.LENGTH_LONG).show();
        }
    }

}
