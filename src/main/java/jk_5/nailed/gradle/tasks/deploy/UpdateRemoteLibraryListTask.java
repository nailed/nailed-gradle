package jk_5.nailed.gradle.tasks.deploy;

import com.google.common.collect.Lists;
import groovy.lang.Closure;
import jk_5.nailed.gradle.deploy.DeployTask;
import jk_5.nailed.gradle.json.deploy.Library;
import org.gradle.api.tasks.TaskAction;

import java.util.List;

/**
 * No description given
 *
 * @author jk-5
 */
public class UpdateRemoteLibraryListTask extends DeployTask {

    private final List<Library> librariesToUpdate = Lists.newArrayList();

    public UpdateRemoteLibraryListTask(){
        super();

        this.onlyIf(new Closure<Boolean>(this, this) {
            @Override
            public Boolean call(Object... args){
                return !UpdateRemoteLibraryListTask.this.librariesToUpdate.isEmpty();
            }
        });
    }

    @TaskAction
    public void doTask(){

    }

    public void addLibrary(Library library){
        this.librariesToUpdate.add(library);
    }
}
