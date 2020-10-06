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
import me.ksviety.nutrition_diary.data.model.isNotWithinDate
import java.time.LocalDate
import java.time.ZoneId
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

	private val currentDate: LocalDate
		get() = LocalDate.now(ZoneId.systemDefault())

	val intakes = _intakes as LiveData<List<Intake>>
	val date = _date as LiveData<LocalDate>

	init {
		val database = DiaryDatabase(application)
		val dao = database.getDao()

		repository = DiaryRepository(dao)

		_date.value = currentDate

		repository.intakes.observeForever(intakesObserver)
	}

	override fun onCleared() {
		repository.intakes.removeObserver(intakesObserver)
		super.onCleared()
	}

	fun setDate(date: LocalDate) {
		_date.postValue(date)
	}

	fun delete(vararg intakes: Intake) = dateAware(*intakes) {
		viewModelScope.launch {
			repository.delete(*intakes)
		}
	}

	fun add(vararg intakes: Intake) = dateAware(*intakes) {
		viewModelScope.launch {
			repository.add(*intakes)
		}
	}

	fun update(vararg intakes: Intake) = dateAware(*intakes) {
		viewModelScope.launch {
			repository.update(*intakes)
		}
	}

	/**
	 * Executes date aware intakes operation. If at least one of the intakes is out of the date,
	 * the date will be changed to the actual (not the date of the intake)
	 *
	 * @throws IllegalStateException if [_date] is unset
	 */
	private inline fun <T> dateAware(vararg intakes: Intake, block: () -> T): T {
		val date = _date.value ?: throw IllegalStateException("Date is not set")
		val isOutdated = intakes.any { it.isNotWithinDate(date) }

		return block().also {
			if (isOutdated)
				_date.postValue(currentDate)
		}
	}
}