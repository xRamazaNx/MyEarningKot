package ru.developer.press.myearningkot.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.developer.press.myearningkot.AdapterPageInterface
import ru.developer.press.myearningkot.PageFragment

class AdapterViewPagerToMain(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val pageInterface: AdapterPageInterface
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    private val fragments = mutableListOf<PageFragment>()

    //
    init {
        fragments.clear()
        val pages = pageInterface.getPages()
        pages.forEach {
            addPage(it.value!!.refId)
        }

        PageFragment.getPageLiveData = {
            pages.find { liveData -> liveData.value!!.refId == it }!!.value!!
        }
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(pagePosition: Int): Fragment {
        return fragments[pagePosition].apply { page = pageInterface.getPages()[pagePosition].value }
    }

    fun insertCardToPosition(positionPage: Int, positionCard: Int) {
        fragments[positionPage].insertToPosition(positionCard)
    }

    fun addPage(pageId: String) {
        fragments.add(
            PageFragment.create(pageId)
        )
    }
}
