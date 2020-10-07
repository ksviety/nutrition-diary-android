package me.ksviety.nutrition_diary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.ksviety.nutrition_diary.data.DiaryRepository
import me.ksviety.nutrition_diary.data.database.DiaryDatabase
import me.ksviety.nutrition_diary.data.model.Intake
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class DayViewModel(application: Application) : AndroidViewModel(application) {
	private val repository: DiaryRepository

	private val _date = MutableLiveData<LocalDate>()

	val intakes : LiveData<List<Intake>>

	init {
		_date.value = ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate()

		DiaryDatabase(application).also {
			repository = DiaryRepository(it.getDao())
		}

		intakes = Transformations.map(repository.intakes) { intakes ->
			val date  = _date.value ?: throw IllegalStateException()
			intakes.filter { it.isWithin(date) }
		}
	}

	fun setDate(date: LocalDate) = _date.postValue(date)

	fun delete(vararg intakes: Intake) = viewModelScope.launch {
		repository.delete(*intakes)
	}

	fun add(vararg intakes: Intake) = viewModelScope.launch {
		repository.add(*intakes)
	}

	fun update(vararg intakes: Intake) = viewModelScope.launch {
		repository.update(*intakes)
	}
}