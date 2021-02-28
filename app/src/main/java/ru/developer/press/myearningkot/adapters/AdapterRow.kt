package ru.developer.press.myearningkot.adapters

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent
import ru.developer.press.myearningkot.ProvideDataRows
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.RowClickListener
import ru.developer.press.myearningkot.RowDataListener
import ru.developer.press.myearningkot.helpers.animateColor
import ru.developer.press.myearningkot.helpers.getColorFromRes
import ru.developer.press.myearningkot.helpers.prefLayouts.setSelectBackground
import ru.developer.press.myearningkot.model.Column
import ru.developer.press.myearningkot.model.Row
import ru.developer.press.myearningkot.model.SwitchColumn

class AdapterRecyclerInCard(
    private var rowClickListener: RowClickListener?,
    private val provideDataRows: ProvideDataRows,
    private val totalView: View?
) : RecyclerView.Adapter<RowHolder>() {
    private var cellClickPrefFunction: ((Int) -> Unit)? = null
    fun setCellClickPref(cellClickFun: ((Int) -> Unit)?) {
        cellClickPrefFunction = cellClickFun
    }

    fun setCellClickListener(_rowClickListener: RowClickListener?) {
        rowClickListener = _rowClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowHolder {
        val context = parent.context

        if (viewType == -1)
            return RowHolder(FrameLayout(context).apply {
                backgroundColorResource = R.color.colorPrimaryDark
            })
        val width = provideDataRows.getWidth()

        val rowHeight = context.dip(provideDataRows.getRowHeight())
        val rowView = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                width,
                if (provideDataRows.isEnableSomeStroke()) {
                    minimumHeight = rowHeight
                    wrapContent
                } else rowHeight
            )
            orientation = LinearLayout.HORIZONTAL
        }
        val rowHolder = RowHolder(FrameLayout(context).apply {
            addView(rowView)
        })

        provideDataRows.getColumns().forEachIndexed { index, column ->
            val cellView: View = column.createCellView(context)
            if (index == 0)
                rowHolder.rowNumber = cellView as TextView

            rowView.addView(cellView)
            // держим все вью в листе для удобства использования
            rowHolder.viewList.add(cellView)
        }

        // нажатие для настройки колоны
        return rowHolder.apply {
            viewList.forEachIndexed { columnIndex, view ->
                if (cellClickPrefFunction != null) {
                    view.setOnClickListener {
                        cellClickPrefFunction!!.invoke(columnIndex)
                    }
                } else {
                    if (columnIndex > 0) {
                        rowClickListener?.let {
                            view.setOnClickListener { view ->
                                it.cellClick(view, adapterPosition, columnIndex)
                            }
                        }
                    }

                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val size = provideDataRows.sortedRows.size
        return if (size == position) -1 else 0
    }

    override fun getItemCount(): Int {
        return provideDataRows.sortedRows.size + 1 // отступ от тотал
    }

    override fun onBindViewHolder(holder: RowHolder, position: Int) {
        if (holder.itemViewType == -1) {
            holder.itemView.layoutParams =
                FrameLayout.LayoutParams(matchParent, totalView?.height ?: 0)
            return
        }

        val itemView = holder.itemView
        val context = itemView.context
        val dip = context.dip(8)
        val dip2 = context.dip(2)
        if (position == itemCount - 1) {
//            itemView.setPadding(dip, 0, dip, dip2)
        } else {
//            itemView.setPadding(dip, dip2, dip, 0)

        }
        val sortedRows = provideDataRows.sortedRows
        val previousRow = if (position > 0) sortedRows[position - 1] else null
        val secondRow = if (position < sortedRows.lastIndex) sortedRows[position + 1] else null
        holder.bind(
            sortedRows[position],
            provideDataRows.getColumns(),
            previousRow,
            secondRow
        )
    }

}

lateinit var animationDelete: Animation
lateinit var animationAdd: Animation

class RowHolder(view: View) : DragDropSwipeAdapter.ViewHolder(view), RowDataListener {
    var viewList = mutableListOf<View>()
    var rowNumber: TextView? = null
    var rowLayout: LinearLayout? = null
    private var positionRow = 0

    fun bind(
        row: Row,
        columns: MutableList<Column>,
        previousRow: Row?,
        secondRow: Row?
    ) {
        if (itemViewType == -1) {
            return
        }
        val context = itemView.context

        positionRow = adapterPosition
        row.cellList.forEachIndexed { index, cell ->
            val cellView: View = viewList[index]
            cell.displayCellView(cellView)

            when {
                // колона выделена- обвести
                cell.isPrefColumnSelect -> {
                    setSelectBackground(cellView)
                }
                // ячейка выделена - обвести
                cell.isSelect -> {
                    cell.setBackground(cellView, R.drawable.cell_selected_background)
                }
                else ->
                    cell.setBackground(cellView, R.drawable.cell_default_background)
            }
        }

        rowNumber?.text = (layoutPosition + 1).toString()

        when (row.status) {
            Row.Status.SELECT -> {
//                rowNumber?.text = "✔"
                rowNumber?.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorSecondary
                    )
                )

                val isPrevSelect = previousRow?.status == Row.Status.SELECT
                val isSecondSelect = secondRow?.status == Row.Status.SELECT
                if (isPrevSelect && isSecondSelect)
                    row.setBackground(itemView, R.drawable.row_selected_background_border)
                else if (isPrevSelect) {
                    row.setBackground(itemView, R.drawable.row_selected_background_bottom)
                } else if (isSecondSelect) {
                    row.setBackground(itemView, R.drawable.row_selected_background_top)
                } else {
                    row.setBackground(itemView, R.drawable.row_selected_background)
                }
            }
            Row.Status.ADDED -> {
                itemView.startAnimation(animationAdd)
                itemView.animateColor(
                    context.getColorFromRes(R.color.colorSecondaryLight),
                    Color.TRANSPARENT,
                    700
                )
                row.status = Row.Status.NONE
                bind(row, columns, previousRow, secondRow)
            }
            Row.Status.DELETED -> {
                itemView.startAnimation(animationDelete)
                itemView.animateColor(Color.TRANSPARENT, Color.RED, 700)
            }
            else -> {
                // если есть хоть один свитч который настроен на поведение меняющее запись
                row.cellList.forEachIndexed { index, cell ->
                    val column = columns[index]
                    if (column is SwitchColumn) {
                        val behavior = column.typePref.behavior
                        row.crossOut(itemView, behavior.crossOut && cell.sourceValue.toBoolean())
                    }
                }

                row.setBackground(itemView, R.color.colorBackgroundCard)
            }
        }
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
