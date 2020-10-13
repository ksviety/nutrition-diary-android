package me.ksviety.nutrition_diary.ui.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.content_edit.*
import kotlinx.android.synthetic.main.dialog_fragment_edit.*
import me.ksviety.nutrition_diary.R
import me.ksviety.nutrition_diary.data.model.Intake
import me.ksviety.nutrition_diary.data.model.IntakeType
import me.ksviety.nutrition_diary.ui.viewmodel.EditViewModel

private const val ARG_INTAKE = "edit_fragment.intake"
private const val ERROR_VIEW_NOT_FOUND = "View not found!"

class EditFragment : DialogFragment() {
	private lateinit var viewModel: EditViewModel
	private lateinit var purpose: Purpose

	private lateinit var nameTextFieldLayout: TextInputLayout

	private var onPositiveClickHandler: ((Intake) -> Unit)? = null
	private var onNegativeClickHandler: (() -> Unit)? = null

	override fun onStart() {
		super.onStart()

		dialog?.window?.let { window ->
			val width = ViewGroup.LayoutParams.MATCH_PARENT
			val height = ViewGroup.LayoutParams.MATCH_PARENT
			window.setLayout(width, height)
			window.setWindowAnimations(R.style.AppTheme_Slide)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)

		// Get the intake or create new from the placeholder
		(arguments?.getParcelable(ARG_INTAKE) ?: Intake.placeholder).also { intake ->

			// Initialize the view model
			EditViewModel.Factory(intake).also { factory ->
				viewModel = ViewModelProvider(this, factory)[EditViewModel::class.java]
			}

			purpose = if (intake.id == -1) Purpose.Adding else Purpose.Editing
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewModel.intake.value?.let { intake ->

			// Setup name text field
			content_edit_text_field_name?.run {
				nameTextFieldLayout = this

				editText?.let {
					it as? TextInputEditText ?: throw IllegalStateException()

					// Synchronize data with the intake
					it.setText(intake.name)

					it.doAfterTextChanged { text ->
						if (text.isNullOrBlank())
							return@doAfterTextChanged

						viewModel.modifyIntake { original -> original.setName(text.toString()) }
					}
				} ?: throw IllegalStateException(ERROR_VIEW_NOT_FOUND)
			} ?: throw IllegalStateException(ERROR_VIEW_NOT_FOUND)

			// Setup calories text field
			content_edit_text_field_calories?.run {
				editText?.let {
					it as? TextInputEditText ?: throw IllegalStateException()

					// Synchronize data with the intake
					it.setText(intake.calories.toString())

					it.doAfterTextChanged { text ->
						viewModel.modifyIntake { original -> original.setCalories(text?.toString()?.toFloatOrNull()
								?: 0f) }
					}
				} ?: throw IllegalStateException(ERROR_VIEW_NOT_FOUND)
			} ?: throw IllegalStateException(ERROR_VIEW_NOT_FOUND)

			// Setup intake type dropdown
			content_edit_dropdown_type?.run {
				editText?.let {
					it as? AutoCompleteTextView ?: throw IllegalStateException()

					resources.getStringArray(R.array.intake_types).also { types ->
						ArrayAdapter(requireContext(), R.layout.intake_type_list_item, types).also { adapter ->
							it.setAdapter(adapter)

							// Synchronize data with the intake
							it.setText(adapter.getItem(intake.type.ordinal), false)

							it.setOnItemClickListener { _, _, position, _ ->
								val type = IntakeType.values()[position]
								viewModel.modifyIntake { original -> original.setType(type) }
							}
						}
					}
				} ?: throw IllegalStateException(ERROR_VIEW_NOT_FOUND)
			} ?: throw IllegalStateException(ERROR_VIEW_NOT_FOUND)

			// Setup intake necessity checkbox
			content_edit_checkbox_necessity?.run {
				// Synchronize with the intake
				isChecked = intake.isNecessary

				setOnCheckedChangeListener { _, isChecked ->
					viewModel.modifyIntake { it.setNecessity(isChecked) }
				}
			} ?: throw IllegalStateException(ERROR_VIEW_NOT_FOUND)
		} ?: throw IllegalStateException()

		dialog_edit_toolbar?.run {
			title = when (purpose) {
				Purpose.Editing -> resources.getString(R.string.edit_intake_title_edit)
				Purpose.Adding -> resources.getString(R.string.edit_intake_title_add)
			}

			// Close button pressed
			setNavigationOnClickListener {
				dismissAllowingStateLoss()
			}

			setOnMenuItemClickListener {
				when (it.itemId) {
					R.id.menu_item_save -> onSaveButtonPressed()

					else -> false
				}
			}
		} ?: throw IllegalStateException(ERROR_VIEW_NOT_FOUND)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
			inflater.inflate(R.layout.dialog_fragment_edit, container, false)!!

	fun setOnPositiveClickHandler(handler: (Intake) -> Unit) {
		onPositiveClickHandler = handler
	}

	fun setOnNegativeClickHandler(handler: () -> Unit) {
		onNegativeClickHandler = handler
	}

	private fun onSaveButtonPressed(): Boolean {

		viewModel.intake.value?.let { intake ->
			if (intake.name.isBlank()) {
				nameTextFieldLayout.error = resources.getString(R.string.cannot_be_empty_error)
				return false
			}

			// Dismiss the dialog and fire the event
			dismissAllowingStateLoss()
			onPositiveClickHandler?.invoke(intake)

			return true
		} ?: throw IllegalStateException()
	}

	private enum class Purpose {
		Editing,
		Adding
	}

	companion object {

		@JvmStatic
		fun build(intake: Intake? = null) =
				EditFragment().apply {
					arguments = bundleOf(ARG_INTAKE to intake)
				}
	}
}