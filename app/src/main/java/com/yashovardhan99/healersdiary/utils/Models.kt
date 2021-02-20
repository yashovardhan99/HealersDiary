package com.yashovardhan99.healersdiary.utils

import java.util.*

sealed class HealingParent {
    data class HealingSeparator(val heading: String) : HealingParent()
    data class Healing(
            val id: Long,
            val time: Date,
            val charge: Long,
            val notes: String,
            val patientId: Long) : HealingParent() {
        fun toDatabaseHealing() = com.yashovardhan99.healersdiary.database.Healing(id, time, charge, notes, patientId)

        companion object {
            fun com.yashovardhan99.healersdiary.database.Healing.toUiHealing(): Healing {
                return Healing(id, time, charge, notes, patientId)
            }
        }
    }
}