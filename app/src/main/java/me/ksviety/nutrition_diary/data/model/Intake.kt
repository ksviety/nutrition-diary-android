package me.ksviety.nutrition_diary.data.model

import android.os.Parcel
import android.os.Parcelable
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
		val id: Int = -1,
		val datetime: LocalDateTime = LocalDateTime.now()
) : Parcelable {

	constructor(parcel: Parcel) : this(
			parcel.readString() ?: throw IllegalArgumentException(),
			parcel.readFloat(),
			parcel.readByte() != 0.toByte(),
			IntakeType.values()[parcel.readInt()],
			parcel.readInt(),
			LocalDateTime.ofEpochSecond(parcel.readLong(), 0, ZoneOffset.UTC)) {
	}

	fun setName(name: String) =
			Intake(name, calories, isNecessary, type, id, datetime)

	fun setCalories(calories: Float) =
			Intake(name, calories, isNecessary, type, id, datetime)

	fun setNecessity(necessity: Boolean) =
			Intake(name, calories, necessity, type, id, datetime)

	fun setType(type: IntakeType) =
			Intake(name, calories, isNecessary, type, id, datetime)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(name)
		parcel.writeFloat(calories)
		parcel.writeByte(if (isNecessary) 1 else 0)
		parcel.writeInt(type.ordinal)
		parcel.writeInt(id)
		parcel.writeLong(datetime.toEpochSecond(ZoneOffset.UTC))
	}

	override fun describeContents(): Int {
		return 0
	}

	class Converter {

		@TypeConverter
		fun fromDatetime(datetime: LocalDateTime) = datetime.toEpochSecond(ZoneOffset.UTC)

		@TypeConverter
		fun toDatetime(timestamp: Long) =
				LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC)!!
	}

	companion object {
		val placeholder: Intake
			get() = Intake("", 0.0f, false, IntakeType.Food)

		val CREATOR = object : Parcelable.Creator<Intake> {

			override fun createFromParcel(parcel: Parcel): Intake {
				return Intake(parcel)
			}

			override fun newArray(size: Int): Array<Intake?> {
				return arrayOfNulls(size)
			}
		}
	}
}