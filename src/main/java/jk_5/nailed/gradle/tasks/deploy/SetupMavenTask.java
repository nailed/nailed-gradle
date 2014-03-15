package jk_5.nailed.gradle.tasks.deploy;

import jk_5.nailed.gradle.deploy.DeployExtension;
import jk_5.nailed.gradle.json.deploy.Library;
import org.gradle.api.tasks.TaskAction;

/**
 * No description given
 *
 * @author jk-5
 */
public class SetupMavenTask extends SetupTask {

    @TaskAction
    public void doTask(){
        DeployExtension ext = DeployExtension.getInstance(this.getProject());
        Library library = new Library();
        library.name = ext.getName();
        library.server = ext.getUrl();
        library.restart = ext.getRestart();

        String extensionParts[] = ext.getArtifact().split("@");
        String extension = "jar";
        if(extensionParts.length == 2){
            extension = extensionParts[1];
        }
        String parts[] = extensionParts[0].split(":");
        String parsed = parts[0].replace(".", "/") + "/" + parts[1] + "/" + parts[2];
        if(parts.length == 4){
            //We have a classifier. Do something with it
            library.location = parsed + "/" + parts[1] + "-" + parts[2] + "-" + parts[3] + "." + extension;
        }else{
            library.location = parsed + "/" + parts[1] + "-" + parts[2] + "." + extension;
        }
        //The minecraft launcher doesn't support classifiers, so just leave them out.
        library.destination = "{MC_LIB_DIR}/" + parsed + "/" + parts[1] + "-" + parts[2] + "." + extension;

        this.registerLibrary(library);
    }
}
