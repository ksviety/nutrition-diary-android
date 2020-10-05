package me.ksviety.nutrition_diary.data

import me.ksviety.nutrition_diary.data.database.DiaryDao
import me.ksviety.nutrition_diary.data.model.Intake

class DiaryRepository(private val dao: DiaryDao) {
	val intakes = dao.getIntakes()

	suspend fun loadIntakes() = dao.loadIntakes()

	suspend fun add(vararg intakes: Intake) = dao.insert(*intakes)

	suspend fun update(vararg intakes: Intake) = dao.update(*intakes)

	suspend fun delete(vararg intakes: Intake) = dao.delete(*intakes)
}