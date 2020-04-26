package ru.developer.press.myearningkot

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.bugsnag.android.Bugsnag
import kotlinx.coroutines.*
import ru.developer.press.myearningkot.activity.ID_UPDATE_CARD
import ru.developer.press.myearningkot.model.*
import ru.developer.press.myearningkot.helpers.Database
import ru.developer.press.myearningkot.helpers.SampleHelper


class App : Application(), ActivityLifecycleCallbacks {

    companion object {
        var instance: App? = null

    }

    var currentActivity: AppCompatActivity? = null
    var copyCell: Cell? = null
        set(value) {
            if (value == null)
                field = value
            else {
                field = Cell().apply {
                    sourceValue = value.sourceValue
                    type = value.type
                }
            }
        }
    lateinit var database: Database
    var pref: SharedPreferences? = null

    override fun onCreate() {
        registerActivityLifecycleCallbacks(this)
        if (instance == null) {
            instance = this
            pref = getSharedPreferences("app.setting", Context.MODE_PRIVATE)
            database =
                Room.databaseBuilder(
                    applicationContext,
                    Database::class.java,
                    "Database.db"
                ).build()

            val isFirst = pref!!.getBoolean(prefFirstKey, true)
            if (isFirst) {
                pref!!.edit().putBoolean(prefFirstKey, false).apply()
                DataController().addPage(getString(R.string.active))
                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val sampleHelper = SampleHelper()
                            // доход
                            sampleHelper.addSample(Card(name = getString(R.string.earning)).apply {
                                //                                    deleteColumn()
                                val summaColumn =
                                    addColumn(ColumnType.NUMBER, getString(R.string.summa))

                                val avansColumn =
                                    addColumn(ColumnType.NUMBER, getString(R.string.avans))

                                addColumn(ColumnType.TEXT, getString(R.string.note)).apply {
                                    width = 450
                                }
                                addColumn(ColumnType.DATE, getString(R.string.date)).apply {
                                    width = 430
                                }

                                val summaTotal = totals[0]
                                summaTotal.apply {
                                    title = getString(R.string.SUMMA)
                                    formula = Formula().apply {
                                        formulaElements.add(
                                            Formula.FormulaElement(
                                                Formula.COLUMN_ID,
                                                summaColumn.id.toString()
                                            )
                                        )
                                    }
                                }
                                val avansTotal = addTotal().apply {
                                    title = getString(R.string.AVANS)
                                    formula = Formula().apply {
                                        formulaElements.add(
                                            Formula.FormulaElement(
                                                Formula.COLUMN_ID,
                                                avansColumn.id.toString()
                                            )
                                        )
                                    }
                                }
                                addTotal().apply {
                                    title = getString(R.string.RESIDUE)
                                    formula = Formula().apply {
                                        formulaElements.add(
                                            Formula.FormulaElement(
                                                Formula.TOTAL_ID,
                                                summaTotal.id.toString()
                                            )
                                        )
                                        formulaElements.add(
                                            Formula.FormulaElement(
                                                Formula.OTHER,
                                                "-"
                                            )
                                        )
                                        formulaElements.add(
                                            Formula.FormulaElement(
                                                Formula.TOTAL_ID,
                                                avansTotal.id.toString()
                                            )
                                        )
                                    }
                                }
                            })
                            // расход
                            sampleHelper.addSample(Card(name = getString(R.string.expenses)).apply {
                                deleteColumn()

                                val summaColumn =
                                    addColumn(ColumnType.NUMBER, getString(R.string.budget)) as NumberColumn

                                val avansColumn =
                                    addColumn(ColumnType.NUMBER, getString(R.string.expenses)) as NumberColumn

                                addColumn(ColumnType.LIST, getString(R.string.category)).apply {
                                    width = 430
                                }
                                addColumn(ColumnType.TEXT, getString(R.string.note)).apply {
                                    width = 450
                                }
                                addColumn(ColumnType.DATE, getString(R.string.date)).apply {
                                    width = 430
                                }
                                val summaTotal = totals[0]
                                summaTotal.apply {
                                    title = getString(R.string.BUDGET)
                                    formula = Formula().apply {
                                        formulaElements.add(
                                            Formula.FormulaElement(
                                                Formula.COLUMN_ID,
                                                summaColumn.id.toString()
                                            )
                                        )
                                    }
                                }
                                val avansTotal = addTotal().apply {
                                    title = getString(R.string.EXPENSES)
                                    formula = Formula().apply {
                                        formulaElements.add(
                                            Formula.FormulaElement(
                                                Formula.COLUMN_ID,
                                                avansColumn.id.toString()
                                            )
                                        )
                                    }
                                }
                                addTotal().apply {
                                    title = getString(R.string.RESIDUE)
                                    formula = Formula().apply {
                                        formulaElements.add(
                                            Formula.FormulaElement(
                                                Formula.TOTAL_ID,
                                                summaTotal.id.toString()
                                            )
                                        )
                                        formulaElements.add(
                                            Formula.FormulaElement(
                                                Formula.OTHER,
                                                "-"
                                            )
                                        )
                                        formulaElements.add(
                                            Formula.FormulaElement(
                                                Formula.TOTAL_ID,
                                                avansTotal.id.toString()
                                            )
                                        )
                                    }
                                }
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
    override fun onActivityPaused(p0: Activity) {

    }

    override fun onActivityStarted(p0: Activity) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity as AppCompatActivity
    }


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
