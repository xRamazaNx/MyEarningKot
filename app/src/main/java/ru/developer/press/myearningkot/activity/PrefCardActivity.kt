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
import android.widget.*
import androidx.core.view.forEach
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.android.synthetic.main.card.*
import kotlinx.android.synthetic.main.card.datePeriodCard
import kotlinx.android.synthetic.main.card.nameCard
import kotlinx.android.synthetic.main.card.view.*
import kotlinx.android.synthetic.main.total_item.view.totalValue
import kotlinx.android.synthetic.main.total_item_layout.view.*
import kotlinx.android.synthetic.main.width_seek_bar_layout.*
import kotlinx.android.synthetic.main.width_seek_bar_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent
import ru.developer.press.myearningkot.viewmodels.CardViewModel
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.viewmodels.ViewModelCardFactory
import ru.developer.press.myearningkot.dialogs.DialogBasicPrefCard
import ru.developer.press.myearningkot.dialogs.DialogSetName
import ru.developer.press.myearningkot.model.*
import ru.developer.press.myearningkot.helpers.PrefCardInfo
import ru.developer.press.myearningkot.helpers.prefLayouts.*
import ru.developer.press.myearningkot.helpers.prefLayouts.ElementPrefType.*
import ru.developer.press.myearningkot.helpers.SampleHelper
import ru.developer.press.myearningkot.helpers.showItemChangeDialog
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.negativeButton
import splitties.alertdialog.appcompat.positiveButton


val CARD_ID = "card_id"
val PREF_CARD_INFO_JSON = "card_category_json"
val TITLE_PREF_ACTIVITY = "title_pref_activity"
val CARD_EDIT_JSON_REQ_CODE = 0

class PrefCardActivity : BasicCardActivity() {
    private var seekBarWindow: PopupWindow? = null
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
    private val addTotalImageButton: ImageView
        get() {
            return if (totalContainerDisableScroll.childCount > 0)
                totalContainerDisableScroll.getChildAt(1) as ImageView
            else
                totalContainerScroll.getChildAt(1) as ImageView
        }

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
            viewModel = ViewModelProvider(
                this@PrefCardActivity, ViewModelCardFactory(
                    card!!
                )
            ).get(
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

        selectedControl.showWidthSeekBar = { selectedElementList ->

            var isFirstChangeProgress = true
            val columnList =
                getColumnsFromSelectedElements(selectedElementList)

            val listTotal = mutableListOf<TotalItem>().apply {
                selectedElementList.filterIsInstance(
                    SelectedElement.ElementTotal::class.java
                )
                    .forEach {
                        add(viewModel!!.card.totals[it.index])
                    }
            }

            fun widthChanged() {
                if (selectedControl.selectPrefType == COLUMN) {

                    createTitles()
                    createRecyclerView()
                    clickPrefToAdapter()
                    initElementClick()

                    widthDrawer.positionList.clear()
                    widthDrawer.invalidate()
                } else {
                    updatePlate()
                }
                // фуакция на изменение ширины колоны
            }

            fun widthProgress(progress: Int) {
                if (selectedControl.selectPrefType == COLUMN) {

                    columnList.forEach { column ->
                        column.width = progress
                        val index = viewModel!!.card.columns.indexOf(column)
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
                } else {

                    listTotal.forEach { total ->
                        total.width = progress
                        val index = viewModel!!.card.totals.indexOf(total)
                        totalContainer.totalValueContainer.getChildAt(
                            index
                        )
                            .layoutParams.width =
                            total.width
                        totalContainer.totalTitleContainer.getChildAt(
                            index
                        )
                            .layoutParams.width =
                            total.width
                    }
                    totalContainer.requestLayout()
                }

            }

            if (seekBarWindow == null) {
                seekBarWindow = PopupWindow(this).also { popupWindow ->
                    popupWindow.contentView =
                        layoutInflater.inflate(R.layout.width_seek_bar_layout, null)
                    popupWindow.width = matchParent
                }
            }
            val seekBar = seekBarWindow!!.contentView.widthColumnSeekBar
            seekBar?.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    p0: SeekBar?,
                    p1: Int,
                    p2: Boolean
                ) {
                    if (isFirstChangeProgress) {
                        isFirstChangeProgress = false
                        return
                    }
                    val progress = p0!!.progress
                    if (progress > 30) {

                        widthProgress(progress)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    widthChanged()
                }
            })

            if (!seekBarWindow!!.isShowing) {
                if (selectedControl.selectPrefType == COLUMN)
                    seekBarWindow!!.showAsDropDown(columnContainer)
                else{
//                    seekbarLayout.animate().translationY(-(totalAmountView.height).toFloat())
                    seekBarWindow!!.showAsDropDown(totalAmountView, 0, -totalAmountView.height*2)
                }
            }
            var progress = 0
            if (columnList.isNotEmpty()) {
                var i = 0
                columnList.forEach {
                    i += it.width
                }
                progress = i / columnList.size
            }
            if (listTotal.isNotEmpty()) {
                var i = 0
                listTotal.forEach {
                    i += it.width
                }
                progress = i / listTotal.size
            }
            seekBar?.progress = progress
        }
        selectedControl.hideWidthSeekBar =
            { seekBarWindow?.dismiss() }

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
            totalAmountView.animate().translationY(0f)
            selectedControl.unSelectAll()
        }
    }

    fun showPrefWindow(view: View) {
//        if (y > 0) {
//            view.post {
//                totalAmountView.animate().translationY(-recycler.height.toFloat()+y)
//            }
//        }
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

        addTotalImageButton.setOnClickListener {
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
            R.id.addColumn -> {
                val list = getColumnTypeList()
                showItemChangeDialog(getString(R.string.change_type_data), list, -1, null) {
                    val columnType = getColumnTypeEnumList()[it]
                    // если семпл настраиваем то добавляем колону с семплами
                    if (prefCardInfo.cardCategory == PrefCardInfo.CardCategory.SAMPLE)
                        viewModel?.addColumnSample(columnType, list[it])
                    else
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
            R.id.renameElement -> {
                selectedControl.rename()
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

    fun getColumnsFromSelectedElements(selectedElementList: List<SelectedElement>): MutableList<Column> {
        return mutableListOf<Column>().apply {
            selectedElementList.filterIsInstance(SelectedElement.ElementColumn::class.java)
                .forEach {
                    val columnIndex = it.columnIndex
                    val column = viewModel!!.card.columns[columnIndex]
                    add(column)
                }
        }
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
                            val selectedColumn =
                                selectedElement as SelectedElement.ElementColumn
                            val columnIndex = selectedColumn.columnIndex
//                            val column = card.columns[columnIndex]
                            // назначение ячейкам что они выбраны
                            viewModel?.selectionColumn(columnIndex, true)
                            // обновление
                            // для обновления ширины задать а потом убрать
//                            adapter.updateWidthIndex = columnIndex
//                            adapter.widthSelected = column.width
                            notifyAdapter()
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
                            val selectTotalItem =
                                selectedElement as SelectedElement.ElementTotal
                            val index = selectTotalItem.index
                            val totalView =
                                totalContainer.totalValueContainer.getChildAt(index).totalValue
                            setSelectBackground(totalView)
                        }
                        ElementType.TOTAL_TITLE -> {
                            val selectTotalItem =
                                selectedElement as SelectedElement.ElementTotal
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
                            notifyAdapter()
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
                            val selectTotalItem =
                                selectedElement as SelectedElement.ElementTotal
                            val index = selectTotalItem.index
                            val totalView =
                                totalContainer.totalValueContainer.getChildAt(index).totalValue
                            totalView.background = selectedElement.oldDrawable
                        }
                        ElementType.TOTAL_TITLE -> {
                            val selectTotalItem =
                                selectedElement as SelectedElement.ElementTotal
                            val index = selectTotalItem.index
                            val totalView = totalContainer.totalTitleContainer.getChildAt(index)
                            totalView.background = selectedElement.oldDrawable
                        }
                    }

                }

                override fun showPref(elementPref: ElementPref) {
                    val prefLayout =
                        when (elementPref.elementPrefType) {
                            TEXT_VIEW -> {
                                var isWorkAlignPanel = true

                                // проверка для настройки выравнивания
                                elementPref.selectedElementList.forEach {
                                    val elementType = it.elementType
                                    if (elementType == ElementType.NAME
                                        || elementType == ElementType.DATE
                                        || elementType == ElementType.TOTAL_TITLE
                                    )
                                        isWorkAlignPanel = false
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
                                getPrefTextLayout(prefList, isWorkAlignPanel,
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
                                                        card.totals[elementTotal.index].title =
                                                            text
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

                                val listTotal = mutableListOf<TotalItem>().apply {
                                    elementPref.selectedElementList.filterIsInstance(
                                        SelectedElement.ElementTotal::class.java
                                    )
                                        .forEach {
                                            add(card.totals[it.index])
                                        }
                                }
                                getPrefTotalLayout(
                                    listTotal,
                                    object : PrefTotalChangedCallBack {
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
                                    })
                            }

                            DATE_PERIOD -> {
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

                                        override fun prefChanged() {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                withContext(Dispatchers.IO) {
                                                    columnList.forEach {
                                                        viewModel?.updateTypeControlColumn(it)
                                                    }
                                                }
                                                notifyAdapter()
                                                updatePlate()
                                            }
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
                            backgroundColorResource = R.color.colorBackground
                        }
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

                override fun setVisiblePrefButton(isVisible: Boolean) {
                    toolbar.menu.findItem(R.id.elementSetting).isVisible = isVisible
                    visibleButton(selectedControl.selectPrefType, selectedControl.isRenameMode)

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
                                toast(getString(R.string.cannot_delete_numbering_column))
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
                                    //#edit надо тут показать окошко с этим сообщением
                                    toast("Нельзя удалить единственный итог, если хотите отключить итоговую панель, отключите ее в настройках карточки")
                                }
                            }
                        }
                        initElementClick()
                    }
                }

                override fun rename(selectedElementList: MutableList<SelectedElement>) {
                    val element = selectedElementList[0]
                    val text = when (element.elementType) {
                        ElementType.COLUMN_TITLE -> {
                            val columnTitleElement =
                                element as SelectedElement.ElementColumnTitle
                            val column = card.columns[columnTitleElement.columnIndex]
                            column.name
                        }
                        ElementType.TOTAL_TITLE -> {
                            val totalElement = element as SelectedElement.ElementTotal
                            card.totals[totalElement.index].title

                        }
                        else -> {
                            card.name
                        }
                    }

                    DialogSetName {
                        when (element.elementType) {
                            ElementType.COLUMN_TITLE -> {
                                val columnTitleElement =
                                    element as SelectedElement.ElementColumnTitle
                                val column = card.columns[columnTitleElement.columnIndex]
                                column.name = it
                                val columnTitle =
                                    columnContainer.getChildAt(columnTitleElement.columnIndex) as TextView
                                columnTitle.text = it
                            }
                            ElementType.TOTAL_TITLE -> {
                                val totalElement = element as SelectedElement.ElementTotal
                                card.totals[totalElement.index].title = it
                                updatePlate()
                            }
                            else -> {
                                card.name = it
                                updatePlate()
                            }
                        }
                    }.setFirstName(text)
                        .show(supportFragmentManager, "setNameElement")
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
            visibleButton(selectedControl.selectPrefType, selectedControl.isRenameMode)
        }

    }

    private fun visibleButton(prefType: ElementPrefType, isRenameMode: Boolean) {
        val isColumnOrTotal = prefType == TOTAL || prefType == COLUMN
        toolbar.menu.findItem(R.id.moveToRight).isVisible = isColumnOrTotal
        toolbar.menu.findItem(R.id.moveToLeft).isVisible = isColumnOrTotal
        toolbar.menu.findItem(R.id.deleteColumn).isVisible = isColumnOrTotal
        toolbar.menu.findItem(R.id.renameElement).isVisible = isRenameMode
    }

    private fun updatePlate() {
        viewModel?.updatePlateChanged()
        selectedControl.updateSelected()
        initClickTotals(viewModel?.card)
    }

}
