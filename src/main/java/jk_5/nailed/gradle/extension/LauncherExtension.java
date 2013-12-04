package jk_5.nailed.gradle.extension;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import groovy.lang.Closure;
import jk_5.nailed.gradle.Constants;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.artifacts.configurations.DefaultConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 * No description given
 *
 * @author jk-5
 */
public class LauncherExtension {

    public static LauncherExtension getInstance(Project project){
        return project.getExtensions().getByType(LauncherExtension.class);
    }

    private final Project project;

    @Getter
    @Setter
    private String versionName = "Nailed";

    @Getter
    @Setter
    private String loadingMavenUrl = "http://maven.reening.nl/";

    @Getter
    @Setter
    private String mainClass = null;

    @Getter
    @Setter
    private String deployHost = null;

    @Getter
    @Setter
    private String deployUsername = null;

    @Getter
    @Setter
    private String deployPassword = null;

    @Getter
    @Setter
    private String remoteProfileDir = "";

    @Getter
    private List<String> tweakers = Lists.newArrayList();

    @Getter
    private List<JsonObject> loadingDependencies = Lists.newArrayList();

    public LauncherExtension(Project project){
        this.project = project;
    }

    public void setLoadingDependencies(DefaultConfiguration configuration){
        for(Dependency dep : configuration.getAllDependencies()){
            JsonObject obj = new JsonObject();
            obj.addProperty("name", dep.getGroup() + ":" + dep.getName() + ":" + dep.getVersion());
            obj.addProperty("url", "{MAVEN_URL}");
            loadingDependencies.add(obj);
        }
    }

    public void setTweakers(String... tweakers){
        this.tweakers = Arrays.asList(tweakers);
    }
}
