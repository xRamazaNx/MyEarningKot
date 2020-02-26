package ru.developer.press.myearningkot.otherHelpers

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.padding
import ru.developer.press.myearningkot.ColumnTypeControl
import ru.developer.press.myearningkot.ProvideValueProperty
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.dpsToPixels
import ru.developer.press.myearningkot.model.*
import java.util.*


// общий класс для реализации cellType
open class TypeControlImpl(
    var provideValueProperty: ProvideValueProperty
) {
    var weight = 1f
    protected fun View.getLayoutParamOfCell(): ViewGroup.LayoutParams {
        backgroundResource = R.drawable.shape
        padding = 8
        return LayoutParams(
            provideValueProperty.getWidthColumn(),
            MATCH_PARENT
        ).apply {
            gravity = CENTER
            weight = this@TypeControlImpl.weight
        }
    }

    protected fun TextView.settingTextView() {
        gravity = CENTER
        if (provideValueProperty.provideCardPropertyForCell.isSingleLine()) {
            isSingleLine = true
            maxLines = 1
        } else {
            isSingleLine = false
        }
        ellipsize = TextUtils.TruncateAt.END
        layoutParams = getLayoutParamOfCell()
    }

}

// реализация для тех у кого textview
open class TextTypeControl(
    provideValueProperty: ProvideValueProperty
) : TypeControlImpl(provideValueProperty), ColumnTypeControl {

    override fun display(view: View, value: String) {
        val textView = view as TextView
        val typePref = provideValueProperty.typePref as TextTypePref
        typePref.prefForTextView.customize(textView)
        textView.text = value
    }

    override fun createCellView(context: Context): View {
        return TextView(context).apply {
            settingTextView()
        }
    }

}


class NumerationTypeControl(
    provideValueProperty: ProvideValueProperty
) : TextTypeControl(provideValueProperty) {
    override fun createCellView(context: Context): View {
        weight = 0F
        return super.createCellView(context)
    }
}


class NumberTypeControl(
    provideValueProperty: ProvideValueProperty

) : TextTypeControl(provideValueProperty){
    override fun display(view: View, value: String) {
        super.display(view, value)
    }
}

class DateTypeControl(
    provideValueProperty: ProvideValueProperty

) : TextTypeControl(provideValueProperty)

class PhoneTypeControl(
    provideValueProperty: ProvideValueProperty
) : TextTypeControl(provideValueProperty)

class ListTypeControl(
    provideValueProperty: ProvideValueProperty
) : TextTypeControl(provideValueProperty) {
    override fun createCellView(context: Context): View {
        return (super.createCellView(context) as TextView).apply {
            setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(context, R.drawable.ic_drop_down_dark),
                null
            )
            compoundDrawablePadding = context.dpsToPixels(2)
        }

    }
}

class SwitchTypeControl(
    provideValueProperty: ProvideValueProperty
) : TextTypeControl(provideValueProperty) {


    override fun createCellView(context: Context): View {
        val typePref = provideValueProperty.typePref as SwitchTypePref
        return if (typePref.isTextSwitchMode)
            TextView(context).apply {
                settingTextView()
            }
        else
            FrameLayout(context).apply {
                backgroundResource = R.drawable.shape
                padding = 8
                layoutParams = LayoutParams(
                    provideValueProperty.getWidthColumn(),
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply {
                    weight = 1f
                }
                val dpsToPixels = context.dpsToPixels(8)
                setPadding(dpsToPixels, paddingTop, dpsToPixels, paddingBottom)

//                addView(
//                    Switch(context).apply {
//                        layoutParams = FrameLayout.LayoutParams(
//                            ViewGroup.LayoutParams.WRAP_CONTENT,
//                            ViewGroup.LayoutParams.WRAP_CONTENT
//                        ).apply {
//                            gravity = CENTER
//                        }
//                        isEnabled = false
//                    }
//                )
                addView(
                    ImageView(context).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            gravity = CENTER
                        }
                        setImageResource(R.drawable.ic_check)
                        this.adjustViewBounds = true
                        this.maxHeight = context.dpsToPixels(48)
                        this.maxWidth = context.dpsToPixels(48)
                    }
                )
            }
    }

    override fun display(view: View, value: String) {
        val toBoolean = value.toBoolean()

        val typePref = provideValueProperty.typePref as SwitchTypePref
        if (typePref.isTextSwitchMode) {
            val textView = view as TextView
            if (toBoolean) {
                textView.text = typePref.textEnable
                typePref.enablePref.customize(textView)
            } else {
                textView.text = typePref.textDisable
                typePref.disablePref.customize(textView)
            }
        } else {
            val frame = view as FrameLayout
            val image = frame.getChildAt(0) as ImageView
            if (toBoolean) {
                image.setColorFilter(Color.BLACK)
            } else
                image.setColorFilter(Color.LTGRAY)
        }
    }
}

class ImageTypeControl(
    provideValueProperty: ProvideValueProperty
) : TypeControlImpl(provideValueProperty), ColumnTypeControl {
    override fun createCellView(context: Context): View {
        return ImageView(context).apply {
            layoutParams = getLayoutParamOfCell()
        }
    }

    override fun display(view: View, value: String) {
        // потом поработать над тем что бы получать изображение и показать это если файл не найден
        Glide
            .with(view)
            .load(Uri.parse(value))
            .fitCenter()
            .into(view as ImageView)

    }

}

class ColorTypeControl(
    provideValueProperty: ProvideValueProperty
) : TypeControlImpl(provideValueProperty), ColumnTypeControl {
    override fun createCellView(context: Context): View {
        return FrameLayout(context).apply {
            layoutParams = getLayoutParamOfCell()

            addView(FrameLayout(context).apply {
                padding = context.dpsToPixels(4)
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            })
        }
    }

    override fun display(view: View, value: String) {
        val frameLayout = view as FrameLayout
        frameLayout.getChildAt(0).background = ColorDrawable(value.toInt())
    }

}


