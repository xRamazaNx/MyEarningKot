package ru.developer.press.myearningkot.database

import com.bugsnag.android.Bugsnag
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import ru.developer.press.myearningkot.helpers.getColumnFromJson
import ru.developer.press.myearningkot.model.Column
import ru.developer.press.myearningkot.model.Row
import ru.developer.press.myearningkot.model.Total

private const val PAGE_PATH = "pages"
private const val CARD_PATH = "cards"
private const val COLUMN_PATH = "columns"
private const val ROW_PATH = "rows"
private const val TOTAL_PATH = "totals"

class FireStore {
    private val store: FirebaseFirestore = Firebase.firestore
    private val currentUser get() = Firebase.auth.uid

    private fun userStore(block: DocumentReference.() -> Unit) {
        currentUser?.let { userId ->
            store.collection("users")
                .document(userId)
                .block()
        }
    }

    fun deleteRow(row: Row){

    }
    fun addPage(page: PageRef) {
        userStore {
            collection(PAGE_PATH)
                .document(page.refId)
                .set(page).addOnSuccessListener {

                }.addOnFailureListener {
                    Bugsnag.notify(it)
                }
        }
    }

    fun addCard(card: CardRef) {
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

            card.columns.forEach { addColumn(it) }
            card.rows.forEach { addRow(it) }
            card.totals.forEach { addTotal(it) }
        }
    }

    fun addTotal(total: Total) {
        userStore {
            val totalRef = RowRef(total.pageId, total.cardId).apply {
                refId = total.refId
                json = Gson().toJson(total)
            }
            cardDocument(total.pageId, total.cardId)
                .collection(TOTAL_PATH)
                .document(total.refId)
                .set(totalRef)
                .addOnSuccessListener {

                }
                .addOnFailureListener {
                    Bugsnag.notify(it)
                }
        }
    }

    private fun DocumentReference.cardDocument(
        pageId: String,
        refId: String
    ) = collection(PAGE_PATH)
        .document(pageId)
        .collection(CARD_PATH)
        .document(refId)

    fun addColumn(column: Column){
        userStore {
            val columnRef = ColumnRef(column.className, column.pageId, column.cardId).apply {
                refId = column.refId
                json = Gson().toJson(column)
            }
            val cardDocument = cardDocument(column.pageId, column.cardId)
            val columnCollection = cardDocument.collection(COLUMN_PATH)

            columnCollection
                .document(columnRef.refId)
                .set(columnRef)
                .addOnSuccessListener {

                }
                .addOnFailureListener {
                    Bugsnag.notify(it)
                }
        }
    }

    fun addRow(row: Row) {
        userStore {
            val rowRef = RowRef(row.pageId, row.cardId).apply {
                refId = row.refId
                json = Gson().toJson(row)
            }
            cardDocument(row.pageId, row.cardId)
                .collection(ROW_PATH)
                .document(row.refId)
                .set(rowRef)
                .addOnSuccessListener {

                }
                .addOnFailureListener {
                    Bugsnag.notify(it)
                }
        }
    }

    fun synchronization() {

    }
}