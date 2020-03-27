package ru.developer.press.myearningkot.dialogs

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.postDelayed
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.edit_cell_number.view.*
import kotlinx.android.synthetic.main.edit_cell_number.view.editCellText
import kotlinx.android.synthetic.main.edit_cell_phone.view.*
import kotlinx.android.synthetic.main.edit_cell_text.view.*
import org.jetbrains.anko.*
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.model.Column
import ru.developer.press.myearningkot.model.ColumnType
import ru.developer.press.myearningkot.model.PhoneTypeValue
import ru.developer.press.myearningkot.otherHelpers.PrefLayouts.initClickOperation
import java.lang.StringBuilder
import java.util.*


class DialogEditCell(
    private val column: Column,
    private var value: String,
    private val changed: (sourceValue: String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = context!!

        return when (column.getType()) {
            ColumnType.TEXT -> getTextDialog(context)
            ColumnType.NUMBER -> getNumberDialog(context)
            ColumnType.PHONE -> getPhoneDialog(context)
            ColumnType.DATE -> getDateDialog(context)
            ColumnType.IMAGE -> getImageDialog(context)
            ColumnType.LIST -> getListDialog(context)

            else -> AlertDialog.Builder(context).create()
        }
    }

    private fun getDateDialog(context: Context): AlertDialog {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = value.toLong()
        }
        return DatePickerDialog(
            context,
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                value = calendar.timeInMillis.toString()
                changed(value)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            //            datePicker.backgroundColor = Color.WHITE
            datePicker.post {

                window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            }
        }
    }


    private fun getListDialog(context: Context): AlertDialog {
        return getAlertDialog().apply {
            setView(context.layoutInflater.inflate(R.layout.edit_cell_list, null))
        }.create()
    }

    private fun getImageDialog(context: Context): AlertDialog {
        return getAlertDialog().apply {
            setView(context.layoutInflater.inflate(R.layout.edit_cell_image, null))
        }.create()
    }

    private fun getNumberDialog(context: Context): AlertDialog {
        return getAlertDialog().apply {
            val view = context.layoutInflater.inflate(R.layout.edit_cell_number, null)
            view.titleNumberEdit.text = column.name
            val editCellText = view.editCellText
            initClickOperation(view) {
                val stringBuilder = StringBuilder()
                val toMutableList = value.toMutableList()
                toMutableList.add(editCellText.selectionStart, it[0])
                toMutableList.forEach { char ->
                    stringBuilder.append(char)
                }
                value = stringBuilder.toString()
                editCellText?.setText(value)
                editCellText.setSelection(value.length)
            }
            editCellText.setText(value)
            editCellText.addTextChangedListener {
                value = it.toString()
            }
            editCellText.showKeyboard()
            setView(view)
        }.create()
    }

    private fun getPhoneDialog(context: Context): AlertDialog {
        return getAlertDialog().apply {
            val view = context.layoutInflater.inflate(R.layout.edit_cell_phone, null)
            view.titlePhoneEdit.text = column.name
            val phone = view.editPhone
            val name = view.editName
            val family = view.editFamily
            val org = view.editOrganization

            val phoneTypeValue = Gson().fromJson(value, PhoneTypeValue::class.java)

            phone.setText(phoneTypeValue.phone.toString())
            name.setText(phoneTypeValue.name)
            family.setText(phoneTypeValue.lastName)
            org.setText(phoneTypeValue.organization)

            fun updateValue() {
                value = Gson().toJson(phoneTypeValue)
            }
            phone.addTextChangedListener {
                phoneTypeValue.phone = it.toString()
                updateValue()
            }
            name.addTextChangedListener {
                phoneTypeValue.name = it.toString()
                updateValue()
            }
            family.addTextChangedListener {
                phoneTypeValue.lastName = it.toString()
                updateValue()
            }
            org.addTextChangedListener {
                phoneTypeValue.organization = it.toString()
                updateValue()
            }

            name.showKeyboard()

            setView(view)
        }.create()
    }

    private fun getTextDialog(context: Context): AlertDialog {
        return getAlertDialog().apply {
            val view = context.layoutInflater.inflate(R.layout.edit_cell_text, null)
            view.titleTextEdit.text = column.name
            val editCellText = view.editCellText
            editCellText.setText(value)
            editCellText.addTextChangedListener {
                value = it.toString()
            }

            editCellText.showKeyboard()

            setView(view)

        }.create()
    }

    private fun getAlertDialog(): AlertDialog.Builder {

        return AlertDialog.Builder(context).apply {
            setPositiveButton(R.string.OK) { v: DialogInterface, i: Int ->
                changed(value)
            }
            setNegativeButton(R.string.CANCEL) { v: DialogInterface, i: Int ->

            }
        }
    }


    override fun onResume() {
        super.onResume()

        this.dialog?.apply {

            //            window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
//            window?.setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
//            )
//            window?.setGravity(Gravity.BOTTOM)
//            window?.setGravity(Gravity.CENTER)
            context.let {
                window?.setBackgroundDrawable(
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
}

fun EditText.showKeyboard() {

    postDelayed(150) {
        requestFocus()
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.toggleSoftInput(
            InputMethodManager.SHOW_IMPLICIT,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )
        setSelection(text.length)
    }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}