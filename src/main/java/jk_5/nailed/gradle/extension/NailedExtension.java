package jk_5.nailed.gradle.extension;

import com.google.common.collect.Lists;
import jk_5.nailed.gradle.delayed.DelayedString;
import jk_5.nailed.gradle.tasks.CreateLauncherProfileTask;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;

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
    @Getter private List<Project> deployedProjects = Lists.newArrayList();

    public void setLauncherDependency(String dep){
        ((CreateLauncherProfileTask) this.project.getTasks().getByName("createLauncherProfile")).addDependency(new DelayedString(this.project, dep));
    }

    public void setDeployedArtifact(String artifact){
        for(Project p : this.project.getSubprojects()){
            if(p.getName().equals(artifact)){
                this.deployedProjects.add(p);
            }
        }
    }

    public void setTweaker(String tweaker){
        this.tweakers.add(tweaker);
    }

    public NailedExtension(Project project){
        this.project = project;
    }
}
