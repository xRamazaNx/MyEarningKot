package ru.developer.press.myearningkot.helpers

import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import org.jetbrains.anko.backgroundColor

fun AlertDialog.setAlertButtonColors(colorPositiveRes: Int, colorNegativeRes: Int) {
    setOnShowListener {
        getButton(DialogInterface.BUTTON_POSITIVE).apply {
            backgroundColor = Color.TRANSPARENT
            setTextColor(context.getColorFromRes(colorPositiveRes))
        }
        getButton(DialogInterface.BUTTON_NEGATIVE).apply {
            backgroundColor = Color.TRANSPARENT
            setTextColor(context.getColorFromRes(colorNegativeRes))
        }
    }
}

fun android.app.AlertDialog.setAlertButtonColors(colorPositiveRes: Int, colorNegativeRes: Int) {
    setOnShowListener {
        getButton(DialogInterface.BUTTON_POSITIVE).apply {
            backgroundColor = Color.TRANSPARENT
            setTextColor(context.getColorFromRes(colorPositiveRes))
        }
        getButton(DialogInterface.BUTTON_NEGATIVE).apply {
            backgroundColor = Color.TRANSPARENT
            setTextColor(context.getColorFromRes(colorNegativeRes))
        }
    }
}