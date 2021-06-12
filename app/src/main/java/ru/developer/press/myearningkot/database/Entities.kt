package ru.developer.press.myearningkot.database

import androidx.room.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Exclude
import ru.developer.press.myearningkot.ProvideCardPropertyForCell
import ru.developer.press.myearningkot.helpers.MyLiveData
import ru.developer.press.myearningkot.helpers.getDate
import ru.developer.press.myearningkot.model.*
import java.util.*

open class Ref {
    /*
    используется при добавлении новой сушности
    из существующей, которая наследуется от нее,
    что бы они имели новый ид и дату создания!
     */
    fun newRef() {
        refId = UUID.randomUUID().toString()
        dateCreate = System.currentTimeMillis()
        dateChange = dateCreate
    }

    open fun copyRefFrom(ref: Ref) {
        refId = ref.refId
        dateCreate = ref.dateCreate
        dateChange = System.currentTimeMillis()
        ref.dateChange = dateChange
    }

    @PrimaryKey
    var refId: String = UUID.randomUUID().toString()
    var dateCreate = System.currentTimeMillis()
    var dateChange: Long = dateCreate
}

open class BelongIds(
    var pageId: String,
    var cardId: String
) : Ref() {
    companion object {
        fun withPage(refId: String): BelongIds {
            return BelongIds("", "").apply {
                this.refId = refId
            }
        }
    }
}

@Entity
class Page(
    var name: String = ""
) : Ref() {
    var position = 0

    override fun copyRefFrom(ref: Ref) {
        super.copyRefFrom(ref)
        val copPage = ref as Page
        position = copPage.position
        name = copPage.name
    }

    @Ignore
    @get:Exclude
    val cards = mutableListOf<MyLiveData<Card>>()
}

@Entity(
    foreignKeys = [ForeignKey(
        entity = Page::class,
        parentColumns = arrayOf("refId"),
        childColumns = arrayOf("pageId"),
        onDelete = ForeignKey.CASCADE
    )]
)
open class Card(var pageId: String, var name: String = "") : Ref(), ProvideCardPropertyForCell {
    constructor() : this("")

    @Embedded(prefix = "card_pref")
    var cardPref = PrefForCard()

    @Embedded(prefix = "sort_pref")
    var sortPref = SortPref()
    var isUpdating = false
    var isShowDatePeriod: Boolean = false
    var isShowTotalInfo = true
    var valuta = 0
    var enableSomeStroke = true
    var enableHorizontalScroll = false
    var enableHorizontalScrollTotal = false
    var heightCells = 35
        set(value) {
            if (value in 18..70) {
                field = value
            }
        }

    @Ignore
    @get:Exclude
    val rows = mutableListOf<Row>()

    @Ignore
    @get:Exclude
    val columns = mutableListOf<Column>()

    @Ignore
    @get:Exclude
    val totals = mutableListOf<Total>()

    val dateOfPeriod: String
        get() {
            val variantDate = cardPref.dateOfPeriodPref.type
            val enableTime = cardPref.dateOfPeriodPref.enableTime
            val first = getDate(variantDate, dateCreate, enableTime)
            val last = getDate(variantDate, dateChange, enableTime)
            return "$first - $last"
        }

    override fun isSingleLine(): Boolean = !enableSomeStroke

    override fun getValutaType(): Int = valuta
}

open class JsonValue(pageId: String, cardId: String) : BelongIds(pageId, cardId) {
    var json: String = ""
}

@Entity(
    foreignKeys = [ForeignKey(
        entity = Card::class,
        parentColumns = arrayOf("refId"),
        childColumns = arrayOf("cardId"),
        onDelete = ForeignKey.CASCADE
    )]
)
class ColumnJson(pageId: String, cardId: String) : JsonValue(pageId, cardId) {
    constructor() : this("", "")
}

@Entity(
    foreignKeys = [ForeignKey(
        entity = Card::class,
        parentColumns = arrayOf("refId"),
        childColumns = arrayOf("cardId"),
        onDelete = ForeignKey.CASCADE
    )]
)
class RowJson(pageId: String, cardId: String) : JsonValue(pageId, cardId) {
    constructor() : this("", "")
}

@Entity(
    foreignKeys = [ForeignKey(
        entity = Card::class,
        parentColumns = arrayOf("refId"),
        childColumns = arrayOf("cardId"),
        onDelete = ForeignKey.CASCADE
    )]
)
class TotalJson(pageId: String, cardId: String) : JsonValue(pageId, cardId) {
    constructor() : this("", "")
}

@Entity
class ListTypeJson : Ref() {
    var json: String = ""
}

fun Column.columnJson(): ColumnJson {
    val columnJson = ColumnJson(pageId, cardId)
    columnJson.json = gson.toJson(this)
    columnJson.copyRefFrom(this)
    return columnJson
}

fun Row.rowJson(): RowJson {
    val rowJson = RowJson(pageId, cardId)
    rowJson.json = gson.toJson(this)
    rowJson.copyRefFrom(this)
    return rowJson
}

fun Total.totalJson(): TotalJson {
    val totalJson = TotalJson(pageId, cardId)
    totalJson.json = gson.toJson(this)
    totalJson.copyRefFrom(this)
    return totalJson
}

data class UpdatedRefData(
    val refIds: BelongIds,
    val refType: FireStore.RefType,
    val updatedType: DocumentChange.Type
)

