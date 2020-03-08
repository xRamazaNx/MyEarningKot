package ru.developer.press.myearningkot.otherHelpers.PrefLayouts

import android.graphics.drawable.Drawable
import android.view.View
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.model.ColumnType

class PrefSelectedControl {
    val selectPrefType: ElementPrefType
        get() =
            when {
                selectedElementList.any { it.elementType == ElementType.COLUMN } -> ElementPrefType.COLUMN
                selectedElementList.any { it.elementType == ElementType.TOTAL } -> ElementPrefType.TOTAL
                selectedElementList.any { it.elementType == ElementType.DATE} -> ElementPrefType.DATE_PERIOD
                else -> ElementPrefType.TEXT_VIEW
            }
    private val selectedElementList = mutableListOf<SelectedElement>()
    val isSelect: Boolean
        get() {
            return selectedElementList.isNotEmpty()
        }
    var selectCallback: SelectCallback? = null

    fun select(_selectedElement: SelectedElement) {
        // если нажали на то что было уже выделено то убираем
//        if (selectedElementList.any { it == selectedElement }) {
//        }
        selectedElementList.forEach {
            if (it.elementType == _selectedElement.elementType) {
                if (it is SelectedElement.ElementColumn &&
                    _selectedElement is SelectedElement.ElementColumn
                ) {
                    it.apply {
                        if (columnType == _selectedElement.columnType
                            && columnIndex == _selectedElement.columnIndex
                        ) {
                            unSelect(it)
                            return
                        }
                    }
                } else if (it is SelectedElement.ElementColumnTitle &&
                    _selectedElement is SelectedElement.ElementColumnTitle
                ) {
                    it.apply {
                        if (columnIndex == _selectedElement.columnIndex) {
                            unSelect(it)
                            return
                        }
                    }
                } else if (it is SelectedElement.ElementTotal &&
                    _selectedElement is SelectedElement.ElementTotal
                ) {
                    if (it.index == _selectedElement.index) {
                        unSelect(it)
                        return
                    }
                } else {
                    unSelect(it)
                    return
                }
            }
        }

        // суета для того что бы понять убрать с других элементов выделение
        when (_selectedElement.elementType) {
            ElementType.COLUMN -> {
                // если до этого было выделено другое
                if (selectedElementList.any { it.elementType != ElementType.COLUMN }) {
                    // убираем все выделения
                    unSelectAll()
                }
                selectedElementList.add(_selectedElement)
                selectCallback?.select(_selectedElement)

            }
            ElementType.TOTAL -> {
                // если нажали на не колону и в нем присутсвует колона
                // и проверяем что это не колона заголовок а именно колона по типу
                if (selectedElementList.any { it.elementType != ElementType.TOTAL }) {
                    unSelectAll()
                }

                selectedElementList.add(_selectedElement)
                selectCallback?.select(_selectedElement)

            }
            ElementType.DATE ->{
                // если до этого было выделено другое
                if (selectedElementList.any { it.elementType != ElementType.DATE}) {
                    // убираем все выделения
                    unSelectAll()
                }
                selectedElementList.add(_selectedElement)
                selectCallback?.select(_selectedElement)
            }
            else -> {

                if (selectedElementList.any { it.elementType == ElementType.TOTAL }
                    || selectedElementList.any { it.elementType == ElementType.DATE }
                    || selectedElementList.any { it.elementType == ElementType.COLUMN }) {
                    unSelectAll()
                }
                selectedElementList.add(_selectedElement)
                selectCallback?.select(_selectedElement)
            }
        }
        selectCallback?.setVisiblePrefButton(isSelect)
    }

    private fun unSelect(selectedElement: SelectedElement) {
        if (selectedElementList.contains(selectedElement)) {
            selectedElementList.remove(selectedElement)
            selectCallback?.unSelect(selectedElement)
        }

        selectCallback?.setVisiblePrefButton(isSelect)
    }

    fun unSelectAll(): Boolean {
        val isContain = selectedElementList.isNotEmpty()
        val listTemp = mutableListOf<SelectedElement>().apply {
            addAll(selectedElementList)
        }
        listTemp.forEach {
            unSelect(it)
        }
        selectCallback?.setVisiblePrefButton(isSelect)
        return isContain
    }

    fun showPref() {

        val elementPref = ElementPref().apply {
            selectedElementList = this@PrefSelectedControl.selectedElementList
            elementPrefType = selectPrefType
        }

        // определяем какого типа будет лайот
//        selectedElementList.forEach {
//
//            if (it.elementType == ElementType.COLUMN) {
//                elementPref.elementPrefType = ElementPrefType.COLUMN
//                return@forEach
//            } else if (it.elementType == ElementType.TOTAL) {
//                elementPref.elementPrefType = ElementPrefType.TOTAL
//                return@forEach
//            }
//        }

        // определяем точно
        if (elementPref.elementPrefType == ElementPrefType.COLUMN) {
            selectedElementList.forEachIndexed { index, selectedElement ->
                val elementColumn = selectedElement as SelectedElement.ElementColumn
                if (index == 0) {
                    elementPref.columnType = elementColumn.columnType
                } else {
                    when (elementColumn.columnType) {
                        ColumnType.TEXT -> {
                            when (elementPref.columnType) {
                                ColumnType.PHONE,
                                ColumnType.LIST,
                                ColumnType.NUMERATION,
                                ColumnType.NUMBER,
                                ColumnType.DATE ->
                                    elementPref.columnType = ColumnType.TEXT


                                ColumnType.COLOR,
                                ColumnType.SWITCH,
                                ColumnType.IMAGE,
                                ColumnType.NONE -> {
                                    elementPref.columnType = ColumnType.NONE
                                }
                                ColumnType.TEXT -> {}
                            }
                        }
                        ColumnType.NUMBER -> {
                            when (elementPref.columnType) {
                                ColumnType.TEXT,
                                ColumnType.PHONE,
                                ColumnType.DATE,
                                ColumnType.NUMERATION,
                                ColumnType.LIST -> {
                                    elementPref.columnType = ColumnType.TEXT
                                }

                                ColumnType.SWITCH,
                                ColumnType.IMAGE,
                                ColumnType.COLOR,
                                ColumnType.NONE -> {
                                    elementPref.columnType = ColumnType.NONE
                                }
                                ColumnType.NUMBER -> {}
                            }
                        }
                        ColumnType.PHONE -> {
                            when (elementPref.columnType) {
                                ColumnType.TEXT,
                                ColumnType.NUMBER,
                                ColumnType.DATE,
                                ColumnType.NUMERATION,
                                ColumnType.LIST -> {
                                    elementPref.columnType = ColumnType.TEXT
                                }

                                ColumnType.SWITCH,
                                ColumnType.IMAGE,
                                ColumnType.COLOR,
                                ColumnType.NONE -> {
                                    elementPref.columnType = ColumnType.NONE
                                }
                                else -> {}
                            }
                        }
                        ColumnType.DATE -> {
                            when (elementPref.columnType) {
                                ColumnType.TEXT,
                                ColumnType.PHONE,
                                ColumnType.NUMBER,
                                ColumnType.NUMERATION,
                                ColumnType.LIST -> {
                                    elementPref.columnType = ColumnType.TEXT
                                }

                                ColumnType.SWITCH,
                                ColumnType.IMAGE,
                                ColumnType.COLOR,
                                ColumnType.NONE -> {
                                    elementPref.columnType = ColumnType.NONE
                                }
                                else -> {}
                            }
                        }
                        ColumnType.LIST -> {
                            when (elementPref.columnType) {
                                ColumnType.TEXT,
                                ColumnType.PHONE,
                                ColumnType.DATE,
                                ColumnType.NUMERATION,
                                ColumnType.NUMBER -> {
                                    elementPref.columnType = ColumnType.TEXT
                                }

                                ColumnType.SWITCH,
                                ColumnType.IMAGE,
                                ColumnType.COLOR,
                                ColumnType.NONE -> {
                                    elementPref.columnType = ColumnType.NONE
                                }
                                else -> {}
                            }
                        }
                        ColumnType.NUMERATION -> {
                            when (elementPref.columnType) {
                                ColumnType.TEXT,
                                ColumnType.PHONE,
                                ColumnType.DATE,
                                ColumnType.NUMBER,
                                ColumnType.LIST -> {
                                    elementPref.columnType = ColumnType.TEXT
                                }

                                ColumnType.SWITCH,
                                ColumnType.IMAGE,
                                ColumnType.COLOR,
                                ColumnType.NONE -> {
                                    elementPref.columnType = ColumnType.NONE
                                }
                                else -> {}
                            }
                        }
                        ColumnType.COLOR ->
                            if (elementPref.columnType != ColumnType.COLOR)
                                elementPref.columnType = ColumnType.NONE
                        ColumnType.SWITCH ->
                            if (elementPref.columnType != ColumnType.SWITCH)
                                elementPref.columnType = ColumnType.NONE
                        ColumnType.IMAGE ->
                            if (elementPref.columnType != ColumnType.IMAGE)
                                elementPref.columnType = ColumnType.NONE
                        ColumnType.NONE ->
                            elementPref.columnType = ColumnType.NONE
                    }
                }
            }
        }

        selectCallback?.showPref(elementPref)
    }

    fun moveToRight() {
        selectCallback?.moveToRight(selectedElementList)
    }

    fun moveToLeft() {
        selectCallback?.moveToLeft(selectedElementList)

    }

    fun delete() {
        selectCallback?.delete(selectedElementList)
    }

    fun updateSelected() {
        selectedElementList.forEach {
            selectCallback?.select(it)
        }
    }
}

abstract class SelectedElement(
    var oldDrawable: Drawable?,
    var elementType: ElementType
) {

    class ElementTextView(oldDrawable: Drawable?, elementType: ElementType) :
        SelectedElement(oldDrawable, elementType)

    open class ElementColumnTitle(
        var columnIndex: Int,
        elementType: ElementType,
        drawable: Drawable?
    ) : SelectedElement(drawable, elementType)

    class ElementColumn(columnIndex: Int, elementType: ElementType, drawable: Drawable?) :
        ElementColumnTitle(columnIndex, elementType, drawable) {
        var columnType = ColumnType.TEXT
    }

    class ElementTotal(
        val index: Int,
        oldDrawable: Drawable?,
        elementType: ElementType
    ) : SelectedElement(oldDrawable, elementType)
}

interface SelectCallback {
    fun select(selectedElement: SelectedElement)
    fun unSelect(selectedElement: SelectedElement?)
    fun showPref(elementPref: ElementPref)
    fun setVisiblePrefButton(isVisible: Boolean)
    fun moveToRight(selectedElementList: List<SelectedElement>)
    fun moveToLeft(selectedElementList: List<SelectedElement>)
    fun delete(selectedElementList: List<SelectedElement>)
}

enum class ElementType {
    COLUMN, COLUMN_TITLE,
    TOTAL, TOTAL_TITLE,
    NAME, DATE
}

class ElementPref {
    lateinit var columnType: ColumnType
    var elementPrefType = ElementPrefType.TEXT_VIEW
    lateinit var selectedElementList: MutableList<SelectedElement>
}

enum class ElementPrefType {
    COLUMN, TEXT_VIEW, TOTAL, DATE_PERIOD
}

fun setSelectBackground(view: View) {
    view.setBackgroundResource(R.drawable.select_pref_view)
}