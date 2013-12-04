package jk_5.nailed.gradle.delayed;

import net.minecraftforge.gradle.ZipFileTree;
import org.gradle.api.Project;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.collections.FileTreeAdapter;

/**
 * No description given
 *
 * @author jk-5
 */
public class DelayedFileTree extends DelayedBase<FileTree> {

    private boolean zipTree = false;

    public DelayedFileTree(Project owner, String pattern, boolean zipTree) {
        super(owner, pattern);
        this.zipTree = zipTree;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public DelayedFileTree(Project owner, String pattern, IDelayedResolver... resolvers) {
        super(owner, pattern, resolvers);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public DelayedFileTree(Project owner, String pattern, boolean zipTree, IDelayedResolver... resolvers) {
        super(owner, pattern, resolvers);
        this.zipTree = zipTree;
    }

    @Override
    public FileTree call() {
        if (resolved == null) {
            if (zipTree){
                resolved = new FileTreeAdapter(new ZipFileTree(project.file(DelayedBase.resolve(pattern, project, resolvers))));
            }else{
                resolved = project.fileTree(DelayedBase.resolve(pattern, project, resolvers));
            }
        }
        return resolved;
    }
}
