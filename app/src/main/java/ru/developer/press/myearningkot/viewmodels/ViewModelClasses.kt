package ru.developer.press.myearningkot.viewmodels

import androidx.lifecycle.MutableLiveData
import ru.developer.press.myearningkot.CellTypeControl
import ru.developer.press.myearningkot.model.*
import java.util.*

class PageVM {
    val name = MutableLiveData<String>()
    val cards = mutableListOf<Card>()
}

class CardVM {
    val isShowDatePeriod = MutableLiveData<Boolean>()
    val datePeriod = MutableLiveData<String>()

    val isShowTotalInfo = MutableLiveData<Boolean>()
    val isCardPrefUpdate = MutableLiveData<Boolean>()

//    var valuta = 0
    val enableSomeStroke = MutableLiveData<Boolean>()
    val sortPref = SortPrefVM()
    val enableHorizontalScroll = MutableLiveData<Boolean>()
    val enableHorizontalScrollTotal = MutableLiveData<Boolean>()
    val heightCells = MutableLiveData<Int>()
    val dateCreated = MutableLiveData<Long>()
    val dateModify = MutableLiveData<Long>()
    val rows = mutableListOf<RowVM>()
    val columns = mutableListOf<ColumnVM>()
    val totals = mutableListOf<TotalItem>()

    val dateOfPeriod = MutableLiveData<String>()
}

class ColumnVM {

}
class RowVM {
    val status = MutableLiveData<Row.Status>()
    val cellList = mutableListOf<CellVM>()
}

class CellVM {
    val isSelect =MutableLiveData<Boolean>()

    val isPrefColumnSelect = MutableLiveData<Boolean>()

    val cellTypeControl = MutableLiveData<CellTypeControl>()

    var displayValue = MutableLiveData<String>()
}

class SortPrefVM {
    var isSave = MutableLiveData<Boolean>()
    var sortMethod = MutableLiveData<SortMethod>()
    var sortFofColumnId = MutableLiveData<Int>()
}