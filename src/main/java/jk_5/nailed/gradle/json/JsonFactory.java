package jk_5.nailed.gradle.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import jk_5.nailed.gradle.json.dependencies.DependencyFile;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

/**
 * No description given
 *
 * @author jk-5
 */
public class JsonFactory {

    public static final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new EnumAdapterFactory()).registerTypeAdapter(Date.class, new DateAdapter()).registerTypeAdapter(File.class, new FileAdapter()).enableComplexMapKeySerialization().setPrettyPrinting().create();

    public static DependencyFile loadDependencyFile(File file) throws JsonSyntaxException, JsonIOException, IOException {
        FileReader reader = new FileReader(file);
        DependencyFile v = gson.fromJson(reader, DependencyFile.class);
        reader.close();
        return v;
    }
}
