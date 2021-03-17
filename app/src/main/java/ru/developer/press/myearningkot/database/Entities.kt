package ru.developer.press.myearningkot.database

import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.google.firebase.firestore.Exclude
import ru.developer.press.myearningkot.ProvideCardPropertyForCell
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

    fun copyRefFrom(ref: Ref) {
        refId = ref.refId
        dateCreate = ref.dateCreate
        dateChange = ref.dateChange
    }

    @PrimaryKey
    var refId: String = UUID.randomUUID().toString()
    var dateCreate = System.currentTimeMillis()
    var dateChange: Long = dateCreate
}

open class IdsRef(
    var pageId: String,
    var cardId: String
) : Ref()

@Entity
class Page(
    var name: String = ""
) : Ref() {
    var position = 0

    @Exclude
    @Ignore
    val cards = mutableListOf<MutableLiveData<Card>>()
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
    @Exclude
    val rows = mutableListOf<Row>()

    @Ignore
    @Exclude
    val columns = mutableListOf<Column>()

    @Ignore
    @Exclude
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

open class JsonValue(pageId: String, cardId: String) : IdsRef(pageId, cardId) {
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
class ColumnJson(pageId: String, cardId: String) : JsonValue(pageId, cardId)

@Entity(
    foreignKeys = [ForeignKey(
        entity = Card::class,
        parentColumns = arrayOf("refId"),
        childColumns = arrayOf("cardId"),
        onDelete = ForeignKey.CASCADE
    )]
)
class RowJson(pageId: String, cardId: String) : JsonValue(pageId, cardId)

@Entity(
    foreignKeys = [ForeignKey(
        entity = Card::class,
        parentColumns = arrayOf("refId"),
        childColumns = arrayOf("cardId"),
        onDelete = ForeignKey.CASCADE
    )]
)
class TotalJson(pageId: String, cardId: String) : JsonValue(pageId, cardId)

@Entity
class ListTypeJson : Ref() {
    var json: String = ""
}

fun Column.columnJson():ColumnJson{
    val columnJson = ColumnJson(pageId, cardId)
    columnJson.json = gson.toJson(this)
    columnJson.copyRefFrom(this)
    return columnJson
}

fun Row.rowJson():RowJson{
    val rowJson = RowJson(pageId, cardId)
    rowJson.json = gson.toJson(this)
    rowJson.copyRefFrom(this)
    return rowJson
}
fun Total.totalJson():TotalJson{
    val totalJson = TotalJson(pageId, cardId)
    totalJson.json = gson.toJson(this)
    totalJson.copyRefFrom(this)
    return totalJson
}

