package ru.developer.press.myearningkot.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.CropTransformation
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import ru.developer.press.myearningkot.ColumnTypeControl
import ru.developer.press.myearningkot.ProvideValueProperty
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.model.ImageTypePref
import ru.developer.press.myearningkot.model.SwitchTypePref
import ru.developer.press.myearningkot.model.TextTypePref


// общий класс для реализации cellType
open class TypeControlImpl(
    var provideValueProperty: ProvideValueProperty
) {
    var weight = 1f
    protected fun View.getLayoutParamOfCell(): ViewGroup.LayoutParams {
        backgroundResource = R.drawable.cell_default_background
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

    override fun display(view: View, value: String) {

        val textView = view as TextView
        val typePref = provideValueProperty.typePref as TextTypePref
        typePref.prefForTextView.customize(textView, R.font.roboto_light)
        textView.text = value
    }
}


class NumberTypeControl(
    provideValueProperty: ProvideValueProperty

) : TextTypeControl(provideValueProperty) {
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
                context.getDrawableRes(R.drawable.ic_drop_down),
                null
            )
            compoundDrawablePadding = context.dip(2)
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
                backgroundResource = R.drawable.cell_default_background
                padding = 8
                layoutParams = LayoutParams(
                    provideValueProperty.getWidthColumn(),
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply {
                    weight = 1f
                }
                val dip = context.dip(8)
                setPadding(dip, paddingTop, dip, paddingBottom)

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
                        this.maxHeight = context.dip(48)
                        this.maxWidth = context.dip(48)
                    }
                )
            }
    }

    override fun display(view: View, value: String) {
        val toBoolean = value.toBoolean()

        view.elevation = 5f
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
                image.setColorFilter(frame.context.getColorFromRes(R.color.colorControlEnabled))
            } else
                image.setColorFilter(frame.context.getColorFromRes(R.color.colorControlNormal))
        }
    }
}

class ImageTypeControl(
    provideValueProperty: ProvideValueProperty
) : TypeControlImpl(provideValueProperty), ColumnTypeControl {
    override fun createCellView(context: Context): View {
        // используем контейнер для фото что бы не было проблем с краями
        return FrameLayout(context).apply {
            layoutParams = getLayoutParamOfCell()
            val imageView = ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(matchParent, matchParent)
            }
            addView(imageView)

        }
    }

    @SuppressLint("CheckResult")
    override fun display(view: View, value: String) {
        // потом поработать над тем что бы получать изображение и показать это если файл не найден
        val imageTypePref = provideValueProperty.typePref as ImageTypePref


        val imageView = (view as FrameLayout).getChildAt(0) as ImageView
        Glide
            .with(view)
            .load("")
            .into(imageView)
        // через пост чтобы использовать размеры изображения для управления качеством в ячейке
        imageView.post {
            // если нет фото то игнор
            if (value.isNotEmpty()) {
                val glide = Glide
                    .with(view)
                    .load(value)
                    // если ошщибка в полуении изображений
                    .error(R.drawable.ic_image_error)
                if (imageTypePref.imageViewMode == 0) {
                    // если выбрано "поместить" то этого хватает
                    glide.fitCenter()
                } else {
                    // если выбрано обрезать
                    val width = imageView.width
                    val height = imageView.height
                    glide.apply(
                        // используем трансформацию (нужна библиотека для glide 'jp.wasabeef:glide-transformations:4.1.0')
                        RequestOptions.bitmapTransform(
                            // обрезаем под изображение
                            CropTransformation(
                                width,
                                height
                            )
                            // этот метод устанавливает качество и поэтому тут тоже выбрали ширину и высоту imageView
                        ).override(width, height)
                    )
                }
                glide.into(imageView)
            }
        }
    }

}

class ColorTypeControl(
    provideValueProperty: ProvideValueProperty
) : TypeControlImpl(provideValueProperty), ColumnTypeControl {
    override fun createCellView(context: Context): View {
        return FrameLayout(context).apply {
            layoutParams = getLayoutParamOfCell()

            addView(FrameLayout(context).apply {
                padding = context.dip(4)
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


