package ru.developer.press.myearningkot.helpers

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.text.format.DateFormat
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.list_item_change_layout.view.*
import kotlinx.coroutines.*
import org.jetbrains.anko.*
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.model.Column
import ru.developer.press.myearningkot.model.NumberTypePref
import ru.developer.press.myearningkot.model.NumerationColumn
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

abstract class MyLiveData<T>(t: T?) : MutableLiveData<T>() {
    init {
        t?.let {
            value = it
        }
    }

    fun updateValue() {
        postValue(value)
    }
}

fun <T> liveData(t: T? = null): MyLiveData<T> {
    return object : MyLiveData<T>(t) {}
}

fun <T> observer(changed: (T) -> Unit): Observer<T> {
    return object : Observer<T> {
        override fun onChanged(t: T?) {
            t?.let { changed.invoke(it) }
        }

    }
}


fun <T> singleObserver(changed: (T) -> Unit): Observer<T> {
    return object : Observer<T> {
        private var isFirst = true
        private var oldValue: T? = null
        override fun onChanged(t: T?) {
            if (isFirst) {
                isFirst = false
                return
            }
            if (oldValue === t)
                return
            t?.let { changed.invoke(it) }
            oldValue = t
        }

    }
}

class SingleLiveEvent<T> : MutableLiveData<T>() {

    private val mPending = AtomicBoolean(false)

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {

        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registered but only one will be notified of changes.")
        }

        // Observe the internal MyLiveData
        super.observe(owner, { t ->
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    @MainThread
    override fun setValue(t: T?) {
        mPending.set(true)
        super.setValue(t)
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    fun call(id: T) {
        setValue(id)
    }

    fun call() {
        value = null
    }

    companion object {

        private const val TAG = "SingleLiveEvent"
    }
}

fun getValutaTypeList(context: Context): MutableList<String> {
    return context.resources.getStringArray(R.array.valuta_list).toMutableList()

}

fun getDateTypeList(): MutableList<String> {
    return mutableListOf<String>().apply {
        add(getDate(0, enableTime = false))
        add(getDate(1, enableTime = false))
        add(getDate(2, enableTime = false))
    }
}


fun getDate(variantDate: Int, time: Long = Date().time, enableTime: Boolean): String {
//    val sDayOfWeek = arrayOf("вс, ", "пн, ", "вт, ", "ср, ", "чт, ", "пт, ", "сб, ")

    val calendar = Calendar.getInstance().apply {
        timeInMillis = time
    }
//    val dayName: String = sDayOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1]
    var timeFormat = ""
    when (variantDate) {
        0 -> timeFormat = "dd.MM.yy"
        1 -> timeFormat = "dd.MM.yyyy"
        2 -> timeFormat = "dd MMMM yyyy"
    }
    if (enableTime)
        timeFormat += " hh:mm"
    return DateFormat.format(timeFormat, calendar.time).toString()
}


fun bindTitleOfColumn(column: Column, title: TextView) {
    var w = 1f
    if (column is NumerationColumn) {
        w = 0f
    }
    val width = column.width

    title.layoutParams =
        LinearLayout.LayoutParams(
            width,
            title.dip(35)
        ).apply {
            gravity = Gravity.CENTER
            weight = w
        }

    title.text = column.name
    column.titlePref.customize(title)
}

@SuppressLint("InflateParams")
fun Context.showItemChangeDialog(
    title: String,
    list: MutableList<String>,
    _selectItem: Int,
    firstElementText: String?,
    itemClickEvent: (Int) -> Unit
) {
    val builder = AlertDialog.Builder(this).create()
    builder.apply {
        val linear: LinearLayout =
            layoutInflater.inflate(R.layout.list_item_change_layout, null) as LinearLayout
        linear.titleList.text = title
        val addItemInListButton = linear.addItemInListButton
        if (firstElementText == null) {
            addItemInListButton.visibility = GONE
        } else
            addItemInListButton.setOnClickListener {
                itemClickEvent(-1)
                builder.dismiss()
            }
        val itemListContainer = linear.itemListContainer
        val dpsToPixels = dip(16)
        fun setSelectedItemDecor(textView: TextView) {
            textView.textColorResource = R.color.colorAccent
            textView.textSize = 16f
            textView.setTypeface(textView.typeface, Typeface.BOLD)
//            list.forEachIndexed { index, it ->
//                if (it != textView.text.toString()){
//                    val item = itemListContainer.getChildAt(index) as TextView
//                    item.textColorResource = R.color.textColorTertiary
//                    item.textSize = 14f
//                    item.setTypeface(textView.typeface, Typeface.NORMAL)
//                }
//            }
        }
        list.forEachIndexed { index, name ->
            val itemTextView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = dip(8)
                    marginEnd = dip(8)
                }
                text = name
                textColorResource = R.color.textColorPrimary

                setPadding(dpsToPixels, dpsToPixels, dpsToPixels, dpsToPixels)
                setOnClickListener {
                    itemClickEvent(index)
                    post {
                        setSelectedItemDecor(this)
                        dismiss()
                    }
                }

                addRipple()
            }

            itemListContainer.addView(itemTextView)
            if (index == _selectItem) {
                setSelectedItemDecor(itemTextView)
            }
        }
        setView(linear)

    }.show()
}

fun Context.getColorFromRes(res: Int): Int = ContextCompat.getColor(this, res)

fun getColorFromText(): Int = Color.parseColor("#f1f1f1")


inline fun <reified T> clone(source: T): T {
    val stringProject = Gson().toJson(source, T::class.java)
    return Gson().fromJson<T>(stringProject, T::class.java)
}

inline fun <reified T> Any.equalByGson(equalObject: T): Boolean {
    val sourceAny = Gson().toJson(equalObject, T::class.java)
    val any = Gson().toJson(this, T::class.java)
    return sourceAny == any
}

fun getPathForResource(resourceId: Int): String {
    return Uri.parse("android.resource://" + R::class.java.getPackage()!!.name + "/" + resourceId)
        .toString()
}

fun getDecimalFormatNumber(
    value: Double,
    numberTypePref: NumberTypePref = NumberTypePref()
): String {
    val count = numberTypePref.digitsCount
    val groupNumber = numberTypePref.isGrouping
    val groupSize = numberTypePref.groupSize

    val format = StringBuilder("#")
    repeat(count) {
        if (it == 0)
            format.append('.')
        format.append('#')
    }
    val decimalFormat = DecimalFormat(
        format.toString(),
        DecimalFormatSymbols.getInstance(Locale.getDefault())
    ).apply {
        maximumFractionDigits = count
        roundingMode = RoundingMode.HALF_EVEN
        isGroupingUsed = groupNumber
        groupingSize = groupSize

    }

    return decimalFormat.format(value)
}

fun View.addRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
    setBackgroundResource(resourceId)
}

fun View.animateColor(
    colorFrom: Int,
    colorTo: Int,
    duration: Long = 325,
    endAnimate: () -> Unit = {}
) {
//    val drawable = background
    val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
    valueAnimator.duration = duration
    valueAnimator.interpolator = AccelerateInterpolator()
    valueAnimator.addUpdateListener {
        val fractionAnim = valueAnimator.animatedValue as Float
        backgroundColor = ColorUtils.blendARGB(colorFrom, colorTo, fractionAnim)
    }
    valueAnimator.doOnEnd {
        endAnimate.invoke()
    }
//    valueAnimator.doOnEnd {
//        background = drawable
//    }
    valueAnimator.start()
}

fun log(log: Any?) {
    Log.d("RamLog", log.toString())
}

fun Any.log(message: String) {
    Log.d(this::class.qualifiedName, message)
}

fun Context.showImageTest(text: String, imagePath: String) {
    runOnUiThread {
        AlertDialog.Builder(this).apply {
            setView(verticalLayout {
                addView(ImageView(this@runOnUiThread).apply {
                    post {
                        val scaled = BitmapFactory.decodeFile(imagePath)
                        setImageBitmap(scaled)
                    }
                })
                addView(TextView(this@runOnUiThread).apply {
                    setText(text)
                })
            }
            )
        }.show()
    }
}