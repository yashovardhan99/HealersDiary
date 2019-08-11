package com.yashovardhan99.healersdiary.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.activities.MainActivity
import com.yashovardhan99.healersdiary.activities.MainActivity.Companion.healingsToday
import com.yashovardhan99.healersdiary.activities.MainActivity.Companion.healingsYesterday
import com.yashovardhan99.healersdiary.activities.NewPatient

/**
 * Created by Yashovardhan99 on 05-08-2018 as a part of HealersDiary.
 */
class MainListFragment : Fragment() {

    private lateinit var mAdapter: RecyclerView.Adapter<*>
    private lateinit var today: TextView
    private lateinit var yesterday: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_main_list, container, false)

        val toolbar = rootView.findViewById<Toolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_name)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        }

        //
        //Recycler view setup
        mAdapter = (activity as MainActivity).adapter
        val mRecyclerView: RecyclerView
        mRecyclerView = rootView.findViewById(R.id.recycler_main)
        mRecyclerView.setHasFixedSize(false)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.adapter = mAdapter
        //displays the divider line bw each item
        val itemLine = DividerItemDecoration(mRecyclerView.context, DividerItemDecoration.VERTICAL)
        mRecyclerView.addItemDecoration(itemLine)

        ////new patient record button
        val newPatientButton = rootView.findViewById<FloatingActionButton>(R.id.new_fab)
        newPatientButton.setOnClickListener {
            val newPatientIntent = Intent(activity, NewPatient::class.java)
            startActivity(newPatientIntent)
            //log firebase analytics event
            val newPatient = Bundle().apply {
                putString(FirebaseAnalytics.Param.LOCATION, MainActivity::class.java.name)
                putString(FirebaseAnalytics.Param.ITEM_CATEGORY, MainActivity.PATIENT_RECORD)
                putString(FirebaseAnalytics.Param.CONTENT_TYPE, MainActivity.NEW)
            }
            MainActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, newPatient)
        }


        today = rootView.findViewById(R.id.healingsToday)
        yesterday = rootView.findViewById(R.id.healingsYesterday)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        updateTextFields()
    }

    fun updateTextFields() {
        Log.d("UPDATE", "UDT called with : $healingsToday and $healingsYesterday")
        val res = resources
        today.text = res.getQuantityString(R.plurals.healing, healingsToday, healingsToday, getString(R.string.today))
        yesterday.text = res.getQuantityString(R.plurals.healing, healingsYesterday, healingsYesterday, getString(R.string.yesterday))
    }
}