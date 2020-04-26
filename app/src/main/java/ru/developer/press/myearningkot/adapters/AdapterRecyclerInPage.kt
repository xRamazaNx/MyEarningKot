package ru.developer.press.myearningkot.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_card.view.*
import kotlinx.android.synthetic.main.card.view.*
import ru.developer.press.myearningkot.CardClickListener
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.activity.setShowTotalInfo
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.model.createViewInPlate

class AdapterRecyclerInPage(
    private val cards: MutableList<Card>,
    private val cardClickListener: CardClickListener
) : RecyclerView.Adapter<AdapterRecyclerInPage.CardHolder>() {

    private var animatePosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card, parent, false)

        return CardHolder(view, cardClickListener)
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    override fun onBindViewHolder(holder: CardHolder, position: Int) {
        holder.bind(cards[position])
        if (animatePosition > -1 && position == animatePosition) {
            animate(holder.itemView)
            animatePosition = -1
        }
    }

    private fun animate(view: View) {
        val animate = AnimationUtils.loadAnimation(view.context, R.anim.anim_add)
        view.startAnimation(animate)
    }

    fun animateCardUpdated(cardPosition: Int) {
        animatePosition = cardPosition
        notifyItemChanged(cardPosition)
    }


    class CardHolder(
        view: View,
        cardClickListener: CardClickListener
    ) : RecyclerView.ViewHolder(view) {
        private var idCard: Long = -1

//        private val nameCard = itemView.nameCard
//        private val dateOfPeriod = itemView.datePeriodCard
//
//        private val sumTitle = itemView.sumTitle
//        private val sum = itemView.sum
//
//        private val avansTitle = itemView.avansTitle
//        private val avans = itemView.avans
//
//        private val balanceTitle = itemView.balanceTitle
//        private val balance = itemView.balance

        init {
            val click: (View) -> Unit = {
                cardClickListener.cardClick(idCard)
            }
            itemView.setOnClickListener(click)
            itemView.totalContainerScroll.setOnClickListener(click)
        }

        fun bind(card: Card) {

            card.createViewInPlate(itemView)

            idCard = card.id

            itemView.setShowTotalInfo(card.isShowTotalInfo)

        }

    }
}
