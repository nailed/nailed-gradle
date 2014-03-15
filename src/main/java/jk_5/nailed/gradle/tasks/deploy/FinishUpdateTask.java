package jk_5.nailed.gradle.tasks.deploy;

import jk_5.nailed.gradle.SshConnectionPool;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

/**
 * No description given
 *
 * @author jk-5
 */
public class FinishUpdateTask extends DefaultTask {

    @TaskAction
    public void doTask(){
        SshConnectionPool.close();
    }
}
