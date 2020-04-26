package ru.developer.press.myearningkot.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.card_basic_pref_layout.view.switchEnableHorizontalScroll
import kotlinx.android.synthetic.main.plate_basic_pref_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.textColorResource
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.dpsToPixels
import ru.developer.press.myearningkot.model.Card

class DialogBasicPrefPlate(
    val card: Card,
    val basicPrefEvent: () -> Unit
) : DialogFragment() {

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(context).apply {
            setCustomTitle(TextView(context).apply {
                text = context.getString(R.string.plate_setting)
                textColorResource = R.color.light_gray
                val dp24 = context.dpsToPixels(24)
                val dp8 = context.dpsToPixels(8)
                setPadding(dp24, dp24, dp24, dp8)
                textSize = 20f
            })
            val view = context.layoutInflater.inflate(R.layout.plate_basic_pref_layout, null)

            val switchEnableShowDatePeriod = view.switchEnableDatePeriod
            val switchEnableHorizontalScroll = view.switchEnableHorizontalScroll
            val switchShowTotalInfo = view.switchShowTotalInfo

            switchEnableShowDatePeriod.isChecked = card.isShowDatePeriod
            switchEnableHorizontalScroll.isChecked = card.enableHorizontalScrollTotal
            switchShowTotalInfo.isChecked = card.isShowTotalInfo

            switchEnableShowDatePeriod.setOnCheckedChangeListener { _, isChecked ->
                card.isShowDatePeriod = isChecked
                updateCard()
            }
            switchEnableHorizontalScroll.setOnCheckedChangeListener { _, b ->
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

