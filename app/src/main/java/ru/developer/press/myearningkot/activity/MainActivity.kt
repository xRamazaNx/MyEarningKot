package ru.developer.press.myearningkot.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.MarginPageTransformer
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
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.mikepenz.materialdrawer.Drawer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.*
import org.jetbrains.anko.collections.forEachReversedByIndex
import org.jetbrains.anko.collections.forEachReversedWithIndex
import org.jetbrains.anko.dip
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textColorResource
import ru.developer.press.myearningkot.*
import ru.developer.press.myearningkot.adapters.AdapterViewPagerMain
import ru.developer.press.myearningkot.dialogs.DialogCreateCard
import ru.developer.press.myearningkot.dialogs.DialogSetName
import ru.developer.press.myearningkot.helpers.getColorFromRes
import ru.developer.press.myearningkot.helpers.setFont
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.model.DataController
import ru.developer.press.myearningkot.viewmodels.MainViewModel
import ru.developer.press.myearningkot.viewmodels.ViewModelMainFactory


// GITHUB
const val ID_UPDATE_CARD = "id_card"

class MainActivity : AppCompatActivity(), ProvideDataCards, CardClickListener {
    private lateinit var drawer: Drawer
    private lateinit var adapterViewPagerMain: AdapterViewPagerMain
    private val initObserver = MutableLiveData<(() -> Unit)>()

    private var initializerViewModel: Job = GlobalScope.launch(Dispatchers.Main) {
        val pageList = withContext(Dispatchers.IO) {
            DataController().getPageList()
        }
        viewModel = ViewModelProvider(this@MainActivity, ViewModelMainFactory(pageList)).get(
            MainViewModel::class.java
        )
//            ViewModelProviders.of(
//                this@MainActivity,
//                ViewModelMainFactory(
//                    pageList
//                )
//            ).get(PageViewModelController::class.java)

        progressBar.visibility = GONE
        initObserver.value = {
            viewModel.calcAllCards()
            viewInit()
        }
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        // он должен быть тут первым а то статусбар внизу оказывается из за поздей инициализации
        initDrawer()
        toolbar.setTitleTextColor(getColorFromRes(R.color.colorOnPrimary))

        initializerViewModel.start()
        initObserver.observe(this, Observer {
            it?.invoke()
        })

    }

    private fun viewInit() {
        // нажали на карточку
        viewModel.openCardEvent.observe(this, Observer { id ->
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
                    viewModel.addPage(pageName)
                    adapterViewPagerMain.addPage()
                    linkViewPagerAndTabs()
                    tabs.postDelayed({
                        tabs.getTabAt(tabs.tabCount - 1)?.select()
                    }, 200)
                }
            }.show(supportFragmentManager, "setName")
        }
        val pages = viewModel.getPages()
        pages.forEachReversedWithIndex { index, it ->
            it.observe(this@MainActivity, Observer {
                tabs.getTabAt(index)?.select()
            })
        }
//        pages.forEachIndexed { index, it ->
//            it.observe(this@MainActivity, Observer {
//                tabs.getTabAt(index)?.select()
//            })
//        }
    }

    private fun initTabAndViewPager() {
        this.adapterViewPagerMain = AdapterViewPagerMain(
            supportFragmentManager,
            lifecycle,
            viewModel
        )
        viewPager.adapter = adapterViewPagerMain
        viewPager.setPageTransformer(MarginPageTransformer(dip(4)))

        linkViewPagerAndTabs()
    }

    private fun initDrawer() {
        val textColor = R.color.textColorPrimary

        drawer = drawer {
            selectedItem = -1
            toolbar = this@MainActivity.toolbar
            closeOnClick = false
            sliderBackgroundColorRes = R.color.colorPrimary
            actionBarDrawerToggleAnimated = true
//            headerViewRes = R.layout.card_view
//            footerDivider = true
            headerDivider = true

            accountHeader {

                this.emailTypeface =
                    ResourcesCompat.getFont(this@MainActivity, R.font.roboto_light)!!
                selectionListEnabledForSingleProfile = false
                currentHidden = true
//                selectionSecondLine = "This is not an email!" // вместо него показан маил
                threeSmallProfileImages = false
                textColorRes = textColor
//                background = ContextCompat.getColor(this@MainActivity, R.color.centDark)
                backgroundDrawable = ColorDrawable(getColorFromRes(R.color.colorBackground))

                profile("Profile 1", "user1@gmail.com") {
                    textColorRes = R.color.textColorTertiary
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
                textColorRes = textColor
                arrowColorRes = textColor
                iconDrawable = getDrawable(R.drawable.ic_sort)!!
                arrowRotationAngle = Pair(90, 0)

                primaryItem(getString(R.string.to_date_create)) {
                    onClick { view, position, drawerItem ->
                        true
                    }
                    selectedColorRes = R.color.colorTransparent
                    selectedTextColorRes = R.color.colorAccent
                    textColorRes = textColor
                    level = 2
                    selectedIconDrawable = getDrawable(R.drawable.ic_create_selected)!!
                    iconDrawable = getDrawable(R.drawable.ic_create)!!
                }
                primaryItem(getString(R.string.to_date_modify)) {
                    onClick { view, position, drawerItem ->
                        true
                    }
                    selectedColorRes = R.color.colorTransparent
                    selectedTextColorRes = R.color.colorAccent
                    textColorRes = textColor
                    selectedIconDrawable = getDrawable(R.drawable.ic_edit_selected)!!
                    iconDrawable = getDrawable(R.drawable.ic_edit)!!
                    level = 2
                }

            }
            secondaryItem(getString(R.string.settings_label)) {
                selectable = false
                textColorRes = textColor
                iconDrawable = getDrawable(R.drawable.ic_setting)!!
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
//            footer {
//                // о программе
//                primaryItem(getString(R.string.info_programm)) {
//                    textColorRes = textColor
//                    iconDrawable = getDrawable(R.drawable.ic_info)!!
//                    onClick { _ ->
//                        true
//                    }
//                }
//            }
        }
        drawer.actionBarDrawerToggle.drawerArrowDrawable.color =
            getColorFromRes(R.color.colorOnPrimary)
    }

    override fun getCard(position: Int): Card {
        return viewModel.getCardInPage(tabs.selectedTabPosition, position)
    }

    override fun getSize(): Int {
        return viewModel.getPages()[tabs.selectedTabPosition].value!!.cards.size
    }

    override fun cardClick(idCard: Long) {
        viewModel.openCardEvent.call(idCard)
    }

    override fun onResume() {
        super.onResume()
        initializerViewModel.invokeOnCompletion {
            val instance = App.instance
            val id = instance?.getUpdateCardId()
            if (id != null && id > -1) {
                // после того как зашли в карточку изменили что то и выходим назад
                // в списке карточек идет обновление карточки из табицы так как внутри мы ее сохраняли и надо вытащить и обновить ее и в списке
                val selectedTabPosition = tabs.selectedTabPosition
                if (selectedTabPosition > -1) {
                    val position = viewModel.updateCardInPage(
                        id,
                        selectedTabPosition
                    )
                    viewPager.post {
                        adapterViewPagerMain.notifyCardInPage(selectedTabPosition, position)
                    }
                }
                instance.setUpdateCardId(-1)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.page_bacground_color -> {
//                val tabs = tabs
//                val position = tabs.selectedTabPosition
//                ColorPickerDialog.newBuilder()
//                    .setColor(viewModel.getPages()[position].value!!.background)
//                    .create().apply {
//                        setColorPickerDialogListener(object : ColorPickerDialogListener {
//                            override fun onColorSelected(dialogId: Int, color: Int) {
////                                tabs.getTabAt(tabs.selectedTabPosition)?.select()
//                                viewModel.pageColorChanged(color, position)
//
//                            }
//
//                            override fun onDialogDismissed(dialogId: Int) {
//
//                            }
//                        })
//                    }.show(supportFragmentManager, "pageBackgroundColorDialog")
//            }
        }
        return true
    }

    private fun TabLayout.Tab.tabSelected() {
        val textView = customView as TextView
        textView.textColorResource = R.color.textColorTabsTitleSelected
        parent?.setSelectedTabIndicatorColor(getColorFromRes(R.color.textColorTabsTitleSelected))
    }

    private fun linkViewPagerAndTabs() {
        val nameList = mutableListOf<String>().apply {
            repeat(viewModel.getPageCount()) {
                add(viewModel.getTabName(it))
            }
        }
        val context = tabs.context
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            val tabTextView = TextView(context).apply {
                textSize = 16f
                isSingleLine = true
                layoutParams = TableLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT).apply {
                    weight = 0f
                }
                gravity = Gravity.CENTER
                text = nameList[position]

                setFont(R.font.roboto_medium)
                textColorResource = R.color.textColorTabsTitleNormal
            }
            tab.customView = tabTextView
            tab.view.apply {
                layoutParams =
                    LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT).apply {
                        weight = 0f
                    }
            }
            if (position == 0) {
                tab.view.post {
                    tab.tabSelected()
                }
            }
        }.attach()

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.tabSelected()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val textView = tab?.customView as TextView
                textView.textColorResource = R.color.textColorTabsTitleNormal
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.tabSelected()
            }

        })
    }
}


fun Context.toast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
