package ru.developer.press.myearningkot.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.wrapContent
import ru.developer.press.myearningkot.*
import ru.developer.press.myearningkot.model.Cell
import ru.developer.press.myearningkot.model.Column
import ru.developer.press.myearningkot.model.Row
import ru.developer.press.myearningkot.model.SwitchColumn
import ru.developer.press.myearningkot.otherHelpers.PrefLayouts.setSelectBackground

class AdapterRecyclerInCard(
    private var rowClickListener: RowClickListener?,
    private val provideDataRows: ProvideDataRows,
    list: MutableList<Row>
) : DragDropSwipeAdapter<Row, RowHolder>(list) {
    private var cellClickPrefFunction: ((Int) -> Unit)? = null
    fun setCellClickPref(cellClickFun: ((Int) -> Unit)?) {
        cellClickPrefFunction = cellClickFun
    }

    fun setCellClickListener(_rowClickListener: RowClickListener?){
        rowClickListener = _rowClickListener
    }
    private val rowNumberScrollListenerList = mutableSetOf<RowDataListener>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowHolder {
        val context = parent.context
        val width = provideDataRows.getWidth()

        val rowHeight = context.dpsToPixels(provideDataRows.getRowHeight())
        val rowView = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(

                width,
                if (provideDataRows.isEnableSomeStroke()) wrapContent else rowHeight
            )
            orientation = LinearLayout.HORIZONTAL
        }
        val rowHolder = RowHolder(FrameLayout(context).apply {
            addView(rowView)
        })

        provideDataRows.getColumns().forEachIndexed { index, column ->
            // ячейка равно...
            val cellView = column.createCellView(context)
            if (index == 0)
                rowHolder.rowNumber = cellView as TextView

            rowView.addView(cellView)
            // держим все вью в листе для удобства использования
            rowHolder.viewList.add(cellView)
        }

        // нажатие для настройки колоны
        return rowHolder.apply {
            viewList.forEachIndexed { columnIndex, view ->
                view.setOnClickListener {
                    if (cellClickPrefFunction != null) {
                        cellClickPrefFunction!!.invoke(columnIndex)
                    } else {
                        rowClickListener?.cellClick(adapterPosition, columnIndex)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return provideDataRows.getSize()
    }

    fun notifyAdapter() {
//        rowNumberScrollListenerList.forEachIndexed { index, rowHolder ->
//            rowHolder.bind(dataSet[index])
//        }
        rowNumberScrollListenerList.clear()
        notifyDataSetChanged()

    }

    override fun getViewHolder(itemView: View): RowHolder = RowHolder(itemView)

    override fun getViewToTouchToStartDraggingItem(
        item: Row,
        viewHolder: RowHolder,
        position: Int
    ): View? {
        return null
    }

    override fun onBindViewHolder(item: Row, viewHolder: RowHolder, position: Int) {
        rowNumberScrollListenerList.add(viewHolder)
        viewHolder.bind(item.cellList, provideDataRows.getColumns())
    }

//    fun clickEvent(y: Float, isLongClick: Boolean) {
//        var height = 0
//        rowNumberScrollListenerList.forEachIndexed { index, rowDataListener ->
//            height += rowDataListener.getItemHeight()
//            if (y < height) {
//                if (isLongClick)
//                    rowClickListener.rowLongClick(index)
//                else {
//                    rowClickListener.cellClick(index,)
//                }
//
//                return
//            }
//
//        }
//
//    }
}

class RowHolder(view: View) : DragDropSwipeAdapter.ViewHolder(view), RowDataListener {
    var viewList = mutableListOf<View>()
    var rowNumber: TextView? = null

    fun bind(
        row: MutableList<Cell>,
        columns: MutableList<Column>
    ) {

        row.forEachIndexed { index, cell ->
            val cellView = viewList[index]
            cell.displayCellView(cellView)

            // если есть хоть один свитч который настроен на поведение меняющее запись

            val column = columns[index]
            if (column is SwitchColumn) {
                val behavior = column.typePref.behavior
                val frameLayout = itemView as FrameLayout
                if (behavior.crossOut && cell.sourceValue.toBoolean()) {
                    frameLayout.foreground = itemView.context.getDrawable(R.drawable.cross_line)
                } else {
                    frameLayout.foreground = ColorDrawable(Color.TRANSPARENT)
                }
            }
            // колона выделена- обвести
            when {
                cell.isPrefColumnSelect -> {
                    setSelectBackground(cellView)
                }
                cell.isSelect -> {
                    cellView.backgroundResource = R.drawable.shape_select

                }
                else -> cellView.backgroundResource = R.drawable.shape
            }
        }

        rowNumber?.text = (layoutPosition + 1).toString()

    }

    override fun scrollRowNumber(x: Float) {
        rowNumber?.translationX = x

//        if (rowNumber!!.x < 0)
//            rowNumber?.translationX = 0f
    }

    override fun getItemHeight(): Int {
        return itemView.height
    }


}
