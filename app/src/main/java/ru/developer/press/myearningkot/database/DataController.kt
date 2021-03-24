package ru.developer.press.myearningkot.database

import android.content.Context
import androidx.room.Transaction
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.doAsync
import ru.developer.press.myearningkot.App
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.database.FireStore.RefType.*
import ru.developer.press.myearningkot.helpers.MyLiveData
import ru.developer.press.myearningkot.helpers.liveData
import ru.developer.press.myearningkot.helpers.runOnMain
import ru.developer.press.myearningkot.helpers.scoups.calcTotals
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
            val total = gson.fromJson(totalRef.json, Total::class.java)
            list.add(total)
            list
        }
        card.totals.addAll(totals)
        card.calcTotals()

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

    suspend fun syncRefs() {
        withContext(Dispatchers.IO) {
            val pageList = getPageList()
            pageList.forEach { pageDB ->
                val pageFire: Page? = fireStore.getRef(FireStore.ChangedRef(PAGE, pageDB.refId))
                val pageWithName = fireStore.pageWithName(pageDB.name)
                val newPage = when {
                    // если нет такой вкладки на сервере и нет с таким же именем
                    pageFire == null && pageWithName == null -> {
                        fireStore.addPage(pageDB)
                        pageDB
                    }
                    pageFire == null && pageWithName != null -> {
                        replacePageInDB(pageDB, pageWithName)
                        pageWithName
                    }
                    else -> {
                        when {
                            pageFire!!.dateChange > pageDB.dateChange -> {
                                pageDao.insert(pageFire)
                                pageFire
                            }
                            pageFire.dateChange < pageDB.dateChange -> {
                                fireStore.addPage(pageDB)
                                pageDB
                            }
                            else -> null
                        }
                    }
                }
                newPage?.let { changedPage ->
                    val changedRef: FireStore.ChangedRef =
                        FireStore.ChangedRef(PAGE, changedPage.refId)
                            .apply { name = changedPage.name }
                    runOnMain {
                        App.fireStoreChanged.value = changedRef
                    }
                }
            }
            fireStore.setSyncListener { changedRef ->
                GlobalScope.launch {
                    var newChangedRef: FireStore.ChangedRef? = null
                    when (changedRef.type) {
                        PAGE -> {
                            val pageDB = pageDao.getById(changedRef.refId)
                            val pageFire = fireStore.getRef<Page>(changedRef)!!
                            // если нет то значит и так новое
                            newChangedRef =
                                if (pageDB != null) {
                                    when {
                                        // если в базе дата старая чем в фиресторе
                                        pageDB.dateChange < pageFire.dateChange -> {
                                            pageDao.update(pageFire)
                                            FireStore.ChangedRef(PAGE, pageFire.refId)
                                                .apply { name = pageFire.name }
                                        }
                                        // если в базе дата новая чем в фиресторе
                                        pageDB.dateChange > pageFire.dateChange -> {
                                            fireStore.addPage(pageDB)
                                            FireStore.ChangedRef(PAGE, pageDB.refId)
                                                .apply { name = pageDB.name }
                                        }
                                        else -> null
                                    }
                                } else {
                                    val findEqualNamesPage =
                                        pageDao.getAll().find { it.name == pageFire.name }

                                    if (findEqualNamesPage != null) {
                                        replacePageInDB(findEqualNamesPage, pageFire)
                                    } else {
                                        pageDao.insert(pageFire)
                                    }
                                    FireStore.ChangedRef(PAGE, pageFire.refId)
                                        .apply { name = pageFire.name }
                                }
                        }
                        CARD -> {
                            val cardDB = cardDao.getById(changedRef.refId)
                            val cardFire = fireStore.getRef<Card>(changedRef)!!

                            if (cardDB != null) {
                                if (cardDB.dateChange < cardFire.dateChange) {
                                    cardDao.update(cardFire)
                                } else {
                                    fireStore.addCard(cardDB)
                                }
                            } else {
                                cardDao.insert(cardFire)
                            }
                        }
                        COLUMN -> {
                            val columnDB: ColumnJson? = columnDao.getById(changedRef.refId)
                            val columnFire = fireStore.getRef<ColumnJson>(changedRef)!!

                            if (columnDB != null) {
                                if (columnDB.dateChange < columnFire.dateChange) {
                                    columnDao.update(columnFire)
                                } else {
                                    fireStore.addJsonValue(columnDB, COLUMN_PATH)
                                }
                            } else {
                                columnDao.insert(columnFire)
                            }
                        }
                        ROW -> {
                            val rowDB: RowJson? = rowDao.getById(changedRef.refId)
                            val rowFire = fireStore.getRef<RowJson>(changedRef)!!

                            if (rowDB != null) {
                                if (rowDB.dateChange < rowFire.dateChange) {
                                    rowDao.update(rowFire)
                                } else {
                                    fireStore.addJsonValue(rowDB, ROW_PATH)
                                }
                            } else {
                                rowDao.insert(rowFire)
                            }
                        }
                        TOTAL -> {
                            val totalDB: TotalJson? = totalDao.getById(changedRef.refId)
                            val totalFire = fireStore.getRef<TotalJson>(changedRef)!!

                            if (totalDB != null) {
                                if (totalDB.dateChange < totalFire.dateChange) {
                                    totalDao.update(totalFire)
                                } else {
                                    fireStore.addJsonValue(totalDB, TOTAL_PATH)
                                }
                            } else {
                                totalDao.insert(totalFire)
                            }
                        }
                    }
                    newChangedRef?.let {
                        runOnMain {
                            App.fireStoreChanged.value = it
                        }
                    }
                }
            }
        }
    }

    @Transaction
    private fun replacePageInDB(
        from: Page,
        to: Page
    ) {
        // все карточки из вкладки
        val cards = cardDao.getAllOf(from.refId)
        // удаляем вкладку (обновить не получится так как обновить можно только по ид а ид приходится менять)
        pageDao.delete(from.refId)
        // переназначаем им новый ид вкладки
        cards.forEach {
            it.pageId = to.refId
            cardDao.update(it)
        }
        pageDao.insert(to)
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
            inflateCard(cardDao.getById(refId)!!).apply { updateTypeControl() }
        }
    }

    suspend fun addPage(pageName: String, position: Int): Page {
        return withContext(dispatcher) {
            val page = Page(pageName)
            page.position = position
            fireStore.addPage(page)
            pageDao.insert(page)
            page
        }
    }

    private suspend fun inflatePage(page: Page) {
        val cards = cardDao.getAllOf(page.refId)
        cards.forEach { card ->
            inflateCard(card)
            page.cards.add(runOnMain { liveData(card) })
        }
    }

    suspend fun getPageList(): MutableList<Page> {
        return withContext(dispatcher) {
            val pageList = pageDao.getAll().toMutableList()
            pageList.sortBy { it.position }
            pageList.find { it.name == samplePageName }?.let { samplePage ->
                pageList.remove(samplePage)
            }
            pageList.forEach { page ->
                inflatePage(page)
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
        doAsync {
            page.dateChange = System.currentTimeMillis()
            pageDao.update(page)
        }
    }

    suspend fun getSampleCard(sampleID: String): Card {
        return withContext(dispatcher) {
            inflateCard(sampleDao.getByRefId(sampleID)!!) as Card
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
            card.dateChange = System.currentTimeMillis()
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

    suspend fun getPage(pageId: String): Page? {
        return withContext(Dispatchers.IO) {
            pageDao.getById(pageId)?.also { inflatePage(it) }
        }
    }
}