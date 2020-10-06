package me.ksviety.nutrition_diary.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import me.ksviety.nutrition_diary.extension.asPeriod
import java.time.Instant
import java.time.LocalDate

/**
 * @param timestamp Unix epoch seconds (by default set to now)
 */
@Entity(tableName = "intakes")
data class Intake(
		var name: String,
		var calories: Float,
		@ColumnInfo(name = "is_necessary")
		var isNecessary: Boolean,
		var type: IntakeType,
		@PrimaryKey(autoGenerate = true)
		val id: Int = 0,
		val timestamp: Long = Instant.now().epochSecond
)

fun Intake.isWithinDate(date: LocalDate) = timestamp in date.asPeriod

fun Intake.isNotWithinDate(date: LocalDate) = !isWithinDate(date)