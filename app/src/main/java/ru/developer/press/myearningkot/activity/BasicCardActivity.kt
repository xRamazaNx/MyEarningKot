package ru.developer.press.myearningkot.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.contains
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.android.synthetic.main.card.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.developer.press.myearningkot.App
import ru.developer.press.myearningkot.CardViewModel
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.RowClickListener
import ru.developer.press.myearningkot.adapters.AdapterRecyclerInCard
import ru.developer.press.myearningkot.otherHelpers.bindTitleOfColumn

@SuppressLint("Registered")
abstract class BasicCardActivity : AppCompatActivity() {
    protected lateinit var adapter: AdapterRecyclerInCard
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
            createTitlesFromCard()
            // подписываем
            observeTotalAmount()
        }
//        tableView.horizontalScrollView.columnsScrollView = columnScrollContainer
        updateHorizontalScrollSwitched()
        createRecyclerView()
    }

    private fun observeTotalAmount() {
        viewModel?.cardLiveData?.observe(this, Observer {
            it.customizeTotalAmount(totalAmountView)
        })
    }


    fun updateHorizontalScrollSwitched() {
        viewModel?.let {

            if (it.isEnableHorizontalScroll()) {
                if (columnDisableScrollContainer.contains(columnContainer)) {
                    columnDisableScrollContainer.removeView(columnContainer)
                }
                columnDisableScrollContainer.visibility = View.GONE
                columnScrollContainer.visibility = View.VISIBLE
                if (!columnScrollContainer.contains(columnContainer))
                    columnScrollContainer.addView(columnContainer)

            } else {
                if (columnScrollContainer.contains(columnContainer)) {
                    columnScrollContainer.removeView(columnContainer)
                }
                columnScrollContainer.visibility = View.GONE
                columnDisableScrollContainer.visibility = View.VISIBLE

                if (!columnDisableScrollContainer.contains(columnContainer))
                    columnDisableScrollContainer.addView(columnContainer)
            }
        }

    }

    protected open fun createRecyclerView() {
        recycler.apply {
            orientation =
                DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
            //            longPressToStartDragging = true
            orientation?.removeSwipeDirectionFlag(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.LEFT)
            orientation?.removeSwipeDirectionFlag(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.RIGHT)

            layoutManager = LinearLayoutManager(this@BasicCardActivity)

            this@BasicCardActivity.adapter = getAdapterForRecycler()


            adapter = this@BasicCardActivity.adapter


        }
//        tableView.horizontalScrollView.moveRowNumber =
//            (recycler.adapter as AdapterRecyclerInCard).moveRowNumber
    }

    protected fun getAdapterForRecycler(): AdapterRecyclerInCard {
        return AdapterRecyclerInCard(object : RowClickListener {
            override fun rowClick(position: Int) {
                toast("click item")
            }

            override fun rowLongClick(position: Int) {
            }
        }, viewModel!!, viewModel!!.card.rows)
    }

    fun createTitlesFromCard() {
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
            totalAmountView.setShowTotalInfo(viewModel!!.card.isShowTotalInfo)
        }
    }

}

 fun View.setShowTotalInfo(showTotalInfo: Boolean) {
    if (showTotalInfo)
        total_container.visibility = View.VISIBLE
    else
        total_container.visibility = View.GONE
}