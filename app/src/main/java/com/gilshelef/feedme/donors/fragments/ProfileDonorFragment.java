package com.gilshelef.feedme.donors.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.donors.data.Donor;
import com.gilshelef.feedme.launcher.RegistrationActivity;
import com.gilshelef.feedme.launcher.RegistrationHandler;
import com.gilshelef.feedme.nonprofit.data.Association;
import com.gilshelef.feedme.nonprofit.data.types.Type;
import com.gilshelef.feedme.nonprofit.data.types.TypeManager;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;
import java.util.List;

/**
 * Created by gilshe on 3/26/17.
 */

public class ProfileDonorFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    public static final String TAG = ProfileDonorFragment.class.getSimpleName();
    private TextView businessName;
    private TextView contactName;
    private TextView address;
    private TextView phone;
    private Button removeRegistration;
    private Spinner spinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.donors_fragment_profile, container, false);

        businessName = (TextView) rootView.findViewById(R.id.business_name);
        contactName = (TextView) rootView.findViewById(R.id.donor_name);
        address = (TextView) rootView.findViewById(R.id.donor_address);
        phone = (TextView) rootView.findViewById(R.id.contact_phone);
        removeRegistration = (Button) rootView.findViewById(R.id.remove_registration_btn);
        TextView tvSpinner = (TextView) rootView.findViewById(R.id.donation_type);

        spinner = (Spinner) rootView.findViewById(R.id.donation_type_spinner);
        List<Type> typesArray = TypeManager.get().getAll();
        Collections.sort(typesArray, new TypeManager.TypeComparator(Donor.get(getActivity()).getDonationType()));
        ArrayAdapter<Type> adapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, typesArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setEnabled(false);

        final Donor instance = Donor.get(getActivity());
        businessName.setText(instance.getBusinessName());
        contactName.setText(instance.getContact());
        address.setText(instance.getAddress());
        phone.setText(instance.getPhone());

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
        removeRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeRegistration();
            }
        });

        tvSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setEnabled(true);
            }
        });
        return rootView;
    }

    private void removeRegistration() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getString(R.string.remove_registration));
        alertDialog.setMessage(R.string.remove_registration_confirmation);

        alertDialog.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefs = getContext().getSharedPreferences(RegistrationActivity.PREFS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(RegistrationActivity.DONOR, false);
                        editor.apply();

                        Intent intent = new Intent(getActivity(), RegistrationActivity.class);
                        Toast.makeText(getContext(), R.string.remove_registration_successfully, Toast.LENGTH_LONG).show();
                        startActivity(intent);
                        //TODO notify db?
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
                                Association.get(getActivity()).setPhone(getContext(), newPhone);
                                phone.setText(newPhone);
                                Toast.makeText(getActivity(), R.string.conatct_phone_changed_successfully, Toast.LENGTH_LONG).show();
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
                            Donor.get(getActivity()).setContact(getContext(), newContact);
                            contactName.setText(newContact);
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
                        String newAddress = input.getText().toString();
                        if (!RegistrationHandler.isEmpty(input)) {
                            LatLng latLng = RegistrationHandler.getLocationFromAddress(getContext(), newAddress);
                            if(latLng == null)
                                input.setError(getString(R.string.unrecognized_address));
                            else {
                                Donor.get(getActivity()).setAddress(getContext(), latLng, newAddress);
                                address.setText(newAddress);
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

    private void createBusinessDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.business_name);
        alertDialog.setMessage(R.string.enter_business_name);

        final EditText input = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_work_black_24dp);

        alertDialog.setPositiveButton(R.string.update,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String newBusinessName = input.getText().toString();
                        if (!RegistrationHandler.isEmpty(input)) {
                            Donor.get(getActivity()).setBusinessName(getContext(), newBusinessName);
                            businessName.setText(newBusinessName);
                            Toast.makeText(getActivity(), R.string.business_name_changed_successfully, Toast.LENGTH_LONG).show();
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String donationType = parent.getItemAtPosition(position).toString();
        Donor.get(getActivity()).setTypeByString(getContext(), donationType);
        Toast.makeText(getActivity(), R.string.donation_type_changed_successfully, Toast.LENGTH_LONG).show();
        spinner.setEnabled(false);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(getContext(), "נא לבחור סוג תרומה אחד לפחות", Toast.LENGTH_LONG).show();
    }
}
