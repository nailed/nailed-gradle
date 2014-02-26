package jk_5.nailed.gradle.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.IOException;

/**
 * No description given
 *
 * @author jk-5
 */
public class FileAdapter extends TypeAdapter<File> {

    @Override
    public void write(JsonWriter jsonWriter, File file) throws IOException{
        if(file == null){
            jsonWriter.nullValue();
        }else{
            jsonWriter.value(file.getCanonicalPath());
        }
    }

    @Override
    public File read(JsonReader jsonReader) throws IOException{
        if(jsonReader.hasNext()){
            String value = jsonReader.nextString();
            return value == null ? null : new File(value);
        }
        return null;
    }
}
