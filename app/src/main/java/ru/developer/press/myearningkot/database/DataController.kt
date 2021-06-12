package ru.developer.press.myearningkot.database

import android.content.Context
import androidx.room.Transaction
import com.google.firebase.firestore.DocumentChange.Type.*
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.developer.press.myearningkot.App
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.database.FireStore.RefType.PAGE
import ru.developer.press.myearningkot.helpers.io
import ru.developer.press.myearningkot.helpers.liveData
import ru.developer.press.myearningkot.helpers.main
import ru.developer.press.myearningkot.helpers.runOnDefault
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

    var refUpdatedNotify: ((FireStore.RefType, FireStore.ChangedRef) -> Unit)? = null

    private fun inflateCard(card: Card): Card {
        // add columns
        val columnsRef: List<JsonValue> = columnDao.getAllOf(card.refId)
        val columns = convertRefToColumn(columnsRef)
        card.columns.addAll(columns)
        // add rows
        val rowRefs: List<JsonValue> = rowDao.getAllOf(card.refId)
        val rows = rowRefs.fold(mutableListOf<Row>()) { list, rowRef ->
            list.add(gson.fromJson(rowRef.json, Row::class.java).apply { status = Row.Status.NONE })
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
    suspend fun createDefaultSamplesJob(context: Context) = io {
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

    private suspend fun updateRefUi(updatedRefData: UpdatedRefData?) = main {
        App.fireStoreChanged.value = updatedRefData
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

        fireStore.apply {
            pageChangedNotify = {
                runOnDefault {

                    val refId = it.refId
                    var pageDB = pageDao.getById(refId)
                    val pageFire: Page = it.documentChange.document.toObject(Page::class.java)
                    fun addPageIfNotExist(): Boolean {
                        val pageEqualName = pageDao.getAll()
                            .find { it.name == pageFire.name }

                        when {
                            pageDB == null && pageEqualName != null -> {
                                replacePageInDB(pageEqualName, pageFire)
                                pageDB = pageEqualName
                                return true
                            }
                            pageDB == null && pageEqualName == null -> {
                                pageDao.insert(pageFire)
                                pageDB = pageFire
                                return true
                            }
                            else -> {
                                pageDB!!
                                return false
                            }
                        }
                    }
                    when (it.documentChange.type) {
                        ADDED -> {
                            if (addPageIfNotExist())
                                updateRefUi(
                                    UpdatedRefData(
                                        BelongIds.withPage(pageDB!!.refId),
                                        PAGE,
                                        ADDED
                                    )
                                )
                        }
                        MODIFIED -> {
                            addPageIfNotExist()
                            // если в базе дата старая чем в фиресторе
                            if (pageDB!!.dateChange < pageFire.dateChange) {
                                pageDao.update(pageFire)
                                pageDB = pageFire
                                updateRefUi(
                                    UpdatedRefData(
                                        BelongIds.withPage(pageDB!!.refId),
                                        PAGE,
                                        MODIFIED
                                    )
                                )
                            }
                            // если в базе дата новая чем в фиресторе
                            else if (pageDB!!.dateChange > pageFire.dateChange) {
                                fireStore.addPage(pageDB!!)
                            }
                        }
                        REMOVED -> {
                            pageDao.delete(it.refId)
                            pageDB = null
                            updateRefUi(
                                UpdatedRefData(
                                    BelongIds.withPage(it.refId),
                                    PAGE,
                                    REMOVED
                                )
                            )
                        }
                    }
                }
            }
//            cardChangedNotify = {
//
//                GlobalScope.launch {
//
//                    var cardDB = cardDao.getById(it.refId)
//                    val cardFire = it.documentChange.document.toObject(Card::class.java)
//
//                    fun addCardIfNotExist() {
//                        if (cardDB == null) {
//                            cardDao.insert(cardFire)
//                            cardDB = cardFire
//                        }
//                    }
//                    when (it.documentChange.type) {
//                        ADDED -> {
//                            addCardIfNotExist()
//                        }
//                        MODIFIED -> {
//                            addCardIfNotExist()
//                            if (cardDB!!.dateChange < cardFire.dateChange) {
//                                cardDao.update(cardFire)
//                            } else {
//                                fireStore.addCard(cardDB!!)
//                            }
//                        }
//                        REMOVED -> {
//                            cardDao.delete(it.refId)
//                        }
//                    }
//                }
//            }
//            columnChangedNotify = {
//                GlobalScope.launch {
//                    var columnDB: ColumnJson? = columnDao.getById(it.refId)
//                    val columnFire = it.documentChange.document.toObject(ColumnJson::class.java)
//
//                    fun addColumnIfNotExist() {
//                        if (columnDB == null) {
//                            columnDao.insert(columnFire)
//                            columnDB = columnFire
//                        }
//                    }
//
//                    when (it.documentChange.type) {
//                        ADDED -> {
//                            addColumnIfNotExist()
//                        }
//                        MODIFIED -> {
//                            addColumnIfNotExist()
//                            if (columnDB!!.dateChange < columnFire.dateChange) {
//                                columnDao.update(columnFire)
//                            } else {
//                                fireStore.addJsonValue(columnDB!!, COLUMN_PATH)
//                            }
//                        }
//                        REMOVED -> {
//                            columnDao.delete(columnFire.refId)
//                        }
//                    }
//                }
//            }
//            rowChangedNotify = {
//                GlobalScope.launch {
//                    var rowDB: RowJson? = rowDao.getById(it.refId)
//                    val rowFire = it.documentChange.document.toObject(RowJson::class.java)
//
//                    fun addRowIfNotExist() {
//                        if (rowDB == null) {
//                            rowDao.insert(rowFire)
//                            rowDB = rowFire
//                        }
//                    }
//
//                    when (it.documentChange.type) {
//                        ADDED -> {
//                            addRowIfNotExist()
//                        }
//                        MODIFIED -> {
//                            addRowIfNotExist()
//                            if (rowDB!!.dateChange < rowFire.dateChange) {
//                                rowDao.update(rowFire)
//                            } else {
//                                fireStore.addJsonValue(rowDB!!, ROW_PATH)
//                            }
//                        }
//                        REMOVED -> {
//                            rowDao.delete(rowFire.refId)
//                        }
//                    }
//                }
//            }
//            totalChangedNotify = {
//                GlobalScope.launch {
//                    var totalDB: TotalJson? = totalDao.getById(it.refId)
//                    val totalFire = it.documentChange.document.toObject(TotalJson::class.java)
//
//                    fun addTotalIfNotExist() {
//                        if (totalDB == null) {
//                            totalDao.insert(totalFire)
//                            totalDB = totalFire
//                        }
//                    }
//
//                    when (it.documentChange.type) {
//                        ADDED -> {
//                            addTotalIfNotExist()
//                        }
//                        MODIFIED -> {
//                            addTotalIfNotExist()
//                            if (totalDB!!.dateChange < totalFire.dateChange) {
//                                totalDao.update(totalFire)
//                            } else {
//                                fireStore.addJsonValue(totalDB!!, TOTAL_PATH)
//                            }
//                        }
//                        REMOVED -> {
//                            totalDao.delete(totalFire.refId)
//                        }
//                    }
//                }
//            }
        }
    }

    suspend fun syncRefs() = io {
        val pageList = getPageList()
        pageList.forEach { pageDB ->
            val pageFire: Page? =
                fireStore.getRef(PAGE, BelongIds.withPage(pageDB.refId))
            val pageWithName = fireStore.pageWithName(pageDB.name)
            when {
                // если нет такой вкладки на сервере и нет с таким же именем
                pageFire == null && pageWithName == null -> {
                    fireStore.addPage(pageDB)
                }
                pageFire == null && pageWithName != null -> {
                    replacePageInDB(pageDB, pageWithName)
                }
                else -> {
                    if (pageFire!!.dateChange > pageDB.dateChange) {
                        pageDao.insert(pageFire)
                        updateRefUi(
                            UpdatedRefData(
                                BelongIds.withPage(pageFire.refId),
                                PAGE, MODIFIED
                            )
                        )
                    } else if (pageFire.dateChange < pageDB.dateChange) {
                        fireStore.addPage(pageDB)
                    }
                }
            }
        }
        fireStore.setSyncListener()
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
    suspend fun addCard(card: Card) = io {
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

    suspend fun getCard(refId: String): Card = io {
        inflateCard(cardDao.getById(refId)!!).apply { updateTypeControl() }
    }

    suspend fun addPage(pageName: String, position: Int): Page = io {
        val page = Page(pageName)
        page.position = position
        fireStore.addPage(page)
        pageDao.insert(page)
        page
    }

    private suspend fun inflatePage(page: Page) {
        val cards = cardDao.getAllOf(page.refId)
        cards.forEach { card ->
            inflateCard(card)
            page.cards.add(main { liveData(card) })
        }
    }

    suspend fun getPageList(): MutableList<Page> = io {
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

    suspend fun getAllListType(): MutableList<ListType> = io {
        val allListJson = listTypeDao.getAll()
        val list = mutableListOf<ListType>()
        allListJson.forEach { listTypeJson ->
            val typeJson = listTypeJson.json
            val listType = gson.fromJson(typeJson, ListType::class.java)
            list.add(listType)
        }
        list
    }

    suspend fun addListType(listType: ListType) = io {
        val json = gson.toJson(listType)
        val listTypeJson = ListTypeJson().apply {
            this.json = json
        }
        listTypeDao.insert(listTypeJson)
    }

    suspend fun updatePage(page: Page) = io {
        page.dateChange = System.currentTimeMillis()
        pageDao.update(page)
        fireStore.addPage(page)
    }

    suspend fun getSampleCard(sampleID: String): Card = io {
        inflateCard(sampleDao.getByRefId(sampleID)!!)
    }

    suspend fun getSampleList(): List<Card> = io {
        sampleDao.getAll().onEach { cardRef ->
            inflateCard(cardRef)
        }
    }

    private suspend fun addSample(card: Card): List<Card> = io {
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

    suspend fun updateSample(card: Card) = io {
        sampleDao.update(card)
    }

    suspend fun deleteSample(deleteId: String) = io {
        sampleDao.delete(deleteId)
    }

    @Transaction // для обновления всей карточки после настройки
    suspend fun updateCard(card: Card) = io {
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
        }
        // добавляем все строки
        card.rows.forEach {
            val rowRef = it.rowJson()
            rowDao.insert(rowRef)
        }
        // добавляем все итоги
        card.totals.forEach {
            val totalRef = it.totalJson()
            totalDao.insert(totalRef)
        }

        fireStore.addCard(card)
    }

    suspend fun deleteRows(rows: List<Row>) = io {
        rows.forEach {
            rowDao.delete(it.refId)
            fireStore.deleteJsonValue(it.rowJson(), ROW_PATH)
        }
    }

    suspend fun addRow(row: Row) = io {
        val jsonValue = row.rowJson()
        rowDao.insert(jsonValue)
        fireStore.addJsonValue(jsonValue, ROW_PATH)
    }

    suspend fun updateRow(row: Row) = io {
        val jsonValue = row.rowJson()
        rowDao.update(jsonValue)
        fireStore.addJsonValue(jsonValue, ROW_PATH)
    }

    suspend fun getPage(pageId: String): Page? = io {
        pageDao.getById(pageId)?.also { inflatePage(it) }
    }

    suspend fun deleteCard(card: Card) = io {
        cardDao.delete(card.refId)
        fireStore.deleteCard(card.pageId, card.refId)
    }

    suspend fun deletePage(page: Page) = io {
        pageDao.delete(page.refId)
        fireStore.deletePage(page)
    }
}