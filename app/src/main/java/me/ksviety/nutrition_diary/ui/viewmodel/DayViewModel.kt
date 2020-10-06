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

class DayViewModel(application: Application) : AndroidViewModel(application) {
	private val repository: DiaryRepository

	private val _date = MutableLiveData<LocalDate>()

	private val currentDate: LocalDate
		get() = LocalDate.now(ZoneId.systemDefault())

	val intakes : LiveData<List<Intake>>

	init {
		_date.value = currentDate

		DiaryDatabase(application).also {
			repository = DiaryRepository(it.getDao())
		}

		intakes = Transformations.map(repository.intakes) { intakes ->
			val date  = _date.value ?: throw IllegalStateException()
			intakes.filter { it.isWithin(date) }
		}
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
		val isOutdated = intakes.any { it.isNotWithin(date) }

		return block().also {
			if (isOutdated)
				_date.postValue(currentDate)
		}
	}
}