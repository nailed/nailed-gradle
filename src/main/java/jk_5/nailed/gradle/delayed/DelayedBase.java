package jk_5.nailed.gradle.delayed;

import groovy.lang.Closure;
import net.minecraftforge.gradle.common.BaseExtension;
import net.minecraftforge.gradle.common.Constants;
import org.gradle.api.Project;

/**
 * No description given
 *
 * @author jk-5
 */
@SuppressWarnings("serial")
public abstract class DelayedBase<V> extends Closure<V> {

    @SuppressWarnings("rawtypes")
    protected IDelayedResolver[] resolvers;
    protected Project project;
    protected V resolved;
    protected String pattern;

    public static final IDelayedResolver RESOLVER = new IDelayedResolver() {

        @Override
        public String resolve(String pattern, Project project) {
            return pattern;
        }
    };

    @SuppressWarnings("unchecked")
    public DelayedBase(Project owner, String pattern) {
        this(owner, pattern, RESOLVER);
    }

    public DelayedBase(Project owner, String pattern, IDelayedResolver... resolvers) {
        super(owner);
        this.project = owner;
        this.pattern = pattern;
        this.resolvers = resolvers;
    }

    @Override
    public abstract V call();

    @Override
    public String toString() {
        return call().toString();
    }

    public static interface IDelayedResolver {
        public String resolve(String pattern, Project project);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static String resolve(String patern, Project project, IDelayedResolver... resolvers) {
        project.getLogger().info("Resolving: " + patern);
        BaseExtension minecraftExtension = (BaseExtension) project.getExtensions().getByName(Constants.EXT_NAME_MC);

        String build = "0";
        if (System.getenv().containsKey("BUILD_NUMBER")) {
            build = System.getenv("BUILD_NUMBER");
        }

        String mcVersion = minecraftExtension.getVersion().split("-")[0];

        patern = patern.replace("{MC_VERSION}", mcVersion);
        patern = patern.replace("{CACHE_DIR}", project.getGradle().getGradleUserHomeDir().getAbsolutePath().replace('\\', '/') + "/caches/nailed-forge");
        patern = patern.replace("{BUILD_DIR}", project.getBuildDir().getAbsolutePath().replace('\\', '/'));
        patern = patern.replace("{BUILD_NUM}", build);
        patern = patern.replace("{PROJECT}", project.getName());

        for (IDelayedResolver r : resolvers) {
            patern = r.resolve(patern, project);
        }

        project.getLogger().info("Resolved:  " + patern);
        return patern;
    }
}
