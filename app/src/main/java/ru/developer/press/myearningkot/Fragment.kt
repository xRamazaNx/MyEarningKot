package ru.developer.press.myearningkot

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.main_cards_layout.*
import ru.developer.press.myearningkot.activity.MainActivity
import ru.developer.press.myearningkot.adapters.AdapterRecyclerInPage
import ru.developer.press.myearningkot.model.Card

class PageFragment: Fragment() {
    var cards: MutableList<Card> = mutableListOf()
    lateinit var cardClickListener: CardClickListener
    private lateinit var adapterRecyclerInPage: AdapterRecyclerInPage
    private var recycler: RecyclerView? = null

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_cards_layout, null)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        cardClickListener = context as MainActivity
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler = recyclerCards
        recycler?.layoutManager = LinearLayoutManager(context)
        adapterRecyclerInPage = AdapterRecyclerInPage(cards, cardClickListener)
        updateRecycler()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun updateRecycler() {

        recycler?.adapter = adapterRecyclerInPage

    }

    fun scrollToPosition(cardPosition: Int) {
        recycler?.smoothScrollToPosition(cardPosition)
        adapterRecyclerInPage.animateCardUpdated(cardPosition)
    }

    fun notifyCardInRecycler(positionCard: Int) {
//       adapterRecyclerInPage.notifyItemChanged(positionCard)
        scrollToPosition(positionCard)
    }

//    override fun scrollToPosition(indexCard : Int) {
//        recycler?.smoothScrollToPosition(indexCard)
//    }

}

