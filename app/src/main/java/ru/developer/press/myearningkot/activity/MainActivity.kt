package ru.developer.press.myearningkot.activity

import android.content.Context
import android.content.Intent
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
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.MarginPageTransformer
import co.zsmb.materialdrawerkt.builders.accountHeader
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.badgeable.secondaryItem
import co.zsmb.materialdrawerkt.draweritems.expandable.expandableItem
import co.zsmb.materialdrawerkt.draweritems.profile.profile
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mikepenz.materialdrawer.Drawer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import org.jetbrains.anko.collections.forEachReversedWithIndex
import org.jetbrains.anko.dip
import org.jetbrains.anko.textColorResource
import ru.developer.press.myearningkot.*
import ru.developer.press.myearningkot.adapters.AdapterViewPagerFromMain
import ru.developer.press.myearningkot.databinding.ActivityMainBinding
import ru.developer.press.myearningkot.dialogs.DialogSetName
import ru.developer.press.myearningkot.helpers.Page
import ru.developer.press.myearningkot.helpers.getColorFromRes
import ru.developer.press.myearningkot.helpers.getDrawableRes
import ru.developer.press.myearningkot.helpers.setFont
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.model.DataController
import ru.developer.press.myearningkot.viewmodels.MainViewModel
import ru.developer.press.myearningkot.viewmodels.ViewModelMainFactory

class MainActivity : AppCompatActivity(), ProvideDataCards, CardClickListener {
    private lateinit var drawer: Drawer
    private lateinit var adapterViewPagerFromMain: AdapterViewPagerFromMain
    private val initObserver = MutableLiveData<(() -> Unit)>()

    private var initializerViewModel: Job = GlobalScope.launch(Dispatchers.Main) {
        val pageList = withContext(Dispatchers.IO) {
            DataController(this@MainActivity).getPageList()
        }
        viewModel = ViewModelProvider(
            this@MainActivity,
            ViewModelMainFactory(this@MainActivity, pageList)
        ).get(
            MainViewModel::class.java
        )
        root.progressBar.visibility = GONE
        initObserver.value = {
            viewModel.calcAllCards()
            viewInit()
        }
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var root: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = ActivityMainBinding.inflate(layoutInflater)
        setContentView(root.root)
        setSupportActionBar(root.toolbar)
        // он должен быть тут первым а то статусбар внизу оказывается из за поздей инициализации
        initDrawer()
        root.toolbar.setTitleTextColor(getColorFromRes(R.color.colorOnPrimary))

        initializerViewModel.start()
        initObserver.observe(this, {
            it?.invoke()
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK)
            when (requestCode) {
                CreateCardActivity.createCardRequest -> {
                    val id = data?.getLongExtra(CreateCardActivity.createCardID, -1)
                    val name = data?.getStringExtra(CreateCardActivity.createCardName)
                    if (id != null) {
                        if (id > -1) {
                            val indexPage = tabs.selectedTabPosition
                            viewModel.createCard(indexPage, id, name ?: "") { positionCard ->
                                adapterViewPagerFromMain.scrollToPosition(
                                    indexPage,
                                    positionCard
                                )
                                root.appBar.setExpanded(false, true)
                            }
                        }
                    }
                }
            }
    }

    private fun viewInit() {
        // нажали на карточку
        viewModel.openCardEvent.observe(this, Observer { id ->
            // для дальнейшего обновления когда опять выйду в маин
            val intent =
                Intent(this@MainActivity, CardActivity::class.java).apply {
                    putExtra(CARD_ID, id)
                }
            startActivity(intent)
        })

        initTabAndViewPager()
        // настройка fb при скрытии и показе тулбара
        root.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->

            val heightToolbar = root.toolbar.height

            val isHide = -verticalOffset == heightToolbar
            val isShow = verticalOffset == 0
            root.fbMain.apply {
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
        root.fbMain.setOnClickListener {

            val indexPage = root.tabs.selectedTabPosition
            //            viewModel.addCard(indexPage, Card())
            startActivityForResult(
                Intent(this, CreateCardActivity::class.java),
                CreateCardActivity.createCardRequest
            )
//            DialogCreateCard { card ->
//                 что произхойдет при нажатии на "создать"
//                viewModel.addCard(indexPage, card) { positionCard ->
//                    root.appBar.setExpanded(false, true).apply {
//                        adapterViewPagerMain.scrollToPosition(indexPage, positionCard)
//                    }
//                }
//            }.show(supportFragmentManager, "createCard")
        }

        root.addPageButton.setOnClickListener {
            DialogSetName().setTitle(getString(R.string.create_page))
                .setPositiveListener { pageName ->
                    viewModel.addPage(pageName) { _: Page ->
                        adapterViewPagerFromMain.addPage()
                        linkViewPagerAndTabs()
                        root.tabs.postDelayed({
                            root.tabs.getTabAt(root.tabs.tabCount - 1)?.select()
                        }, 200)
                    }

                }.show(supportFragmentManager, "setName")
        }
        val pages = viewModel.getPages()
        pages.forEachReversedWithIndex { index, it ->
            it.observe(this@MainActivity, Observer {
                root.tabs.getTabAt(index)?.select()
            })
        }
//        pages.forEachIndexed { index, it ->
//            it.observe(this@MainActivity, Observer {
//                tabs.getTabAt(index)?.select()
//            })
//        }
    }

    private fun initTabAndViewPager() {
        this.adapterViewPagerFromMain = AdapterViewPagerFromMain(
            supportFragmentManager,
            lifecycle,
            viewModel
        )
        root.viewPager.adapter = adapterViewPagerFromMain
        root.viewPager.setPageTransformer(MarginPageTransformer(dip(4)))

        linkViewPagerAndTabs()
    }

    private fun initDrawer() {
        val textColor = R.color.textColorPrimary

        drawer = drawer {
            selectedItem = -1
            toolbar = root.toolbar
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
                iconDrawable = getDrawableRes(R.drawable.ic_sort)!!
                arrowRotationAngle = Pair(90, 0)

                primaryItem(getString(R.string.to_date_create)) {
                    onClick { view, position, drawerItem ->
                        true
                    }
                    selectedColorRes = R.color.colorTransparent
                    selectedTextColorRes = R.color.colorAccent
                    textColorRes = textColor
                    level = 2
                    selectedIconDrawable = getDrawableRes(R.drawable.ic_create_selected)!!
                    iconDrawable = getDrawableRes(R.drawable.ic_create)!!
                }
                primaryItem(getString(R.string.to_date_modify)) {
                    onClick { view, position, drawerItem ->
                        true
                    }
                    selectedColorRes = R.color.colorTransparent
                    selectedTextColorRes = R.color.colorAccent
                    textColorRes = textColor
                    selectedIconDrawable = getDrawableRes(R.drawable.ic_edit_selected)!!
                    iconDrawable = getDrawableRes(R.drawable.ic_edit)!!
                    level = 2
                }

            }
            secondaryItem(getString(R.string.settings_label)) {
                selectable = false
                textColorRes = textColor
                iconDrawable = getDrawableRes(R.drawable.ic_setting)!!
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
        return viewModel.getCardInPage(root.tabs.selectedTabPosition, position)
    }

    override fun onResume() {
        super.onResume()
        initializerViewModel.invokeOnCompletion {
            viewModel.checkUpdatedCard()
        }
    }

    override fun getSize(): Int {
        return viewModel.getPages()[root.tabs.selectedTabPosition].value!!.cards.size
    }

    override fun cardClick(idCard: Long) {
        viewModel.cardClick(idCard)
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
        val context = root.tabs.context
        TabLayoutMediator(root.tabs, root.viewPager) { tab, position ->
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

        root.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

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
