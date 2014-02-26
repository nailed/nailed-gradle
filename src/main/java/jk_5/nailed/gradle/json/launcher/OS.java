package jk_5.nailed.gradle.json.launcher;

import java.util.Locale;

/**
 * No description given
 *
 * @author jk-5
 */
public enum OS {

    LINUX("linux", "bsd", "unix"),
    WINDOWS("windows", "win"),
    OSX("osx", "mac"),
    UNKNOWN("unknown");

    private String name;
    private String[] aliases;

    public static final OS CURRENT = getCurrentPlatform();
    public static final String VERSION = System.getProperty("os.version");

    private OS(String name, String... aliases){
        this.name = name;
        this.aliases = aliases;
    }

    public static OS getCurrentPlatform(){
        String osName = System.getProperty("os.name").toLowerCase(Locale.US);
        for (OS os : values()){
            if (osName.contains(os.name)) return os;
            for (String alias : os.aliases){
                if (osName.contains(alias)) return os;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString(){
        return name;
    }
}