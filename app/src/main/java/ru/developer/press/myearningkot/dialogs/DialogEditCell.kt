package ru.developer.press.myearningkot.dialogs

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.edit_cell_number.view.*
import kotlinx.android.synthetic.main.edit_cell_number.view.editCellText
import kotlinx.android.synthetic.main.edit_cell_phone.view.*
import kotlinx.android.synthetic.main.edit_cell_text.view.*
import org.jetbrains.anko.layoutInflater
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.helpers.getColorFromRes
import ru.developer.press.myearningkot.helpers.prefLayouts.initClickOperation
import ru.developer.press.myearningkot.helpers.setAlertButtonColors
import ru.developer.press.myearningkot.model.Column
import ru.developer.press.myearningkot.model.ColumnType
import ru.developer.press.myearningkot.model.PhoneTypeValue
import java.util.*


val PICK_IMAGE_MULTIPLE = 1
const val editCellTag = "dialogEditCell"

class DialogEditCell(
    private val column: Column,
    private var value: String,
    private val changed: (sourceValue: String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val alertDialog: AlertDialog = when (column.getType()) {
            ColumnType.TEXT -> getTextDialog()
            ColumnType.NUMBER -> getNumberDialog()
            ColumnType.PHONE -> getPhoneDialog()
            ColumnType.DATE -> getDateDialog()
            ColumnType.LIST -> getListDialog()

            else -> AlertDialog.Builder(context).create()
        }
        alertDialog.setAlertButtonColors(R.color.colorAccent, R.color.colorAccent)
        return alertDialog
    }

    private fun getDateDialog(): AlertDialog {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = try {
                value.toLong()
            } catch (exc: Exception) {
                Date().time
            }
        }
        return DatePickerDialog(
            context!!, { _, year, month, dayOfMonth ->
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

                window?.setBackgroundDrawable(ColorDrawable(context.getColorFromRes(R.color.colorDialogBackground)))
            }
        }
    }


    private fun getListDialog(): AlertDialog {
        return getAlertDialog().apply {
            setView(context.layoutInflater.inflate(R.layout.edit_cell_list, null))
        }.create()
    }

    private fun getNumberDialog(): AlertDialog {
        return getAlertDialog().apply {
            val view = context.layoutInflater.inflate(R.layout.edit_cell_number, null)
            view.titleNumberEdit.text = column.name
            val editCellText = view.editCellText
            val callBackOperatorClick: (String) -> Unit = {
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
            initClickOperation(view, callBackOperatorClick)
            editCellText.setText(value)
            editCellText.addTextChangedListener {
                value = it.toString()
            }
            editCellText.showKeyboard()
            setView(view)
        }.create()
    }

    private fun getPhoneDialog(): AlertDialog {
        return getAlertDialog().apply {
            val view = context.layoutInflater.inflate(R.layout.edit_cell_phone, null)
            view.titlePhoneEdit.text = column.name
            val phone = view.editPhone
            val name = view.editName
            val family = view.editFamily
            val org = view.editOrganization

            val phoneTypeValue = Gson().fromJson(value, PhoneTypeValue::class.java)

            phone.setText(phoneTypeValue.phone)
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

            setView(view)
        }.create()
    }

    private fun getTextDialog(): AlertDialog {
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
            setPositiveButton(R.string.OK) { _: DialogInterface, _: Int ->
                changed(value)
            }
            setNegativeButton(R.string.CANCEL) { _: DialogInterface, _: Int ->

            }
        }
    }

    override fun onResume() {
        super.onResume()

        this.dialog?.apply {
            context.let {
                window?.setBackgroundDrawable(
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
}

fun EditText.showKeyboard() {

    post {
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