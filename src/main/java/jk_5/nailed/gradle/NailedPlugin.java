package jk_5.nailed.gradle;

import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import jk_5.nailed.gradle.common.BasePlugin;
import jk_5.nailed.gradle.delayed.DelayedBase;
import jk_5.nailed.gradle.extension.LauncherExtension;
import jk_5.nailed.gradle.extension.NailedExtension;
import jk_5.nailed.gradle.tasks.DeployLauncherProfileTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.io.File;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;

/**
 * No description given
 *
 * @author jk-5
 */
public class NailedPlugin extends BasePlugin implements DelayedBase.IDelayedResolver {

    private boolean jsonApplied = false;

    @Override
    public void applyPlugin() {

        this.applyExternalPlugin("forge");
        this.applyExternalPlugin("maven");

        this.getProject().getExtensions().create(Constants.NAILED_EXTENSION, NailedExtension.class, this.getProject());
        this.getProject().getExtensions().create(Constants.NAILED_LAUNCHER_EXTENSION, LauncherExtension.class, this.getProject());

        this.registerLauncherTasks();
    }

    public void registerLauncherTasks(){
        DeployLauncherProfileTask task = this.makeTask("deployLauncherProfile", DeployLauncherProfileTask.class);
    }

    @Override
    public String resolve(String pattern, Project project) {
        return pattern;
    }
}
