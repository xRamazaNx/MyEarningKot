@file:Suppress("UNCHECKED_CAST")

package ru.developer.press.myearningkot.viewmodels

import android.content.Context
import android.widget.LinearLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.developer.press.myearningkot.ProvideDataRows
import ru.developer.press.myearningkot.adapters.AdapterRow.Companion.animatedDuration
import ru.developer.press.myearningkot.database.Card
import ru.developer.press.myearningkot.database.DataController
import ru.developer.press.myearningkot.database.Page
import ru.developer.press.myearningkot.helpers.MyLiveData
import ru.developer.press.myearningkot.helpers.liveData
import ru.developer.press.myearningkot.helpers.runOnIO
import ru.developer.press.myearningkot.helpers.runOnMain
import ru.developer.press.myearningkot.helpers.scoups.*
import ru.developer.press.myearningkot.model.*

open class CardViewModel(context: Context, var card: Card) : ViewModel(),
    ProvideDataRows {

    // статус занесения изменений карточки в базу данных
    val updatedCardStatus: MyLiveData<Boolean> = liveData(false)
    var cellSelectPosition: Int = -1
    var rowSelectPosition: Int = -1
    var selectMode = liveData(SelectMode.NONE)
    val titleLiveData = liveData(card.name)
    val displayParam = DisplayParam()
    val cardLiveData = liveData(card)
    val totalLiveData = liveData(card)
    var copyRowList: MutableList<Row>? = null

    var columnLDList = mutableListOf<MyLiveData<Column>>()

    override val sortedRows: MutableList<Row> = mutableListOf()

    init {
        sortList()
        updateColumnDL()
        updateCardLD()
    }

    override fun getColumns(): MutableList<Column> = card.columns
    override fun getWidth(): Int {
        return if (cardLiveData.value!!.enableHorizontalScroll)
            LinearLayout.LayoutParams.MATCH_PARENT
        else
            displayParam.width
    }

    private fun updateCardLD() {
        cardLiveData.postValue(card)
        titleLiveData.postValue(card.name)

        columnLDList.clear()
        card.apply {
            columns.forEach {
                columnLDList.add(liveData(it))
            }
        }
    }

    fun updatePlateChanged() {
        cardLiveData.postValue(card)
    }

    fun updateTotals() {
        totalLiveData.postValue(card)
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
                if (cell.isSelect) {
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
        updateCardLD()
    }

    fun addColumnSample(columnType: ColumnType, name: String) {
        card.addColumnSample(columnType, name)
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
    fun moveToRightTotal(selectedTotals: List<Total>, result: (Boolean) -> Unit) {
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
    fun moveToLeftTotal(selectedTotals: List<Total>, result: (Boolean) -> Unit) {
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

    fun deleteTotal(it: Total): Boolean {
        return card.deleteTotal(card.totals.indexOf(it))

    }

    private val dataController = DataController(context)

    fun addRow(end: () -> Unit) {
        viewModelScope.launch {
            val addRow = card.addRow()
            dataController.addRow(addRow)
            sortList()
            runOnMain { end.invoke() }
            delay(animatedDuration)
            addRow.status = Row.Status.NONE
            addRow.elementView.animation = null
            card.calcTotals()
            runOnMain { end.invoke() }
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
        viewModelScope.launch {
            if (isEqualTypeCellAndCopyCell(copyCell))
                card.rows.forEachIndexed { indexRow, row ->
                    row.cellList.forEachIndexed { indexCell, cell ->
                        if (cell.isSelect) {
                            copyCell?.let {
                                cell.sourceValue = it.sourceValue
                                updateRowToDB(row)
                                updateTypeControlColumn(indexCell)//
                                updateTotals()
                            }

                            withContext(Dispatchers.Main) {
                                updateRow(indexRow)
                            }
                            return@launch
                        }
                    }
                }
        }
    }

    private suspend fun updateRowToDB(row: Row) {
        runOnIO {
            updatedCardStatus.postValue(true)
            card.calcTotals()
            dataController.updateRow(row)
            updatedCardStatus.postValue(false)
        }
    }

    private fun getSelectedCellType(): ColumnType? {
        return card.getSelectedCell()?.type
    }

    fun deleteRows(updateView: (position: Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val deletedRows = sortedRows.filter { it.status == Row.Status.SELECT }
            deletedRows.forEach {
                it.status = Row.Status.DELETED
                runOnMain {
                    updateView(
                        sortedRows.indexOf(it)
                    )
                }
            }
            delay(animatedDuration)
            card.deleteRows(deletedRows)
            dataController.deleteRows(deletedRows)
            sortList()
            selectMode.postValue(SelectMode.NONE)
        }
        // сортировка листа и обновлении происходит после анимации удаления
    }

    fun copySelectedRows() {
        if (copyRowList == null)
            copyRowList = mutableListOf()
        copyRowList?.clear()
        copyRowList?.addAll(card.getSelectedRows())
    }

    fun pasteRows() {
        viewModelScope.launch {
            // самый нижний элемент чтобы вставить туда
            val indexLastRow = sortedRows.indexOfLast { it.status == Row.Status.SELECT }
            copyRowList?.let { copyList ->

                if (isCapabilityPaste()) {
                    // выделенные строки ниже которых надо добавить
                    card.getSelectedRows().forEach { it.status = Row.Status.NONE }
                    // отдельный лист чтоб копировать элементы а не ссылки на них потому что в копилист бывают ссылки
                    val list = copyList.fold(mutableListOf<Row>()) { mutableList, row ->
                        mutableList.apply {
                            add(row.copy().also { it.status = Row.Status.ADDED })
                        }
                    }
                    card.rows.addAll(indexLastRow + 1, list)
                    card.calcTotals()
                    list.forEach {
                        dataController.addRow(it)
                    }
                    updateTypeControl()
                    sortList()
                    updateTotals()

                }

            }
        }
    }

    fun isCapabilityPaste(): Boolean {
        var capability = false
        copyRowList?.let { copyList ->
            val copyFirstRow = copyList[0]
            val currentFirstRow = card.getSelectedRows()[0]

            val copyColumnSize = copyFirstRow.cellList.size
            val currentColumnSize = currentFirstRow.cellList.size

            // равно ли колличесвто колон в копированном и настоящем положении у строк
            if (copyColumnSize == currentColumnSize) {
                capability = true
                // проходим по первым строкам у копированного и настоящего выделенного
                currentFirstRow.cellList.forEachIndexed { index, cell ->
                    // ячейка из скопированной строки
                    val copyCell = copyFirstRow.cellList[index]
                    // равняется ли тип скопированного с настоящим
                    if (cell.type != copyCell.type) {
                        // если хоть один не совпадает то атас
                        capability = false
                        return@forEachIndexed
                    }
                }
            }
        }

        return capability
    }

    fun duplicateRows() {
        copySelectedRows()
        val rows = card.rows
        rows.forEach {
            it.status = Row.Status.NONE
        }
        sortList()
        sortedRows.last().status = Row.Status.SELECT
        pasteRows()
    }

    fun updateEditCellRow() {
        viewModelScope.launch {
            val row = sortedRows[rowSelectPosition]
            updateRowToDB(row)
            updateTotals()
        }
    }

    enum class SelectMode {
        CELL, ROW, NONE
    }
}

class ViewModelMainFactory(
    private val context: Context,
    private val pageList: MutableList<Page>
) :
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
