package com.yashovardhan99.healersdiary.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.yashovardhan99.healersdiary.R

/**
 * Created by Yashovardhan99 on 15-08-2018 as a part of HealersDiary.
 */
class DonateFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_donate_display, container, false)
        val toolbar = rootView.findViewById<Toolbar>(R.id.toolbar)
        val donatePaypal = rootView.findViewById<MaterialButton>(R.id.donate_paypal)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setTitle(R.string.donate)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        }
        donatePaypal.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.paypal_url))))
        }
        return rootView
    }


}
