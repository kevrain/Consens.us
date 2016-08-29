package com.kevrain.consensus.fragments;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;

/**
 * Created by shravyagarlapati
 */
public class DatePickerFragment extends DialogFragment {
    OnDateSetListener onDateSet;

    public DatePickerFragment() {
    }

    public void setCallBack(OnDateSetListener onDate) {
        onDateSet = onDate;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of TimePickerDialog and return it
        return new DatePickerDialog(getActivity(), onDateSet, year, month, day);
    }
}
