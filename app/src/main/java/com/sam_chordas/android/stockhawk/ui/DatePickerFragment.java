package com.sam_chordas.android.stockhawk.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by GOURAV on 10-11-2016.
 */

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public interface DateChanged{
        public abstract void dateChanged(String startDate,boolean isStartDate);
    }


    boolean isStartDate = false;

    public static DatePickerFragment newInstance(Bundle args){
        DatePickerFragment fragment = new DatePickerFragment();
        if(args!=null){
            fragment.isStartDate = args.getBoolean("isStartDate");
        }
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        ((DateChanged)getActivity()).dateChanged(year + "-" + (++month > 9 ? month : "0" + month) + "-" + (day > 9 ? day : "0" + day),isStartDate);
    }
}