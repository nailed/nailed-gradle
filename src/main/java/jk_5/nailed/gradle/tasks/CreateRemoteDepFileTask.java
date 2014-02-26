package jk_5.nailed.gradle.tasks;

import jk_5.nailed.gradle.delayed.DelayedFile;
import jk_5.nailed.gradle.json.JsonFactory;
import jk_5.nailed.gradle.json.dependencies.DependencyFile;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * No description given
 *
 * @author jk-5
 */
public class CreateRemoteDepFileTask extends DefaultTask {

    @Getter @Setter private DelayedFile destination;
    @Getter @Setter private DependencyFile dependencyFile;

    @TaskAction
    public void doTask() throws IOException{
        this.dependencyFile.date = new Date();
        if(this.destination.call().isFile()) this.destination.call().delete();
        FileWriter writer = new FileWriter(this.destination.call());
        JsonFactory.gson.toJson(this.dependencyFile, writer);
        writer.close();
    }
}
