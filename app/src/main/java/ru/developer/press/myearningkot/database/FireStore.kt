package ru.developer.press.myearningkot.database

import com.bugsnag.android.Bugsnag
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ru.developer.press.myearningkot.database.FireStore.RefType.*

const val PAGE_PATH = "pages"
const val CARD_PATH = "cards"
const val COLUMN_PATH = "columns"
const val ROW_PATH = "rows"
const val TOTAL_PATH = "totals"

typealias RefChangedNotify = ((FireStore.ChangedRef) -> Unit)

class FireStore {
    private val store: FirebaseFirestore = Firebase.firestore
    private val currentUser get() = Firebase.auth.uid
    var pageChangedNotify: RefChangedNotify? = null
    var cardChangedNotify: RefChangedNotify? = null
    var columnChangedNotify: RefChangedNotify? = null
    var rowChangedNotify: RefChangedNotify? = null
    var totalChangedNotify: RefChangedNotify? = null

    fun userStore(block: DocumentReference.() -> Unit) {
        currentUser?.let { userId ->
            store.collection("users")
                .document(userId)
                .block()
        }
    }

    fun addPage(page: Page) {
        userStore {
            collection(PAGE_PATH)
                .document(page.refId)
                .set(page)
                .addOnSuccessListener {

                }.addOnFailureListener {
                    Bugsnag.notify(it)
                }
        }
    }

    fun addCard(card: Card) {
        userStore {
            val cardDocument = cardDocument(card.pageId, card.refId)
            // добавить карточку
            cardDocument
                .set(card)
                .addOnSuccessListener {

                }
                .addOnFailureListener {
                    Bugsnag.notify(it)
                }

            card.columns.forEach {
                addJsonValue(it.columnJson(), COLUMN_PATH)
            }
            card.rows.forEach {
                addJsonValue(it.rowJson(), ROW_PATH)
            }
            card.totals.forEach {
                addJsonValue(it.totalJson(), TOTAL_PATH)
            }
        }
    }

    private fun DocumentReference.cardDocument(
        pageId: String,
        cardId: String
    ) = collection(PAGE_PATH)
        .document(pageId)
        .collection(CARD_PATH)
        .document(cardId)

    fun addJsonValue(jsonValue: JsonValue, firePath: String) {
        userStore {
            val cardDocument = cardDocument(jsonValue.pageId, jsonValue.cardId)
            val columnCollection = cardDocument.collection(firePath)
            columnCollection
                .document(jsonValue.refId)
                .set(jsonValue)
                .addOnSuccessListener {

                }
                .addOnFailureListener {
                    Bugsnag.notify(it)
                }
        }
    }

    fun setSyncListener() {
        // сначала смотрим есть ли измененные данные на сервере
        userStore {
            // переходим к вкладкам
            collection(PAGE_PATH).apply {
                addChangedListener { pageId, pageChanged ->
                    pageChangedNotify?.invoke(pageChanged)
                    document(pageId)
                        .collection(CARD_PATH).apply {
                            addChangedListener { cardId, cardChanged ->
                                cardChangedNotify?.invoke(cardChanged)
                                val document = document(cardId)
                                document.collection(COLUMN_PATH)
                                    .addChangedListener { _: String, columnChanged ->
                                        columnChangedNotify?.invoke(columnChanged.apply {
                                            this.cardId = cardId
                                        })
                                    }
                                document.collection(ROW_PATH)
                                    .addChangedListener { _, rowChanged ->
                                        rowChangedNotify?.invoke(rowChanged.apply {
                                            this.cardId = cardId
                                        })
                                    }
                                document.collection(TOTAL_PATH)
                                    .addChangedListener { _, totalChanged ->
                                        totalChangedNotify?.invoke(totalChanged.apply {
                                            this.cardId = cardId
                                        })
                                    }
                            }
                        }
                }
            }
        }
    }

    private fun CollectionReference.addChangedListener(
        changed: (id: String, ref: ChangedRef) -> Unit
    ) {
        addSnapshotListener { value: QuerySnapshot?, error ->
            value?.let { pageSnapShot: QuerySnapshot ->
                // получаем документы которые изменились со времени моего присутсвия в приложении
                // при входе и вызове заного то получаем все
                pageSnapShot.documentChanges.forEach { documentChange: DocumentChange ->
                    val document = documentChange.document
                    val id = document.id
                    changed.invoke(id, ChangedRef(documentChange))
                }
            }
        }
    }

    fun deleteJsonValue(jsonValue: JsonValue, firePath: String) {
        userStore {
            val cardDocument = cardDocument(jsonValue.pageId, jsonValue.cardId)
            cardDocument
                .collection(firePath)
                .document(jsonValue.refId)
                .delete()
                .addOnSuccessListener {

                }.addOnFailureListener {
                    Bugsnag.notify(it)
                }
        }
    }

    inline fun <reified T> getRef(type: RefType, belongIds: BelongIds): T? {
        var ref: T? = null
        userStore {
            val collection = when (type) {
                PAGE -> {
                    collection(PAGE_PATH)
                }
                CARD -> {
                    collection(PAGE_PATH)
                        .document(belongIds.pageId)
                        .collection(CARD_PATH)
                }
                COLUMN -> {
                    collection(PAGE_PATH)
                        .document(belongIds.pageId)
                        .collection(CARD_PATH)
                        .document(belongIds.cardId)
                        .collection(COLUMN_PATH)
                }
                ROW -> {
                    collection(PAGE_PATH)
                        .document(belongIds.pageId)
                        .collection(CARD_PATH)
                        .document(belongIds.cardId)
                        .collection(ROW_PATH)
                }
                TOTAL -> {
                    collection(PAGE_PATH)
                        .document(belongIds.pageId)
                        .collection(CARD_PATH)
                        .document(belongIds.cardId)
                        .collection(TOTAL_PATH)
                }
            }
            collection(PAGE_PATH)
            ref = try {
                Tasks.await(
                    collection
                        .document(belongIds.refId)
                        .get()
                ).toObject(T::class.java)
            } catch (ex: Exception) {
                null
            }
        }
        return ref
    }

    fun pageWithName(pageName: String): Page? {
        var page: Page? = null
        userStore {
            val await: QuerySnapshot = Tasks.await(
                collection(PAGE_PATH)
                    .get()
            )
            await.documents.forEach {
                val toObject = it.toObject(Page::class.java)
                if (toObject?.name == pageName) {
                    page = toObject
                    return@forEach
                }
            }
        }
        return page
    }

    fun deleteCard(pageId: String, cardId: String) {
        userStore {

            val cardDocument = cardDocument(pageId, cardId)
            // deleted columns
            val columnCollection = cardDocument.collection(COLUMN_PATH)
            Tasks.await(
                columnCollection
                    .get()
            ).documents.forEach { columnSnapshot ->
                columnCollection
                    .document(columnSnapshot.id)
                    .delete()
                    .addOnSuccessListener {}
                    .addOnFailureListener {
                        Bugsnag.notify(it)
                    }
            }
            // deleted rows
            val rowCollection = cardDocument.collection(ROW_PATH)
            Tasks.await(
                rowCollection
                    .get()
            ).documents.forEach { rowSnapshot ->
                rowCollection
                    .document(rowSnapshot.id)
                    .delete()
                    .addOnSuccessListener {}
                    .addOnFailureListener {
                        Bugsnag.notify(it)
                    }
            }
            // deleted totals
            val totalCollection = cardDocument.collection(TOTAL_PATH)
            Tasks.await(
                totalCollection
                    .get()
            ).documents.forEach { totalSnapshot ->
                totalCollection
                    .document(totalSnapshot.id)
                    .delete()
                    .addOnSuccessListener {}
                    .addOnFailureListener {
                        Bugsnag.notify(it)
                    }
            }

            cardDocument
                .delete()
                .addOnSuccessListener {}
                .addOnFailureListener {
                    Bugsnag.notify(it)
                }
        }
    }

    fun deletePage(page: Page) {
        userStore {
            val pageDocument = collection(PAGE_PATH)
                .document(page.refId)

            val cardSnapshotList: QuerySnapshot = Tasks.await(
                pageDocument
                    .collection(CARD_PATH)
                    .get()
            )

            cardSnapshotList.documents.forEach { cardSnapshot ->
                deleteCard(page.refId, cardSnapshot.id)
            }

            pageDocument
                .delete()
                .addOnSuccessListener {}
                .addOnFailureListener {
                    Bugsnag.notify(it)
                }
        }
    }

    data class ChangedRef(val documentChange: DocumentChange) {
        val refId: String = documentChange.document.id
        var cardId: String = ""
        var name = ""
    }

    enum class RefType {
        PAGE,
        CARD,
        COLUMN,
        ROW,
        TOTAL
    }
}


/*
*       - removed   = remove
* ref   - changed   = dateChanged -> logic
*       - added     = if not exist added
*
*
* */