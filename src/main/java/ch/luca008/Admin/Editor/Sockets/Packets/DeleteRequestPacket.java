package ch.luca008.Admin.Editor.Sockets.Packets;

import ch.luca008.Utils.JsonUtils;
import org.json.simple.JSONObject;

public class DeleteRequestPacket implements IPacket{

    private int id = -1;
    private String uuid;
    private Target target;
    private boolean success;
    private String error;

    public DeleteRequestPacket(int id, boolean success, String error){
        this.id = id;
        this.success = success;
        this.error = error;
    }

    public DeleteRequestPacket(JSONObject json){
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
        json.put("Success", success);
        if(!success&&error!=null){
            json.put("ErrorMsg", error);
        }
        return json;
    }
}

