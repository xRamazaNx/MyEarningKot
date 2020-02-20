package ru.developer.press.myearningkot.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.card_basic_pref_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.textColorResource
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.dpsToPixels
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.otherHelpers.getDateTypeList
import ru.developer.press.myearningkot.otherHelpers.getValutaTypeList
import ru.developer.press.myearningkot.otherHelpers.showItemChangeDialog

class DialogBasicPrefCard(
    val card: Card,
    val basicPrefEvent: () -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(context).apply {
            setCustomTitle(TextView(context).apply {
                text = "Общие настройки"
                textColorResource = R.color.light_gray
                val dp24 = context.dpsToPixels(24)
                val dp8 = context.dpsToPixels(8)
                setPadding(dp24, dp24, dp24, dp8)
                textSize = 22f
            })
            val view = context.layoutInflater.inflate(R.layout.card_basic_pref_layout, null)

            // выбор валюты
            val textViewValutaType = view.textViewValutaType
            val listValutaType = getValutaTypeList()
            textViewValutaType.text =
                "${getString(R.string.change_valuta)} (${listValutaType[card.valuta]})"
            textViewValutaType.setOnClickListener {
                context.showItemChangeDialog(
                    "Выберите валюту",
                    listValutaType,
                    card.valuta,
                    null,
                    fun(selected) {
                        card.valuta = selected
                        textViewValutaType.text =
                            "${getString(R.string.change_valuta)} (${listValutaType[selected]})"
                        updateCard()
                    })
            }

            // выбор типа даты
            val textViewDateType = view.textViewDateType
            val listDateType = getDateTypeList()
            textViewDateType.text =
                "${getString(R.string.date_type)} (${listDateType[card.dateType]})"
            textViewDateType.setOnClickListener {
                context.showItemChangeDialog(
                    "Выберите тип даты",
                    listDateType,
                    card.dateType,
                    null,
                    fun(selected) {
                        card.dateType = selected
                        textViewDateType.text =
                            "${getString(R.string.date_type)} (${listDateType[selected]})"
                        updateCard()
                    })
            }
            view.switchEnableHorizontalScroll.isChecked = card.enableHorizontalScroll
            view.switchEnableSomeStroke.isChecked = card.enableSomeStroke

            view.switchEnableHorizontalScroll.setOnCheckedChangeListener { _, b ->
                card.enableHorizontalScroll = b
                updateCard()
            }
            view.switchEnableSomeStroke.setOnCheckedChangeListener { _, b ->
                card.enableSomeStroke = b
                updateCard()
            }
            setView(view)
            setPositiveButton(" ") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()

            }

        }

        return dialog.create()
    }

    private fun updateCard() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(250)
            basicPrefEvent()
        }
    }


    override fun onResume() {
        super.onResume()
        context?.let {
            dialog?.window?.setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        it,
                        R.color.cent
                    )
                )
            )
        }
    }
}
//    override fun onStart() {
//        val window = dialog?.window
//        window?.apply {
//            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
//        }
//        super.onStart()
//    }

