package ch.luca008.Utils;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonUtils {

    public static long getLong(JSONObject json, String key){
        return ((Long)json.get(key)).longValue();
    }

    public static int getInt(JSONObject json, String key){
        Object o = json.get(key);
        if(o instanceof Long) return ((Long)json.get(key)).intValue();
        else return (int)o;
    }

    public static int getInt(Object o){
        if(o instanceof Long) return ((Long)o).intValue();
        else return (int)o;
    }

    public static String prettyJson(JSONObject json){
        return prettyJson(json.toJSONString());
    }
    public static String prettyJson(String json){
        return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(new JsonParser().parse(json));
    }

    public static JSONObject readFile(File f) {
        if(!f.exists()){
            try {
                f.createNewFile();
            } catch (IOException e) {
                System.err.println("Can't create new file : " + f.getName());
                e.printStackTrace();
            }
            return new JSONObject();
        }
        try(BufferedReader r = Files.newBufferedReader(Paths.get(f.toURI()))){
            return (JSONObject) new JSONParser().parse(r);
        }catch(ParseException | IOException e){
            System.err.println("Can't parse file '"+f.getName()+"' to jsonobject.");
        }
        return new JSONObject();
    }

    public static File write(String path, String text){
        return write(new File(path), text);
    }

    public static File write(File f, String text){
        try{
            if(!f.exists()){
                f.createNewFile();
            }
            Files.write(Paths.get(f.toURI()), text.getBytes(StandardCharsets.UTF_8));
            return f;
        }catch(IOException e){
            System.err.println("Can't write on path " + f.getPath() + ".");
            e.printStackTrace();
        }
        return null;
    }

}
