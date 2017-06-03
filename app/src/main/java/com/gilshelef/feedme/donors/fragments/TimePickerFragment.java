package com.gilshelef.feedme.donors.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.gilshelef.feedme.util.Constants;

import java.util.Calendar;

/**
 * Created by gilshe on 3/31/17.
 */

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR_OF_DAY, Constants.DEFAULT_DELTA_TIME);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (getTargetFragment() != null)
            ((TimePickerDialog.OnTimeSetListener) getTargetFragment()).onTimeSet(view, hourOfDay, minute);
        else
            ((TimePickerDialog.OnTimeSetListener) getActivity()).onTimeSet(view, hourOfDay, minute);
    }
}