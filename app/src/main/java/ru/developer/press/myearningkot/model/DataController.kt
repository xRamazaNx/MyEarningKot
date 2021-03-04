package ru.developer.press.myearningkot.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.anko.doAsync
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.helpers.*

class DataController(context: Context) {

    private val cardJsonDao: CardJsonDao
    private val pageDao: PageDao
    private val listTypeDao: ListTypeDao
    private val sampleHelper: SampleHelper

    suspend fun createDefaultSamplesJob(context: Context) {
        withContext(Dispatchers.Default) {
            addPage(context.getString(R.string.active))
            sampleHelper.addDefaultSamples(context)
        }
    }

    init {
        val database = Database.create(context)
        cardJsonDao = database.cardJsonDao()
        pageDao = database.pageDao()
        listTypeDao = database.listTypeDao()
        sampleHelper = SampleHelper(context)
    }

    private val dispatcher = Dispatchers.Default

    suspend fun addCard(card: Card) {
        withContext(dispatcher) {
            val json = Gson().toJson(card)
            val cardJson = CardJson().apply {
                this.json = json
            }
            val insert = cardJsonDao.insert(cardJson)
            card.id = insert
        }
    }

    suspend fun getCard(id: Long): Card {
        return withContext(dispatcher) {
            val cardJson = cardJsonDao.getById(id)
            getCardFromJson(cardJson.json).apply {
                this.id = cardJson.id
            }
        }
    }

    suspend fun addPage(pageName: String): Page {
        return withContext(dispatcher) {
            val page = Page().apply {
                this.pageName = pageName
            }
            page.id = pageDao.insert(page)
            page
        }
    }

    suspend fun getPageList(): MutableList<Page> {
        return withContext(dispatcher) {

            val cardJsonList = cardJsonDao.getAll()
            val cardList = mutableListOf<Card>()
            cardJsonList.forEach {
                cardList.add(getCardFromJson(it.json).apply {
                    id = it.id
                })
            }
            val pageList = pageDao.getAll()

            pageList.forEach { page ->
                cardList.forEach { card ->
                    if (page.id == card.idPage)
                        page.cards.add(MutableLiveData<Card>().apply { postValue(card) })
                }
            }
            pageList
        }
    }

    suspend fun updateCard(card: Card) {
        withContext(dispatcher) {
            val json = Gson().toJson(card)
            val cardJson = CardJson().apply {
                this.json = json
                id = card.id
            }
            cardJsonDao.update(cardJson)
        }
    }

    suspend fun getAllListType(): MutableList<ListType> {
        return withContext(dispatcher) {
            val allListJson = listTypeDao.getAll()
            val list = mutableListOf<ListType>()
            allListJson.forEach {
                val listTypeJson = it.json
                val listType = Gson().fromJson<ListType>(listTypeJson, ListType::class.java)
                list.add(listType)
            }
            list
        }
    }

    suspend fun addListType(listType: ListType) {
        withContext(dispatcher) {
            val json = Gson().toJson(listType)
            val listTypeJson = ListTypeJson().apply {
                this.json = json
            }
            listTypeDao.insert(listTypeJson)
        }
    }

    fun updatePage(page: Page) {
        doAsync { pageDao.update(page) }
    }

    suspend fun getSampleCard(sampleID: Long): Card {
        return withContext(dispatcher) {
            sampleHelper.getSample(sampleID)
        }
    }
}