package ru.developer.press.myearningkot.database

import com.bugsnag.android.Bugsnag
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import ru.developer.press.myearningkot.database.FireStore.RefType.*
import java.util.concurrent.ExecutionException

const val PAGE_PATH = "pages"
const val CARD_PATH = "cards"
const val COLUMN_PATH = "columns"
const val ROW_PATH = "rows"
const val TOTAL_PATH = "totals"

class FireStore {
    private val store: FirebaseFirestore = Firebase.firestore
    private val currentUser get() = Firebase.auth.uid

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

    fun setSyncListener(changed: (ref: ChangedRef) -> Unit) {
        // сначала смотрим есть ли измененные данные на сервере
        userStore {
            // переходим к вкладкам
            collection(PAGE_PATH).apply {
                addChangedListener(PAGE) { pageId, pageChanged ->
                    changed.invoke(pageChanged)
                    document(pageId)
                        .collection(CARD_PATH).apply {
                            addChangedListener(CARD) { cardId, cardChanged ->
                                changed.invoke(cardChanged.apply { this.pageId = pageId })
                                val document = document(cardId)
                                document.collection(COLUMN_PATH)
                                    .addChangedListener(COLUMN) { _: String, columnChanged ->
                                        changed.invoke(columnChanged.apply {
                                            this.pageId = pageId
                                            this.cardId = cardId
                                        })
                                    }
                                document.collection(ROW_PATH)
                                    .addChangedListener(ROW) { _, rowChanged ->
                                        changed.invoke(rowChanged.apply {
                                            this.pageId = pageId
                                            this.cardId = cardId
                                        })
                                    }
                                document.collection(TOTAL_PATH)
                                    .addChangedListener(TOTAL) { _, totalChanged ->
                                        changed.invoke(totalChanged.apply {
                                            this.pageId = pageId
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
        type: RefType,
        changed: (id: String, ref: ChangedRef) -> Unit
    ) {
        addSnapshotListener { value: QuerySnapshot?, error ->
            value?.let { pageSnapShot: QuerySnapshot ->
                // получаем документы которые изменились со времени моего присутсвия в приложении
                // при входе и вызове заного то получаем все
                pageSnapShot.documentChanges.forEach { documentChange: DocumentChange ->
                    val document = documentChange.document
                    if (document.exists()) {
                        val id = document.id
                        changed.invoke(id, ChangedRef(type, id))
                    }
                }
            }
        }
    }

    fun deleteJsonValue(it: JsonValue, firePath: String) {
        userStore {
            val cardDocument = cardDocument(it.pageId, it.cardId)
            cardDocument
                .collection(firePath)
                .document(it.refId).delete()
                .addOnSuccessListener {

                }.addOnFailureListener {
                    Bugsnag.notify(it)
                }
        }
    }

    inline fun <reified T> getRef(changedRef: ChangedRef): T? {
        var ref: T? = null
        userStore {
            val collection = when (changedRef.type) {
                PAGE -> {
                    collection(PAGE_PATH)
                }
                CARD -> {
                    collection(PAGE_PATH)
                        .document(changedRef.pageId)
                        .collection(CARD_PATH)
                }
                COLUMN -> {
                    collection(PAGE_PATH)
                        .document(changedRef.pageId)
                        .collection(CARD_PATH)
                        .document(changedRef.cardId)
                        .collection(COLUMN_PATH)
                }
                ROW -> {
                    collection(PAGE_PATH)
                        .document(changedRef.pageId)
                        .collection(CARD_PATH)
                        .document(changedRef.cardId)
                        .collection(ROW_PATH)
                }
                TOTAL -> {
                    collection(PAGE_PATH)
                        .document(changedRef.pageId)
                        .collection(CARD_PATH)
                        .document(changedRef.cardId)
                        .collection(TOTAL_PATH)
                }
            }
            collection(PAGE_PATH)
            ref = try {
                Tasks.await(
                    collection
                        .document(changedRef.refId)
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

    data class ChangedRef(val type: RefType, val refId: String) {
        var name = ""
        var pageId = ""
        var cardId = ""
    }

    enum class RefType {
        PAGE,
        CARD,
        COLUMN,
        ROW,
        TOTAL
    }
}