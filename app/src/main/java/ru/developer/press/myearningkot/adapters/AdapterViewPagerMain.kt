package ru.developer.press.myearningkot.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.developer.press.myearningkot.AdapterPageInterface
import ru.developer.press.myearningkot.PageFragment
import ru.developer.press.myearningkot.model.Card

class AdapterViewPagerMain(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val pageInterface: AdapterPageInterface
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragments = mutableListOf<PageFragment>()

    //
    init {
        fragments.clear()
        repeat(pageInterface.getPages().size) {
            addPage()
        }
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(pagePosition: Int): Fragment {

        return fragments[pagePosition].apply {
            page = pageInterface.getPages()[pagePosition]
        }
    }

    fun scrollToPosition(positionPage: Int, positionCard: Int) {
        fragments[positionPage].scrollToPosition(positionCard)
    }

    fun addPage() {
        fragments.add(
            PageFragment()
        )
    }

    fun notifyCardInPage(tabPosition: Int, cardPosition: Int) {
        fragments[tabPosition].notifyCardInRecycler(cardPosition)
    }
}
