package ru.developer.press.myearningkot.model

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.toSpannable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.card.view.*
import kotlinx.android.synthetic.main.total_item.view.*
import kotlinx.android.synthetic.main.total_item_layout.view.*
import org.jetbrains.anko.*
import ru.developer.press.myearningkot.*
import ru.developer.press.myearningkot.activity.CardActivity
import ru.developer.press.myearningkot.model.Formula.Companion.COLUMN_ID
import ru.developer.press.myearningkot.model.Formula.Companion.OTHER
import ru.developer.press.myearningkot.model.Formula.Companion.TOTAL_ID
import ru.developer.press.myearningkot.otherHelpers.*
import ru.developer.press.myearningkot.otherHelpers.PrefLayouts.multiplyChar
import ru.developer.press.myearningkot.otherHelpers.PrefLayouts.subtractChar
import java.util.*
import kotlin.Exception
import kotlin.random.Random

// каточка
class Card(var name: String = "") : ProvideCardPropertyForCell {

    var isShowDatePeriod: Boolean = false
    var idPage = -1L
    var id = -1L
    var isShowTotalInfo = true
    val cardPref = PrefForCard().initDefault()

    var valuta = 0
    var enableSomeStroke = true
    val sortPref = SortPref()
    var enableHorizontalScroll = false
    var enableHorizontalScrollTotal = false
    var heightCells = 35
        set(value) {
            if (value in 18..70) {
                field = value
            }
        }
    var dateCreated = Date().time
    var dateModify = dateCreated
    val rows = mutableListOf<Row>()
    var columns = mutableListOf<Column>()
    var totals = mutableListOf<TotalItem>()


//    // ид колон которые суммируются
//    val sumColumnId = mutableSetOf<Long>()
//    val avansColumnId = mutableSetOf<Long>()

    private val dateOfPeriod: String
        get() {
            val variantDate = cardPref.dateOfPeriodPref.type
            val enableTime = cardPref.dateOfPeriodPref.enableTime
            val first = getDate(variantDate, dateCreated, enableTime)
            val last = getDate(variantDate, dateModify, enableTime)
            return "$first - $last"
        }

    init {
        addColumn(ColumnType.NUMERATION, "№").apply {
            width = App.instance?.dimen(R.dimen.column_height) ?: 50
        }
        addTotal()
        // временная суета
//        addColumn(ColumnType.NUMBER, "Столбец")
//        fillTotalAmount()
    }

    fun addTotal() {
        totals.add(TotalItem().apply {
            formula.formulaElements.add(Formula.FormulaElement(OTHER, "0"))
        })
    }

    fun deleteTotal(index: Int): Boolean {
        return if (totals.size > 1) {
            totals.removeAt(index)
            true
        } else
            false
    }

    fun addRow(
        row: Row = Row().apply {
            cellList = mutableListOf<Cell>().apply {
                columns.forEach { column ->
                    add(getNewCell(column))
                }
            }
        }
    ) :Row{
        row.status = Row.Status.ADDED
        rows.add(row)
        updateTypeControlRow(rows.size - 1)
        return row
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
                        phone = 89881234567.toString(),
                        name = "Иван",
                        lastName = "Иванов",
                        organization = "press dev"
                    )
                )

            is DateColumn -> Date().time.toString()
            is NumberColumn -> Random.nextDouble(100.987644, 15956.9999999).toString()
            else -> {
                "текст который может быть таким длинным что он просто не помещается на экране"
            }
        }
        cellTypeControl = column.columnTypeControl
    }

    fun deleteColumn(column: Column? = null): Boolean {
        // если колоны пусты то ни чего не делаем
        if (column is NumerationColumn || columns.size == 1)
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
//        // удаляем ид колоны из списка суммируемых если он есть в нем
//        sumColumnId.forEach {
//            if (it == col.id) {
//                sumColumnId.remove(it)
//                return true
//            }
//        }
//        // удаляем ид колоны из списка авансируемых если он есть в нем
//        avansColumnId.forEach {
//            if (it == col.id) {
//                avansColumnId.remove(it)
//                return true
//            }
//
//        }
        return true
    }

    private fun findColumnAtId(idColumn: Long): Column? {
        columns.forEach {
            if (it.id == idColumn)
                return it
        }
        return null
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
                    getDateTypeList()[0]
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
//        fillTotalAmount()
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
        val context = totalAmountView.context
        val nameCard = totalAmountView.nameCard
        val datePeriodCard = totalAmountView.datePeriodCard

        nameCard.text = name
        val isCardActivity = context is CardActivity
        datePeriodCard.visibility = if (isShowDatePeriod && !isCardActivity) View.VISIBLE else GONE
        datePeriodCard.text = dateOfPeriod

        // визуальная настройка
        cardPref.namePref.customize(nameCard)
        cardPref.dateOfPeriodPref.prefForTextView.customize(datePeriodCard)

        //главный контейнер для заголовков и значений
        val totalContainer: LinearLayout =
            context.layoutInflater.inflate(
                R.layout.total_item_layout,
                null
            ) as LinearLayout
        totalContainer.layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)

        //удаляем где бы не были
        totalAmountView.totalContainerDisableScroll.removeAllViews()
        totalAmountView.totalContainerScroll.removeAllViews()
        // добавляем в главный лейаут
        if (enableHorizontalScrollTotal) {
            totalAmountView.totalContainerScroll.addView(totalContainer)
        } else {
            totalAmountView.totalContainerDisableScroll.addView(totalContainer)
        }
        // контейнер для всех значений
        val totalValueLayout = totalContainer.totalValueContainer
        // кнтейнер для всех заголовков
        val totalTitleLayout = totalContainer.totalTitleContainer

        totals.forEachIndexed { index, totalItem ->
            // лайот где валуе и линия
            val valueLayout =
                context.layoutInflater.inflate(R.layout.total_item_value, null)

            val layoutParams = LinearLayout.LayoutParams(totalItem.width, matchParent).apply {
                weight = 1f
            }
            valueLayout.layoutParams = layoutParams
            if (index == totals.size - 1) {
                valueLayout._verLine.visibility = GONE
            }

            val title = TextView(context).apply {
                this.layoutParams = layoutParams
                gravity = Gravity.CENTER
                padding = 5
            }
            val value = valueLayout.totalValue

            title.text = totalItem.title
            title.maxLines = 1
            title.ellipsize = TextUtils.TruncateAt.END
            totalItem.titlePref.customize(title)

            totalItem.totalPref.prefForTextView.customize(value)
            totalItem.calcFormula(this)
            value.text = totalItem.value

            totalTitleLayout.addView(title)
            totalValueLayout.addView(valueLayout)
        }

    }

    fun updateTypeControlColumn(column: Column) {
        column.updateTypeControl(this)
        val indexOf = columns.indexOf(column)
        rows.forEachIndexed { rowIndex, row ->
            row.cellList[indexOf].also { cell ->
                cell.cellTypeControl = column.columnTypeControl
                cell.type = column.getType()
                when (column) {
                    is NumerationColumn ->
                        cell.updateTypeValue(column.typePref)
                    is TextColumn ->
                        cell.updateTypeValue(column.typePref)
                    is NumberColumn -> {
                        if (column.inputType == InputTypeNumberColumn.FORMULA) {
                            cell.sourceValue = column.calcFormula(rowIndex, this)
                        }
                        cell.updateTypeValue(column.typePref)
                    }
                    is PhoneColumn ->
                        cell.updateTypeValue(column.typePref)
                    is DateColumn ->
                        cell.updateTypeValue(column.typePref)
                    is ColorColumn ->
                        cell.updateTypeValue(column.typePref)
                    is SwitchColumn ->
                        cell.updateTypeValue(column.typePref)
                    is ImageColumn ->
                        cell.updateTypeValue(column.typePref)
                    is ListColumn ->
                        cell.updateTypeValue(column.typePref)
                }

            }
        }
    }

    fun updateTypeControlRow(indexRow: Int) {
        columns.forEachIndexed { index, column ->
            rows[indexRow].cellList[index].also { cell ->
                cell.cellTypeControl = column.columnTypeControl
                cell.type = column.getType()
                when (column) {
                    is NumerationColumn ->
                        cell.updateTypeValue(column.typePref)
                    is TextColumn ->
                        cell.updateTypeValue(column.typePref)
                    is NumberColumn -> {
                        if (column.inputType == InputTypeNumberColumn.FORMULA) {
                            cell.sourceValue = column.calcFormula(indexRow, this)
                        }
                        cell.updateTypeValue(column.typePref)
                    }
                    is PhoneColumn ->
                        cell.updateTypeValue(column.typePref)
                    is DateColumn ->
                        cell.updateTypeValue(column.typePref)
                    is ColorColumn ->
                        cell.updateTypeValue(column.typePref)
                    is SwitchColumn ->
                        cell.updateTypeValue(column.typePref)
                    is ImageColumn ->
                        cell.updateTypeValue(column.typePref)
                    is ListColumn ->
                        cell.updateTypeValue(column.typePref)
                }

            }
        }
    }

    fun unSelectCell(): Int {
        rows.forEachIndexed { rowIndex, row ->
            val cell = row.cellList.find { it.isSelect }
            if (cell != null) {
                cell.isSelect = false
                return rowIndex
            }
        }
        return -1
    }

    fun unSelectRows() {
        rows.forEach {
            it.status = Row.Status.NONE
        }
    }

    fun getSelectedCell(): Cell? {
        rows.forEach { row ->
            row.cellList.forEach { cell ->
                if (cell.isSelect) {
                    return cell
                }
            }
        }
        return null
    }

    fun deleteRow(row: Row) {
        rows.remove(row)
    }

    fun getSelectedRows(): MutableList<Row> {
        val selRows = mutableListOf<Row>()
        rows.forEach {
            if (it.status == Row.Status.SELECT)
                selRows.add(it)
        }
        return selRows
    }

}

class PrefForCard(
    var namePref: PrefForTextView = PrefForTextView(),
    var dateOfPeriodPref: DateTypePref = DateTypePref()

) {
    fun initDefault(): PrefForCard {
        namePref.color = Color.WHITE
        val app = App.instance
        if (app != null) {
            dateOfPeriodPref.prefForTextView.color = app.getColorFromRes(R.color.gray)
        }
        return this
    }
}


data class Cell(
    @SerializedName("v")
    var sourceValue: String = ""
) {
    @Transient
    var type = ColumnType.TEXT

    @Transient
    var isSelect: Boolean = false

    @Transient
    var isPrefColumnSelect = false

    @Transient
    lateinit var cellTypeControl: CellTypeControl

    fun updateTypeValue(typePref: Prefs) {
        when (typePref) {
            is NumberTypePref -> {
                val calc = Calc()
                displayValue =
                    if (sourceValue == "")
                        ""
                    else
                        try {
                            //                    val double = sourceValue.toDouble()
                            val value = calc.evaluate(sourceValue)!!
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
                    Gson().fromJson<PhoneTypeValue>(sourceValue, PhoneTypeValue::class.java)
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

class TotalItem {

    val id = Date().time + Random.nextLong()
    var width = 250
    var formula: Formula = Formula()
    var title: String = "ИТОГ"
    var value: String = "0"
    var titlePref: PrefForTextView = PrefForTextView().apply {
        color = Color.WHITE
    }
    var totalPref: NumberTypePref = NumberTypePref().apply {
        prefForTextView.color = Color.WHITE
    }

    fun calcFormula(card: Card) {
        val calc = Calc()
        val string = java.lang.StringBuilder()
        formula.formulaElements.forEach { element ->
            val elementVal = element.value
            if (element.type == COLUMN_ID) {
                val elementId = elementVal.toLong()
                if (card.columns.any { it.id == elementId }) {
                    string.append(card.getSumFromColumn(elementId))
                } else {
                    formula.formulaElements.remove(element)
                    calcFormula(card)
                    return
                }
            }
            if (element.type == TOTAL_ID) {
                val elementId = elementVal.toLong()
                if (card.totals.any { it.id == elementId }) {
                    val findTotal = card.totals.find { it.id == elementId }!!
                    findTotal.calcFormula(card)
//                    logD(findTotal.value)
                    string.append(findTotal.value)
                } else {
                    formula.formulaElements.remove(element)
                    calcFormula(card)
                    return
                }
            } else
                string.append(elementVal)
        }
        value = try {
            val d = calc.evaluate(string.toString())!!
            getDecimalFormatNumber(d, totalPref)
        } catch (exception: Exception) {

            "Error formula"
        }
    }

    private fun Card.getSumFromColumn(id: Long): String {

        var index = -1
        val calc = Calc()
        var value = 0.0
        columns.forEachIndexed { i, column ->
            if (column.id == id) {
                index = i
                return@forEachIndexed
            }
        }
        // есть ли в карточке свитч колона
        val isAnySwitchColumn = columns.any { it is SwitchColumn }
        return if (index > -1) {
            val column = columns[index] as NumberColumn
            rows.forEach { row ->
                var isAddSum = true
                if (isAnySwitchColumn)
                // проходим по колонам в основном для того что бы узнать индекс для доступа к ячейке
                    columns.forEachIndexed { columnIndex, column ->
                        // если нашли
                        if (column is SwitchColumn) {
                            // в настройках включена опция "учитывать в итоговой панели"
                            if (column.typePref.behavior.control)
                                isAddSum = row.cellList[columnIndex].sourceValue.toBoolean()
                        }
                    }

                val cell = row.cellList[index]
                if (isAddSum)
                    value += try {
                        calc.evaluate(cell.sourceValue)!!
                    } catch (exc: Exception) {
                        return "Error"
                    }
            }
            value.toString()
        } else
            "Error"
    }
}

class Row {
    fun copy(): Row {
        return Row().apply {
            this.dateModify = this@Row.dateModify
            this.status = Status.SELECT
            this@Row.cellList.forEach {
                cellList.add(it.copy())
            }
        }
    }

    @Transient
    var status = Status.NONE

    @SerializedName("dc")
    val dateCreated = Date().time

    @SerializedName("dm")
    var dateModify = Date().time

    @SerializedName("sl")
    var cellList = mutableListOf<Cell>()

    enum class Status {
        SELECT, DELETED, NONE, ADDED
    }
}

// для отображения записи внутри карточки

class DisplayParam {
    var width: Int = 0
    var height: Int = 0
}

class PhoneTypeValue(
    var name: String = "",
    var lastName: String = "",
    var phone: String = "",
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

class ListType {
    var listName: String = ""
    var list: MutableList<String> = mutableListOf()
}

class Formula {
    fun getFormulaString(columnList: List<NumberColumn>, totalList: List<TotalItem>?): Spannable {
        val spannable = SpannableStringBuilder()
        val strBuilder = java.lang.StringBuilder()
        val instance = App.instance!!
        formulaElements.forEach { element ->
            when (element.type) {
                COLUMN_ID -> {
                    columnList.forEach {
                        if (it.id == element.value.toLong()) {
                            val name = it.name
                            strBuilder.append(name)
                            spannable.append(SpannableString(name).apply {
                                setSpan(
                                    ForegroundColorSpan(instance.getColorFromRes(R.color.md_green_300)),
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
                                    ForegroundColorSpan(instance.getColorFromRes(R.color.md_blue_200)),
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
                            ForegroundColorSpan(Color.WHITE),
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

    fun copyFrom(_formula: Formula) {
        formulaElements.clear()
        _formula.formulaElements.forEach { element ->
            formulaElements.add(element.copy())
        }
    }

    fun getColumnIdList(): List<Long> {
        val list = mutableListOf<Long>()

        formulaElements.forEach {
            if (it.type == COLUMN_ID)
                list.add(it.value.toLong())
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
        val OTHER = 0
        val COLUMN_ID = 1
        val TOTAL_ID = 2
    }

    val formulaElements = mutableListOf<FormulaElement>()

    data class FormulaElement(var type: Int = 0, var value: String = "")

}
