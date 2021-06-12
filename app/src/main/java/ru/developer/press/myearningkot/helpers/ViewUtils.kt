package ru.developer.press.myearningkot.helpers

import android.content.Context
import android.graphics.Typeface
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

fun TextView.setFont(fontRes: Int, style: Int = Typeface.NORMAL) {
    val tf = ResourcesCompat.getFont(context, fontRes)
    setTypeface(tf, style)
}

fun Context.getDrawableRes(id: Int) = ContextCompat.getDrawable(this, id)