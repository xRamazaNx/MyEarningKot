package ru.developer.press.myearningkot.otherHelpers

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.text.format.DateFormat
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.list_item_change_layout.view.*
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.textColorResource
import ru.developer.press.myearningkot.App
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.dpsToPixels
import ru.developer.press.myearningkot.model.Column
import ru.developer.press.myearningkot.model.NumberTypePref
import ru.developer.press.myearningkot.model.NumerationColumn
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class SingleLiveEvent<T> : MutableLiveData<T>() {

    private val mPending = AtomicBoolean(false)

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {

        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registered but only one will be notified of changes.")
        }

        // Observe the internal MutableLiveData
        super.observe(owner, Observer<T> { t ->
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

fun getValutaTypeList(): MutableList<String> {
    val context = App.instance!!
    return context.resources.getStringArray(R.array.valuta_list).toMutableList()

}

fun getDateTypeList(): MutableList<String> {
    return mutableListOf<String>().apply {
        add("Не показывать")
        add(getDate(1, enableTime = false))
        add(getDate(2, enableTime = false))
        add(getDate(3, enableTime = false))
    }
}

//    public void setDateInt(int day, int month, int year) {
//
//        dateInt[0] = year;
//        dateInt[1] = month;
//        dateInt[2] = day;
//
//        calendar.set(dateInt[0], dateInt[1], dateInt[2]);
//    }
//    private void updateDateMl() {
//        calendar.set(dateInt[0], dateInt[1], dateInt[2]);
//        dateMl = calendar.getTimeInMillis();
//    }
//    public void setDateInt(int[] dateInt) {
//        this.dateInt = dateInt;
//        calendar.set(dateInt[0], dateInt[1], dateInt[2]);
//    }
//    public int[] getDateIntArray() {
//        return dateInt;
//    }
//    public int getDateInt() { //
//
//        String year = String.valueOf(dateInt[0]);
//        String month = String.valueOf(dateInt[1]);
//        String day = String.valueOf(dateInt[2]);
//
//        if (month.length() < 2) {
//            month = "0" + month;
//        }
//        if (day.length() < 2) {
//            day = "0" + day;
//        }
//
//        String dateForSort = year + month + day;
//        return Integer.parseInt(dateForSort);
//    }
//колличетсво рулонов 1

fun getDate(variantDate: Int, time: Long = Date().time, enableTime: Boolean): String {
    val sDayOfWeek = arrayOf("вс, ", "пн, ", "вт, ", "ср, ", "чт, ", "пт, ", "сб, ")

    val calendar = Calendar.getInstance().apply {
        timeInMillis = time
    }
    val dayName: String = sDayOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1]
    var timeFormat = ""
    when (variantDate) {
        1 -> timeFormat = "dd.MM.yy"
        2 -> timeFormat = "dd.MM.yyyy"
        3 -> timeFormat = "dd MMMM yyyy"
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
            App.instance!!.dpsToPixels(35)
        ).apply {
            gravity = Gravity.CENTER
            weight = w
        }

    title.text = column.name
    column.titlePref.customize(title)
}

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
        val dpsToPixels = dpsToPixels(16)
        list.forEachIndexed { index, name ->
            val itemTextView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = dpsToPixels(8)
                    marginEnd = dpsToPixels(8)
                }
                text = name
                textColorResource = R.color.centDarkTemnee2

                setPadding(dpsToPixels, dpsToPixels, dpsToPixels, dpsToPixels)
                setOnClickListener {
                    itemClickEvent(index)
                    postDelayed({
                        backgroundColorResource = R.color.gray
                        builder.dismiss()
                    }, 150)
                }

                addRipple()
            }

            if (index == _selectItem) {
                itemTextView.backgroundColorResource = R.color.gray
            }
            itemListContainer.addView(itemTextView)
        }
        setView(linear)

    }.show()
}

fun Context.getColorFromRes(res: Int): Int = ContextCompat.getColor(this, res)

fun getColorFromText(): Int = Color.parseColor("#141414")


inline fun <reified T> clone(source: T): T {
    val stringProject = Gson().toJson(source, T::class.java)
    return Gson().fromJson<T>(stringProject, T::class.java)
}

inline fun <reified T> Any.equalGson(equalObject: T): Boolean {
    val sourceAny = Gson().toJson(equalObject, T::class.java)
    val any = Gson().toJson(this, T::class.java)
    return sourceAny == any
}

fun getURLForResource(resourceId: Int): String? {
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

// # ширину колоны для цвета можно выбирать такой какая высота ячеек