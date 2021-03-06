package ru.developer.press.myearningkot.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.contains
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.android.synthetic.main.card.view.*
import kotlinx.coroutines.*
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.adapters.AdapterRow
import ru.developer.press.myearningkot.databinding.ActivityCardBinding
import ru.developer.press.myearningkot.helpers.*
import ru.developer.press.myearningkot.helpers.scoups.inflatePlate
import ru.developer.press.myearningkot.helpers.scoups.updateTotalAmount
import ru.developer.press.myearningkot.viewmodels.CardViewModel

@SuppressLint("Registered")
abstract class BasicCardActivity : AppCompatActivity() {
    private lateinit var root: ActivityCardBinding
    protected lateinit var adapter: AdapterRow
    lateinit var columnContainer: LinearLayout
    abstract var viewModel: CardViewModel?


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = ActivityCardBinding.inflate(layoutInflater)
        setContentView(root.root)
        columnContainer = LinearLayout(this).also {
            it.layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
        }

        setSupportActionBar(root.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        root.toolbar.setTitleTextColor(getColorFromRes(R.color.colorOnPrimary))

        // для того что бы тоталвью не пропускал сквозь себя клики на ресайклер с записями
        root.totalAmountView.root.callOnClick()

    }


    fun doStart() {
        viewModel?.apply {
            val diametric = resources.displayMetrics
            displayParam.width = diametric.widthPixels
            displayParam.height = diametric.heightPixels

            // создаем заголовки колон и подписываемся
            createTitles()
            // подписываем
            observePlate()
            observeTotals()
        }
        updateHorizontalScrollSwitched()
        initRecyclerView()
    }

    private fun observePlate() {
        viewModel?.cardLiveData?.observe(this, {
            it.inflatePlate(totalAmountView)
            totalAmountView.backgroundResource = R.drawable.background_for_card_in_card_activity
        })
    }

    private fun observeTotals() {
        viewModel?.totalLiveData?.observe(this, {
            it.updateTotalAmount(totalAmountView)
        })
    }


    fun updateHorizontalScrollSwitched() {
        viewModel?.let {

            val currentLayout: View?
            if (it.isEnableHorizontalScroll()) {
                if (columnDisableScrollContainer.contains(columnContainer)) {
                    columnDisableScrollContainer.removeView(columnContainer)
                }
                columnDisableScrollContainer.visibility = GONE
                columnScrollContainer.visibility = VISIBLE
                if (!columnScrollContainer.contains(columnContainer))
                    columnScrollContainer.addView(columnContainer)
                currentLayout = columnScrollContainer
            } else {
                if (columnScrollContainer.contains(columnContainer)) {
                    columnScrollContainer.removeView(columnContainer)
                }
                columnScrollContainer.visibility = GONE
                columnDisableScrollContainer.visibility = VISIBLE

                if (!columnDisableScrollContainer.contains(columnContainer))
                    columnDisableScrollContainer.addView(columnContainer)
                currentLayout = columnDisableScrollContainer
            }
            currentLayout?.backgroundColorResource = R.color.colorPrimary
        }
    }

    protected open fun initRecyclerView() {
        recycler.apply {

            layoutManager = LinearLayoutManager(this@BasicCardActivity)

            this@BasicCardActivity.adapter = getAdapterForRecycler()

            adapter = this@BasicCardActivity.adapter

            itemAnimator = ItemAnimator()

        }
//        tableView.horizontalScrollView.moveRowNumber =
//            (recycler.adapter as AdapterRecyclerInCard).moveRowNumber
    }

    protected fun getAdapterForRecycler(): AdapterRow {
        return AdapterRow(null, viewModel!!, totalAmountView)
    }

    @SuppressLint("InflateParams")
    fun createTitles() {
        columnContainer.removeAllViews()
        viewModel?.columnLDList?.forEach { column ->
            val title: TextView = layoutInflater.inflate(R.layout.title_column, null) as TextView
            column.observe(this@BasicCardActivity, {
                bindTitleOfColumn(it, title)
            })
            columnContainer.addView(title)
        }
    }

    override fun onResume() {
        super.onResume()
        runOnMaim {
            io {
                while (true) {
                    if (viewModel != null) {
                        break
                    }
                    delay(50)
                }
            }
            setShowTotalInfo(viewModel!!.card.isShowTotalInfo)
        }
    }

    protected fun setShowTotalInfo(showTotalInfo: Boolean) {
        totalAmountView.setShowTotalInfo(showTotalInfo)
//        if (showTotalInfo)
//            addTotal.visibility = VISIBLE
//        else
//            addTotal.visibility = GONE
    }

    protected fun notifyAdapter() {
        adapter.notifyDataSetChanged()
    }
}

fun View.setShowTotalInfo(showTotalInfo: Boolean) {
    if (showTotalInfo) {
        totalContainerDisableScroll.visibility = VISIBLE
        totalContainerScroll.visibility = VISIBLE
        divide_line.visibility = GONE
    } else {
        totalContainerDisableScroll.visibility = GONE
        totalContainerScroll.visibility = GONE
        divide_line.visibility = GONE
    }
}