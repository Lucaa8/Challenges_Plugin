package ch.luca008.Admin.Editor.Sockets.Packets;

import ch.luca008.Utils.JsonUtils;
import org.json.simple.JSONObject;

public class InfoRequestPacket implements IPacket{

    private int id = -1;
    private String uuid;
    private IPacket.Target target;
    private boolean doesExist;
    private JSONObject json;

    public InfoRequestPacket(int id, boolean doesExist, JSONObject json){
        this.id = id;
        this.doesExist = doesExist;
        this.json = json;
    }

    public InfoRequestPacket(JSONObject json){
        if(json.containsKey("ID")){
            id = JsonUtils.getInt(json, "ID");
        }
        uuid = (String)json.get("UUID");
        target = Target.valueOf((String)json.get("Type"));
    }

    public String getUuid(){
        return uuid;
    }

    public Target getType(){
        return target;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("ID", id);
        json.put("Exists", doesExist);
        if(doesExist){
            json.put("JSON", this.json.toJSONString());
        }
        return json;
    }
}

