package com.yashovardhan99.healersdiary.fragments;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.yashovardhan99.healersdiary.R;
import com.yashovardhan99.healersdiary.activities.MainActivity;
import com.yashovardhan99.healersdiary.helpers.MyNotificationPublisher;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Objects;

/**
 * Created by Yashovardhan99 on 20/10/18 as a part of HealersDiary.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, CompoundButton.OnCheckedChangeListener, TimePickerFragment.TimePickerListener {

    private static final String CHANNEL_ID = "dailyReminderChannel";
    private static final String DAILY_REMINDER_BOOL = "dailyreminderboolean";
    private static final String DAILY_REMINDER_TIME = "dailytime";
    LinearLayout mainListChoice;
    TextView selectedMainListChoice, dailyReminder;
    Switch crashSwitch, dailyReminderSwitch;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Calendar dailyReminderCalendar;
    PendingIntent activity;
    Intent intent;
    AlarmManager alarmManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dailyReminderCalendar = Calendar.getInstance();
        preferences = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);
        createNotificationChannel();
        dailyReminderCalendar.set(Calendar.HOUR_OF_DAY,20);
        dailyReminderCalendar.set(Calendar.MINUTE,0);
        intent = new Intent(getActivity(), MainActivity.class);
        activity = PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_settings, container, false);

        Toolbar toolbar = RootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        }

        mainListChoice = RootView.findViewById(R.id.ChooseMainListPref);
        selectedMainListChoice = RootView.findViewById(R.id.chosenMainListPref);
        crashSwitch = RootView.findViewById(R.id.CrashSwitch);
        dailyReminder = RootView.findViewById(R.id.dailyReminderDetail);
        dailyReminderSwitch = RootView.findViewById(R.id.ReminderSwitch);

        crashSwitch.setOnCheckedChangeListener(this);
        dailyReminder.setOnClickListener(this);
        dailyReminderSwitch.setOnCheckedChangeListener(this);
        dailyReminderSwitch.setOnClickListener(this);
        RootView.findViewById(R.id.TextDailyReminder).setOnClickListener(this);

        dailyReminderSwitch.setChecked(preferences.getBoolean(DAILY_REMINDER_BOOL, false));

        setDailyReminderText();

        crashSwitch.setChecked(preferences.getBoolean(MainActivity.CRASH_ENABLED, true));

        String selectedText;
        switch (preferences.getInt(MainActivity.MAIN_LIST_CHOICE, 0)) {
            case 1:
                selectedText = getString(R.string.payment_due);
                break;
            case 2:
                selectedText = getString(R.string.rate);
                break;
            case 3:
                selectedText = getString(R.string.disease);
                break;
            default:
                selectedText = getString(R.string.number_of_healings_today);
        }

        selectedMainListChoice.setText(selectedText);
        mainListChoice.setOnClickListener(this);

        return RootView;
    }

    private void setDailyReminderText() {
        if (dailyReminderSwitch.isChecked()){
            dailyReminder.setText(getString(R.string.daily_time, preferences.getString(DAILY_REMINDER_TIME, DateFormat.getTimeInstance().format(dailyReminderCalendar.getTime()))));
            try {
                dailyReminderCalendar.setTime(DateFormat.getTimeInstance().parse(preferences.getString(DAILY_REMINDER_TIME,"")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else
            dailyReminder.setText(R.string.turn_it_on_to_schedule_a_daily_reminder_to_help_you_update_your_healing_records);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ChooseMainListPref:
                PopupMenu mainListPref = new PopupMenu(getActivity(), selectedMainListChoice);
                mainListPref.getMenuInflater().inflate(R.menu.main_display_preference, mainListPref.getMenu());
                mainListPref.show();
                mainListPref.setOnMenuItemClickListener(this);
                break;
            case R.id.ReminderSwitch:
            case R.id.TextDailyReminder:
            case R.id.dailyReminderDetail:
                if(!dailyReminderSwitch.isChecked())
                    return;
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.setTargetFragment(this,0);
                timePicker.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(),"timePicker");
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        editor = preferences.edit();
        switch (item.getGroupId()) {
            case R.id.mainDisplayPrefGroup:
                selectedMainListChoice.setText(item.getTitle());
                editor.putInt(MainActivity.MAIN_LIST_CHOICE, item.getOrder());
                editor.apply();
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        editor = preferences.edit();
        switch (buttonView.getId()) {
            case R.id.CrashSwitch:
                editor.putBoolean(MainActivity.CRASH_ENABLED, isChecked);
                if (isChecked) {
                    ((MainActivity) Objects.requireNonNull(getActivity())).setCrashEnabled();
                } else
                    Toast.makeText(getActivity(), "Please restart the app for the change to take effect", Toast.LENGTH_SHORT).show();
                editor.apply();
                break;
            case R.id.ReminderSwitch:
                editor.putBoolean(DAILY_REMINDER_BOOL, isChecked).apply();
                if(!isChecked)
                    alarmManager.cancel(activity);
                setDailyReminderText();
                break;
        }
    }

    public void scheduleNotification(Context context, int hour_of_day, int minute, int notificationId) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,hour_of_day);
        calendar.set(Calendar.MINUTE,minute);

        Log.d("NOTIFICATION TIME",calendar.getTime().toString());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.title))
                .setContentText(context.getString(R.string.content))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        Log.d("NOTIFICATION BUILDER",builder.toString());

        builder.setContentIntent(activity);

        Notification notification = builder.build();

        Intent notificationIntent = new Intent(context, MyNotificationPublisher.class);
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, pendingIntent);
        Log.d("NOTIFICATION",alarmManager.toString());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = Objects.requireNonNull(getContext()).getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onTimeSet(DialogFragment dialogFragment) {
        int hour = ((TimePickerFragment) dialogFragment).hour;
        int minute = ((TimePickerFragment) dialogFragment).minute;
        dailyReminderCalendar.set(Calendar.HOUR_OF_DAY, hour);
        dailyReminderCalendar.set(Calendar.MINUTE, minute);
        dailyReminderCalendar.set(Calendar.SECOND,0);
        preferences.edit().putString(DAILY_REMINDER_TIME,DateFormat.getTimeInstance().format(dailyReminderCalendar.getTime())).commit();
        setDailyReminderText();
        scheduleNotification(getActivity(), hour, minute, 0);
    }
}
