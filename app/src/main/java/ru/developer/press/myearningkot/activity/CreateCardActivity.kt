package ru.developer.press.myearningkot.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.create_card_activity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.viewmodels.CreateCardViewModel

class CreateCardActivity : AppCompatActivity() {

    companion object {
        const val createCardID: String = "createCardID"
        const val createCardName: String = "createCardName"
    }

    private lateinit var adapter: CreateCardViewModel.AdapterForSamples
    private lateinit var viewModel: CreateCardViewModel
    val editSampleRegister: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data
            if (data != null) {
                val id = data.getStringExtra(CARD_ID) ?: ""
                if (id.isNotEmpty()) {
                    lifecycleScope.launch {
                        viewModel.updateSamples {
                            adapter.updateItem(id)
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_card_activity)
        lifecycleScope.launch(Dispatchers.IO) {

            viewModel = ViewModelProvider(
                this@CreateCardActivity,
                ViewModelProvider.NewInstanceFactory()
            ).get(CreateCardViewModel::class.java)
                .apply {
                    create(this@CreateCardActivity)
                }
            adapter = viewModel.getAdapter()
            withContext(Dispatchers.Main) {
                recycler.layoutManager = LinearLayoutManager(this@CreateCardActivity)
                recycler.adapter = adapter
            }

            create.setOnClickListener {
                val selectId = adapter.selectId
                if (selectId == null) {
                    toast(getString(R.string.select_sample))
                } else {

                    setResult(RESULT_OK, Intent().apply {
                        putExtra(createCardID, selectId)
                        putExtra(createCardName, sampleEditTextName.text.toString())
                    })
                    finish()
                }
            }
            cancel.setOnClickListener {
                finish()
            }
        }
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