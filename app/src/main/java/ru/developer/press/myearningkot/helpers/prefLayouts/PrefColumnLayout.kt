package ru.developer.press.myearningkot.helpers.prefLayouts

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import kotlinx.android.synthetic.main.pref_column_date.view.*
import kotlinx.android.synthetic.main.pref_column_image.view.*
import kotlinx.android.synthetic.main.pref_column_list.view.*
import kotlinx.android.synthetic.main.pref_column_number.view.*
import kotlinx.android.synthetic.main.pref_column_phone.view.*
import kotlinx.android.synthetic.main.pref_column_switch.view.*
import kotlinx.android.synthetic.main.toolbar_pref.view.*
import kotlinx.android.synthetic.main.width_seek_bar_layout.view.*
import kotlinx.coroutines.*
import org.jetbrains.anko.*
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.activity.PrefCardActivity
import ru.developer.press.myearningkot.adapters.AdapterRecyclerPhoneParams
import ru.developer.press.myearningkot.adapters.ParamModel
import ru.developer.press.myearningkot.database.DataController
import ru.developer.press.myearningkot.helpers.*
import ru.developer.press.myearningkot.model.*
import splitties.alertdialog.appcompat.alertDialog
import java.util.*

fun Context.getPrefColumnLayout(
    columns: MutableList<Column>,
    columnType: ColumnType,
    prefColumnChangedCallback: PrefColumnChangedCallback
): View {

    val columnLayout: PrefColumnLayout = when (columnType) {
        ColumnType.NUMERATION -> PrefNumerationColumnLayout(
            columns,
            prefColumnChangedCallback
        )
        ColumnType.TEXT -> PrefTextColumnLayout(
            columns,
            prefColumnChangedCallback
        )
        ColumnType.NUMBER -> PrefNumberColumnLayout(
            columns,
            prefColumnChangedCallback
        )

        ColumnType.PHONE -> PrefPhoneColumnLayout(
            columns,
            prefColumnChangedCallback
        )
        ColumnType.DATE -> PrefDateColumnLayout(
            columns,
            prefColumnChangedCallback
        )
        ColumnType.COLOR -> PrefColorColumnLayout(
            columns,
            prefColumnChangedCallback
        )
        ColumnType.SWITCH -> PrefSwitchColumnLayout(
            columns,
            prefColumnChangedCallback
        )
        ColumnType.IMAGE -> PrefImageColumnLayout(
            columns,
            prefColumnChangedCallback
        )
        ColumnType.LIST -> PrefListColumnLayout(
            columns,
            prefColumnChangedCallback
        )
        ColumnType.NONE -> PrefNoneColumnLayout(
            columns,
            prefColumnChangedCallback
        )
    }
    return columnLayout?.getPrefColumnView(this) ?: View(this)
}

// интерфейс для обратной связи когда надо показать изменения столбца в вью
interface PrefColumnChangedCallback {
    fun widthChanged()
    fun prefChanged()
    fun widthProgress()
    fun recreateView() //  для колоны переключателя
    fun getNumberColumns(): MutableList<NumberColumn>
}

abstract class PrefColumnLayout(
    val columnList: MutableList<Column>,
    val prefColumnChangedCallback: PrefColumnChangedCallback
) {
    abstract fun initBasicPref(view: View)

    protected fun initSeekBarAndToolbarButtons(view: View) {
        val widthColumnSeekBar = view.widthColumnSeekBar
        val column = columnList[0]
        widthColumnSeekBar.progress = column.width
        widthColumnSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val progress = p0!!.progress
                if (progress > 30) {
                    columnList.forEach {
                        it.width = progress
                    }
                    prefColumnChangedCallback.widthProgress()
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                prefColumnChangedCallback.widthChanged()
            }
        })

        view.defaultPref.setOnClickListener {
            columnList.forEach {
                it.setDefaultPref()
            }
            prefColumnChangedCallback.prefChanged()
            initBasicPref(view)
        }

    }

    abstract fun getPrefColumnView(context: Context): View
}

open class PrefTextColumnLayout(
    columnList: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefColumnLayout(columnList, prefColumnChangedCallback) {

    override fun initBasicPref(view: View) {
        initSeekBarAndToolbarButtons(view)

        val prefForTextViewList = getPrefForTextViewList()
        textPrefButtonsInit(view, prefForTextViewList) {
            prefColumnChangedCallback.prefChanged()
        }
    }

    protected fun getPrefForTextViewList(): MutableList<PrefForTextView> =
        mutableListOf<PrefForTextView>().apply {
            columnList.forEach {
                val prefForTextView = when (it) {
                    is NumerationColumn -> it.typePref.prefForTextView
                    is NumberColumn -> it.typePref.prefForTextView
                    is TextColumn -> it.typePref.prefForTextView
                    is PhoneColumn -> it.typePref.prefForTextView
                    is DateColumn -> it.typePref.prefForTextView
                    is ListColumn -> it.typePref.prefForTextView
                    else -> null
                }
                if (prefForTextView != null) {
                    add(prefForTextView)
                }
            }
        }


    override fun getPrefColumnView(context: Context): View {
        val view = context.layoutInflater.inflate(R.layout.pref_column_text, null)
        initBasicPref(view)
        return view
    }
}

class PrefNumberColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefTextColumnLayout(column, prefColumnChangedCallback) {

    override fun initBasicPref(view: View) {
        initSeekBarAndToolbarButtons(view)
        val numberColumn = columnList[0] as NumberColumn
        val numberColumns = columnList.filterIsInstance(NumberColumn::class.java)

        val typePref = numberColumn.typePref

        textPrefButtonsInit(view, getPrefForTextViewList()) {
            prefColumnChangedCallback.prefChanged()
        }

        val digitDown = view.digitsCountDown
        val digitUp = view.digitsCountUp
        val digitsSizeTextView = view.digitsSize
        val grouping = view.groupNumberSwitch

        digitsSizeTextView.text = typePref.digitsCount.toString()
        grouping.isChecked = typePref.isGrouping
        grouping.setOnCheckedChangeListener { _, b ->
            numberColumns.forEach {
                it.typePref.isGrouping = b
            }
            prefColumnChangedCallback.prefChanged()
        }

        val editDigit = fun(digitOffset: Int) {
            var digit = typePref.digitsCount
            digit += digitOffset

            if (digit < 0)
                digit = 0

            numberColumns.forEach {
                it.typePref.digitsCount = digit
            }
            prefColumnChangedCallback.prefChanged()
            digitsSizeTextView.text = digit.toString()
        }
        digitDown.setOnClickListener {
            editDigit(-1)
        }
        digitUp.setOnClickListener {
            editDigit(1)
        }

        //inputType
        val manualInput = view.manualInput
        val formulaInput = view.formula
        fun select(textView: TextView) {
            textView.textColorResource = R.color.colorAccent
        }

        fun unSelect(textView: TextView) {
            textView.textColorResource = R.color.textColorPrimary
        }
        if (numberColumn.inputType == InputTypeNumberColumn.MANUAL) {
            select(manualInput)
            unSelect(formulaInput)
        } else {
            select(formulaInput)
            unSelect(manualInput)
        }

        manualInput.setOnClickListener {
            select(manualInput)
            unSelect(formulaInput)

            numberColumns.forEach {
                it.inputType = InputTypeNumberColumn.MANUAL
            }
            prefColumnChangedCallback.prefChanged()
        }
        val context = view.context
        formulaInput.setOnClickListener {

            val allColumns = prefColumnChangedCallback.getNumberColumns()

            val filterColumns = mutableListOf<NumberColumn>().apply {
                addAll(allColumns)
                // удаляем сами выбранные колоны
                removeAll(numberColumns)
                //удаляем те колоны которые указывают в формуле на выбранные колоны
                // цикл по всем нумберколонам
                mutableListOf<NumberColumn>().also { listToFind ->
                    listToFind.addAll(this)
                }.forEach {
                    // если колона настроена на формулу
                    if (it.inputType == InputTypeNumberColumn.FORMULA) {
                        // достаем из этой колоны все ид колон задействованных в формуле
                        val columnIdList = it.formula.getColumnIdList()
                        // смотрим в цикле встречаются ли в них те колоны которые выделены
                        columnIdList.forEach { id ->
                            // проверяем у всех выделенных
                            numberColumns.forEach { selectColumn ->
                                // одна из выбранных колон учавствует в формуле в не выделеной колоне(все выделенные и так не попадают в список в формуле)
                                if (selectColumn.refId == id) {
                                    // удаляем в алл эту колону
                                    this.remove(it)
                                }
                            }
                        }
                    }
                }

            }
            formulaDialogShow(
                numberColumn.formula,
                context,
                filterColumns,
                allColumns,
                null,
                null
            ) { formula ->
                select(formulaInput)
                unSelect(manualInput)

                numberColumns.forEach {
                    it.inputType = InputTypeNumberColumn.FORMULA
                    it.formula = formula
                }
                prefColumnChangedCallback.prefChanged()
            }
        }

    }

    override fun getPrefColumnView(context: Context): View {
        val view = context.layoutInflater.inflate(R.layout.pref_column_number, null)
        initBasicPref(view)
        return view
    }
}

class PrefPhoneColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefTextColumnLayout(column, prefColumnChangedCallback) {

    override fun initBasicPref(view: View) {
        initSeekBarAndToolbarButtons(view)
        val column = columnList[0] as PhoneColumn

        textPrefButtonsInit(view, getPrefForTextViewList()) {
            prefColumnChangedCallback.prefChanged()
        }


        val recycler = view.phoneParamRecycler
        recycler.layoutManager = LinearLayoutManager(view.context)
        val adapterRecyclerPhoneParams =
            AdapterRecyclerPhoneParams(column.getPhoneParamList()) { phoneParamModel ->

                columnList.filterIsInstance(PhoneColumn::class.java).forEach { column ->

                    column.editPhoneParam(phoneParamModel)
                }
                prefColumnChangedCallback.prefChanged()
            }
        recycler.adapter =
            adapterRecyclerPhoneParams
        recycler.dragListener = object : OnItemDragListener<ParamModel> {
            override fun onItemDragged(
                previousPosition: Int,
                newPosition: Int,
                item: ParamModel
            ) {

            }

            override fun onItemDropped(
                initialPosition: Int,
                finalPosition: Int,
                item: ParamModel
            ) {
                columnList.filterIsInstance(PhoneColumn::class.java).forEach {
                    it.sortPositionParam(adapterRecyclerPhoneParams.dataSet)
                }
                prefColumnChangedCallback.prefChanged()
            }

        }
        // дальнеие настройки
    }

    override fun getPrefColumnView(context: Context): View {
        val view = context.layoutInflater.inflate(R.layout.pref_column_phone, null)
        initBasicPref(view)
        return view
    }
}

class PrefDateColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefTextColumnLayout(column, prefColumnChangedCallback) {

    override fun initBasicPref(view: View) {
        initSeekBarAndToolbarButtons(view)
        val typePref = (columnList[0] as DateColumn).typePref

        textPrefButtonsInit(view, getPrefForTextViewList()) {
            prefColumnChangedCallback.prefChanged()
        }

        val dateTypeTextView = view.dateTypeTextView

        val updateDateType = {
            val dateType = view.context.getString(R.string.date_type)
            val date: String = getDate(typePref.type, enableTime = false)
            dateTypeTextView.text = "$dateType ($date)"
        }
        updateDateType()
        val showTime = view.enableTime
        showTime.isChecked = typePref.enableTime

        dateTypeTextView.setOnClickListener { view1 ->
            val context = view1.context
            context.showItemChangeDialog(
                context.getString(R.string.date_type),
                getDateTypeList(),
                typePref.type,
                null
            ) { type ->
                columnList.filterIsInstance(DateColumn::class.java).forEach {
                    it.typePref.type = type
                }
                prefColumnChangedCallback.prefChanged()
                updateDateType()
            }
        }
        showTime.setOnCheckedChangeListener { _, b ->
            columnList.filterIsInstance(DateColumn::class.java).forEach {
                it.typePref.enableTime = b
            }
            prefColumnChangedCallback.prefChanged()
        }

        // дальнеие настройки
    }

    override fun getPrefColumnView(context: Context): View {
        val view = context.layoutInflater.inflate(R.layout.pref_column_date, null)
        initBasicPref(view)
        return view
    }
}

class PrefColorColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefColumnLayout(column, prefColumnChangedCallback) {
    override fun initBasicPref(view: View) {
        initSeekBarAndToolbarButtons(view)
    }

    override fun getPrefColumnView(context: Context): View {
        val view = context.layoutInflater.inflate(R.layout.pref_column_color, null)
        initBasicPref(view)
        return view
    }
}


class PrefSwitchColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefColumnLayout(column, prefColumnChangedCallback) {
    override fun initBasicPref(view: View) {
        initSeekBarAndToolbarButtons(view)
        view.defaultPref.setOnClickListener {
            columnList.forEach {
                it.setDefaultPref()
            }
            prefColumnChangedCallback.recreateView()
            initBasicPref(view)
        }
        val firstTypePref = (columnList[0] as SwitchColumn).typePref

        val container = view.switchTextSettingContainer
        val enableTextSwitch = view.enableTextSwitch
        val enableEditText = view.enableEditText
        val disableEditText = view.disableEditText
        val enableTextSetting = view.enabledTextSetting
        val disableTextSetting = view.disableTextSetting

        //
        val crossOutText = view.crossRow
        val acceptRowInTotal = view.acceptRow

        val context = view.context
        fun init() {
            val switchMode = firstTypePref.isTextSwitchMode
            if (switchMode) {
                container.foreground = ColorDrawable(Color.TRANSPARENT)
            } else {
                container.foreground =
                    ColorDrawable(context.getColorFromRes(R.color.colorBackground_transparent))
            }

            enableTextSwitch.isChecked = switchMode

            enableEditText.isEnabled = switchMode
            disableEditText.isEnabled = switchMode
            enableTextSetting.isEnabled = switchMode
            disableTextSetting.isEnabled = switchMode

            enableEditText.setText(firstTypePref.textEnable)
            disableEditText.setText(firstTypePref.textDisable)

            crossOutText.isChecked = firstTypePref.behavior.crossOut
            acceptRowInTotal.isChecked = firstTypePref.behavior.control

        }
        init()
        enableTextSwitch.setOnCheckedChangeListener { _, isChecked ->
            columnList.filterIsInstance(SwitchColumn::class.java).forEach {
                it.typePref.isTextSwitchMode = isChecked
            }
            prefColumnChangedCallback.recreateView()
            init()
//            prefColumnChangedCallback.prefChanged()
        }

        enableEditText.addTextChangedListener {
            if (it.toString() == firstTypePref.textEnable)
                return@addTextChangedListener

            columnList.filterIsInstance(SwitchColumn::class.java).forEach { switchColumn ->
                switchColumn.typePref.textEnable = it.toString()
            }
            prefColumnChangedCallback.prefChanged()
        }
        disableEditText.addTextChangedListener {
            if (it.toString() == firstTypePref.textEnable)
                return@addTextChangedListener

            columnList.filterIsInstance(SwitchColumn::class.java).forEach { switchColumn ->
                switchColumn.typePref.textDisable = it.toString()
            }
            prefColumnChangedCallback.prefChanged()
        }


        val clickSetting: (View) -> Unit = {
            context.alertDialog {
                val prefForTextView = mutableListOf<PrefForTextView>()
                val isEnableSetting = it == enableTextSetting
                columnList.filterIsInstance(SwitchColumn::class.java).forEach {
                    if (isEnableSetting)
                        prefForTextView.add(it.typePref.enablePref)
                    else
                        prefForTextView.add(it.typePref.disablePref)
                }
                setView(
                    context.getPrefTextLayout(
                        prefForTextView,
                        true,
                        object : PrefTextChangedCallback {
                            override fun nameEdit(text: String) {

                            }

                            override fun prefChanged() {
                                prefColumnChangedCallback.recreateView()
//                                prefColumnChangedCallback.prefChanged()
                            }
                        })
                )
            }.show()
        }
        enableTextSetting.setOnClickListener(clickSetting)
        disableTextSetting.setOnClickListener(clickSetting)


        crossOutText.setOnCheckedChangeListener { _, isChecked ->
            columnList.filterIsInstance(SwitchColumn::class.java).forEach {
                it.typePref.behavior.crossOut = isChecked
            }
            init()
            prefColumnChangedCallback.prefChanged()
        }
        acceptRowInTotal.setOnCheckedChangeListener { _, isChecked ->
            columnList.filterIsInstance(SwitchColumn::class.java).forEach {
                it.typePref.behavior.control = isChecked
            }
            init()
            prefColumnChangedCallback.prefChanged()
        }


    }

    override fun getPrefColumnView(context: Context): View {
        val view = context.layoutInflater.inflate(R.layout.pref_column_switch, null)
        initBasicPref(view)
        return view
    }
}


class PrefImageColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefColumnLayout(column, prefColumnChangedCallback) {
    override fun initBasicPref(view: View) {
        initSeekBarAndToolbarButtons(view)
    }

    override fun getPrefColumnView(context: Context): View {
        val view = context.layoutInflater.inflate(R.layout.pref_column_image, null)
        val radioGroup = view.imageViewSettingInCellRG
        val firstColumn = columnList[0] as ImageColumn
        if (firstColumn.typePref.imageViewMode == 0)
            radioGroup.check(R.id.putImageRB)
        else
            radioGroup.check(R.id.cutImageRB)

        radioGroup.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
            columnList.filterIsInstance<ImageColumn>().forEach {
                it.typePref.imageViewMode = if (id == R.id.putImageRB) 0 else 1
            }
            prefColumnChangedCallback.prefChanged()
        }
        initBasicPref(view)
        return view
    }
}

class PrefListColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefTextColumnLayout(column, prefColumnChangedCallback) {

    override fun initBasicPref(view: View) {
        val dataController = DataController(view.context)
        val context = view.context
        initSeekBarAndToolbarButtons(view)
        val typePref = (columnList[0] as ListColumn).typePref

        textPrefButtonsInit(view, getPrefForTextViewList()) {
            prefColumnChangedCallback.prefChanged()
        }

        val listTextView = view.list_change
        val editList = view.edit_list

        (context as PrefCardActivity).lifecycleScope.launch {
            var allList: MutableList<ListType> = dataController.getAllListType()
            val typeIndex = typePref.listTypeIndex
            val listType: ListType? = if (typeIndex == -1) null else allList[typeIndex]
            fun updateListTextView() {
                val listString = context.getString(R.string.list)
                val listName: String = listType?.listName ?: ""
                val listText = "$listString ($listName)"
                listTextView.text = listText
            }
            updateListTextView()


            listTextView.setOnClickListener {
                val mutableList = mutableListOf<String>()
                val newList = context.getString(R.string.new_list)

                val create = context.getString(R.string.create)
                val createListText = "$create ${newList.toLowerCase(Locale.getDefault())}"

                allList.forEach { listType ->
                    val element = listType.listName
                    mutableList.add(element)
                }

                context.showItemChangeDialog(
                    context.getString(R.string.change_list),
                    mutableList,
                    typeIndex,
                    createListText
                ) { index ->
                    runOnMaim {
                        if (index == -1) {
                            io {
                                dataController.addListType(ListType().apply {
                                    listName = newList
                                })
                            }
                            allList = dataController.getAllListType()
                            columnList.filterIsInstance(ListColumn::class.java).forEach {
                                it.typePref.listTypeIndex = allList.size - 1
                            }
                        } else
                            columnList.filterIsInstance(ListColumn::class.java).forEach {
                                it.typePref.listTypeIndex = index
                            }
                        initBasicPref(view)
                        prefColumnChangedCallback.prefChanged()
                    }
                }
            }
            editList.setOnClickListener { }

        }
        // дальнеие настройки
    }

    override fun getPrefColumnView(context: Context): View {
        val view = context.layoutInflater.inflate(R.layout.pref_column_list, null)
        initBasicPref(view)
        return view
    }
}

class PrefNumerationColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefTextColumnLayout(column, prefColumnChangedCallback) {

    override fun initBasicPref(view: View) {
        initSeekBarAndToolbarButtons(view)

        textPrefButtonsInit(view, getPrefForTextViewList()) {
            prefColumnChangedCallback.prefChanged()
        }
    }
}

class PrefNoneColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefColumnLayout(column, prefColumnChangedCallback) {
    override fun initBasicPref(view: View) {
        initSeekBarAndToolbarButtons(view)
    }

    override fun getPrefColumnView(context: Context): View {
        val view = context.layoutInflater.inflate(R.layout.pref_column_image, null)
        initBasicPref(view)
        return view
    }
}

