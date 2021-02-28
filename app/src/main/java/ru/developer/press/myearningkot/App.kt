package ru.developer.press.myearningkot

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.bugsnag.android.Bugsnag
import ru.developer.press.myearningkot.adapters.animationAdd
import ru.developer.press.myearningkot.adapters.animationDelete
import ru.developer.press.myearningkot.helpers.SampleHelper
import ru.developer.press.myearningkot.helpers.filesFolder
import ru.developer.press.myearningkot.helpers.getColorFromRes
import ru.developer.press.myearningkot.model.*
import kotlin.concurrent.thread


class App : Application(), ActivityLifecycleCallbacks {

    companion object {
        fun Activity.app(): App {
            return application as App
        }
    }

    var currentActivity: AppCompatActivity? = null
    var copyCell: Cell? = null
        set(value) {
            if (value == null)
                field = value
            else
                field = Cell().apply {
                    sourceValue = value.sourceValue
                    type = value.type
                }
        }
    val pref: SharedPreferences get() = getSharedPreferences("app.setting", Context.MODE_PRIVATE)

    override fun onCreate() {
        registerActivityLifecycleCallbacks(this)
        animationDelete = AnimationUtils.loadAnimation(applicationContext, R.anim.anim_delete)
        animationAdd = AnimationUtils.loadAnimation(applicationContext, R.anim.anim_left_to_right)
        filesFolder = filesDir.path + "/"
        Column.titleColor = getColorFromRes(R.color.textColorTabsTitleNormal)
        NumerationColumn.color = getColorFromRes(R.color.textColorSecondary)
        PrefForCard.nameColor = getColorFromRes(R.color.colorTitle)

        PhoneColumn.apply {
            nameOfMan = getString(R.string.name)
            lastName = getString(R.string.last_name)
            phone = getString(R.string.phone)
            organization = getString(R.string.organization)
        }

        val isFirst = pref.getBoolean(prefFirstKey, true)
        if (isFirst) {
            pref.edit().putBoolean(prefFirstKey, false).apply()
            // должен быть в главном потоке
            DataController(this).addPage(getString(R.string.active))
            thread {
                SampleHelper(applicationContext).addDefaultSamples(applicationContext)
            }
        }
        super.onCreate()

        Bugsnag.init(this)
    }

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

const val prefFirstKey = "isFirst"
const val prefSampleLastChanged = "sampleLastChanged"
const val prefEnableDate = "enableDate"
const val prefSortSelected = "sortSelected"

const val prefEnableSomeStrokeChanged = "enableSomeStrokeChanged"
const val prefDateLastChanged = "dateLastChanged"
