package ru.developer.press.myearningkot.database

import android.content.Context
import androidx.room.*
import androidx.room.Database
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.helpers.getColumnFromJson
import ru.developer.press.myearningkot.helpers.scoups.addColumn
import ru.developer.press.myearningkot.helpers.scoups.addTotal
import ru.developer.press.myearningkot.model.*

class Converter {
    @TypeConverter
    fun toSortMethod(enum: String) = enumValueOf<SortMethod>(enum)

    @TypeConverter
    fun fromSortMethod(sortMethod: SortMethod) = sortMethod.name
}

@Database(
    entities = [
        Page::class,
        Card::class,
        ColumnJson::class,
        RowJson::class,
        TotalJson::class,
        ListTypeJson::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converter::class)
abstract class Database : RoomDatabase() {
    companion object {
        private var database: ru.developer.press.myearningkot.database.Database? = null
        fun create(context: Context): ru.developer.press.myearningkot.database.Database {
            if (database == null)
                database = Room.databaseBuilder(
                    context,
                    ru.developer.press.myearningkot.database.Database::class.java,
                    "Database.db"
                ).build()
            return database!!
        }
    }

    abstract fun sampleDao(): SampleDao
    abstract fun cardDao(): CardDao
    abstract fun pageDao(): PageDao
    abstract fun listTypeDao(): ListTypeDao
    abstract fun rowDao(): RowDao
    abstract fun columnDao(): ColumnDao
    abstract fun totalDao(): TotalDao
}

@Dao
interface TotalDao {
    @Query("Select * FROM TotalJson where cardId = :id")
    fun getAllOf(id: String): List<TotalJson>

    @Query("Select * FROM TotalJson where refId = :id")
    fun getById(id: String): TotalJson?

    @Delete
    fun delete(totalRef: TotalJson)

    @Query("delete from TotalJson where refId = :refId")
    fun delete(refId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(totalRef: TotalJson)

    @Update
    fun update(totalRef: TotalJson)
}

@Dao
interface ColumnDao {

    @Query("Select * FROM ColumnJson where refId = :id")
    fun getById(id: String): ColumnJson?

    @Delete
    fun delete(columnRef: ColumnJson)

    @Query("delete from ColumnJson where refId = :refId")
    fun delete(refId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(columnRef: ColumnJson)

    @Update
    fun update(columnRef: ColumnJson)

    @Query("Select * FROM ColumnJson where cardId = :id")
    fun getAllOf(id: String): List<ColumnJson>
}

@Dao
interface RowDao {

    @Query("SELECT * FROM RowJson where cardId = :id")
    fun getAllOf(id: String): List<RowJson>

    @Query("Select * FROM RowJson where refId = :id")
    fun getById(id: String): RowJson?

    @Delete
    fun delete(jsonValue: RowJson)

    @Query("delete from RowJson where refId = :refId")
    fun delete(refId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(jsonValue: RowJson)

    @Update
    fun update(jsonValue: RowJson)
}

@Dao
interface ListTypeDao {
    @Query("SELECT * FROM ListTypeJson")
    fun getAll(): List<ListTypeJson>

    @Query("Select * FROM ListTypeJson where refId = :id")
    fun getById(id: String): ListTypeJson?

    @Delete
    fun delete(listType: ListTypeJson)

    @Insert
    fun insert(listType: ListTypeJson)

    @Update
    fun update(listType: ListTypeJson)
}

@Dao
interface SampleDao {

    @Query("SELECT * FROM Card where pageId = :pageRefId")
    fun getAll(pageRefId: String = samplePageName): List<Card>

    @Query("Select * FROM Card where refId = :id")
    fun getByRefId(id: String): Card?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sample: Card)

    @Update
    fun update(sample: Card)

    @Query("DELETE FROM Card WHERE refId = :deleteId")
    fun delete(deleteId: String)
}

@Dao
interface CardDao {

    @Query("SELECT * FROM Card where pageId = :id")
    fun getAllOf(id: String): List<Card>

    @Query("Select * FROM Card where refId = :id")
    fun getById(id: String): Card?

    @Query("DELETE FROM Card WHERE refId = :deleteId")
    fun delete(deleteId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(card: Card)

    @Update
    fun update(card: Card)
}

@Dao
interface PageDao {

    @Query("SELECT * FROM Page")
    fun getAll(): List<Page>

    @Query("Select * FROM Page where refId = :id")
    fun getById(id: String): Page?

    @Query("DELETE FROM Page WHERE refId = :deleteId")
    fun delete(deleteId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(page: Page)

    @Update
    fun update(page: Page)
}

fun convertRefToColumn(refs: List<JsonValue>): List<Column> {
    return refs.fold(mutableListOf()) { list, columnRef ->
        list.add(getColumnFromJson(columnRef))
        list
    }
}

//
const val samplePageName = "|/*sample_cards*/|"

class SampleHelper {

    companion object {

        fun defaultSamples(context: Context): List<Card> {
            val list = mutableListOf<Card>()
            // доход
            list.add(Card(samplePageName, name = context.getString(R.string.earning)).apply {
                addColumn(ColumnType.NUMERATION, "№").apply {
                    width = 70
                }

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

                val summaTotal = addTotal().apply {
                    title = context.getString(R.string.SUMMA)
                    formula = Formula().apply {
                        formulaElements.add(
                            Formula.FormulaElement(
                                Formula.COLUMN_ID,
                                summaColumn.idToFormula.toString()
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
                                avansColumn.idToFormula.toString()
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
                                summaTotal.idToFormula.toString()
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
                                avansTotal.idToFormula.toString()
                            )
                        )
                    }
                }
                enableSomeStroke = false
            })
            // расход
            list.add(Card(samplePageName, name = context.getString(R.string.expenses)).apply {
                addColumn(ColumnType.NUMERATION, "№").apply {
                    width = 70
                }
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
                val summaTotal = addTotal().apply {
                    title = context.getString(R.string.BUDGET)
                    formula = Formula().apply {
                        formulaElements.add(
                            Formula.FormulaElement(
                                Formula.COLUMN_ID,
                                summaColumn.idToFormula.toString()
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
                                avansColumn.idToFormula.toString()
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
                                summaTotal.idToFormula.toString()
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
                                avansTotal.idToFormula.toString()
                            )
                        )
                    }
                }
            })
            return list
        }
    }

}
