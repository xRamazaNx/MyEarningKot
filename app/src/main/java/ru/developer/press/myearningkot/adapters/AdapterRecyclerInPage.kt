package ru.developer.press.myearningkot.adapters

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.FloatRange
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.forEach
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_card.view.*
import kotlinx.android.synthetic.main.card.view.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundColorResource
import ru.developer.press.myearningkot.App
import ru.developer.press.myearningkot.CardClickListener
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.activity.MainActivity
import ru.developer.press.myearningkot.activity.setShowTotalInfo
import ru.developer.press.myearningkot.helpers.animateColor
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.model.createViewInPlate

class AdapterRecyclerInPage(
    private val cards: MutableList<MutableLiveData<Card>>,
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
        val color = ContextCompat.getColor(view.context, R.color.cent)
        view.animateColor(Color.WHITE, color, 500)
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

        init {
            val click: (View) -> Unit = {
                cardClickListener.cardClick(idCard)
            }
            itemView.setOnClickListener(click)
            itemView.totalContainerScroll.setOnClickListener(click)
        }

        fun bind(card: MutableLiveData<Card>) {

            card.observe(itemView.context as AppCompatActivity, Observer {
                it.createViewInPlate(itemView)
                idCard = it.id
                itemView.setShowTotalInfo(it.isShowTotalInfo)
            })


        }

    }
}
