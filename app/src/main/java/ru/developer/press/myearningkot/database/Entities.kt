package ru.developer.press.myearningkot.database

import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.google.firebase.firestore.Exclude
import ru.developer.press.myearningkot.JsonValue
import ru.developer.press.myearningkot.ProvideCardPropertyForCell
import ru.developer.press.myearningkot.helpers.getDate
import ru.developer.press.myearningkot.helpers.scoups.addColumn
import ru.developer.press.myearningkot.helpers.scoups.addTotal
import ru.developer.press.myearningkot.model.*
import java.util.*

open class Ref {
    @PrimaryKey
    var refId: String = UUID.randomUUID().toString()
    val dateCreate = System.currentTimeMillis()
    var dateChange: Long = dateCreate
}

@Entity
class PageRef(
    var name: String = ""
) : Ref() {
    var position = 0

    @Exclude
    @Ignore
    val cards = mutableListOf<MutableLiveData<CardRef>>()
}

@Entity(
    foreignKeys = [ForeignKey(
        entity = PageRef::class,
        parentColumns = arrayOf("refId"),
        childColumns = arrayOf("pageId"),
        onDelete = ForeignKey.CASCADE
    )]
)
open class CardRef(var pageId: String, var name: String = "") : Ref(), ProvideCardPropertyForCell {

    @Embedded(prefix = "card_pref")
    val cardPref = PrefForCard()

    @Embedded(prefix = "sort_pref")
    val sortPref = SortPref()
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
    var columns = mutableListOf<Column>()

    @Ignore
    @Exclude
    var totals = mutableListOf<Total>()

    val dateOfPeriod: String
        get() {
            val variantDate = cardPref.dateOfPeriodPref.type
            val enableTime = cardPref.dateOfPeriodPref.enableTime
            val first = getDate(variantDate, dateCreate, enableTime)
            val last = getDate(variantDate, dateChange, enableTime)
            return "$first - $last"
        }

    init {
        addColumn(ColumnType.NUMERATION, "№").apply {
            width = 70
        }
        addTotal()
    }

    override fun isSingleLine(): Boolean = !enableSomeStroke

    override fun getValutaType(): Int = valuta
}

open class IdsRef(
    var pageId: String,
    var cardId: String
) : Ref()

@Entity(
    foreignKeys = [ForeignKey(
        entity = CardRef::class,
        parentColumns = arrayOf("refId"),
        childColumns = arrayOf("cardId"),
        onDelete = ForeignKey.CASCADE
    )]
)
open class RowRef(pageId: String, cardId: String) : JsonValue, IdsRef(pageId, cardId) {
    override var json: String = ""

}

@Entity(
    foreignKeys = [ForeignKey(
        entity = CardRef::class,
        parentColumns = arrayOf("refId"),
        childColumns = arrayOf("cardId"),
        onDelete = ForeignKey.CASCADE
    )]
)
open class ColumnRef(
    val columnClass: Class<out Column>,
    pageId: String,
    cardId: String
) : JsonValue, IdsRef(pageId, cardId) {
    override var json: String = ""
}

@Entity(
    foreignKeys = [ForeignKey(
        entity = CardRef::class,
        parentColumns = arrayOf("refId"),
        childColumns = arrayOf("cardId"),
        onDelete = ForeignKey.CASCADE
    )]
)
class TotalRef(pageId: String, cardId: String) : JsonValue, IdsRef(pageId, cardId) {
    override var json: String = ""
}

@Entity
class ListTypeJson : JsonValue, Ref() {
    override var json: String = ""
}