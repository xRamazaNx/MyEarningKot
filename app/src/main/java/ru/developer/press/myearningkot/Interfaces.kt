package ru.developer.press.myearningkot

import android.content.Context
import android.view.View
import androidx.lifecycle.MutableLiveData
import ru.developer.press.myearningkot.model.*
//import ru.developer.press.myearningkot.model.card
import ru.developer.press.myearningkot.helpers.Page

// для viewPage обратная связь реализатор PageViewModel
interface AdapterPageInterface {
    fun getPageCount(): Int
    fun getPages(): MutableList<MutableLiveData<Page>>
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
interface ProvideDataRows{
    val sortedRows: MutableList<Row>

    fun getColumns(): MutableList<Column>
    // отдает ширину или мачпарент или ширину дисплея
    fun getWidth(): Int

    fun isEnableHorizontalScroll(): Boolean
    fun isEnableSomeStroke(): Boolean
    fun getRowHeight(): Int

}

interface ProvideValueProperty {
    fun getWidthColumn():Int
    var provideCardPropertyForCell :ProvideCardPropertyForCell
    var typePref : Prefs?
}


// listeners
interface CardClickListener {
    fun cardClick(idCard: Long)
}

interface RowClickListener {
    fun cellClick(view: View, rowPosition: Int, cellPosition: Int)
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

interface ProvideCardPropertyForCell{
    fun isSingleLine ():Boolean
    fun getValutaType():Int
}


