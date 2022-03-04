package ch.luca008.Admin.Editor.Sockets.Packets;

import ch.luca008.Challenges;
import ch.luca008.Utils.JsonUtils;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;

public class LoginPacket implements IPacket {

    private int id = -1;
    private String key;
    private String version = "1.0.0";
    private boolean isAllowed = false;
    private String reason;

    public LoginPacket(JSONObject json){
        if(json.containsKey("ID")){
            id = JsonUtils.getInt(json,"ID");
        }
        if(json.containsKey("Key")){
            key = (String)json.get("Key");
        }
        if(json.containsKey("Version")){
            version = (String)json.get("Version");
        }
    }

    public LoginPacket(int id, boolean allowed, @Nullable String reason){
        this.id = id;
        isAllowed = allowed;
        if(!isAllowed){
            this.reason = reason;
        }
    }

    public String getKey(){
        return key;
    }

    public String getVersion(){
        return version;
    }

    @Override
    public int getID() {
        return id;
    }

    public static boolean isVersionValid(String version){
        String currentVersion = Challenges.clientVersion;
        if(version.contains(".")){
            String[] client = version.split("\\.");
            String[] current = currentVersion.split("\\.");
            if(client.length>=3 && current.length>=3){
                try{
                    int majorClient = Integer.parseInt(client[0]);
                    int majorCurrent = Integer.parseInt(current[0]);
                    int minorClient = Integer.parseInt(client[1]);
                    int minorCurrent = Integer.parseInt(current[1]);
                    return majorClient==majorCurrent&&minorClient==minorCurrent;
                }catch(NumberFormatException e){
                    System.err.println("LoginPacket: Cannot read client or current version.");
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("Allowed", isAllowed);
        if(!isAllowed&&reason!=null){
            json.put("Reason", reason);
        }
        json.put("ID", id);
        return json;
    }
}

