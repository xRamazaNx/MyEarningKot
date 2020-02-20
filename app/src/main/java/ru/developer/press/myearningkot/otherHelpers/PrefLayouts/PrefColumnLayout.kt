package ru.developer.press.myearningkot.otherHelpers.PrefLayouts

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.SeekBar
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import kotlinx.android.synthetic.main.pref_column_date.view.*
import kotlinx.android.synthetic.main.pref_column_list.view.*
import kotlinx.android.synthetic.main.pref_column_number.view.*
import kotlinx.android.synthetic.main.pref_column_phone.view.*
import kotlinx.android.synthetic.main.pref_column_switch.view.*
import kotlinx.android.synthetic.main.toolbar_pref.view.*
import kotlinx.android.synthetic.main.width_seek_bar_layout.view.*
import kotlinx.coroutines.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.layoutInflater
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.adapters.AdapterRecyclerPhoneParams
import ru.developer.press.myearningkot.adapters.PhoneParamModel
import ru.developer.press.myearningkot.model.*
import ru.developer.press.myearningkot.otherHelpers.getColorFromRes
import ru.developer.press.myearningkot.otherHelpers.getDate
import ru.developer.press.myearningkot.otherHelpers.getDateTypeList
import ru.developer.press.myearningkot.otherHelpers.showItemChangeDialog
import splitties.alertdialog.appcompat.alertDialog
import java.util.*
import java.util.logging.Handler

fun Context.getPrefColumnLayout(
    columns: MutableList<Column>,
    columnType: ColumnType,
    prefColumnChangedCallback: PrefColumnChangedCallback
): View {

    val columnLayout: PrefColumnLayout? = when (columnType) {
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
    fun recreateView()
}

abstract class PrefColumnLayout(
    val columnList: MutableList<Column>,
    val prefColumnChangedCallback: PrefColumnChangedCallback
) {
    abstract fun initPref(view: View)

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
            initPref(view)
        }

    }

    abstract fun getPrefColumnView(context: Context): View
}

open class PrefTextColumnLayout(
    columnList: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefColumnLayout(columnList, prefColumnChangedCallback) {

    override fun initPref(view: View) {
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
        initPref(view)
        return view
    }
}

class PrefNumberColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefTextColumnLayout(column, prefColumnChangedCallback) {

    override fun initPref(view: View) {
        initSeekBarAndToolbarButtons(view)
        val typePref = (columnList[0] as NumberColumn).typePref

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
            columnList.filterIsInstance(NumberColumn::class.java).forEach {
                it.typePref.isGrouping = b
            }
            prefColumnChangedCallback.prefChanged()
        }

        val editDigit = fun(digitOffset: Int) {
            var digit = typePref.digitsCount
            digit += digitOffset

            if (digit < 0)
                digit = 0

            columnList.filterIsInstance(NumberColumn::class.java).forEach {
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
    }

    override fun getPrefColumnView(context: Context): View {
        val view = context.layoutInflater.inflate(R.layout.pref_column_number, null)
        initPref(view)
        return view
    }
}

class PrefPhoneColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefTextColumnLayout(column, prefColumnChangedCallback) {

    override fun initPref(view: View) {
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
        recycler.dragListener = object : OnItemDragListener<PhoneParamModel> {
            override fun onItemDragged(
                previousPosition: Int,
                newPosition: Int,
                item: PhoneParamModel
            ) {

            }

            override fun onItemDropped(
                initialPosition: Int,
                finalPosition: Int,
                item: PhoneParamModel
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
        initPref(view)
        return view
    }
}

class PrefDateColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefTextColumnLayout(column, prefColumnChangedCallback) {

    override fun initPref(view: View) {
        initSeekBarAndToolbarButtons(view)
        val typePref = (columnList[0] as DateColumn).typePref

        textPrefButtonsInit(view, getPrefForTextViewList()) {
            prefColumnChangedCallback.prefChanged()
        }

        val dateTypeTextView = view.dateTypeTextView

        val typeText = dateTypeTextView.text
        val updateDateType = {
            val date = getDate(typePref.type, enableTime = false)
            dateTypeTextView.text = "$typeText ($date)"
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
        initPref(view)
        return view
    }
}

class PrefColorColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefColumnLayout(column, prefColumnChangedCallback) {
    override fun initPref(view: View) {
        initSeekBarAndToolbarButtons(view)
    }

    override fun getPrefColumnView(context: Context): View {
        val view = context.layoutInflater.inflate(R.layout.pref_column_color, null)
        initPref(view)
        return view
    }
}


class PrefSwitchColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefColumnLayout(column, prefColumnChangedCallback) {
    override fun initPref(view: View) {
        initSeekBarAndToolbarButtons(view)
        view.defaultPref.setOnClickListener {
            columnList.forEach {
                it.setDefaultPref()
            }
            prefColumnChangedCallback.recreateView()
            initPref(view)
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
                    ColorDrawable(context.getColorFromRes(R.color.cent_opacity))
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
                        null,
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
        initPref(view)
        return view
    }
}


class PrefImageColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefColumnLayout(column, prefColumnChangedCallback) {
    override fun initPref(view: View) {
        initSeekBarAndToolbarButtons(view)
    }

    override fun getPrefColumnView(context: Context): View {
        val view = context.layoutInflater.inflate(R.layout.pref_column_image, null)
        initPref(view)
        return view
    }
}

class PrefListColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefTextColumnLayout(column, prefColumnChangedCallback) {

    override fun initPref(view: View) {
        val dataController = DataController()
        val context = view.context
        initSeekBarAndToolbarButtons(view)
        val typePref = (columnList[0] as ListColumn).typePref

        textPrefButtonsInit(view, getPrefForTextViewList()) {
            prefColumnChangedCallback.prefChanged()
        }

        val listTextView = view.list_change
        val editList = view.edit_list


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
                CoroutineScope(Dispatchers.Main).launch {
                    if (index == -1) {
                        withContext(Dispatchers.IO) {
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
                    initPref(view)
                    prefColumnChangedCallback.prefChanged()
                }
            }
        }
        editList.setOnClickListener { }

        // дальнеие настройки
    }

    override fun getPrefColumnView(context: Context): View {
        val view = context.layoutInflater.inflate(R.layout.pref_column_list, null)
        initPref(view)
        return view
    }
}

class PrefNumerationColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefTextColumnLayout(column, prefColumnChangedCallback) {

    override fun initPref(view: View) {
        initSeekBarAndToolbarButtons(view)

        textPrefButtonsInit(view, getPrefForTextViewList()) {
            prefColumnChangedCallback.prefChanged()
        }
        // дальнеие настройки
    }
}

class PrefNoneColumnLayout(
    column: MutableList<Column>,
    prefColumnChangedCallback: PrefColumnChangedCallback
) : PrefColumnLayout(column, prefColumnChangedCallback) {
    override fun initPref(view: View) {
        initSeekBarAndToolbarButtons(view)
    }

    override fun getPrefColumnView(context: Context): View {
        val view = context.layoutInflater.inflate(R.layout.pref_column_image, null)
        initPref(view)
        return view
    }
}

