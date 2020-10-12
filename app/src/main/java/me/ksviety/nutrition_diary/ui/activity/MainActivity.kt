package me.ksviety.nutrition_diary.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import me.ksviety.nutrition_diary.R
import me.ksviety.nutrition_diary.ui.viewmodel.DayViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val ZONE_ID = ZoneId.systemDefault()

class MainActivity : AppCompatActivity() {
	private lateinit var viewModel: DayViewModel

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setSupportActionBar(findViewById(R.id.toolbar))

		ViewModelProvider.AndroidViewModelFactory(application).also { factory ->

			viewModel = ViewModelProvider(this, factory)[DayViewModel::class.java].also { model ->

				model.date.observe(this) {
					supportActionBar?.let { actionBar ->
						actionBar.title = resources.getString(R.string.app_name)
						actionBar.subtitle =
								it.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
					}
				}
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