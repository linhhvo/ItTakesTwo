package me.linhvo.ittakestwo.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun parseDateTimeToLocalTZ(instant: Instant) =
    instant.toLocalDateTime(TimeZone.currentSystemDefault())