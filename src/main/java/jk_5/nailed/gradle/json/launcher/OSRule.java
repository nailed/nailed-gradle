package jk_5.nailed.gradle.json.launcher;

import java.util.regex.Pattern;

/**
 * No description given
 *
 * @author jk-5
 */
public class OSRule {

    public Action action = Action.ALLOW;
    public OSInfo os;

    public class OSInfo{
        private OS name;
        private String version;
    }

    public boolean applies(){
        if(os == null) return true;
        if(os.name != null && os.name != OS.getCurrentPlatform()) return false;
        if(os.version != null){
            try{
                if(!Pattern.compile(os.version).matcher(OS.VERSION).matches()){
                    return false;
                }
            }catch(Throwable e){}
        }
        return true;
    }
}
