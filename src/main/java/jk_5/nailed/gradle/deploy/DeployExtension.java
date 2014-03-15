package jk_5.nailed.gradle.deploy;

import jk_5.nailed.gradle.json.deploy.RestartLevel;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;

/**
 * No description given
 *
 * @author jk-5
 */
public class DeployExtension {

    public static DeployExtension getInstance(Project project){
        return project.getExtensions().getByType(DeployExtension.class);
    }

    private final Project project;

    /**
     * Type can be "maven", "remote"
     */
    @Getter @Setter private String type;
    @Getter @Setter private String url = null;
    @Getter @Setter private String artifact = null;
    @Getter @Setter private String version = null;
    @Getter @Setter private String name;
    @Getter @Setter private RestartLevel restart = RestartLevel.NOTHING;
    /**
     * Update the launcher profile when we update?
     */
    @Getter @Setter private boolean updateProfile = false;
    /**
     * Let the updater check for changes even when the version stays the same.
     */
    @Getter @Setter private boolean versionUpdates = true;

    public DeployExtension(Project project){
        this.project = project;
        this.name = project.getName();
    }
}
