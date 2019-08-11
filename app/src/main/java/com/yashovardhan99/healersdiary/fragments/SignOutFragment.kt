package com.yashovardhan99.healersdiary.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.activities.Login

/**
 * Created by Yashovardhan99 on 15-08-2018 as a part of HealersDiary.
 */
class SignOutFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val ad = AlertDialog.Builder(activity!!)
                .setIcon(R.drawable.ic_power_settings_new_black_24dp)
                .setTitle(R.string.sign_out)
                //                .setMessage()
                .setPositiveButton(R.string.sign_out) { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(activity, Login::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
                    activity!!.finishAffinity()
                }
                .setNegativeButton(R.string.cancel) { _, _ -> dismiss() }.create()
        ad.setMessage("This will sign you out of the app. If you are connected to the internet, your data has been synced online and will not be deleted.")
        return ad
    }
}