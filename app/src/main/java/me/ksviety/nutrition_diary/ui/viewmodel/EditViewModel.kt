package me.ksviety.nutrition_diary.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.ksviety.nutrition_diary.data.model.Intake
import java.lang.IllegalStateException

class EditViewModel(intake: Intake) : ViewModel() {
	private val _intake = MutableLiveData<Intake>()

	val intake = _intake as LiveData<Intake>

	init {
		_intake.value = intake
	}

	fun setIntake(intake: Intake) = _intake.postValue(intake)

	fun modifyIntake(modifier: (Intake) -> Intake) {
		_intake.value?.let {
			setIntake(modifier(it))
		} ?: throw IllegalStateException()
	}

	class Factory(private val intake: Intake) : ViewModelProvider.Factory {

		override fun <T : ViewModel?> create(modelClass: Class<T>): T {
			return EditViewModel(intake) as T
		}
	}
}