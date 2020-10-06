package me.ksviety.nutrition_diary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.ksviety.nutrition_diary.data.DiaryRepository
import me.ksviety.nutrition_diary.data.database.DiaryDatabase
import me.ksviety.nutrition_diary.data.model.Intake
import java.time.LocalDate
import java.time.ZoneOffset

class DayViewModel(application: Application) : AndroidViewModel(application) {
	private val repository: DiaryRepository

	private val _intakes = MutableLiveData<List<Intake>>()
	private val _date = MutableLiveData<LocalDate>()

	private val intakesObserver = Observer<List<Intake>> { intakes ->
		val date = _date.value!!
		val zone = ZoneOffset.systemDefault()
		val period = LongRange(
				date.atStartOfDay(zone).toEpochSecond(),
				date.plusDays(1).atStartOfDay(zone).toEpochSecond() - 1L
		)

		_intakes.postValue(intakes.filter { it.timestamp in period })
	}

	val intakes = _intakes as LiveData<List<Intake>>
	val date = _date as LiveData<LocalDate>

	init {
		val database = DiaryDatabase(application)
		val dao = database.getDao()

		repository = DiaryRepository(dao)

		_date.value = LocalDate.now()

		repository.intakes.observeForever(intakesObserver)
	}

	override fun onCleared() {
		repository.intakes.removeObserver(intakesObserver)
		super.onCleared()
	}

	fun setDate(date: LocalDate) {
		_date.postValue(date)
	}

	fun delete(vararg intakes: Intake) =
			viewModelScope.launch {
				repository.delete(*intakes)
			}

	fun add(vararg intakes: Intake) =
			viewModelScope.launch {
				repository.add(*intakes)
			}

	fun update(vararg intakes: Intake) =
			viewModelScope.launch {
				repository.update(*intakes)
			}

}