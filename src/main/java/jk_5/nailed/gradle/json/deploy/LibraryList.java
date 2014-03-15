package jk_5.nailed.gradle.json.deploy;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * No description given
 *
 * @author jk-5
 */
public class LibraryList {

    public List<Library> libraries = Lists.newArrayList();

    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder("LibraryList{");
        sb.append("libraries=").append(libraries);
        sb.append('}');
        return sb.toString();
    }

    public void addLibs(List<Library> librariesToUpdate, boolean updateWhenVersionSame){
        for(Library library : librariesToUpdate){
            Library local = null;
            for(Library lib : this.libraries){
                if(lib.name.equals(library.name)){
                    local = lib;
                }
            }
            if(local == null){
                this.libraries.add(library);
            }else{
                if(local.location.equals(library.location) && updateWhenVersionSame){
                    local.rev++;
                }
                local.destination = library.destination;
                local.location = library.location;
                local.restart = library.restart;
            }
        }
    }
}
