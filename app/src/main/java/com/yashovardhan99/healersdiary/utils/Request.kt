package com.yashovardhan99.healersdiary.utils

import android.net.Uri

sealed class Request(val path: String) {
    data class NewHealing(val patientId: Long = -1) : Request("new_healing") {
        override fun getUri(): Uri {
            return if (patientId == -1L) super.getUri()
            else super.uriBuilder.appendQueryParameter(PATIENT_ID, patientId.toString()).build()
        }
    }

    data class NewPayment(val patientId: Long = -1) : Request("new_payment") {
        override fun getUri(): Uri {
            return if (patientId == -1L) super.getUri()
            else super.uriBuilder.appendQueryParameter(PATIENT_ID, patientId.toString()).build()
        }
    }

    object NewPatient : Request("new_patient")
    data class NewActivity(val patientId: Long = -1L) : Request("new_activity") {
        override fun getUri(): Uri {
            return if (patientId == -1L) super.getUri()
            else super.uriBuilder.appendQueryParameter(PATIENT_ID, patientId.toString()).build()
        }
    }

    data class ViewPatient(val patientId: Long) : Request("patients") {
        override fun getUri(): Uri {
            return super.uriBuilder.appendQueryParameter(PATIENT_ID, patientId.toString()).build()
        }
    }

    data class UpdateHealing(val patientId: Long, val healingId: Long) : Request("edit_healing")
    data class UpdatePayment(val patientId: Long, val paymentId: Long) : Request("edit_payment")
    data class UpdatePatient(val patientId: Long) : Request("edit_patient")

    open fun getUri(): Uri = uriBuilder.build()
    private val uriBuilder = Uri.Builder()
            .scheme(SCHEME)
            .authority(AUTHORITY)
            .appendPath(path)


    companion object {
        private fun Uri.getLong(key: String): Long {
            return getQueryParameter(key)?.toLongOrNull()
                    ?: throw IllegalArgumentException("Invalid/missing query parameters for key=$key")
        }

        fun fromUri(uri: Uri): Request {
            if (uri.scheme != SCHEME) throw IllegalArgumentException("Unknown scheme: ${uri.scheme}")
            if (uri.authority != AUTHORITY) throw IllegalArgumentException("Unknown authority: ${uri.authority}")
            return when (uri.lastPathSegment) {
                "new_healing" -> if (uri.queryParameterNames.contains(PATIENT_ID)) NewHealing(uri.getLong(PATIENT_ID)) else NewHealing()
                "new_payment" -> if (uri.queryParameterNames.contains(PATIENT_ID)) NewPayment(uri.getLong(PATIENT_ID)) else NewPayment()
                "new_patient" -> NewPatient
                "new_activity" -> if (uri.queryParameterNames.contains(PATIENT_ID)) NewActivity(uri.getLong(PATIENT_ID)) else NewActivity()
                "edit_patient" -> UpdatePatient(uri.getLong(PATIENT_ID))
                "patients" -> ViewPatient(uri.getLong(PATIENT_ID))
                "edit_healing" -> UpdateHealing(uri.getLong(PATIENT_ID), uri.getLong(HEALING_ID))
                "edit_payment" -> UpdatePayment(uri.getLong(PATIENT_ID), uri.getLong(PAYMENT_ID))
                else -> throw java.lang.IllegalArgumentException("Unknown path: ${uri.path}")
            }
        }

        private const val AUTHORITY = "com.yashovardhan99.healersdiary"
        private const val SCHEME = "healersdiary"
        private const val PATIENT_ID = "patient_id"
        private const val HEALING_ID = "healing_id"
        private const val PAYMENT_ID = "payment_id"
    }
}

