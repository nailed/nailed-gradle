package jk_5.nailed.gradle.common;

import com.google.common.collect.Maps;
import jk_5.nailed.gradle.delayed.DelayedFile;
import jk_5.nailed.gradle.delayed.DelayedFileTree;
import jk_5.nailed.gradle.delayed.DelayedString;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

import java.util.Map;

/**
 * No description given
 *
 * @author jk-5
 */
public abstract class BasePlugin implements Plugin<Project> {

    @Getter
    private Project project;

    @Override
    public final void apply(Project project) {
        this.project = project;

        this.addMavenRepo("reening", "http://maven.reening.nl");
        this.addMavenRepo("forge", "http://files.minecraftforge.net/maven");
        this.project.getRepositories().mavenCentral();
        this.addMavenRepo("minecraft", "http://s3.amazonaws.com/Minecraft.Download/libraries");
        this.addIvyRepo("forgeLegacy", "http://files.minecraftforge.net/[module]/[module]-dev-[revision].[ext]");

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                BasePlugin.this.afterEvaluate();
            }
        });

        this.applyPlugin();
    }

    public void afterEvaluate(){}
    public abstract void applyPlugin();

    public final void addMavenRepo(final String name, final String url){
        project.getRepositories().maven(new Action<MavenArtifactRepository>() {
            @Override
            public void execute(MavenArtifactRepository repo){
                repo.setName(name);
                repo.setUrl(url);
            }
        });
    }

    public final void addIvyRepo(final String name, final String pattern){
        project.getRepositories().ivy(new Action<IvyArtifactRepository>() {
            @Override
            public void execute(IvyArtifactRepository repo) {
                repo.setName(name);
                repo.artifactPattern(pattern);
            }
        });
    }

    public void applyExternalPlugin(String plugin){
        Map<String, Object> map = Maps.newHashMap();
        map.put("plugin", plugin);
        this.project.apply(map);
    }

    @SuppressWarnings("unchecked")
    public <T extends Task> T makeTask(String name, Class<T> type){
        Map<String, Object> map = Maps.newHashMap();
        map.put("name", name);
        map.put("type", type);
        return (T) this.getProject().task(map, name);
    }

    protected DelayedString delayedString(String path){
        return new DelayedString(project, path);
    }

    protected DelayedFile delayedFile(String path){
        return new DelayedFile(project, path);
    }

    protected DelayedFileTree delayedFileTree(String path){
        return new DelayedFileTree(project, path);
    }

    protected DelayedFileTree delayedZipTree(String path){
        return new DelayedFileTree(project, path, true);
    }
}
