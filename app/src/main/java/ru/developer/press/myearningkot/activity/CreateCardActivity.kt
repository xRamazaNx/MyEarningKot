package ru.developer.press.myearningkot.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kotlinx.android.synthetic.main.create_card_activity.*
import org.jetbrains.anko.toast
import ru.developer.press.myearningkot.App.Companion.app
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.helpers.PrefCardInfo
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.viewmodels.CreateCardViewModel
import kotlin.concurrent.thread

class CreateCardActivity : AppCompatActivity() {

    companion object {
        const val createCardID: String = "createCardID"
        const val createCardName: String = "createCardName"
        const val createCardRequest = 100
    }

    private lateinit var adapter: CreateCardViewModel.AdapterForSamples
    private lateinit var viewModel: CreateCardViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_card_activity)
        thread {

            viewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
                CreateCardViewModel::class.java
            ).apply { create(app()) }
            adapter = viewModel.getAdapter()
            runOnUiThread {
                recycler.layoutManager = LinearLayoutManager(this)
                recycler.adapter = adapter
            }

            create.setOnClickListener {
                adapter.selectId?.let {
                    setResult(RESULT_OK, Intent().apply {
                        putExtra(createCardID, it)
                        putExtra(createCardName, sampleEditTextName.text.toString())
                    })
                    finish()
                } ?: kotlin.run {
                    runOnUiThread {
                        toast(getString(R.string.select_sample))
                    }
                }
            }
            cancel.setOnClickListener {
                runOnUiThread {
                    finish()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CARD_EDIT_JSON_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    val id = data.getLongExtra(CARD_ID, -1)
                    if (id > -1) {
                        viewModel.updateSamples()
                        adapter.updateItem(id)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.sample_item_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_sample -> {

            }
        }
        return true
    }
}

fun startPrefActivity(
    category: PrefCardInfo.CardCategory,
    activity: Activity? = null,
    card: Card,
    title: String
) {
    val intent = Intent(activity, PrefCardActivity::class.java)
    val cardInfo = PrefCardInfo(
        card.id,
        cardCategory =
        category
    )
    val prefCategoryJson = Gson().toJson(cardInfo)

    intent.putExtra(PREF_CARD_INFO_JSON, prefCategoryJson)
    intent.putExtra(TITLE_PREF_ACTIVITY, title)
    activity?.startActivityForResult(intent, CARD_EDIT_JSON_REQ_CODE)
}