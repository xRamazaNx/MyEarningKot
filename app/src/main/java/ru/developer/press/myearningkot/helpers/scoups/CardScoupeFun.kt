package ru.developer.press.myearningkot.helpers.scoups

import android.graphics.Color
import com.google.gson.Gson
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.database.Card
import ru.developer.press.myearningkot.helpers.getPathForResource
import ru.developer.press.myearningkot.model.*
import java.util.*


fun Card.addTotal(): Total {
    val totalItem = Total(pageId, refId).apply {
        formula.formulaElements.add(Formula.FormulaElement(Formula.OTHER, "0"))
    }
    totals.add(totalItem)

    return totalItem
}

fun Card.deleteTotal(index: Int): Boolean {
    return if (totals.size > 1) {
        totals.removeAt(index)
        true
    } else
        false
}

private fun getNewCell(column: Column): Cell = Cell().apply {
    val gson = Gson()
    sourceValue = when (column) {
        is ColorColumn -> {
            Color.WHITE.toString()
        }
        is ImageColumn -> {
            Gson().toJson(ImageTypeValue())
        }
        is SwitchColumn -> {
            false.toString()
        }
        is ListColumn ->
            ""
        is PhoneColumn ->
            gson.toJson(
                PhoneTypeValue()
            )

        is DateColumn -> {
            ""
        }
        is NumberColumn -> {
            ""
        }
        else -> {
            ""
        }
    }
    cellTypeControl = column.columnTypeControl
}

fun Card.addRow(
    row: Row = Row(pageId, refId).apply {
        cellList = mutableListOf<Cell>().apply {
            columns.forEach { column ->
                add(getNewCell(column))
            }
        }
    }
): Row {
    row.status = Row.Status.ADDED
    rows.add(row)
    updateTypeControlRow(rows.size - 1)
    calcTotals()
    return row
}

fun Card.addColumn(type: ColumnType, name: String, position: Int = columns.size): Column {
    val column = when (type) {
        ColumnType.NUMERATION -> NumerationColumn(name,pageId, refId)
        ColumnType.NUMBER -> NumberColumn(name,pageId, refId)
        ColumnType.PHONE -> PhoneColumn(name,pageId, refId)
        ColumnType.DATE -> DateColumn(name,pageId, refId)
        ColumnType.COLOR -> ColorColumn(name,pageId, refId)
        ColumnType.SWITCH -> SwitchColumn(name,pageId, refId)
        ColumnType.IMAGE -> ImageColumn(name,pageId, refId)
        ColumnType.LIST -> ListColumn(name,pageId, refId)
        ColumnType.TEXT -> TextColumn(name,pageId, refId)
        // не будет ни когда использоваться
        ColumnType.NONE -> TextColumn(name,pageId, refId)
    }

    columns.add(position, column)
    column.updateTypeControl(this)
    rows.forEach {
        it.cellList.add(position, getNewCell(column))
    }
    updateTypeControlColumn(column)
    calcTotals()
    return column

}

fun Card.calcTotals(){
    totals.forEach {
        it.calcFormula(this)
    }
}
fun Card.addColumnSample(type: ColumnType, name: String, position: Int = columns.size) {
    val column = when (type) {
        ColumnType.NUMERATION -> NumerationColumn(name,pageId, refId)
        ColumnType.NUMBER -> NumberColumn(name,pageId, refId)
        ColumnType.PHONE -> PhoneColumn(name,pageId, refId)
        ColumnType.DATE -> DateColumn(name,pageId, refId)
        ColumnType.COLOR -> ColorColumn(name,pageId, refId)
        ColumnType.SWITCH -> SwitchColumn(name,pageId, refId)
        ColumnType.IMAGE -> ImageColumn(name,pageId, refId)
        ColumnType.LIST -> ListColumn(name,pageId, refId)
        ColumnType.TEXT -> TextColumn(name,pageId, refId)
        // не будет ни когда использоваться
        ColumnType.NONE -> TextColumn(name,pageId, refId)
    }
    column.pageId = pageId
    column.cardId = refId

    columns.add(position, column)
    column.updateTypeControl(this)
    rows.forEach {
        it.cellList.add(position, getCellOfSample(position))
    }
    updateTypeControlColumn(column)
}

fun Card.deleteColumn(column: Column? = null): Boolean {
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
    calcTotals()
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

fun Card.findColumnAtId(idColumn: String): Column? {
    columns.forEach {
        if (it.refId == idColumn)
            return it
    }
    return null
}

fun Card.getCellOfSample(position: Int): Cell {
    val column = columns[position]
    return Cell().apply {
        cellTypeControl = column.columnTypeControl
        sourceValue = when (column) {
            is ImageColumn -> {
                Gson().toJson(ImageTypeValue().apply { imagePathList.add(getPathForResource(R.drawable.ic_sample_image).toString()) })
            }
            is SwitchColumn -> {
                val newVal = (0..20).random() > 10
                newVal.toString()
            }
            is ColorColumn -> {
                val r = (0..255).random()
                val g = (0..255).random()
                val b = (0..255).random()
                val rgb: Int = Color.rgb(r, g, b)
                rgb.toString()
            }
            is ListColumn -> {
                "Выбранный элемент"
            }
            is DateColumn -> {
                Date().time.toString()
            }
            is NumberColumn -> {
                "12345.987"
            }
            is PhoneColumn -> {
                Gson().toJson(PhoneTypeValue(phone = "7 999 123-45-67"))
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

fun Card.updateTypeControl() {
    columns.forEach { column ->
        updateTypeControlColumn(column)
    }
//        fillTotalAmount()
}

fun Card.addSampleRow() {
    val row = mutableListOf<Cell>()
    columns.forEachIndexed { index, _ ->
        row.add(getCellOfSample(index))
    }
    rows.add(Row(pageId, refId).apply { cellList = row })
}

fun Card.deleteRow(index: Int = rows.size - 1) {
    if (rows.isEmpty())
        return
    rows.removeAt(index)
    calcTotals()

}

fun Card.deleteRows(removedList: List<Row>) {
    if (removedList.isNotEmpty())
        rows.removeAll(removedList)
    calcTotals()
}

fun Card.updateTypeControlColumn(column: Column) {
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

fun Card.updateTypeControlRow(indexRow: Int) {
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

fun Card.unSelectCell(): Int {
    rows.forEachIndexed { rowIndex, row ->
        val cell = row.cellList.find { it.isSelect }
        if (cell != null) {
            cell.isSelect = false
            return rowIndex
        }
    }
    return -1
}

fun Card.unSelectRows() {
    rows.forEach {
        it.status = Row.Status.NONE
    }
}

fun Card.getSelectedCell(): Cell? {
    rows.forEach { row ->
        row.cellList.forEach { cell ->
            if (cell.isSelect) {
                return cell
            }
        }
    }
    return null
}

fun Card.deleteRow(row: Row) {
    rows.remove(row)
    calcTotals()
}

fun Card.getSelectedRows(): MutableList<Row> {
    val selRows = mutableListOf<Row>()
    rows.forEach {
        if (it.status == Row.Status.SELECT)
            selRows.add(it)
    }
    return selRows
}

