package ru.developer.press.myearningkot.viewmodels

import ru.developer.press.myearningkot.CellTypeControl
import ru.developer.press.myearningkot.database.Card
import ru.developer.press.myearningkot.helpers.liveData
import ru.developer.press.myearningkot.model.Row
import ru.developer.press.myearningkot.model.SortMethod
import ru.developer.press.myearningkot.model.Total

class PageVM {
    val name = liveData<String>()
    val cards = mutableListOf<Card>()
}

class CardVM {
    val isShowDatePeriod = liveData<Boolean>()
    val datePeriod = liveData<String>()

    val isShowTotalInfo = liveData<Boolean>()
    val isCardPrefUpdate = liveData<Boolean>()

    //    var valuta = 0
    val enableSomeStroke = liveData<Boolean>()
    val sortPref = SortPrefVM()
    val enableHorizontalScroll = liveData<Boolean>()
    val enableHorizontalScrollTotal = liveData<Boolean>()
    val heightCells = liveData<Int>()
    val dateCreated = liveData<Long>()
    val dateModify = liveData<Long>()
    val rows = mutableListOf<RowVM>()
    val columns = mutableListOf<ColumnVM>()
    val totals = mutableListOf<Total>()

    val dateOfPeriod = liveData<String>()
}

class ColumnVM {

}

class RowVM {
    val status = liveData<Row.Status>()
    val cellList = mutableListOf<CellVM>()
}

class CellVM {
    val isSelect = liveData<Boolean>()

    val isPrefColumnSelect = liveData<Boolean>()

    val cellTypeControl = liveData<CellTypeControl>()

    var displayValue = liveData<String>()
}

class SortPrefVM {
    var isSave = liveData<Boolean>()
    var sortMethod = liveData<SortMethod>()
    var sortFofColumnId = liveData<Int>()
}