package jk_5.nailed.gradle.deploy;

import com.google.common.collect.Maps;
import jk_5.nailed.gradle.common.BasePlugin;
import jk_5.nailed.gradle.tasks.deploy.*;
import org.gradle.api.Action;
import org.gradle.api.Project;

import java.util.Map;

/**
 * No description given
 *
 * @author jk-5
 */
public class DeployPlugin extends BasePlugin {

    private static final Map<String, Class<? extends SetupTask>> types = Maps.newHashMap();

    static {
        types.put("maven", SetupMavenTask.class);
        types.put("ivy", SetupIvyTask.class);
    }

    @Override
    public void applyPlugin(){
        this.getProject().subprojects(new Action<Project>() {
            @Override
            public void execute(Project project){
                throw new RuntimeException("Subproject " + project.getName() + " detected. nailed-deploy does not support this!");
            }
        });

        this.getProject().getExtensions().create("nailedDeploy", DeployExtension.class, this.getProject());
        this.getProject().getExtensions().create("nailedCredentials", CredentialsExtension.class);
    }

    @Override
    public void afterEvaluate(){
        super.afterEvaluate();

        UpdateRemoteLibraryListTask updateTask = this.makeTask("updateRemote", UpdateRemoteLibraryListTask.class);
        this.makeTask("update", FinishUpdateTask.class).dependsOn("updateRemote");

        DeployExtension ext = DeployExtension.getInstance(this.getProject());

        Class<? extends SetupTask> taskType = types.get(ext.getType());

        if(taskType == null){
            throw new RuntimeException("Unknown task type " + ext.getType());
        }

        String taskName = "setup" + firstUppercase(ext.getType());
        SetupTask task = this.makeTask(taskName, taskType);
        task.setUpdateRemoteLibraryListTask(updateTask);

        this.getProject().getTasks().getByName("updateRemote").dependsOn(taskName);

        CredentialsExtension cext = CredentialsExtension.getInstance(this.getProject());
        if(cext.getDeployServer() == null || cext.getDeployUsername() == null || cext.getDeployPassword() == null || cext.getVersionFile() == null){
            this.getProject().getLogger().warn("Credentials are not set, skipping all tasks");
        }
    }

    private static String firstUppercase(String input){
        char[] chars = input.toCharArray();
        if(chars.length > 0){
            chars[0] = Character.toUpperCase(chars[0]);
        }
        return new String(chars);
    }
}
