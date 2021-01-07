package ru.developer.press.myearningkot.helpers.prefLayouts

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.View.GONE
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.formula_layout.view.*
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.textColor
import org.jetbrains.anko.wrapContent
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.dpsToPixels
import ru.developer.press.myearningkot.model.Formula
import ru.developer.press.myearningkot.model.Formula.Companion.COLUMN_ID
import ru.developer.press.myearningkot.model.Formula.Companion.OTHER
import ru.developer.press.myearningkot.model.Formula.Companion.TOTAL_ID
import ru.developer.press.myearningkot.model.NumberColumn
import ru.developer.press.myearningkot.model.TotalItem
import ru.developer.press.myearningkot.helpers.Calc
import ru.developer.press.myearningkot.helpers.getColorFromRes
import splitties.alertdialog.appcompat.alertDialog
import java.lang.Exception
import java.lang.StringBuilder


val subtractChar = " − "
val multiplyChar = " × "

class FormulaLayout(
    val view: View,
    filterNColumns: List<NumberColumn>,
    private val allNColumns: List<NumberColumn> = filterNColumns,
    filterNTotals: List<TotalItem>? = null,
    private val allNTotals: List<TotalItem>? = filterNTotals,
    _formula: Formula
) {
    private var formula = Formula()
    private val columnList = mutableListOf<NumberColumn>()
    private val totalList = mutableListOf<TotalItem>()
    private val displayTextView: TextView = view.formulaTextView

    init {

        formula.copyFrom(_formula)
        columnList.addAll(filterNColumns)
        filterNTotals?.let {
            totalList.addAll(it)
        }

        initClickNumbers()
        initClickOperation(view){
            formula.formulaElements.add(Formula.FormulaElement().apply {
                type = OTHER
                value = " $it "
            })
            displayFormula()
        }
        initClickColumns()
        initClickTotals()

        view.clearElementInFormula.setOnClickListener {
            formula.formulaElements.apply {
                if (isNotEmpty()) {
                    removeAt(size - 1)
                    displayFormula()
                }
            }
        }

        displayFormula()
    }

    private fun initClickTotals() {
        val title = view.totalContainerTitle
        val container = view.totalsContainerInFormula

        if (totalList.isEmpty()){
            title.visibility = GONE
        }
        val elementList = formula.formulaElements
        totalList.forEach {total ->
            val textView = TextView(view.context).apply {
                initParamTextView()
                text = total.title
                textColor = view.context.getColorFromRes(R.color.md_blue_200)
                setOnClickListener {
                    elementList.add(Formula.FormulaElement().apply {
                        type = TOTAL_ID
                        value = total.id.toString()
                    })

                    displayFormula()
                }
            }
            container.addView(textView)
        }

    }

    private fun initClickColumns() {
        val container = view.columnContainerInFormula
        val elementList = formula.formulaElements
        columnList.forEach { column ->
            val textView = TextView(view.context).apply {
                initParamTextView()
                text = column.name
                textColor = view.context.getColorFromRes(R.color.md_green_300)
                setOnClickListener {
                    elementList.add(Formula.FormulaElement().apply {
                        type = COLUMN_ID
                        value = column.id.toString()
                    })

                    displayFormula()
                }
            }
            container.addView(textView)
        }


    }

    private fun TextView.initParamTextView() {
        padding = context.dpsToPixels(16)
        textColor = context.getColorFromRes(R.color.textColorPrimary)
        layoutParams = LinearLayout.LayoutParams(wrapContent, matchParent)
    }



    private fun initClickNumbers() {
        val one = view.one
        val two = view.two
        val three = view.three
        val four = view.four
        val five = view.five
        val six = view.six
        val seven = view.seven
        val eight = view.eight
        val nine = view.nine
        val zero = view.zero

        val click: (View) -> Unit = {
            val textView = it as TextView

            formula.formulaElements.add(Formula.FormulaElement().apply {
                type = OTHER
                value = textView.text.toString()
            })

            displayFormula()
        }
        one.setOnClickListener(click)
        two.setOnClickListener(click)
        three.setOnClickListener(click)
        four.setOnClickListener(click)
        five.setOnClickListener(click)
        six.setOnClickListener(click)
        seven.setOnClickListener(click)
        eight.setOnClickListener(click)
        nine.setOnClickListener(click)
        zero.setOnClickListener(click)
    }

    private fun displayFormula() {
        displayTextView.text = formula.getFormulaString(allNColumns, allNTotals)
    }


    fun getFormula(): Formula? {

        // заварушка лишняя для проверки подленности формулы
        // если нет то загорится красным
        val stringBuilder = StringBuilder()

        formula.formulaElements.forEach {
            when (it.type) {
                TOTAL_ID,
                COLUMN_ID -> {
                    stringBuilder.append(1.345)
                }
                OTHER -> {
                    stringBuilder.append(it.value)
                }
            }
        }

        return try {
            Calc.evaluate(stringBuilder.toString())
            formula
        } catch (exception: Exception) {
            null
        }
    }

    fun errorFormula() {
        displayTextView.textColor = displayTextView.context.getColorFromRes(R.color.colorRed)
    }
}


fun formulaDialogShow(
    formula: Formula,
    context: Context,
    filterNColumns: List<NumberColumn>,
    allNColumns: List<NumberColumn> = filterNColumns,
    filterNTotals: List<TotalItem>?,
    allNTotals: List<TotalItem>?,
    positiveClick: (Formula) -> Unit
) {
    val inflate = View.inflate(context, R.layout.formula_layout, null)
    val formulaLayout = FormulaLayout(
        view = inflate,
        filterNColumns = filterNColumns,
        allNColumns = allNColumns,
        filterNTotals = filterNTotals,
        allNTotals = allNTotals,
        _formula = formula
    )

    val dialog = context.alertDialog {
        setCustomTitle(TextView(context).apply {
            padding = context.dpsToPixels(18)
            textSize = 18F
            text = "Введите формулу"
            textColor = context.getColorFromRes(R.color.textColorPrimary)
        })
        setView(inflate)
        setPositiveButton("OK", null)
        setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
    }
    dialog.show()
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
        textColor = context.getColorFromRes(R.color.accent)
        setOnClickListener {
            formulaLayout.getFormula()?.let {
                positiveClick(it)
                dialog.dismiss()
            } ?: formulaLayout.errorFormula()
        }

    }
    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
        textColor = context.getColorFromRes(R.color.accent)
    }
    dialog.window?.setBackgroundDrawable(ColorDrawable(context.getColorFromRes(R.color.colorPrimary)))
}
fun initClickOperation(view: View, callBack : (String) -> Unit) {
    val add = view.add
    val sub = view.subtract
    val mult = view.multiply
    val div = view.divide
    val percent = view.percent
    val leftBracket = view.leftBracket
    val rightBracket = view.rightBracket
    val point = view.point

    val click: (View) -> Unit = {
        val textView = it as TextView
        var op = textView.text.toString()
        if (it == view.subtract)
            op = "-"
        if (it == view.multiply)
            op = "*"

        callBack(op)
    }

    add.setOnClickListener(click)
    sub.setOnClickListener(click)
    mult.setOnClickListener(click)
    div.setOnClickListener(click)
    percent.setOnClickListener(click)
    leftBracket.setOnClickListener(click)
    rightBracket.setOnClickListener(click)
    point.setOnClickListener(click)
}