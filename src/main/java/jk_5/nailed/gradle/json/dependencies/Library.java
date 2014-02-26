package jk_5.nailed.gradle.json.dependencies;

import jk_5.nailed.gradle.Constants;
import jk_5.nailed.gradle.json.launcher.Action;
import jk_5.nailed.gradle.json.launcher.ExtractRule;
import jk_5.nailed.gradle.json.launcher.OS;
import jk_5.nailed.gradle.json.launcher.OSRule;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * No description given
 *
 * @author jk-5
 */
public class Library {

    public String id;
    public String name;
    public String url = null;
    public String pattern = null;
    public String tweaker;
    public boolean dev = true;
    public String[] classLoaderExclusions;
    public String[] transformerExclusions;
    public boolean launcher = false;
    public List<OSRule> rules;
    public Map<OS, String> natives;
    public ExtractRule extract;

    private transient Artifact artifact = null;
    private Action applies = null;

    public boolean applies(){
        if(applies == null){
            applies = Action.DISALLOW;
            if(rules == null){
                applies = Action.ALLOW;
            }else{
                for(OSRule rule : rules){
                    if(rule.applies()){
                        applies = rule.action;
                    }
                }
            }
        }
        return applies == Action.ALLOW;
    }

    public Artifact getArtifact(){
        if(this.artifact == null){
            this.artifact = new Artifact(this.name);
        }
        return this.artifact;
    }

    public String getPath(){
        if(this.artifact == null){
            this.artifact = new Artifact(this.name);
        }
        return this.artifact.getPath();
    }

    public String getPathNatives(){
        if(this.natives == null) return null;
        if(this.artifact == null){
            this.artifact = new Artifact(name);
        }
        return this.artifact.getPath(natives.get(OS.CURRENT));
    }

    public String getArtifactName(){
        if(this.artifact == null){
            this.artifact = new Artifact(this.name);
        }
        if(this.natives == null){
            return this.artifact.getArtifact();
        }else{
            return this.artifact.getArtifact(this.natives.get(OS.CURRENT));
        }
    }

    public String getUrl(){
        if(this.artifact == null){
            this.artifact = new Artifact(this.name);
        }
        if(this.url != null){
            return this.url;
        }
        if(this.pattern != null){
            return this.pattern.replace("{VERSION}", this.artifact.version);
        }
        return Constants.MINECRAFT_MAVEN_URL;
    }

    @Override
    public String toString(){
        return this.name;
    }

    @Getter
    public class Artifact {
        private String domain;
        private String name;
        private String version;
        private String classifier;
        private String ext = "jar";

        public Artifact(String rep){
            String[] pts = rep.split(":");
            int idx = pts[pts.length - 1].indexOf('@');
            if(idx != -1){
                ext = pts[pts.length - 1].substring(idx + 1);
                pts[pts.length - 1] = pts[pts.length - 1].substring(0, idx);
            }
            domain = pts[0];
            name = pts[1];
            version = pts[2];
            if(pts.length > 3) classifier = pts[3];
        }

        public String getArtifact(){
            return getArtifact(classifier);
        }

        public String getArtifact(String classifier){
            String ret = domain + ":" + name + ":" + version;
            if(classifier != null) ret += ":" + classifier;
            if(!"jar".equals(ext)) ret += "@" + ext;
            return ret;
        }

        public String getPath(){
            return getPath(classifier);
        }

        public String getPath(String classifier){
            String ret = String.format("%s/%s/%s/%s-%s", domain.replace('.', '/'), name, version, name, version);
            if(classifier != null) ret += "-" + classifier;
            return ret + "." + ext;
        }
    }
}
