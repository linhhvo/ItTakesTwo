package me.linhvo.ittakestwo.database.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun parseDateTimeToLocalTZ(milliseconds: Long) =
    Instant.fromEpochMilliseconds(milliseconds).toLocalDateTime(TimeZone.currentSystemDefault())