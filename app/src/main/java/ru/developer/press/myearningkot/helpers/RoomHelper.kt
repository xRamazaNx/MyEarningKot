package ru.developer.press.myearningkot.helpers

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import androidx.room.Database
import com.google.gson.Gson
import org.jetbrains.anko.doAsyncResult
import ru.developer.press.myearningkot.App
import ru.developer.press.myearningkot.model.Card


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
    var background = Color.WHITE

    @Ignore val cards = mutableListOf<MutableLiveData<Card>>()
}


@Database(entities = [SampleJson::class, CardJson::class, Page::class, ListTypeJson::class], version = 1)
abstract class Database : RoomDatabase() {
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
    fun insert(listType: ListTypeJson) : Long

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
class SampleHelper {
    private val sampleJsonDao = App.instance?.database!!.sampleJsonDao()
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

    fun getSample(id: Long): Card? {
        val sampleJson = doAsyncResult {
            sampleJsonDao.getById(id)
        }.get()
        return getCardFromJson(sampleJson.json).apply {
            this.id = sampleJson.id
            updateTypeControl()
        }
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
