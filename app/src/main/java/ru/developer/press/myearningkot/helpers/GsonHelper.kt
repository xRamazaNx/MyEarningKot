package ru.developer.press.myearningkot.helpers

import com.google.gson.*
import com.google.gson.JsonParseException
import ru.developer.press.myearningkot.database.JsonValue
import ru.developer.press.myearningkot.model.Column
import java.lang.reflect.Type


const val column_cast_gson = "col_cast_gson"
const val column_type_gson = "col_type_gson"

class JsonDeserializerWithColumn : JsonDeserializer<Column> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext
    ): Column {
        val jsonObject = json.asJsonObject
        val classNamePrimitive = jsonObject[column_cast_gson] as JsonPrimitive
        val className = classNamePrimitive.asString
        val clazz: Class<*> = try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            throw JsonParseException(e.message)
        }
        return context.deserialize(jsonObject, clazz)
    }
}
fun getColumnFromJson(columnRef: JsonValue): Column {
    val gson = GsonBuilder().registerTypeAdapter(
        Column::class.java,
        JsonDeserializerWithColumn()
    ).create()
    return gson.fromJson(columnRef.json, Column::class.java)
}