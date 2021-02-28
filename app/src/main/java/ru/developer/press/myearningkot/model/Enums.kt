package ru.developer.press.myearningkot.model

enum class ColumnType {
    TEXT,
    NUMBER,
    PHONE,
    DATE,
    COLOR,
    SWITCH,
    IMAGE,
    LIST,
    NUMERATION,
    NONE
}

fun getColumnTypeList(): MutableList<String> = mutableListOf<String>().apply {
    add("Число")
    add("Текст")
    add("Контакт")
    add("Дата")
    add("Цвет")
    add("Переключатель")
    add("Изображение")
//    add("Список")
}
fun getColumnTypeEnumList(): MutableList<ColumnType> = mutableListOf<ColumnType>().apply {
    add(ColumnType.NUMBER)
    add(ColumnType.TEXT)
    add(ColumnType.PHONE)
    add(ColumnType.DATE)
    add(ColumnType.COLOR)
    add(ColumnType.SWITCH)
    add(ColumnType.IMAGE)
    add(ColumnType.LIST)
}


enum class SortMethod {

    UP,
    DOWN
}
enum class InputTypeNumberColumn{
    MANUAL, FORMULA
}