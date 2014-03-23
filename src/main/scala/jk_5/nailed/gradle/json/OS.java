package jk_5.nailed.gradle.json;

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

    private OS(String name, String... aliases){
        this.name = name;
        this.aliases = aliases;
    }

    @Override
    public String toString(){
        return name;
    }
}
