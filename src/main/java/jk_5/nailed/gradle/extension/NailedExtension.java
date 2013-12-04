package jk_5.nailed.gradle.extension;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;

import java.io.File;

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

    @Getter
    @Setter
    private String minecraftVersion = null;

    public NailedExtension(Project project){
        this.project = project;
    }
}
