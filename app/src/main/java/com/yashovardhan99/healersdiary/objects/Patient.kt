package com.yashovardhan99.healersdiary.objects

/**
 * Created by Yashovardhan99 on 23-05-2018 as a part of HealersDiary.
 * This is a class meant for creating patient objects. To be modified as needed.
 */
data class Patient(
        var name: String = "",
        var uid: String = "",
        var disease: String = "",
        var healingsToday: Int = 0,
        var rate: Double = 0.toDouble(),
        var due: Double = 0.toDouble()
)
