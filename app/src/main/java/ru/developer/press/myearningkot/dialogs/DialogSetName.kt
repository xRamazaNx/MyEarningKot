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

class DialogSetName(val setName: (String) -> Unit) : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(context).apply {
            val view = context.layoutInflater.inflate(R.layout.set_name_layout, null)

            val editTextCardName = view.editTextCardName
            //
            setView(view)
            setPositiveButton(R.string.OK) { _: DialogInterface, _: Int ->
                setName(editTextCardName.text.toString())
            }
            setNegativeButton(R.string.CANCEL) { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
            }

        }

        return dialog.create()
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

