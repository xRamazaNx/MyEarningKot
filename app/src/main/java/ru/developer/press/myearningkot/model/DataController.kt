package ru.developer.press.myearningkot.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult
import ru.developer.press.myearningkot.helpers.*

class DataController(context: Context) {

    private val cardJsonDao: CardJsonDao
    private val pageDao: PageDao
    private val listTypeDao: ListTypeDao
    private val sampleHelper: SampleHelper

    init {
        val database = Database.create(context)
        cardJsonDao = database.cardJsonDao()
        pageDao = database.pageDao()
        listTypeDao = database.listTypeDao()
        sampleHelper = SampleHelper(context)
    }

    fun addCard(card: Card) {
        val json = Gson().toJson(card)
        val cardJson = CardJson().apply {
            this.json = json
        }
        val insert = cardJsonDao.insert(cardJson)
        card.id = insert
    }

    fun getCard(id: Long): Card {
        val cardJson = doAsyncResult { cardJsonDao.getById(id) }.get()
        return getCardFromJson(cardJson.json).apply {
            this.id = cardJson.id
        }
    }

    fun addPage(pageName: String): Page {
        val page = Page().apply {
            this.pageName = pageName
        }

        page.id = doAsyncResult { pageDao.insert(page) }.get()
        return page
    }

    fun getPageList(): MutableList<Page> {
        val cardJsonList = doAsyncResult { cardJsonDao.getAll() }.get()
        val cardList = mutableListOf<Card>()
        cardJsonList.forEach {
            cardList.add(getCardFromJson(it.json).apply {
                id = it.id
            })
        }
        val pageList = doAsyncResult { pageDao.getAll() }.get()

        pageList.forEach { page ->
            cardList.forEach { card ->
                if (page.id == card.idPage)
                    page.cards.add(MutableLiveData<Card>().apply { postValue(card) })
            }
        }
        return pageList
    }

    fun updateCard(card: Card) {
        val json = Gson().toJson(card)
        val cardJson = CardJson().apply {
            this.json = json
            id = card.id
        }
        cardJsonDao.update(cardJson)
    }

    fun getAllListType(): MutableList<ListType> {
        val allListJson = doAsyncResult { listTypeDao.getAll() }.get()
        val list = mutableListOf<ListType>()
        allListJson.forEach {
            val listTypeJson = it.json
            val listType = Gson().fromJson<ListType>(listTypeJson, ListType::class.java)
            list.add(listType)
        }
        return list
    }

    fun addListType(listType: ListType) {
        val json = Gson().toJson(listType)
        val listTypeJson = ListTypeJson().apply {
            this.json = json
        }
        listTypeDao.insert(listTypeJson)
    }

    fun updatePage(page: Page) {
        doAsync { pageDao.update(page) }
    }

    fun getSampleCard(sampleID: Long): Card? {
        return sampleHelper.getSample(sampleID)
    }
}