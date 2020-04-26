package ru.developer.press.myearningkot.model

import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.card.view.*
import kotlinx.android.synthetic.main.total_item.view.*
import kotlinx.android.synthetic.main.total_item_layout.view.*
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.wrapContent
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.activity.CardActivity

fun Card.createViewInPlate(plateView: View) {
    val context = plateView.context
    val nameCard = plateView.nameCard
    val datePeriodCard = plateView.datePeriodCard

    nameCard.text = name
    val isCardActivity = context is CardActivity
    datePeriodCard.visibility = if (isShowDatePeriod && !isCardActivity) View.VISIBLE else View.GONE
    datePeriodCard.text = dateOfPeriod

    // визуальная настройка
    cardPref.namePref.customize(nameCard)
    cardPref.dateOfPeriodPref.prefForTextView.customize(datePeriodCard)

    //главный контейнер для заголовков и значений
    val totalContainer: LinearLayout =
        context.layoutInflater.inflate(
            R.layout.total_item_layout,
            null
        ) as LinearLayout
    totalContainer.layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)

    //удаляем где бы не были
    plateView.totalContainerDisableScroll.removeAllViews()
    plateView.totalContainerScroll.removeAllViews()
    // добавляем в главный лейаут
    if (enableHorizontalScrollTotal) {
        plateView.totalContainerScroll.addView(totalContainer)
    } else {
        plateView.totalContainerDisableScroll.addView(totalContainer)
    }
    // контейнер для всех значений
    val totalValueLayout = totalContainer.totalValueContainer
    // кнтейнер для всех заголовков
    val totalTitleLayout = totalContainer.totalTitleContainer

    totals.forEachIndexed { index, totalItem ->
        // лайот где валуе и линия
        val valueLayout =
            context.layoutInflater.inflate(R.layout.total_item_value, null)

        val layoutParams = LinearLayout.LayoutParams(totalItem.width, matchParent).apply {
            weight = 1f
        }
        valueLayout.layoutParams = layoutParams
        if (index == totals.size - 1) {
            valueLayout._verLine.visibility = View.GONE
        }

        val title = TextView(context).apply {
            this.layoutParams = layoutParams
            gravity = Gravity.CENTER
            padding = 5
        }
        val value = valueLayout.totalValue

        title.text = totalItem.title
        title.maxLines = 1
        title.ellipsize = TextUtils.TruncateAt.END
        totalItem.titlePref.customize(title)

        totalItem.totalPref.prefForTextView.customize(value)
        totalItem.calcFormula(this)
        value.text = totalItem.value

        totalTitleLayout.addView(title)
        totalValueLayout.addView(valueLayout)
    }

}

fun Card.updateTotalAmount(plateView: View) {
    val totalValueLayout = plateView.totalValueContainer
    totals.forEachIndexed { index, totalItem ->
        // лайот где валуе и линия
        val valueLayout = totalValueLayout.getChildAt(index)
        val value = valueLayout.totalValue
        totalItem.calcFormula(this)
        value.text = totalItem.value
    }
}
