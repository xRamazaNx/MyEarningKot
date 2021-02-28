package ru.developer.press.myearningkot.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.developer.press.myearningkot.ImageFragment

class AdapterViewPagerFromImageCell(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val imageUriList: MutableList<String>
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragments = mutableListOf<ImageFragment>()

    override fun getItemCount(): Int {
        return imageUriList.size
    }

    init {
        repeat(imageUriList.size) {
            fragments.add(ImageFragment())
        }
    }
    override fun createFragment(position: Int): Fragment {

        return fragments[position].apply {
            imagePath = imageUriList[position]
        }
//        return fragments[position].apply {
//            imageUrl = imageUrlList[position]
//        }
    }

//    init {
//        repeat(imageUrlList.size) {
//            addPage()
//        }
//    }

//    private fun addPage() {
//        fragments.add(
//            ImageFragment()
//        )
//    }
}
