package ru.developer.press.myearningkot.model

import android.content.Context
import android.view.View
import com.google.gson.annotations.SerializedName
import ru.developer.press.myearningkot.*
import ru.developer.press.myearningkot.adapters.ParamModel
import ru.developer.press.myearningkot.helpers.*
import java.lang.Exception
import java.lang.StringBuilder
import java.math.BigDecimal
import java.util.*
import kotlin.random.Random

abstract class Column(var name: String) {
    @SerializedName(column_cast_gson)
    var className = javaClass.name

    val id: Long = Date().time + Random.nextLong(99999)

    @SerializedName("tp")
    val titlePref: PrefForTextView = PrefForTextView().apply {
        isBold = true
        App.instance?.apply {
            color = getColorFromRes(R.color.textColorTabsTitleNormal)
        }
    }

    fun resetTitlePref() {
        titlePref.apply {
            isBold = true
            App.instance?.apply {
                color = getColorFromRes(R.color.textColorTabsTitleNormal)
            }
        }
    }

    var width: Int = 350

    @Transient
    lateinit var columnTypeControl: ColumnTypeControl

    fun createCellView(context: Context): View {
        return columnTypeControl.createCellView(context)
    }

    protected fun getProvideProperty(provideCardPropertyForCell: ProvideCardPropertyForCell): ProvideValueProperty {

        return object : ProvideValueProperty {
            override fun getWidthColumn(): Int = width
            override var provideCardPropertyForCell =
                provideCardPropertyForCell
            override var typePref: Prefs? = null

        }
    }

    abstract fun updateTypeControl(provideCardProperty: ProvideCardPropertyForCell)
    abstract fun setDefaultPref()
    fun getType(): ColumnType = when (this) {
        is NumerationColumn -> ColumnType.NUMERATION
        is TextColumn -> ColumnType.TEXT
        is NumberColumn -> ColumnType.NUMBER
        is DateColumn -> ColumnType.DATE
        is PhoneColumn -> ColumnType.PHONE
        is ListColumn -> ColumnType.LIST
        is ImageColumn -> ColumnType.IMAGE
        is SwitchColumn -> ColumnType.SWITCH
        is ColorColumn -> ColumnType.COLOR
        else -> ColumnType.NONE
    }

}

class NumerationColumn(name: String) : Column(name) {
    @SerializedName("cp")
    var typePref = TextTypePref()

    override fun updateTypeControl(provideCardProperty: ProvideCardPropertyForCell) {

        columnTypeControl =
            NumerationTypeControl(getProvideProperty(provideCardProperty)).apply {
                provideValueProperty.typePref = typePref
            }
    }

    init {
        setDefaultPref()
    }

    override fun setDefaultPref() {
        typePref.resetPref()
        typePref.apply {
            prefForTextView.isItalic = true
            prefForTextView.textSize = 14
            App.instance?.apply {
                prefForTextView.color = getColorFromRes(R.color.textColorSecondary)
            }
        }
    }

}

class TextColumn(name: String) : Column(name) {
    @SerializedName("cp")
    var typePref = TextTypePref()

    override fun updateTypeControl(provideCardProperty: ProvideCardPropertyForCell) {

        columnTypeControl = TextTypeControl(getProvideProperty(provideCardProperty)).apply {
            provideValueProperty.typePref = typePref
        }
    }

    override fun setDefaultPref() {
        typePref.resetPref()
    }
}

class NumberColumn(name: String) : Column(name) {
    @SerializedName("cp")
    var typePref = NumberTypePref()

    var formula: Formula = Formula()
    var inputType: InputTypeNumberColumn = InputTypeNumberColumn.MANUAL

    override fun updateTypeControl(provideCardProperty: ProvideCardPropertyForCell) {

        columnTypeControl = NumberTypeControl(getProvideProperty(provideCardProperty)).apply {
            provideValueProperty.typePref = typePref
        }
    }

    override fun setDefaultPref() {
        typePref.resetPref()

    }

    fun calcFormula(rowIndex: Int, card: Card): String {
        val string = StringBuilder()
        return try {
            formula.formulaElements.forEach {
                if (it.type == Formula.COLUMN_ID) {
                    var index = -1
                    card.columns.forEachIndexed { i, column ->
                        if (column.id == it.value.toLong()) {
                            index = i
                            return@forEachIndexed
                        }
                    }
                    if (index == -1) {
                        formula.formulaElements.remove(it)
                        calcFormula(rowIndex, card)
                    } else {

                        val numberColumn = card.columns[index] as NumberColumn
                        val value: String =
                            // проверяем колона работает по формуле или ручной ввод
                            if (numberColumn.inputType == InputTypeNumberColumn.FORMULA) {
                                numberColumn.calcFormula(rowIndex, card)
                            } else {
                                val cell = card.rows[rowIndex].cellList[index]
                                cell.updateTypeValue(numberColumn.typePref)
                                cell.displayValue
                            }
                        string.append(value)
                    }
                } else
                    string.append(it.value)
            }
            val value: Double? = Calc.evaluate(string.toString())
            value?.let { BigDecimal(it).toPlainString() }?: ""
        } catch (exception: Exception) {
            "Error formula cell"
        }
    }
}

class PhoneColumn(name: String) : Column(name) {
    @SerializedName("cp")
    var typePref = PhoneTypePref()

    override fun updateTypeControl(provideCardProperty: ProvideCardPropertyForCell) {

        columnTypeControl = PhoneTypeControl(getProvideProperty(provideCardProperty)).apply {
            provideValueProperty.typePref = typePref
        }
    }

    override fun setDefaultPref() {
        typePref.resetPref()
    }

    fun getPhoneParamList(): MutableList<ParamModel> {
        val app = App.instance!!

        return mutableListOf<ParamModel>().apply {
            val name = app.getString(R.string.name_man)
            val lastName = app.getString(R.string.last_name)
            val phone = app.getString(R.string.phone)
            val organization = app.getString(R.string.organization)

            typePref.sort.forEach { id ->
                when (id) {
                    0 ->
                        add(ParamModel(name, typePref.name, 0))
                    1 ->
                        add(ParamModel(lastName, typePref.lastName, 1))
                    2 ->
                        add(ParamModel(phone, typePref.phone, 2))
                    3 ->
                        add(ParamModel(organization, typePref.organization, 3))
                }
            }
        }
    }

    fun editPhoneParam(paramModel: ParamModel) {
        val check = paramModel.isCheck
        when (paramModel.id) {
            0 -> typePref.name = check
            1 -> typePref.lastName = check
            2 -> typePref.phone = check
            3 -> typePref.organization = check
        }
    }

    fun sortPositionParam(list: List<ParamModel>) {
        list.forEachIndexed { index, param ->
            typePref.sort[index] = param.id
        }
    }

}

class DateColumn(name: String) : Column(name) {
    @SerializedName("cp")
    var typePref = DateTypePref()

    override fun updateTypeControl(provideCardProperty: ProvideCardPropertyForCell) {

        columnTypeControl = DateTypeControl(getProvideProperty(provideCardProperty)).apply {
            provideValueProperty.typePref = typePref
        }
    }

    override fun setDefaultPref() {
        typePref.resetPref()
    }

}

class ColorColumn(name: String) : Column(name) {
    @SerializedName("cp")
    var typePref = ColorTypePref()

    override fun updateTypeControl(provideCardProperty: ProvideCardPropertyForCell) {
        columnTypeControl = ColorTypeControl(getProvideProperty(provideCardProperty)).apply {
            provideValueProperty.typePref = typePref
        }
    }

    override fun setDefaultPref() {
        typePref.resetPref()
    }
//        выбор фигруы для цвета
}

class SwitchColumn(name: String) : Column(name) {
    @SerializedName("cp")
    var typePref = SwitchTypePref()

    override fun updateTypeControl(provideCardProperty: ProvideCardPropertyForCell) {
        columnTypeControl = SwitchTypeControl(getProvideProperty(provideCardProperty)).apply {
            provideValueProperty.typePref = typePref
        }
    }

    override fun setDefaultPref() {
        typePref.resetPref()
    }
    // может будут выборы типа
}

class ImageColumn(name: String) : Column(name) {
    @SerializedName("cp")
    var typePref = ImageTypePref()

    override fun updateTypeControl(provideCardProperty: ProvideCardPropertyForCell) {
        columnTypeControl = ImageTypeControl(getProvideProperty(provideCardProperty)).apply {
            provideValueProperty.typePref = typePref
        }
    }

    override fun setDefaultPref() {
        typePref.resetPref()
    }
    // может рамки не знаю
}

class ListColumn(name: String) : Column(name) {
    @SerializedName("cp")
    var typePref = ListTypePref()

    override fun updateTypeControl(provideCardProperty: ProvideCardPropertyForCell) {

        columnTypeControl = ListTypeControl(getProvideProperty(provideCardProperty)).apply {
            provideValueProperty.typePref = typePref
        }
    }

    override fun setDefaultPref() {
        typePref.resetPref()
    }

}

