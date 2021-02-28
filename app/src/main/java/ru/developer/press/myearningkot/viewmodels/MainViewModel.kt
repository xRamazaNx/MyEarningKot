package ru.developer.press.myearningkot.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.developer.press.myearningkot.AdapterPageInterface
import ru.developer.press.myearningkot.helpers.Page
import ru.developer.press.myearningkot.helpers.SingleLiveEvent
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.model.DataController
import ru.developer.press.myearningkot.model.NumberColumn
import kotlin.concurrent.thread

// этот класс создается (ViewModelProviders.of(this).get(Class::class.java))
// и существует пока существует активити до уничтожения он только обновляет данные представления
class MainViewModel(context: Context, list: MutableList<Page>) : ViewModel(),
    AdapterPageInterface {
    private var openedCardId: Long = -1
    private val pageList: MutableList<MutableLiveData<Page>> = mutableListOf()

    init {
        list.forEach {
            pageList.add(MutableLiveData<Page>().apply { value = it })
        }
    }

    // нажали на карточку
    fun cardClick(idCard: Long) {
        openCardEvent.call(idCard)
        openedCardId = idCard
    }

    val openCardEvent = SingleLiveEvent<Long>()
    //

    // реализация для адаптера чтоб брал количество страниц
    override fun getPageCount(): Int {
        return pageList.size

    }

    override fun getPages(): MutableList<MutableLiveData<Page>> {
        return pageList
    }

    fun getTabName(position: Int): String {
        return pageList[position].value!!.pageName
    }

    private fun getPositionCardInPage(indexPage: Int, card: Card): Int {
        val cardsInPage = pageList[indexPage].value!!.cards
        // тут будет логика определения позиции с учетом сортировки
//        cardsInPage.forEachIndexed { index, cardTemp ->
//            if (card === cardTemp) {
//                return index
//            }
//        }
        return cardsInPage.size
    }

    private val dataController = DataController(context)

    fun createCard(
        indexPage: Int,
        sampleID: Long,
        name: String,
        updateView: (position: Int) -> Unit
    ) {
        thread {
            val card: Card = dataController.getSampleCard(sampleID)!!
            // для того что бы удвлить времянки
            card.rows.clear()
            // добавляем в базу данных новую Card присовение ид очень важно
            val mutableLiveData = pageList[indexPage]
            val page = mutableLiveData.value!!
            card.idPage = page.id
            card.name = name
            // добавляем в базу
            dataController.addCard(card)
            // узнать позицию для добавления во вкладку и для ее обновления во вью... а ее надо узнавать смотря какая сортировка
            val position = getPositionCardInPage(indexPage, card)
            // добавляем во вкладку
            page.cards.add(position, MutableLiveData<Card>().apply { postValue(card) })
            mutableLiveData.postValue(page)
            updateView(position)
        }

    }

    fun addPage(pageName: String): Page {
        val page: Page = dataController.addPage(pageName)
        pageList.add(MutableLiveData<Page>().apply { value = page })
        return page
    }

    fun updateCardInPage(idCard: Long, selectedTabPosition: Int): Int {
        var position = 0
        val cards = pageList[selectedTabPosition].value!!.cards
        cards.forEachIndexed { index, card ->
            if (card.value!!.id == idCard) {
                val updatedCard = dataController.getCard(idCard)
                calcCard(updatedCard)
                cards[index].postValue(updatedCard)
                position = index
                return@forEachIndexed
            }
        }
        return position
    }

    fun getCardInPage(selectedTabPosition: Int, position: Int): Card {
        return pageList[selectedTabPosition].value!!.cards[position].value!!
    }

    private fun calcCard(card: Card) {
        card.apply {
            columns.filterIsInstance<NumberColumn>().forEach { column ->
                updateTypeControlColumn(column)
            }
        }
    }

    fun calcAllCards() {
        pageList.forEach { page ->
            page.value!!.cards.forEach { card ->
                card.value?.let {
                    calcCard(it)
                }
            }
        }
    }

    fun pageColorChanged(color: Int, selectedPage: Int) {
        val mutableLiveData = pageList[selectedPage]
        val page: Page? = mutableLiveData.value
        dataController.updatePage(page!!)
        page.background = color
        mutableLiveData.postValue(page)

    }

    fun checkUpdatedCard() {
        pageList.forEach { liveData ->
            val find = liveData.value?.cards?.find { it.value?.id == openedCardId }
            find?.let {
                thread {
                    val card = dataController.getCard(openedCardId)
                    card.isUpdating = true
                    it.postValue(card)
                    openedCardId = -1
                }
                return@forEach
            }
        }
//
//        pageList.forEach { liveData ->
//            liveData.value?.cards?.forEach {
//                val value = it.value
//                if (value?.id == openedCardId) {
//                    value.isUpdating = true
//                    it.postValue(value)
//                }
//                return
//            }
//        }
    }

//    fun deletePage(position: Int, deleteEvent: (Boolean) -> Unit) {
//        val isEmpty = dataController.pageCount == 0
//        if (!isEmpty) {
//            dataController.pageList.removeAt(position)
//        }
//        deleteEvent(!isEmpty)
//    }

}