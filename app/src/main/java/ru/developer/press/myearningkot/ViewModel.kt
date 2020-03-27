@file:Suppress("UNCHECKED_CAST")

package ru.developer.press.myearningkot

import android.widget.LinearLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.developer.press.myearningkot.model.*
import ru.developer.press.myearningkot.otherHelpers.Page
import ru.developer.press.myearningkot.otherHelpers.SingleLiveEvent


// этот класс создается (ViewModelProviders.of(this).get(Class::class.java))
// и существует пока существует активити до уничтожения он только обновляет данные представления
class PageViewModel(private val pageList: MutableList<Page> = mutableListOf()) : ViewModel(),
    AdapterPageInterface,
    CardClickListener {

    // нажали на карточку
    override fun cardClick(idCard: Long) {
        openCardEvent.call(idCard)
    }

    val openCardEvent = SingleLiveEvent<Long>()
    //

    // реализация для адаптера чтоб брал количество страниц
    override fun getPageCount(): Int {
        return pageList.size

    }

    override fun getPages(): MutableList<Page> {
        return pageList
    }

    fun getTabName(position: Int): String {
        return pageList[position].pageName
    }

    private fun getPositionCardInPage(indexPage: Int, card: Card): Int {
        val cardsInPage = pageList[indexPage].cards
        // тут будет логика определения позиции с учетом сортировки
//        cardsInPage.forEachIndexed { index, cardTemp ->
//            if (card === cardTemp) {
//                return index
//            }
//        }
        return cardsInPage.size
    }

    private val dataController = DataController()

    fun addCard(
        indexPage: Int,
        card: Card,
        updateView: (position: Int) -> Unit
    ) {

        // для того что бы удвлить времянки
        card.rows.clear()
        // добавляем в базу данных новую Card присовение ид очень важно
        val page = pageList[indexPage]
        card.idPage = page.id
        // добавляем в базу
        dataController.addCard(card)
        // узнать позицию для добавления во вкладку и для ее обновления во вью... а ее надо узнавать смотря какая сортировка
        val position = getPositionCardInPage(indexPage, card)
        // добавляем во вкладку
        page.cards.add(position, card)
        updateView(position)

    }

    fun addPage(pageName: String): Page {
        val page: Page = dataController.addPage(pageName)
        pageList.add(page)

        return page
    }

    fun updateCardInPage(idCard: Long, selectedTabPosition: Int): Int {
        var position = 0
        val cards = pageList[selectedTabPosition].cards
        cards.forEachIndexed { index, card ->
            if (card.id == idCard) {
                cards[index] = dataController.getCard(idCard)
                position = index
                return@forEachIndexed
            }
        }
        return position
    }

    fun getCardInPage(selectedTabPosition: Int, position: Int): Card {
        return pageList[selectedTabPosition].cards[position]
    }

    fun calcAllCards() {
        pageList.forEach { page ->
            page.cards.forEach { card ->
                card.columns.filterIsInstance<NumberColumn>().forEach { column ->
                    card.updateTypeControlColumn(column)
                }
            }
        }
    }

//    fun deletePage(position: Int, deleteEvent: (Boolean) -> Unit) {
//        val isEmpty = dataController.pageCount == 0
//        if (!isEmpty) {
//            dataController.pageList.removeAt(position)
//        }
//        deleteEvent(!isEmpty)
//    }

}

//
//
//
//
// для отображения открытой карточки
open class CardViewModel(var card: Card) : ViewModel(), ProvideDataRows {

    var cellSelectPosition: Int = -1
    var rowSelectPosition: Int = -1
    var selectMode = MutableLiveData<SelectMode>().apply {
        value = SelectMode.NONE
    }
    val titleLiveData = MutableLiveData<String>()
    val displayParam = DisplayParam()
    val cardLiveData = MutableLiveData<Card>()
    var copyRowList: MutableList<Row>? = null
    private val sortedList = mutableListOf<Row>()

    var columnLDList = mutableListOf<MutableLiveData<Column>>()

    override val rows: List<Row>
        get() = sortedList

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

    fun updateCard(card: Card = this.card) {
        this.card = card
        updateCardLD()
    }

    fun addColumn(columnType: ColumnType, name: String) {
        card.addColumn(columnType, name)
        updateTypeControl()
        updateCardLD()
    }


    init {
        sortedList()
        updateColumnDL()
        updateCardLD()
    }

    fun selectionColumn(columnIndex: Int, isSelect: Boolean) {
        sortedList().forEach { row ->
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

            sortedList().forEach { row ->
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

            sortedList().forEach { row ->
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

    private val dataController = DataController()

    fun addRow(): Row {
        return card.addRow().apply {
            sortedList()
            dataController.updateCard(card)
        }
    }

    fun sortedList(): MutableList<Row> {
        //#postedit
        sortedList.clear()
        sortedList.addAll(card.rows)

        return sortedList
    }

    fun cellClicked(
        rowPosition: Int,
        cellPosition: Int,
        function: (Int, Boolean) -> Unit
    ) {

        this.rowSelectPosition = rowPosition
        this.cellSelectPosition = cellPosition

        val cell = sortedList()[rowPosition].cellList[cellPosition]
        val isDoubleTap = cell.isSelect
        val oldSelectPosition: Int = card.unSelectCell()
        cell.isSelect = true

        // присваиваем cell только если не было выделено
        selectMode.value?.let {
            if (it == SelectMode.ROW) {
                card.unSelectRows()
            }
        }
        selectMode.value = SelectMode.CELL
        function(if (oldSelectPosition > -1) oldSelectPosition else rowPosition, isDoubleTap)
    }

    fun rowClicked(rowPosition: Int = card.rows.size - 1, function: (Int) -> Unit) {
        rowSelectPosition = rowPosition
        val row = sortedList()[rowPosition]
        val oldStatus = row.status
        row.status = if (oldStatus == Row.Status.SELECT) Row.Status.NONE else Row.Status.SELECT

        // присваиваем cell только если не было выделено
        selectMode.value?.let {
            if (it != SelectMode.ROW) {
                if (it == SelectMode.CELL) {
                    card.unSelectCell()
                }
            }
            selectMode.value = SelectMode.ROW
        }
        function(rowPosition)
    }

    fun unSelect() {
        card.unSelectCell()
        card.unSelectRows()
        selectMode.value = SelectMode.NONE
    }

    fun updateTypeControlColumn(columnPosition: Int) {
        updateTypeControlColumn(card.columns[columnPosition])
    }

    fun copySelectedCell(isCut: Boolean) {
        card.rows.forEach {
            it.cellList.forEachIndexed { index, cell ->
                if (cell.isSelect) {
                    App.instance?.copyCell = cell
                    if (isCut) {
                        cell.clear()
                        updateTypeControlColumn(index)
                        updatePlateChanged()
                    }
                    // заного назначаю чтоб меню создалось заного и иконка вставки если надо станет серой или белой
                    selectMode.value = SelectMode.CELL
                    return
                }
            }
        }
    }

    fun pasteCell() {
        card.rows.forEach { row ->
            row.cellList.forEachIndexed { index, cell ->
                if (cell.isSelect) {
                    App.instance?.copyCell?.let {
                        cell.sourceValue = it.sourceValue
                        updateTypeControlColumn(index)
                        updatePlateChanged()
                    }
                    return
                }
            }
        }
    }

    fun getSelectedCellType(): ColumnType? {
        return card.getSelectedCell()?.type
    }

    fun deleteRows(updateView: () -> Unit) {
        val selectedRows = card.getSelectedRows()
        selectedRows.forEach {
            it.status = Row.Status.DELETED
        }
        updateView()
        card.rows.removeAll(selectedRows)
        dataController.updateCard(card)
    }

    fun copySelectedRows(isCut: Boolean) {
        copyRowList = mutableListOf()
        card.getSelectedRows().forEach {
            copyRowList!!.add(it.copy())
        }
        if (isCut)
            deleteRows {

            }
    }

    fun pasteRows() {
        val selectedRowList = card.getSelectedRows()
        // самый нижний элемент чтобы вставить туда
        val indexLastRow = card.rows.indexOf(selectedRowList[selectedRowList.size - 1])

        copyRowList?.let { copyList ->

            val copyFirstRow = copyList[0]
            val currentFirstRow = selectedRowList[0]

            val copyColumnSize = copyFirstRow.cellList.size
            val currentColumnSize = currentFirstRow.cellList.size

            // равно ли колличесвто колон в копированном и настоящем положении у строк
            if (copyColumnSize == currentColumnSize) {
                var equalColumns = true
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
                if (equalColumns) {
                    selectedRowList.forEach { it.status = Row.Status.NONE }

                    card.rows.addAll(indexLastRow, copyList.apply {
                        val list = mutableListOf<Row>()
                        forEach {
                            list.add(it.copy())
                        }
                        clear()
                        addAll(list)
                    })
                    updateTypeControl()
                    copyRowList = null
                    updatePlateChanged()
                }
            }
        }
    }

    fun duplicateRows() {
        copySelectedRows(false)
        unSelect()
        rowClicked {}
//        card.rows.last().isSelect = true
        pasteRows()

    }

    enum class SelectMode {
        CELL, ROW, NONE
    }
}

class ViewModelMainFactory(private val pageList: MutableList<Page>) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PageViewModel(pageList) as T
    }
}

class ViewModelCardFactory(private val card: Card) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CardViewModel(card) as T
    }
}
//
//
//
//
