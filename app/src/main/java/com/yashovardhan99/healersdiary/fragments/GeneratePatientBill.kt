package com.yashovardhan99.healersdiary.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.activities.MainActivity

/**
 * A simple [Fragment] subclass.
 */
class GeneratePatientBill : DialogFragment(), View.OnClickListener {

    internal lateinit var uid: String
    private lateinit var includeLogs: Switch


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val rootView = inflater.inflate(R.layout.fragment_generate_patient_bill, container, false)
        val thisMonth = rootView.findViewById<Button>(R.id.thisMonthBillButton)
        val lastMonth = rootView.findViewById<Button>(R.id.lastMonthBillButton)
        val all = rootView.findViewById<Button>(R.id.allBillButton)
        includeLogs = rootView.findViewById(R.id.include_healing_logs_switch)
        thisMonth.setOnClickListener(this)
        lastMonth.setOnClickListener(this)
        all.setOnClickListener(this)
        assert(arguments != null)
        uid = arguments!!.getString(MainActivity.PATIENT_UID)!!
        return rootView
    }

    override fun onClick(v: View) {
        val patientBillView = PatientBillView()
        val bundle = Bundle()
        bundle.putString(MainActivity.PATIENT_UID, uid)
        bundle.putInt(MainActivity.BILL_TYPE, v.id)
        bundle.putBoolean(MainActivity.INCLUDE_LOGS, includeLogs.isChecked)
        patientBillView.arguments = bundle
        patientBillView.show(activity?.supportFragmentManager, "billview")
        dismiss()
    }
}
