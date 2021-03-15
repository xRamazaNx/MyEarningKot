package ru.developer.press.myearningkot.model

import android.graphics.Color
import android.net.Uri
import android.view.View
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import ru.developer.press.myearningkot.CellTypeControl
import ru.developer.press.myearningkot.helpers.Calc
import ru.developer.press.myearningkot.helpers.getDate
import ru.developer.press.myearningkot.helpers.getDecimalFormatNumber

data class Cell(
    @SerializedName("v")
    var sourceValue: String = ""
) : Backgrounder {

    @Transient
    override var currentBackground: Int = -1
    @Transient
    override lateinit var elementView: View

    @Transient
    var type = ColumnType.TEXT

    @Transient
    var isSelect: Boolean = false

    @Transient
    var isPrefColumnSelect = false

    @Transient
    lateinit var cellTypeControl: CellTypeControl

    @Transient
    var displayValue: String = ""

    fun updateTypeValue(typePref: Prefs) {
        when (typePref) {
            is NumberTypePref -> {
                displayValue =
                    if (sourceValue == "")
                        ""
                    else
                        try {
                            val value = Calc.evaluate(sourceValue)!!
                            getDecimalFormatNumber(value, typePref)
                        } catch (exception: Exception) {
                            "Error numbers"
                        }
            }
            is DateTypePref -> {
                displayValue =
                    if (sourceValue == "")
                        ""
                    else {
                        val timeML: Long = sourceValue.toLong()
                        getDate(typePref.type, timeML, typePref.enableTime)
                    }
            }
            is PhoneTypePref -> {
                val typeValue =
                    Gson().fromJson(sourceValue, PhoneTypeValue::class.java)
                val number = typeValue.phone

//                val formatNumber = if (number.isNotEmpty())
//                    PhoneNumberUtils.formatNumber(
//                    number,
//                    Locale.getDefault().country
//                ) else
//                    ""
                val name = typeValue.name
                val lastName = typeValue.lastName
                val organization = typeValue.organization

                val info = StringBuilder("")

                typePref.sort.forEachIndexed { index, id ->
                    var s = ""
                    when (id) {
                        0 ->
                            if (typePref.name)
                                s = name
                        1 ->
                            if (typePref.lastName)
                                s = lastName
                        2 ->
                            if (typePref.phone)
                                s = number
                        3 ->
                            if (typePref.organization)
                                s = organization
                    }
                    val notEmpty = s.isNotEmpty()
                    if (index > 0 && notEmpty && info.isNotEmpty())
                        info.append("\n")

                    if (notEmpty)
                        info.append(s)
                }
                displayValue = info.toString()
            }
            is ImageTypePref -> {
                val imageTypeValue = Gson().fromJson(sourceValue, ImageTypeValue::class.java)
                val imageUriList = imageTypeValue.imagePathList
                displayValue =
                    if (imageUriList.isEmpty())
                        Uri.EMPTY.toString()
                    else {
                        val changeImage = imageTypeValue.changeImage
                        if (changeImage > -1)
                            imageUriList[changeImage]
                        else
                            imageUriList[0]
                    }

            }
            else -> {
                displayValue = sourceValue
            }
        }
    }

    fun displayCellView() {
        cellTypeControl.display(elementView, displayValue)
    }

    fun copy(): Cell {
        return Cell(sourceValue).also {
            it.type = type
            it.cellTypeControl = cellTypeControl
            it.displayValue = displayValue
        }
    }

    // опустошить
    fun clear() {
        sourceValue = when (type) {
            ColumnType.PHONE -> Gson().toJson(PhoneTypeValue())
            ColumnType.COLOR -> Color.WHITE.toString()
            ColumnType.SWITCH -> false.toString()
            else -> {
                ""
            }
        }
    }
}