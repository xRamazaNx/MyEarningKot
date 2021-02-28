package ru.developer.press.myearningkot.model

import android.graphics.Typeface
import android.widget.TextView
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.helpers.getColorFromText
import ru.developer.press.myearningkot.helpers.setFont

class SortPref(
    var isSave: Boolean = false,
    var sortMethod: SortMethod = SortMethod.UP,
    // id колоны по которой идет сортировка
    var sortFofColumnId: Int = 0
)

abstract class Prefs : ResetPreferences

class PrefForTextView(
    var textSize: Int = 16,
    var isBold: Boolean = false,
    var isItalic: Boolean = false,
    var color: Int = getColorFromText(),
    var align: Int = TextView.TEXT_ALIGNMENT_CENTER // 0 - left, 1 - center, 2 right
) : ResetPreferences {
    fun customize(textView: TextView?, fontRes:Int = R.font.roboto) {
        textView?.also {
            it.textSize = this.textSize.toFloat()
            it.setTextColor(color)
            val style =
                if (isBold) {
                    if (isItalic)
                        Typeface.BOLD_ITALIC
                    else
                        Typeface.BOLD
                } else if (isItalic)
                    Typeface.ITALIC
                else
                    Typeface.NORMAL

            it.setFont(fontRes, style)
            it.textAlignment = align
        }
    }

    override fun resetPref() {
        textSize = 14
        isBold = false
        isItalic = false
        color = getColorFromText()
        align = TextView.TEXT_ALIGNMENT_CENTER // 0 - left, 1 - center, 2 right
    }

}

open class TextTypePref(
    var prefForTextView: PrefForTextView = PrefForTextView().apply {
        textSize = 14
    }
) : Prefs() {
    override fun resetPref() {
        prefForTextView.resetPref()
    }
}


class PhoneTypePref : TextTypePref() {
    var sort: MutableList<Int> = mutableListOf(0, 1, 2, 3)

    var name: Boolean = true
    var lastName: Boolean = true
    var phone: Boolean = true
    var organization: Boolean = true

    override fun resetPref() {
        super.resetPref()

        sort = mutableListOf(0, 1, 2, 3)
        name = true
        lastName = true
        phone = false
        organization = false
    }
}

class DateTypePref(
    var type: Int = 1,
    var enableTime: Boolean = true
) : TextTypePref() {
    init {
        resetPref()
    }
    override fun resetPref() {
        super.resetPref()
        type = 0
        enableTime = true
        prefForTextView.textSize = 12
    }
}

class NumberTypePref(
    var digitsCount: Int = 2,
    var isGrouping: Boolean = true,
    var groupSize: Int = 3
) : TextTypePref() {
    init {
        prefForTextView.textSize = 16
    }
    override fun resetPref() {
        super.resetPref()
        prefForTextView.textSize = 16
        digitsCount = 2
        isGrouping = true
        groupSize = 3
    }
}

class ColorTypePref : Prefs() {
    override fun resetPref() {

    }
}

class SwitchTypePref : Prefs() {
    var isTextSwitchMode = false

    var textEnable = "Вкл"
    var textDisable = "Выкл"
    var enablePref = PrefForTextView()
    var disablePref = PrefForTextView()

    var behavior = Behavior()

    override fun resetPref() {
        isTextSwitchMode = false
        textEnable = "Вкл"
        textDisable = "Выкл"
        enablePref.resetPref()
        disablePref.resetPref()

        behavior = Behavior()
    }


    class Behavior {
        var crossOut = false
        var control = false
    }

}

class ImageTypePref : Prefs() {
    var imageViewMode = 0
    override fun resetPref() {
        imageViewMode = 0
    }
}

class ListTypePref : TextTypePref() {

    init {
        prefForTextView.isItalic = true
    }
    var listTypeIndex = -1
    override fun resetPref() {
        super.resetPref()
        listTypeIndex = -1
    }
}

interface ResetPreferences {
    fun resetPref()
}
