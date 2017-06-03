package com.gilshelef.feedme.donors.fragments;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.donors.data.DonationsManager;
import com.gilshelef.feedme.donors.data.Donor;
import com.gilshelef.feedme.nonprofit.data.Donation;
import com.gilshelef.feedme.nonprofit.fragments.OnCounterChangeListener;
import com.gilshelef.feedme.util.Constants;
import com.gilshelef.feedme.util.Logger;
import com.gilshelef.feedme.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * Created by gilshe on 3/27/17.
 */

public class AddDonationFragment extends Fragment implements TimePickerDialog.OnTimeSetListener{
    public static final String TAG = AddDonationFragment.class.getSimpleName();
    public static final int REQUEST_IMAGE_CAPTURE = Constants.REQUEST_3;


    private EditText description;
    private Bitmap imageBitmap;
    private TextView timeView;
    private ImageView imageView;
    private Calendar calendar;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private Logger mLogger;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mLogger = Logger.get(getContext());
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.donors_fragment_add_donation, container, false);
        TextView donationType = (TextView) rootView.findViewById(R.id.donation_type_header);
        String header = getString(R.string.new_donation) + " " + Donor.get(getActivity()).getDonationType().hebrew();
        donationType.setText(header);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        description = (EditText) view.findViewById(R.id.donation_description);
        View.OnClickListener timeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                DialogFragment dialog = new TimePickerFragment();
                dialog.setTargetFragment(AddDonationFragment.this, 0);
                dialog.show(fm, "dialog");
            }
        };


        View.OnClickListener imageListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OnCameraEvent) getActivity()).onCameraEvent();
            }
        };

        view.findViewById(R.id.pick_time_btn).setOnClickListener(timeListener);
        view.findViewById(R.id.time_image).setOnClickListener(timeListener);

        view.findViewById(R.id.add_image_btn).setOnClickListener(imageListener);
        view.findViewById(R.id.add_image).setOnClickListener(imageListener);

        Button addDonation = (Button) view.findViewById(R.id.add_donation_btn);
        addDonation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UploadDonationTask().execute();
            }
        });
        timeView = (TextView) view.findViewById(R.id.time_info);
        imageView = (ImageView) view.findViewById(R.id.image_info);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setVisibility(View.VISIBLE);
        }

    }

    private void uploadImageToStorage(String donationId, OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener) {

        if(imageBitmap == null) {
            onSuccessListener.onSuccess(null);
            return;
        }


        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference donationRef = mStorageRef.child(donationId);
        UploadTask uploadTask = donationRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getContext(), "Error occurred while uploading image to storage, please try again later", Toast.LENGTH_LONG).show();
                Log.e(TAG, exception.getMessage());
            }
        }).addOnSuccessListener(onSuccessListener);

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        String minuteStr = String.valueOf(minute);
        if(minute < 10){
            minuteStr = "0" + minute;
        }
        this.timeView.setText(hourOfDay + ":" + minuteStr);
        this.timeView.setVisibility(View.VISIBLE);
    }

    private class UploadDonationTask extends AsyncTask<Void, Void, Boolean> {
        final Donor donor = Donor.get(getActivity());


        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                final String donationId = mDatabase.child(Constants.DB_DONATION).push().getKey();

                final Donation donation = new Donation();
                donor.setProfileInfo(donation);
                donation.setId(donationId);
                donation.setDescription(description.getText().toString());
                donation.calendar = calendar != null ? calendar : getDefaultCalendar();

                donation.setImageUrl("");
                uploadImageToStorage(donationId, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if(taskSnapshot != null && taskSnapshot.getDownloadUrl() != null) {
                            Uri imageUri = taskSnapshot.getDownloadUrl();
                            DonationsManager.get().updateImageUrl(donation, imageUri.toString());
                            Log.d(TAG, "upload new image");
                        }
                    }
                });

                DonationsManager.get().newDonationEvent(donation);
                mLogger.newDonation(donationId);
                Util.scheduleAlarm(getActivity(), donation);
                return true;
            }

            catch (Exception e){
                Log.e(TAG, e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                donor.updateDonationCount(getContext(), 1);
                ((OnCounterChangeListener)getActivity()).updateViewCounters();
                Toast.makeText(getContext(), "תרומתך הועלתה בהצלחה", Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(getContext(), "תקלה ארעה בזמן העלת התרומה, נא נסה שנית בעוד מספר דקות", Toast.LENGTH_LONG).show();

            clearAll();
        }

        private Calendar getDefaultCalendar() {

            Locale locale = new Locale.Builder().setLanguage("he").build();
            Calendar cal = Calendar.getInstance(locale); // creates calendar
            cal.setTime(new Date()); // sets calendar time/date
            cal.add(Calendar.HOUR_OF_DAY, Constants.DEFAULT_DELTA_TIME); // adds two hours as default
            return cal;
        }
    }

    private void clearAll() {
        this.description.setText("");
        this.timeView.setVisibility(View.GONE);
        this.imageView.setVisibility(View.GONE);
        this.imageBitmap = null;
    }

    public interface OnCameraEvent {
        void onCameraEvent();
    }
}
