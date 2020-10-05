package me.ksviety.nutrition_diary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import me.ksviety.nutrition_diary.data.DiaryRepository
import me.ksviety.nutrition_diary.data.database.DiaryDatabase
import me.ksviety.nutrition_diary.data.model.Intake

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
	private val repository: DiaryRepository

	/**
	 * All intakes for all time
	 */
	val intakes: LiveData<List<Intake>>

	init {
		val database = DiaryDatabase(application)
		val dao = database.getDao()

		repository = DiaryRepository(dao).also {
			intakes = it.intakes
		}
	}
}