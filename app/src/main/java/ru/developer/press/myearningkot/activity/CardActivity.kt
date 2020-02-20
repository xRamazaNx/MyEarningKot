package ru.developer.press.myearningkot.activity

import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView.ItemAnimator.ItemAnimatorFinishedListener
import com.bugsnag.android.Bugsnag
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.android.synthetic.main.activity_card.view.*
import kotlinx.android.synthetic.main.card.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.developer.press.myearningkot.CardViewModel
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.ViewModelCardFactory
import ru.developer.press.myearningkot.adapters.AdapterRecyclerInCard
import ru.developer.press.myearningkot.dialogs.startPrefActivity
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.model.DataController
import java.lang.RuntimeException


open class CardActivity : BasicCardActivity() {
    override var viewModel: CardViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // внести все нужные события ид, подписки и т.д.
        val id = intent.getLongExtra(CARD_ID, 0)
        hideViewWhileScroll()
        hideUnnecessaryElementsFromTotalAmount()

        CoroutineScope(Dispatchers.Main).launch {
            val card = withContext(Dispatchers.IO) {
                DataController().getCard(id)
            }
            createViewModel(card)

            progressBar.visibility = GONE
            doStart()
            viewModel?.titleLiveData?.observe(this@CardActivity, Observer {
                title = it
            })
        }
    }

    private fun createViewModel(card: Card) {
        viewModel =
            ViewModelProviders.of(this@CardActivity, ViewModelCardFactory(card))
                .get(CardViewModel::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.card_main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CARD_EDIT_JSON_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    val id = data.getLongExtra(CARD_ID, -1)
                    if (id > -1) {
                        if (viewModel == null) {
                            recreate()
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                val card = DataController().getCard(id)
                                // потому что вьюможель не умирает и не создается занового
                                viewModel!!.updateCard(card)
                                createTitlesFromCard()
                                updateHorizontalScrollSwitched()
                                createRecyclerView()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
            }
            R.id.period -> {
            }
            R.id.sort -> {
            }
            R.id.setting -> {
                startPrefActivity(
                    activity = this,
                    card = viewModel!!.card,
                    title = getString(R.string.setting)
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
//        CoroutineScope(Dispatchers.Main).launch {
//            DataController().updateCard(viewModel.card)
        super.finish()
//        }
    }


    //
    //
    //
    //
    //
    // выполняем что ни будь и рекуклер обновляется после конца анимации
    private fun recyclerRunEvent(runEventRecycler: () -> Unit) { // ...
        runEventRecycler()
        Handler().post(waitForAnimationsToFinishRunnable)
    }

    private val waitForAnimationsToFinishRunnable =
        Runnable { waitForAnimationsToFinish() }

    // When the data in the recycler view is changed all views are animated. If the
// recycler view is animating, this method sets up a listener that is called when the
// current animation finishes. The listener will call this method again once the
// animation is done.
    private fun waitForAnimationsToFinish() {
        if (recycler.isAnimating) { // The recycler view is still animating, try again when the animation has finished.
            recycler.itemAnimator?.isRunning(animationFinishedListener)
            return
        }
        // The recycler view have animated all it's views
        notifyAdapter()
    }

    // Listener that is called whenever the recycler view have finished animating one view.
    private val animationFinishedListener =
        ItemAnimatorFinishedListener {
            // The current animation have finished and there is currently no animation running,
            // but there might still be more items that will be animated after this method returns.
            // Post a message to the message queue for checking if there are any more
            // animations running.
            Handler().post(waitForAnimationsToFinishRunnable)
        }

    // The recycler view is done animating, it's now time to doStuff().
    private fun notifyAdapter() {
        (recycler.adapter as AdapterRecyclerInCard).notifyAdapter()
    }
    //
    //
    //
    //
    //


    private fun hideUnnecessaryElementsFromTotalAmount() {
        totalAmountView.apply {
            datePeriodCard.visibility = GONE
            divide_line.visibility = GONE
            nameCard.visibility = GONE
        }
    }


    private fun hideViewWhileScroll() {
        val animListener = object :
            Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                if (totalAmountView.translationY == 0f)
                    fbAddRow.show()
                if (totalAmountView.translationY == totalAmountView.height.toFloat())
                    fbAddRow.hide()
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }


        }
        totalAmountView.animate().setListener(animListener)
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->

            val heightToolbar = appBarLayout.toolbar.height

            val isHide = -verticalOffset == heightToolbar
            val isShow = verticalOffset == 0

            totalAmountView.apply {
                if (isShow) {
                    animate().translationY(0f)
                }
                if (isHide) {
                    animate().translationY(height.toFloat())

                }
            }
        })
    }

    override fun onBackPressed() {
        // надо отправить в маин
//        val intent = Intent()
//        intent.putExtra(CARD_ID, viewModel!!.card.id)
//        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}


/*
- ширина всей таблицы не должна быть меньше ширины экрана
- только rowClick
- колумны не должны наживаться
 */
