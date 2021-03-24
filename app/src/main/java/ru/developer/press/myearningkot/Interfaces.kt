package ru.developer.press.myearningkot

//import ru.developer.press.myearningkot.model.card
import android.content.Context
import android.view.View
import ru.developer.press.myearningkot.database.Card
import ru.developer.press.myearningkot.database.Page
import ru.developer.press.myearningkot.helpers.MyLiveData
import ru.developer.press.myearningkot.model.Column
import ru.developer.press.myearningkot.model.Prefs
import ru.developer.press.myearningkot.model.Row

// для viewPage обратная связь реализатор PageViewModel
interface AdapterPageInterface {
    fun getPageCount(): Int
    fun getPages(): MutableList<MyLiveData<Page>>
}

interface ProvideData {
    fun getSize(): Int
}

// с помошью него всегда можно получить данные для списка карточек реализатор PageViewModel
interface ProvideDataCards : ProvideData {
    //  дать инфу о карточке в текущей странице
    fun getCard(position: Int): Card
    // колличество карточек в текущей странице

}

// для получения данных списка записей реализатор CardViewModel
interface ProvideDataRows {
    val sortedRows: MutableList<Row>

    fun getColumns(): MutableList<Column>

    // отдает ширину или мачпарент или ширину дисплея
    fun getWidth(): Int

    fun isEnableHorizontalScroll(): Boolean
    fun isEnableSomeStroke(): Boolean
    fun getRowHeight(): Int
    fun getSelectCellPairIndexes(): Pair<Int, Int>?

}

interface ProvideValueProperty {
    fun getWidthColumn(): Int
    var provideCardPropertyForCell: ProvideCardPropertyForCell
    var typePref: Prefs?
}

interface RowClickListener {
    fun cellClick(rowPosition: Int, cellPosition: Int)
}

interface NotifyCallback {
    fun setPositionPage(index: Int)
    fun scrollToPosition(indexCard: Int)
    fun isRecyclerInPageScrolled(): Boolean
}

interface RowDataListener {
    fun scrollRowNumber(x: Float)
    fun getItemHeight(): Int
}

interface CellTypeControl {
    fun display(view: View, value: String)
}

interface ColumnTypeControl : CellTypeControl {
    fun createCellView(context: Context): View
    override fun display(view: View, value: String)
}

interface ProvideCardPropertyForCell {
    fun isSingleLine(): Boolean
    fun getValutaType(): Int
}

interface FormulaId {
    var idToFormula: Long
}
