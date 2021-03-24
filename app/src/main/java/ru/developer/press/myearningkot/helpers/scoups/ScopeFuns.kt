package ru.developer.press.myearningkot.helpers.scoups

import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.card.view.*
import kotlinx.android.synthetic.main.total_item_layout.view.*
import kotlinx.android.synthetic.main.total_item_value.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.activity.CardActivity
import ru.developer.press.myearningkot.database.Card
import ru.developer.press.myearningkot.helpers.runOnIO
import ru.developer.press.myearningkot.helpers.runOnMain
import kotlinx.android.synthetic.main.total_item_layout.view.totalValueContainer as totalValueContainer1

fun Card.createViewInPlate(plateView: View) {
    val context = plateView.context
    val nameCard = plateView.nameCard
    val datePeriodCard = plateView.datePeriodCard
    nameCard.text = name
    val isCardActivity = context is CardActivity
    datePeriodCard.visibility = if (isShowDatePeriod && !isCardActivity) View.VISIBLE else GONE
    datePeriodCard.text = dateOfPeriod

    // визуальная настройка
    cardPref.namePref.customize(nameCard, R.font.roboto_medium)
    cardPref.dateOfPeriodPref.prefForTextView.customize(datePeriodCard, R.font.roboto_medium)

    //главный контейнер для заголовков и значений
    val inflater = LayoutInflater.from(context)
    val totalContainer: LinearLayout =
        inflater.inflate(
            R.layout.total_item_layout,
            null
        ) as LinearLayout
    totalContainer.layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent).apply {
        weight = 1f
    }

    val totalContainerDisableScroll = plateView.totalContainerDisableScroll
    val totalContainerScroll = plateView.totalContainerScroll

    //удаляем где бы не были
    totalContainerDisableScroll.removeAllViews()
    totalContainerScroll.removeAllViews()

    // добавляем в главный лейаут для тоталов
    if (enableHorizontalScrollTotal) {
        totalContainerScroll.addView(totalContainer)
//        totalContainerScroll.addView(addTotalImageButton)
        totalContainerDisableScroll.visibility = GONE
    } else {
        totalContainerDisableScroll.addView(totalContainer)
//        totalContainerDisableScroll.addView(addTotalImageButton)
        totalContainerScroll.visibility = GONE
    }
    // контейнер для всех значений
    val totalValueLayout = totalContainer.totalValueContainer1
    // кнтейнер для всех заголовков
    val totalTitleLayout = totalContainer.totalTitleContainer

    totals.forEachIndexed { index, totalItem ->
        // лайот где валуе и линия
        val valueLayout =
            inflater.inflate(R.layout.total_item_value, null)

        val layoutParams = LinearLayout.LayoutParams(totalItem.width, matchParent).apply {
            weight = 1f
        }
        valueLayout.layoutParams = layoutParams
        if (index == totals.size - 1) {
            valueLayout._verLine.visibility = GONE
        }

        val title = TextView(context).apply {
            this.layoutParams = layoutParams
            gravity = Gravity.CENTER
            padding = dip(3)
        }
        val value = valueLayout.totalValue

        title.text = totalItem.title
        title.maxLines = 1
        title.ellipsize = TextUtils.TruncateAt.END
        totalItem.titlePref.customize(title)

        totalItem.totalPref.prefForTextView.customize(value)
        valueLayout.totalValue.text = totalItem.value
        totalTitleLayout.addView(title)
        totalValueLayout.addView(valueLayout)
    }

}

fun Card.updateTotalAmount(plateView: View) {
    val totalValueLayout = plateView.totalValueContainer1
    totals.forEachIndexed { index, totalItem ->
        // лайот где валуе и линия
        val valueLayout = totalValueLayout.getChildAt(index)
        val value = valueLayout.totalValue
        value.text = totalItem.value
    }
}
