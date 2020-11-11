package ru.developer.press.myearningkot

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.stfalcon.imageviewer.StfalconImageViewer
import kotlinx.android.synthetic.main.main_cards_layout.*
import org.jetbrains.anko.matchParent
import ru.developer.press.myearningkot.activity.MainActivity
import ru.developer.press.myearningkot.adapters.AdapterRecyclerInPage
import ru.developer.press.myearningkot.model.Card


class PageFragment : Fragment() {
    var cards: MutableList<Card> = mutableListOf()
    private lateinit var cardClickListener: CardClickListener
    private var adapterRecyclerInPage: AdapterRecyclerInPage? = null
    private var recycler: RecyclerView? = null

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_cards_layout, null)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        cardClickListener = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler = recyclerCards
        recycler?.layoutManager = LinearLayoutManager(context)
        adapterRecyclerInPage = AdapterRecyclerInPage(cards, cardClickListener)
        updateRecycler()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun updateRecycler() {

        recycler?.adapter = adapterRecyclerInPage

    }

    fun scrollToPosition(cardPosition: Int) {
        recycler?.smoothScrollToPosition(cardPosition)
        adapterRecyclerInPage?.animateCardUpdated(cardPosition)
    }

    fun notifyCardInRecycler(positionCard: Int) {
        scrollToPosition(positionCard)
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
                    val dpsToPixels = context.dpsToPixels(300)
                    layoutParams = FrameLayout.LayoutParams(dpsToPixels, matchParent).apply {
                        gravity = Gravity.CENTER
                    }
                })
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        toast(imageUri.toString())
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

