package jk_5.nailed.gradle.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * No description given
 *
 * @author jk-5
 */
public class DateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

    private final DateFormat enUsFormat = DateFormat.getDateTimeInstance(2, 2, Locale.US);
    private final DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Override
    public JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext){
        synchronized(this.enUsFormat){
            String ret = this.iso8601Format.format(date);
            return new JsonPrimitive(ret.substring(0, 22) + ":" + ret.substring(22));
        }
    }

    @Override
    public Date deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException{
        if(!(jsonElement instanceof JsonPrimitive)){
            throw new JsonParseException("Date was not string: " + jsonElement);
        }
        if (type != Date.class){
            throw new IllegalArgumentException(getClass() + " cannot deserialize to " + type);
        }
        String value = jsonElement.getAsString();
        synchronized(this.enUsFormat) {
            try{
                return this.enUsFormat.parse(value);
            }catch (ParseException e){
                try{
                    return this.iso8601Format.parse(value);
                }catch (ParseException e2){
                    try{
                        String tmp = value.replace("Z", "+00:00");
                        if (tmp.length() < 22){
                            return new Date();
                        }else{
                            return this.iso8601Format.parse(tmp.substring(0, 22) + tmp.substring(23));
                        }
                    }catch (ParseException e3){
                        throw new JsonSyntaxException("Invalid date: " + value, e3);
                    }
                }
            }
        }
    }
}
