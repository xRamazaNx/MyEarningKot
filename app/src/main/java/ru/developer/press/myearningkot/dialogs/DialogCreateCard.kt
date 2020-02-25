package ru.developer.press.myearningkot.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.create_card_layout.*
import kotlinx.android.synthetic.main.create_card_layout.view.*
import kotlinx.android.synthetic.main.set_name_layout.view.*
import org.jetbrains.anko.layoutInflater
import ru.developer.press.myearningkot.App
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.activity.*
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.otherHelpers.PrefCardInfo
import ru.developer.press.myearningkot.otherHelpers.SampleHelper
import ru.developer.press.myearningkot.otherHelpers.showItemChangeDialog
import ru.developer.press.myearningkot.prefSampleLastChanged

class DialogCreateCard(val createCard: (Card) -> Unit) : DialogFragment() {

    private val sampleHelper = SampleHelper()
    private val pref = App.instance!!.pref!!
    private var selectSample = pref.getInt(prefSampleLastChanged, 0)
    private var card: Card? = null
    private var sampleList = sampleHelper.getSampleList().apply {
        if (size > 0)
            card =
                this[selectSample]// потом будет настраиваться (var потому что из шаблонов можно будет заменить)
    }
    private val selectedCardTitle = MutableLiveData<String>().apply {
        value = card?.name
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(context).apply {
            val view = context.layoutInflater.inflate(R.layout.create_card_layout, null)

            val editTextCardName = view.editTextCardName
            editTextCardName.hint = getString(R.string.set_name_card)
//            editTextCardName.setText(asyncSampleList.get()[selectSample].name)
//            editTextCardName.setText("")
            // выбор шаблона
            val textViewSample = view.textViewSample
            selectedCardTitle.observe(this@DialogCreateCard, Observer {
                textViewSample.text =
                    "${getString(R.string.sample_of_card)} ($it)"
            })
            textViewSample.setOnClickListener {
                val newSampleText = getString(R.string.new_sample)
                val list = mutableListOf<String>().apply {
                    sampleList.forEach {
                        add(it.name)
                    }
                }
                context.showItemChangeDialog(
                    getString(R.string.change_sample_of_card),
                    list,
                    selectSample,
                    newSampleText
                ) { select ->
                    var selectItem = select
                    if (select == -1) {
                        sampleList =
                            sampleHelper.addSample(newSampleText)
                        selectItem = sampleList.size - 1
                    }
                    selectSample = selectItem
                    pref.edit().putInt(prefSampleLastChanged, selectSample).apply()
                    card = sampleList[selectSample]
                    textViewSample.text =
                        "${getString(R.string.sample_of_card)} (${card?.name})"

                    view.editSampleButton.isEnabled = true
                }
            }
            // настройка внешнего вида

            val editSampleButton = view.editSampleButton
            editSampleButton.isEnabled = card != null
            editSampleButton.setOnClickListener {
                card?.let { it1 ->
                    startPrefActivity(
                        fragment = this@DialogCreateCard,
                        card = it1,
                        title = getString(R.string.setting_sample)
                    )
                }
            }

            setView(view)
            setPositiveButton("Создать") { _: DialogInterface, _: Int ->
                card?.let {
                    it.name = editTextCardName.text.toString()
                    createCard(it.apply {
                        // ВАЖНО включить в код когда надо реально работать с прогой
//                        repeat(rows.size) {
//                            deleteRow()
//                        }
                    })
                }
            }
            setNegativeButton("Отменить") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }

        }

        return dialog.create()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CARD_EDIT_JSON_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    val id = data.getLongExtra(CARD_ID, -1)
                    if (id > -1) {
                        card = sampleHelper.getSample(id)!!
                        sampleList = sampleHelper.getSampleList()
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onResume() {
        super.onResume()
        selectedCardTitle.value = card?.name

        context?.let {
            dialog?.window?.setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        it,
                        R.color.cent
                    )
                )
            )
        }
    }
}

fun startPrefActivity(
    activity: Activity? = null,
    fragment: Fragment? = null,
    card: Card,
    title: String
) {
    val context = activity?.baseContext ?: fragment?.context
    val intent = Intent(context, PrefCardActivity::class.java)
    val cardInfo = PrefCardInfo(
        card.id,
        cardCategory =
        if (activity == null)
            PrefCardInfo.CardCategory.SAMPLE
        else
            PrefCardInfo.CardCategory.CARD
    )
    val prefCategoryJson = Gson().toJson(cardInfo)

    intent.putExtra(PREF_CARD_INFO_JSON, prefCategoryJson)
    intent.putExtra(TITLE_PREF_ACTIVITY, title)
    activity?.startActivityForResult(intent, CARD_EDIT_JSON_REQ_CODE)
    fragment?.startActivityForResult(intent, CARD_EDIT_JSON_REQ_CODE)
}
//    override fun onStart() {
//        val window = dialog?.window
//        window?.apply {
//            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
//        }
//        super.onStart()
//    }
