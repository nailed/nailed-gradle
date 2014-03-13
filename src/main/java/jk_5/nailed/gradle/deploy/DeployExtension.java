package jk_5.nailed.gradle.deploy;

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

    public DeployExtension(Project project){
        this.project = project;
    }
}
