package com.yashovardhan99.healersdiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.common.SignInButton
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.activities.Login

class SignInButtonFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val rootView = inflater.inflate(R.layout.fragment_sign_in_button, container, false)
        val gsb = rootView.findViewById<SignInButton>(R.id.GoogleSignInButton)
        gsb.setOnClickListener { (activity as Login).signInWithGoogle() }

        return rootView
    }

}
