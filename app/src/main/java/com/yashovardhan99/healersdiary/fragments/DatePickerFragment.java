package com.yashovardhan99.healersdiary.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by Yashovardhan99 on 1/11/18 as a part of HealersDiary.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    int year, month, day;
    public DatePickerListener listener;

    public DatePickerFragment(){
        Calendar c = Calendar.getInstance();
        setDate(c);
    }

    private void setDate(Calendar c) {
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
    }

    public interface DatePickerListener{
        void onDateSet(DialogFragment dialogFragment);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            listener = (DatePickerListener) getTargetFragment();
        } catch (ClassCastException e){
            throw new ClassCastException(getTargetFragment()+ "must implement DatePickerListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        if(getArguments()!=null) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(getArguments().getLong("DATE"));
            setDate(c);
        }
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(Calendar.getInstance().getTime().getTime());
        return datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.day = dayOfMonth;
        listener.onDateSet(this);
    }
}
