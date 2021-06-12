package ru.developer.press.myearningkot.dialogs

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.helpers.setAlertButtonColors

fun myDialog(init: AlertDialog.Builder.() -> Unit): MyDialog {
    return MyDialog().apply {
        setAlertConfig(init)
    }
}

class MyDialog : DialogFragment() {
    var positiveButtonColorRes = R.color.colorAccent
    var negativeButtonColorRes = R.color.colorAccent

    private var alertInit: AlertDialog.Builder.() -> Unit = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: AlertDialog.Builder = AlertDialog.Builder(requireContext()).apply {
            alertInit.invoke(this)
        }

        val alertDialog = dialog.create()
        alertDialog.setAlertButtonColors(positiveButtonColorRes, negativeButtonColorRes)
        return alertDialog
    }

    override fun onResume() {
        super.onResume()
        context?.let {
            dialog?.window?.setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        it,
                        R.color.colorDialogBackground
                    )
                )
            )
        }
    }

    fun setAlertConfig(init: AlertDialog.Builder.() -> Unit) {
        alertInit = init
    }
}


