package me.ksviety.nutrition_diary.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import me.ksviety.nutrition_diary.data.model.Intake

@Dao
interface DiaryDao {

	@Query("SELECT * FROM intakes ORDER BY datetime ASC")
	fun getIntakes(): LiveData<List<Intake>>

	@Query("SELECT * FROM intakes ORDER BY datetime ASC")
	suspend fun loadIntakes(): List<Intake>

	@Update
	suspend fun update(vararg intakes: Intake)

	@Insert(onConflict = OnConflictStrategy.ABORT)
	suspend fun insert(vararg intakes: Intake)

	@Delete
	suspend fun delete(vararg intakes: Intake)
}