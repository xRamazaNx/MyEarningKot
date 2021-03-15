package ru.developer.press.myearningkot.database

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.anko.doAsync
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.model.ListType
import ru.developer.press.myearningkot.model.Row
import ru.developer.press.myearningkot.model.Total

class DataController(context: Context) {

    private val sampleDao: SampleDao
    private val listTypeDao: ListTypeDao

    private val pageDao: PageDao
    private val cardDao: CardDao
    private val columnDao: ColumnDao
    private val rowDao: RowDao
    private val totalDao: TotalDao

    private val fireStore = FireStore()
    private val dispatcher = Dispatchers.Default

    private fun inflateCard(cardRef: CardRef): CardRef {
        // add columns
        val columnsRef: List<ColumnRef> = columnDao.getAllOf(cardRef.refId)
        val columns = convertRefToColumn(columnsRef)
        cardRef.columns.addAll(columns)
        // add rows
        val rowRefs: List<RowRef> = rowDao.getAllOf(cardRef.refId)
        val rows = rowRefs.fold(mutableListOf<Row>()) { list, rowRef ->
            list.add(Gson().fromJson(rowRef.json, Row::class.java))
            list
        }
        cardRef.rows.addAll(rows)
        // add totals
        val totalRefs = totalDao.getAllOf(cardRef.refId)
        val totals = totalRefs.fold(mutableListOf<Total>()) { list, totalRef ->
            list.add(Gson().fromJson(totalRef.json, Total::class.java))
            list
        }
        cardRef.totals.addAll(totals)

        return cardRef
    }

    suspend fun createDefaultSamplesJob(context: Context) {
        withContext(dispatcher) {
            val pageName = context.getString(R.string.active)
            val pageRef = PageRef(pageName)
            pageDao.insert(pageRef)
            SampleHelper.defaultSamples(context).forEach { cardRef ->
                addSample(cardRef)
            }

            fireStore.addPage(pageRef)
        }
    }

    init {
        val database = Database.create(context)
        sampleDao = database.sampleDao()
        cardDao = database.cardDao()
        pageDao = database.pageDao()
        listTypeDao = database.listTypeDao()
        columnDao = database.columnDao()
        rowDao = database.rowDao()
        totalDao = database.totalDao()
    }

    suspend fun addCard(card: CardRef) {
        withContext(dispatcher) {
            val gson = Gson()
            cardDao.insert(card)
            card.columns.forEach { column ->
                val columnRef = ColumnRef(column.className, card.pageId, card.refId).apply { refId = column.refId }
                columnRef.json = gson.toJson(column)
                columnDao.insert(columnRef)
            }
            card.rows.forEach { row ->
                val rowRef = RowRef(card.pageId, card.refId).apply { refId = row.refId }
                rowRef.json = gson.toJson(row)
                rowDao.insert(rowRef)
            }
            card.totals.forEach { total ->
                val totalRef = TotalRef(card.pageId, card.refId).apply { refId = total.refId }
                totalRef.json = gson.toJson(total)
                totalDao.insert(totalRef)
            }
            fireStore.addCard(card)
        }
    }

    suspend fun getCard(refId: String): CardRef {
        return withContext(dispatcher) {
            inflateCard(cardDao.getById(refId))
        }
    }

    suspend fun addPage(pageName: String): PageRef {
        return withContext(dispatcher) {
            val page = PageRef(pageName)
            fireStore.addPage(page)
            pageDao.insert(page)
            page
        }
    }

    suspend fun getPageList(): MutableList<PageRef> {
        return withContext(dispatcher) {
            val pageList = pageDao.getAll()
            pageList.forEach { page ->
                val cards = cardDao.getAllOf(page.refId)
                cards.forEach { card ->
                    inflateCard(card)
                    page.cards.add(MutableLiveData<CardRef>().apply { postValue(card) })
                }
            }
            pageList.toMutableList()
        }
    }

    suspend fun updateCard(card: CardRef) {
        withContext(dispatcher) {
            cardDao.update(card)
        }
    }

    suspend fun getAllListType(): MutableList<ListType> {
        return withContext(dispatcher) {
            val allListJson = listTypeDao.getAll()
            val list = mutableListOf<ListType>()
            allListJson.forEach { listTypeJson ->
                val typeJson = listTypeJson.json
                val listType = Gson().fromJson(typeJson, ListType::class.java)
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

    fun updatePage(page: PageRef) {
        doAsync { pageDao.update(page) }
    }

    suspend fun getSampleCard(sampleID: String): CardRef {
        return withContext(dispatcher) {
            inflateCard(sampleDao.getByRefId(sampleID)) as CardRef
        }
    }

    suspend fun getSampleList(): List<CardRef> {
        return withContext(Dispatchers.Default) {
            sampleDao.getAll().onEach { cardRef ->
                inflateCard(cardRef)
            }
        }
    }

    suspend fun addSample(card: CardRef): List<CardRef> {
        return withContext(dispatcher) {
            val gson = Gson()
            sampleDao.insert(card)
            card.columns.forEach { column ->
                val columnRef = ColumnRef(column.className,card.pageId, card.refId).apply { refId = column.refId }
                columnRef.json = gson.toJson(column)
                columnDao.insert(columnRef)
            }
            card.rows.forEach { row ->
                val rowRef = RowRef(card.pageId, card.refId).apply { refId = row.refId }
                rowRef.json = gson.toJson(row)
                rowDao.insert(rowRef)
            }
            card.totals.forEach { total ->
                val totalRef = TotalRef(card.pageId, card.refId).apply { refId = total.refId }
                totalRef.json = gson.toJson(total)
                totalDao.insert(totalRef)
            }
            fireStore.addCard(card)
            getSampleList()
        }
    }

    suspend fun updateSample(card: CardRef) {
        withContext(Dispatchers.Default) {
            sampleDao.update(card)
        }
    }

    suspend fun deleteSample(deleteId: String) {
        sampleDao.delete(deleteId)
    }
}