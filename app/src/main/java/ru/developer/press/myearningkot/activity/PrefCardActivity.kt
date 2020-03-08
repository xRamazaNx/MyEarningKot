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
import android.widget.LinearLayout
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
import kotlinx.android.synthetic.main.card.nameCard
import kotlinx.android.synthetic.main.card.view.*
import kotlinx.android.synthetic.main.total_item.view.totalValue
import kotlinx.android.synthetic.main.total_item_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent
import ru.developer.press.myearningkot.CardViewModel
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.ViewModelCardFactory
import ru.developer.press.myearningkot.dialogs.DialogBasicPrefCard
import ru.developer.press.myearningkot.dialogs.DialogBasicPrefPlate
import ru.developer.press.myearningkot.model.*
import ru.developer.press.myearningkot.otherHelpers.PrefCardInfo
import ru.developer.press.myearningkot.otherHelpers.PrefLayouts.*
import ru.developer.press.myearningkot.otherHelpers.PrefLayouts.ElementPrefType.*
import ru.developer.press.myearningkot.otherHelpers.SampleHelper
import ru.developer.press.myearningkot.otherHelpers.showItemChangeDialog
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.negativeButton
import splitties.alertdialog.appcompat.positiveButton


val CARD_ID = "card_id"
val PREF_CARD_INFO_JSON = "card_category_json"
val TITLE_PREF_ACTIVITY = "title_pref_activity"
val CARD_EDIT_JSON_REQ_CODE = 0

class PrefCardActivity : BasicCardActivity() {
    private val totalContainer: LinearLayout
        get() {
            return if (totalContainerDisableScroll.childCount > 0)
                totalContainerDisableScroll.getChildAt(0) as LinearLayout
            else
                totalContainerScroll.getChildAt(0) as LinearLayout

        }
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
        prefWindow.setOnDismissListener {
            containerPlate.animate().translationY(0f)
        }
    }

    fun showPrefWindow(view: View, y: Int) {
        if (y > 0) {
            view.post {
                containerPlate.animate().translationY(-view.height.toFloat())
            }
        }
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
                    it.background,
                    ElementType.NAME
                )
            )

        }
        datePeriodCard.setOnClickListener {
            selectedControl.select(
                SelectedElement.ElementTextView(
                    it.background,
                    ElementType.DATE
                )
            )
        }

        initClickTotals(card)

        plateSetting.setOnClickListener {
            viewModel?.card.let { card ->
                val dialogBasicPrefCard = DialogBasicPrefPlate(card!!) {

                    // ps покрывает обновление всех четырех настроек которые настраиваются в диалоге
                    CoroutineScope(Dispatchers.Main).launch {
                        withContext(Dispatchers.IO) {
                            adapter = getAdapterForRecycler()
                        }
                        updateHorizontalScrollSwitched()
                        recycler.adapter = adapter
                        clickPrefToAdapter()
                        updatePlate()
                        setShowTotalInfo(card.isShowTotalInfo)//
                        initElementClick()
                        if (card.isShowTotalInfo)
                            selectedControl.updateSelected()
                        else
                            selectedControl.unSelectAll()
                    }


                }
                dialogBasicPrefCard.show(supportFragmentManager, "dialogBasicPrefCard")
            }
        }

        addTotal.setOnClickListener {
            viewModel?.addTotal()
            viewModel?.updatePlateChanged()
            initElementClick()
            selectedControl.unSelectAll()
            selectTotal(viewModel!!.card.totals.size - 1)
        }

    }

    private fun initClickTotals(card: Card?) {
        card?.totals?.forEachIndexed { index, _ ->
            // как в колоне надо сделать
            val titleContainer = totalContainer.totalTitleContainer
            val valueContainer = totalContainer.totalValueContainer
            // клик по значению
            valueContainer.getChildAt(index).totalValue.setOnClickListener {
                selectTotal(index)
            }
            // клик по заголовку
            titleContainer.getChildAt(index).setOnClickListener {
                selectedControl.select(
                    SelectedElement.ElementTotal(
                        index,
                        it.background,
                        ElementType.TOTAL_TITLE
                    )
                )
            }

        }
    }

    private fun visibleHideElements() {

        totalAmountView.apply {
            datePeriodCard.visibility = VISIBLE
            divide_line.visibility = VISIBLE
            nameCard.visibility = VISIBLE
        }
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
                            updatePlate()
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

                    createTitles()
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
                selectedControl.delete()
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
                            setSelectBackground(nameCard)
                        }
                        ElementType.DATE -> {
                            setSelectBackground(datePeriodCard)

                        }
                        ElementType.TOTAL -> {
                            val selectTotalItem = selectedElement as SelectedElement.ElementTotal
                            val index = selectTotalItem.index
                            val totalView =
                                totalContainer.totalValueContainer.getChildAt(index).totalValue
                            setSelectBackground(totalView)
                        }
                        ElementType.TOTAL_TITLE -> {
                            val selectTotalItem = selectedElement as SelectedElement.ElementTotal
                            val index = selectTotalItem.index
                            val totalView = totalContainer.totalTitleContainer.getChildAt(index)
                            setSelectBackground(totalView)
                        }
                    }
                }

                private fun changedCard() {
                    updatePlate()
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
                            val elementColumn =
                                selectedElement as SelectedElement.ElementColumnTitle
                            val columnIndex = elementColumn.columnIndex
                            columnContainer.getChildAt(columnIndex).background =
                                elementColumn.oldDrawable

                        }
                        ElementType.NAME -> {
                            nameCard.background = selectedElement.oldDrawable
                        }
                        ElementType.DATE -> {
                            datePeriodCard.background = selectedElement.oldDrawable
                        }
                        ElementType.TOTAL -> {
                            val selectTotalItem = selectedElement as SelectedElement.ElementTotal
                            val index = selectTotalItem.index
                            val totalView =
                                totalContainer.totalValueContainer.getChildAt(index).totalValue
                            totalView.background = selectedElement.oldDrawable
                        }
                        ElementType.TOTAL_TITLE -> {
                            val selectTotalItem = selectedElement as SelectedElement.ElementTotal
                            val index = selectTotalItem.index
                            val totalView = totalContainer.totalTitleContainer.getChildAt(index)
                            totalView.background = selectedElement.oldDrawable
                        }
                    }

                }

                override fun showPref(elementPref: ElementPref) {
                    var yOff = 0
                    val prefLayout =
                        when (elementPref.elementPrefType) {
                            TEXT_VIEW -> {
                                var isWorkAlignPanel = true
                                val name: String?

                                // проверка имени
                                val firstSelected = elementPref.selectedElementList[0]
                                when (firstSelected.elementType) {
                                    ElementType.COLUMN_TITLE -> {
                                        val elementColumn =
                                            firstSelected as SelectedElement.ElementColumnTitle
                                        val column = card.columns[elementColumn.columnIndex]
                                        name = column.name
                                    }
                                    ElementType.NAME -> {
                                        name = card.name
                                    }
                                    ElementType.TOTAL_TITLE -> {
                                        val selectTotalItem =
                                            firstSelected as SelectedElement.ElementTotal
                                        val index = selectTotalItem.index
                                        name = card.totals[index].title
                                    }
                                    else -> {
                                        name = null
                                    }
                                }

                                // проверка для настройки выравнивания
                                elementPref.selectedElementList.forEach {
                                    val elementType = it.elementType
                                    if (elementType == ElementType.NAME
                                        || elementType == ElementType.DATE
                                        || elementType == ElementType.TOTAL_TITLE
                                    )
                                        isWorkAlignPanel = false
                                }
                                if (!isWorkAlignPanel) {
                                    yOff = containerPlate.height
                                }
                                val prefList = mutableListOf<PrefForTextView>()

                                // сбор настроек
                                elementPref.selectedElementList.forEach {
                                    val prefForTextView = when (it.elementType) {
                                        ElementType.COLUMN_TITLE -> {
                                            val elementColumn =
                                                it as SelectedElement.ElementColumnTitle
                                            val column = card.columns[elementColumn.columnIndex]
                                            column.titlePref
                                        }
                                        ElementType.NAME -> card.cardPref.namePref

                                        ElementType.TOTAL_TITLE -> {
                                            val totalItem =
                                                card.totals[(it as SelectedElement.ElementTotal).index]
                                            totalItem.titlePref
                                        }
                                        else -> null
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

                                                    ElementType.COLUMN_TITLE -> {
                                                        val elementColumn =
                                                            it as SelectedElement.ElementColumnTitle
                                                        val column =
                                                            card.columns[elementColumn.columnIndex]
                                                        column.name = text
                                                    }
                                                    ElementType.NAME -> {
                                                        card.name = text
                                                    }
                                                    ElementType.TOTAL_TITLE -> {
                                                        val elementTotal =
                                                            it as SelectedElement.ElementTotal
                                                        card.totals[elementTotal.index].title = text
                                                    }
                                                    else -> {

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

                            TOTAL -> {
                                yOff = containerPlate.height

                                val listTotal = mutableListOf<TotalItem>().apply {
                                    elementPref.selectedElementList.filterIsInstance(SelectedElement.ElementTotal::class.java)
                                        .forEach {
                                            add(card.totals[it.index])
                                        }
                                }
                                getPrefTotalLayout(listTotal, object : PrefTotalChangedCallBack {
                                    override fun prefChanged() {
                                        updatePlate()
                                    }

                                    override fun calcFormula() {
                                        card.totals.forEach {
                                            it.calcFormula(card)
                                        }
                                        prefChanged()
                                    }

                                    override fun getNumberColumns(): MutableList<NumberColumn> {
                                        return card.columns.filterIsInstance<NumberColumn>()
                                            .toMutableList()
                                    }

                                    override fun getTotals(): List<TotalItem> = card.totals
                                    override fun widthProgress() {
                                        listTotal.forEach { total ->

                                            val index = card.totals.indexOf(total)
                                            totalContainer.totalValueContainer.getChildAt(index)
                                                .layoutParams.width =
                                                total.width
                                            totalContainer.totalTitleContainer.getChildAt(index)
                                                .layoutParams.width =
                                                total.width
                                        }
                                        containerPlate.requestLayout()
                                    }

                                    override fun widthChanged() {
                                    }
                                })
                            }

                            DATE_PERIOD -> {
                                yOff = containerPlate.height
                                getPrefDatePeriod(card.cardPref.dateOfPeriodPref,
                                    object : PrefChangedCallBack {
                                        override fun prefChanged() {
                                            changedCard()
                                        }
                                    })
                            }

                            else -> {
                                val columnList =
                                    getColumnsFromSelectedElements(elementPref.selectedElementList)
                                getPrefColumnLayout(columnList, elementPref.columnType,
                                    object : PrefColumnChangedCallback {
                                        override fun widthChanged() {
                                            createTitles()
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
                                                updatePlate()
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

                                        override fun getNumberColumns(): MutableList<NumberColumn> {
                                            val filterIsInstance =
                                                card.columns.filterIsInstance<NumberColumn>()
                                            return filterIsInstance.toMutableList()

                                        }
                                    })
                            }
                        }
                    //
                    showPrefWindow(
                        prefLayout.apply {
                            backgroundColorResource = R.color.cent
                        }, yOff
                    )
                }

                private fun reSelectAfterMove(list: List<Any>) {
                    val isColumn = selectedControl.selectPrefType == COLUMN
                    selectedControl.unSelectAll()
                    if (isColumn)
                        list.filterIsInstance<Column>().forEach { column ->
                            val columnIndex = card.columns.indexOf(column)
                            selectColumn(columnIndex)
                        }
                    else
                        list.filterIsInstance<TotalItem>().forEach { total ->
                            val columnIndex = card.totals.indexOf(total)
                            selectTotal(columnIndex)
                        }

                }

                private fun getColumnsFromSelectedElements(selectedElementList: List<SelectedElement>): MutableList<Column> {
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
                    visibleButton(selectedControl.selectPrefType)

                }

                override fun moveToRight(selectedElementList: List<SelectedElement>) {

                    if (selectedControl.selectPrefType == COLUMN) {
                        val columnList = getColumnsFromSelectedElements(selectedElementList)
                        viewModel?.moveToRightColumn(columnList) {
                            if (!it)
                                toast(getString(R.string.moving_not_available))
                            else {
                                createTitles()
                                createRecyclerView()
                                clickPrefToAdapter()
                                initElementClick()
                                reSelectAfterMove(columnList)
                            }
                        }
                    } else {
                        val totalList: List<TotalItem> =
                            getTotalsFromSelectedElements(selectedElementList)
                        viewModel?.moveToRightTotal(totalList) {
                            if (!it)
                                toast(getString(R.string.moving_not_available))
                            else {
                                initElementClick()
                                reSelectAfterMove(totalList)
                            }
                        }
                    }
                }

                private fun getTotalsFromSelectedElements(selectedElementList: List<SelectedElement>): List<TotalItem> {
                    return mutableListOf<TotalItem>().apply {
                        selectedElementList.filterIsInstance(SelectedElement.ElementTotal::class.java)
                            .forEach {
                                val totalIndex = it.index
                                val total = card.totals[totalIndex]
                                add(total)
                            }
                    }
                }

                override fun moveToLeft(selectedElementList: List<SelectedElement>) {
                    if (selectedControl.selectPrefType == COLUMN) {

                        val columnList = getColumnsFromSelectedElements(selectedElementList)
                        viewModel?.moveToLeftColumn(columnList) {
                            if (!it) toast(getString(R.string.moving_not_available))
                            else {
                                createTitles()
                                createRecyclerView()
                                clickPrefToAdapter()
                                initElementClick()
                                reSelectAfterMove(columnList)
                            }
                        }
                    } else {
                        val totalList = getTotalsFromSelectedElements(selectedElementList)
                        viewModel?.moveToLeftTotal(totalList) {
                            if (!it) toast(getString(R.string.moving_not_available))
                            else {
                                initElementClick()
                                reSelectAfterMove(totalList)
                            }
                        }
                    }
                }

                override fun delete(selectedElementList: List<SelectedElement>) {
                    if (selectedControl.selectPrefType == COLUMN) {

                        val columnList = getColumnsFromSelectedElements(selectedElementList)
                        selectedControl.unSelectAll()
                        columnList.forEach {
                            val deleteColumnResult: Boolean? = viewModel?.deleteColumn(it)
                            if (!deleteColumnResult!!) {
                                toast("Нельзя удалить столбец для нумерации")
                            }
                        }
                        createTitles()
                        createRecyclerView()
                        clickPrefToAdapter()
                        initElementClick()
                    } else {
                        val totalList = getTotalsFromSelectedElements(selectedElementList)
                        selectedControl.unSelectAll()
                        totalList.forEach {
                            val deleteTotal = viewModel?.deleteTotal(it)
                            deleteTotal?.let { delTotal ->
                                if (delTotal) {
                                    viewModel?.updatePlateChanged()
                                } else {
                                    toast("Нельзя удалить единственный итог, если хотите отключить итоговую панель, отключите ее в настройках карточки")
                                }
                            }
                        }
                        initElementClick()
                    }
                }
            }
        }
    }

    private fun selectTotal(totalIndex: Int) {
        selectedControl.select(
            SelectedElement.ElementTotal(
                totalIndex,
                totalContainer.totalValueContainer.getChildAt(totalIndex).background,
                ElementType.TOTAL
            )
        )
    }

    override fun onResume() {
        super.onResume()

        toolbar.post {
            val isVisible = selectedControl.isSelect
            toolbar.menu.findItem(R.id.elementSetting).isVisible = isVisible
            visibleButton(selectedControl.selectPrefType)
        }

    }

    private fun visibleButton(prefType: ElementPrefType) {
        val isColumnOrTotal = prefType == TOTAL || prefType == COLUMN
        toolbar.menu.findItem(R.id.moveToRight).isVisible = isColumnOrTotal
        toolbar.menu.findItem(R.id.moveToLeft).isVisible = isColumnOrTotal
        toolbar.menu.findItem(R.id.deleteColumn).isVisible = isColumnOrTotal
    }

    private fun updatePlate() {
        viewModel?.updatePlateChanged()
        selectedControl.updateSelected()
        initClickTotals(viewModel?.card)
    }

}
