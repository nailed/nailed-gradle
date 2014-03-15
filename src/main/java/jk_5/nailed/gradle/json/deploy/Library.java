package jk_5.nailed.gradle.json.deploy;

/**
 * No description given
 *
 * @author jk-5
 */
public class Library {

    public transient String name;
    public int rev;
    public String destination;
    public String location;
    public RestartLevel restart;

    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder("Library{");
        sb.append("name='").append(name).append('\'');
        sb.append(", rev=").append(rev);
        sb.append(", destination='").append(destination).append('\'');
        sb.append(", location='").append(location).append('\'');
        sb.append(", restart=").append(restart);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Library library = (Library) o;

        if(rev != library.rev) return false;
        if(destination != null ? !destination.equals(library.destination) : library.destination != null) return false;
        if(location != null ? !location.equals(library.location) : library.location != null) return false;
        if(name != null ? !name.equals(library.name) : library.name != null) return false;
        if(restart != library.restart) return false;

        return true;
    }

    @Override
    public int hashCode(){
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + rev;
        result = 31 * result + (destination != null ? destination.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (restart != null ? restart.hashCode() : 0);
        return result;
    }
}
