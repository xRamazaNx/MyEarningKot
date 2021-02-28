package ru.developer.press.myearningkot.helpers.prefLayouts

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.widget.Switch
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.helpers.getColorFromRes


class MySwitch : Switch {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun setChecked(checked: Boolean) {
        super.setChecked(checked)
        changeColor(checked)
    }

    private fun changeColor(isChecked: Boolean) {
        val thumbColor: Int
        val trackColor: Int
        if (isChecked) {
            thumbColor = context.getColorFromRes(R.color.colorControlEnabled)
            trackColor = thumbColor
        } else {
            thumbColor = context.getColorFromRes(R.color.colorControlNormal)
            trackColor = context.getColorFromRes(R.color.textColorSecondary)
        }
        try {
            thumbDrawable.setColorFilter(thumbColor, PorterDuff.Mode.MULTIPLY)
            trackDrawable.setColorFilter(trackColor, PorterDuff.Mode.MULTIPLY)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }
}