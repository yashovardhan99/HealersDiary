package com.yashovardhan99.healersdiary.utils

import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import timber.log.Timber

sealed class AnalyticsEvent(private val name: String, private val params: Bundle) {
    fun trackEvent() {
        Timber.d("Tracking event: $name with params: $params")
        Firebase.analytics.logEvent(name, params)
    }

    sealed class Import(status: String, result: String? = null) : AnalyticsEvent("import_v1", bundleOf("status" to status).also {
        if (result != null) it.putString("result", result)
    }) {

        object Requested : Import("requested")
        object Started : Import("started")
        class Completed(isSuccessful: Boolean, willRetry: Boolean) :
                Import("completed", when {
                    isSuccessful -> "success"
                    willRetry -> "retry"
                    else -> "failed"
                })
    }

    sealed class Onboarding(status: String) : AnalyticsEvent("onboarding",
            bundleOf("status" to status)) {
        object Completed : Onboarding("completed")
        object ClearAll : Onboarding("clear_all")
    }

    sealed class Content(internal val bundle: Bundle) {
        object Healing : Content(bundleOf(FirebaseAnalytics.Param.CONTENT_TYPE to "healing"))
        object Payment : Content(bundleOf(FirebaseAnalytics.Param.CONTENT_TYPE to "payment"))
        class Patient(id: Long) : Content(bundleOf(FirebaseAnalytics.Param.CONTENT_TYPE to "patient",
                FirebaseAnalytics.Param.ITEM_ID to id.toString()))
    }

    sealed class Screen(internal val name: String) {
        object Dashboard : Screen("dashboard")
        object PatientList : Screen("patient_list")
        object PatientDetail : Screen("patient_detail")
        object Analytics : Screen("analytics")
        object Settings : Screen("settings")
        object HealingLog : Screen("healing_log")
        object PaymentLog : Screen("payment_log")
        object CreateChoosePatient : Screen("create_choose_patient")
        object CreateChooseActivity : Screen("create_choose_activity")
        object CreatePatient : Screen("create_patient")
        object CreateHealing : Screen("create_healing")
        object CreatePayment : Screen("create_payment")
        object Onboarding : Screen("onboarding")
        object ImportV1 : Screen("import_from_v1")
    }

    class ScreenView(screen: Screen) : AnalyticsEvent(FirebaseAnalytics.Event.SCREEN_VIEW,
            bundleOf(FirebaseAnalytics.Param.SCREEN_NAME to screen.name))

    sealed class SelectReason(internal val name: String) {
        object Open : SelectReason("open")
        object Edit : SelectReason("edit")
        object Delete : SelectReason("delete")
        object Unknown : SelectReason("unknown")
    }

    class Select(content: Content, where: Screen, why: SelectReason = SelectReason.Unknown) : AnalyticsEvent(
            FirebaseAnalytics.Event.SELECT_CONTENT,
            content.bundle.apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, where.name)
                if (why !is SelectReason.Unknown) putString("action", why.name)
            }
    )

    class InternalBuild(name: String, params: Bundle) : AnalyticsEvent(name, params)

    class Builder(private val name: String) {
        private val params = bundleOf()

        fun put(key: String, value: Any): Builder {
            when (value) {
                is Int -> params.putInt(key, value)
                is String -> params.putString(key, value)
                is Float -> params.putFloat(key, value)
                is Long -> params.putLong(key, value)
            }
            return this
        }

        fun build(): AnalyticsEvent = InternalBuild(name, params)
    }
}