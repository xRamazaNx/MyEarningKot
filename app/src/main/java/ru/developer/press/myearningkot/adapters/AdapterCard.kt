package ru.developer.press.myearningkot.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.card.view.*
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.activity.setShowTotalInfo
import ru.developer.press.myearningkot.database.Card
import ru.developer.press.myearningkot.helpers.*
import ru.developer.press.myearningkot.helpers.scoups.inflatePlate
import ru.developer.press.myearningkot.viewmodels.MainViewModel

class AdapterCard(private val cards: MutableList<MyLiveData<Card>>) :
    RecyclerView.Adapter<AdapterCard.CardHolder>() {
    private lateinit var inflater: LayoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardHolder {
        if (!this::inflater.isInitialized) {
            inflater = LayoutInflater.from(parent.context)
        }
        val view = inflater.inflate(R.layout.card, parent, false)
        return CardHolder(view)
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    override fun onBindViewHolder(holder: CardHolder, position: Int) {
        holder.bind(cards[position])
    }

    class CardHolder(view: View) : RecyclerView.ViewHolder(view) {
        val activity = itemView.context as AppCompatActivity

        fun bind(card: MyLiveData<Card>) {
            val idCard = card.value!!.refId
            setClick(idCard)
            setLongClick(idCard)
            card.observe(activity, observer { c ->
                c.inflatePlate(itemView)
                itemView.setShowTotalInfo(c.isShowTotalInfo)

                if (c.isUpdating) {
                    itemView.animate().alpha(0f).setUpdateListener {
                        it.doOnEnd {
                            itemView.animate().alpha(1f)
                            c.isUpdating = false
                        }
                    }
                }

            })

            val colorFromRes = itemView.context.getColorFromRes(R.color.colorSelectCard)
            itemView.animateColor(
                Color.Transparent.toArgb(),
                colorFromRes,
                ItemAnimator.animateDuration
            )
        }

        private fun setLongClick(idCard: String) {
            val click: (View) -> Unit = {
                MainViewModel.cardClick.invoke(idCard)
            }
            itemView.setOnClickListener(click)
            itemView.totalContainerScroll.setOnClickListener(click)
        }

        private fun setClick(idCard: String) {
            val click: (View) -> Unit = {
                MainViewModel.cardClick.invoke(idCard)
            }
            itemView.setOnClickListener(click)
            itemView.totalContainerScroll.setOnClickListener(click)
        }
    }
}
