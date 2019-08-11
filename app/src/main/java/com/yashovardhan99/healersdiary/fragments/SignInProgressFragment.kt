package com.yashovardhan99.healersdiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yashovardhan99.healersdiary.R

/**
 * Created by Yashovardhan99 on 05-08-2018 as a part of HealersDiary.
 */
class SignInProgressFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.signing_in_progress, container, false)
    }
}
