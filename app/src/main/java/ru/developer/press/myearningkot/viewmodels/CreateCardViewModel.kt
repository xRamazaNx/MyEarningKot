package ru.developer.press.myearningkot.viewmodels

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.sample_card_item.view.*
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.wrapContent
import ru.developer.press.myearningkot.App
import ru.developer.press.myearningkot.ProvideDataRows
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.RowClickListener
import ru.developer.press.myearningkot.activity.CreateCardActivity
import ru.developer.press.myearningkot.activity.startPrefActivity
import ru.developer.press.myearningkot.adapters.AdapterRecyclerInCard
import ru.developer.press.myearningkot.helpers.PrefCardInfo
import ru.developer.press.myearningkot.helpers.SampleHelper
import ru.developer.press.myearningkot.helpers.bindTitleOfColumn
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.model.Column
import ru.developer.press.myearningkot.model.Row
import kotlin.concurrent.thread

class CreateCardViewModel : ViewModel() {
    fun updateSamples() {
        sampleList = sampleHelper.getSampleList()
    }

    fun getAdapter(): AdapterForSamples {

        val adapterForSamples = AdapterForSamples(sampleList.fold(ArrayList()) { list, card ->
            list.add(AdapterForSamples.SampleItem(card))
            list
        })
        adapterForSamples.getCard = { id: Long ->
            sampleList.find { it.id == id }!!
        }
        adapterForSamples.deleteCard = { deleteId ->
            sampleHelper.deleteSample(deleteId)
            sampleList.remove(sampleList.find { it.id == deleteId })

        }
        adapterForSamples.updateItemInCard = { item ->
            item?.let { sampleItem ->
                val find = sampleList.find { it.id == sampleItem.card.id }!!
                sampleItem.card = find
            }
        }
        return adapterForSamples
    }

    fun create(app: App) {
        sampleHelper = SampleHelper(app)
        sampleList = sampleHelper.getSampleList()
    }

    private lateinit var sampleHelper: SampleHelper
    private lateinit var sampleList: MutableList<Card>

    class AdapterForSamples(val list: MutableList<SampleItem>) :
        RecyclerView.Adapter<AdapterForSamples.SampleCardHolder>() {

        lateinit var deleteCard: (Long) -> Unit
        lateinit var getCard: (Long) -> Card
        var selectId: Long? = null

        data class SampleItem(var card: Card) {
            var isSelect: Boolean = false
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleCardHolder {
            val context = parent.context
            val view: View = LayoutInflater.from(context)
                .inflate(R.layout.sample_card_item, null).apply {
                    layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
                }

            return SampleCardHolder(view)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: SampleCardHolder, position: Int) {
            val sampleItem = list[position]
            holder.bind(sampleItem) {
                val find = list.find { it.isSelect }
                if (find != null) {
                    find.isSelect = false
                    notifyItemChanged(list.indexOf(find))
                }
                sampleItem.isSelect = true
                selectId = sampleItem.card.id
                notifyItemChanged(position)
            }
        }

        fun updateItem(id: Long) {
            val find = list.find { it.card.id == id }
            updateItemInCard(find)
            notifyItemChanged(list.indexOfFirst { it.card.id == id })
        }

        lateinit var updateItemInCard: (find: SampleItem?) -> Unit

        inner class SampleCardHolder(view: View) : RecyclerView.ViewHolder(view) {
            fun bind(sampleItem: SampleItem, click: () -> Unit) {

                val context = itemView.context
                itemView.setOnClickListener {
                    click.invoke()
                }

                itemView.sampleName.text = sampleItem.card.name

                val columnContainer = itemView.sampleColumnContainer
                columnContainer.removeAllViews()
                sampleItem.card.columns.forEach {
                    val title: TextView =
                        LayoutInflater.from(context)
                            .inflate(R.layout.title_column, null) as TextView
                    bindTitleOfColumn(it, title)
                    columnContainer.addView(title)

                }
                columnContainer.setOnClickListener { click.invoke() }
                val sampleRecycler = itemView.sampleRecycler
                sampleRecycler.setOnClickListener { click.invoke() }

                sampleRecycler.layoutManager = LinearLayoutManager(context)
                val adapterRecyclerInCard = AdapterRecyclerInCard(null, object : ProvideDataRows {
                    override val sortedRows: MutableList<Row>
                        get() = sampleItem.card.rows

                    override fun getColumns(): MutableList<Column> = sampleItem.card.columns

                    override fun getWidth(): Int = matchParent

                    override fun isEnableHorizontalScroll() = true

                    override fun isEnableSomeStroke(): Boolean = false

                    override fun getRowHeight(): Int = sampleItem.card.heightCells
                }, null)
                adapterRecyclerInCard.setCellClickListener(object : RowClickListener {
                    override fun cellClick(view: View, rowPosition: Int, cellPosition: Int) {
                        click.invoke()
                    }
                })
                sampleRecycler.adapter = adapterRecyclerInCard
                itemView.sampleMenu.setOnClickListener { view ->
                    val popupMenu = PopupMenu(context, view)
                    popupMenu.gravity = Gravity.BOTTOM
                    popupMenu.inflate(R.menu.sample_item_menu)
                    popupMenu.show()
                    popupMenu.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.edit_sample -> {
                                startPrefActivity(
                                    PrefCardInfo.CardCategory.SAMPLE,
                                    activity = context as CreateCardActivity,
                                    card = getCard(sampleItem.card.id),
                                    title = view.context.getString(R.string.setting_sample)
                                )
                            }
                            R.id.delete_sample -> {
                                thread {
                                    deleteCard.invoke(sampleItem.card.id)
                                    list.remove(sampleItem)
                                    context.runOnUiThread {
                                        notifyItemRemoved(adapterPosition)
                                    }
                                }
                            }
                        }
                        true
                    }
                }
                if(sampleItem.isSelect){
                    itemView.sampleContainer.setBackgroundResource(R.drawable.row_selected_background)
                } else
                    itemView.sampleContainer.setBackgroundResource(R.drawable.background_for_card)
            }
        }
    }

}