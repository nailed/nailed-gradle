package jk_5.nailed.gradle.deploy;

import groovy.lang.Closure;
import org.gradle.api.DefaultTask;

/**
 * No description given
 *
 * @author jk-5
 */
public class DeployTask extends DefaultTask {

    public DeployTask(){
        super();
        this.onlyIf(new Closure<Boolean>(this, this){
            @Override
            public Boolean call(Object... args){
                CredentialsExtension ext = CredentialsExtension.getInstance(DeployTask.this.getProject());
                return ext.getDeployServer() != null && ext.getDeployUsername() != null && ext.getDeployPassword() != null && ext.getVersionFile() != null;
            }
        });
    }
}
