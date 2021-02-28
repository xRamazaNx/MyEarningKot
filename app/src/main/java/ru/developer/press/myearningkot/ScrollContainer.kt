package ru.developer.press.myearningkot

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.activity_card.view.*


class ScrollContainer(
    context: Context, attributeSet: AttributeSet
) : LinearLayout(context, attributeSet) {
    private lateinit var motionEventFromActionDown: MotionEvent
    var dragNdropMode: Boolean = false
    private var isMove: Boolean = false
    private var moveSize = 0f
    val isLong = MutableLiveData<Boolean>().apply {
        value = false
    }

    private val hand = Handler()
    private var mLongPressed = Runnable {
        isLong.value = true
        clickEvent(MotionEvent.obtain(motionEventFromActionDown).apply {
            action = MotionEvent.ACTION_UP
        })
    }

    init {
        horizontalScrollView?.isSmoothScrollingEnabled = true

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev == null)
            return false
//        val newSize = ev.x

        if (ev.action == MotionEvent.ACTION_MOVE) {
            isMove =
                // диапозон который используется для определения движения пальца
                !(ev.x - motionEventFromActionDown.x in -10f..10f && ev.y - motionEventFromActionDown.y in -10f..10f)
        }
        if (ev.action == MotionEvent.ACTION_DOWN) {
            isLong.value = false
            isMove = false
            motionEventFromActionDown = MotionEvent.obtain(ev)
            hand.postDelayed(mLongPressed, ViewConfiguration.getLongPressTimeout().toLong())
        }
        if (isMove || ev.action == MotionEvent.ACTION_UP) {
            hand.removeCallbacks(mLongPressed)
        }
//        logD("clickEvent x = ${ev.x} ")
//        logD("clickEvent y = ${ev.y} ")
//
//        logD("clickEvent x = ${motionEventFromActionDown.x} ")
//        logD("clickEvent y = ${motionEventFromActionDown.y} ")
//        logD("clickEvent !isMove = ${!isMove} ")
//        logD("clickEvent ev.pointerCount = ${ev.pointerCount} ")
//        logD("clickEvent ev.action = ${ev.action} ")
//        logD("clickEvent \n ///////////////////////////////////// \n ")

        // при прикосновении 2 пальцами происходит ошибка pointerIndex out of range
        if (!isMove && ev.pointerCount == 1 && ev.action == MotionEvent.ACTION_UP) {
            if (!isLong.value!!)
                clickEvent(ev)

        } else {
            recycler.onTouchEvent(ev)
            columnDisableScrollContainer.onTouchEvent(ev)
            columnScrollContainer.onTouchEvent(ev)
        }
        horizontalScrollView.onTouchEvent(ev)
        return true
    }

    private fun clickEvent(ev: MotionEvent?) {
        super.dispatchTouchEvent(motionEventFromActionDown)
        super.dispatchTouchEvent(ev)
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

