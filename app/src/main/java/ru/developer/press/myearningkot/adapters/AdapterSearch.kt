package ru.developer.press.myearningkot.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import ru.developer.press.myearningkot.helpers.*

class AdapterSearch(private val list: List<Search>) :
    RecyclerView.Adapter<AdapterSearch.SearchHolder>() {

    private val title = 0
    private val card = 1
    private val item = 2
    private val total = 3

    inner class SearchHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHolder {
        val context = parent.context
        val view: View? = when (viewType) {
            0 -> {
                context.verticalLayout {
                    textView("title") { }
                }
            }
//            1 -> {
//
//            }
//            2 -> {
//            }
//            3 -> {
//            }
            else -> null
        }

        return SearchHolder(view!!)
    }

    override fun onBindViewHolder(holder: SearchHolder, position: Int) {

    }

    override fun getItemViewType(position: Int): Int = when (list[position]) {
        is TitleSearch -> title
        is CardSearch -> card
        is ItemSearch -> item
        is TotalSearch -> total
    }


    override fun getItemCount(): Int = list.size
}