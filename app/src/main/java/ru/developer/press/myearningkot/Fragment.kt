package ru.developer.press.myearningkot

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.stfalcon.imageviewer.StfalconImageViewer
import kotlinx.android.synthetic.main.main_cards_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import ru.developer.press.myearningkot.adapters.AdapterCard
import ru.developer.press.myearningkot.database.Card
import ru.developer.press.myearningkot.database.Page
import ru.developer.press.myearningkot.helpers.MyLiveData
import ru.developer.press.myearningkot.helpers.runOnMain

class PageFragment : Fragment() {
    var page: Page? = null
    private var adapterCard: AdapterCard? = null
    private var recycler: RecyclerView? = null

    companion object {
        private const val PAGE_ID = "page_id"
        fun create(pageId: String): PageFragment {
            val pageFragment = PageFragment()
            pageFragment.arguments = Bundle().apply {
                putString(PAGE_ID, pageId)
            }
            return pageFragment
        }

        var getPageLiveData: ((String) -> Page)? = null

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPageLiveData?.let {
            page = it.invoke(arguments?.getString(PAGE_ID)!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflate = inflater.inflate(R.layout.main_cards_layout, null)
        recycler = inflate.recyclerCards
        iniAdapter(page!!)
        return inflate
    }

    private fun iniAdapter(page: Page) {
        lifecycleScope.launch(Dispatchers.IO) {
            adapterCard = AdapterCard(page.cards)
            runOnMain {
                recycler?.adapter = adapterCard
            }
        }
    }

    fun insertToPosition(cardPosition: Int) {
        adapterCard?.notifyDataSetChanged()
        recycler?.scrollToPosition(cardPosition)
    }
}

//
//
//
//
//
class ImageFragment : Fragment() {
    var isErrorImage = false
    var imagePath: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return context?.let { ctx ->
            FrameLayout(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(matchParent, matchParent).apply {
                    gravity = Gravity.CENTER
                }
                addView(ImageView(ctx).apply {
                    val dpsToPixels = context.dip(300)
                    layoutParams = FrameLayout.LayoutParams(dpsToPixels, matchParent).apply {
                        gravity = Gravity.CENTER
                    }
                })
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val frame = view as FrameLayout
        val imageView = frame.getChildAt(0) as ImageView

        imageView.setOnClickListener {
            if (!isErrorImage)
                StfalconImageViewer.Builder(
                    activity,
                    arrayOf(imagePath)
                ) { view: ImageView, image: String? ->
                    Glide
                        .with(this)
                        .load(image)
                        .fitCenter()
                        .into(view)
                }.show()
        }
        Glide
            .with(this)
            .load(imagePath)
            .error(R.drawable.ic_image_error)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    isErrorImage = true
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

            })
            .fitCenter()
            .into(imageView)
    }

}

