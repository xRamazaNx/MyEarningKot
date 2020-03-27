package ru.developer.press.myearningkot

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.edit_cell_text.view.*
import kotlinx.android.synthetic.main.main_cards_layout.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.support.v4.act
import ru.developer.press.myearningkot.activity.MainActivity
import ru.developer.press.myearningkot.adapters.AdapterRecyclerInPage
import ru.developer.press.myearningkot.dialogs.hideKeyboard
import ru.developer.press.myearningkot.dialogs.showKeyboard
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.model.Cell
import ru.developer.press.myearningkot.model.ColumnType


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

class EditCellFragment : Fragment() {

    var editCellParam: EditCellParam? = null
    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var resource = R.layout.edit_cell_text
        editCellParam?.apply {
            when (columnType) {
                ColumnType.TEXT -> resource = R.layout.edit_cell_text
                ColumnType.NUMBER -> resource = R.layout.edit_cell_number
                ColumnType.PHONE -> resource = R.layout.edit_cell_phone
                ColumnType.DATE -> resource = R.layout.edit_cell_date
                ColumnType.COLOR -> resource = R.layout.edit_cell_color
                ColumnType.SWITCH -> resource = R.layout.edit_cell_switch
                ColumnType.IMAGE -> resource = R.layout.edit_cell_image
                ColumnType.LIST -> resource = R.layout.edit_cell_list
                ColumnType.NUMERATION -> {
                }
                ColumnType.NONE -> {
                }
            }
        }
        return inflater.inflate(resource, null).apply {
            layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        view?.layoutParams?.height = matchParent
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        view?.requestLayout()
        view?.editCellText?.apply {
            post {
                requestLayout()
//                showKeyboard()
                if (!KeyboardVisibilityEvent.isKeyboardVisible(activity))
                    showKeyboard()
            }
        } ?: view?.hideKeyboard()
        super.onResume()
    }

    private fun showKeyboard(editText: EditText) {
        editText.dispatchTouchEvent(
            MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                0f,
                0f,
                0
            )
        )
        editText.dispatchTouchEvent(
            MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP,
                0f,
                0f,
                0
            )
        )
    }

    class EditCellParam(val cell: Cell, val columnType: ColumnType)
}

