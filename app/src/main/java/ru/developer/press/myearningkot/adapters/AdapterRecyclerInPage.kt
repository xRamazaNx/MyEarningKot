package ru.developer.press.myearningkot.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_card.view.*
import kotlinx.android.synthetic.main.card.view.*
import ru.developer.press.myearningkot.CardClickListener
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.activity.setShowTotalInfo
import ru.developer.press.myearningkot.model.Card

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
            itemView.setOnClickListener {
                cardClickListener.cardClick(idCard)
            }
        }


        fun bind(card: Card) {

            card.customizeTotalAmount(itemView)
//            card.cardPref.namePref.customize(nameCard)
//            card.cardPref.dateOfPeriodPref.customize(dateOfPeriod)
//
//            card.cardPref.sumTitlePref.customize(sumTitle)
//            card.cardPref.sumPref.customize(sum)
//
//            card.cardPref.avansTitlePref.customize(avansTitle)
//            card.cardPref.avansPref.customize(avans)
//
//            card.cardPref.balanceTitlePref.customize(balanceTitle)
//            card.cardPref.balancePref.customize(balance)
//
//            nameCard.text = card.name
//            dateOfPeriod.visibility = if (card.visibleDate) VISIBLE else GONE
//            dateOfPeriod.text =
//                card.dateOfPeriod //  предполагается что вся инфа о дате в бизнес логике ставится
//
//            sum.text = card.totalAmount.sum.toString()
//            avans.text = card.totalAmount.avans.toString()
//            balance.text = card.totalAmount.balance.toString()

            idCard = card.id

            itemView.setShowTotalInfo(card.isShowTotalInfo)

        }

    }
}
