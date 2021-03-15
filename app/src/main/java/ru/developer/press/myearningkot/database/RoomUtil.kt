package ru.developer.press.myearningkot.database

import android.content.Context
import androidx.room.*
import androidx.room.Database
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.helpers.getColumnFromJson
import ru.developer.press.myearningkot.helpers.scoups.addColumn
import ru.developer.press.myearningkot.helpers.scoups.addTotal
import ru.developer.press.myearningkot.helpers.scoups.deleteColumn
import ru.developer.press.myearningkot.model.Column
import ru.developer.press.myearningkot.model.ColumnType
import ru.developer.press.myearningkot.model.Formula
import ru.developer.press.myearningkot.model.NumberColumn


@Database(
    entities = [
        PageRef::class,
        CardRef::class,
        RowRef::class,
        ColumnRef::class,
        ListTypeJson::class],
    version = 1,
    exportSchema = false
)
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
    @Query("Select * FROM TotalRef where cardId = :id")
    fun getAllOf(id: String): List<TotalRef>

    @Query("Select * FROM TotalRef where refId = :id")
    fun getById(id: String): TotalRef

    @Delete
    fun delete(totalRef: TotalRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(totalRef: TotalRef): String

    @Update
    fun update(totalRef: TotalRef)
}

@Dao
interface ColumnDao {

    @Query("Select * FROM ColumnRef where refId = :id")
    fun getById(id: String): ColumnRef

    @Delete
    fun delete(columnRef: ColumnRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(columnRef: ColumnRef): String

    @Update
    fun update(columnRef: ColumnRef)

    @Query("Select * FROM ColumnRef where cardId = :id")
    fun getAllOf(id: String): List<ColumnRef>
}

@Dao
interface RowDao {

    @Query("SELECT * FROM RowRef where cardId = :id")
    fun getAllOf(id: String): List<RowRef>

    @Query("Select * FROM RowRef where refId = :id")
    fun getById(id: String): RowRef

    @Delete
    fun delete(rowRef: RowRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rowRef: RowRef): String

    @Update
    fun update(rowRef: RowRef)
}

@Dao
interface ListTypeDao {
    @Query("SELECT * FROM ListTypeJson")
    fun getAll(): List<ListTypeJson>

    @Query("Select * FROM ListTypeJson where refId = :id")
    fun getById(id: String): ListTypeJson

    @Delete
    fun delete(listType: ListTypeJson)

    @Insert
    fun insert(listType: ListTypeJson): String

    @Update
    fun update(listType: ListTypeJson)
}

@Dao
interface SampleDao {

    @Query("SELECT * FROM CardRef")
    fun getAll(): List<CardRef>

    @Query("Select * FROM CardRef where refId = :id")
    fun getByRefId(id: String): CardRef

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sample: CardRef): String

    @Update
    fun update(sample: CardRef)

    @Query("DELETE FROM CardRef WHERE refId = :deleteId")
    fun delete(deleteId: String)
}

@Dao
interface CardDao {

    @Query("SELECT * FROM CardRef where pageId = :id")
    fun getAllOf(id: String): List<CardRef>

    @Query("Select * FROM CardRef where refId = :id")
    fun getById(id: String): CardRef

    @Query("DELETE FROM CardRef WHERE refId = :deleteId")
    fun delete(deleteId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cardJson: CardRef): String

    @Update
    fun update(cardJson: CardRef)
}

@Dao
interface PageDao {

    @Query("SELECT * FROM PageRef")
    fun getAll(): List<PageRef>

    @Query("Select * FROM PageRef where refId = :id")
    fun getById(id: String): PageRef

    @Query("DELETE FROM PageRef WHERE refId = :deleteId")
    fun delete(deleteId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(page: PageRef): String

    @Update
    fun update(page: PageRef)
}

fun convertRefToColumn(refs: List<ColumnRef>): List<Column> {
    return refs.fold(mutableListOf()) { list, columnRef ->
        list.add(getColumnFromJson(columnRef))
        list
    }
}
//
class SampleHelper {

    companion object {

        fun defaultSamples(context: Context) :List<CardRef> {
            val samplePageName = "|/*sample_cards*/|"
            val list = mutableListOf<CardRef>()
            // доход
            list.add(CardRef(samplePageName, name = context.getString(R.string.earning)).apply {
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
                                summaColumn.refId
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
                                avansColumn.refId
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
            list.add(CardRef(samplePageName,name = context.getString(R.string.expenses)).apply {
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
                                summaColumn.refId
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
                                avansColumn.refId
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
            return list
        }
    }

}
