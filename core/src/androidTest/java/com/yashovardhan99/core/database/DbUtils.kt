package com.yashovardhan99.core.database

import java.time.LocalDateTime
import java.time.Month

private val dateEarly = LocalDateTime.of(2019, Month.MAY, 24, 14, 25)
private val dateMid = LocalDateTime.of(2020, Month.JULY, 31, 19, 50)
private val dateLate = LocalDateTime.of(2021, Month.FEBRUARY, 28, 23, 4)

internal object DbUtils {
    val patients = listOf(
        Patient(0, "Amit", 100_00, 5000_00, "", dateEarly, dateEarly),
        Patient(0, "Ankur", 500_00, 20_000_00, "", dateEarly, dateMid),
        Patient(0, "Deepak", 500_00, 200_00, "", dateEarly, dateLate),
        Patient(0, "Rohit", 300_00, 0, "", dateMid, dateMid),
        Patient(0, "Mohit", 400_00, 1200_00, "", dateMid, dateMid),
        Patient(0, "Rohit", 600_00, 9530_00, "", dateMid, dateLate),
        Patient(0, "Soumya", 700_00, 9000_00, "", dateMid, dateLate),
        Patient(0, "Disha", 1000_00, 10_000_00, "", dateLate, dateLate),
        Patient(0, "Rajput", 300_00, 7500_00, "", dateLate, dateLate),
        Patient(0, "Yash", 300_00, 6000_00, "", dateLate, dateLate),
    )
}