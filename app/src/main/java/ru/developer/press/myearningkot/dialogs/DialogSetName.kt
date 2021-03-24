package ru.developer.press.myearningkot.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.set_name_layout.view.*
import org.jetbrains.anko.layoutInflater
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.helpers.setAlertButtonColors

class DialogSetName : DialogFragment() {

    private lateinit var editTextCardName: EditText
    private var positiveClick: ((String) -> Boolean)? = null
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
    fun setPositiveListener(click:((String) -> Boolean)? = null): DialogSetName {
        positiveClick = click
        return this
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: AlertDialog.Builder = AlertDialog.Builder(requireContext()).apply {
            val view = context.layoutInflater.inflate(R.layout.set_name_layout, null)

            view.title.setText(title)
            editTextCardName = view.editTextSetName
            editTextCardName.setText(name)
            editTextCardName.showKeyboard()
            //
            setView(view)
            setPositiveButton(R.string.OK) { dialog: DialogInterface, _: Int ->
            }
            setNegativeButton(R.string.CANCEL) { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
            }

        }

        val alertDialog: AlertDialog = dialog.create()
        alertDialog.setAlertButtonColors(R.color.colorAccent, R.color.colorAccent)
        return alertDialog
    }

    override fun onResume() {
        super.onResume()
        val alertDialog = dialog as AlertDialog
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (positiveClick?.invoke(editTextCardName.text.toString()) == true)
                dismiss()
        }
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

