package ru.developer.press.myearningkot.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.developer.press.myearningkot.AdapterEditCellInterface
import ru.developer.press.myearningkot.EditCellFragment

class AdapterViewPagerEditCell(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val adapterEditCellInterface: AdapterEditCellInterface
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragments = mutableListOf<EditCellFragment>()

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(cellPosition: Int): Fragment {

        return fragments[cellPosition].apply {
            editCellParam = adapterEditCellInterface.getEditCellParams()[cellPosition]
        }
    }
    init {
        repeat(adapterEditCellInterface.getCellCount()){
            addEditCellPage()
        }
    }

    private fun addEditCellPage() {
        fragments.add(
            EditCellFragment()
        )
    }
}
