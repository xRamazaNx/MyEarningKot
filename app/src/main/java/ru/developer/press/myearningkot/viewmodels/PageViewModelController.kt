package ru.developer.press.myearningkot.viewmodels

import androidx.lifecycle.ViewModel
import ru.developer.press.myearningkot.AdapterPageInterface
import ru.developer.press.myearningkot.CardClickListener
import ru.developer.press.myearningkot.helpers.Page
import ru.developer.press.myearningkot.helpers.SingleLiveEvent
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.model.DataController
import ru.developer.press.myearningkot.model.NumberColumn

// этот класс создается (ViewModelProviders.of(this).get(Class::class.java))
// и существует пока существует активити до уничтожения он только обновляет данные представления
class PageViewModelController(private val pageList: MutableList<Page> = mutableListOf()) : ViewModel(),
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