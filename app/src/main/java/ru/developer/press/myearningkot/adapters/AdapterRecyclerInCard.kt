package ru.developer.press.myearningkot.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import org.jetbrains.anko.*
import ru.developer.press.myearningkot.*
import ru.developer.press.myearningkot.model.Column
import ru.developer.press.myearningkot.model.NumerationColumn
import ru.developer.press.myearningkot.model.Row
import ru.developer.press.myearningkot.model.SwitchColumn
import ru.developer.press.myearningkot.helpers.prefLayouts.setSelectBackground

class AdapterRecyclerInCard(
    private var rowClickListener: RowClickListener?,
    private val provideDataRows: ProvideDataRows,
    private val plate: View
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
                backgroundColor = Color.TRANSPARENT
            })
        val width = provideDataRows.getWidth()

        val rowHeight = context.dpsToPixels(provideDataRows.getRowHeight())
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
                if (cellClickPrefFunction != null) {
                    view.setOnClickListener {
                        cellClickPrefFunction!!.invoke(columnIndex)
                    }
                } else {
                    if (columnIndex > 0) {

                        view.setOnClickListener {
                            rowClickListener?.cellClick(it, adapterPosition, columnIndex)
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
            holder.itemView.layoutParams = FrameLayout.LayoutParams(matchParent, plate.height)
            return
        }
        holder.bind(provideDataRows.sortedRows[position], provideDataRows.getColumns())
    }

}

val animationDelete: Animation =
    AnimationUtils.loadAnimation(App.instance?.baseContext, R.anim.anim_delete)
val animationAdd: Animation = AnimationUtils.loadAnimation(App.instance?.baseContext, R.anim. anim_add)

class RowHolder(view: View) : DragDropSwipeAdapter.ViewHolder(view), RowDataListener {
    var viewList = mutableListOf<View>()
    var rowNumber: TextView? = null
    private var positionRow = 0

    fun bind(
        row: Row,
        columns: MutableList<Column>
    ) {
        if (itemViewType == -1) {
            return
        }
        positionRow = adapterPosition
        row.cellList.forEachIndexed { index, cell ->
            val cellView = viewList[index]
            cell.displayCellView(cellView)

            when {
                // колона выделена- обвести
                cell.isPrefColumnSelect -> {
                    setSelectBackground(cellView)
                }
                // ячейка выделена - обвести
                cell.isSelect -> {
                    cellView.backgroundResource = R.drawable.shape_cell_select
                }
                else ->
                    cellView.backgroundResource = R.drawable.shape

            }
        }

        rowNumber?.text = (layoutPosition + 1).toString()
        rowNumber?.setTextColor((columns[0] as NumerationColumn).typePref.prefForTextView.color)

        when (row.status) {
            Row.Status.SELECT -> {
                rowNumber?.text = "✔"
                //            rowNumber?.text = ""
                rowNumber?.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.shape_select_border
                    )
                )
                //            itemView.backgroundColorResource = R.color.color_long_click_item
                itemView.backgroundResource = R.drawable.shape_select
                //            itemView.backgroundColorResource = R.color.shape_select_solid
                //            rowNumber?.backgroundResource = R.drawable.ic_check_ring
            }
            Row.Status.ADDED -> {
                itemView.startAnimation(animationAdd)
                row.status = Row.Status.NONE
                bind(row, columns)
            }
            Row.Status.DELETED -> {
                itemView.backgroundColorResource = R.color.md_red_200
                itemView.startAnimation(animationDelete)
            }
            else -> {
                // если есть хоть один свитч который настроен на поведение меняющее запись
                row.cellList.forEachIndexed { index, cell ->
                    val column = columns[index]
                    if (column is SwitchColumn) {
                        val behavior = column.typePref.behavior
                        val frameLayout = itemView as FrameLayout
                        if (behavior.crossOut && cell.sourceValue.toBoolean()) {
                            frameLayout.foreground =
                                itemView.context.getDrawable(R.drawable.cross_line)
                            itemView.backgroundColorResource = R.color.light_gray_opacity
                        } else {
                            frameLayout.foreground = ColorDrawable(Color.TRANSPARENT)
                        }
                    }
                }
                itemView.backgroundColor = Color.TRANSPARENT
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
