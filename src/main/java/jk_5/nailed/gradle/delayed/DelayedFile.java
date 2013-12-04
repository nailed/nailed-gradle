package jk_5.nailed.gradle.delayed;

import org.gradle.api.Project;

import java.io.File;

/**
 * No description given
 *
 * @author jk-5
 */
public class DelayedFile extends DelayedBase<File> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public DelayedFile(Project owner, String pattern, IDelayedResolver... resolvers){
        super(owner, pattern, resolvers);
    }

    @Override
    public File call(){
        if (resolved == null){
            resolved = project.file(DelayedBase.resolve(pattern, project, resolvers));
        }
        return resolved;
    }

    public DelayedFileTree toZipTree(){
        return new DelayedFileTree(project, pattern, true, resolvers);
    }
}
