package com.gilshelef.feedme.nonprofit.fragments;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.nonprofit.data.Association;
import com.gilshelef.feedme.nonprofit.data.OnBooleanResult;
import com.google.android.gms.maps.model.LatLng;

import com.gilshelef.feedme.launcher.RegistrationActivity;
import com.gilshelef.feedme.launcher.RegistrationHandler;

/**
 * Created by gilshe on 3/2/17.
 */
public class ProfileFragment extends Fragment {
    public static final String TAG = ProfileFragment.class.getSimpleName();

    TextView nonProfitName;
    TextView address;
    TextView contactName;
    TextView phone;
    Button removeRegistration;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        ((ToggleHomeBar) getActivity()).drawHomeBar(false);

        nonProfitName = (TextView) rootView.findViewById(R.id.non_profit_name);
        address = (TextView) rootView.findViewById(R.id.non_profit_address);
        contactName = (TextView) rootView.findViewById(R.id.contact_name);
        phone = (TextView) rootView.findViewById(R.id.contact_phone);
        removeRegistration = (Button) rootView.findViewById(R.id.remove_registration_btn);

        final Association instance = Association.get(getActivity());
        nonProfitName.setText(instance.getName());
        address.setText(instance.getAddress());
        contactName.setText(instance.getContact());
        phone.setText(instance.getPhone());

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
        return rootView;
    }

    private void removeRegistration() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("הסר רישום");
        alertDialog.setMessage("האם אתה בטוח שתרצה להסיר את רישומך?");

        alertDialog.setPositiveButton("כן",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefs = getContext().getSharedPreferences(RegistrationActivity.PREFS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(RegistrationActivity.NON_PROFIT, false);
                        editor.apply();

                        Intent intent = new Intent(getActivity(), RegistrationActivity.class);
                        Toast.makeText(getContext(), "רישומך הוסר בהצלחה", Toast.LENGTH_LONG).show();
                        startActivity(intent);
                        //TODO notify db?
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
        alertDialog.setTitle("טלפון");
        alertDialog.setMessage("הכנס טלפון של איש הקשר");

        final EditText input = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_call_black_24dp);

        alertDialog.setPositiveButton("עדכן",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String newPhone = input.getText().toString();
                        if (!RegistrationHandler.isEmpty(input)) {
                            if(RegistrationHandler.checkPhone(getContext(), newPhone)) {
                                Association.get(getActivity()).setPhone(getContext(), newPhone);
                                phone.setText(newPhone);
                                Toast.makeText(getActivity(), "שם עמותה שונה בהצלחה", Toast.LENGTH_LONG).show();
                            }
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

    private void createContactDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("איש קשר");
        alertDialog.setMessage("הכנס שם של איש הקשר");

        final EditText input = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_person_black_24dp);

        alertDialog.setPositiveButton("עדכן",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String newContact = input.getText().toString();
                        if (!RegistrationHandler.isEmpty(input)) {
                            Association.get(getActivity()).setContact(getContext(), newContact);
                            contactName.setText(newContact);
                            Toast.makeText(getActivity(), "איש קשר שונה בהצלחה", Toast.LENGTH_LONG).show();

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

    private void createAddressDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("כתובת");
        alertDialog.setMessage("הכנס כתובת");

        final EditText input = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_room_black_24dp);

        alertDialog.setPositiveButton("עדכן",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String newAddress = input.getText().toString();
                        if (!RegistrationHandler.isEmpty(input)) {
                            LatLng latLng = RegistrationHandler.getLocationFromAddress(getContext(), newAddress);
                            if(latLng == null)
                                input.setError("כתובת לא מזוהה");
                            else {
                                Association.get(getActivity()).setAddress(getContext(), latLng, newAddress);
                                address.setText(newAddress);
                                Toast.makeText(getActivity(), "כתובת עמותה שונתה בהצלחה", Toast.LENGTH_LONG).show();
                            }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ToggleHomeBar) getActivity()).drawHomeBar(true);
    }

}
