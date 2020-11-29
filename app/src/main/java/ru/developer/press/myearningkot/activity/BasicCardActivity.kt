package ru.developer.press.myearningkot.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.contains
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.android.synthetic.main.card.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.developer.press.myearningkot.App
import ru.developer.press.myearningkot.viewmodels.CardViewModel
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.adapters.AdapterRecyclerInCard
import ru.developer.press.myearningkot.model.createViewInPlate
import ru.developer.press.myearningkot.model.updateTotalAmount
import ru.developer.press.myearningkot.helpers.bindTitleOfColumn

@SuppressLint("Registered")
abstract class BasicCardActivity : AppCompatActivity() {
    protected lateinit var adapter: AdapterRecyclerInCard
    val totalViewList = mutableListOf<View>()
    val columnContainer = LinearLayout(App.instance!!.applicationContext).also {
        it.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
    }
    abstract var viewModel: CardViewModel?


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(Color.WHITE)

        // для того что бы тоталвью не пропускал сквозь себя клики на ресайклер с записями
        totalAmountView.setOnClickListener { }

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
//        tableView.horizontalScrollView.columnsScrollView = columnScrollContainer
        updateHorizontalScrollSwitched()
        createRecyclerView()
    }

    private fun observePlate() {
        viewModel?.cardLiveData?.observe(this, Observer {
            it.createViewInPlate(totalAmountView)
        })
    }
    private fun observeTotals() {
        viewModel?.totalLiveData?.observe(this, Observer {
            it.updateTotalAmount(totalAmountView)
        })
    }


    fun updateHorizontalScrollSwitched() {
        viewModel?.let {

            if (it.isEnableHorizontalScroll()) {
                if (columnDisableScrollContainer.contains(columnContainer)) {
                    columnDisableScrollContainer.removeView(columnContainer)
                }
                columnDisableScrollContainer.visibility = GONE
                columnScrollContainer.visibility = VISIBLE
                if (!columnScrollContainer.contains(columnContainer))
                    columnScrollContainer.addView(columnContainer)

            } else {
                if (columnScrollContainer.contains(columnContainer)) {
                    columnScrollContainer.removeView(columnContainer)
                }
                columnScrollContainer.visibility = GONE
                columnDisableScrollContainer.visibility = VISIBLE

                if (!columnDisableScrollContainer.contains(columnContainer))
                    columnDisableScrollContainer.addView(columnContainer)
            }
        }
    }

    protected open fun createRecyclerView() {
        recycler.apply {

            layoutManager = LinearLayoutManager(this@BasicCardActivity)

            this@BasicCardActivity.adapter = getAdapterForRecycler()

            adapter = this@BasicCardActivity.adapter


        }
//        tableView.horizontalScrollView.moveRowNumber =
//            (recycler.adapter as AdapterRecyclerInCard).moveRowNumber
    }

    protected fun getAdapterForRecycler(): AdapterRecyclerInCard {
        return AdapterRecyclerInCard(null, viewModel!!, totalAmountView)
    }

    fun createTitles() {
        columnContainer.removeAllViews()
        viewModel?.columnLDList?.forEach { column ->
            val title: TextView =
                layoutInflater.inflate(R.layout.title_column, null) as TextView
            column.observe(this@BasicCardActivity, Observer {
                bindTitleOfColumn(it, title)
            })
            columnContainer.addView(title)
        }
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                while (true) {
                    if (viewModel != null) {
                        break
                    }
                }
            }
            setShowTotalInfo(viewModel!!.card.isShowTotalInfo)
        }
    }

    protected fun setShowTotalInfo(showTotalInfo: Boolean){
        totalAmountView.setShowTotalInfo(showTotalInfo)
//        if (showTotalInfo)
//            addTotal.visibility = VISIBLE
//        else
//            addTotal.visibility = GONE
    }

    protected fun notifyAdapter() {
        viewModel?.let {
            adapter.notifyDataSetChanged()
        }
    }
}

fun View.setShowTotalInfo(showTotalInfo: Boolean) {
    if (showTotalInfo) {
        totalContainerDisableScroll.visibility = VISIBLE
        totalContainerScroll.visibility = VISIBLE
    } else {
        totalContainerDisableScroll.visibility = GONE
        totalContainerScroll.visibility = GONE
    }
}