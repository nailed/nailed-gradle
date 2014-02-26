package jk_5.nailed.gradle.tasks;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import jk_5.nailed.gradle.delayed.DelayedFile;
import jk_5.nailed.gradle.json.dependencies.DependencyFile;
import jk_5.nailed.gradle.json.dependencies.Library;
import jk_5.nailed.gradle.json.launcher.LauncherProfile;
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
public class CreateLauncherProfileTask extends DefaultTask {

    @Getter @Setter private DelayedFile destination;
    @Getter @Setter private DependencyFile dependencyFile;

    @TaskAction
    public void doTask() throws IOException, JSchException, SftpException {
        LauncherProfile profile = new LauncherProfile();
        profile.id = this.dependencyFile.profile.name;
        profile.mainClass = this.dependencyFile.profile.mainClass;
        profile.releaseTime = this.dependencyFile.profile.releaseTime;
        profile.time = new Date();
        profile.sync = false;
        profile.type = this.dependencyFile.profile.type;
        profile.minimumLauncherVersion = 13;

        StringBuilder argumentsBuilder = new StringBuilder(this.dependencyFile.profile.arguments);
        profile.libraries = Lists.newArrayList();
        for(Library library : this.dependencyFile.libraries){
            if(library.launcher){
                profile.libraries.add(library);
                if(library.tweaker != null){
                    argumentsBuilder.append(" --tweakClass ");
                    argumentsBuilder.append(library.tweaker);
                }
            }
        }
        profile.minecraftArguments = argumentsBuilder.toString();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if(this.destination.call().isFile()) this.destination.call().delete();
        FileWriter writer = new FileWriter(this.destination.call());
        gson.toJson(profile, writer);
        writer.close();
    }
}
