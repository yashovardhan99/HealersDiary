package com.yashovardhan99.core.utils

import android.net.Uri

sealed class Request(vararg val path: String) {
    protected val uriBuilder: Uri.Builder
        get() = Uri.Builder().scheme(SCHEME).authority(AUTHORITY).apply {
            for (segment in path) appendPath(segment)
        }

    open fun getUri(): Uri = uriBuilder.build()

    data class NewHealing(val patientId: Long = -1) : Request("activities", "new_healing") {
        override fun getUri(): Uri {
            return if (patientId == -1L) super.getUri()
            else super.uriBuilder.appendQueryParameter(PATIENT_ID, patientId.toString()).build()
        }
    }

    data class NewPayment(val patientId: Long = -1) : Request("activities", "new_payment") {
        override fun getUri(): Uri {
            return if (patientId == -1L) super.getUri()
            else super.uriBuilder.appendQueryParameter(PATIENT_ID, patientId.toString()).build()
        }
    }

    object NewPatient : Request("patients", "new_patient")
    data class NewActivity(val patientId: Long = -1L) : Request("activities", "new_activity") {
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

    data class UpdateHealing(val patientId: Long, val healingId: Long) :
        Request("activities", "edit_healing") {
        override fun getUri(): Uri {
            return super.uriBuilder.appendQueryParameter(PATIENT_ID, patientId.toString())
                .appendQueryParameter(HEALING_ID, healingId.toString()).build()
        }
    }

    data class UpdatePayment(val patientId: Long, val paymentId: Long) :
        Request("activities", "edit_payment") {
        override fun getUri(): Uri {
            return super.uriBuilder.appendQueryParameter(PATIENT_ID, patientId.toString())
                .appendQueryParameter(PAYMENT_ID, paymentId.toString()).build()
        }
    }

    data class UpdatePatient(val patientId: Long) : Request("activities", "edit_patient") {
        override fun getUri(): Uri {
            return super.uriBuilder.appendQueryParameter(PATIENT_ID, patientId.toString()).build()
        }
    }

    object ViewDashboard : Request("dashboard")

    companion object {

        private fun Uri.getLong(key: String): Long {
            return getQueryParameter(key)?.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid/missing query parameters for key=$key")
        }

        fun fromUri(uri: Uri): Request {
            if (uri.scheme != SCHEME)
                throw IllegalArgumentException("Unknown scheme: ${uri.scheme}")
            if (uri.authority != AUTHORITY)
                throw IllegalArgumentException("Unknown authority: ${uri.authority}")
            return when (uri.lastPathSegment) {
                "new_healing" -> if (uri.queryParameterNames.contains(PATIENT_ID)) NewHealing(
                    uri.getLong(
                        PATIENT_ID
                    )
                ) else NewHealing()
                "new_payment" -> if (uri.queryParameterNames.contains(PATIENT_ID)) NewPayment(
                    uri.getLong(
                        PATIENT_ID
                    )
                ) else NewPayment()
                "new_patient" -> NewPatient
                "new_activity" -> if (uri.queryParameterNames.contains(PATIENT_ID)) NewActivity(
                    uri.getLong(
                        PATIENT_ID
                    )
                ) else NewActivity()
                "edit_patient" -> UpdatePatient(uri.getLong(PATIENT_ID))
                "patients" -> ViewPatient(uri.getLong(PATIENT_ID))
                "edit_healing" -> UpdateHealing(uri.getLong(PATIENT_ID), uri.getLong(HEALING_ID))
                "edit_payment" -> UpdatePayment(uri.getLong(PATIENT_ID), uri.getLong(PAYMENT_ID))
                "dashboard" -> ViewDashboard
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
