package ru.developer.press.myearningkot.activity

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.animation.Animation
import androidx.core.view.postDelayed
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView.ItemAnimator.ItemAnimatorFinishedListener
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.android.synthetic.main.activity_card.view.*
import kotlinx.android.synthetic.main.card.view.*
import kotlinx.coroutines.*
import ru.developer.press.myearningkot.*
import ru.developer.press.myearningkot.CardViewModel.SelectMode
import ru.developer.press.myearningkot.adapters.animationAdd
import ru.developer.press.myearningkot.adapters.animationDelete
import ru.developer.press.myearningkot.dialogs.PICK_IMAGE_MULTIPLE
import ru.developer.press.myearningkot.dialogs.editCellTag
import ru.developer.press.myearningkot.dialogs.startPrefActivity
import ru.developer.press.myearningkot.model.*
import ru.developer.press.myearningkot.helpers.EditCellControl
import ru.developer.press.myearningkot.helpers.getColorFromRes
import uk.co.markormesher.android_fab.SpeedDialMenuAdapter
import uk.co.markormesher.android_fab.SpeedDialMenuItem
import java.lang.Runnable


open class CardActivity : BasicCardActivity() {
    override var viewModel: CardViewModel? = null
    private var cellClickTime: Long = 0
    private var isLongClick = false
    private val launch = CoroutineScope(Dispatchers.Main).launch {
        val id = intent.getLongExtra(CARD_ID, 0)
        val card = withContext(Dispatchers.IO) {
            DataController().getCard(id)
        }
        createViewModel(card)

        progressBar.visibility = GONE
        doStart()
        viewModel?.apply {
            titleLiveData.observe(this@CardActivity, Observer {
                title = it
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // внести все нужные события ид, подписки и т.д.
        hideUnnecessaryElementsFromTotalAmount()
        launch.start()
        tableView.isLong.observe(this, Observer {
            isLongClick = it
        })
        fbAddRow.setContentCoverColour(Color.TRANSPARENT)
        hideViewWhileScroll()

        // листенер для обновления после удаления то есть после ее анимации
        val animateListener = object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                CoroutineScope(Dispatchers.IO).launch {

                    viewModel?.apply {
                        deleteRows { indexDel: Int ->
                            launch(Dispatchers.Main) {
                                adapter.notifyItemRemoved(indexDel)

                            }
                        }
                        launch(Dispatchers.Main) {
                            updateTotals()
                            selectMode.value = SelectMode.NONE
                        }
                    }
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
        viewModel?.selectMode?.observe(this, Observer { selectMode ->
            menu.clear()
            when (selectMode) {
                SelectMode.CELL -> {
                    menuInflater.inflate(R.menu.cell_menu, menu)
                    // ставим иконку вставить в зависимости доступности вставки
                    if (viewModel!!.isEqualCellAndCopyCell()) {
                        menu.findItem(R.id.pasteCell).setIcon(R.drawable.ic_paste_white)
                    } else
                        menu.findItem(R.id.pasteCell).setIcon(R.drawable.ic_paste_white_disabled)

                    val speedAdapter = object : SpeedDialMenuAdapter() {

                        private val list = mutableListOf<SpeedDialMenuItem>().apply {
                            add(
                                SpeedDialMenuItem(
                                    this@CardActivity,
                                    if (viewModel!!.isEqualCellAndCopyCell())
                                        getDrawable(R.drawable.ic_paste_white)!!
                                    else
                                        getDrawable(R.drawable.ic_paste_white_disabled)!!
                                    ,
//                                    getString(R.string.PASTE)
                                    ""
                                )
                            )
                            add(
                                SpeedDialMenuItem(
                                    this@CardActivity,
                                    getDrawable(R.drawable.ic_copy_white)!!,
//                                    getString(R.string.COPY)
                                    ""
                                )
                            )
                            add(
                                SpeedDialMenuItem(
                                    this@CardActivity,
                                    getDrawable(R.drawable.ic_cut_white)!!,
                                    ""
//                                    getString(R.string.cut)
                                )
                            )
                            add(
                                SpeedDialMenuItem(
                                    this@CardActivity,
                                    getDrawable(R.drawable.ic_edit_white)!!,
//                                    getString(R.string.DELETE)
                                    ""
                                )
                            )
                        }

                        override fun getCount(): Int = list.size

                        override fun onMenuItemClick(position: Int): Boolean {
                            fbAddRow.closeSpeedDialMenu()
                            fbAddRow.postDelayed(100) {

                                when (position) {
                                    0 -> {
                                        pasteCell()
                                    }
                                    1 -> {
                                        copySelectedCell()
                                    }
                                    2 -> {
                                        cutSelectedCell()
                                    }
                                    3 -> {
                                        editCell()
                                    }
                                }
                            }
                            return true
                        }

                        override fun getMenuItem(
                            context: Context,
                            position: Int
                        ): SpeedDialMenuItem = list[position]

                        override fun getBackgroundColour(position: Int): Int {
                            return getColorFromRes(R.color.red_fb_button)
                        }
                    }
                    fbAddRow.speedDialMenuAdapter = speedAdapter
                    // при открытии меню проверка одного ли типа ячейки чтоб работала кнопка вставки или нет
                    fbAddRow.setButtonIconResource(R.drawable.ic_menu_3_line)
                    fbAddRow.setButtonBackgroundColour(getColorFromRes(R.color.red_fb_button))
                    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_check)
                }

                SelectMode.ROW -> {
                    menuInflater.inflate(R.menu.row_menu, menu)
                    if (viewModel!!.isCapabilityPaste()) {
                        menu.findItem(R.id.pasteRow).setIcon(R.drawable.ic_paste_white)
                    } else
                        menu.findItem(R.id.pasteRow).setIcon(R.drawable.ic_paste_white_disabled)
                    fbAddRow.speedDialMenuAdapter = object : SpeedDialMenuAdapter() {
                        private val list = mutableListOf<SpeedDialMenuItem>().apply {
                            add(
                                SpeedDialMenuItem(
                                    this@CardActivity,
                                    if (viewModel!!.isCapabilityPaste())
                                        getDrawable(R.drawable.ic_paste_white)!!
                                    else
                                        getDrawable(R.drawable.ic_paste_white_disabled)!!,
//                                    getString(R.string.PASTE)
                                    ""
                                )
                            )
                            add(
                                SpeedDialMenuItem(
                                    this@CardActivity,
                                    getDrawable(R.drawable.ic_copy_white)!!,
//                                    getString(R.string.COPY)
                                    ""
                                )
                            )
                            add(
                                SpeedDialMenuItem(
                                    this@CardActivity,
                                    getDrawable(R.drawable.ic_cut_white)!!,
                                    ""
//                                    getString(R.string.cut)
                                )
                            )
                            add(
                                SpeedDialMenuItem(
                                    this@CardActivity,
                                    getDrawable(R.drawable.ic_delete_white)!!,
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
                            return getColorFromRes(R.color.shape_select_border)
                        }
                    }
                    fbAddRow.setButtonIconResource(R.drawable.ic_menu_3_line)
                    fbAddRow.setButtonBackgroundColour(getColorFromRes(R.color.shape_select_border))
                    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_check)
                }
                else -> {
                    menuInflater.inflate(R.menu.card_main_menu, menu)
                    fbAddRow.speedDialMenuAdapter = null
                    fbAddRow.setButtonIconResource(R.drawable.ic_add_not_ring)
                    fbAddRow.setButtonBackgroundColour(getColorFromRes(R.color.greenButton))
                    // тут именно это пусть будет
                    Handler().post { waitForAnimationsToFinish() }
                    if (fbAddRow.isShown) {
                        if (!appBar.isShown)
                            fbAddRow.hide()
                    }
                    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_white)
                }
            }
        })

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
                            CoroutineScope(Dispatchers.IO).launch {
                                val card = DataController().getCard(id)
                                withContext(Dispatchers.Main) {
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
                startPrefActivity(
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
        viewModel?.pasteCell()
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
            this@CardActivity.prefButtonContainer.visibility = GONE
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
                        if (containerPlate.translationY == 0f)
                            fbAddRow.show()
                        if (containerPlate.translationY == containerPlate.height.toFloat())
                            fbAddRow.hide()
                    }
                }
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }


        }
        containerPlate.animate().setListener(animListener)
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->

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
                selectMode.value?.let {
                    if (it != SelectMode.NONE) {
                        unSelect()
                    } else
                        finish()
                }
            } ?: finish()
    }

    private val rowClickListener = object : RowClickListener {
        override fun cellClick(view: View, rowPosition: Int, cellPosition: Int) {
            if (isLongClick) {
                viewModel?.rowClicked(rowPosition) {
                    notifyAdapter()
                    adapter.notifyItemChanged(rowPosition)
                }
            } else {
                if (viewModel!!.selectMode.value == SelectMode.ROW) {
                    isLongClick = true
                    cellClick(view, rowPosition, cellPosition)
                    return
                }
                viewModel?.cellClicked(
                    rowPosition,
                    cellPosition
                ) { oldRowPosition, isDoubleTap ->
                    if (isDoubleTap) {
                        editCell()
                    } else {
                        adapter.notifyItemChanged(oldRowPosition)
                        adapter.notifyItemChanged(rowPosition)
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
        }
    }

    private fun updateTotals() {
        viewModel?.card?.updateTotalAmount(totalAmountView)
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
            updateTypeControlColumn(cellSelectPosition)
            if (column is NumberColumn) {
                card.columns.filterIsInstance<NumberColumn>().forEach {
                    updateTypeControlColumn(it)
                }
            }
            adapter.notifyDataSetChanged()
            updateCardInDB().invokeOnCompletion {
                updateTotals()
            }

        }.editCell()
    }

    private fun updateCardInDB() =
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                viewModel?.card?.let {
                    DataController().updateCard(it)
                }
            }
        }

}