package jk_5.nailed.gradle.tasks.deploy;

import org.gradle.api.tasks.TaskAction;

/**
 * No description given
 *
 * @author jk-5
 */
public class SetupMavenTask extends SetupTask {

    @TaskAction
    public void doTask(){
        this.getProject().getLogger().lifecycle("Setting up maven project...");
    }
}
