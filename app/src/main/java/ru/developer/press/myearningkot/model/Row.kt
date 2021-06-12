package ru.developer.press.myearningkot.model

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import org.jetbrains.anko.backgroundColorResource
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.database.BelongIds

class Row(pageId: String, cardId: String) : BelongIds(pageId, cardId), Backgrounder {
    fun crossOut(itemView: View, isCrossOut: Boolean) {
        val frameLayout = itemView as FrameLayout
        if (isCrossOut) {
            val drawable = ContextCompat.getDrawable(itemView.context, R.drawable.cross_line)
            frameLayout.foreground = drawable
            itemView.backgroundColorResource = R.color.textColorSecondary
        } else {
            val colorDrawable = ColorDrawable(Color.TRANSPARENT)
            frameLayout.foreground = colorDrawable

        }
    }

    @Transient
    var status = Status.NONE

    @Transient
    override var currentBackground: Int = -1

    @Transient
    override lateinit var elementView: View

    var cellList = mutableListOf<Cell>()


    fun copy(): Row {
        return Row(pageId, cardId).also { copyRow ->
            copyRow.dateChange = dateChange
            copyRow.status = Status.NONE
            cellList.forEach { cell ->
                copyRow.cellList.add(cell.copy())
            }
        }
    }

    enum class Status {
        SELECT,
        DELETED,
        NONE,
        ADDED
    }
}