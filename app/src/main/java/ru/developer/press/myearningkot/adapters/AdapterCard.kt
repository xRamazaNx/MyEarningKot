package ru.developer.press.myearningkot.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.card.view.*
import ru.developer.press.myearningkot.CardClickListener
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.activity.setShowTotalInfo
import ru.developer.press.myearningkot.database.Page
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.model.createViewInPlate
import ru.developer.press.myearningkot.model.hideAddTotalButton

class AdapterCard(
    page: Page,
    private val cardClickListener: CardClickListener
) : RecyclerView.Adapter<AdapterCard.CardHolder>() {
    private val cards: MutableList<MutableLiveData<Card>> = page.cards

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card, parent, false)
        return CardHolder(view, cardClickListener)
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    override fun onBindViewHolder(holder: CardHolder, position: Int) {
        holder.bind(cards[position])
    }

    class CardHolder(
        view: View,
        cardClickListener: CardClickListener
    ) : RecyclerView.ViewHolder(view) {
        private var idCard: String = ""

        init {
            val click: (View) -> Unit = {
                cardClickListener.cardClick(idCard)
            }
            itemView.setOnClickListener(click)
            itemView.totalContainerScroll.setOnClickListener(click)
        }

        fun bind(card: MutableLiveData<Card>) {
            val owner = itemView.context as AppCompatActivity
            card.observe(owner, {
                idCard = it.refId
                it.createViewInPlate(itemView)
                itemView.setShowTotalInfo(it.isShowTotalInfo)
                itemView.hideAddTotalButton(it)

                if (it.isUpdating) {
                    val animate1 = AnimationUtils.loadAnimation(
                        itemView.context,
                        R.anim.anim_alfa_down
                    )
                    val animate2 = AnimationUtils.loadAnimation(
                        itemView.context,
                        R.anim.anim_alfa_up
                    )
                    itemView.post {
                        itemView.startAnimation(animate1)
                    }
                    animate1.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(p0: Animation?) {}

                        override fun onAnimationEnd(p0: Animation?) {
                            itemView.startAnimation(animate2)
                        }

                        override fun onAnimationRepeat(p0: Animation?) {}

                    })

                    it.isUpdating = false
                }


            })
        }
    }
}
