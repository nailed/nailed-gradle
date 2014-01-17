package jk_5.nailed.gradle.extension;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;

import java.util.Arrays;
import java.util.List;

/**
 * No description given
 *
 * @author jk-5
 */
public class NailedExtension {

    public static NailedExtension getInstance(Project project){
        return project.getExtensions().getByType(NailedExtension.class);
    }

    private final Project project;

    @Getter @Setter private String minecraftVersion = null;
    @Getter @Setter private String forgeVersion = null;
    @Getter @Setter private String versionName = "Nailed";
    @Getter @Setter private String loadingMavenUrl = "http://maven.reening.nl/";
    @Getter @Setter private String mainClass = null;
    @Getter @Setter private String deployHost = null;
    @Getter @Setter private String deployUsername = null;
    @Getter @Setter private String deployPassword = null;
    @Getter @Setter private String remoteProfileDir = "";
    @Getter private List<String> tweakers = Lists.newArrayList();
    @Getter private List<JsonObject> launcherDependencies = Lists.newArrayList();

    public void setLauncherDependency(String launcherDependency){
        JsonObject obj = new JsonObject();
        obj.addProperty("name", launcherDependency);
        obj.addProperty("url", "{MAVEN_URL}");
        launcherDependencies.add(obj);
    }

    public void setTweakers(String... tweakers){
        this.tweakers = Arrays.asList(tweakers);
    }

    public NailedExtension(Project project){
        this.project = project;
    }
}
