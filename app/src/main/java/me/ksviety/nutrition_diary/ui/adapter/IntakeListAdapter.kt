package me.ksviety.nutrition_diary.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import me.ksviety.nutrition_diary.R
import me.ksviety.nutrition_diary.data.model.Intake
import me.ksviety.nutrition_diary.ui.fragment.dialog.EditFragment
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class IntakeListAdapter(context: Context) : RecyclerView.Adapter<IntakeListAdapter.ViewHolder>() {
	private val inflater = LayoutInflater.from(context)
	private var intakes = emptyList<Intake>()

	private var onItemClickedHandler: ((Intake) -> Unit)? = null
	private var onItemDeletedHandler: ((Intake) -> Boolean)? = null

	inner class ViewHolder(itemView: MaterialCardView) : RecyclerView.ViewHolder(itemView) {
		private var onSelectedHandler: (() -> Unit)? = null
		private var onDeselectedHandler: (() -> Unit)? = null

		var isChecked: Boolean
			get() = (itemView as MaterialCardView).isChecked
			set(value) {
				(itemView as MaterialCardView).isChecked = value
			}

		fun setOnSelectedHandler(handler: (() -> Unit)?) {
			onSelectedHandler = handler
		}

		fun setOnDeselectedHandler(handler: (() -> Unit)?) {
			onDeselectedHandler = handler
		}

		fun initializeWith(intake: Intake) {
			itemView.run {
				findViewById<TextView>(R.id.list_item_intake_name).text =
						intake.name

				findViewById<TextView>(R.id.list_item_intake_type).text =
						resources.getStringArray(
								if (intake.isRedundant)
									R.array.redundant_intake_types
								else
									R.array.intake_types
						)[intake.type.ordinal]

				findViewById<TextView>(R.id.list_item_intake_calories).text =
						if (intake.calories > 0f)
							String.format(resources.getString(R.string.list_item_intake_calories), intake.calories)
						else
							String()

				findViewById<TextView>(R.id.list_item_intake_time).text =
						intake.datetime.toLocalTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

				itemView.setOnClickListener {
					onItemClickedHandler?.invoke(intake)
				}

				itemView.setOnLongClickListener {
					onItemDeletedHandler?.invoke(intake) ?: false
				}
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
			ViewHolder(
					inflater.inflate(R.layout.list_item_intake, parent, false) as MaterialCardView
			)

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.initializeWith(intakes[position])
	}

	override fun getItemCount() = intakes.size

	fun setContent(intakes: List<Intake>) {
		this.intakes = intakes
		notifyDataSetChanged()
	}

	fun setOnItemClickedHandler(handler: (Intake) -> Unit) {
		onItemClickedHandler = handler
	}

	fun setOnItemDeletedHandler(handler: (Intake) -> Boolean) {
		onItemDeletedHandler = handler
	}

	fun removeOnItemClickedHandler() {
		onItemClickedHandler = null
	}

	fun removeOnItemDeletedHandler() {
		onItemDeletedHandler = null
	}
}