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

    fun addCard(
        indexPage: Int,
        card: Card,
        updateView: (position: Int) -> Unit
    ) {
        // временная суета...
        repeat(100) {
            card.addRow()
        }
        // добавляем в базу данных новую Card присовение ид очень важно
        val page = pageList[indexPage]
        card.idPage = page.id
        // добавляем в базу
        DataController().addCard(card)
        // узнать позицию для добавления во вкладку и для ее обновления во вью... а ее надо узнавать смотря какая сортировка
        val position = getPositionCardInPage(indexPage, card)
        // добавляем во вкладку
        page.cards.add(position, card)
        updateView(position)

    }

    fun addPage(pageName: String): Page {
        val page: Page = DataController().addPage(pageName)
        pageList.add(page)

        return page
    }

    fun updateCardInPage(idCard: Long, selectedTabPosition: Int): Int {
        var position = 0
        val cards = pageList[selectedTabPosition].cards
        cards.forEachIndexed { index, card ->
            if (card.id == idCard) {
                cards[index] = DataController().getCard(idCard)
                position = index
                return@forEachIndexed
            }
        }
        return position
    }

    fun getCardInPage(selectedTabPosition: Int, position: Int): Card {
        return pageList[selectedTabPosition].cards[position]
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

    val titleLiveData = MutableLiveData<String>()
    val displayParam = DisplayParam()
    val cardLiveData = MutableLiveData<Card>()

    var columnLDList = mutableListOf<MutableLiveData<Column>>()

    override fun getColumns(): MutableList<Column> = card.columns
    override fun getWidth(): Int {
        return if (cardLiveData.value!!.enableHorizontalScroll)
            LinearLayout.LayoutParams.MATCH_PARENT
        else
            displayParam.width
    }

    override fun getSize(): Int = card.rows.size
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


    fun setEnableHorizontalScroll(isEnable: Boolean) {
        card.enableHorizontalScroll = isEnable
        cardLiveData.value = card
    }

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
        updateCardLD()
    }

    fun selectionColumn(columnIndex: Int, isSelect: Boolean) {
        card.rows.forEach { row ->
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

    fun moveToRight(selectedColumns: MutableList<Column>, result: (Boolean) -> Unit) {
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

            card.rows.forEach { row ->
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

    fun moveToLeft(selectedColumns: MutableList<Column>, result: (Boolean) -> Unit) {
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

            card.rows.forEach { row ->
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

    fun setDefaultPrefColumn(column: Column) {
        column.setDefaultPref()
        updateTypeControl()
        updateCardLD()
    }

    fun updateTypeControlColumn(column: Column) {
        card.updateTypeControlColumn(column)
    }

    fun updateColumnDL() {
        columnLDList.forEachIndexed { index, mutableLiveData ->
            mutableLiveData.value = card.columns[index]
        }
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