package com.yashovardhan99.core.analytics

import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.yashovardhan99.core.analytics.AnalyticsEvent.Builder
import com.yashovardhan99.core.analytics.AnalyticsEvent.Import.Requested.trackEvent
import com.yashovardhan99.core.analytics.AnalyticsEvent.Import.Started.trackEvent
import com.yashovardhan99.core.analytics.AnalyticsEvent.Onboarding.ClearAll.trackEvent
import com.yashovardhan99.core.analytics.AnalyticsEvent.Onboarding.Completed.trackEvent
import com.yashovardhan99.core.analytics.AnalyticsEvent.SelectReason.Unknown

/**
 * Utility class to help track analytics events.
 *
 * For common events, use a sub-class with [trackEvent]. For custom events, use [Builder]
 * @since v2.0.0-alpha06
 * @property name Event name
 * @property params Event parameters
 */
sealed class AnalyticsEvent(private val name: String, private val params: Bundle) {
    /**
     * Utility function to help track events.
     *
     * Some classes might provide their own wrapper over this function.
     */
    fun trackEvent() {
        timber.log.Timber.d("Tracking event: $name with params: $params")
        Firebase.analytics.logEvent(name, params)
    }

    /**
     * Used to track import events.
     *
     * Use a sub-class and call [trackEvent] to log an event.
     * @param status The current status
     * @param result The result, if completed
     */
    sealed class Import(status: String, result: String? = null) : AnalyticsEvent("import_v1", bundleOf("status" to status).also {
        if (result != null) it.putString("result", result)
    }) {
        /**
         * Import is Requested.
         *
         * Use [trackEvent] to log the event
         */
        object Requested : Import("requested")

        /**
         * Import started
         *
         * Use [trackEvent] to log the event
         */
        object Started : Import("started")

        /**
         * Import completed
         *
         * Use [trackEvent] to log the event
         * @param isSuccessful Whether the import completed successfully.
         * @param willRetry If the import failed, whether the import will be tried again.
         */
        class Completed(isSuccessful: Boolean, willRetry: Boolean) : Import(
                "completed", when {
            isSuccessful -> "success"
            willRetry -> "retry"
            else -> "failed"
        })
    }

    /**
     * Log events relating to onboarding.
     *
     * Use a sub-class and call [trackEvent] to log the event.
     */
    sealed class Onboarding(status: String) : AnalyticsEvent("onboarding",
            bundleOf("status" to status)) {
        /**
         * To indicate onboarding is completed successfully.
         *
         * Use [trackEvent] to log the event.
         */
        object Completed : Onboarding("completed")

        /**
         * To indicate user has requested to clear all data and has been brought to onboarding.
         *
         * Use [trackEvent] to log the event.
         */
        object ClearAll : Onboarding("clear_all")
    }

    /**
     * Different Types of content which can be tracked.
     *
     * Use [trackCreate], [trackEdit] or [trackDelete] to log an event directly.
     * Use with other classes to log other events.
     *
     * @param bundle The bundle to log.
     * @see Select
     */
    sealed class Content(internal val bundle: Bundle) {
        /**
         * Log a creation event.
         */
        fun trackCreate() = InternalBuild("create", bundle).trackEvent()

        /**
         * Log an edit event.
         */
        fun trackEdit() = InternalBuild("edit", bundle).trackEvent()

        /**
         * Log a delete event.
         */
        fun trackDelete() = InternalBuild("delete", bundle).trackEvent()

        /**
         * A healing content type.
         * @param patientId The id of the patient related to this healing.
         */
        class Healing(patientId: Long) : Content(bundleOf(FirebaseAnalytics.Param.CONTENT_TYPE to "healing",
                FirebaseAnalytics.Param.ITEM_ID to patientId.toString()))

        /**
         * A payment content type.
         * @param patientId The id of the patient related to this payment.
         */
        class Payment(patientId: Long) : Content(bundleOf(FirebaseAnalytics.Param.CONTENT_TYPE to "payment",
                FirebaseAnalytics.Param.ITEM_ID to patientId.toString()))

        /**
         * A patient content type
         * @param id The patient id
         */
        class Patient(id: Long) : Content(bundleOf(FirebaseAnalytics.Param.CONTENT_TYPE to "patient",
                FirebaseAnalytics.Param.ITEM_ID to id.toString()))
    }

    /**
     * Utility class to track different screen events.
     *
     * Use [trackView] to record a screen view event.
     * Can be used as a parameter in other events.
     * @param name The screen name
     * @see Select
     */
    sealed class Screen(internal val name: String) {
        /**
         * Use to record a screen view event
         */
        fun trackView() {
            InternalBuild(FirebaseAnalytics.Event.SCREEN_VIEW,
                    bundleOf(FirebaseAnalytics.Param.SCREEN_NAME to name))
                    .trackEvent()
        }

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

    /**
     * Utility class to indicate the reason a particular content is selected.
     *
     * Used in [Select]. [Unknown] type is not logged.
     */
    sealed class SelectReason(internal val name: String) {
        object Open : SelectReason("open")
        object Edit : SelectReason("edit")
        object Delete : SelectReason("delete")
        object Unknown : SelectReason("unknown")
    }

    /**
     * Event to indicate an item is selected.
     *
     * Use with [trackEvent] to log an event.
     * @param content The content selected
     * @param where The screen where the content was selected
     * @param why The [reason][SelectReason] why the content was selected.
     * @see Content
     */
    class Select(content: Content, where: Screen, why: SelectReason = SelectReason.Unknown) : AnalyticsEvent(
            FirebaseAnalytics.Event.SELECT_CONTENT,
            content.bundle.apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, where.name)
                if (why !is SelectReason.Unknown) putString("action", why.name)
            }
    )

    /**
     * Internal builder used to build events
     *
     * Use with [trackEvent] to log an event.
     */
    private class InternalBuild(name: String, params: Bundle) : AnalyticsEvent(name, params)

    /**
     * A tracking builder used to log custom events.
     *
     * Avoid using this and use a provided Event tracker wherever possible.
     *
     * Use [put] to insert values to track. Call [build] to build an [AnalyticsEvent].
     * Call [trackEvent] to log the event.
     * @param name The event name
     */
    class Builder(private val name: String) {
        private val params = bundleOf()

        /**
         * Insert key-value pairs to track with the event.
         */
        fun put(key: String, value: Any): Builder {
            when (value) {
                is Int -> params.putInt(key, value)
                is String -> params.putString(key, value)
                is Float -> params.putFloat(key, value)
                is Long -> params.putLong(key, value)
            }
            return this
        }

        /**
         * Used to build an [AnalyticsEvent] from this builder.
         *
         * Use with [trackEvent] to log an event.
         */
        fun build(): AnalyticsEvent = InternalBuild(name, params)
    }
}