package ru.developer.press.myearningkot.model

import android.graphics.Color
import android.telephony.PhoneNumberUtils
import android.view.View
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.card.view.*
import org.jetbrains.anko.dimen
import ru.developer.press.myearningkot.App
import ru.developer.press.myearningkot.CellTypeControl
import ru.developer.press.myearningkot.ProvideCardPropertyForCell
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.otherHelpers.*
import java.util.*

// каточка
class Card(var name: String = "") : ProvideCardPropertyForCell {

    var idPage = -1L
    var id = -1L
    val cardPref = PrefForCard().initDefault()

    var dateType = 0
    var valuta = 0
    @SerializedName("ess")
    var enableSomeStroke = true
    val sortPref = SortPref()
    @SerializedName("ehs")
    var enableHorizontalScroll = false
    @SerializedName("hc")
    var heightCells = App.instance?.dimen(R.dimen.column_height) ?: 50
    var dateCreated = Date().time
    var dateModify = dateCreated
    val rows = mutableListOf<Row>()
    var columns = mutableListOf<Column>()
    // ид колон которые суммируются
    val sumColumnId = mutableListOf<Long>()
    val avansColumnId = mutableListOf<Long>()

    val dateOfPeriod: String
        get() = " date"
    @Transient
    val totalAmount: TotalAmountOfCard = TotalAmountOfCard()

    init {
        addColumn(ColumnType.NUMERATION, "№").apply {
            width = App.instance?.dimen(R.dimen.column_height) ?: 50
        }
        // временная суета
        addColumn(ColumnType.NUMBER, "Столбец")
        fillTotalAmount()
    }

    fun addRow(
        row: Row = Row().apply {
            cellList = mutableListOf<Cell>().apply {
                columns.forEach { column ->
                    add(getNewCell(column))
                }
            }
        }
    ) {
        rows.add(row)
    }

    fun addColumn(type: ColumnType, name: String, position: Int = columns.size): Column {
        val column = when (type) {
            ColumnType.NUMERATION -> NumerationColumn(name)
            ColumnType.NUMBER -> NumberColumn(name)
            ColumnType.PHONE -> PhoneColumn(name)
            ColumnType.DATE -> DateColumn(name)
            ColumnType.COLOR -> ColorColumn(name)
            ColumnType.SWITCH -> SwitchColumn(name)
            ColumnType.IMAGE -> ImageColumn(name)
            ColumnType.LIST -> ListColumn(name)
            ColumnType.TEXT -> TextColumn(name)
            // не будет ни когда использоваться
            ColumnType.NONE -> TextColumn(name)
        }
        column.titlePref.color = Color.LTGRAY
        column.updateTypeControl(this)
        columns.add(position, column)
        rows.forEach {
            it.cellList.add(position, getNewCell(column))
        }

        return column

    }

    private fun getNewCell(column: Column): Cell = Cell().apply {
        val gson = Gson()
        sourceValue = when (column) {
            is ColorColumn -> {
                Color.WHITE.toString()
            }
            is ImageColumn ->
                getURLForResource(R.drawable.ic_image).toString()
            is SwitchColumn -> {
                val r = (0..1).random()
                (r == 0).toString()
            }
            is ListColumn ->
                "Выбранный элемент"
            is PhoneColumn ->
                gson.toJson(
                    PhoneTypeValue(
                        number = 89881234567,
                        name = "Иван",
                        lastName = "Иванов",
                        organization = "press dev"
                    )
                )

            is DateColumn -> Date().time.toString()
            is NumberColumn -> 12345.98765.toString()
            else -> {
                "текст который может быть таким длинным что он просто не помещается на экране"
            }
        }
        cellTypeControl = column.columnTypeControl
    }

    fun deleteColumn(column: Column? = null): Boolean {
        // если колоны пусты то ни чего не делаем
        if (column is NumerationColumn)
            return false

        // ищем колону по параметрам и без
        val lastIndex = columns.size - 1
        val col = when {
            // если
            column != null -> column
            else -> columns[lastIndex]
        }
        val index = columns.indexOf(col)
        // удалаяем
        columns.remove(col)
        rows.forEach {
            it.cellList.removeAt(index)
        }
        // удаляем ид колоны из списка суммируемых если он есть в нем
        sumColumnId.forEach {
            if (it == col.id) {
                sumColumnId.remove(it)
                return true
            }
        }
        // удаляем ид колоны из списка авансируемых если он есть в нем
        avansColumnId.forEach {
            if (it == col.id) {
                avansColumnId.remove(it)
                return true
            }

        }
        return true
    }

    private fun findColumnAtId(idColumn: Long): Column? {
        columns.forEach {
            if (it.id == idColumn)
                return it
        }
        return null
    }

    private fun fillTotalAmount() {
        if (rows.isEmpty())
            return

        var tempSum = 0.0
        var tempAvans = 0.0

        sumColumnId.forEach { idColumn ->
            columns.forEachIndexed { indexColumn, column ->
                if (idColumn == column.id) {
                    rows.forEach { row ->
                        //                        if (  если в карте в записях есть переключатель то проыерить регулирует ли он участвование в общей сумме)
                        tempSum += row.cellList[indexColumn].sourceValue.toDouble()
                    }
                }
            }
        }
        avansColumnId.forEach { idColumn ->
            columns.forEachIndexed { indexColumn, column ->
                if (idColumn == column.id) {
                    rows.forEach {
                        tempAvans += it.cellList[indexColumn].sourceValue.toDouble()
                    }
                }
            }
        }

        totalAmount.sum = tempSum
        totalAmount.avans = tempAvans
    }


    private fun getCellOfSample(position: Int): Cell {
        val column = columns[position]
        return Cell().apply {
            cellTypeControl = column.columnTypeControl
            sourceValue = when (column) {
                is ImageColumn -> {
                    getURLForResource(R.drawable.ic_image).toString()
                }
                is SwitchColumn -> {
                    "true"
                }
                is ColorColumn -> {
                    Color.GREEN.toString()
                }
                is ListColumn -> {
                    "Выбранный элемент"
                }
                is DateColumn -> {
                    getDateTypeList()[dateType]
                }
                is NumberColumn -> {
                    "12345.987"
                }
                is PhoneColumn -> {
                    "7 999 123-45-67"
                }
                is NumerationColumn -> {
                    "1"
                }
                // если та где можно использовать текст
                else -> {
                    "текст который может быть порой очень длинным"
                }
            }
        }
    }

    fun updateTypeControl() {
        columns.forEach { column ->
            updateTypeControlColumn(column)
        }
        fillTotalAmount()
    }

    fun addSampleRow() {
        val row = mutableListOf<Cell>()
        columns.forEachIndexed { index, _ ->
            row.add(getCellOfSample(index))
        }
        rows.add(Row().apply { cellList = row })
    }

    fun deleteRow(index: Int = rows.size - 1) {
        if (rows.isEmpty())
            return
        rows.removeAt(index)

    }

    override fun isSingleLine(): Boolean = !enableSomeStroke

    override fun getValutaType(): Int = valuta

    fun customizeTotalAmount(totalAmountView: View) {
        val nameCard = totalAmountView.nameCard
        val datePeriodCard = totalAmountView.datePeriodCard
        val sum = totalAmountView.sum
        val avans = totalAmountView.avans
        val balance = totalAmountView.balance
        val sumTitle = totalAmountView.sumTitle
        val avansTitle = totalAmountView.avansTitle
        val balanceTitle = totalAmountView.balanceTitle

        nameCard.text = name
        datePeriodCard.text = dateOfPeriod
        // назначаем значения
        val totalAmount = totalAmount
        sum.text = totalAmount.sum.toString()
        avans.text = totalAmount.avans.toString()
        balance.text = totalAmount.balance.toString()

        //имена
        sumTitle.text = cardPref.sumTitle
        avansTitle.text = cardPref.avansTitle
        balanceTitle.text = cardPref.balanceTitle
        // визуальная настройка
        cardPref.namePref.customize(nameCard)
        cardPref.dateOfPeriodPref.customize(datePeriodCard)

        cardPref.sumPref.customize(sum)
        cardPref.sumTitlePref.customize(sumTitle)

        cardPref.avansPref.customize(avans)
        cardPref.avansTitlePref.customize(avansTitle)

        cardPref.balancePref.customize(balance)
        cardPref.balanceTitlePref.customize(balanceTitle)
    }

    fun updateTypeControlColumn(column: Column) {
        column.updateTypeControl(this)
        val indexOf = columns.indexOf(column)
        rows.forEach { row ->
            row.cellList[indexOf].also {
                it.cellTypeControl = column.columnTypeControl
                when (column) {
                    is NumerationColumn ->
                        it.updateTypeValue(column.typePref)
                    is TextColumn ->
                        it.updateTypeValue(column.typePref)
                    is NumberColumn ->
                        it.updateTypeValue(column.typePref)
                    is PhoneColumn ->
                        it.updateTypeValue(column.typePref)
                    is DateColumn ->
                        it.updateTypeValue(column.typePref)
                    is ColorColumn ->
                        it.updateTypeValue(column.typePref)
                    is SwitchColumn ->
                        it.updateTypeValue(column.typePref)
                    is ImageColumn ->
                        it.updateTypeValue(column.typePref)
                    is ListColumn ->
                        it.updateTypeValue(column.typePref)

                }

            }
        }
    }

}

class PrefForCard(
    var sumTitle: String = "СУММА",
    var avansTitle: String = "АВАНС",
    var balanceTitle: String = "ОСТАТОК",

    var namePref: PrefForTextView = PrefForTextView(),
    var dateOfPeriodPref: PrefForTextView = PrefForTextView(),

    var sumTitlePref: PrefForTextView = PrefForTextView(),
    var sumPref: PrefForTextView = PrefForTextView(),
    var avansTitlePref: PrefForTextView = PrefForTextView(),
    var avansPref: PrefForTextView = PrefForTextView(),
    var balanceTitlePref: PrefForTextView = PrefForTextView(),
    var balancePref: PrefForTextView = PrefForTextView()
) {
    fun initDefault(): PrefForCard {
        namePref.color = Color.WHITE
        val app = App.instance
        if (app != null) {
            dateOfPeriodPref.color = app.getColorFromRes(R.color.gray)
            val titcleColor = app.getColorFromRes(R.color.light_gray)
            sumTitlePref.color = titcleColor
            avansTitlePref.color = titcleColor
            balanceTitlePref.color = titcleColor

            sumPref.color = app.getColorFromRes(R.color.color_sum)
            avansPref.color = app.getColorFromRes(R.color.color_avans)
            balancePref.color = app.getColorFromRes(R.color.color_balance)
        }
        return this
    }

    val visibilityOfDate: Int
        get() {
            return if (true) //  будет браться значение из настроек
                View.VISIBLE
            else
                View.GONE
        }
}


class Cell(
    @SerializedName("v")
    var sourceValue: String = ""
) {
    @Transient
    var isPrefColumnSelect = false
    @Transient
    lateinit var cellTypeControl: CellTypeControl

    fun updateTypeValue(typePref: Prefs) {
        when (typePref) {
            is NumberTypePref -> {
                val double = try {
                    sourceValue.toDouble()
                } catch (exception: NumberFormatException) {
                    0.0
                }
                displayValue = getDecimalFormatNumber(double, typePref)
            }
            is DateTypePref -> {
                val timeML: Long = sourceValue.toLong()
                displayValue = getDate(typePref.type, timeML, typePref.enableTime)
            }
            is PhoneTypePref -> {
                val typeValue =
                    Gson().fromJson<PhoneTypeValue>(sourceValue, PhoneTypeValue::class.java)
                val number = typeValue.number

                val formatNumber =
                    PhoneNumberUtils.formatNumber(number.toString(), Locale.getDefault().country)
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
                                s = formatNumber
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
            else -> {
                displayValue = sourceValue
            }


        }
    }

    @Transient
    var displayValue: String = ""

    fun displayCellView(view: View) {
        cellTypeControl.display(view, displayValue)
    }
}

class Row {
    val dateCreated = Date().time
    var dateModify = Date().time
    var cellList = mutableListOf<Cell>()
}

class TotalAmountOfCard {
    var sum: Double = 0.0
        get() {
            return getDecimalFormatNumber(field).toDouble()
        }
    var avans = 0.0
        get() {
            return getDecimalFormatNumber(field).toDouble()
        }
    val balance: Double
        get() {
            val value = (sum - avans)
            return getDecimalFormatNumber(value).toDouble()
        }
}

//
// для отображения записи внутри карточки


class DisplayParam {
    var width: Int = 0
    var height: Int = 0
}

// value для cell
class CheckTypeValue(
    @SerializedName("ch")
    var check: Boolean = false,
    @SerializedName("v")
    var text: String = "check"
)

class PhoneTypeValue(
    var name: String = "",
    var lastName: String = "",
    var number: Long = 0,
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
                    append = number.toString()
                3 -> if (phoneTypePref.organization)
                    append = organization
            }
            infoBuilder.append(append)
        }
        return infoBuilder.toString()
    }
}

class ListType {
    var listName: String = ""
    var list: MutableList<String> = mutableListOf()
}
