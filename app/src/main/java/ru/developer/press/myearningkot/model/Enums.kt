package ru.developer.press.myearningkot.model

enum class ColumnType {
    TEXT,
    NUMBER, /*
    разделение цифр
    колличество дробей
    */

    PHONE, /*
    выбор формата
    */
    DATE, /*
    выбор формата даты
    показать часы флаг
    */
    COLOR, /*
    выбор фигруы для цвета
    */
    SWITCH, /*
    в принципе это тоже будет текст только в конце будет флажок типа выполнено
    */
    IMAGE, /*
    ну что тут можно сделать я хз
    */
    LIST,/*
    управление списком, добавить элемент в список или удалить
    а если удалит то что делать? получается надо не номер списка хранить а сам элемент
    или когда с элементом производят действия надо пройтись везде и посмотреть где используется и принять меры
    */
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
    add("Список")
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
//
//enum class DateType {
//    DD_MM_YY,
//    DD_MM_YYYY,
//    DD_MMMM_YYYY,
//    DAY_DD_MM_YY,
//    DAY_DD_MM_YYYY,
//    DAY_DD_MMMM_YYYY
//}

enum class SetNameOf {
    PAGE,
    OTHER
}

enum class InputTypeNumberColumn{
    MANUAL, FORMULA
}