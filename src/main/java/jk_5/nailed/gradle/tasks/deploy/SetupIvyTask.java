package jk_5.nailed.gradle.tasks.deploy;

import jk_5.nailed.gradle.deploy.DeployExtension;
import jk_5.nailed.gradle.json.deploy.Library;
import org.gradle.api.tasks.TaskAction;

/**
 * No description given
 *
 * @author jk-5
 */
public class SetupIvyTask extends SetupTask {

    @TaskAction
    public void doTask(){
        DeployExtension ext = DeployExtension.getInstance(this.getProject());
        Library library = new Library();
        library.name = ext.getName();
        library.restart = ext.getRestart();

        library.location = ext.getUrl().replace("{VERSION}", ext.getVersion());
        ext.setArtifact(ext.getArtifact().replace("{VERSION}", ext.getVersion()));

        String extensionParts[] = ext.getArtifact().split("@");
        String extension = "jar";
        if(extensionParts.length == 2){
            extension = extensionParts[1];
        }
        String parts[] = extensionParts[0].split(":");
        String parsed = parts[0].replace(".", "/") + "/" + parts[1] + "/" + parts[2];
        library.destination = "{MC_LIB_DIR}/" + parsed + "/" + parts[1] + "-" + parts[2] + "." + extension;

        this.registerLibrary(library);
    }
}
