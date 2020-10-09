package me.ksviety.nutrition_diary.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset

@Entity(tableName = "intakes")
data class Intake(
		val name: String,
		val calories: Float,
		@ColumnInfo(name = "is_necessary")
		val isNecessary: Boolean,
		val type: IntakeType,
		@PrimaryKey(autoGenerate = true)
		val id: Int = 0,
		val datetime: LocalDateTime = LocalDateTime.now()
) {

	fun setName(name: String) =
			Intake(name, calories, isNecessary, type, id, datetime)

	fun setCalories(calories: Float) =
			Intake(name, calories, isNecessary, type, id, datetime)

	fun setNecessity(necessity: Boolean) =
			Intake(name, calories, necessity, type, id, datetime)

	fun setType(type: IntakeType) =
			Intake(name, calories, isNecessary, type, id, datetime)

	class Converter {

		@TypeConverter
		fun fromDatetime(datetime: LocalDateTime) = datetime.toEpochSecond(ZoneOffset.UTC)

		@TypeConverter
		fun toDatetime(timestamp: Long) =
				LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC)!!
	}
}