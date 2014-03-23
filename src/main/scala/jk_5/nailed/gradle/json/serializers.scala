package jk_5.nailed.gradle.json

import com.google.gson._
import com.google.gson.reflect.TypeToken
import com.google.common.collect.Maps
import java.util
import com.google.gson.stream.{JsonToken, JsonWriter, JsonReader}
import java.util.{Locale, Date}
import java.lang.reflect.Type
import java.text.{ParseException, SimpleDateFormat, DateFormat}

/**
 * No description given
 *
 * @author jk-5
 */
object Serialization {
  val gson = new GsonBuilder()
    .registerTypeAdapterFactory(new EnumAdapterFactory)
    .registerTypeAdapter(classOf[Date], new DateAdapter)
    .enableComplexMapKeySerialization()
    .setPrettyPrinting()
    .create()
}

class EnumAdapterFactory extends TypeAdapterFactory {

  override def create[T](gson: Gson, typ: TypeToken[T]): TypeAdapter[T] = {
    if(!typ.getRawType.isEnum) return null

    val map: util.Map[String, T] = Maps.newHashMap()
    typ.getRawType.getEnumConstants.foreach(c => {
      map.put(c.toString.toLowerCase, c.asInstanceOf[T])
    })

    new TypeAdapter[T]{
      override def read(reader: JsonReader): T = {
        if(reader.peek() == JsonToken.NULL){
          reader.nextNull()
          return null.asInstanceOf[T]
        }
        val name = reader.nextString()
        if(name == null) return null.asInstanceOf[T]
        map.get(name.toLowerCase)
      }

      override def write(writer: JsonWriter, value: T){
        if(value == null){
          writer.nullValue()
        }else{
          writer.value(value.toString.toLowerCase)
        }
      }
    }
  }
}

class DateAdapter extends JsonDeserializer[Date] with JsonSerializer[Date] {

  private val enUsFormat = DateFormat.getDateTimeInstance(2, 2, Locale.ENGLISH)
  private val iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

  override def serialize(value: Date, typ: Type, context: JsonSerializationContext): JsonElement = {
    enUsFormat synchronized {
      val ret = this.iso8601Format.format(value)
      return new JsonPrimitive(ret.substring(0, 22) + ":" + ret.substring(22))
    }
  }

  override def deserialize(json: JsonElement, typ: Type, context: JsonDeserializationContext): Date = {
    if(!json.isInstanceOf[JsonPrimitive]){
      throw new JsonParseException("Date was not string: " + json)
    }
    if(typ != classOf[Date]){
      throw new IllegalArgumentException(this.getClass + " cannot deserialize to " + typ)
    }
    val value = json.getAsString
    enUsFormat synchronized{
      try{
        return enUsFormat.parse(value)
      }catch{
        case e: ParseException =>
          try{
            return iso8601Format.parse(value)
          }catch{
            case e2: ParseException =>
              try{
                val tmp = value.replace("Z", "+00:00")
                if(tmp.length < 22){
                  return new Date()
                }else{
                  return iso8601Format.parse(tmp.substring(0, 22) + tmp.substring(23))
                }
              }catch{
                case e3: ParseException => throw new JsonSyntaxException("Invalid date: " + value, e3)
              }
          }
      }
    }
  }
}
