package ru.developer.press.myearningkot.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View.GONE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.widget.ViewPager2
import co.zsmb.materialdrawerkt.builders.accountHeader
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.builders.footer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.badgeable.secondaryItem
import co.zsmb.materialdrawerkt.draweritems.expandable.expandableItem
import co.zsmb.materialdrawerkt.draweritems.profile.profile
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mikepenz.materialdrawer.Drawer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.*
import org.jetbrains.anko.textColorResource
import ru.developer.press.myearningkot.*
import ru.developer.press.myearningkot.adapters.AdapterViewPagerMain
import ru.developer.press.myearningkot.dialogs.DialogCreateCard
import ru.developer.press.myearningkot.dialogs.DialogSetName
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.model.DataController
import ru.developer.press.myearningkot.helpers.getColorFromRes
import ru.developer.press.myearningkot.viewmodels.PageViewModelController
import ru.developer.press.myearningkot.viewmodels.ViewModelMainFactory

// GITHUB
const val ID_UPDATE_CARD = "id_card"

class MainActivity : AppCompatActivity(), ProvideDataCards, CardClickListener {
    private lateinit var drawer: Drawer
    private lateinit var adapterViewPagerMain: AdapterViewPagerMain

    private var initializerViewModel: Job = GlobalScope.launch(Dispatchers.Main) {
        val pageList = withContext(Dispatchers.IO) {
            DataController().getPageList()
        }
        viewModel = withContext(Dispatchers.IO) {
            ViewModelProviders.of(
                this@MainActivity,
                ViewModelMainFactory(
                    pageList
                )
            )
                .get(PageViewModelController::class.java)
        }
        progressBar.visibility = GONE
    }

    private lateinit var viewModel: PageViewModelController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // он должен быть тут первым а то статусбар внизу оказывается из за поздей инициализации
        initDrawer()
        toolbar.setTitleTextColor(Color.WHITE)

        initializerViewModel.start()
    }

    private fun viewInit() {
        viewModel.openCardEvent.observe(this@MainActivity, observer = Observer { id ->
            // для дальнейшего обновления когда опять выйду в маин
            App.instance?.setUpdateCardId(id)
            val intent =
                Intent(this@MainActivity, CardActivity::class.java).apply {
                    putExtra(CARD_ID, id)
                }
            startActivity(intent)
        })

        initTabAndViewPager()
        // настройка fb при скрытии и показе тулбара
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->

            val heightToolbar = appBarLayout.toolbar.height

            val isHide = -verticalOffset == heightToolbar
            val isShow = verticalOffset == 0
            fbMain.apply {
                val animator = animate().setDuration(400)
                if (isShow) {
                    animator
                        .translationX(0f)
                        .alpha(1f)
                        .start()
                } else if (isHide) {

                    animator
                        .translationX((resources.displayMetrics.widthPixels / 2).toFloat())
                        .alpha(0f)
                        .start()
                }
            }
        })
        // настройка клика fb
        fbMain.setOnClickListener {

            val indexPage = tabs.selectedTabPosition
            //            viewModel.addCard(indexPage, Card())
            DialogCreateCard { card ->
                // что произхойдет при нажатии на "создать"
                viewModel.addCard(indexPage, card) { positionCard ->
                    appBar.setExpanded(false, true).apply {
                        adapterViewPagerMain.scrollToPosition(indexPage, positionCard)
                    }
                }
            }.show(supportFragmentManager, "createCard")
        }

        addPageButton.setOnClickListener {
            DialogSetName { pageName ->
                GlobalScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        viewModel.addPage(pageName)
                    }
                    adapterViewPagerMain.addPage()
                    val index = viewModel.getPageCount() - 1
                    adapterViewPagerMain.notifyItemInserted(index)
                    delay(300)
                    tabs.getTabAt(index)?.select()
                }
            }.show(supportFragmentManager, "setName")
        }
    }

    private fun initTabAndViewPager() {
        this.adapterViewPagerMain = AdapterViewPagerMain(
            supportFragmentManager,
            lifecycle,
            viewModel
        )
        viewPager.adapter = adapterViewPagerMain

        linkViewPagerAndTabs(tabs, viewPager, mutableListOf<String>().apply {
            repeat(viewModel.getPageCount()) {
                add(viewModel.getTabName(it))
            }
        })
    }

    private fun initDrawer() {
        val lightGray = R.color.centDark
        drawer = drawer {
            selectedItem = -1
            toolbar = this@MainActivity.toolbar
            closeOnClick = false
//            sliderBackgroundColorRes = R.color.white
            actionBarDrawerToggleAnimated = true
//            headerViewRes = R.layout.card_view
//            footerDivider = true
            headerDivider = true

            accountHeader {
                selectionListEnabledForSingleProfile = false
                currentHidden = true
//                selectionSecondLine = "This is not an email!" // вместо него показан маил
                threeSmallProfileImages = false
                textColorRes = R.color.gray
//                background = ContextCompat.getColor(this@MainActivity, R.color.centDark)
                backgroundDrawable = ColorDrawable(getColorFromRes(R.color.centDark))

                profile("Profile 1", "user1@gmail.com") {
                    textColorRes = R.color.white
                }

                onProfileChanged { _, profile, _ ->
                    toast("Selected ${profile.name}")
                    false
                }
                onProfileImageLongClick { _, profile, _ ->
                    toast("Long clicked ${profile.name}")
                    true
                }
            }
            //
            expandableItem {
                nameRes = R.string.sort
                selectable = false
                textColorRes = lightGray
                arrowColorRes = lightGray
                iconDrawable = getDrawable(R.drawable.ic_sort_dark)!!
                arrowRotationAngle = Pair(90, 0)


                primaryItem(getString(R.string.to_date_create)) {
                    textColorRes = lightGray
                    level = 2
                    iconDrawable = getDrawable(R.drawable.ic_create)!!
                }
                primaryItem(getString(R.string.to_date_modify)) {
                    textColorRes = lightGray
                    iconDrawable = getDrawable(R.drawable.ic_edit)!!
                    level = 2
                }

            }
            secondaryItem(getString(R.string.settings_label)) {
                selectable = false
                textColorRes = lightGray
                iconDrawable = getDrawable(R.drawable.ic_setting_table)!!
                onClick { _ ->
                    drawer.closeDrawer()
                    true
                }
                // значок с надписью с право от item
//                badge("111") {
//                    cornersDp = 0
//                    color = 0xFF0099FF
//                    colorPressed = 0xFFCC99FF
//                }
            }
            // нижний отдельный бар
            footer {
                // о программе
                primaryItem(getString(R.string.info_programm)) {
                    selectable = false
                    textColorRes = lightGray
                    iconDrawable = getDrawable(R.drawable.ic_info)!!
                    iconColorRes = lightGray
                    onClick { _ ->
                        true
                    }
                }
            }
        }
    }

    override fun getCard(position: Int): Card {
        return viewModel.getCardInPage(tabs.selectedTabPosition, position)
    }

    override fun getSize(): Int {
        return viewModel.getPages()[tabs.selectedTabPosition].cards.size
    }

    override fun cardClick(idCard: Long) {
        viewModel.openCardEvent.call(idCard)
    }

    override fun onResume() {
        super.onResume()
        initializerViewModel.invokeOnCompletion {
            initTabAndViewPager()

            viewModel.calcAllCards()
            viewInit()
            val instance = App.instance
            val id = instance?.getUpdateCardId()
            if (id != null && id > -1) {
                val position = viewModel.updateCardInPage(
                    id,
                    tabs.selectedTabPosition
                )
                viewPager.post {
                    adapterViewPagerMain.notifyCardInPage(tabs.selectedTabPosition, position)
                }
                instance.setUpdateCardId(-1)
            }
            adapterViewPagerMain.notifyDataSetChanged()

//            viewPager.postDelayed(500) {
//                val sizeVM = viewModel.getPages()[0].cards.size
//                val sizeVP = adapterViewPagerMain.fragments[0].cards.size
//
//                if (sizeVM != sizeVP) {
//                    initTabAndViewPager()
//                }
////                toast("cards in page for viewModel = $sizeVM \n cards in page for viewPager = $sizeVP")
//            }
        }

    }

}

fun linkViewPagerAndTabs(tabs: TabLayout, viewPager: ViewPager2, nameList: List<String>) {
    val context = tabs.context
    TabLayoutMediator(tabs, viewPager) { tab, position ->
        val tabTextView = TextView(context).apply {
            isSingleLine = true
            layoutParams = TableLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT).apply {
                weight = 0f
            }
            gravity = Gravity.CENTER
            text = nameList[position]
            textColorResource =
                R.color.dark_text
            if (position == 0) {
                textColorResource =
                    R.color.white

            }
        }
        tab.customView = tabTextView
        tab.view.layoutParams =
            LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT).apply {
                weight = 0f
            }
    }.attach()


    tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        val white = R.color.white
        override fun onTabReselected(tab: TabLayout.Tab?) {
            (tab?.customView as TextView).textColorResource = white
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            (tab?.customView as TextView).textColorResource =
                R.color.dark_text
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            (tab?.customView as TextView).textColorResource = white
        }

    })
}

fun Context.toast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
