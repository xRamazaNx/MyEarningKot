package ru.developer.press.myearningkot.otherHelpers.PrefLayouts

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
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
import ru.developer.press.myearningkot.model.Column
import ru.developer.press.myearningkot.model.Formula
import ru.developer.press.myearningkot.model.Formula.Companion.COLUMN_ID
import ru.developer.press.myearningkot.model.Formula.Companion.OTHER
import ru.developer.press.myearningkot.model.NumberColumn
import ru.developer.press.myearningkot.otherHelpers.Calc
import ru.developer.press.myearningkot.otherHelpers.getColorFromRes
import splitties.alertdialog.appcompat.alertDialog
import java.lang.Exception
import java.lang.StringBuilder


class FormulaLayout(val view: View, columns: List<NumberColumn>) {
    private val subtractChar = "−"
    private val multiplyChar = "×"
    private val formula = Formula()
    private val columnList = mutableListOf<Column>()
    private val displayTextView: TextView = view.formulaTextView

    init {

        columnList.addAll(columns)

        initClickNumbers()
        initClickOperation()
        initClickColumns()

        view.clearElementInFormula.setOnClickListener {
            formula.formulaElements.apply {
                if (isNotEmpty()) {
                    removeAt(size - 1)
                    displayFormula()
                }
            }
        }
    }

    private fun initClickColumns() {
        val container = view.columnContainerInFormula
        val elementList = formula.formulaElements
        columnList.forEach { column ->
            val textView = TextView(view.context).apply {
                padding = context.dpsToPixels(16)
                textColor = Color.WHITE
                layoutParams = LinearLayout.LayoutParams(wrapContent, matchParent)
                text = column.name
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

    private fun initClickOperation() {
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

            formula.formulaElements.add(Formula.FormulaElement().apply {
                type = OTHER
                value = op
            })
            displayFormula()
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
        val strBuilder = StringBuilder()
        formula.formulaElements.forEach { element ->
            if (element.type == COLUMN_ID) {
                columnList.forEach {
                    if (it.id == element.value.toLong()) {
                        strBuilder.append(it.name)
                    }
                }
            } else {
                var value = element.value
                if (value == "-")
                    value = subtractChar
                if (value == "*")
                    value = multiplyChar
                strBuilder.append(value)
            }
        }
        displayTextView.text = strBuilder
        displayTextView.textColor = Color.GRAY
    }


    fun getFormula(): Formula? {

        // заварушка лишняя для проверки подленности формулы
        // если нет то загорится красным
        val stringBuilder = StringBuilder()

        formula.formulaElements.forEach {
            when (it.type) {
                COLUMN_ID -> {
                    stringBuilder.append(12.345)
                }
                OTHER -> {
                    stringBuilder.append(it.value)
                }
            }
        }
        val calc = Calc()

        return try {
            calc.evaluate(stringBuilder.toString())
            formula
        } catch (exception: Exception) {
            null
        }
    }

    fun errorFormula() {
        displayTextView.textColor = Color.RED
    }
}


fun formulaDialogShow(
    context: Context,
    columns: MutableList<NumberColumn>,
    positiveClick: (Formula) -> Unit
) {
    val inflate = View.inflate(context, R.layout.formula_layout, null)
    val formulaLayout = FormulaLayout(inflate, columns)

    val dialog = context.alertDialog {
        setCustomTitle(TextView(context).apply {
            padding = context.dpsToPixels(18)
            textSize = 18F
            text = "Введите формулу"
            textColor = Color.WHITE
        })
        setView(inflate)
        setPositiveButton("OK", null)
        setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
    }
    dialog.show()
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
        textColor = Color.WHITE
        setOnClickListener {
            formulaLayout.getFormula()?.let {
                positiveClick(it)
                dialog.dismiss()
            } ?: formulaLayout.errorFormula()
        }

    }
    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
        textColor = Color.WHITE
    }
    dialog.window?.setBackgroundDrawable(ColorDrawable(context.getColorFromRes(R.color.cent)))
}