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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import ru.developer.press.myearningkot.adapters.animationAdd
import ru.developer.press.myearningkot.adapters.animationDelete
import ru.developer.press.myearningkot.database.DataController
import ru.developer.press.myearningkot.database.UpdatedRefData
import ru.developer.press.myearningkot.helpers.filesFolder
import ru.developer.press.myearningkot.helpers.getColorFromRes
import ru.developer.press.myearningkot.helpers.liveData
import ru.developer.press.myearningkot.model.*

class App : Application(), ActivityLifecycleCallbacks {

    lateinit var authUser: FirebaseAuth

    companion object {
        fun Activity.app(): App {
            return application as App
        }

        var fireStoreChanged = liveData<UpdatedRefData>()
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
    private val pref: SharedPreferences
        get() = getSharedPreferences("app.setting", Context.MODE_PRIVATE)

    override fun onCreate() {
        val dataController = DataController(applicationContext)
        authUser = Firebase.auth

        GlobalScope.launch {
            registerActivityLifecycleCallbacks(this@App)
            animationDelete = AnimationUtils.loadAnimation(applicationContext, R.anim.anim_delete)
            animationAdd =
                AnimationUtils.loadAnimation(applicationContext, R.anim.anim_left_to_right)
            filesFolder = filesDir.path + "/"
            Column.titleColor = getColorFromRes(R.color.textColorTabsTitleNormal)
            NumerationColumn.color = getColorFromRes(R.color.textColorSecondary)
            PrefForCard.nameColor = getColorFromRes(R.color.colorTitle)

            PhoneColumn.apply {
                nameOfMan = getString(R.string.name_man)
                lastName = getString(R.string.last_name)
                phone = getString(R.string.phone)
                organization = getString(R.string.organization)
            }

            Bugsnag.init(applicationContext)
            val isFirst = pref.getBoolean(prefFirstKey, true)
            if (isFirst) {
                dataController.createDefaultSamplesJob(applicationContext)
                pref.edit().putBoolean(prefFirstKey, false).apply()
            }
            dataController.syncRefs()
            super.onCreate()
        }
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
        if (activity is AppCompatActivity)
            currentActivity = activity
    }


}

const val prefFirstKey = "isFirst"
const val prefSampleLastChanged = "sampleLastChanged"
const val prefEnableDate = "enableDate"
const val prefSortSelected = "sortSelected"

const val prefEnableSomeStrokeChanged = "enableSomeStrokeChanged"
const val prefDateLastChanged = "dateLastChanged"
