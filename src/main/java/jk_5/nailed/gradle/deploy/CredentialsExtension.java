package jk_5.nailed.gradle.deploy;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;

/**
 * No description given
 *
 * @author jk-5
 */
public class CredentialsExtension {

    public static CredentialsExtension getInstance(Project project){
        return project.getExtensions().getByType(CredentialsExtension.class);
    }

    @Getter @Setter private String deployServer = null;
    @Getter @Setter private String deployUsername = null;
    @Getter @Setter private String deployPassword = null;
    @Getter @Setter private String versionFile = null;
}
