package ru.developer.press.myearningkot.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.developer.press.myearningkot.AdapterPageInterface
import ru.developer.press.myearningkot.database.*
import ru.developer.press.myearningkot.helpers.MyLiveData
import ru.developer.press.myearningkot.helpers.SingleLiveEvent
import ru.developer.press.myearningkot.helpers.liveData
import ru.developer.press.myearningkot.helpers.runOnMain
import ru.developer.press.myearningkot.helpers.scoups.*
import ru.developer.press.myearningkot.model.NumberColumn

// этот класс создается (ViewModelProviders.of(this).get(Class::class.java))
// и существует пока существует активити до уничтожения он только обновляет данные представления
class MainViewModel(context: Context, list: MutableList<Page>) : ViewModel(),
    AdapterPageInterface {
    companion object {
        var cardClick: (cardId: String) -> Unit = {}
    }

    private var openedCardId: String = ""
    private val pageList: MutableList<MyLiveData<Page>> = mutableListOf()
    private val dataController = DataController(context)

    init {
        list.forEach {
            pageList.add(liveData(it))
        }
        // нажали на карточку
        cardClick = { idCard ->
            openedCardId = idCard
            openCardEvent.call(idCard)
        }

    }

    val openCardEvent = SingleLiveEvent<String>()
    //

    // реализация для адаптера чтоб брал количество страниц
    override fun getPageCount(): Int {
        return pageList.size
    }

    override fun getPages(): MutableList<MyLiveData<Page>> {
        return pageList
    }

    fun getTabName(position: Int): String {
        return pageList[position].value!!.name
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

    fun createCard(
        indexPage: Int,
        sampleID: String,
        name: String,
        updateView: (position: Int) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            val pageLiveData = pageList[indexPage]
            val page = pageLiveData.value!!

            val card: Card = dataController.getSampleCard(sampleID)
            // для того что бы удвлить времянки
            card.rows.clear()
            // добавляем в базу данных новую Card присовение ид очень важно
            card.pageId = page.refId
            if (name.isNotEmpty())
                card.name = name
            card.isUpdating = true
            // добавляем в базу
            dataController.addCard(card)
            // узнать позицию для добавления во вкладку и для ее обновления во вью... а ее надо узнавать смотря какая сортировка
            val position = getPositionCardInPage(indexPage, card)
            // добавляем во вкладку
            page.cards.add(position, runOnMain { liveData(card) })
            runOnMain {
//                pageLiveData.value = page
                updateView(position)
            }
        }
    }

    fun addPage(pageName: String, mainBlock: (Page?) -> Unit): Boolean {
        val find: MyLiveData<Page>? = pageList.find { it.value!!.name == pageName }
        viewModelScope.launch {
            find?.let {
                mainBlock.invoke(null)
                false
            } ?: kotlin.run {
                val page: Page = dataController.addPage(pageName, pageList.size)
                pageList.add(runOnMain { liveData(page) })
                mainBlock.invoke(page)
            }
        }
        // если нулл значит такой вкладки нет и можно добавить
        return find == null
    }

//    fun updateCardInPage(idCard: Long, selectedTabPosition: Int): Int {
//        var position = 0
//        val cards = pageList[selectedTabPosition].value!!.cards
//        cards.forEachIndexed { index, card ->
//            if (card.value!!.id == idCard) {
//                viewModelScope.launch {
//                    val updatedCard = dataController.getCard(idCard)
//                    calcCard(updatedCard)
//                    cards[index].postValue(updatedCard)
//                    position = index
//                    return@launch
//                }
//            }
//        }
//        return position
//    }

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
        val liveData = pageList[selectedPage]
        val page: Page? = liveData.value
        page?.let {
            dataController.updatePage(it)
            liveData.postValue(it)
        }
    }

    fun checkUpdatedCard(selectedTabPosition: Int) {
        val page = pageList[selectedTabPosition]
        val find = page.value!!.cards.find { it.value!!.refId == openedCardId }
        find?.let {
            viewModelScope.launch(Dispatchers.IO) {
                val card = dataController.getCard(openedCardId)
                openedCardId = ""
                card.isUpdating = true
                it.postValue(card)
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

    fun loginSuccess() {
        viewModelScope.launch {
            dataController.syncRefs()
        }
    }

    fun changedPage(id: String, updateViewPager: () -> Unit) {
        synchronized(pageList) {
            viewModelScope.launch {
                val pageDB = dataController.getPage(id)
                val find = pageList.find { it.value!!.name == pageDB!!.name }
                if (find == null) {
                    pageList.add(liveData(pageDB))
                    pageList.sortBy { it.value?.position }
                    updateViewPager.invoke()
                } else {
                    // на всякий пожарный
                    find.value!!.refId = pageDB!!.refId
                    find.updateValue()
                }
            }
        }
    }

    fun deletePage(tabPosition: Int, updateView: (position: Int) -> Unit) {
        viewModelScope.launch {
            val pageLiveData = pageList[tabPosition]
            pageList.removeAt(tabPosition)
            dataController.deletePage(pageLiveData.value!!)
            runOnMain {
                updateView.invoke(tabPosition)
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