package me.ksviety.nutrition_diary.extension

import java.time.LocalDate
import java.time.ZoneOffset

/**
 * LongRange of epoch seconds within the date. Uses [ZoneOffset.systemDefault] as the timezone offset
 */
val LocalDate.asPeriod: LongRange
	get() {
		val zone = ZoneOffset.systemDefault()
		val start = atStartOfDay(zone).toEpochSecond()
		val end = tomorrow.atStartOfDay(zone).toEpochSecond() - 1L

		return LongRange(start, end)
	}

private val LocalDate.tomorrow: LocalDate
	get() = plusDays(1)