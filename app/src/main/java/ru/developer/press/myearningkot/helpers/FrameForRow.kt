package ru.developer.press.myearningkot.helpers

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.widget.FrameLayout
import ru.developer.press.myearningkot.R

class FrameForRow(context: Context) : FrameLayout(context){
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(7f, 7f), 15f)
        color = context.getColorFromRes(R.color.cent_opacity)
        strokeWidth = 3f
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val y = (height / 2).toFloat()
        canvas?.drawLine(0f, 50f, 300f, 60f, paint)
    }
}