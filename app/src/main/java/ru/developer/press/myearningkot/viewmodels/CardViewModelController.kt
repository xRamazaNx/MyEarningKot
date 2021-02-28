@file:Suppress("UNCHECKED_CAST")

package ru.developer.press.myearningkot.viewmodels

import android.content.Context
import android.widget.LinearLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.jetbrains.anko.collections.forEachByIndex
import ru.developer.press.myearningkot.ProvideDataRows
import ru.developer.press.myearningkot.helpers.Page
import ru.developer.press.myearningkot.model.*


//
//
//
//
// для отображения открытой карточки
open class CardViewModel(context: Context, var card: Card) : ViewModel(),
    ProvideDataRows {

    var cellSelectPosition: Int = -1
    var rowSelectPosition: Int = -1
    var selectMode = MutableLiveData<SelectMode>().apply {
        value =
            SelectMode.NONE
    }
    val titleLiveData = MutableLiveData<String>()
    val displayParam = DisplayParam()
    val cardLiveData = MutableLiveData<Card>()
    val totalLiveData = MutableLiveData<Card>()
    var copyRowList: MutableList<Row>? = null

    var columnLDList = mutableListOf<MutableLiveData<Column>>()

    override val sortedRows: MutableList<Row> = mutableListOf()

    override fun getColumns(): MutableList<Column> = card.columns
    override fun getWidth(): Int {
        return if (cardLiveData.value!!.enableHorizontalScroll)
            LinearLayout.LayoutParams.MATCH_PARENT
        else
            displayParam.width
    }

    private fun updateCardLD() {
        cardLiveData.value = card
        titleLiveData.value = card.name

        columnLDList.clear()
        card.apply {
            columns.forEach {
                columnLDList.add(MutableLiveData<Column>().apply { value = it })
            }

        }
    }

    fun updatePlateChanged() {
        cardLiveData.value = card
    }

    fun updateTotals() {
        totalLiveData.value = card
    }

    private fun updateTypeControl() {
        card.updateTypeControl()
    }

    override fun isEnableHorizontalScroll(): Boolean {
        return cardLiveData.value!!.enableHorizontalScroll
    }

    override fun isEnableSomeStroke(): Boolean {
        return card.enableSomeStroke
    }

    override fun getRowHeight(): Int = card.heightCells
    override fun getSelectCellPairIndexes(): Pair<Int, Int>? {
        var pair: Pair<Int, Int>?
        sortedRows.forEachIndexed { indexRow, row ->
            row.cellList.forEachIndexed { indexCell, cell ->
                if (cell.isSelect){
                    pair = Pair(indexRow, indexCell)
                    return pair
                }
            }
        }
        return null
    }

    fun updateCard(card: Card = this.card) {
        this.card = card
        sortList()
        updateCardLD()
    }

    fun addColumn(columnType: ColumnType, name: String) {
        card.addColumn(columnType, name)
        updateTypeControl()
        updateCardLD()
    }

    fun addColumnSample(columnType: ColumnType, name: String) {
        card.addColumnSample(columnType, name)
//        updateTypeControl()
        updateCardLD()
    }


    init {
        sortList()
        updateColumnDL()
        updateCardLD()
    }

    fun selectionColumn(columnIndex: Int, isSelect: Boolean) {
        sortList().forEach { row ->
            row.cellList.forEachIndexed { cellIndex, cell ->
                if (columnIndex == cellIndex)
                    cell.isPrefColumnSelect = isSelect
            }
        }
    }

    fun deleteColumn(column: Column): Boolean {
        val deleteResult = card.deleteColumn(column)
        if (deleteResult) {
            updateCardLD()
            updateTypeControl()
        }

        return deleteResult
    }

    // prefFun
    fun moveToRightTotal(selectedTotals: List<TotalItem>, result: (Boolean) -> Unit) {
        val totals = card.totals
        selectedTotals.forEach {
            val index = totals.indexOf(it)
            if (index > totals.size - 2) {
                result(false)
                return
            }
        }

        val indexSortedList = mutableListOf<Int>()
        selectedTotals.forEach {
            indexSortedList.add(totals.indexOf(it))
        }
        indexSortedList.sortDescending()

        indexSortedList.forEach {
            val indexOfTotal = it
            val total = totals[it]

            val totalRight = totals[indexOfTotal + 1]
            totals[indexOfTotal + 1] = total
            totals[indexOfTotal] = totalRight
        }

        updatePlateChanged()
        result(true)

    }

    fun moveToRightColumn(selectedColumns: MutableList<Column>, result: (Boolean) -> Unit) {
        val columns = card.columns
        selectedColumns.forEach {

            val index = columns.indexOf(it)
            if (index == 0 || index > columns.size - 2) {
                result(false)
                return
            }
        }
        val indexSortedList = mutableListOf<Int>()

        selectedColumns.forEach {
            indexSortedList.add(columns.indexOf(it))
        }
        indexSortedList.sortDescending()

        indexSortedList.forEach {
            val indexOfColumn = it
            val column = columns[it]

            val columnRight = columns[indexOfColumn + 1]
            columns[indexOfColumn + 1] = column
            columns[indexOfColumn] = columnRight

            sortList().forEach { row ->
                val cell = row.cellList[indexOfColumn]
                val cellRight = row.cellList[indexOfColumn + 1]

                row.cellList[indexOfColumn + 1] = cell
                row.cellList[indexOfColumn] = cellRight
            }
        }

        updateTypeControl()
        updateCardLD()
        result(true)
    }

    // prefFun
    fun moveToLeftTotal(selectedTotals: List<TotalItem>, result: (Boolean) -> Unit) {
        val totals = card.totals

        selectedTotals.forEach {
            val index = totals.indexOf(it)
            if (index < 1) {
                result(false)
                return
            }
        }
        val indexSortedList = mutableListOf<Int>()

        selectedTotals.forEach {
            indexSortedList.add(totals.indexOf(it))
        }
        indexSortedList.sort()

        indexSortedList.forEach {
            val indexOfTotal = it

            val total = totals[it]
            val totalLeft = totals[indexOfTotal - 1]
            totals[indexOfTotal - 1] = total
            totals[indexOfTotal] = totalLeft
        }

        updatePlateChanged()
        result(true)

    }

    fun moveToLeftColumn(selectedColumns: MutableList<Column>, result: (Boolean) -> Unit) {
        val columns = card.columns

        selectedColumns.forEach {
            val index = columns.indexOf(it)
            if (index < 2) {
                result(false)
                return
            }
        }
        val indexSortedList = mutableListOf<Int>()

        selectedColumns.forEach {
            indexSortedList.add(columns.indexOf(it))
        }
        indexSortedList.sort()

        indexSortedList.forEach {
            val indexOfColumn = it

            val column = columns[it]
            val columnLeft = columns[indexOfColumn - 1]
            columns[indexOfColumn - 1] = column
            columns[indexOfColumn] = columnLeft

            sortList().forEach { row ->
                val cell = row.cellList[indexOfColumn]
                val cellLeft = row.cellList[indexOfColumn - 1]

                row.cellList[indexOfColumn - 1] = cell
                row.cellList[indexOfColumn] = cellLeft
            }
        }

        updateTypeControl()
        updateCardLD()
        result(true)

    }

    fun updateTypeControlColumn(column: Column) {
        card.updateTypeControlColumn(column)
    }

    // тут не создается а обновляется
    fun updateColumnDL() {
        columnLDList.forEachIndexed { index, mutableLiveData ->
            mutableLiveData.value = card.columns[index]
        }
    }

    fun addTotal() {
        card.addTotal()
    }

    fun deleteTotal(it: TotalItem): Boolean {
        return card.deleteTotal(card.totals.indexOf(it))

    }

    private val dataController = DataController(context)

    fun addRow(): Row {
        return card.addRow().apply {
            sortList()
            dataController.updateCard(card)
        }
    }

    fun sortList(): MutableList<Row> {
        //#postedit
        sortedRows.clear()
        sortedRows.addAll(card.rows)

        return this.sortedRows
    }

    fun cellClicked(
        rowPosition: Int,
        cellPosition: Int,
        function: (Boolean) -> Unit
    ) {

        this.rowSelectPosition = rowPosition
        this.cellSelectPosition = cellPosition

        val cell = sortList()[rowPosition].cellList[cellPosition]
        val isDoubleTap = cell.isSelect
        cell.isSelect = true

        // присваиваем cell только если не было выделено
        selectMode.value?.let {
            if (it == SelectMode.ROW) {
                card.unSelectRows()
            }
        }
        selectMode.value =
            SelectMode.CELL
        function(isDoubleTap)
    }

    fun rowClicked(rowPosition: Int = card.rows.size - 1, function: (Int) -> Unit) {
        rowSelectPosition = rowPosition
        val row = this.sortedRows[rowPosition]
        val oldStatus = row.status
        row.status = if (oldStatus == Row.Status.SELECT) Row.Status.NONE else Row.Status.SELECT

        // присваиваем cell только если не было выделено
        selectMode.value?.let {
            if (it != SelectMode.ROW) {
                if (it == SelectMode.CELL) {
                    card.unSelectCell()
                }
            }
            if (card.getSelectedRows().isEmpty())
                selectMode.value =
                    SelectMode.NONE
            else
                selectMode.value =
                    SelectMode.ROW
        }
        function(rowPosition)
    }

    fun unSelect() {
        card.unSelectCell()
        card.unSelectRows()
        selectMode.value =
            SelectMode.NONE
    }

    fun updateTypeControlColumn(columnPosition: Int) {
        updateTypeControlColumn(card.columns[columnPosition])
    }

    fun copySelectedCell(isCut: Boolean): Cell? {
        card.rows.forEach {
            it.cellList.forEachIndexed { index, cell ->
                if (cell.isSelect) {
                    if (isCut) {
                        cell.clear()
                        updateTypeControlColumn(index)
                        updateTotals()
                    }
                    // заного назначаю чтоб меню создалось заного и иконка вставки если надо станет серой или белой
                    selectMode.value =
                        SelectMode.CELL
                    return cell
                }
            }
        }
        return null
    }

    fun isEqualTypeCellAndCopyCell(copyCell: Cell?): Boolean {
        val selectedCellType = getSelectedCellType()
        var eq = false
        copyCell?.let {
            eq = it.type == selectedCellType
        }
        return eq
    }

    fun pasteCell(copyCell: Cell?, updateRow: (Int) -> Unit) {
        if (isEqualTypeCellAndCopyCell(copyCell))
            card.rows.forEachIndexed { indexRow, row ->
                row.cellList.forEachIndexed { indexCell, cell ->
                    if (cell.isSelect) {
                        copyCell?.let {
                            cell.sourceValue = it.sourceValue
                            updateTypeControlColumn(indexCell)//
                            updateTotals()
                        }
                        updateRow(indexRow)
                        return
                    }
                }
            }
    }

    fun getSelectedCellType(): ColumnType? {
        return card.getSelectedCell()?.type
    }

    fun deleteRows(updateView: (Int) -> Unit) {
        var delIndex = 0
        sortedRows.forEach { r ->
            if (r.status == Row.Status.DELETED) {
                r.status = Row.Status.NONE
                updateView(delIndex)
                delIndex--
                card.rows.remove(r)
            }
            delIndex++
        }
        sortList()
        dataController.updateCard(card)
        // сортировка листа и обновлении происходит после анимации удаления
    }

    fun copySelectedRows() {
        copyRowList = mutableListOf()
        card.getSelectedRows().forEach {
            copyRowList!!.add(it.copy())
        }
    }

    fun pasteRows() {
        val selectedRowList = card.getSelectedRows()
        // самый нижний элемент чтобы вставить туда
        val i = selectedRowList.size - 1
        val element = selectedRowList[i]
        val indexLastRow = card.rows.indexOf(element)

        copyRowList?.let { copyList ->


            if (isCapabilityPaste()) {
                // выделенные строки ниже которых надо добавить
                selectedRowList.forEach { it.status = Row.Status.NONE }

                // отдельный лист чтоб копировать элементы а не ссылки на них потому что в копилист бывают ссылки
                val list = mutableListOf<Row>()
                // добавляем в лист копии методом .копи
                copyList.forEach {
                    list.add(it.copy().apply {
                        status = Row.Status.ADDED
                    }) //  копируемый ров
                }
                card.rows.addAll(indexLastRow + 1, list)
                updateTypeControl()
                sortList()
                updateTotals()

            }

        }
    }

    fun isCapabilityPaste(): Boolean {
        var equalColumns = false
        copyRowList?.let { copyList ->
            val copyFirstRow = copyList[0]
            val currentFirstRow = card.getSelectedRows()[0]

            val copyColumnSize = copyFirstRow.cellList.size
            val currentColumnSize = currentFirstRow.cellList.size

            // равно ли колличесвто колон в копированном и настоящем положении у строк
            if (copyColumnSize == currentColumnSize) {
                equalColumns = true
                // проходим по первым строкам у копированного и настоящего выделенного
                currentFirstRow.cellList.forEachIndexed { index, cell ->
                    // ячейка из скопированной строки
                    val copyCell = copyFirstRow.cellList[index]
                    // равняется ли тип скопированного с настоящим
                    if (cell.type != copyCell.type) {
                        // если хоть один не совпадает то атас
                        equalColumns = false
                        return@forEachIndexed
                    }
                }
            }
        }

        return equalColumns
    }

    fun duplicateRows() {
        copySelectedRows()
        val rows = card.rows
        rows.forEach {
            it.status = Row.Status.NONE
        }
        val lastRow = rows[rows.size - 1]
        lastRow.status = Row.Status.SELECT
        pasteRows()
    }

    fun updateCardIntoDB() = dataController.updateCard(card)

    enum class SelectMode {
        CELL, ROW, NONE
    }
}

class ViewModelMainFactory(private val context: Context, private val pageList: MutableList<Page>) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(context, pageList) as T
    }
}

class ViewModelCardFactory(private val context: Context, private val card: Card) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CardViewModel(context, card) as T
    }
}
//
//
//
//
