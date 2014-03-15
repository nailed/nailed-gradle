package jk_5.nailed.gradle.tasks.deploy;

import jk_5.nailed.gradle.deploy.DeployTask;
import jk_5.nailed.gradle.json.deploy.Library;
import lombok.Setter;

/**
 * No description given
 *
 * @author jk-5
 */
public abstract class SetupTask extends DeployTask {

    @Setter private UpdateRemoteLibraryListTask updateRemoteLibraryListTask;

    public void registerLibrary(Library library){
        this.updateRemoteLibraryListTask.addLibrary(library);
    }
}
