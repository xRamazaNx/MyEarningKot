package ru.developer.press.myearningkot.helpers

import ru.developer.press.myearningkot.database.Card


sealed class Search

class TitleSearch(val type: TitleSearchType) : Search() {
    companion object {
        enum class TitleSearchType(title: String) {
            CARD("КАРТОЧКИ"),
            COLUMN("КОЛОНЫ"),
            TOTAL("ИТОГИ"),
            ITEM("ЗАПИСИ")
        }
    }
}

open class CardSearch(val card: Card) : Search()

class ItemSearch(val card: Card, val position: Int, val columnID: Long) : Search()

class TotalSearch(val card: Card, val totalID: Long, val type: TotalSearchType) : Search() {
    companion object {
        enum class TotalSearchType {
            TITLE, VALUE
        }
    }
}
