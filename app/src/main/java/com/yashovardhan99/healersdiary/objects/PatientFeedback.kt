package com.yashovardhan99.healersdiary.objects

import com.google.firebase.Timestamp

import java.text.DateFormat

/**
 * Created by Yashovardhan99 on 28-07-2018 as a part of HealersDiary.
 */
data class PatientFeedback(
        val uid: String,
        val feedback: String,
        val timestamp: Timestamp,
        val verified: Boolean
)
