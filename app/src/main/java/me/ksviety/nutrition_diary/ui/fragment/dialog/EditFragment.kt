package me.ksviety.nutrition_diary.ui.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.content_edit.*
import me.ksviety.nutrition_diary.R
import me.ksviety.nutrition_diary.data.model.Intake
import me.ksviety.nutrition_diary.data.model.IntakeType
import me.ksviety.nutrition_diary.ui.viewmodel.EditViewModel

private const val ARG_INTAKE = "edit_fragment.intake"
private const val ERROR_VIEW_NOT_FOUND = "View not found!"

class EditFragment : BottomSheetDialogFragment() {
	private lateinit var viewModel: EditViewModel
	private lateinit var purpose: Purpose

	private lateinit var nameTextFieldLayout: TextInputLayout

	private var onPositiveClickHandler: ((Intake) -> Unit)? = null
	private var onNegativeClickHandler: (() -> Unit)? = null

	override fun onCreateDialog(savedInstanceState: Bundle?) =
			super.onCreateDialog(savedInstanceState).apply {

				setOnShowListener {

					findViewById<View>(com.google.android.material.R.id.design_bottom_sheet).also { dialog ->
						dialog.setBackgroundResource(R.drawable.background_bottom_sheet)
					}
				}
			}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Get the intake or create new from the placeholder
		(arguments?.getParcelable(ARG_INTAKE) ?: Intake.placeholder).also { intake ->

			// Initialize the view model
			EditViewModel.Factory(intake).also { factory ->
				viewModel = ViewModelProvider(this, factory)[EditViewModel::class.java]
			}

			purpose = if (intake.id == 0) Purpose.Adding else Purpose.Editing
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

			// Setup intake redundancy checkbox
			content_edit_checkbox_redundancy?.run {
				// Synchronize with the intake
				isChecked = intake.isRedundant

				setOnCheckedChangeListener { _, isChecked ->
					viewModel.modifyIntake { it.setRedundancy(isChecked) }
				}
			} ?: throw IllegalStateException(ERROR_VIEW_NOT_FOUND)
		} ?: throw IllegalStateException()

		content_edit_save_button?.run {
			text = when (purpose) {
				Purpose.Editing -> resources.getString(R.string.edit_intake_title_edit)
				Purpose.Adding -> resources.getString(R.string.edit_intake_title_add)
			}

			setOnClickListener {

				viewModel.intake.value?.let { intake ->
					if (intake.name.isBlank()) {
						nameTextFieldLayout.error = resources.getString(R.string.cannot_be_empty_error)
						return@let
					}

					// Dismiss the dialog and fire the event
					dismissAllowingStateLoss()
					onPositiveClickHandler?.invoke(intake)
				} ?: throw IllegalStateException()
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