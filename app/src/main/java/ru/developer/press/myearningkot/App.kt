package ru.developer.press.myearningkot

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.TypedValue
import androidx.room.Room
import com.bugsnag.android.Bugsnag
import com.google.gson.Gson
import kotlinx.coroutines.*
import ru.developer.press.myearningkot.activity.ID_UPDATE_CARD
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.model.ColumnType
import ru.developer.press.myearningkot.model.DataController
import ru.developer.press.myearningkot.model.NumberColumn
import ru.developer.press.myearningkot.otherHelpers.CustomExceptionHandler
import ru.developer.press.myearningkot.otherHelpers.Database
import ru.developer.press.myearningkot.otherHelpers.SampleJson


class App : Application() {

    companion object {
        var instance: App? = null

    }

    lateinit var database: Database
    var pref: SharedPreferences? = null

    override fun onCreate() {
        if (instance == null) {
            instance = this
            pref = getSharedPreferences("app.setting", Context.MODE_PRIVATE)
            database =
                Room.databaseBuilder(
                    applicationContext,
                    Database::class.java,
                    "Database.db"
                ).build()
            val sampleJsonDao =
                database
                    .sampleJsonDao()

            val isFirst = pref!!.getBoolean(prefFirstKey, true)
            logD("isFirst = $isFirst")
            if (isFirst) {
                pref!!.edit().putBoolean(prefFirstKey, false).apply()
                DataController().addPage("Доход")
                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        CoroutineScope(Dispatchers.IO).launch {
                            sampleJsonDao.insert(SampleJson().apply {
                                json = Gson().toJson(Card(name = "Доход").apply {
//                                    deleteColumn()
                                        addColumn(ColumnType.NUMBER, getString(R.string.summa))

                                        addColumn(ColumnType.NUMBER, getString(R.string.avans))

                                    addColumn(ColumnType.TEXT, getString(R.string.note)).apply {
                                        width = 450
                                    }
                                    addColumn(ColumnType.DATE, getString(R.string.date)).apply {
                                        width = 430
                                    }

                                    // временная суета...
                                    repeat(30) {
                                        addRow()
                                    }

                                    addTotal()
                                    addTotal()
                                })
                            })
                            // расход
                            sampleJsonDao.insert(SampleJson().apply {
                                json = Gson().toJson(Card(name = "Расход").apply {
                                    deleteColumn()

                                    addColumn(ColumnType.NUMBER, "Бюджет") as NumberColumn

                                    addColumn(ColumnType.NUMBER, "Потрачено") as NumberColumn

                                    addColumn(ColumnType.LIST, "Категория").apply {
                                        width = 430
                                    }
                                    addColumn(ColumnType.TEXT, getString(R.string.note)).apply {
                                        width = 450
                                    }
                                    addColumn(ColumnType.DATE, getString(R.string.date)).apply {
                                        width = 430
                                    }
                                    // временная суета...
                                    repeat(30) {
                                        addRow()
                                    }
                                    addTotal()
                                    addTotal()
                                })
                            })
                            //долги
                            sampleJsonDao.insert(SampleJson().apply {
                                json = Gson().toJson(Card(name = "Мои долги").apply {
                                    deleteColumn()

                                    addColumn(ColumnType.NUMBER, "Должен") as NumberColumn

                                    addColumn(ColumnType.NUMBER, "Оплатил") as NumberColumn

                                    addColumn(ColumnType.LIST, "Кому").apply {
                                        width = 430
                                    }
                                    addColumn(ColumnType.TEXT, getString(R.string.note)).apply {
                                        width = 450
                                    }
                                    addColumn(ColumnType.DATE, getString(R.string.date)).apply {
                                        width = 430
                                    }
                                    // временная суета...
                                    repeat(30) {
                                        addRow()
                                    }
                                    addTotal()
                                    addTotal()
                                })
                            })
//
                        }
                    }
                }
            }
        }
        super.onCreate()

        Bugsnag.init(this)
    }

    fun dpsToPixels(dps: Int): Int = applicationContext.dpsToPixels(dps)
    fun setUpdateCardId(i: Long) {
        pref?.edit()?.putLong(ID_UPDATE_CARD, i)?.apply()
    }

    fun getUpdateCardId(): Long? = pref?.getLong(ID_UPDATE_CARD, -1)


}

fun Context.dpsToPixels(dps: Int): Int {
    val r = resources
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dps.toFloat(), r.displayMetrics
    ).toInt()
}

const val prefFirstKey = "isFirst"
const val prefSampleLastChanged = "sampleLastChanged"
const val prefEnableDate = "enableDate"
const val prefSortSelected = "sortSelected"

const val prefEnableSomeStrokeChanged = "enableSomeStrokeChanged"
const val prefDateLastChanged = "dateLastChanged"
