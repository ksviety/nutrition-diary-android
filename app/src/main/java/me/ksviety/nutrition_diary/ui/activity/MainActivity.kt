package me.ksviety.nutrition_diary.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import me.ksviety.nutrition_diary.R
import me.ksviety.nutrition_diary.ui.adapter.IntakeListAdapter
import me.ksviety.nutrition_diary.ui.fragment.dialog.EditFragment
import me.ksviety.nutrition_diary.ui.viewmodel.DayViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val ZONE_ID = ZoneId.systemDefault()

class MainActivity : AppCompatActivity() {
	private lateinit var viewModel: DayViewModel

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setSupportActionBar(findViewById(R.id.activity_main_toolbar))

		ViewModelProvider.AndroidViewModelFactory(application).also { factory ->

			viewModel = ViewModelProvider(this, factory)[DayViewModel::class.java].also { model ->

				model.date.observe(this) {
					supportActionBar?.let { actionBar ->
						actionBar.subtitle =
								it.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
					}
				}

				model.intakes.observe(this) {
					content_main_no_intake_notes_message.visibility =
							if (it?.isNotEmpty() == true) View.GONE else View.VISIBLE
				}
			}
		}

		IntakeListAdapter(this).also { adapter ->

			viewModel.intakes.observe(this) {
				adapter.setContent(it)
			}

			adapter.setOnItemClickedHandler {
				EditFragment.build(it).apply {
					setOnPositiveClickHandler { result ->
						viewModel.update(result)
					}

					show(supportFragmentManager, this.toString())
				}
			}

			adapter.setOnItemDeletedHandler { intake ->
				MaterialAlertDialogBuilder(this).apply {
					setTitle(R.string.dialog_remove_intake_note_title)
					setMessage(R.string.dialog_remove_intake_note_message)
					setPositiveButton(R.string.action_remove) { _, _ ->
						viewModel.delete(intake)
						Snackbar.make(fab, R.string.snackbar_removed_intake_note, Snackbar.LENGTH_LONG).show()
					}
					setNegativeButton(R.string.action_cancel) { _, _, -> }
					show()
				}
				true
			}

			content_main_intakes_recycler.run {
				this.adapter = adapter
				layoutManager = LinearLayoutManager(this@MainActivity)
			}
		}

		fab.setOnClickListener { view ->
			EditFragment.build(null).apply {
				setOnPositiveClickHandler {
					viewModel.add(it)
					Snackbar.make(view, R.string.snackbar_added_intake_note, Snackbar.LENGTH_LONG).show()
				}

				show(supportFragmentManager, this.toString())
			}
		}
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.menu_day, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem) =
			when (item.itemId) {
				R.id.option_select_date -> onSelectDateClicked()

				else -> false
			}

	override fun onDestroy() {
		super.onDestroy()

		(content_main_intakes_recycler.adapter as IntakeListAdapter).apply {
			removeOnItemClickedHandler()
			removeOnItemDeletedHandler()
		}
	}

	private fun onSelectDateClicked(): Boolean {
		MaterialDatePicker.Builder.datePicker().apply {
			val localDate = viewModel.date.value ?: throw IllegalStateException()
			val startOfDay = localDate.atStartOfDay().toInstant(ZoneOffset.MIN)
			setSelection(startOfDay.toEpochMilli())

			build().apply {

				addOnPositiveButtonClickListener {
					val instant = Instant.ofEpochMilli(it)
					val selection = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)

					viewModel.setDate(selection.toLocalDate(), ZONE_ID)
				}

				show(supportFragmentManager, tag)
			}
		}

		return true
	}
}