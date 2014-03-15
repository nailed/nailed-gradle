package jk_5.nailed.gradle.json.deploy;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * No description given
 *
 * @author jk-5
 */
public class LibraryListSerializer implements JsonSerializer<LibraryList>, JsonDeserializer<LibraryList> {

    public static Gson serializer = new GsonBuilder().registerTypeAdapter(LibraryList.class, new LibraryListSerializer()).registerTypeAdapterFactory(new EnumAdapterFactory()).setPrettyPrinting().create();

    @Override
    public LibraryList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
        JsonObject obj = json.getAsJsonObject();
        LibraryList libraryList = new LibraryList();
        for(Map.Entry<String, JsonElement> e : obj.entrySet()){
            Library lib = context.deserialize(e.getValue(), Library.class);
            lib.name = e.getKey();
            if(lib.restart == null){
                lib.restart = RestartLevel.NOTHING;
            }
            libraryList.libraries.add(lib);
        }
        return libraryList;
    }

    @Override
    public JsonElement serialize(LibraryList src, Type typeOfSrc, JsonSerializationContext context){
        JsonObject ret = new JsonObject();
        for(Library lib : src.libraries){
            ret.add(lib.name, context.serialize(lib));
        }
        return ret;
    }
}
