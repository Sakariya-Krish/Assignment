package com.example.assignment.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateUtils {
    fun toLocalDate(timestamp: Long): LocalDate {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    fun isSameDay(t1: Long, t2: Long): Boolean {
        return toLocalDate(t1) == toLocalDate(t2)
    }

    fun getDaysRemaining(expiryTimestamp: Long): Long {
        val today = LocalDate.now()
        val expiry = toLocalDate(expiryTimestamp)
        return ChronoUnit.DAYS.between(today, expiry)
    }

    fun formatDisplayDate(timestamp: Long): String {
        val date = toLocalDate(timestamp)
        val monthName = date.month.name.lowercase(Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        return "${date.dayOfMonth} $monthName ${date.year}"
    }
}

enum class ExpiryStatus {
    EXPIRED,
    EXPIRING_TODAY,
    EXPIRING_SOON,
    FRESH
}

fun getExpiryStatus(expiryTimestamp: Long): ExpiryStatus {
    val daysLeft = DateUtils.getDaysRemaining(expiryTimestamp)
    return when {
        daysLeft < 0 -> ExpiryStatus.EXPIRED
        daysLeft == 0L -> ExpiryStatus.EXPIRING_TODAY
        daysLeft <= 3L -> ExpiryStatus.EXPIRING_SOON
        else -> ExpiryStatus.FRESH
    }
}
