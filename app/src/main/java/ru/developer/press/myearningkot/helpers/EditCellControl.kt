package ru.developer.press.myearningkot.helpers

import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.activity.CardActivity
import ru.developer.press.myearningkot.activity.toast
import ru.developer.press.myearningkot.dialogs.DialogEditCell
import ru.developer.press.myearningkot.dialogs.DialogEditImageCell
import ru.developer.press.myearningkot.dialogs.editCellTag
import ru.developer.press.myearningkot.model.Column
import ru.developer.press.myearningkot.model.ColumnType
import ru.developer.press.myearningkot.model.InputTypeNumberColumn
import ru.developer.press.myearningkot.model.NumberColumn

class EditCellControl(
    private val activity: CardActivity,
    private val column: Column,
    sourceValue: String,
    private val changed: (sourceValue: String) -> Unit
) {
    private var value = ""

    fun editCell() {
        when (column.getType()) {
            ColumnType.SWITCH -> {
                val toBoolean = !value.toBoolean()
                value = toBoolean.toString()
                changed(value)
            }
            ColumnType.COLOR -> {
                ColorPickerDialog
                    .newBuilder()
                    .setColor(value.toInt())
                    .setShowAlphaSlider(false)
                    .create().apply {
                        setColorPickerDialogListener(
                            object : ColorPickerDialogListener {
                                override fun onDialogDismissed(dialogId: Int) {

                                }

                                override fun onColorSelected(dialogId: Int, color: Int) {
                                    value = color.toString()
                                    changed(value)
                                }

                            })
                    }.show(activity.supportFragmentManager, "colorPicker")
            }

            ColumnType.IMAGE -> {
                DialogEditImageCell(column, value) {
                    changed(it)
                }.show(
                    activity.supportFragmentManager,
                    editCellTag
                )
            }
            else -> {
                if (column is NumberColumn && column.inputType == InputTypeNumberColumn.FORMULA) {
                    activity.toast(activity.getString(R.string.formula_works_for_this_column))
                } else {
                    DialogEditCell(column, value) {
                        changed(it)
                    }.show(
                        activity.supportFragmentManager,
                        editCellTag
                    )
                }
            }
        }
    }

    init {
        value = sourceValue
    }
}


