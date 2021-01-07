package ru.developer.press.myearningkot.helpers

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat

fun TextView.setFont(fontRes: Int, style: Int = Typeface.NORMAL) {
    val tf = ResourcesCompat.getFont(context, fontRes)
    setTypeface(tf, style)
}