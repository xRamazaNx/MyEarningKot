package ru.developer.press.myearningkot.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.pref_phone_type_param_item.view.*
import ru.developer.press.myearningkot.R

class AdapterRecyclerTotal(
    val list: MutableList<ParamModel>,
    val clickListener: (ParamModel) -> Unit
) :
    RecyclerView.Adapter<TotalHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TotalHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.pref_phone_type_param_item, null)
                .apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
        return TotalHolder(view) {
            clickListener(it)
        }
    }

    override fun onBindViewHolder(holder: TotalHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

}


class TotalHolder(view: View, var clickListener: (ParamModel) -> Unit) :
    RecyclerView.ViewHolder(view) {
    private lateinit var paramModel: ParamModel
    private val checkBox = view.phoneParamCheckBox.apply {
        setOnCheckedChangeListener { _, b ->
            paramModel.isCheck = b
            clickListener(paramModel)
        }
    }

    fun bind(paramModel: ParamModel) {
        this.paramModel = paramModel
        checkBox.text = paramModel.name
        checkBox.isChecked = paramModel.isCheck
    }

}