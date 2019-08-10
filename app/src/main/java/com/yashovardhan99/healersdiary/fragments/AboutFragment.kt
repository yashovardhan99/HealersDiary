package com.yashovardhan99.healersdiary.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.yashovardhan99.healersdiary.BuildConfig
import com.yashovardhan99.healersdiary.R
import java.util.*

/**
 * Created by Yashovardhan99 on 18-08-2018 as a part of HealersDiary.
 */
class AboutFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_about, container, false)
        val imageAttrib = rootView.findViewById<TextView>(R.id.imageCour)
        imageAttrib.movementMethod = LinkMovementMethod.getInstance()
        val version = rootView.findViewById<TextView>(R.id.appVersionString)
        version.text = getString(R.string.version_s, BuildConfig.VERSION_NAME)
        rootView.findViewById<View>(R.id.oss).setOnClickListener(this)
        rootView.findViewById<View>(R.id.privacy).setOnClickListener(this)
        rootView.findViewById<View>(R.id.eula).setOnClickListener(this)

        val toolbar = rootView.findViewById<Toolbar>(R.id.toolbar)
        (Objects.requireNonNull(activity) as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setTitle(R.string.about)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        }
        return rootView
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.oss -> startActivity(Intent(activity, OssLicensesMenuActivity::class.java))
            R.id.privacy -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yashovardhan99/HealersDiary/blob/master/PRIVACY%20POLICY.md")))
            R.id.eula -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yashovardhan99/HealersDiary/blob/master/EULA.md")))
        }
    }
}
