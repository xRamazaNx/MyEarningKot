package ru.developer.press.myearningkot.model

import android.graphics.Typeface
import android.widget.TextView
import androidx.room.Embedded
import com.google.gson.annotations.SerializedName
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.helpers.getColorFromText
import ru.developer.press.myearningkot.helpers.setFont

class PrefForCard(
    @Embedded
    var namePref: PrefForTextView = PrefForTextView(),
    @Embedded
    var dateOfPeriodPref: DateTypePref = DateTypePref()

) {

    companion object {
        var nameColor = 0
    }

    init {
        initDefault()
    }

    private fun initDefault() {
        dateOfPeriodPref.prefForTextView.textSize = 14
        dateOfPeriodPref.prefForTextView.color = NumerationColumn.color
        namePref.color = nameColor

        namePref.textSize = 18
    }
}

class SortPref(

    var isSave: Boolean = false,
    var sortMethod: SortMethod = SortMethod.UP,
    // id колоны по которой идет сортировка
    var sortFofColumnId: Int = 0
)

abstract class Prefs : ResetPreferences

class PrefForTextView(
    @SerializedName("ts")
    var textSize: Int = 16,
    @SerializedName("ib")
    var isBold: Boolean = false,
    @SerializedName("ii")
    var isItalic: Boolean = false,
    @SerializedName("c")
    var color: Int = getColorFromText(),
    @SerializedName("a")
    var align: Int = TextView.TEXT_ALIGNMENT_CENTER // 0 - left, 1 - center, 2 right
) : ResetPreferences {
    fun customize(textView: TextView?, fontRes: Int = R.font.roboto) {
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
    @SerializedName("pftv")
    var prefForTextView: PrefForTextView = PrefForTextView().apply {
        textSize = 14
    }
) : Prefs() {
    override fun resetPref() {
        prefForTextView.resetPref()
    }
}


class PhoneTypePref : TextTypePref() {
    @SerializedName("s")
    var sort: MutableList<Int> = mutableListOf(0, 1, 2, 3)

    @SerializedName("n")
    var name: Boolean = true

    @SerializedName("ln")
    var lastName: Boolean = true

    @SerializedName("p")
    var phone: Boolean = true

    @SerializedName("o")
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
    @SerializedName("t")
    var type: Int = 1,
    @SerializedName("et")
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
    @SerializedName("dc")
    var digitsCount: Int = 2,
    @SerializedName("ig")
    var isGrouping: Boolean = true,
    @SerializedName("gs")
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
    @SerializedName("itsm")
    var isTextSwitchMode = false

    @SerializedName("te")
    var textEnable = "Вкл"

    @SerializedName("td")
    var textDisable = "Выкл"

    @SerializedName("ep")
    var enablePref = PrefForTextView()

    @SerializedName("dp")
    var disablePref = PrefForTextView()

    @SerializedName("b")
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
    @SerializedName("ivm")
    var imageViewMode = 0
    override fun resetPref() {
        imageViewMode = 0
    }
}

class ListTypePref : TextTypePref() {

    init {
        prefForTextView.isItalic = true
    }

    @SerializedName("lti")
    var listTypeIndex = -1
    override fun resetPref() {
        super.resetPref()
        listTypeIndex = -1
    }
}

interface ResetPreferences {
    fun resetPref()
}
