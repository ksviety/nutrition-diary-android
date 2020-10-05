package me.ksviety.nutrition_diary.data.model

import androidx.room.TypeConverter

enum class IntakeType {
	Food,
	Drink;

	class Converter {

		@TypeConverter
		fun fromType(type: IntakeType) = type.ordinal.toByte()

		@TypeConverter
		fun fromByte(ordinal: Byte) =
				values().find { it.ordinal == ordinal.toInt() } ?: throw IllegalArgumentException()
	}
}