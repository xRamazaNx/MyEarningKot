package ru.developer.press.myearningkot.model

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import com.google.gson.annotations.SerializedName
import ru.developer.press.myearningkot.*
import ru.developer.press.myearningkot.helpers.*
import ru.developer.press.myearningkot.helpers.prefLayouts.multiplyChar
import ru.developer.press.myearningkot.helpers.prefLayouts.subtractChar

// пздц имя
interface Backgrounder {
    var currentBackground: Int

    var elementView:View

    fun setBackground(backgroundRes: Int) {
        currentBackground = backgroundRes
        val drawable =
            elementView.let { ContextCompat.getDrawable(it.context,currentBackground) }
        elementView.background = drawable
    }
}

// для отображения записи внутри карточки

class DisplayParam {
    var width: Int = 0
    var height: Int = 0
}

class PhoneTypeValue(
    @SerializedName("n")
    var name: String = "",
    @SerializedName("ln")
    var lastName: String = "",
    @SerializedName("p")
    var phone: String = "",
    @SerializedName("o")
    var organization: String = ""
) {
    fun getPhoneInfo(phoneTypePref: PhoneTypePref): String {
        val infoBuilder = StringBuilder()
        phoneTypePref.sort.forEach {
            var append = ""
            when (it) {
                0 -> if (phoneTypePref.name)
                    append = name
                1 -> if (phoneTypePref.lastName)
                    append = lastName
                2 -> if (phoneTypePref.phone)
                    append = phone
                3 -> if (phoneTypePref.organization)
                    append = organization
            }
            infoBuilder.append(append)
        }
        return infoBuilder.toString()
    }
}

// примечание: фото копируются в папку в дирректории программы, и потом ячейка ссылается на него
// если копировать эту ячейку, копируется путь к папке в дирректории в программе
// при удалении строки в которой есть изображение - изображение не удаляется из папки, оно остается там как мусор
// надо будет раз в неделю чтоб папка очищалась
class ImageTypeValue {
    fun removePath(selectedTabPosition: Int) {
        imagePathList.removeAt(selectedTabPosition)
        val size = imagePathList.size
        if (changeImage >= size && imagePathList.isNotEmpty())
            changeImage = size - 1
    }

    @SerializedName("ip")
    val imagePathList = mutableListOf<String>()

    @SerializedName("ci")
    var changeImage = -1
}

class ListType {
    @SerializedName("ln")
    var listName: String = ""
    @SerializedName("l")
    var list: MutableList<String> = mutableListOf()
}

class Formula {
    fun getFormulaString(context:Context, columnList: List<NumberColumn>, totalList: List<Total>?): Spannable {
        val spannable = SpannableStringBuilder()
        val strBuilder = java.lang.StringBuilder()
        formulaElements.forEach { element ->
            when (element.type) {
                COLUMN_ID -> {
                    columnList.forEach {
                        if (it.refId == element.value) {
                            val name = it.name
                            strBuilder.append(name)
                            spannable.append(SpannableString(name).apply {
                                setSpan(
                                    ForegroundColorSpan(context.getColorFromRes(R.color.md_green_300)),
                                    0,
                                    name.length,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            })
                        }
                    }
                }
                TOTAL_ID -> {
                    totalList?.forEach {
                        if (it.id == element.value.toLong()) {
                            val title = it.title
                            strBuilder.append(title)
                            spannable.append(SpannableString(title).apply {
                                setSpan(
                                    // fixme добавить нормальные цветовые различия
                                    ForegroundColorSpan(context.getColorFromRes(R.color.colorSecondaryLight)),
                                    0,
                                    title.length,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            })
                        }
                    }
                }
                else -> {
                    var value = element.value
                    if (value == " - ")
                        value = subtractChar
                    if (value == " * ")
                        value = multiplyChar
                    strBuilder.append(value)
                    spannable.append(SpannableString(value).apply {
                        setSpan(
                            ForegroundColorSpan(Color.YELLOW),
                            0,
                            value.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    })
                }
            }
        }
//        return strBuilder.toString()
        return spannable.toSpannable()
    }

    // копировать из формулы в ту из которой вызвали этот метод
    fun copyFrom(_formula: Formula) {
        formulaElements.clear()
        _formula.formulaElements.forEach { element ->
            formulaElements.add(element.copy())
        }
    }

    fun getColumnIdList(): List<String> {
        val list = mutableListOf<String>()

        formulaElements.forEach {
            if (it.type == COLUMN_ID)
                list.add(it.value)
        }
        return list
    }

    fun getTotalIdList(): List<Long> {
        val list = mutableListOf<Long>()

        formulaElements.forEach {
            if (it.type == TOTAL_ID)
                list.add(it.value.toLong())
        }
        return list
    }

    internal companion object {
        const val OTHER = 0
        const val COLUMN_ID = 1
        const val TOTAL_ID = 2
    }

    @SerializedName("fe")
    val formulaElements = mutableListOf<FormulaElement>()

    data class FormulaElement(
        @SerializedName("t")
        var type: Int = 0,
        @SerializedName("v")
        var value: String = ""
    )

}
