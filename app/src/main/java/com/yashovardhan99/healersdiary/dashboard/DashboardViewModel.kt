package com.yashovardhan99.healersdiary.dashboard

import androidx.lifecycle.ViewModel
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.setToStartOfDay
import com.yashovardhan99.core.setToStartOfLastMonth
import com.yashovardhan99.core.setToStartOfMonth
import com.yashovardhan99.core.toLocalDateTime
import com.yashovardhan99.core.utils.ActivityParent
import com.yashovardhan99.core.utils.HealingParent
import com.yashovardhan99.core.utils.PaymentParent
import com.yashovardhan99.core.utils.Request
import com.yashovardhan99.core.utils.Stat.Companion.earnedLastMonth
import com.yashovardhan99.core.utils.Stat.Companion.earnedThisMonth
import com.yashovardhan99.core.utils.Stat.Companion.healingsThisMonth
import com.yashovardhan99.core.utils.Stat.Companion.healingsToday
import com.yashovardhan99.core.utils.Utils.insertSeparators
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

/**
 * Viewmodel shared between all top level destinations and some inner destinations
 * @param repository The dashboard repository consumed
 * @see MainActivity
 * @see DashboardRepository
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(repository: DashboardRepository) : ViewModel() {
    private val patientsFlow = repository.patients // just a list of all patients

    // get dates for starting today, this month and last month
    // used in calculations for stats
    private val today = Calendar.getInstance().apply {
        setToStartOfDay()
    }
    private val thisMonth = Calendar.getInstance().apply { setToStartOfMonth() }
    private val lastMonth = Calendar.getInstance().apply { setToStartOfLastMonth() }

    // healing and payments flow starting last month (eg. If it's february, include all activity starting January
    private val healings = repository.getHealingsStarting(lastMonth.time)

    // current selected patient (for inner pages)
    private var currentPatientId = -1L

    /**
     * Patients list sorted by last modified
     * Includes - associated healings and payments done
     */
    val patientsList = healings.combine(patientsFlow) { healings, patients ->
        val patientsMap = patients.associateBy { it.id }
        Timber.d(patientsMap.toString())
        Timber.d("Healings 1 = $healings")
        val patientWithHealings = healings.groupBy {
            patientsMap[it.patientId] ?: Patient.MissingPatient
        }
        // setting no. of healings today and this month for patients list page
        patients.map { patient ->
            val today = patientWithHealings[patient]?.count { it.time >= today.time } ?: 0
            val thisMonth = patientWithHealings[patient]?.count { it.time >= thisMonth.time } ?: 0
            patient.copy(healingsToday = today, healingsThisMonth = thisMonth)
        }.sortedByDescending { it.lastModified }
    }

    val activitiesFlow = repository
        .getActivitiesStarting(lastMonth.time.toLocalDateTime().toLocalDate())
        .map { list ->
            list.map {
                it.toUiActivity(patientsFlow.first())
            }.insertSeparators()
        }
    val statsFlow = patientsFlow.combine(activitiesFlow) { patients, activities ->
        val healings: List<ActivityParent.Activity> =
            activities.filterIsInstance<ActivityParent.Activity>()
                .filter { it.type is ActivityParent.Activity.Type.HEALING }
        // val payments: List<ActivityParent.Activity> =
        //     activities.filterIsInstance<ActivityParent.Activity>()
        //         .filter { it.type is ActivityParent.Activity.Type.PAYMENT }
        val healingsThisMonth = healings.filter { !it.time.before(thisMonth.time) }
        val healingsLastMonth = healings - healingsThisMonth
        val healingsToday = healings.filter { !it.time.before(today.time) }
        val earningsThisMonth = healingsThisMonth.sumOf { it.amount }.toDouble() / 100
        val earningsLastMonth = healingsLastMonth.sumOf { it.amount }.toDouble() / 100
        val stats = listOf(
            healingsToday(healingsToday.size), healingsThisMonth(healingsThisMonth.size),
            earnedThisMonth(earningsThisMonth), earnedLastMonth(earningsLastMonth)
        )
        stats
    }.onStart {
        val emptyStats = listOf(
            healingsToday(0),
            healingsThisMonth(0),
            earnedThisMonth(0.0),
            earnedLastMonth(0.0)
        )
        emit(emptyStats)
    }

    private val _requests = MutableStateFlow<Request?>(null)
    val requests: StateFlow<Request?> = _requests
    fun viewPatient(patientId: Long) {
        _requests.value = Request.ViewPatient(patientId)
    }

    fun newHealing(patientId: Long) {
        _requests.value = Request.NewHealing(patientId)
    }

    fun newPayment(patientId: Long) {
        _requests.value = Request.NewPayment(patientId)
    }

    fun resetRequest() {
        _requests.value = null
    }

    fun addNewPatient() {
        _requests.value = Request.NewPatient
    }

    fun editPatient(patientId: Long) {
        _requests.value = Request.UpdatePatient(patientId)
    }

    fun setPatientId(patientId: Long) {
        currentPatientId = patientId
    }

    fun resetPatientId() {
        currentPatientId = -1L
    }

    fun getPatientId(): Long = currentPatientId
    fun editHealing(healing: HealingParent.Healing) {
        _requests.value = Request.UpdateHealing(healing.patientId, healing.id)
    }

    fun editPayment(payment: PaymentParent.Payment) {
        _requests.value = Request.UpdatePayment(payment.patientId, payment.id)
    }

    init {
        Timber.d(
            """
                DATES: Today = ${today.time}
                This month = ${thisMonth.time}
                Last month = ${lastMonth.time}
            """.trimIndent()
        )
    }
}
