package ch.luca008.Admin.Editor.Sockets.Packets;

import org.json.simple.JSONObject;
import java.lang.reflect.InvocationTargetException;

public interface IPacket {

    public enum Target{
        CATEGORY, CHALLENGE
    }

    static IPacket load(JSONObject json) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> c = null;
        if(json.containsKey("Type")){
            String clazz = (String)json.get("Type");
            c = Class.forName("ch.luca008.Admin.Editor.Sockets.Packets."+clazz.substring(clazz.lastIndexOf(".")+1));
        }
        if(c!=null&&json.containsKey("Body")){
            return (IPacket) c.getConstructor(JSONObject.class).newInstance((JSONObject)json.get("Body"));
        }
        return null;
    }

    static JSONObject getPacket(IPacket packet){
        JSONObject json = new JSONObject();
        json.put("Type", "Challenges.Packet.Packets."+packet.getClass().getSimpleName());
        json.put("Body", packet.toJson());
        return json;
    }

    int getID();
    JSONObject toJson();

}
