package me.ksviety.nutrition_diary.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.ksviety.nutrition_diary.data.model.Intake
import me.ksviety.nutrition_diary.data.model.IntakeType

@Database(
		entities = [Intake::class],
		version = 1,
		exportSchema = false
)
@TypeConverters(
		IntakeType.Converter::class
)
abstract class DiaryDatabase : RoomDatabase() {
	abstract fun getDao(): DiaryDao

	companion object {
		@Volatile
		private var instance: DiaryDatabase? = null

		operator fun invoke(context: Context) =
				instance ?: createDatabase(context)

		private fun createDatabase(context: Context) =
				synchronized(this) {
					val builder = Room.databaseBuilder(
							context.applicationContext,
							DiaryDatabase::class.java,
							"nutrition_diary.db"
					)

					instance ?: builder.build()
				}
	}
}