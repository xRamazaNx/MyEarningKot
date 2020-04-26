package ru.developer.press.myearningkot.model

import com.google.gson.Gson
import org.jetbrains.anko.doAsyncResult
import ru.developer.press.myearningkot.App
import ru.developer.press.myearningkot.helpers.CardJson
import ru.developer.press.myearningkot.helpers.ListTypeJson
import ru.developer.press.myearningkot.helpers.Page
import ru.developer.press.myearningkot.helpers.getCardFromJson

class DataController {
    private val cardJsonDao = App.instance!!.database.cardJsonDao()
    private val pageDao = App.instance!!.database.pageDao()
    private val listTypeDao = App.instance!!.database.listTypeDao()

    fun addCard(card: Card) {
        //
        val json = Gson().toJson(card)
        val cardJson = CardJson().apply {
            this.json = json
        }
        val id = doAsyncResult {
            cardJsonDao.insert(cardJson)
        }
        card.id = id.get()

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
                    page.cards.add(card)
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
}