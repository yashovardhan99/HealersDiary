package com.yashovardhan99.healersdiary.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by Yashovardhan99 on 1/11/18 as a part of HealersDiary.
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    int hour, minute;
    public TimePickerListener listener;

    public TimePickerFragment(){
        Calendar c = Calendar.getInstance();
        setTime(c);
    }

    private void setTime(Calendar c) {
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
    }

    public interface TimePickerListener{
        void onTimeSet(DialogFragment dialogFragment);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (TimePickerListener) getTargetFragment();
        } catch (ClassCastException e){
            throw new ClassCastException(getTargetFragment() + "must implement TimePickerListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if(getArguments()!=null){
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(getArguments().getLong("DATE"));
            setTime(c);
        }
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getContext()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;
        listener.onTimeSet(this);
    }
}
