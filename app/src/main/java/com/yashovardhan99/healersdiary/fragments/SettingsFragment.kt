package com.yashovardhan99.healersdiary.fragments

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.activities.MainActivity
import com.yashovardhan99.healersdiary.helpers.MyNotificationPublisher
import java.text.DateFormat
import java.text.ParseException
import java.util.*

/**
 * Created by Yashovardhan99 on 20/10/18 as a part of HealersDiary.
 */
class SettingsFragment : Fragment(), View.OnClickListener, PopupMenu.OnMenuItemClickListener, CompoundButton.OnCheckedChangeListener, TimePickerFragment.TimePickerListener {
    private lateinit var mainListChoice: LinearLayout
    private lateinit var selectedMainListChoice: TextView
    private lateinit var dailyReminder: TextView
    private lateinit var crashSwitch: Switch
    private lateinit var dailyReminderSwitch: Switch
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var dailyReminderCalendar: Calendar
    private lateinit var activity: PendingIntent
    private lateinit var intent: Intent
    private lateinit var alarmManager: AlarmManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dailyReminderCalendar = Calendar.getInstance()
        preferences = getActivity()!!.getPreferences(Context.MODE_PRIVATE)
        createNotificationChannel()
        dailyReminderCalendar.set(Calendar.HOUR_OF_DAY, 20)
        dailyReminderCalendar.set(Calendar.MINUTE, 0)
        intent = Intent(getActivity(), MainActivity::class.java)
        activity = PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        alarmManager = getActivity()!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)

        val toolbar = rootView.findViewById<Toolbar>(R.id.toolbar)
        (getActivity()!! as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (getActivity() as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setTitle(R.string.settings)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        }

        mainListChoice = rootView.findViewById(R.id.ChooseMainListPref)
        selectedMainListChoice = rootView.findViewById(R.id.chosenMainListPref)
        crashSwitch = rootView.findViewById(R.id.CrashSwitch)
        dailyReminder = rootView.findViewById(R.id.dailyReminderDetail)
        dailyReminderSwitch = rootView.findViewById(R.id.ReminderSwitch)

        crashSwitch.setOnCheckedChangeListener(this)
        dailyReminder.setOnClickListener(this)
        dailyReminderSwitch.setOnCheckedChangeListener(this)
        dailyReminderSwitch.setOnClickListener(this)
        rootView.findViewById<View>(R.id.TextDailyReminder).setOnClickListener(this)

        dailyReminderSwitch.isChecked = preferences.getBoolean(DAILY_REMINDER_BOOL, false)

        setDailyReminderText()

        crashSwitch.isChecked = preferences.getBoolean(MainActivity.CRASH_ENABLED, true)

        val selectedText: String = when (preferences.getInt(MainActivity.MAIN_LIST_CHOICE, 0)) {
            1 -> getString(R.string.payment_due)
            2 -> getString(R.string.rate)
            3 -> getString(R.string.disease)
            else -> getString(R.string.number_of_healings_today)
        }

        selectedMainListChoice.text = selectedText
        mainListChoice.setOnClickListener(this)

        return rootView
    }

    private fun setDailyReminderText() {
        if (dailyReminderSwitch.isChecked) {
            dailyReminder.text = getString(R.string.daily_time, preferences.getString(DAILY_REMINDER_TIME, DateFormat.getTimeInstance().format(dailyReminderCalendar.time)))
            try {
                dailyReminderCalendar.time = DateFormat.getTimeInstance().parse(preferences.getString(DAILY_REMINDER_TIME, ""))
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        } else
            dailyReminder.setText(R.string.turn_it_on_to_schedule_a_daily_reminder_to_help_you_update_your_healing_records)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ChooseMainListPref -> {
                val mainListPref = PopupMenu(getActivity(), selectedMainListChoice)
                mainListPref.menuInflater.inflate(R.menu.main_display_preference, mainListPref.menu)
                mainListPref.show()
                mainListPref.setOnMenuItemClickListener(this)
            }
            R.id.ReminderSwitch, R.id.TextDailyReminder, R.id.dailyReminderDetail -> {
                if (!dailyReminderSwitch.isChecked)
                    return
                val timePicker = TimePickerFragment()
                timePicker.setTargetFragment(this, 0)
                timePicker.show(getActivity()!!.supportFragmentManager, "timePicker")
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        editor = preferences.edit()
        when (item.groupId) {
            R.id.mainDisplayPrefGroup -> {
                selectedMainListChoice.text = item.title
                editor.putInt(MainActivity.MAIN_LIST_CHOICE, item.order)
                editor.apply()
            }
        }
        return false
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        editor = preferences.edit()
        when (buttonView.id) {
            R.id.CrashSwitch -> {
                editor.putBoolean(MainActivity.CRASH_ENABLED, isChecked)
                if (isChecked) {
                    (getActivity()!! as MainActivity).setCrashEnabled()
                } else
                    Toast.makeText(getActivity(), "Please restart the app for the change to take effect", Toast.LENGTH_SHORT).show()
                editor.apply()
            }
            R.id.ReminderSwitch -> {
                editor.putBoolean(DAILY_REMINDER_BOOL, isChecked).apply()
                if (!isChecked)
                    alarmManager.cancel(activity)
                setDailyReminderText()
            }
        }
    }

    private fun scheduleNotification(context: Context?, hour_of_day: Int, minute: Int, notificationId: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour_of_day)
        calendar.set(Calendar.MINUTE, minute)

        Log.d("NOTIFICATION TIME", calendar.time.toString())
        val builder = NotificationCompat.Builder(context!!, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.title))
                .setContentText(context.getString(R.string.content))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        Log.d("NOTIFICATION BUILDER", builder.toString())

        builder.setContentIntent(activity)

        val notification = builder.build()

        val notificationIntent = Intent(context, MyNotificationPublisher::class.java)
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION_ID, notificationId)
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION, notification)
        val pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
        Log.d("NOTIFICATION", alarmManager.toString())
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context!!.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ApplySharedPref")
    override fun onTimeSet(dialogFragment: DialogFragment) {
        val hour = (dialogFragment as TimePickerFragment).hour
        val minute = dialogFragment.minute
        dailyReminderCalendar.set(Calendar.HOUR_OF_DAY, hour)
        dailyReminderCalendar.set(Calendar.MINUTE, minute)
        dailyReminderCalendar.set(Calendar.SECOND, 0)
        preferences.edit().putString(DAILY_REMINDER_TIME, DateFormat.getTimeInstance().format(dailyReminderCalendar.time)).commit()
        setDailyReminderText()
        scheduleNotification(getActivity(), hour, minute, 0)
    }

    companion object {
        private const val CHANNEL_ID = "dailyReminderChannel"
        private const val DAILY_REMINDER_BOOL = "dailyreminderboolean"
        private const val DAILY_REMINDER_TIME = "dailytime"
    }
}
