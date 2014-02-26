package jk_5.nailed.gradle;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * No description given
 *
 * @author jk-5
 */
public class Constants {

    public static final String NEWLINE = System.getProperty("line.separator");
    public static final String NAILED_EXTENSION = "nailed";
    public static final String NAILED_JSON = "{JSON_FILE}";
    public static final String DEPENDENCY_CONFIG = "nailed";
    public static final String FML_JSON_URL = "https://raw.github.com/MinecraftForge/FML/master/jsons/{MC_VERSION}-rel.json";
    public static final String MINECRAFT_URL = "https://s3.amazonaws.com/Minecraft.Download/versions/{MC_VERSION}/{MC_VERSION}.jar";
    public static final String MINECRAFT_CACHE = "{CACHE_DIR}/{MC_VERSION}/minecraft-{MC_VERSION}.jar";
    public static final String FORGE_URL = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/{MC_VERSION}-{FORGE_VERSION}/forge-{MC_VERSION}-{FORGE_VERSION}-universal.jar";
    public static final String FORGE_CACHE = "{CACHE_DIR}/{MC_VERSION}/forge-{MC_VERSION}-{FORGE_VERSION}.jar";
    public static final String CLIENT_LOCATION = "{BUILD_DIR}/libs/Nailed-Client-{CLIENT_VERSION}.jar";
    public static final String PROFILE_LOCATION = "{CACHE_DIR}/{MC_VERSION}/launcherProfile.json";
    public static final String DEPFILE_LOCATION = "{CACHE_DIR}/{MC_VERSION}/remoteDepFile.json";

    public static final String CONFIG_DEPS = "nailedDeps";
    public static final String JSON_LOCATION = "jsons/{MC_VERSION}.json";
    public static final String MINECRAFT_MAVEN_URL = "https://libraries.minecraft.net";

    public static String hash(File file){
        return hash(file, "MD5");
    }

    public static String hash(File file, String function){
        try{
            InputStream fis = new FileInputStream(file);

            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance(function);
            int numRead;

            do{
                numRead = fis.read(buffer);
                if(numRead > 0){
                    complete.update(buffer, 0, numRead);
                }
            }while(numRead != -1);

            fis.close();
            byte[] hash = complete.digest();

            String result = "";

            for(int i = 0; i < hash.length; i++){
                result += Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1);
            }
            return result;
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static String hash(String str){
        try{
            MessageDigest complete = MessageDigest.getInstance("MD5");
            byte[] hash = complete.digest(str.getBytes());

            String result = "";

            for(int i = 0; i < hash.length; i++){
                result += Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1);
            }
            return result;
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static String getSHA1(File file){
        return getDigest(file, "SHA-1", 40);
    }

    public static String getDigest(File file, String algorithm, int hashLength){
        DigestInputStream stream = null;
        try{
            stream = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance(algorithm));
            int read;
            byte[] buffer = new byte[65536];
            do{
                read = stream.read(buffer);
            }while(read > 0);
        }catch(Exception ignored){
            return null;
        }finally{
            IOUtils.closeQuietly(stream);
        }

        return String.format("%1$0" + hashLength + "x", new Object[]{new BigInteger(1, stream.getMessageDigest().digest())});
    }
}
