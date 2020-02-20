package ru.developer.press.myearningkot.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import kotlinx.android.synthetic.main.pref_phone_type_param_item.view.*
import ru.developer.press.myearningkot.R

class AdapterRecyclerPhoneParams(
    list: MutableList<PhoneParamModel>,
    val clickListener: (PhoneParamModel) -> Unit
) :
    DragDropSwipeAdapter<PhoneParamModel, PhoneParamHolder>(list) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneParamHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.pref_phone_type_param_item, null)
                .apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
        return PhoneParamHolder(view) {
            clickListener(it)
        }
    }

    override fun getViewHolder(itemView: View): PhoneParamHolder {
        return PhoneParamHolder(itemView) {
            clickListener(it)
        }
    }

    /*
    создал рекуклер для выбора чекбоксов
    модель для вью каждого чекбокса имя позиция для сортировки и чек
    лайот тоже сделал
    можно пробовать все это
     */
    override fun getViewToTouchToStartDraggingItem(
        item: PhoneParamModel,
        viewHolder: PhoneParamHolder,
        position: Int
    ): View? {
        return viewHolder.itemView
    }

    override fun onBindViewHolder(
        item: PhoneParamModel,
        viewHolder: PhoneParamHolder,
        position: Int
    ) {
        viewHolder.bind(item)
    }
}


class PhoneParamHolder(view: View, var clickListener: (PhoneParamModel) -> Unit) :
    DragDropSwipeAdapter.ViewHolder(view) {
    private lateinit var phoneParamModel: PhoneParamModel
    private val checkBox = view.phoneParamCheckBox.apply {
        setOnCheckedChangeListener { _, b ->
            itemView.phoneParamCheckBox.isChecked = b
            phoneParamModel.isCheck = b
            clickListener(phoneParamModel)
        }
    }

    fun bind(phoneParamModel: PhoneParamModel) {
        this.phoneParamModel = phoneParamModel
        checkBox.text = phoneParamModel.name
        checkBox.isChecked = phoneParamModel.isCheck
    }

}

class PhoneParamModel(val name: String, var isCheck: Boolean, val id: Int)