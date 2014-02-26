package jk_5.nailed.gradle.json.launcher;

import jk_5.nailed.gradle.json.dependencies.Library;

import java.util.Date;
import java.util.List;

/**
 * No description given
 *
 * @author jk-5
 */
public class LauncherProfile {

    public String id;
    public String mainClass;
    public int minimumLauncherVersion;
    public LauncherProfileType type;
    public Date time;
    public Date releaseTime;
    public boolean sync;
    public String minecraftArguments;
    public List<Library> libraries;
}
