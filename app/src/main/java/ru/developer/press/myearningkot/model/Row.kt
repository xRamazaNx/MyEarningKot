package ru.developer.press.myearningkot.model

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import org.jetbrains.anko.backgroundColorResource
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.database.IdsRef
import java.util.*

class Row(pageId:String, cardId:String) : IdsRef(pageId, cardId), Backgrounder {
    var status = Status.NONE
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

    override var currentBackground: Int = -1
    override lateinit var elementView: View
    var cellList = mutableListOf<Cell>()


    fun copy(): Row {
        return Row(pageId, cardId).apply {
            this.dateChange = this@Row.dateChange
            this.status = Status.NONE
            this@Row.cellList.forEach {
                cellList.add(it.copy())
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