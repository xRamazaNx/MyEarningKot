package ru.developer.press.myearningkot.model

import android.graphics.Color
import com.google.gson.annotations.SerializedName
import ru.developer.press.myearningkot.FormulaId
import ru.developer.press.myearningkot.database.BelongIds
import ru.developer.press.myearningkot.database.Card
import ru.developer.press.myearningkot.helpers.Calc
import ru.developer.press.myearningkot.helpers.getDecimalFormatNumber
import java.math.BigDecimal
import java.util.*
import kotlin.random.Random

class Total(pageId: String, cardId: String) : BelongIds(pageId, cardId), FormulaId {
    override var idToFormula: Long = Random.nextLong()

    @SerializedName("w")
    var width = 250

    @SerializedName("f")
    var formula: Formula = Formula()

    @SerializedName("t")
    var title: String = "ИТОГ"

    @SerializedName("v")
    var value: String = "0"

    @SerializedName("tip")
    var titlePref: PrefForTextView = PrefForTextView().apply {
        textSize = 14
        isBold = true
        color = Color.parseColor("#8c8d8f")
    }

    @SerializedName("top")
    var totalPref: NumberTypePref = NumberTypePref()

    @SerializedName("iisw")
    var isIgnoreSwitchWork: Boolean = false

    fun calcFormula(card: Card) {
        val stringBuilder = java.lang.StringBuilder()
        formula.formulaElements.forEach { element ->
            val elementVal = element.value
            if (element.type == Formula.COLUMN_ID) {
                if (card.columns.any { it.idToFormula.toString() == elementVal }) {
                    val sumFromColumn = card.getSumFromColumn(elementVal)
                    stringBuilder.append(sumFromColumn)
                } else {
                    formula.formulaElements.remove(element)
                    calcFormula(card)
                    return
                }
            } else if (element.type == Formula.TOTAL_ID) {
                val elementId = elementVal.toLong()
                if (card.totals.any { it.idToFormula == elementId }) {
                    val findTotal = card.totals.find { it.idToFormula == elementId }!!
                    findTotal.calcFormula(card)
                    stringBuilder.append(findTotal.value)
                } else {
                    formula.formulaElements.remove(element)
                    calcFormula(card)
                    return
                }
            } else
                stringBuilder.append(elementVal)
        }
        value = try {
            val d = Calc.evaluate(stringBuilder.toString())!!
            getDecimalFormatNumber(d, totalPref)

        } catch (exception: Exception) {

            "Error formula"
        }
    }

    private fun Card.getSumFromColumn(id: String): String {

        var index = -1
        var value = 0.0
        columns.forEachIndexed { i, column ->
            if (column.idToFormula.toString() == id) {
                index = i
                return@forEachIndexed
            }
        }
        // есть ли в карточке свитч колона
        val isAnySwitchColumn = columns.any { it is SwitchColumn }
        return if (index > -1) {
            rows.forEach { row ->
                var isAddSum = true
                if (isAnySwitchColumn)
                //  если в итоговой не включен игнор этой функции
                    if (!isIgnoreSwitchWork)
                    // проходим по колонам в основном для того что бы узнать индекс для доступа к ячейке
                        columns.forEachIndexed { columnIndex, column ->
                            // если нашли колону свитч
                            if (column is SwitchColumn) {
                                // в настройках включена опция "учитывать в итоговой панели"
                                if (column.typePref.behavior.control)
                                // то посмотрим переключатель на что установлен
                                    isAddSum = row.cellList[columnIndex].sourceValue.toBoolean()
                            }
                        }

                val cell = row.cellList[index]
                val source = cell.sourceValue
                // если надо прибавить и сорц не пустой
                if (isAddSum && source.isNotEmpty())
                    value += try {
                        Calc.evaluate(source)!!
                    } catch (exc: Exception) {
                        return "Error"
                    }
            }
            BigDecimal(value).toPlainString()
        } else
            "Error"
    }
}