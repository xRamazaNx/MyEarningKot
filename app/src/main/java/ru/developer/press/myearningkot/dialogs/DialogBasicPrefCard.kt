package ru.developer.press.myearningkot.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.card_basic_pref_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.layoutInflater
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.helpers.getValutaTypeList
import ru.developer.press.myearningkot.helpers.setAlertButtonColors
import ru.developer.press.myearningkot.helpers.showItemChangeDialog

class DialogBasicPrefCard(
    val card: Card,
    val basicPrefEvent: () -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(context).apply {
//            setCustomTitle(TextView(context).apply {
//                text = context.getString(R.string.general_setting)
//                textColorResource = R.color.light_gray
//                val dp24 = context.dpsToPixels(24)
//                val dp8 = context.dpsToPixels(8)
//                setPadding(dp24, dp24, dp24, dp8)
//                textSize = 20f
//            })
            val view = context.layoutInflater.inflate(R.layout.card_basic_pref_layout, null)

            // выбор валюты
            val textViewValutaType = view.textViewValutaType
            val listValutaType = getValutaTypeList()
            textViewValutaType.text =
                "${getString(R.string.valuta)} (${listValutaType[card.valuta]})"
            textViewValutaType.setOnClickListener {
                context.showItemChangeDialog(
                    "Выберите валюту",
                    listValutaType,
                    card.valuta,
                    null,
                    fun(selected) {
                        card.valuta = selected
                        textViewValutaType.text =
                            "${getString(R.string.valuta)} (${listValutaType[selected]})"
                        updateCard()
                    })
            }


            val switchEnableHorizontalScroll = view.switchEnableHorizontalScrollItems
            val switchEnableSomeStroke = view.switchEnableSomeStroke

            switchEnableHorizontalScroll.isChecked = card.enableHorizontalScroll
            switchEnableSomeStroke.isChecked = card.enableSomeStroke

            switchEnableHorizontalScroll.setOnCheckedChangeListener { _, b ->
                card.enableHorizontalScroll = b
                updateCard()
            }
            switchEnableSomeStroke.setOnCheckedChangeListener { _, b ->
                card.enableSomeStroke = b
                updateCard()
            }
            val heightUp = view.heightSizeUp
            val heightDown = view.heightSizeDown
            val heightSize = view.heightSize

            fun updateHeightInfo() {
                heightSize.text = card.heightCells.toString()
            }
            updateHeightInfo()
            heightUp.setOnClickListener {
                card.heightCells += 1
                updateHeightInfo()
                updateCard()
            }
            heightDown.setOnClickListener {
                card.heightCells -= 1
                updateHeightInfo()
                updateCard()
            }

            setView(view)
            setPositiveButton(" ") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()

            }


            val switchEnableShowDatePeriod = view.switchEnableDatePeriod
            val switchEnableHorizontalScrollTotals = view.switchEnableHorizontalScrollTotals
            val switchShowTotalInfo = view.switchShowTotalInfo

            switchEnableShowDatePeriod.isChecked = card.isShowDatePeriod
            switchEnableHorizontalScrollTotals.isChecked = card.enableHorizontalScrollTotal
            switchShowTotalInfo.isChecked = card.isShowTotalInfo

            switchEnableShowDatePeriod.setOnCheckedChangeListener { _, isChecked ->
                card.isShowDatePeriod = isChecked
                updateCard()
            }
            switchEnableHorizontalScrollTotals.setOnCheckedChangeListener { _, b ->
                card.enableHorizontalScrollTotal = b
                updateCard()
            }
            switchShowTotalInfo.setOnCheckedChangeListener { _, isChecked ->
                card.isShowTotalInfo = isChecked
                updateCard()
            }
            setView(view)
            setPositiveButton(" ") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()

            }
        }

        val alertDialog = dialog.create()
        alertDialog.setAlertButtonColors(R.color.colorAccent, R.color.colorAccent)
        return alertDialog
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
                        R.color.colorSurface
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

