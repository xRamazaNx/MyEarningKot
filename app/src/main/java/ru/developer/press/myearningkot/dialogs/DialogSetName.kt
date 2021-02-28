package ru.developer.press.myearningkot.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.set_name_layout.view.*
import org.jetbrains.anko.layoutInflater
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.helpers.setAlertButtonColors

class DialogSetName : DialogFragment() {

    private var positiveClick: ((String) -> Unit)? = null
    private var name = ""
    private var title = ""
    fun setName(name: String): DialogSetName {
        this.name = name
        return this
    }
    fun setTitle(title: String): DialogSetName {
        this.title = title
        return this
    }
    fun setPositiveListener(click:((String) -> Unit)? = null): DialogSetName {
        positiveClick = click
        return this
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: AlertDialog.Builder = AlertDialog.Builder(context).apply {
            val view = context.layoutInflater.inflate(R.layout.set_name_layout, null)

            view.title.setText(title)
            val editTextCardName = view.editTextSetName
            editTextCardName.setText(name)
            editTextCardName.showKeyboard()
            //
            setView(view)
            setPositiveButton(R.string.OK) { _: DialogInterface, _: Int ->
                positiveClick?.invoke(editTextCardName.text.toString())
            }
            setNegativeButton(R.string.CANCEL) { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
            }

        }

        val alertDialog = dialog.create()
        alertDialog.setAlertButtonColors(R.color.colorAccent, R.color.colorAccent)
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
}

//    override fun onStart() {
//        val window = dialog?.window
//        window?.apply {
//            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
//        }
//        super.onStart()
//    }

