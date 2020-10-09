package me.ksviety.nutrition_diary.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset

@Entity(tableName = "intakes")
data class Intake(
		var name: String,
		var calories: Float,
		@ColumnInfo(name = "is_necessary")
		var isNecessary: Boolean,
		var type: IntakeType,
		@PrimaryKey(autoGenerate = true)
		val id: Int = 0,
		val datetime: LocalDateTime = LocalDateTime.now()
) {

	class Converter {

		@TypeConverter
		fun fromDatetime(datetime: LocalDateTime) = datetime.toEpochSecond(ZoneOffset.UTC)

		@TypeConverter
		fun toDatetime(timestamp: Long) =
				LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC)!!
	}
}