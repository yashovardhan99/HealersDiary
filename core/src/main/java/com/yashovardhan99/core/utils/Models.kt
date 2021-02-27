package com.yashovardhan99.core.utils

import java.util.*

sealed class HealingParent {
    data class HealingSeparator(val heading: String) : HealingParent()
    data class Healing(
            val id: Long,
            val time: Date,
            val charge: Long,
            val notes: String,
            val patientId: Long) : HealingParent() {
        fun toDatabaseHealing() = com.yashovardhan99.core.database.Healing(id, time, charge, notes, patientId)

        companion object {
            fun com.yashovardhan99.core.database.Healing.toUiHealing(): Healing {
                return Healing(id, time, charge, notes, patientId)
            }
        }
    }
}

sealed class PaymentParent {
    data class PaymentSeparator(val heading: String) : PaymentParent()
    data class Payment(
            val id: Long,
            val time: Date,
            val amount: Long,
            val notes: String,
            val patientId: Long) : PaymentParent() {
        fun toDatabasePayment() = com.yashovardhan99.core.database.Payment(id, time, amount, notes, patientId)

        companion object {
            fun com.yashovardhan99.core.database.Payment.toUiPayment(): Payment {
                return Payment(id, time, amount, notes, patientId)
            }
        }
    }
}