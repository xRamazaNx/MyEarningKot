package ru.developer.press.myearningkot

import android.content.Context
import android.os.SystemClock.uptimeMillis
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_card.view.*


class ScrollContainer(
    context: Context, attributeSet: AttributeSet
) : LinearLayout(context, attributeSet) {
    private lateinit var motionEventFromActionDown: MotionEvent
    var dragNdropMode: Boolean = false
    private var isMove: Boolean = false


    init {
        horizontalScrollView?.isSmoothScrollingEnabled = true

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev!!.action == MotionEvent.ACTION_MOVE) {
            isMove = true
        }
        if (ev.action == MotionEvent.ACTION_DOWN) {
            motionEventFromActionDown = MotionEvent.obtain(ev)
            isMove = false
        }
        // при прикосновении 2 пальцами происходит ошибка pointerIndex out of range
        if (ev.pointerCount == 1 && !isMove && ev.action == MotionEvent.ACTION_UP) {
                super.dispatchTouchEvent(motionEventFromActionDown)
                super.dispatchTouchEvent(ev)

        } else {
            recycler.onTouchEvent(ev)
            columnDisableScrollContainer.onTouchEvent(ev)
            columnScrollContainer.onTouchEvent(ev)
        }
        horizontalScrollView.onTouchEvent(ev)
        return true
    }

}

class HorScrollView(context: Context, attributeSet: AttributeSet) :
    HorizontalScrollView(context, attributeSet) {
    private var moveRowNumber: ((Int) -> Unit)? = null

    override fun scrollBy(x: Int, y: Int) {
        super.scrollBy(x, y)
        moveRowNumber?.invoke(scrollX)
    }

    override fun scrollTo(x: Int, y: Int) {
        super.scrollTo(x, y)
        moveRowNumber?.invoke(scrollX)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        moveRowNumber?.invoke(scrollX)
    }

}

fun logD(valueString: String = "просто проверка") {
    Log.d("log", valueString)
}


//
//
//
//
//
//
//

