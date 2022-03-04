package ch.luca008.Admin.Editor.Sockets.Packets;

import ch.luca008.Utils.JsonUtils;
import org.json.simple.JSONObject;

public class ChallengeStatePacket implements IPacket{

    public enum Type{
        INFO, UPDATE;
    }

    private int id = -1;
    private Type type = Type.INFO;
    private boolean state;

    public ChallengeStatePacket(int id, boolean state){
        this.id = id;
        this.state = state;
    }

    public ChallengeStatePacket(JSONObject json){
        if(json.containsKey("ID")){
            id = JsonUtils.getInt(json,"ID");
        }
        type = Type.valueOf((String)json.get("Type"));
        if(type==Type.UPDATE){
            state = (Boolean)json.get("State");
        }
    }

    public Type getType(){
        return type;
    }
    public boolean getState(){
        return state;
    }
    @Override
    public int getID() {
        return id;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("ID", id);
        json.put("State", state);
        return json;
    }
}

