package ru.developer.press.myearningkot.model

import android.content.Context
import android.view.View
import com.google.gson.annotations.SerializedName
import ru.developer.press.myearningkot.*
import ru.developer.press.myearningkot.adapters.PhoneParamModel
import ru.developer.press.myearningkot.otherHelpers.*
import java.util.*
import kotlin.random.Random

abstract class Column(var name: String) {
    @SerializedName(column_cast_gson)
    var className = javaClass.name


    val id: Long = Date().time + Random.nextLong(99999)
    @SerializedName("tp")
    val titlePref: PrefForTextView = PrefForTextView()
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

    override fun setDefaultPref() {
        typePref.resetPref()
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

    override fun updateTypeControl(provideCardProperty: ProvideCardPropertyForCell) {

        columnTypeControl = NumberTypeControl(getProvideProperty(provideCardProperty)).apply {
            provideValueProperty.typePref = typePref
        }
    }

    override fun setDefaultPref() {
        typePref.resetPref()
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

    fun getPhoneParamList(): MutableList<PhoneParamModel> {
        val app = App.instance!!

        return mutableListOf<PhoneParamModel>().apply {
            val name = app.getString(R.string.name_man)
            val lastName = app.getString(R.string.last_name)
            val phone = app.getString(R.string.phone)
            val organization = app.getString(R.string.organization)

            typePref.sort.forEach { id ->
                when (id) {
                    0 ->
                        add(PhoneParamModel(name, typePref.name, 0))
                    1 ->
                        add(PhoneParamModel(lastName, typePref.lastName, 1))
                    2 ->
                        add(PhoneParamModel(phone, typePref.phone, 2))
                    3 ->
                        add(PhoneParamModel(organization, typePref.organization, 3))
                }
            }
        }
    }

    fun editPhoneParam(phoneParamModel: PhoneParamModel) {
        val check = phoneParamModel.isCheck
        when (phoneParamModel.id) {
            0 -> typePref.name = check
            1 -> typePref.lastName = check
            2 -> typePref.phone = check
            3 -> typePref.organization = check
        }
    }

    fun sortPositionParam(list: List<PhoneParamModel>) {
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

    var isCrossRow = false
    var isAcceptRow = false

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

