package ru.developer.press.myearningkot.helpers.scoups

import android.graphics.Color
import com.google.gson.Gson
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.database.CardRef
import ru.developer.press.myearningkot.helpers.getPathForResource
import ru.developer.press.myearningkot.model.*
import java.util.*


fun CardRef.addTotal(): Total {
    val totalItem = Total().apply {
        formula.formulaElements.add(Formula.FormulaElement(Formula.OTHER, "0"))
    }
    totals.add(totalItem)

    return totalItem
}

fun CardRef.deleteTotal(index: Int): Boolean {
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

fun CardRef.addRow(
    row: Row = Row().apply {
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
    return row
}

fun CardRef.addColumn(type: ColumnType, name: String, position: Int = columns.size): Column {
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
    columns.add(position, column)
    column.updateTypeControl(this)
    rows.forEach {
        it.cellList.add(position, getNewCell(column))
    }
    updateTypeControlColumn(column)
    return column

}

fun CardRef.addColumnSample(type: ColumnType, name: String, position: Int = columns.size) {
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
    columns.add(position, column)
    column.updateTypeControl(this)
    rows.forEach {
        it.cellList.add(position, getCellOfSample(position))
    }
    updateTypeControlColumn(column)
}

fun CardRef.deleteColumn(column: Column? = null): Boolean {
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

fun CardRef.findColumnAtId(idColumn: String): Column? {
    columns.forEach {
        if (it.refId == idColumn)
            return it
    }
    return null
}

fun CardRef.getCellOfSample(position: Int): Cell {
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

fun CardRef.updateTypeControl() {
    columns.forEach { column ->
        updateTypeControlColumn(column)
    }
//        fillTotalAmount()
}

fun CardRef.addSampleRow() {
    val row = mutableListOf<Cell>()
    columns.forEachIndexed { index, _ ->
        row.add(getCellOfSample(index))
    }
    rows.add(Row().apply { cellList = row })
}

fun CardRef.deleteRow(index: Int = rows.size - 1) {
    if (rows.isEmpty())
        return
    rows.removeAt(index)

}

fun CardRef.updateTypeControlColumn(column: Column) {
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

fun CardRef.updateTypeControlRow(indexRow: Int) {
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

fun CardRef.unSelectCell(): Int {
    rows.forEachIndexed { rowIndex, row ->
        val cell = row.cellList.find { it.isSelect }
        if (cell != null) {
            cell.isSelect = false
            return rowIndex
        }
    }
    return -1
}

fun CardRef.unSelectRows() {
    rows.forEach {
        it.status = Row.Status.NONE
    }
}

fun CardRef.getSelectedCell(): Cell? {
    rows.forEach { row ->
        row.cellList.forEach { cell ->
            if (cell.isSelect) {
                return cell
            }
        }
    }
    return null
}

fun CardRef.deleteRow(row: Row) {
    rows.remove(row)
}

fun CardRef.getSelectedRows(): MutableList<Row> {
    val selRows = mutableListOf<Row>()
    rows.forEach {
        if (it.status == Row.Status.SELECT)
            selRows.add(it)
    }
    return selRows
}

