package jk_5.nailed.gradle.delayed;

import org.gradle.api.Project;

/**
 * No description given
 *
 * @author jk-5
 */
public class DelayedString extends DelayedBase<String> {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public DelayedString(Project owner, String pattern, IDelayedResolver... resolvers){
        super(owner, pattern, resolvers);
    }

    @Override
    public String call(){
        if (this.resolved == null){
            this.resolved = DelayedBase.resolve(pattern, project, resolvers);
        }
        return this.resolved;
    }
}
