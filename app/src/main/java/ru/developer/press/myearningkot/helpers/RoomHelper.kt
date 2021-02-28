package ru.developer.press.myearningkot.helpers

import android.content.Context
import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import androidx.room.Database
import com.google.gson.Gson
import org.jetbrains.anko.doAsyncResult
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.model.Card
import ru.developer.press.myearningkot.model.ColumnType
import ru.developer.press.myearningkot.model.Formula
import ru.developer.press.myearningkot.model.NumberColumn


@Entity(tableName = "Card")
open class CardJson {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var json = ""
}

@Entity(tableName = "Sample")
class SampleJson : CardJson()

@Entity(tableName = "ListType")
class ListTypeJson {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var json: String = ""
}

@Entity
class Page {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L
    var pageName = ""
    var background = Color.parseColor("#161719")

    @Ignore
    val cards = mutableListOf<MutableLiveData<Card>>()
}


@Database(
    entities = [SampleJson::class, CardJson::class, Page::class, ListTypeJson::class],
    version = 1
)
abstract class Database : RoomDatabase() {
    companion object {
        fun create(context: Context): ru.developer.press.myearningkot.helpers.Database =
            Room.databaseBuilder(
                context,
                ru.developer.press.myearningkot.helpers.Database::class.java,
                "Database.db"
            ).build()

    }

    abstract fun sampleJsonDao(): SampleJsonDao
    abstract fun cardJsonDao(): CardJsonDao
    abstract fun pageDao(): PageDao
    abstract fun listTypeDao(): ListTypeDao
}

@Dao
interface ListTypeDao {
    @Query("SELECT * FROM ListType")
    fun getAll(): MutableList<ListTypeJson>

    @Query("Select * FROM ListType where id = :id")
    fun getById(id: Long): ListTypeJson

    @Delete
    fun delete(listType: ListTypeJson)

    @Insert
    fun insert(listType: ListTypeJson): Long

    @Update
    fun update(listType: ListTypeJson)
}

@Dao
interface SampleJsonDao {

    @Query("SELECT * FROM Sample")
    fun getAll(): MutableList<SampleJson>

    @Query("Select * FROM Sample where id = :id")
    fun getById(id: Long): SampleJson

    @Delete
    fun delete(sampleJson: SampleJson)

    @Insert
    fun insert(sampleJson: SampleJson)

    @Update
    fun update(sampleJson: SampleJson)

    @Query("DELETE FROM Sample WHERE id = :deleteId")
    fun delete(deleteId: Long)
}

@Dao
interface CardJsonDao {

    @Query("SELECT * FROM Card")
    fun getAll(): MutableList<CardJson>

    @Query("Select * FROM Card where id = :id")
    fun getById(id: Long): CardJson

    @Delete
    fun delete(cardJson: CardJson)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cardJson: CardJson): Long

    @Update
    fun update(cardJson: CardJson)
}

@Dao
interface PageDao {

    @Query("SELECT * FROM Page")
    fun getAll(): MutableList<Page>

    @Query("Select * FROM Page where id = :id")
    fun getById(id: Long): Page

    @Delete
    fun delete(page: Page)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(page: Page): Long

    @Update
    fun update(page: Page)
}


//
class SampleHelper(context: Context) {
    private val sampleJsonDao = ru.developer.press.myearningkot.helpers.Database.create(context).sampleJsonDao()
    fun getSampleList(): MutableList<Card> {
        return doAsyncResult {
            val sampleJsonLst = sampleJsonDao.getAll()
            mutableListOf<Card>().apply {
                sampleJsonLst.forEach {
                    val card: Card = getCardFromJson(it.json).apply {
                        id = it.id
                        updateTypeControl()
                    }
                    this.add(card)
                }
            }
        }.get()
    }

    fun addSample(card: Card): MutableList<Card> {
        return doAsyncResult {
            sampleJsonDao.insert(SampleJson().apply {
                json = Gson().toJson(card.apply {
                    repeat(30) {
                        addSampleRow()
                    }
                })
            })
            getSampleList()
        }.get()
    }

    fun updateSample(card: Card) {
        val jsonSample = Gson().toJson(card)
        sampleJsonDao.update(SampleJson().apply {
            id = card.id
            json = jsonSample
        })
    }

    fun getSample(id: Long): Card {
        val sampleJson: SampleJson = sampleJsonDao.getById(id)
        return getCardFromJson(sampleJson.json).apply {
            this.id = sampleJson.id
            updateTypeControl()
        }
    }

    fun addDefaultSamples(context: Context) {
        // доход
        addSample(Card(name = context.getString(R.string.earning)).apply {
            //                                    deleteColumn()
            val summaColumn =
                addColumn(ColumnType.NUMBER, context.getString(R.string.summa))

            val avansColumn =
                addColumn(ColumnType.NUMBER, context.getString(R.string.avans))

            addColumn(ColumnType.TEXT, context.getString(R.string.note)).apply {
                width = 450
            }
            addColumn(ColumnType.DATE, context.getString(R.string.date)).apply {
                width = 430
            }

            val summaTotal = totals[0]
            summaTotal.apply {
                title = context.getString(R.string.SUMMA)
                formula = Formula().apply {
                    formulaElements.add(
                        Formula.FormulaElement(
                            Formula.COLUMN_ID,
                            summaColumn.id.toString()
                        )
                    )
                }
            }
            val avansTotal = addTotal().apply {
                title = context.getString(R.string.AVANS)
                formula = Formula().apply {
                    formulaElements.add(
                        Formula.FormulaElement(
                            Formula.COLUMN_ID,
                            avansColumn.id.toString()
                        )
                    )
                }
            }
            addTotal().apply {
                title = context.getString(R.string.RESIDUE)
                formula = Formula().apply {
                    formulaElements.add(
                        Formula.FormulaElement(
                            Formula.TOTAL_ID,
                            summaTotal.id.toString()
                        )
                    )
                    formulaElements.add(
                        Formula.FormulaElement(
                            Formula.OTHER,
                            "-"
                        )
                    )
                    formulaElements.add(
                        Formula.FormulaElement(
                            Formula.TOTAL_ID,
                            avansTotal.id.toString()
                        )
                    )
                }
            }
            enableSomeStroke = false
        })
        // расход
        addSample(Card(name = context.getString(R.string.expenses)).apply {
            deleteColumn()

            val summaColumn =
                addColumn(
                    ColumnType.NUMBER,
                    context.getString(R.string.budget)
                ) as NumberColumn

            val avansColumn =
                addColumn(
                    ColumnType.NUMBER,
                    context.getString(R.string.expenses)
                ) as NumberColumn

            addColumn(ColumnType.LIST, context.getString(R.string.category)).apply {
                width = 430
            }
            addColumn(ColumnType.TEXT, context.getString(R.string.note)).apply {
                width = 450
            }
            addColumn(ColumnType.DATE, context.getString(R.string.date)).apply {
                width = 430
            }
            val summaTotal = totals[0]
            summaTotal.apply {
                title = context.getString(R.string.BUDGET)
                formula = Formula().apply {
                    formulaElements.add(
                        Formula.FormulaElement(
                            Formula.COLUMN_ID,
                            summaColumn.id.toString()
                        )
                    )
                }
            }
            val avansTotal = addTotal().apply {
                title = context.getString(R.string.EXPENSES)
                formula = Formula().apply {
                    formulaElements.add(
                        Formula.FormulaElement(
                            Formula.COLUMN_ID,
                            avansColumn.id.toString()
                        )
                    )
                }
            }
            addTotal().apply {
                title = context.getString(R.string.RESIDUE)
                formula = Formula().apply {
                    formulaElements.add(
                        Formula.FormulaElement(
                            Formula.TOTAL_ID,
                            summaTotal.id.toString()
                        )
                    )
                    formulaElements.add(
                        Formula.FormulaElement(
                            Formula.OTHER,
                            "-"
                        )
                    )
                    formulaElements.add(
                        Formula.FormulaElement(
                            Formula.TOTAL_ID,
                            avansTotal.id.toString()
                        )
                    )
                }
            }
        })
    }

    fun deleteSample(deleteId: Long) {
        sampleJsonDao.delete(deleteId)
    }

}

// чтобы узнать мы открыли в настройках карточку или шаблон
class PrefCardInfo(
    var idCard: Long,
    var cardCategory: CardCategory
) {

    enum class CardCategory {
        CARD,
        SAMPLE
    }
}
