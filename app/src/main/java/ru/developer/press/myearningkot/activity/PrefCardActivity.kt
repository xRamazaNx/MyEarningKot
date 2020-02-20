package ru.developer.press.myearningkot.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.forEach
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.android.synthetic.main.card.*
import kotlinx.android.synthetic.main.card.datePeriodCard
import kotlinx.android.synthetic.main.card.divide_line
import kotlinx.android.synthetic.main.card.nameCard
import kotlinx.android.synthetic.main.card.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent
import ru.developer.press.myearningkot.CardViewModel
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.ViewModelCardFactory
import ru.developer.press.myearningkot.dialogs.DialogBasicPrefCard
import ru.developer.press.myearningkot.model.*
import ru.developer.press.myearningkot.otherHelpers.PrefCardInfo
import ru.developer.press.myearningkot.otherHelpers.PrefLayouts.*
import ru.developer.press.myearningkot.otherHelpers.SampleHelper
import ru.developer.press.myearningkot.otherHelpers.showItemChangeDialog
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.negativeButton
import splitties.alertdialog.appcompat.positiveButton
import java.lang.Thread.sleep


val CARD_ID = "card_id"
val PREF_CARD_INFO_JSON = "card_category_json"
val TITLE_PREF_ACTIVITY = "title_pref_activity"
val CARD_EDIT_JSON_REQ_CODE = 0

class PrefCardActivity : BasicCardActivity() {
    private lateinit var prefCardInfo: PrefCardInfo
    private val selectedControl = PrefSelectedControl()
    lateinit var prefWindow: PopupWindow
    override var viewModel: CardViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = intent.getStringExtra(TITLE_PREF_ACTIVITY)
        fbAddRow.visibility = GONE
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_check)

        visibleHideElements()
        disableBehavior()

        CoroutineScope(Dispatchers.Main).launch {

            val card: Card? = withContext(Dispatchers.IO) {
                val prefCardJson = intent.getStringExtra(PREF_CARD_INFO_JSON)
                prefCardInfo = Gson().fromJson(prefCardJson, PrefCardInfo::class.java)

                if (prefCardInfo.cardCategory == PrefCardInfo.CardCategory.CARD)
                    DataController().getCard(prefCardInfo.idCard)
                else {
                    SampleHelper().getSample(prefCardInfo.idCard)
                }
            }
            viewModel =
                ViewModelProviders.of(this@PrefCardActivity, ViewModelCardFactory(card!!)).get(
                    CardViewModel::
                    class.java
                )
            initSelectCallback()
            progressBar.visibility = GONE
            doStart()
            initElementClick()

        }

        initPrefPopupWindow()

        KeyboardVisibilityEvent.setEventListener(this) {
            //            val height =
//                if (it) resources.displayMetrics.heightPixels / 5
//                else (resources.displayMetrics.heightPixels / 2.2).toInt()
//            if (prefWindow.isShowing)
//                prefWindow.update(matchParent, height)
        }

    }

    private fun initPrefPopupWindow() {
        prefWindow = PopupWindow(this)
//        prefWindow.height = (resources.displayMetrics.heightPixels / 2)
        prefWindow.height = wrapContent
        prefWindow.width = matchParent
        prefWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        prefWindow.isFocusable = true
        prefWindow.isOutsideTouchable = true
        prefWindow.animationStyle = R.style.popup_window_animation
//        prefWindow.setOnDismissListener {
//            selectedControl.unSelect(selectedElement)
//        }
    }

    fun showPrefWindow(view: View) {
        view.minimumHeight = (resources.displayMetrics.heightPixels / 3)
        prefWindow.contentView = view
        prefWindow.showAtLocation(totalAmountView, Gravity.BOTTOM, 0, 0)

    }

    private fun clickPrefToAdapter() {
        adapter.setCellClickPref { columnIndex ->
            selectColumn(columnIndex)
        }
    }

    private fun selectColumn(columnIndex: Int) {
        selectedControl.select(
            SelectedElement.ElementColumn(
                columnIndex,
                ElementType.COLUMN,
                null
            ).apply {
                columnType = viewModel!!.card.columns[columnIndex].getType()
            }
        )
    }

    private fun initElementClick() {
        clickPrefToAdapter()

        val card = viewModel?.card
        card?.columns?.forEachIndexed { index, _ ->
            columnContainer.getChildAt(index).setOnClickListener {
                selectedControl.select(
                    SelectedElement.ElementColumnTitle(
                        index,
                        ElementType.COLUMN_TITLE,
                        it.background
                    )
                )
            }
        }

        nameCard.setOnClickListener {
            selectedControl.select(
                SelectedElement.ElementTextView(
                    nameCard.background,
                    ElementType.NAME
                )
            )

        }
        datePeriodCard.setOnClickListener {
            selectedControl.select(
                SelectedElement.ElementTextView(
                    datePeriodCard.background,
                    ElementType.DATE
                )
            )
        }

        sumTitle.setOnClickListener {
            selectedControl.select(
                SelectedElement.ElementTextView(
                    sumTitle.background,
                    ElementType.SUM_TITLE
                )
            )

        }
        sum.setOnClickListener {
            selectedControl.select(
                SelectedElement.ElementTextView(
                    sum.background,
                    ElementType.SUM
                )
            )

        }
        avansTitle.setOnClickListener {
            selectedControl.select(
                SelectedElement.ElementTextView(
                    avansTitle.background,
                    ElementType.AVANS_TITLE
                )
            )

        }
        avans.setOnClickListener {
            selectedControl.select(
                SelectedElement.ElementTextView(
                    avans.background,
                    ElementType.AVANS
                )
            )

        }
        balanceTitle.setOnClickListener {
            selectedControl.select(
                SelectedElement.ElementTextView(
                    balanceTitle.background,
                    ElementType.BALANCE_TITLE
                )
            )

        }
        balance.setOnClickListener {
            selectedControl.select(
                SelectedElement.ElementTextView(
                    balance.background,
                    ElementType.BALANCE
                )
            )

        }

    }

    private fun visibleHideElements() {

        totalAmountView.apply {
            datePeriodCard.visibility = VISIBLE
            divide_line.visibility = VISIBLE
            nameCard.visibility = VISIBLE
        }
//        elementPrefContainerScrollView.visibility = VISIBLE
//        elementPrefContainer.layoutParams.height = resources.displayMetrics.heightPixels / 3
//        elementPrefContainerScrollView.requestLayout()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.card_pref_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.basicPref -> {
                viewModel?.card.let { card ->
                    val dialogBasicPrefCard = DialogBasicPrefCard(card!!) {

                        // ps покрывает обновление всех четырех настроек которые настраиваются в диалоге
                        CoroutineScope(Dispatchers.Main).launch {
                            withContext(Dispatchers.IO) {
                                adapter = getAdapterForRecycler()
                            }
                            updateHorizontalScrollSwitched()
                            recycler.adapter = adapter
                            clickPrefToAdapter()
                            totalAmountView.setShowTotalInfo(card.isShowTotalInfo)//
                        }


                    }
                    dialogBasicPrefCard.show(supportFragmentManager, "dialogBasicPrefCard")
                }
            }
            R.id.addColumn -> {
                val list = getColumnTypeList()
                showItemChangeDialog(getString(R.string.change_type_data), list, -1, null) {
                    val columnType = getColumnTypeEnumList()[it]
                    viewModel?.addColumn(columnType, list[it])

                    createTitlesFromCard()
                    updateHorizontalScrollSwitched()
                    createRecyclerView()
                    // после создания адаптера надо зановго заложить умение выделяться
                    clickPrefToAdapter()
                    initElementClick()

                    horizontalScrollView.post {
                        horizontalScrollView.fullScroll(ScrollView.FOCUS_RIGHT)
                    }
                    selectedControl.unSelectAll()
                    val index = viewModel!!.card.columns.size - 1
                    selectColumn(index)

                }
            }
            R.id.elementSetting -> {
                selectedControl.showPref()
            }
            R.id.moveToRight -> {

                selectedControl.moveToRight()
            }
            R.id.moveToLeft -> {
                selectedControl.moveToLeft()
            }
            R.id.deleteColumn -> {
                selectedControl.deleteColumns()
            }
            android.R.id.home -> {
                setCardOfResult()
            }
        }
        return true

    }

    private fun disableBehavior() {
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = 0
    }

    override fun onBackPressed() {
        if (!selectedControl.unSelectAll())

            alertDialog(getString(R.string.save_changes)) {
                setMessage("")
                positiveButton(R.string.save) {
                    setCardOfResult()
                }
                negativeButton(R.string.cancel) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }.show()
    }

    private fun setCardOfResult() {
        CoroutineScope(Dispatchers.Main).launch {
            progressBar.visibility = VISIBLE
            withContext(Dispatchers.IO) {
                val card = viewModel?.card
                if (prefCardInfo.cardCategory == PrefCardInfo.CardCategory.CARD)
                    DataController().updateCard(card!!)
                else
                    SampleHelper().updateSample(card!!)
            }
            val data = Intent()
            data.putExtra(CARD_ID, prefCardInfo.idCard)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    private fun initSelectCallback() {
        selectedControl.apply {
            selectCallback = object : SelectCallback {
                val card = viewModel!!.card
                override fun select(selectedElement: SelectedElement) {
                    var isSelectTotalAmountElement = false
                    when (selectedElement.elementType) {
                        ElementType.COLUMN -> {
                            val selectedColumn = selectedElement as SelectedElement.ElementColumn
                            val columnIndex = selectedColumn.columnIndex
//                            val column = card.columns[columnIndex]
                            // назначение ячейкам что они выбраны
                            viewModel?.selectionColumn(columnIndex, true)
                            // обновление
                            // для обновления ширины задать а потом убрать
//                            adapter.updateWidthIndex = columnIndex
//                            adapter.widthSelected = column.width
                            adapter.notifyAdapter()
                        }
                        ElementType.COLUMN_TITLE -> {
                            val selectedColumnTitle =
                                selectedElement as SelectedElement.ElementColumnTitle
                            val columnIndex = selectedColumnTitle.columnIndex

                            // выделили заголовок
                            val columnView = columnContainer.getChildAt(columnIndex) as TextView
                            setSelectBackground(columnView)

                        }
                        ElementType.NAME -> {
                            isSelectTotalAmountElement = true
                            setSelectBackground(nameCard)
                        }
                        ElementType.DATE -> {
                            isSelectTotalAmountElement = true
                            setSelectBackground(datePeriodCard)

                        }
                        ElementType.SUM -> {
                            isSelectTotalAmountElement = true
                            setSelectBackground(sum)
                        }
                        ElementType.SUM_TITLE -> {
                            isSelectTotalAmountElement = true
                            setSelectBackground(sumTitle)
                        }
                        ElementType.AVANS -> {
                            isSelectTotalAmountElement = true
                            setSelectBackground(avans)
                        }
                        ElementType.AVANS_TITLE -> {
                            isSelectTotalAmountElement = true
                            setSelectBackground(avansTitle)
                        }
                        ElementType.BALANCE -> {
                            isSelectTotalAmountElement = true
                            setSelectBackground(balance)
                        }
                        ElementType.BALANCE_TITLE -> {
                            isSelectTotalAmountElement = true
                            setSelectBackground(balanceTitle)
                        }
                    }
                }

                private fun changedCard() {
                    viewModel?.cardLiveData?.value = card
                }

                override fun unSelect(selectedElement: SelectedElement?) {

                    when (selectedElement?.elementType) {
                        ElementType.COLUMN -> {
                            val elementColumn = selectedElement as SelectedElement.ElementColumn
                            viewModel?.selectionColumn(elementColumn.columnIndex, false)
                            // обновление
                            adapter.notifyAdapter()
                            // удаление вью для настройки
                        }
                        ElementType.COLUMN_TITLE -> {
                            val elementColumn = selectedElement as SelectedElement.ElementColumnTitle
                            val columnIndex = elementColumn.columnIndex
                            columnContainer.getChildAt(columnIndex).background =
                                elementColumn.oldDrawable

                        }
                        ElementType.SUM -> {
                            sum.background = selectedElement.oldDrawable
                        }
                        ElementType.SUM_TITLE -> {
                            sumTitle.background = selectedElement.oldDrawable
                        }
                        ElementType.AVANS -> {
                            avans.background = selectedElement.oldDrawable
                        }
                        ElementType.AVANS_TITLE -> {
                            avansTitle.background = selectedElement.oldDrawable
                        }
                        ElementType.BALANCE -> {
                            balance.background = selectedElement.oldDrawable
                        }
                        ElementType.BALANCE_TITLE -> {
                            balanceTitle.background = selectedElement.oldDrawable
                        }
                        ElementType.NAME -> {
                            nameCard.background = selectedElement.oldDrawable
                        }
                        ElementType.DATE -> {
                            datePeriodCard.background = selectedElement.oldDrawable
                        }
                    }

                }

                override fun showPref(elementPref: ElementPref) {

                    val prefLayout =
                        when (elementPref.elementPrefType) {
                            ElementPrefType.TEXT_VIEW -> {
                                var isWorkAlignPanel = true
                                var name: String? = null

                                when (elementPref.selectedElementList[0].elementType) {
                                    ElementType.COLUMN_TITLE -> {
                                        val elementColumn =
                                            elementPref.selectedElementList[0] as SelectedElement.ElementColumn
                                        val column = card.columns[elementColumn.columnIndex]
                                        name = column.name
                                    }
                                    ElementType.NAME -> {
                                        name = card.name
                                    }
                                    ElementType.SUM_TITLE -> {
                                        name = card.cardPref.sumTitle
                                    }
                                    ElementType.AVANS_TITLE -> {
                                        name = card.cardPref.avansTitle
                                    }
                                    ElementType.BALANCE_TITLE -> {
                                        name = card.cardPref.balanceTitle
                                    }

                                    else -> {
                                    }
                                }

                                elementPref.selectedElementList.forEach {
                                    val elementType = it.elementType
                                    if (elementType == ElementType.NAME
                                        || elementType == ElementType.DATE
                                        || elementType == ElementType.SUM
                                        || elementType == ElementType.SUM_TITLE
                                        || elementType == ElementType.AVANS
                                        || elementType == ElementType.AVANS_TITLE
                                        || elementType == ElementType.BALANCE
                                        || elementType == ElementType.BALANCE_TITLE
                                    )
                                        isWorkAlignPanel = false
                                    if (elementType == ElementType.DATE
                                        || elementType == ElementType.SUM
                                        || elementType == ElementType.AVANS
                                        || elementType == ElementType.BALANCE
                                    )
                                        name = null
                                }

                                val prefList = mutableListOf<PrefForTextView>()
                                elementPref.selectedElementList.forEach {
                                    val prefForTextView = when (it.elementType) {
                                        ElementType.COLUMN_TITLE -> {
                                            val elementColumn =
                                                it as SelectedElement.ElementColumn
                                            val column = card.columns[elementColumn.columnIndex]
                                            column.titlePref
                                        }
                                        ElementType.NAME -> card.cardPref.namePref

                                        ElementType.DATE -> card.cardPref.dateOfPeriodPref
                                        ElementType.SUM -> card.cardPref.sumPref
                                        ElementType.SUM_TITLE -> card.cardPref.sumTitlePref

                                        ElementType.AVANS -> card.cardPref.avansPref
                                        ElementType.AVANS_TITLE -> card.cardPref.avansTitlePref

                                        ElementType.BALANCE -> card.cardPref.balancePref
                                        ElementType.BALANCE_TITLE -> card.cardPref.balanceTitlePref

                                        ElementType.COLUMN -> null
                                    }
                                    if (prefForTextView != null) {
                                        prefList.add(prefForTextView)
                                    }
                                }
                                getPrefTextLayout(name, prefList, isWorkAlignPanel,
                                    object : PrefTextChangedCallback {
                                        override fun nameEdit(text: String) {
                                            elementPref.selectedElementList.forEach {
                                                when (it.elementType) {
                                                    ElementType.COLUMN -> {
                                                    }
                                                    ElementType.COLUMN_TITLE -> {
                                                        val elementColumn =
                                                            it as SelectedElement.ElementColumn
                                                        val column =
                                                            card.columns[elementColumn.columnIndex]
                                                        column.name = text
                                                    }
                                                    ElementType.NAME -> {
                                                        card.name = text
                                                    }
                                                    ElementType.DATE -> {
                                                    }
                                                    ElementType.SUM -> {
                                                    }
                                                    ElementType.SUM_TITLE -> {
                                                        card.cardPref.sumTitle = text
                                                    }
                                                    ElementType.AVANS -> {
                                                    }
                                                    ElementType.AVANS_TITLE -> {
                                                        card.cardPref.avansTitle = text
                                                    }
                                                    ElementType.BALANCE -> {
                                                    }
                                                    ElementType.BALANCE_TITLE -> {
                                                        card.cardPref.balanceTitle = text
                                                    }
                                                }
                                            }
                                            prefChanged()
                                        }

                                        override fun prefChanged() {
                                            changedCard()
                                            viewModel?.updateColumnDL()
                                        }
                                    })
                            }
                            else -> {
                                val columnList =
                                    getColumnsFromSelectedElements(elementPref.selectedElementList)
                                getPrefColumnLayout(columnList, elementPref.columnType,
                                    object : PrefColumnChangedCallback {
                                        override fun widthChanged() {
                                            createTitlesFromCard()
                                            createRecyclerView()
                                            clickPrefToAdapter()
                                            initElementClick()

                                            widthDrawer.positionList.clear()
                                            widthDrawer.invalidate()
                                            // фуакция на изменение ширины колоны
                                        }

                                        override fun prefChanged() {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                withContext(Dispatchers.IO) {
                                                    columnList.forEach {
                                                        viewModel?.updateTypeControlColumn(it)
                                                    }
                                                }
                                                adapter.notifyAdapter()
                                                viewModel?.cardLiveData?.value = card
                                            }
                                        }

                                        override fun widthProgress() {
                                            columnList.forEach { column ->

                                                val index = card.columns.indexOf(column)
                                                columnContainer.getChildAt(index)
                                                    .layoutParams.width =
                                                    column.width
                                            }
                                            columnContainer.requestLayout()

                                            widthDrawer.positionList.clear()
                                            columnContainer.forEach {
                                                val title = it
                                                val rightPosition =
                                                    title.x + it.width - horizontalScrollView.scrollX
                                                widthDrawer.positionList.add(rightPosition)
                                            }

                                            widthDrawer.invalidate()
                                        }

                                        override fun recreateView() {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                withContext(Dispatchers.IO) {
                                                    adapter = getAdapterForRecycler()
                                                }
                                                updateHorizontalScrollSwitched()
                                                recycler.adapter = adapter
                                                clickPrefToAdapter()
                                            }
                                        }
                                    })
                            }
                        }
                    showPrefWindow(prefLayout.apply {
                        backgroundColorResource = R.color.cent
                    })
                }

                private fun reSelectColumnsAfterMove(columnList: MutableList<Column>) {
                    selectedControl.unSelectAll()
                    columnList.forEach { column ->
                        val columnIndex = card.columns.indexOf(column)
                        selectColumn(columnIndex)
                    }
                }

                private fun getColumnsFromSelectedElements(selectedElementList: MutableList<SelectedElement>): MutableList<Column> {
                    return mutableListOf<Column>().apply {
                        selectedElementList.filterIsInstance(SelectedElement.ElementColumn::class.java)
                            .forEach {
                                val columnIndex = it.columnIndex
                                val column = card.columns[columnIndex]
                                add(column)
                            }
                    }
                }

                override fun setVisiblePrefButton(isVisible: Boolean) {
                    toolbar.menu.findItem(R.id.elementSetting).isVisible = isVisible
                    visibleColumnSetButton(selectedControl.isColumnSelect)

                }

                override fun moveToRight(selectedElementList: MutableList<SelectedElement>) {

                    val columnList = getColumnsFromSelectedElements(selectedElementList)
                    viewModel?.moveToRight(columnList) {
                        if (!it)
                            toast(getString(R.string.moving_not_available))
                        else {
                            createTitlesFromCard()
                            createRecyclerView()
                            clickPrefToAdapter()
                            initElementClick()
                            reSelectColumnsAfterMove(columnList)
                        }
                    }
                }

                override fun moveToLeft(selectedElementList: MutableList<SelectedElement>) {
                    val columnList = getColumnsFromSelectedElements(selectedElementList)
                    viewModel?.moveToLeft(columnList) {
                        if (!it) toast(getString(R.string.moving_not_available))
                        else {
                            createTitlesFromCard()
                            createRecyclerView()
                            clickPrefToAdapter()
                            initElementClick()
                            selectedControl.unSelectAll()
                            reSelectColumnsAfterMove(columnList)
                        }
                    }
                }

                override fun deleteColumns(selectedElementList: MutableList<SelectedElement>) {
                    val columnList = getColumnsFromSelectedElements(selectedElementList)
                    selectedControl.unSelectAll()
                    columnList.forEach {
                        val deleteColumnResult: Boolean? = viewModel?.deleteColumn(it)
                        if (!deleteColumnResult!!) {
                            toast("Нельзя удалить столбец для нумерации")
                        }
                    }
                    createTitlesFromCard()
                    createRecyclerView()
                    clickPrefToAdapter()
                    initElementClick()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        toolbar.post {
            val isVisible = selectedControl.isSelect
            toolbar.menu.findItem(R.id.elementSetting).isVisible = isVisible
            visibleColumnSetButton(selectedControl.isColumnSelect)
        }

    }

    private fun visibleColumnSetButton(isVisible: Boolean) {
        toolbar.menu.findItem(R.id.moveToRight).isVisible = isVisible
        toolbar.menu.findItem(R.id.moveToLeft).isVisible = isVisible
        toolbar.menu.findItem(R.id.deleteColumn).isVisible = isVisible
    }

}
