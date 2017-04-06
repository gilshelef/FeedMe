package com.gilshelef.feedme.donors.fragments;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import java.util.Calendar;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * Created by gilshe on 3/27/17.
 */

public class AddDonationFragment extends Fragment implements TimePickerDialog.OnTimeSetListener{
    public static final String TAG = AddDonationFragment.class.getSimpleName();
    public static final int REQUEST_IMAGE_CAPTURE = 6;

    private EditText description;
    private Bitmap imageBitmap;
    private TextView timeView;
    private ImageView imageView;
    private Calendar calendar;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.donors_fragment_add_donation, container, false);
        TextView donationType = (TextView) rootView.findViewById(R.id.donation_type_header);
        String header = getString(R.string.new_donation) + " " + Donor.get(getActivity()).getDonationType().hebrew();
        donationType.setText(header);
        return rootView;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState){
        description = (EditText) view.findViewById(R.id.donation_description);
        View time = view.findViewById(R.id.pick_time_btn);
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                DialogFragment dialog = new TimePickerFragment();
                dialog.setTargetFragment(AddDonationFragment.this, 0);
                dialog.show(fm, "dialog");
            }
        });

        View image = view.findViewById(R.id.add_image_btn);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OnActionEvent) getActivity()).onCameraEvent();
//                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null)
//                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            }
        });


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
        if(resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setVisibility(View.VISIBLE);
            //TODO upload to db?
        }

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        this.timeView.setText(hourOfDay + ":" + minute);
        this.timeView.setVisibility(View.VISIBLE);
    }

    private class UploadDonationTask extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog progress;
        private Donation donation;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(AddDonationFragment.this.getContext());
            progress.setMessage("מוסיפים את תרומתך למאגר התרומות...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setCanceledOnTouchOutside(true);
            progress.show();
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            Donor donor = Donor.get(getActivity());
            donation = new Donation();
            donation.type = donor.getDonationType();
            donation.phone = donor.getPhone();
            donation.firstName = donor.getFirstName();
            donation.lastName = donor.getLastName();
            donation.location =  donor.getPosition();
            donation.businessName = donor.getBusinessName();
            donation.description = description.getText().toString();
            donation.setState(Donation.State.DONOR);

            String id = String.valueOf(DonationsManager.get().getAll().size() + 1);
            donation.setId(id);
            donation.imageUrl = "";
            Locale locale = new Locale.Builder().setLanguage("he").build();
            donation.calendar = calendar != null ? calendar : Calendar.getInstance(locale);
            //TODO handle image
            //TODO upload donation to db
            //TODO notify data base with my donations - mysql
            //TODO retrieve donation ID

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(progress != null) progress.dismiss();
            if(result) {
                ((OnActionEvent)getActivity()).newDonationEvent(donation);
                clearAll();
                Toast.makeText(getContext(), "תרומתך הועלתה בהצלחה", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getContext(), "תקלה ארעה בזמן העלת התרומה, נא נסה שנית בעוד מספר דקות", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void clearAll() {
        this.description.setText("");
        this.timeView.setVisibility(View.GONE);
    }


    public interface OnActionEvent {
        void newDonationEvent(Donation donation);
        void onCameraEvent();
    }
}
