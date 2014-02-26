package jk_5.nailed.gradle.json.launcher;

import java.util.List;

/**
 * No description given
 *
 * @author jk-5
 */
public class ExtractRule {

    private List<String> exclude;

    public boolean exclude(String name){
        if(exclude == null) return false;
        for (String s : exclude){
            if (name.startsWith(s)) return true;
        }
        return false;
    }
}
