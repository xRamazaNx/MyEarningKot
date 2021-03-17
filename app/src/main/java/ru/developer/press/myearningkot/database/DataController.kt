package ru.developer.press.myearningkot.database

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.room.Transaction
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.anko.doAsync
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.helpers.scoups.updateTypeControl
import ru.developer.press.myearningkot.model.ListType
import ru.developer.press.myearningkot.model.Row
import ru.developer.press.myearningkot.model.Total

val gson = Gson()
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


    private fun inflateCard(card: Card): Card {
        // add columns
        val columnsRef: List<JsonValue> = columnDao.getAllOf(card.refId)
        val columns = convertRefToColumn(columnsRef)
        card.columns.addAll(columns)
        // add rows
        val rowRefs: List<JsonValue> = rowDao.getAllOf(card.refId)
        val rows = rowRefs.fold(mutableListOf<Row>()) { list, rowRef ->
            list.add(gson.fromJson(rowRef.json, Row::class.java))
            list
        }
        card.rows.addAll(rows)
        // add totals
        val totalRefs = totalDao.getAllOf(card.refId)
        val totals = totalRefs.fold(mutableListOf<Total>()) { list, totalRef ->
            list.add(gson.fromJson(totalRef.json, Total::class.java))
            list
        }
        card.totals.addAll(totals)

        return card
    }

    @Transaction
    suspend fun createDefaultSamplesJob(context: Context) {
        withContext(dispatcher) {
            val pageName = context.getString(R.string.active)
            val pageRef = Page(pageName)
            val samplePageRef = Page(samplePageName).apply { refId = samplePageName }
            pageDao.insert(pageRef)
            pageDao.insert(samplePageRef)
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

    @Transaction
    suspend fun addCard(card: Card) {
        withContext(dispatcher) {
            card.newRef()
            cardDao.insert(card)
            card.columns.forEach { column ->
                column.newRef()
                column.pageId = card.pageId
                column.cardId = card.refId

                val columnRef = column.columnJson()
                columnDao.insert(columnRef)
            }
            card.rows.forEach { row ->
                row.newRef()
                row.pageId = card.pageId
                row.cardId = card.refId

                val rowRef = row.rowJson()
                rowDao.insert(rowRef)
            }
            card.totals.forEach { total ->
                total.newRef()
                total.pageId = card.pageId
                total.cardId = card.refId

                val totalRef = total.totalJson()
                totalDao.insert(totalRef)
            }
            fireStore.addCard(card)
        }
    }

    suspend fun getCard(refId: String): Card {
        return withContext(dispatcher) {
            inflateCard(cardDao.getById(refId)).apply { updateTypeControl() }
        }
    }

    suspend fun addPage(pageName: String): Page {
        return withContext(dispatcher) {
            val page = Page(pageName)
            fireStore.addPage(page)
            pageDao.insert(page)
            page
        }
    }

    suspend fun getPageList(): MutableList<Page> {
        return withContext(dispatcher) {
            val pageList = pageDao.getAll().toMutableList()
            pageList.find { it.name == samplePageName }?.let { samplePage ->
                pageList.remove(samplePage)
            }
            pageList.forEach { page ->
                val cards = cardDao.getAllOf(page.refId)
                cards.forEach { card ->
                    inflateCard(card)
                    page.cards.add(MutableLiveData<Card>().apply { postValue(card) })
                }
            }
            pageList.toMutableList()
        }
    }

    suspend fun getAllListType(): MutableList<ListType> {
        return withContext(dispatcher) {
            val allListJson = listTypeDao.getAll()
            val list = mutableListOf<ListType>()
            allListJson.forEach { listTypeJson ->
                val typeJson = listTypeJson.json
                val listType = gson.fromJson(typeJson, ListType::class.java)
                list.add(listType)
            }
            list
        }
    }

    suspend fun addListType(listType: ListType) {
        withContext(dispatcher) {
            val json = gson.toJson(listType)
            val listTypeJson = ListTypeJson().apply {
                this.json = json
            }
            listTypeDao.insert(listTypeJson)
        }
    }

    fun updatePage(page: Page) {
        doAsync { pageDao.update(page) }
    }

    suspend fun getSampleCard(sampleID: String): Card {
        return withContext(dispatcher) {
            inflateCard(sampleDao.getByRefId(sampleID)) as Card
        }
    }

    suspend fun getSampleList(): List<Card> {
        return withContext(Dispatchers.Default) {
            sampleDao.getAll().onEach { cardRef ->
                inflateCard(cardRef)
            }
        }
    }

    suspend fun addSample(card: Card): List<Card> {
        return withContext(dispatcher) {
            sampleDao.insert(card)
            card.columns.forEach { column ->
                val columnRef = column.columnJson()
                columnDao.insert(columnRef)
            }
            card.rows.forEach { row ->
                val rowRef = row.rowJson()
                rowDao.insert(rowRef)
            }
            card.totals.forEach { total ->
                val totalRef = total.totalJson()
                totalDao.insert(totalRef)
            }
            getSampleList()
        }
    }

    suspend fun updateSample(card: Card) {
        withContext(Dispatchers.Default) {
            sampleDao.update(card)
        }
    }

    suspend fun deleteSample(deleteId: String) {
        withContext(dispatcher) {
            sampleDao.delete(deleteId)
        }
    }

    @Transaction // для обновления всей карточки после настройки
    suspend fun updateCard(card: Card) {
        withContext(dispatcher) {
            cardDao.update(card)
            // удалить все имеющиеся колоны этой карточки
            val columnsFromDB = columnDao.getAllOf(card.refId)
            columnsFromDB.forEach {
                columnDao.delete(it)
                fireStore.deleteJsonValue(it, COLUMN_PATH)
            }
            // удалить все имеющиеся строки этой карточки
            val rowsFromDB = rowDao.getAllOf(card.refId)
            rowsFromDB.forEach {
                rowDao.delete(it)
                fireStore.deleteJsonValue(it, ROW_PATH)
            }
            // удалить все имеющиеся total этой карточки
            val totalsFromDB = totalDao.getAllOf(card.refId)
            totalsFromDB.forEach {
                totalDao.delete(it)
                fireStore.deleteJsonValue(it, TOTAL_PATH)
            }
//////////////////////////////////////////////
            // добавляем все колоны
            card.columns.forEach {
                val columnRef = it.columnJson()
                columnDao.insert(columnRef)
                fireStore.addJsonValue(columnRef, COLUMN_PATH)
            }
            // добавляем все строки
            card.rows.forEach {
                val rowRef = it.rowJson()
                rowDao.insert(rowRef)
                fireStore.addJsonValue(rowRef, ROW_PATH)
            }
            // добавляем все итоги
            card.totals.forEach {
                val totalRef = it.totalJson()
                totalDao.insert(totalRef)
                fireStore.addJsonValue(totalRef, TOTAL_PATH)
            }
        }
    }

    suspend fun deleteRows(rows: List<Row>) {
        withContext(dispatcher) {

            rows.forEach {
                rowDao.delete(it.refId)
                fireStore.deleteJsonValue(it.rowJson(), ROW_PATH)
            }
        }
    }

    suspend fun addRow(row: Row) {
        withContext(dispatcher) {
            row.status = Row.Status.NONE
            val jsonValue = row.rowJson()
            rowDao.insert(jsonValue)
            fireStore.addJsonValue(jsonValue, ROW_PATH)
        }
    }

    suspend fun updateRow(row: Row) {
        withContext(dispatcher) {
            val jsonValue = row.rowJson()
            rowDao.update(jsonValue)
            fireStore.addJsonValue(jsonValue, ROW_PATH)
        }
    }
}