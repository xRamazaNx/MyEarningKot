package ru.developer.press.myearningkot.database

import com.bugsnag.android.Bugsnag
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import ru.developer.press.myearningkot.model.Row
import ru.developer.press.myearningkot.model.Total

const val PAGE_PATH = "pages"
const val CARD_PATH = "cards"
const val COLUMN_PATH = "columns"
const val ROW_PATH = "rows"
const val TOTAL_PATH = "totals"

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

    fun deleteRow(row: Row) {

    }

    fun addPage(page: Page) {
        userStore {
            collection(PAGE_PATH)
                .document(page.refId)
                .set(page).addOnSuccessListener {

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

    fun synchronization() {

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
}