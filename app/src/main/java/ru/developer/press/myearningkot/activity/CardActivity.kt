package ru.developer.press.myearningkot.activity

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.animation.Animation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView.ItemAnimator.ItemAnimatorFinishedListener
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.android.synthetic.main.activity_card.view.*
import kotlinx.android.synthetic.main.card.*
import kotlinx.android.synthetic.main.card.view.*
import kotlinx.coroutines.*
import ru.developer.press.myearningkot.*
import ru.developer.press.myearningkot.App.Companion.app
import ru.developer.press.myearningkot.adapters.animationAdd
import ru.developer.press.myearningkot.adapters.animationDelete
import ru.developer.press.myearningkot.database.DataController
import ru.developer.press.myearningkot.dialogs.PICK_IMAGE_MULTIPLE
import ru.developer.press.myearningkot.dialogs.editCellTag
import ru.developer.press.myearningkot.helpers.EditCellControl
import ru.developer.press.myearningkot.database.PrefCardInfo
import ru.developer.press.myearningkot.helpers.getColorFromRes
import ru.developer.press.myearningkot.helpers.getDrawableRes
import ru.developer.press.myearningkot.model.*
import ru.developer.press.myearningkot.viewmodels.CardViewModel
import ru.developer.press.myearningkot.viewmodels.CardViewModel.SelectMode
import ru.developer.press.myearningkot.viewmodels.ViewModelCardFactory
import uk.co.markormesher.android_fab.SpeedDialMenuAdapter
import uk.co.markormesher.android_fab.SpeedDialMenuItem
import java.lang.Runnable


open class CardActivity : BasicCardActivity() {
    private val editCardRegister =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data
            if (data != null) {
                val id = data.getStringExtra(CARD_ID)?:""
                if (id!!.isNotEmpty()) {
                    if (viewModel == null) {
                        recreate()
                    } else {
                        viewModel?.viewModelScope?.launch {
                            val card = withContext(Dispatchers.Default) {
                                DataController(this@CardActivity).getCard(id)
                            }

                            viewModel!!.updateCard(card)
                            createTitles()
                            updateHorizontalScrollSwitched()
                            createRecyclerView()
                            viewModel?.apply {
                                selectMode.value = SelectMode.NONE
                            }

                            onResume()
                        }
                    }
                }
            }
        }
    override var viewModel: CardViewModel? = null
    private var isLongClick = false
    private val launch = CoroutineScope(Dispatchers.Main).launch {
        val id = intent.getStringExtra(CARD_ID)!!
        val card = withContext(Dispatchers.IO) {
            DataController(this@CardActivity).getCard(id)
        }
        createViewModel(card)

        progressBar.visibility = GONE
        doStart()
        viewModel?.apply {
            titleLiveData.observe(this@CardActivity, {
                title = it
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // внести все нужные события ид, подписки и т.д.
        launch.start()
        tableView.isLong.observe(this, {
            isLongClick = it
        })
        fbAddRow.setContentCoverColour(Color.TRANSPARENT)
        hideViewWhileScroll()

        // листенер для обновления после удаления то есть после ее анимации
        val animateListener = object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                viewModel?.apply {
                    deleteRows { indexDel: Int ->
                        adapter.notifyItemRemoved(indexDel)
                    }
                    selectMode.value = SelectMode.NONE
                }
            }

            override fun onAnimationStart(p0: Animation?) {
            }

        }
        animationDelete.setAnimationListener(animateListener)
        animationAdd.setAnimationListener(animateListener)
    }


    private fun selectedModeObserve() {
        val menu = toolbar.menu
        viewModel?.selectMode?.observe(this, { selectMode ->
            menu.clear()
            when (selectMode) {
                SelectMode.CELL -> {
                    menuInflater.inflate(R.menu.cell_menu, menu)
                    // ставим иконку вставить в зависимости доступности вставки
                    if (viewModel!!.isEqualTypeCellAndCopyCell(app().copyCell)) {
                        menu.findItem(R.id.pasteCell).setIcon(R.drawable.ic_paste)
                    } else
                        menu.findItem(R.id.pasteCell).setIcon(R.drawable.ic_paste_disabled)

//                    val speedAdapter = object : SpeedDialMenuAdapter() {
//
//                        private val list = mutableListOf<SpeedDialMenuItem>().apply {
//                            add(
//                                SpeedDialMenuItem(
//                                    this@CardActivity,
//                                    if (viewModel!!.isEqualTypeCellAndCopyCell())
//                                        getDrawable(R.drawable.ic_paste_white)!!
//                                    else
//                                        getDrawable(R.drawable.ic_paste_white_disabled)!!,
////                                    getString(R.string.PASTE)
//                                    ""
//                                )
//                            )
//                            add(
//                                SpeedDialMenuItem(
//                                    this@CardActivity,
//                                    getDrawable(R.drawable.ic_copy_white)!!,
////                                    getString(R.string.COPY)
//                                    ""
//                                )
//                            )
//                            add(
//                                SpeedDialMenuItem(
//                                    this@CardActivity,
//                                    getDrawable(R.drawable.ic_cut_white)!!,
//                                    ""
////                                    getString(R.string.cut)
//                                )
//                            )
//                            add(
//                                SpeedDialMenuItem(
//                                    this@CardActivity,
//                                    getDrawable(R.drawable.ic_edit_white)!!,
////                                    getString(R.string.DELETE)
//                                    ""
//                                )
//                            )
//                        }
//
//                        override fun getCount(): Int = list.size
//
//                        override fun onMenuItemClick(position: Int): Boolean {
//                            fbAddRow.closeSpeedDialMenu()
//                            fbAddRow.postDelayed(100) {
//
//                                when (position) {
//                                    0 -> {
//                                        pasteCell()
//                                    }
//                                    1 -> {
//                                        copySelectedCell()
//                                    }
//                                    2 -> {
//                                        cutSelectedCell()
//                                    }
//                                    3 -> {
//                                        editCell()
//                                    }
//                                }
//                            }
//                            return true
//                        }
//
//                        override fun getMenuItem(
//                            context: Context,
//                            position: Int
//                        ): SpeedDialMenuItem = list[position]
//
//                        override fun getBackgroundColour(position: Int): Int {
//                            return getColorFromRes(R.color.red_fb_button)
//                        }
//                    }
//                    fbAddRow.speedDialMenuAdapter = speedAdapter
//                    fbAddRow.setButtonIconResource(R.drawable.ic_menu_3_line)
//                    fbAddRow.setButtonBackgroundColour(getColorFromRes(R.color.red_fb_button))
                    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_check)
                }

                SelectMode.ROW -> {
                    menuInflater.inflate(R.menu.row_menu, menu)
                    if (viewModel!!.isCapabilityPaste()) {
                        menu.findItem(R.id.pasteRow).setIcon(R.drawable.ic_paste)
                    } else
                        menu.findItem(R.id.pasteRow).setIcon(R.drawable.ic_paste_disabled)
                    fbAddRow.speedDialMenuAdapter = object : SpeedDialMenuAdapter() {
                        private val list = mutableListOf<SpeedDialMenuItem>().apply {
                            add(
                                SpeedDialMenuItem(
                                    this@CardActivity,
                                    if (viewModel!!.isCapabilityPaste())
                                        getDrawableRes(R.drawable.ic_paste)!!
                                    else
                                        getDrawableRes(R.drawable.ic_paste_disabled)!!,
//                                    getString(R.string.PASTE)
                                    ""
                                )
                            )
                            add(
                                SpeedDialMenuItem(
                                    this@CardActivity,
                                    getDrawableRes(R.drawable.ic_copy)!!,
//                                    getString(R.string.COPY)
                                    ""
                                )
                            )
                            add(
                                SpeedDialMenuItem(
                                    this@CardActivity,
                                    getDrawableRes(R.drawable.ic_cut)!!,
                                    ""
//                                    getString(R.string.cut)
                                )
                            )
                            add(
                                SpeedDialMenuItem(
                                    this@CardActivity,
                                    getDrawableRes(R.drawable.ic_delete)!!,
//                                    getString(R.string.DELETE)
                                    ""
                                )
                            )
                        }

                        override fun getCount(): Int = list.size

                        override fun onMenuItemClick(position: Int): Boolean {
                            when (position) {
                                0 -> {
                                    pasteRows()
                                }
                                1 -> {
                                    copySelectedRows()
                                }
                                2 -> {
                                    cutSelectedRows()
                                }
                                3 -> {
                                    removeSelectedRows()
                                }
                            }
                            return true
                        }

                        override fun getMenuItem(
                            context: Context,
                            position: Int
                        ): SpeedDialMenuItem = list[position]

                        override fun getBackgroundColour(position: Int): Int {
                            return getColorFromRes(R.color.colorAccent)
                        }
                    }
                    fbAddRow.setButtonIconResource(R.drawable.ic_more_setting)
                    fbAddRow.setButtonBackgroundColour(getColorFromRes(R.color.colorAccent))
                    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_check)
                }
                else -> {
                    menuInflater.inflate(R.menu.card_main_menu, menu)
                    fbAddRow.speedDialMenuAdapter = null
                    fbAddRow.setButtonIconResource(R.drawable.ic_add_not_ring_white)
                    fbAddRow.setButtonBackgroundColour(getColorFromRes(R.color.colorSecondaryDark))
                    // тут именно это пусть будет
                    Handler(Looper.getMainLooper()).post { waitForAnimationsToFinish() }
                    if (fbAddRow.isShown) {
                        if (!appBar.isShown)
                            fbAddRow.hide()
                    }
                    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home)
                }
            }
            if (selectMode != SelectMode.NONE)
                fbAddRow.hide()
        })

    }

    private fun createViewModel(card: Card) {
        viewModel = ViewModelProvider(
            this, ViewModelCardFactory(
                this,
                card
            )
        ).get(CardViewModel::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.card_main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CARD_EDIT_JSON_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {

            }
        }
        if (requestCode == PICK_IMAGE_MULTIPLE) {
            supportFragmentManager.fragments.find { it.tag == editCellTag }
                ?.onActivityResult(requestCode, resultCode, data)
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
                editCardRegister.startPrefActivity(
                    PrefCardInfo.CardCategory.CARD,
                    activity = this,
                    card = viewModel!!.card,
                    title = getString(R.string.setting)
                )
            }
            // cell
            R.id.editCell -> {
                editCell()
            }
            R.id.pasteCell -> {
                pasteCell()

            }
            R.id.copyCell -> {
                copySelectedCell()
            }
            R.id.cutCell -> {
                cutSelectedCell()
            }
            // row
            R.id.deleteRow -> {
                removeSelectedRows()
            }
            R.id.cutRow -> {
                cutSelectedRows()
            }
            R.id.copyRow -> {
                copySelectedRows()
            }
            R.id.pasteRow -> {
                pasteRows()
            }
            R.id.duplicateRow -> {
                duplicateRows()
            }

            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun duplicateRows() {
        viewModel?.duplicateRows()
        scrollToPosition(viewModel!!.card.rows.size)
    }

    private fun pasteRows() {
        viewModel?.apply {
            pasteRows()
            selectMode.value = SelectMode.NONE
        }

    }

    private fun cutSelectedRows() {
        viewModel?.copySelectedRows()
        removeSelectedRows()
    }

    private fun copySelectedRows() {
        viewModel?.copySelectedRows()
        viewModel?.selectMode?.value = SelectMode.ROW
    }

    private fun cutSelectedCell() {
        viewModel?.copySelectedCell(true)
        notifyAdapter()
    }

    private fun copySelectedCell() {
        viewModel?.copySelectedCell(false)
    }

    private fun pasteCell() {
        // на вход принимается функция которая должна обновить строку после вставки
        viewModel?.pasteCell(app().copyCell) {
            // обновление строки после вставки данных
            adapter.notifyItemChanged(it)
        }
    }

    private fun removeSelectedRows() {
        viewModel?.apply {
            card.getSelectedRows().forEach {
                it.status = Row.Status.DELETED
            }
        }
        notifyAdapter()
    }

    // выполняем что ни будь и рекуклер обновляется после конца анимации
    private fun recyclerRunEvent(runEventRecycler: () -> Unit) { // ...
        runEventRecycler()
        Handler(Looper.getMainLooper()).post(waitForAnimationsToFinishRunnable)
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
            Handler(Looper.getMainLooper()).post(waitForAnimationsToFinishRunnable)
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
        viewModel?.card?.let {
            totalAmountView.hideAddTotalButton(it)
        }
    }

    private fun hideViewWhileScroll() {
        val animListener = object :
            Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                viewModel?.selectMode?.value?.let { selectMode ->
                    if (selectMode != SelectMode.NONE) {
                        if (!fbAddRow.isShown)
                            fbAddRow.show()
                    } else {
                        if (totalAmountView.translationY == 0f)
                            fbAddRow.show()
                        if (totalAmountView.translationY == totalAmountView.height.toFloat())
                            fbAddRow.hide()
                    }
                }
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }


        }
        totalAmountView.animate().setListener(animListener)
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->

            if (viewModel?.selectMode?.value != SelectMode.NONE)
                return@OnOffsetChangedListener
            val heightToolbar = appBarLayout.toolbar.height

            val isHide = -verticalOffset == heightToolbar
            val isShow = verticalOffset == 0

//            containerPlate.apply {
            if (isShow) {
                fbAddRow.show()
//                    animate().translationY(0f)
            }
            if (isHide) {
                fbAddRow.hide()
//                    animate().translationY(height.toFloat())

            }
//            }
        })
    }

    override fun onBackPressed() {
        if (fbAddRow.isSpeedDialMenuOpen)
            fbAddRow.closeSpeedDialMenu()
        else
            viewModel?.apply {
                selectMode.value?.let { selectMode1 ->
                    if (selectMode1 != SelectMode.NONE) {
                        unSelect()
                    } else {
                        updatedCardStatus.observe(this@CardActivity) {
                            if (!it)
                                finish()
                        }
                    }
                }
            } ?: finish()
    }

    private val rowClickListener = object : RowClickListener {
        override fun cellClick(rowPosition: Int, cellPosition: Int) {
            if (isLongClick) {
                viewModel?.rowClicked(rowPosition) {
                    notifyAdapter()
                    adapter.notifyItemChanged(rowPosition)
                }
            } else {
                if (viewModel!!.selectMode.value == SelectMode.ROW) {
                    isLongClick = true
                    cellClick(rowPosition, cellPosition)
                    return
                }
                viewModel?.cellClicked(
                    rowPosition,
                    cellPosition
                ) { isDoubleTap ->
                    if (isDoubleTap) {
                        editCell()
                    }
                }
            }
        }
    }

    private fun editCell() {
        viewModel?.editCell()
    }

    override fun onResume() {
        super.onResume()
        launch.invokeOnCompletion {
            viewModel?.apply {
                val value = selectMode.value
                selectMode.value = value
            }

            fbAddRow.setOnClickListener {
                viewModel?.apply {
                    when (selectMode.value!!) {
                        // если нажали в режиме выбора ячейки
                        SelectMode.CELL -> {
                            fbAddRow.openSpeedDialMenu()
                        }
                        // если нажали в режиме выбора строк
                        SelectMode.ROW -> {
                            fbAddRow.openSpeedDialMenu()
                        }
                        // если нажали в простое
                        else -> CoroutineScope(Dispatchers.Main).launch {
                            val rowAdded = withContext(Dispatchers.IO) {
                                addRow()
                            }
//                            notifyAdapter()
                            // обновляем в начале так как отчет идет в самой карточке
                            updateTotals()
                            scrollToPosition(sortedRows.indexOf(rowAdded) + 1)

                        }
                    }
                }
            }
            adapter.setCellClickListener(rowClickListener)
            // наблюдатель для события выделения ячейки
            selectedModeObserve()
            hideUnnecessaryElementsFromTotalAmount()
        }
    }

    private fun scrollToPosition(position: Int) {
        recycler.scrollToPosition(position) //  у нас на одну больше из за отступа для плейт
        appBar.setExpanded(false, true)
    }

    private fun CardViewModel.editCell() {
        val column = card.columns[cellSelectPosition]
        val selectCell = sortList()[rowSelectPosition].cellList[cellSelectPosition]

        EditCellControl(
            this@CardActivity,
            column,
            selectCell.sourceValue
        ) { newValue ->
            selectCell.sourceValue = newValue
            viewModel?.updateCardIntoDB()
            updateTypeControlColumn(cellSelectPosition)
            if (column is NumberColumn) {
                card.columns.filterIsInstance<NumberColumn>().forEach {
                    updateTypeControlColumn(it)
                }
            }
            adapter.notifyItemChanged(rowSelectPosition)

        }.editCell()
    }

}